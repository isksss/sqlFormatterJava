# SQL Formatter Java VS Code Extension

This extension formats SQL documents with the portable formatter core.

## Development

```bash
npm install
npm run compile:with-wasm
npx vsce package --ignoreFile .vscodeignore --skip-license --allow-unused-files-pattern --target web
```

`compile:with-wasm` builds `../rust-core` for `wasm32-unknown-unknown` and
copies the resulting `sql_formatter_core.wasm` into `dist/wasm`.

If the Wasm file is missing during development, the extension falls back to the
TypeScript formatter shim so the extension host can still start.
