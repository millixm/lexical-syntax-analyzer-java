# Lexical and Syntax Analyzer in Java

A compiler front-end written in Java for a custom object-oriented programming language. It reads a source file, splits it into tokens (**lexical analysis**), and checks that the tokens follow the language grammar (**syntax analysis**). Errors are reported with line numbers.

## Features

- **Lexical analyzer** that reads source code character by character and produces a list of tokens, each with a type, lexeme and line number
- Recognizes 40+ keywords, identifiers, integer and decimal literals, string and character literals (with escape sequences), operators and punctuators
- Supports single-line and multi-line comments
- Flags bad input (unterminated strings, empty or unterminated character literals, unknown symbols) as `INVALID` tokens instead of crashing
- **Recursive-descent parser** that checks the token stream against the grammar, using FIRST sets to choose between rules
- Correct operator precedence for assignment, logical, relational, arithmetic, unary and postfix operators
- Clear syntax error messages with the line number and the token that caused the error

## The language

The language is object-oriented and similar to Java, with its own keywords.

| Keyword | Meaning | Keyword | Meaning |
|---|---|---|---|
| `blueprint` | class | `check` / `otherwise` | if / else |
| `inherits` | extends | `loopfor` | for loop |
| `combine` | implements | `loopwhile` | while loop |
| `pattern` | interface | `rerun ... until` | do-while loop |
| `constructor` | constructor | `pick` / `case range` / `default` | switch |
| `open` / `hidden` / `shielded` | public / private / protected | `halt` / `skip` | break / continue |
| `static` / `const` / `locked` / `override` | modifiers | `giveback` | return |
| `num` / `decimal` / `flag` / `text` / `char` / `void` | data types | `bundle` | array (1–3 dimensions) |
| `self` / `parent` | this / super | `yes` / `no` / `null` | true / false / null |
| `new` | create object or array | `end` | end of program |

**Comments:** `$` starts a single-line comment; `$$ ... $$` wraps a multi-line comment.

## Grammar

Written in EBNF: `{ }` means zero or more, `[ ]` means optional, `|` means or.

```
program      → blueprint { patternDecl } "end"

blueprint    → "blueprint" ID [ "inherits" ID { "," ID } ] [ "combine" ID { "," ID } ]
               "{" { memberDecl } "}"
patternDecl  → "pattern" ID "{" { { modifier } returnType ID "(" [ argList ] ")" ";" } "}" ";"

memberDecl   → { modifier } ( staticBlock | constructor | arrayDecl
               | ID ID ";" | returnType ID ( methodBody | fieldList ) )
modifier     → "open" | "hidden" | "shielded" | "static" | "locked" | "override" | "const"
staticBlock  → "{" { statement } "}"                       (after "static")
constructor  → "constructor" "(" [ argList ] ")" body
methodBody   → "(" [ argList ] ")" body
fieldList    → [ "=" expr ] { "," ID [ "=" expr ] } ";"
arrayDecl    → "bundle" "[" NUM "]" [ "[" NUM "]" [ "[" NUM "]" ] ] ID [ "=" initializer ] ";"
initializer  → "{" [ initElement { "," initElement } ] "}" | expr
initElement  → initializer | expr

returnType   → "void" | ( dataType | ID ) { "[" "]" }
dataType     → "num" | "decimal" | "flag" | "text" | "char"
argList      → arg { "," arg }
arg          → ( dataType | ID ) { "[" "]" } ID [ "=" expr ]

body         → "{" { statement } "}"
statement    → checkStmt | forStmt | whileStmt | pickStmt | rerunStmt
             | ( "halt" | "skip" ) ";" | "giveback" [ expr ] ";"
             | declaration | expr ";"
checkStmt    → "check" "(" expr ")" body [ "otherwise" body ]
forStmt      → "loopfor" "(" ( statement | ";" ) [ expr ] ";" [ expr ] ")" body
whileStmt    → "loopwhile" "(" expr ")" body
rerunStmt    → "rerun" body "until" "(" expr ")" ";"
pickStmt     → "pick" "(" expr ")" "{"
                 { "case" "range" "[" expr "," expr "]" ":" { statement } }
                 [ "default" ":" { statement } ] "}"

expr         → assignment
assignment   → orExpr [ ( "=" | "+=" | "-=" | "*=" | "/=" | "%=" ) assignment ]
orExpr       → andExpr { "||" andExpr }
andExpr      → relExpr { "&&" relExpr }
relExpr      → addExpr [ ( "==" | "!=" | "<" | ">" | "<=" | ">=" ) addExpr ]
addExpr      → mulExpr { ( "+" | "-" ) mulExpr }
mulExpr      → postfix { ( "*" | "/" | "%" ) postfix }
postfix      → primary { "(" [ expr { "," expr } ] ")" | "[" expr "]"
                       | "." ( ID | "constructor" ) | "++" | "--" }
primary      → ( "++" | "--" | "!" | "+" | "-" ) postfix
             | "new" ( dataType | ID ) ( "[" expr "]" | "(" [ expr { "," expr } ] ")" )
             | literal | "(" expr ")" | ID | "self" | "parent"
literal      → NUM | DECIMAL | CHAR | TEXT | "yes" | "no" | "null"
```

## Project structure

```
.
├── src/
│   └── LexicalSyntaxAnalyzer.java   # Token class, lexer, parser and SyntaxErrorException
├── .gitignore
└── README.md
```

## How to run

Requires JDK 9 or later.

```
javac src/LexicalSyntaxAnalyzer.java
java -cp src LexicalSyntaxAnalyzer path/to/code.txt
```

## Example

**Input (`code.txt`)**

```
$ A small program in the custom language
blueprint Student inherits Person {
    hidden num age = 20;
    open text name;
    bundle [3] marks = {80, 75, 90};

    constructor(text n, num a) {
        self.name = n;
        self.age = a;
    }

    open num total() {
        num sum = 0;
        loopfor (num i = 0; i < 3; i++) {
            sum += marks[i];
        }
        giveback sum;
    }

    open void result() {
        check (self.total() >= 200) {
            print("Pass");
        } otherwise {
            print("Fail");
        }
    }
}

pattern Printable {
    open void show();
};

end
```

**Output**

```
--- Lexical Analysis Starting ---
--- Lexical Analysis Finished ---

--- Syntax Analysis Starting ---
--- Syntax Analysis Finished ---

============================
  Result: Syntax is Valid
============================
```

If the code breaks a grammar rule, for example a missing `;`, the analyzer stops and reports where:

```
--- Syntax Error Detected ---
Syntax Error: Expected punctuator ';' at line 4. Found: 'open' (Type: KW_OPEN, Line: 4)
```

## Author

**Malika Fatima** — BS Software Engineering, University of Karachi (UBIT)
[LinkedIn](https://www.linkedin.com/in/malaika-fatima-633b1b27a) · [GitHub](https://github.com/millixm)
