# Lexical and Syntax Analyzer in Java

A compiler front-end project written in Java. It reads a source file, splits it
into tokens (**lexical analysis**), and then checks that the tokens follow the
language grammar (**syntax analysis**). Errors are reported with line numbers.

## Features
- Reads source code from a text file
- **Lexical analyzer** that produces a list of tokens, each with a type, lexeme and line number
- Recognizes [keywords, identifiers, integer/float literals, operators, punctuation, string and character literals]
- Flags bad input as `INVALID` tokens, for example an unterminated character literal, instead of crashing
- **Syntax analyzer** that checks the token stream against the grammar and throws a
  `SyntaxErrorException` with a clear message when the structure is wrong
- Prints the token table and the syntax result to the console

## Project structure
```
.
├── src/
│   └── LexicalSyntaxAnalyzer.java   # lexer, parser, Token and SyntaxErrorException
├── .gitignore
└── README.md
```

## How it works
1. **Lexical analysis.** `lexicalAnalysis(String filename)` reads the file
   character by character, groups characters into lexemes, and returns a
   `List<Token>`. Anything that matches no rule becomes an `INVALID` token.
2. **Syntax analysis.** The parser walks through the token list and checks it
   against the grammar below. On the first violation it throws a
   `SyntaxErrorException` that includes the line number.

### Token format
| Field  | Meaning                                   |
|--------|-------------------------------------------|
| type   | e.g. KEYWORD, IDENTIFIER, NUMBER, OPERATOR, INVALID |
| lexeme | the exact text from the source            |
| line   | line number where it appears              |

## Grammar
```
[paste your grammar rules here, for example:
 program    -> statement*
 statement  -> declaration | assignment | ifStatement | whileStatement
 declaration-> type IDENTIFIER ( '=' expression )? ';'
 ...]
```

## Requirements
- JDK 8 or later

## How to run
```
javac src/LexicalSyntaxAnalyzer.java
java -cp src LexicalSyntaxAnalyzer [input-file]
```

## Example
**Input (`test.txt`)**
```
// Source code is decompiled from a .class file using FernFlower decompiler (from Intellij IDEA).
class Token {
   String classPart;
   String valuePart;
   int lineNo;

   Token(String var1, String var2, int var3) {
      this.classPart = var1;
      this.valuePart = var2;
      this.lineNo = var3;
   }

   public String toString() {
      return "'" + this.valuePart + "' (Type: " + this.classPart + ", Line: " + this.lineNo + ")";
   }
}

```

**Output**
```
--- Lexical Analysis Starting ---
--- Lexical Analysis Finished ---

--- Lexical Analysis Tokens ---
... (193 tokens generated, including EOF) ...

--- Syntax Analysis Starting ---
--- Syntax Analysis Finished ---

============================
  Result: Syntax is Valid
============================
```


## Author
[Malaika Fatima] – [Compiler], University of Karachi (UBIT)
