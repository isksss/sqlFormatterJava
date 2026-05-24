//! Portable formatter core.
//!
//! This module intentionally exposes a narrow string/config API so the same
//! implementation can be used from native Rust tests, WebAssembly, and a future
//! Java wrapper.

#[derive(Clone, Debug, Eq, PartialEq)]
pub struct FormatterConfig {
    pub dialect: Option<String>,
    pub tab_width: usize,
    pub use_tabs: bool,
    pub keyword_case: KeywordCase,
    pub data_type_case: KeywordCase,
    pub function_case: KeywordCase,
    pub identifier_case: KeywordCase,
    pub logical_operator_newline: LogicalOperatorNewline,
    pub expression_width: Option<usize>,
    pub lines_between_queries: usize,
    pub dense_operators: bool,
    pub newline_before_semicolon: bool,
    pub error_policy: ErrorPolicy,
}

impl Default for FormatterConfig {
    fn default() -> Self {
        Self {
            dialect: None,
            tab_width: 2,
            use_tabs: false,
            keyword_case: KeywordCase::Preserve,
            data_type_case: KeywordCase::Preserve,
            function_case: KeywordCase::Preserve,
            identifier_case: KeywordCase::Preserve,
            logical_operator_newline: LogicalOperatorNewline::Before,
            expression_width: Some(50),
            lines_between_queries: 1,
            dense_operators: false,
            newline_before_semicolon: false,
            error_policy: ErrorPolicy::KeepInput,
        }
    }
}

#[derive(Clone, Copy, Debug, Eq, PartialEq)]
pub enum KeywordCase {
    Preserve,
    Upper,
    Lower,
}

#[derive(Clone, Copy, Debug, Eq, PartialEq)]
pub enum LogicalOperatorNewline {
    Before,
    After,
}

#[derive(Clone, Copy, Debug, Eq, PartialEq)]
pub enum ErrorPolicy {
    KeepInput,
    Throw,
}

#[derive(Clone, Debug, Eq, PartialEq)]
enum Token {
    Word(String),
    Number(String),
    StringLiteral(String),
    QuotedIdentifier(String),
    Parameter(String),
    Operator(String),
    Symbol(char),
    Comment(String),
}

#[derive(Debug, Eq, PartialEq)]
pub struct FormatError {
    message: String,
}

impl FormatError {
    pub fn message(&self) -> &str {
        &self.message
    }
}

pub fn format_sql(sql: &str, config: &FormatterConfig) -> Result<String, FormatError> {
    validate_config(config)?;
    let tokens = tokenize(sql)?;
    Ok(render(&tokens, config))
}

fn validate_config(config: &FormatterConfig) -> Result<(), FormatError> {
    if let Some(dialect) = &config.dialect {
        if !matches!(
            dialect.to_ascii_lowercase().as_str(),
            "postgresql"
                | "mysql"
                | "sqlite"
                | "sql"
                | "db2"
                | "mariadb"
                | "n1ql"
                | "plsql"
                | "pl/sql"
                | "redshift"
                | "spark"
                | "tsql"
        ) {
            return Err(FormatError {
                message: format!("Unknown SQL dialect: {dialect}."),
            });
        }
    }
    Ok(())
}

/// Formats SQL using the default config. This is the primary Wasm-friendly API.
pub fn format_sql_default(sql: &str) -> Result<String, String> {
    format_sql(sql, &FormatterConfig::default()).map_err(|error| error.message)
}

#[no_mangle]
pub extern "C" fn sql_formatter_alloc(len: usize) -> *mut u8 {
    let mut buffer = Vec::<u8>::with_capacity(len);
    let ptr = buffer.as_mut_ptr();
    std::mem::forget(buffer);
    ptr
}

#[no_mangle]
pub unsafe extern "C" fn sql_formatter_dealloc(ptr: *mut u8, capacity: usize) {
    if !ptr.is_null() && capacity > 0 {
        drop(Vec::from_raw_parts(ptr, 0, capacity));
    }
}

#[no_mangle]
pub unsafe extern "C" fn sql_formatter_format_default(ptr: *const u8, len: usize) -> u64 {
    if ptr.is_null() {
        return 0;
    }
    let input = std::slice::from_raw_parts(ptr, len);
    let sql = match std::str::from_utf8(input) {
        Ok(value) => value,
        Err(_) => return 0,
    };
    let output = match format_sql_default(sql) {
        Ok(value) => value,
        Err(_) => sql.to_string(),
    };
    let bytes = output.into_bytes();
    let out_len = bytes.len();
    let out_ptr = sql_formatter_alloc(out_len);
    std::ptr::copy_nonoverlapping(bytes.as_ptr(), out_ptr, out_len);
    ((out_ptr as u64) << 32) | out_len as u64
}

fn tokenize(sql: &str) -> Result<Vec<Token>, FormatError> {
    let chars: Vec<char> = sql.chars().collect();
    let mut tokens = Vec::new();
    let mut index = 0;
    while index < chars.len() {
        let current = chars[index];
        if current.is_whitespace() {
            index += 1;
            continue;
        }
        if current == '-' && chars.get(index + 1) == Some(&'-') {
            let start = index;
            index += 2;
            while index < chars.len() && chars[index] != '\n' {
                index += 1;
            }
            tokens.push(Token::Comment(chars[start..index].iter().collect()));
            continue;
        }
        if current == '/' && chars.get(index + 1) == Some(&'*') {
            let start = index;
            index += 2;
            while index + 1 < chars.len() && !(chars[index] == '*' && chars[index + 1] == '/') {
                index += 1;
            }
            if index + 1 >= chars.len() {
                return Err(FormatError {
                    message: "Unterminated block comment.".to_string(),
                });
            }
            index += 2;
            tokens.push(Token::Comment(chars[start..index].iter().collect()));
            continue;
        }
        if current == '\'' {
            let start = index;
            index += 1;
            while index < chars.len() {
                if chars[index] == '\'' {
                    if chars.get(index + 1) == Some(&'\'') {
                        index += 2;
                    } else {
                        index += 1;
                        break;
                    }
                } else {
                    index += 1;
                }
            }
            tokens.push(Token::StringLiteral(chars[start..index].iter().collect()));
            continue;
        }
        if current == '"' {
            let start = index;
            index += 1;
            while index < chars.len() {
                if chars[index] == '"' {
                    if chars.get(index + 1) == Some(&'"') {
                        index += 2;
                    } else {
                        index += 1;
                        break;
                    }
                } else {
                    index += 1;
                }
            }
            tokens.push(Token::QuotedIdentifier(chars[start..index].iter().collect()));
            continue;
        }
        if current == '`' {
            let start = index;
            index += 1;
            while index < chars.len() {
                if chars[index] == '`' {
                    index += 1;
                    break;
                }
                index += 1;
            }
            tokens.push(Token::QuotedIdentifier(chars[start..index].iter().collect()));
            continue;
        }
        if current == ':' && chars.get(index + 1) == Some(&':') {
            tokens.push(Token::Operator("::".to_string()));
            index += 2;
            continue;
        }
        if current == '$' && chars.get(index + 1) == Some(&'$') {
            let start = index;
            index += 2;
            while index + 1 < chars.len() {
                if chars[index] == '$' && chars[index + 1] == '$' {
                    index += 2;
                    break;
                }
                index += 1;
            }
            tokens.push(Token::StringLiteral(chars[start..index].iter().collect()));
            continue;
        }
        if current == ':' || current == '$' || current == '?' {
            let start = index;
            index += 1;
            while index < chars.len() && is_identifier_part(chars[index]) {
                index += 1;
            }
            tokens.push(Token::Parameter(chars[start..index].iter().collect()));
            continue;
        }
        if current.is_ascii_digit() {
            let start = index;
            index += 1;
            while index < chars.len() && (chars[index].is_ascii_digit() || chars[index] == '.') {
                index += 1;
            }
            tokens.push(Token::Number(chars[start..index].iter().collect()));
            continue;
        }
        if is_identifier_start(current) {
            let start = index;
            index += 1;
            while index < chars.len() && is_identifier_part(chars[index]) {
                index += 1;
            }
            tokens.push(Token::Word(chars[start..index].iter().collect()));
            continue;
        }
        if let Some(operator) = read_operator(&chars, index) {
            index += operator.chars().count();
            tokens.push(Token::Operator(operator));
            continue;
        }
        if matches!(current, ',' | '(' | ')' | '[' | ']' | ';') {
            tokens.push(Token::Symbol(current));
            index += 1;
            continue;
        }
        tokens.push(Token::Symbol(current));
        index += 1;
    }
    Ok(tokens)
}

fn render(tokens: &[Token], config: &FormatterConfig) -> String {
    let indent = if config.use_tabs {
        "\t".to_string()
    } else {
        " ".repeat(config.tab_width.max(1))
    };
    let mut out = String::new();
    let mut line_start = true;
    let mut pending_space = false;
    let mut indent_level = 0usize;
    let mut logical_indent_level = 1usize;
    let mut paren_stack = Vec::new();
    let mut index = 0;

    while index < tokens.len() {
        let token = &tokens[index];
        match token {
            Token::Comment(value) => {
                newline(&mut out, &mut line_start, &mut pending_space);
                let comment_indent = if out.is_empty() {
                    0
                } else if logical_indent_level > 0 {
                    indent_level + logical_indent_level
                } else {
                    indent_level
                };
                push_indent(&mut out, &indent, comment_indent, &mut line_start);
                out.push_str(value);
                newline(&mut out, &mut line_start, &mut pending_space);
            }
            Token::Symbol(',') => {
                trim_space(&mut out);
                out.push(',');
                if paren_stack.last() == Some(&ParenKind::FunctionCall) {
                    out.push(' ');
                    pending_space = false;
                } else {
                    newline(&mut out, &mut line_start, &mut pending_space);
                    let comma_indent = if paren_stack.last() == Some(&ParenKind::CreateTable) {
                        indent_level
                    } else {
                        indent_level + 1
                    };
                    push_indent(&mut out, &indent, comma_indent, &mut line_start);
                }
            }
            Token::Symbol(';') => {
                trim_space(&mut out);
                if config.newline_before_semicolon {
                    newline(&mut out, &mut line_start, &mut pending_space);
                }
                out.push(';');
                if index + 1 < tokens.len() {
                    trim_space(&mut out);
                    out.push_str(&"\n".repeat(config.lines_between_queries.max(1)));
                    line_start = true;
                    pending_space = false;
                }
            }
            Token::Symbol('(') => {
                let paren_kind = paren_kind(tokens, index, config);
                push_pending(&mut out, &mut pending_space, &indent, indent_level, &mut line_start);
                if current_line_has_text(&out) && !previous_word_is(tokens, index, "as") && !previous_word_is(tokens, index, "on") {
                    trim_space(&mut out);
                }
                out.push('(');
                if paren_kind.is_block() {
                    indent_level += 1;
                    newline(&mut out, &mut line_start, &mut pending_space);
                    if paren_kind == ParenKind::Block {
                        push_indent(&mut out, &indent, indent_level + 1, &mut line_start);
                    }
                }
                paren_stack.push(paren_kind);
                pending_space = false;
            }
            Token::Symbol('[') => {
                push_pending(&mut out, &mut pending_space, &indent, indent_level, &mut line_start);
                trim_space(&mut out);
                out.push_str(" [ ");
                paren_stack.push(ParenKind::FunctionCall);
                pending_space = false;
            }
            Token::Symbol(']') => {
                if paren_stack.pop() == Some(ParenKind::Block) {
                    indent_level = indent_level.saturating_sub(1);
                }
                trim_space(&mut out);
                out.push_str(" ]");
                pending_space = true;
            }
            Token::Symbol(')') => {
                match paren_stack.pop() {
                    Some(ParenKind::Block) => {
                        newline(&mut out, &mut line_start, &mut pending_space);
                        push_indent(&mut out, &indent, indent_level, &mut line_start);
                        indent_level = indent_level.saturating_sub(1);
                        logical_indent_level = 1;
                    }
                    Some(ParenKind::CreateTable | ParenKind::Cte) => {
                        indent_level = indent_level.saturating_sub(1);
                        newline(&mut out, &mut line_start, &mut pending_space);
                        push_indent(&mut out, &indent, indent_level, &mut line_start);
                    }
                    _ => {
                        trim_space(&mut out);
                    }
                }
                out.push(')');
                pending_space = true;
            }
            Token::Operator(operator) => {
                if operator == "." {
                    trim_space(&mut out);
                    out.push_str(operator);
                    pending_space = false;
                } else if config.dense_operators && operator != "::" && operator != "->" && operator != "->>" {
                    trim_space(&mut out);
                    out.push_str(operator);
                    pending_space = false;
                } else {
                    pending_space = true;
                    push_pending(&mut out, &mut pending_space, &indent, indent_level, &mut line_start);
                    out.push_str(operator);
                    pending_space = true;
                }
            }
            Token::Word(word) if is_join_modifier(tokens, index) => {
                newline(&mut out, &mut line_start, &mut pending_space);
                push_indent(&mut out, &indent, indent_level + 1, &mut line_start);
                out.push_str(&apply_case(word, config.keyword_case));
                out.push(' ');
                if let Some(Token::Word(join)) = tokens.get(index + 1) {
                    out.push_str(&apply_case(join, config.keyword_case));
                }
                pending_space = true;
                index += 1;
            }
            Token::Word(word) if word.eq_ignore_ascii_case("join") => {
                newline(&mut out, &mut line_start, &mut pending_space);
                push_indent(&mut out, &indent, indent_level + 1, &mut line_start);
                out.push_str(&apply_case(word, config.keyword_case));
                pending_space = true;
            }
            Token::Word(word) if word.eq_ignore_ascii_case("on") && is_join_on(tokens, index) => {
                newline(&mut out, &mut line_start, &mut pending_space);
                push_indent(&mut out, &indent, indent_level + 2, &mut line_start);
                out.push_str(&apply_case(word, config.keyword_case));
                pending_space = true;
                logical_indent_level = 2;
            }
            Token::Word(word) if is_compound_clause_start(tokens, index) => {
                newline(&mut out, &mut line_start, &mut pending_space);
                let block_extra = if paren_stack.contains(&ParenKind::Block) {
                    1
                } else {
                    0
                };
                let clause_indent = indent_level + block_extra;
                push_indent(&mut out, &indent, clause_indent, &mut line_start);
                out.push_str(&apply_case(word, config.keyword_case));
                out.push(' ');
                if let Some(Token::Word(next)) = tokens.get(index + 1) {
                    out.push_str(&apply_case(next, config.keyword_case));
                }
                if word.eq_ignore_ascii_case("partition") {
                    pending_space = true;
                } else {
                    newline(&mut out, &mut line_start, &mut pending_space);
                    push_indent(&mut out, &indent, clause_indent + 1, &mut line_start);
                }
                logical_indent_level = 1;
                index += 1;
            }
            Token::Word(word) if is_clause_keyword(word) => {
                newline(&mut out, &mut line_start, &mut pending_space);
                let block_extra = if paren_stack.contains(&ParenKind::Block) {
                    1
                } else {
                    0
                };
                let clause_indent = indent_level + block_extra;
                push_indent(&mut out, &indent, clause_indent, &mut line_start);
                out.push_str(&apply_case(word, config.keyword_case));
                newline(&mut out, &mut line_start, &mut pending_space);
                push_indent(&mut out, &indent, clause_indent + 1, &mut line_start);
                logical_indent_level = 1;
            }
            Token::Word(word) if is_between_and(tokens, index) => {
                pending_space = true;
                push_pending(&mut out, &mut pending_space, &indent, indent_level, &mut line_start);
                out.push_str(&apply_case(word, config.keyword_case));
                pending_space = true;
            }
            Token::Word(word) if is_logical_operator(word) => match config.logical_operator_newline {
                LogicalOperatorNewline::Before => {
                    newline(&mut out, &mut line_start, &mut pending_space);
                    let effective_logical_indent = if paren_stack.contains(&ParenKind::Block) {
                        1
                    } else {
                        logical_indent_level
                    };
                    push_indent(
                        &mut out,
                        &indent,
                        indent_level + effective_logical_indent,
                        &mut line_start,
                    );
                    out.push_str(&apply_case(word, config.keyword_case));
                    pending_space = true;
                }
                LogicalOperatorNewline::After => {
                    pending_space = true;
                    push_pending(&mut out, &mut pending_space, &indent, indent_level, &mut line_start);
                    out.push_str(&apply_case(word, config.keyword_case));
                    newline(&mut out, &mut line_start, &mut pending_space);
                    let effective_logical_indent = if paren_stack.contains(&ParenKind::Block) {
                        1
                    } else {
                        logical_indent_level
                    };
                    push_indent(
                        &mut out,
                        &indent,
                        indent_level + effective_logical_indent,
                        &mut line_start,
                    );
                }
            },
            Token::Word(word) if word.eq_ignore_ascii_case("case") => {
                newline(&mut out, &mut line_start, &mut pending_space);
                push_indent(&mut out, &indent, indent_level + 1, &mut line_start);
                out.push_str(&apply_case(word, config.keyword_case));
                pending_space = false;
            }
            Token::Word(word)
                if word.eq_ignore_ascii_case("when") || word.eq_ignore_ascii_case("else") =>
            {
                newline(&mut out, &mut line_start, &mut pending_space);
                push_indent(&mut out, &indent, indent_level + 2, &mut line_start);
                out.push_str(&apply_case(word, config.keyword_case));
                pending_space = true;
            }
            Token::Word(word) if word.eq_ignore_ascii_case("end") => {
                newline(&mut out, &mut line_start, &mut pending_space);
                push_indent(&mut out, &indent, indent_level + 1, &mut line_start);
                out.push_str(&apply_case(word, config.keyword_case));
                pending_space = true;
            }
            Token::Word(_) => {
                push_pending(&mut out, &mut pending_space, &indent, indent_level, &mut line_start);
                out.push_str(&format_word(tokens, index, config));
                pending_space = true;
            }
            Token::Number(value)
            | Token::StringLiteral(value)
            | Token::QuotedIdentifier(value)
            | Token::Parameter(value) => {
                push_pending(&mut out, &mut pending_space, &indent, indent_level, &mut line_start);
                out.push_str(value);
                pending_space = true;
            }
            Token::Symbol(symbol) => {
                push_pending(&mut out, &mut pending_space, &indent, indent_level, &mut line_start);
                out.push(*symbol);
                pending_space = true;
            }
        }
        index += 1;
    }
    out.trim_matches('\n').trim_end().to_string()
}

fn is_identifier_start(value: char) -> bool {
    value.is_ascii_alphabetic() || value == '_'
}

fn is_identifier_part(value: char) -> bool {
    value.is_ascii_alphanumeric() || value == '_'
}

fn read_operator(chars: &[char], index: usize) -> Option<String> {
    for width in [3usize, 2, 1] {
        if index + width <= chars.len() {
            let candidate: String = chars[index..index + width].iter().collect();
            if matches!(
                candidate.as_str(),
                "->>" | "::" | ">=" | "<=" | "<>" | "!=" | "==" | "||" | "->" | "." | "=" | ">" | "<" | "+" | "-"
                    | "*" | "/" | "%"
            ) {
                return Some(candidate);
            }
        }
    }
    None
}

fn is_clause_keyword(word: &str) -> bool {
    matches!(
        word.to_ascii_lowercase().as_str(),
        "select"
            | "from"
            | "where"
            | "order"
            | "group"
            | "having"
            | "limit"
            | "offset"
            | "values"
            | "with"
            | "insert"
            | "update"
            | "set"
            | "delete"
            | "add"
    )
}

fn is_compound_clause_start(tokens: &[Token], index: usize) -> bool {
    let Some(Token::Word(current)) = tokens.get(index) else {
        return false;
    };
    let Some(Token::Word(next)) = tokens.get(index + 1) else {
        return false;
    };
    matches!(
        (current.to_ascii_lowercase().as_str(), next.to_ascii_lowercase().as_str()),
        ("order", "by") | ("group", "by")
            | ("partition", "by")
            | ("delete", "from")
            | ("insert", "into")
            | ("alter", "table")
            | ("alter", "column")
    )
}

#[derive(Clone, Copy, Debug, Eq, PartialEq)]
enum ParenKind {
    FunctionCall,
    Block,
    CreateTable,
    Cte,
}

impl ParenKind {
    fn is_block(self) -> bool {
        matches!(self, ParenKind::Block | ParenKind::CreateTable | ParenKind::Cte)
    }
}

fn paren_kind(tokens: &[Token], index: usize, config: &FormatterConfig) -> ParenKind {
    if previous_word_is(tokens, index, "on") {
        return ParenKind::Block;
    }
    if is_create_table_columns(tokens, index) {
        return ParenKind::CreateTable;
    }
    match tokens.get(index.saturating_sub(1)) {
        Some(Token::Word(word)) if word.eq_ignore_ascii_case("as") => ParenKind::Cte,
        Some(Token::Word(_)) if config.expression_width.is_some_and(|width| width <= 5) => ParenKind::Block,
        Some(Token::Word(word)) if !is_non_function_paren_word(word) => ParenKind::FunctionCall,
        _ => ParenKind::Block,
    }
}

fn is_create_table_columns(tokens: &[Token], index: usize) -> bool {
    if !matches!(tokens.get(index.saturating_sub(1)), Some(Token::Word(_))) {
        return false;
    }
    let mut saw_table = false;
    let mut saw_qualified_name = false;
    for token in tokens[..index].iter().rev() {
        match token {
            Token::Symbol(';') => break,
            Token::Symbol('(') => return false,
            Token::Operator(operator) if operator == "." => saw_qualified_name = true,
            Token::Word(word) if word.eq_ignore_ascii_case("table") => saw_table = true,
            Token::Word(word) if saw_table && word.eq_ignore_ascii_case("create") => return saw_qualified_name,
            _ => {}
        }
    }
    false
}

fn is_non_function_paren_word(word: &str) -> bool {
    matches!(
        word.to_ascii_lowercase().as_str(),
        "in" | "as" | "over" | "where" | "select" | "from"
    )
}

fn previous_word_is(tokens: &[Token], index: usize, expected: &str) -> bool {
    matches!(tokens.get(index.saturating_sub(1)), Some(Token::Word(word)) if word.eq_ignore_ascii_case(expected))
}

fn is_on_conflict(tokens: &[Token], index: usize) -> bool {
    matches!(tokens.get(index + 1), Some(Token::Word(next)) if next.eq_ignore_ascii_case("conflict"))
}

fn is_join_on(tokens: &[Token], index: usize) -> bool {
    if is_on_conflict(tokens, index) {
        return false;
    }
    tokens[..index].iter().rev().any(|token| match token {
        Token::Symbol(';') => false,
        Token::Word(word) if is_clause_keyword(word) && !word.eq_ignore_ascii_case("join") => false,
        Token::Word(word) if word.eq_ignore_ascii_case("join") => true,
        _ => false,
    })
}

fn is_between_and(tokens: &[Token], index: usize) -> bool {
    if !matches!(tokens.get(index), Some(Token::Word(word)) if word.eq_ignore_ascii_case("and")) {
        return false;
    }
    for token in tokens[..index].iter().rev() {
        match token {
            Token::Word(word) if word.eq_ignore_ascii_case("between") => return true,
            Token::Word(word) if is_logical_operator(word) || is_clause_keyword(word) => return false,
            _ => {}
        }
    }
    false
}

fn is_join_modifier(tokens: &[Token], index: usize) -> bool {
    let Some(Token::Word(current)) = tokens.get(index) else {
        return false;
    };
    let Some(Token::Word(next)) = tokens.get(index + 1) else {
        return false;
    };
    matches!(
        (current.to_ascii_lowercase().as_str(), next.to_ascii_lowercase().as_str()),
        ("left", "join")
            | ("right", "join")
            | ("full", "join")
            | ("inner", "join")
            | ("cross", "join")
            | ("outer", "join")
    )
}

fn is_logical_operator(word: &str) -> bool {
    matches!(word.to_ascii_lowercase().as_str(), "and" | "or")
}

fn format_word(tokens: &[Token], index: usize, config: &FormatterConfig) -> String {
    let Some(Token::Word(word)) = tokens.get(index) else {
        return String::new();
    };
    let case = if is_boolean_literal(word) || is_sql_keyword_word(word) || is_comma_continued_select_item(tokens, index) {
        config.keyword_case
    } else if is_data_type(word) {
        config.data_type_case
    } else if matches!(tokens.get(index + 1), Some(Token::Symbol('('))) {
        config.function_case
    } else {
        config.identifier_case
    };
    apply_case(word, case)
}

fn is_boolean_literal(word: &str) -> bool {
    matches!(word.to_ascii_lowercase().as_str(), "true" | "false")
}

fn is_sql_keyword_word(word: &str) -> bool {
    matches!(
        word.to_ascii_lowercase().as_str(),
        "create" | "table" | "if" | "not" | "exists" | "primary" | "key" | "default" | "null"
    )
}

fn is_comma_continued_select_item(tokens: &[Token], index: usize) -> bool {
    if !matches!(tokens.get(index.saturating_sub(1)), Some(Token::Symbol(','))) {
        return false;
    }
    for token in tokens[..index].iter().rev() {
        match token {
            Token::Word(word) if word.eq_ignore_ascii_case("from") => return false,
            Token::Word(word) if word.eq_ignore_ascii_case("select") => return true,
            _ => {}
        }
    }
    false
}

fn is_data_type(word: &str) -> bool {
    matches!(
        word.to_ascii_lowercase().as_str(),
        "bigint"
            | "boolean"
            | "bool"
            | "date"
            | "decimal"
            | "double"
            | "integer"
            | "int"
            | "json"
            | "jsonb"
            | "numeric"
            | "real"
            | "text"
            | "timestamp"
            | "timestamptz"
            | "uuid"
            | "varchar"
    )
}

fn apply_case(word: &str, keyword_case: KeywordCase) -> String {
    match keyword_case {
        KeywordCase::Preserve => word.to_string(),
        KeywordCase::Upper => word.to_ascii_uppercase(),
        KeywordCase::Lower => word.to_ascii_lowercase(),
    }
}

fn push_pending(
    out: &mut String,
    pending_space: &mut bool,
    indent: &str,
    indent_level: usize,
    line_start: &mut bool,
) {
    push_indent(out, indent, indent_level, line_start);
    if *pending_space && !out.ends_with([' ', '\n', '(', '.']) {
        out.push(' ');
    }
    *pending_space = false;
}

fn push_indent(out: &mut String, indent: &str, indent_level: usize, line_start: &mut bool) {
    if *line_start {
        for _ in 0..indent_level {
            out.push_str(indent);
        }
        *line_start = false;
    }
}

fn newline(out: &mut String, line_start: &mut bool, pending_space: &mut bool) {
    trim_space(out);
    if !out.is_empty() && !out.ends_with('\n') {
        out.push('\n');
    }
    *line_start = true;
    *pending_space = false;
}

fn trim_space(out: &mut String) {
    while out.ends_with(' ') || out.ends_with('\t') {
        out.pop();
    }
}

fn current_line_has_text(out: &str) -> bool {
    out.rsplit('\n')
        .next()
        .is_some_and(|line| !line.trim().is_empty())
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn formats_basic_select() {
        let sql = "select id,name from users where active=true and role='admin';";

        let formatted = format_sql(sql, &FormatterConfig::default()).unwrap();

        assert_eq!(
            "select\n  id,\n  name\nfrom\n  users\nwhere\n  active = true\n  and role = 'admin';",
            formatted
        );
    }

    #[test]
    fn preserves_comments_and_quoted_tokens() {
        let sql = "select \"display Name\", 'a and b' -- keep\nfrom users";

        let formatted = format_sql(sql, &FormatterConfig::default()).unwrap();

        assert!(formatted.contains("\"display Name\""));
        assert!(formatted.contains("'a and b'"));
        assert!(formatted.contains("-- keep"));
    }

    #[test]
    fn can_change_keyword_case() {
        let config = FormatterConfig {
            keyword_case: KeywordCase::Upper,
            ..FormatterConfig::default()
        };

        let formatted = format_sql("select id from users", &config).unwrap();

        assert!(formatted.starts_with("SELECT"));
    }

    #[test]
    fn matches_java_postgresql_fixtures() {
        let fixture_dir = std::path::Path::new(env!("CARGO_MANIFEST_DIR"))
            .join("../core/src/test/resources/fixtures/postgresql");
        for name in [
            "alter", "create", "cte", "delete", "insert", "lexical", "select", "update",
        ] {
            let input = std::fs::read_to_string(fixture_dir.join(format!("{name}.input.sql"))).unwrap();
            let expected = std::fs::read_to_string(fixture_dir.join(format!("{name}.expected.sql"))).unwrap();
            let formatted = format_sql(
                &input,
                &FormatterConfig {
                    dialect: Some("postgresql".to_string()),
                    error_policy: ErrorPolicy::Throw,
                    ..FormatterConfig::default()
                },
            )
            .unwrap();

            assert_eq!(expected.trim_end(), formatted, "fixture {name}");
        }
    }

    #[test]
    fn supports_java_option_cases() {
        assert_eq!(
            "select\n    id,\n    name\nfrom\n    users\nwhere\n    active = true",
            format_sql(
                "select id,name from users where active=true",
                &FormatterConfig {
                    dialect: Some("postgresql".to_string()),
                    tab_width: 4,
                    error_policy: ErrorPolicy::Throw,
                    ..FormatterConfig::default()
                },
            )
            .unwrap()
        );

        assert_eq!(
            "select\n\tid\nfrom\n\tusers",
            format_sql(
                "select id from users",
                &FormatterConfig {
                    dialect: Some("postgresql".to_string()),
                    use_tabs: true,
                    error_policy: ErrorPolicy::Throw,
                    ..FormatterConfig::default()
                },
            )
            .unwrap()
        );

        assert_eq!(
            "SELECT\n  COALESCE(NAME, 'x') LABEL\nFROM\n  USERS",
            format_sql(
                "select coalesce(name,'x') label from users",
                &FormatterConfig {
                    dialect: Some("postgresql".to_string()),
                    keyword_case: KeywordCase::Upper,
                    data_type_case: KeywordCase::Upper,
                    function_case: KeywordCase::Upper,
                    identifier_case: KeywordCase::Upper,
                    error_policy: ErrorPolicy::Throw,
                    ..FormatterConfig::default()
                },
            )
            .unwrap()
        );

        assert_eq!(
            "select\n  id\nfrom\n  users\nwhere\n  a+b>=10\n;\nselect\n  2\n;",
            format_sql(
                "select id from users where a+b>=10; select 2;",
                &FormatterConfig {
                    dialect: Some("postgresql".to_string()),
                    dense_operators: true,
                    newline_before_semicolon: true,
                    error_policy: ErrorPolicy::Throw,
                    ..FormatterConfig::default()
                },
            )
            .unwrap()
        );
    }

    #[test]
    fn rejects_unknown_dialect() {
        assert!(format_sql(
            "select 1",
            &FormatterConfig {
                dialect: Some("unknown".to_string()),
                error_policy: ErrorPolicy::Throw,
                ..FormatterConfig::default()
            },
        )
        .is_err());
    }

    #[test]
    fn covers_dialects_and_formatter_option_matrix() {
        let dialects = [
            None,
            Some("postgresql"),
            Some("mysql"),
            Some("sqlite"),
            Some("sql"),
            Some("mariadb"),
            Some("redshift"),
            Some("spark"),
            Some("tsql"),
        ];
        let tab_widths = [2usize, 4];
        let use_tabs_values = [false, true];
        let keyword_cases = [KeywordCase::Preserve, KeywordCase::Upper, KeywordCase::Lower];
        let logical_positions = [LogicalOperatorNewline::Before, LogicalOperatorNewline::After];
        let expression_widths = [None, Some(5usize), Some(50usize)];
        let lines_between_queries_values = [1usize, 2];
        let dense_operator_values = [false, true];
        let newline_before_semicolon_values = [false, true];
        let sqls = [
            "select id,name from users where active=true and score>=10; select count(*) from users;",
            "select coalesce(name,'x') label from users where created_at between '2026-01-01' and '2026-01-31'",
            "insert into audit_events(event_id,payload,tags) values($1,'{\"kind\":\"login\"}'::jsonb,array['auth','ok'])",
            "select `user`.id from `user` where `user`.active=1",
        ];

        let mut checked = 0usize;
        for dialect in dialects {
            for tab_width in tab_widths {
                for use_tabs in use_tabs_values {
                    for keyword_case in keyword_cases {
                        for data_type_case in keyword_cases {
                            for function_case in keyword_cases {
                                for identifier_case in keyword_cases {
                                    for logical_operator_newline in logical_positions {
                                        for expression_width in expression_widths {
                                            for lines_between_queries in lines_between_queries_values {
                                                for dense_operators in dense_operator_values {
                                                    for newline_before_semicolon in
                                                        newline_before_semicolon_values
                                                    {
                                                        let config = FormatterConfig {
                                                            dialect: dialect.map(str::to_string),
                                                            tab_width,
                                                            use_tabs,
                                                            keyword_case,
                                                            data_type_case,
                                                            function_case,
                                                            identifier_case,
                                                            logical_operator_newline,
                                                            expression_width,
                                                            lines_between_queries,
                                                            dense_operators,
                                                            newline_before_semicolon,
                                                            error_policy: ErrorPolicy::Throw,
                                                        };
                                                        for sql in sqls {
                                                            let formatted = format_sql(sql, &config).unwrap();
                                                            assert!(!formatted.trim().is_empty());
                                                            assert_eq!(
                                                                formatted,
                                                                format_sql(&formatted, &config).unwrap(),
                                                                "idempotence failed for dialect={dialect:?} tab_width={tab_width} use_tabs={use_tabs} keyword_case={keyword_case:?} data_type_case={data_type_case:?} function_case={function_case:?} identifier_case={identifier_case:?} logical={logical_operator_newline:?} expression_width={expression_width:?} lines={lines_between_queries} dense={dense_operators} semicolon={newline_before_semicolon} sql={sql}"
                                                            );
                                                            checked += 1;
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        assert_eq!(559_872, checked);
    }
}
