use std::env;
use std::io::{self, Read};
use std::process::ExitCode;

use sql_formatter_core::{
    format_sql, ErrorPolicy, FormatterConfig, KeywordCase, LogicalOperatorNewline,
};

fn main() -> ExitCode {
    match run() {
        Ok(output) => {
            print!("{output}");
            ExitCode::SUCCESS
        }
        Err(message) => {
            eprintln!("sql-formatter-rust-core: {message}");
            ExitCode::from(1)
        }
    }
}

fn run() -> Result<String, String> {
    let mut config = FormatterConfig::default();
    let mut args = env::args().skip(1);
    while let Some(arg) = args.next() {
        match arg.as_str() {
            "--dialect" => config.dialect = Some(next_value(&mut args, &arg)?),
            "--tab-width" => config.tab_width = next_value(&mut args, &arg)?
                .parse::<usize>()
                .map_err(|_| format!("Invalid integer for {arg}."))?,
            "--use-tabs" => {
                config.use_tabs = next_value(&mut args, &arg)?
                    .parse::<bool>()
                    .map_err(|_| format!("Invalid boolean for {arg}."))?;
            }
            "--keyword-case" => {
                config.keyword_case = match next_value(&mut args, &arg)?.as_str() {
                    "preserve" => KeywordCase::Preserve,
                    "upper" => KeywordCase::Upper,
                    "lower" => KeywordCase::Lower,
                    value => return Err(format!("Invalid keyword case: {value}.")),
                };
            }
            "--data-type-case" => {
                config.data_type_case = parse_case(&next_value(&mut args, &arg)?, "data type case")?;
            }
            "--function-case" => {
                config.function_case = parse_case(&next_value(&mut args, &arg)?, "function case")?;
            }
            "--identifier-case" => {
                config.identifier_case = parse_case(&next_value(&mut args, &arg)?, "identifier case")?;
            }
            "--logical-operator-newline" => {
                config.logical_operator_newline = match next_value(&mut args, &arg)?.as_str() {
                    "before" => LogicalOperatorNewline::Before,
                    "after" => LogicalOperatorNewline::After,
                    value => return Err(format!("Invalid logical operator newline: {value}.")),
                };
            }
            "--expression-width" => {
                config.expression_width = Some(
                    next_value(&mut args, &arg)?
                        .parse::<usize>()
                        .map_err(|_| format!("Invalid integer for {arg}."))?,
                );
            }
            "--lines-between-queries" => {
                config.lines_between_queries = next_value(&mut args, &arg)?
                    .parse::<usize>()
                    .map_err(|_| format!("Invalid integer for {arg}."))?;
            }
            "--dense-operators" => {
                config.dense_operators = next_value(&mut args, &arg)?
                    .parse::<bool>()
                    .map_err(|_| format!("Invalid boolean for {arg}."))?;
            }
            "--newline-before-semicolon" => {
                config.newline_before_semicolon = next_value(&mut args, &arg)?
                    .parse::<bool>()
                    .map_err(|_| format!("Invalid boolean for {arg}."))?;
            }
            "--error-policy" => {
                config.error_policy = match next_value(&mut args, &arg)?.as_str() {
                    "keep-input" => ErrorPolicy::KeepInput,
                    "throw" => ErrorPolicy::Throw,
                    value => return Err(format!("Invalid error policy: {value}.")),
                };
            }
            option if option.starts_with("--") => {
                let _ = next_value(&mut args, option)?;
            }
            value => return Err(format!("Unexpected positional argument: {value}.")),
        }
    }

    let mut sql = String::new();
    io::stdin()
        .read_to_string(&mut sql)
        .map_err(|error| error.to_string())?;
    match format_sql(&sql, &config) {
        Ok(output) => Ok(output),
        Err(_) if config.error_policy == ErrorPolicy::KeepInput => Ok(sql),
        Err(error) => Err(error.message().to_string()),
    }
}

fn next_value(args: &mut impl Iterator<Item = String>, option: &str) -> Result<String, String> {
    args.next()
        .ok_or_else(|| format!("Missing value for {option}."))
}

fn parse_case(value: &str, name: &str) -> Result<KeywordCase, String> {
    match value {
        "preserve" => Ok(KeywordCase::Preserve),
        "upper" => Ok(KeywordCase::Upper),
        "lower" => Ok(KeywordCase::Lower),
        value => Err(format!("Invalid {name}: {value}.")),
    }
}
