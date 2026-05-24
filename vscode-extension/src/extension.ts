import * as vscode from 'vscode';

type FormatterConfig = {
  dialect: string | null;
  tabWidth: number;
  useTabs: boolean;
  keywordCase: 'preserve' | 'upper' | 'lower';
  logicalOperatorNewline: 'before' | 'after';
  errorPolicy: 'keep-input' | 'throw';
};

export function activate(context: vscode.ExtensionContext): void {
  const formatter = new SqlDocumentFormatter();
  context.subscriptions.push(
    vscode.languages.registerDocumentFormattingEditProvider({ language: 'sql' }, formatter),
    vscode.languages.registerDocumentRangeFormattingEditProvider({ language: 'sql' }, formatter),
    vscode.commands.registerCommand('sqlFormatterJava.formatDocument', async () => {
      await vscode.commands.executeCommand('editor.action.formatDocument');
    }),
  );
}

export function deactivate(): void {}

class SqlDocumentFormatter
  implements vscode.DocumentFormattingEditProvider, vscode.DocumentRangeFormattingEditProvider
{
  provideDocumentFormattingEdits(document: vscode.TextDocument): vscode.ProviderResult<vscode.TextEdit[]> {
    const fullRange = new vscode.Range(
      document.positionAt(0),
      document.positionAt(document.getText().length),
    );
    return this.format(document, fullRange);
  }

  provideDocumentRangeFormattingEdits(
    document: vscode.TextDocument,
    range: vscode.Range,
  ): vscode.ProviderResult<vscode.TextEdit[]> {
    return this.format(document, range);
  }

  private async format(document: vscode.TextDocument, range: vscode.Range): Promise<vscode.TextEdit[]> {
    const input = document.getText(range);
    const config = loadConfig(document);
    try {
      const formatted = await formatSql(input, config);
      return formatted === input ? [] : [vscode.TextEdit.replace(range, formatted)];
    } catch (error) {
      if (config.errorPolicy === 'keep-input') {
        return [];
      }
      throw error;
    }
  }
}

function loadConfig(document: vscode.TextDocument): FormatterConfig {
  const section = vscode.workspace.getConfiguration('sqlFormatterJava', document.uri);
  return {
    dialect: section.get<string | null>('dialect', null),
    tabWidth: section.get<number>('tabWidth', 2),
    useTabs: section.get<boolean>('useTabs', false),
    keywordCase: section.get<FormatterConfig['keywordCase']>('keywordCase', 'preserve'),
    logicalOperatorNewline: section.get<FormatterConfig['logicalOperatorNewline']>(
      'logicalOperatorNewline',
      'before',
    ),
    errorPolicy: section.get<FormatterConfig['errorPolicy']>('errorPolicy', 'keep-input'),
  };
}

async function formatSql(sql: string, config: FormatterConfig): Promise<string> {
  const wasm = await loadWasmFormatter();
  if (wasm) {
    return wasm.format(sql);
  }
  const tokens = tokenize(sql);
  return render(tokens, config);
}

type WasmFormatter = {
  format(sql: string): string;
};

type WasmExports = {
  memory: WebAssembly.Memory;
  sql_formatter_alloc(len: number): number;
  sql_formatter_dealloc(ptr: number, len: number): void;
  sql_formatter_format_default(ptr: number, len: number): bigint;
};

let wasmFormatter: Promise<WasmFormatter | null> | null = null;

async function loadWasmFormatter(): Promise<WasmFormatter | null> {
  wasmFormatter ??= instantiateWasmFormatter();
  return wasmFormatter;
}

async function instantiateWasmFormatter(): Promise<WasmFormatter | null> {
  try {
    const extension = vscode.extensions.getExtension('isksss.sql-formatter-java-vscode');
    if (!extension) {
      return null;
    }
    const wasmUri = vscode.Uri.joinPath(
      extension.extensionUri,
      'dist',
      'wasm',
      'sql_formatter_core.wasm',
    );
    const bytes = await vscode.workspace.fs.readFile(wasmUri);
    const instance = await WebAssembly.instantiate(bytes, {});
    const exports = instance.exports as unknown as WasmExports;
    const encoder = new TextEncoder();
    const decoder = new TextDecoder();
    return {
      format(sql: string): string {
        const input = encoder.encode(sql);
        const inputPtr = exports.sql_formatter_alloc(input.length);
        new Uint8Array(exports.memory.buffer, inputPtr, input.length).set(input);
        const packed = exports.sql_formatter_format_default(inputPtr, input.length);
        exports.sql_formatter_dealloc(inputPtr, input.length);
        const outputPtr = Number(packed >> 32n);
        const outputLen = Number(packed & 0xffff_ffffn);
        if (outputPtr === 0 || outputLen === 0) {
          return sql;
        }
        const output = decoder.decode(new Uint8Array(exports.memory.buffer, outputPtr, outputLen));
        exports.sql_formatter_dealloc(outputPtr, outputLen);
        return output;
      },
    };
  } catch {
    return null;
  }
}

type Token =
  | { kind: 'word'; value: string }
  | { kind: 'number'; value: string }
  | { kind: 'literal'; value: string }
  | { kind: 'operator'; value: string }
  | { kind: 'symbol'; value: string }
  | { kind: 'comment'; value: string };

function tokenize(sql: string): Token[] {
  const tokens: Token[] = [];
  let index = 0;
  while (index < sql.length) {
    const current = sql[index];
    if (/\s/.test(current)) {
      index += 1;
      continue;
    }
    if (sql.startsWith('--', index)) {
      const end = sql.indexOf('\n', index);
      const next = end === -1 ? sql.length : end;
      tokens.push({ kind: 'comment', value: sql.slice(index, next) });
      index = next;
      continue;
    }
    if (sql.startsWith('/*', index)) {
      const end = sql.indexOf('*/', index + 2);
      const next = end === -1 ? sql.length : end + 2;
      tokens.push({ kind: 'comment', value: sql.slice(index, next) });
      index = next;
      continue;
    }
    if (current === '\'' || current === '"') {
      const quote = current;
      let next = index + 1;
      while (next < sql.length) {
        if (sql[next] === quote) {
          if (sql[next + 1] === quote) {
            next += 2;
            continue;
          }
          next += 1;
          break;
        }
        next += 1;
      }
      tokens.push({ kind: 'literal', value: sql.slice(index, next) });
      index = next;
      continue;
    }
    const operator = readOperator(sql, index);
    if (operator) {
      tokens.push({ kind: 'operator', value: operator });
      index += operator.length;
      continue;
    }
    if (/[(),;]/.test(current)) {
      tokens.push({ kind: 'symbol', value: current });
      index += 1;
      continue;
    }
    if (/[:$?]/.test(current)) {
      const match = /^[:$?][A-Za-z0-9_]*/.exec(sql.slice(index));
      const value = match?.[0] ?? current;
      tokens.push({ kind: 'literal', value });
      index += value.length;
      continue;
    }
    if (/[0-9]/.test(current)) {
      const match = /^[0-9.]+/.exec(sql.slice(index));
      const value = match?.[0] ?? current;
      tokens.push({ kind: 'number', value });
      index += value.length;
      continue;
    }
    if (/[A-Za-z_]/.test(current)) {
      const match = /^[A-Za-z_][A-Za-z0-9_]*/.exec(sql.slice(index));
      const value = match?.[0] ?? current;
      tokens.push({ kind: 'word', value });
      index += value.length;
      continue;
    }
    tokens.push({ kind: 'symbol', value: current });
    index += 1;
  }
  return tokens;
}

function render(tokens: Token[], config: FormatterConfig): string {
  const indent = config.useTabs ? '\t' : ' '.repeat(Math.max(1, config.tabWidth));
  const lines: string[] = [];
  let current = '';
  let indentLevel = 0;

  const pushLine = () => {
    const trimmed = current.trimEnd();
    if (trimmed.length > 0) {
      lines.push(trimmed);
    }
    current = '';
  };
  const append = (value: string) => {
    if (current.length === 0) {
      current = indent.repeat(indentLevel);
    } else if (!current.endsWith(' ') && !current.endsWith('(') && value !== ',' && value !== ';') {
      current += ' ';
    }
    current += value;
  };

  for (const token of tokens) {
    if (token.kind === 'comment') {
      pushLine();
      current = indent.repeat(indentLevel) + token.value;
      pushLine();
      continue;
    }
    if (token.kind === 'symbol' && token.value === ',') {
      current = current.trimEnd() + ',';
      pushLine();
      continue;
    }
    if (token.kind === 'symbol' && token.value === ';') {
      current = current.trimEnd() + ';';
      continue;
    }
    if (token.kind === 'symbol' && token.value === '(') {
      append('(');
      indentLevel += 1;
      continue;
    }
    if (token.kind === 'symbol' && token.value === ')') {
      indentLevel = Math.max(0, indentLevel - 1);
      current = current.trimEnd() + ')';
      continue;
    }
    if (token.kind === 'word' && isClauseKeyword(token.value)) {
      pushLine();
      current = keyword(token.value, config.keywordCase);
      pushLine();
      current = indent.repeat(indentLevel + 1);
      continue;
    }
    if (token.kind === 'word' && isLogicalOperator(token.value)) {
      if (config.logicalOperatorNewline === 'before') {
        pushLine();
        current = indent.repeat(indentLevel + 1) + keyword(token.value, config.keywordCase);
      } else {
        append(keyword(token.value, config.keywordCase));
        pushLine();
        current = indent.repeat(indentLevel + 1);
      }
      continue;
    }
    if (token.kind === 'operator' && (token.value === '.' || token.value === '::')) {
      current = current.trimEnd() + token.value;
      continue;
    }
    append(token.kind === 'word' ? keyword(token.value, config.keywordCase) : token.value);
  }
  pushLine();
  return lines.join('\n');
}

function readOperator(sql: string, index: number): string | null {
  for (const operator of ['->>', '::', '>=', '<=', '<>', '!=', '==', '||', '->']) {
    if (sql.startsWith(operator, index)) {
      return operator;
    }
  }
  return /^[=><+\-*/%]/.test(sql[index]) ? sql[index] : null;
}

function isClauseKeyword(value: string): boolean {
  return [
    'select',
    'from',
    'where',
    'order',
    'group',
    'having',
    'limit',
    'offset',
    'values',
    'returning',
    'with',
    'insert',
    'update',
    'delete',
  ].includes(value.toLowerCase());
}

function isLogicalOperator(value: string): boolean {
  return ['and', 'or'].includes(value.toLowerCase());
}

function keyword(value: string, keywordCase: FormatterConfig['keywordCase']): string {
  switch (keywordCase) {
    case 'upper':
      return value.toUpperCase();
    case 'lower':
      return value.toLowerCase();
    default:
      return value;
  }
}
