import java.io.*;
import java.util.*;
import java.util.regex.*;

class Token {
    String classPart; String valuePart; int lineNo;
    Token(String cp, String vp, int line) { this.classPart = cp; this.valuePart = vp; this.lineNo = line; }
    @Override public String toString() { return "'" + valuePart + "' (Type: " + classPart + ", Line: " + lineNo + ")"; }
}
class SyntaxErrorException extends RuntimeException {
    public SyntaxErrorException(String message, int lineNo, String currentTokenInfo) { super("Syntax Error: " + message + " at line " + lineNo + ". Found: " + currentTokenInfo); }
    public SyntaxErrorException(String message) { super("Syntax Error: " + message); }
}

public class LexicalSyntaxAnalyzer {


    private static final Map<String, String> keywords = new HashMap<>();
    private static final Map<String, String> operators = new HashMap<>();
    private static final Map<String, String> punctuators = new HashMap<>();
    private static final Pattern charConstantPattern = Pattern.compile("^'(\\\\[btnfr'\"\\\\]|[^\\\\'])'$");

    static {
        keywords.put("open", "KW_OPEN"); keywords.put("hidden", "KW_HIDDEN"); keywords.put("shielded", "KW_SHIELDED");
        keywords.put("blueprint", "KW_BLUEPRINT"); keywords.put("inherits", "KW_INHERITS"); keywords.put("combine", "KW_COMBINE");
        keywords.put("constructor", "KW_CONSTRUCTOR"); keywords.put("pattern", "KW_PATTERN"); keywords.put("end", "KW_END");
        keywords.put("num", "DT_NUM"); keywords.put("decimal", "DT_DECIMAL"); keywords.put("flag", "DT_FLAG");
        keywords.put("text", "DT_TEXT"); keywords.put("char", "DT_CHAR"); keywords.put("void", "DT_VOID");
        keywords.put("bundle", "KW_BUNDLE"); keywords.put("check", "KW_CHECK"); keywords.put("otherwise", "KW_OTHERWISE");
        keywords.put("loopwhile", "KW_LOOPWHILE"); keywords.put("loopfor", "KW_LOOPFOR");
        keywords.put("pick", "KW_PICK"); keywords.put("case", "KW_CASE"); keywords.put("default", "KW_DEFAULT");
        keywords.put("range", "KW_RANGE"); keywords.put("rerun", "KW_RERUN"); keywords.put("until", "KW_UNTIL");
        keywords.put("halt", "KW_HALT"); keywords.put("skip", "KW_SKIP"); keywords.put("giveback", "KW_GIVEBACK");
        keywords.put("static", "KW_STATIC"); keywords.put("const", "KW_CONST");
        keywords.put("locked", "KW_LOCKED"); keywords.put("override", "KW_OVERRIDE");
        keywords.put("null", "LIT_NULL"); keywords.put("yes", "LIT_TRUE"); keywords.put("no", "LIT_FALSE");
        keywords.put("self", "KW_SELF"); keywords.put("parent", "KW_PARENT");
        keywords.put("new", "KW_NEW");
        operators.put("==", "OP_REL1"); operators.put("!=", "OP_REL1"); operators.put("<=", "OP_REL2");
        operators.put(">=", "OP_REL2"); operators.put("<", "OP_REL2"); operators.put(">", "OP_REL2");
        operators.put("&&", "OP_AND"); operators.put("||", "OP_OR"); operators.put("+=", "OP_UPDATE");
        operators.put("-=", "OP_UPDATE"); operators.put("*=", "OP_UPDATE"); operators.put("/=", "OP_UPDATE");
        operators.put("%=", "OP_UPDATE"); operators.put("++", "OP_INCDEC"); operators.put("--", "OP_INCDEC");
        operators.put("+", "OP_PM"); operators.put("-", "OP_PM"); operators.put("*", "OP_MOM");
        operators.put("/", "OP_MOM"); operators.put("%", "OP_MOM"); operators.put("!", "OP_NOT");
        operators.put("=", "OP_ASSIGN"); operators.put(".", "DOT");
        punctuators.put(";", "SEMICOLON"); punctuators.put(",", "COMMA"); punctuators.put(":", "COLON");
        punctuators.put("(", "LPAREN"); punctuators.put(")", "RPAREN"); punctuators.put("{", "LBRACE");
        punctuators.put("}", "RBRACE"); punctuators.put("[", "LBRACKET"); punctuators.put("]", "RBRACKET");
    }

    public static List<Token> lexicalAnalysis(String filename) throws IOException { /* Unchanged and correct */
        List<Token> tokens = new ArrayList<>(); int lineNumber = 1;
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line; boolean multiLineComment = false;
            while ((line = br.readLine()) != null) {
                int i = 0;
                while (i < line.length()) {
                    if (multiLineComment) { if (line.charAt(i) == '$' && i + 1 < line.length() && line.charAt(i + 1) == '$') { multiLineComment = false; i += 2; } else { i++; } continue; }
                    if (line.charAt(i) == '$') { if (i + 1 < line.length() && line.charAt(i + 1) == '$') { multiLineComment = true; i += 2; continue; } else { break; } }
                    char ch = line.charAt(i); String currentLexeme;
                    if (Character.isWhitespace(ch)) { i++; continue; }
                    if (ch == '"') { StringBuilder sb = new StringBuilder(); sb.append(ch); i++; while (i < line.length()) { char nextChar = line.charAt(i); if (nextChar == '\\' && i + 1 < line.length()) { sb.append(nextChar); i++; sb.append(line.charAt(i)); i++; } else if (nextChar == '"') { sb.append(nextChar); i++; break; } else { sb.append(nextChar); i++; } } currentLexeme = sb.toString(); if (currentLexeme.length() >= 2 && currentLexeme.endsWith("\"")) { tokens.add(new Token("LIT_TEXT", currentLexeme, lineNumber)); } else { tokens.add(new Token("INVALID", currentLexeme + " <Unterminated String>", lineNumber)); } continue; }
                    if (ch == '\'') { StringBuilder sb = new StringBuilder(); sb.append(ch); i++; if (i < line.length()) { char charContent = line.charAt(i); if (charContent == '\\' && i + 1 < line.length()) { sb.append(charContent); i++; sb.append(line.charAt(i)); i++; } else if (charContent != '\'') { sb.append(charContent); i++; } else { tokens.add(new Token("INVALID", sb.toString() + " <Empty Char>", lineNumber)); i++; continue; } if (i < line.length() && line.charAt(i) == '\'') { sb.append('\''); i++; currentLexeme = sb.toString(); if (charConstantPattern.matcher(currentLexeme).matches()) { tokens.add(new Token("LIT_CHAR", currentLexeme, lineNumber)); } else { tokens.add(new Token("INVALID", currentLexeme, lineNumber)); } } else { tokens.add(new Token("INVALID", sb.toString() + " <Unterminated Char>", lineNumber)); } } else { tokens.add(new Token("INVALID", sb.toString() + " <Unterminated Char>", lineNumber)); } continue; }
                    boolean found = false; String twoCharOp = (i + 1 < line.length()) ? line.substring(i, i + 2) : null; String oneChar = line.substring(i, i + 1);
                    if (twoCharOp != null && operators.containsKey(twoCharOp)) { tokens.add(new Token(operators.get(twoCharOp), twoCharOp, lineNumber)); i += 2; found = true; }
                    else if (operators.containsKey(oneChar)) { tokens.add(new Token(operators.get(oneChar), oneChar, lineNumber)); i++; found = true; }
                    else if (punctuators.containsKey(oneChar)) { tokens.add(new Token(punctuators.get(oneChar), oneChar, lineNumber)); i++; found = true; }
                    if(found) continue;
                    if (Character.isDigit(ch) || (ch == '.' && i + 1 < line.length() && Character.isDigit(line.charAt(i+1)))) { StringBuilder sb = new StringBuilder(); boolean hasDecimal = (ch == '.'); if(hasDecimal) sb.append('0'); sb.append(ch); i++; while (i < line.length()) { char numChar = line.charAt(i); if (Character.isDigit(numChar)) { sb.append(numChar); i++; } else if (numChar == '.' && !hasDecimal) { sb.append(numChar); i++; hasDecimal = true; } else { break; } } currentLexeme = sb.toString(); if (hasDecimal) { tokens.add(new Token("LIT_DECIMAL", currentLexeme, lineNumber)); } else { tokens.add(new Token("LIT_NUM", currentLexeme, lineNumber)); } continue; }
                    if (Character.isLetter(ch) || ch == '_') { StringBuilder sb = new StringBuilder(); sb.append(ch); i++; while (i < line.length() && (Character.isLetterOrDigit(line.charAt(i)) || line.charAt(i) == '_')) { sb.append(line.charAt(i)); i++; } currentLexeme = sb.toString(); if (keywords.containsKey(currentLexeme)) { tokens.add(new Token(keywords.get(currentLexeme), currentLexeme, lineNumber)); } else { tokens.add(new Token("ID", currentLexeme, lineNumber)); } continue; }
                    tokens.add(new Token("INVALID", String.valueOf(ch), lineNumber)); i++;
                }
                lineNumber++;
            }
        }
        tokens.add(new Token("EOF", "$", lineNumber)); return tokens;
    }

    // ======================================================================
    // ==================== SYNTAX ANALYZER =================================
    // ======================================================================

    private final List<Token> tokens;
    private int currentTokenIndex;
    private Token currentToken;

    private static final Set<String> FIRST_DataType = Set.of("DT_NUM", "DT_CHAR", "DT_DECIMAL", "DT_FLAG", "DT_TEXT");
    private static final Set<String> FIRST_Constant = Set.of("LIT_NUM", "LIT_CHAR", "LIT_DECIMAL", "LIT_TRUE", "LIT_FALSE", "LIT_TEXT", "LIT_NULL");
    private static final Set<String> FIRST_Expression = Set.of("ID", "LIT_NUM", "LIT_CHAR", "LIT_DECIMAL", "LIT_TRUE", "LIT_FALSE", "LIT_TEXT", "LIT_NULL", "OP_NOT", "OP_INCDEC", "KW_SELF", "KW_PARENT", "LPAREN", "KW_NEW", "OP_PM");
    private static final Set<String> ALL_MEMBER_MODIFIERS = Set.of("KW_OPEN", "KW_HIDDEN", "KW_SHIELDED", "KW_STATIC", "KW_LOCKED", "KW_OVERRIDE", "KW_CONST");

    public LexicalSyntaxAnalyzer(List<Token> tokens) {
        if (tokens == null || tokens.isEmpty()) { throw new IllegalArgumentException("FATAL: Token stream is empty."); } this.tokens = tokens; this.currentTokenIndex = 0; this.currentToken = tokens.get(0); if (!this.tokens.get(this.tokens.size() - 1).classPart.equals("EOF")) { throw new IllegalArgumentException("FATAL: Token stream must end with an EOF token."); }
    }

    private void nextToken() { if (currentTokenIndex < tokens.size() - 1) { currentTokenIndex++; currentToken = tokens.get(currentTokenIndex); } }
    private void match(String expectedTokenType) { if (check(expectedTokenType)) nextToken(); else throw new SyntaxErrorException("Expected token type '" + expectedTokenType + "'", currentToken.lineNo, currentToken.toString()); }
    private void matchKeyword(String keyword) { if (checkKeyword(keyword)) nextToken(); else throw new SyntaxErrorException("Expected keyword '" + keyword + "'", currentToken.lineNo, currentToken.toString()); }
    private void matchPunctuator(String punc) { String type = punctuators.get(punc); if (type != null && check(type)) nextToken(); else throw new SyntaxErrorException("Expected punctuator '" + punc + "'", currentToken.lineNo, currentToken.toString()); }
    private boolean check(String tokenType) { return currentToken.classPart.equals(tokenType); }
    private boolean checkKeyword(String keyword) { return currentToken.valuePart.equals(keyword); }
    private Token peek(int k) { int index = currentTokenIndex + k; if (index < tokens.size()) return tokens.get(index); return tokens.get(tokens.size() - 1); }

    // --- Top-Level Parsing ---
    public void parse() {
        if (checkKeyword("blueprint")) {
            parseBlueprint();
        } else {
            throw new SyntaxErrorException("A file must start with a 'blueprint' declaration", currentToken.lineNo, currentToken.toString());
        }
        while (checkKeyword("pattern")) {
            parsePatternDecl();
        }
        matchKeyword("end");
        if (!check("EOF")) throw new SyntaxErrorException("Unexpected token after 'end'", currentToken.lineNo, currentToken.toString());
    }

    // --- Declaration Parsing ---
    private void parseBlueprint() { matchKeyword("blueprint"); match("ID"); parseInheritanceClause(); parseImplementsClause(); matchPunctuator("{"); while (!check("RBRACE")) parseDeclaration(true); matchPunctuator("}"); }
    private void parseInheritanceClause() { if (checkKeyword("inherits")) { nextToken(); match("ID"); while (check(punctuators.get(","))) { nextToken(); match("ID"); } } }
    private void parseImplementsClause() { if (checkKeyword("combine")) { nextToken(); match("ID"); while (check(punctuators.get(","))) { nextToken(); match("ID"); } } }

    private void parseDeclaration(boolean isMemberLevel) {
        Set<String> modifiers = new HashSet<>();
        while (ALL_MEMBER_MODIFIERS.contains(currentToken.classPart)) {
            if (!isMemberLevel && currentToken.classPart.equals("KW_CONST")) { throw new SyntaxErrorException("'const' is only allowed for member declarations", currentToken.lineNo, currentToken.toString()); }
            modifiers.add(currentToken.classPart); nextToken();
        }
        if (isMemberLevel && modifiers.contains("KW_STATIC") && check(punctuators.get("{"))) { parseStaticBlock(); return; }
        if (isMemberLevel && checkKeyword("constructor")) { parseConstructor(); return; }
        if (checkKeyword("bundle")) { parseArrayDecl(modifiers.contains("KW_CONST")); return; }
        if (isMemberLevel && check("ID") && peek(1).classPart.equals("ID") && peek(2).classPart.equals("SEMICOLON")) { match("ID"); match("ID"); matchPunctuator(";"); return; }
        parseReturnType();
        match("ID");
        if (check(punctuators.get("("))) { if (modifiers.contains("KW_CONST")) throw new SyntaxErrorException("Methods cannot be 'const'", currentToken.lineNo, ""); parseMethodBody(); }
        else { parseFieldList(modifiers.contains("KW_CONST")); }
    }

    private void parseStaticBlock() { matchPunctuator("{"); parseStatements(Set.of("RBRACE")); matchPunctuator("}"); }
    private void parseMethodBody() { matchPunctuator("("); parseArgsList(); matchPunctuator(")"); parseBody(); }
    private void parseFieldList(boolean isConst) { parseInitial(isConst); while (check(punctuators.get(","))) { nextToken(); match("ID"); parseInitial(isConst); } matchPunctuator(";"); }
    private void parseReturnType() { if (checkKeyword("void")) { nextToken(); } else if (FIRST_DataType.contains(currentToken.classPart) || check("ID")) { nextToken(); while (check(punctuators.get("["))) { nextToken(); matchPunctuator("]"); } } else { throw new SyntaxErrorException("Expected return type (void, data type, or class ID)", currentToken.lineNo, currentToken.toString()); } }
    private void parseInitial(boolean mustInitialize) { if (mustInitialize) { match("OP_ASSIGN"); parseExpression(); } else if (check("OP_ASSIGN")) { nextToken(); parseExpression(); } }
    private void parseArrayDecl(boolean isConst) { matchKeyword("bundle"); parseDimensions(); match("ID"); if (isConst) { match("OP_ASSIGN"); parseInitializer(); } else { parseArrayInit(); } matchPunctuator(";"); }
    private void parseDimensions() { int dimCount = 0; while (check(punctuators.get("["))) { dimCount++; nextToken(); parseCapacity(); matchPunctuator("]"); } if (dimCount == 0 || dimCount > 3) throw new SyntaxErrorException("Array must have 1, 2, or 3 dimensions", currentToken.lineNo, ""); }
    private void parseCapacity() { match("LIT_NUM"); }
    private void parseArrayInit() { if (check("OP_ASSIGN")) { nextToken(); parseInitializer(); } }
    private void parseInitializer() { if (check(punctuators.get("{"))) { matchPunctuator("{"); if (!check(punctuators.get("}"))) { parseInitializerElement(); while (check(punctuators.get(","))) { nextToken(); parseInitializerElement(); } } matchPunctuator("}"); } else if (isExpressionStart()) { parseExpression(); } else { throw new SyntaxErrorException("Expected initializer list '{...}' or expression", currentToken.lineNo, currentToken.toString()); } }
    private void parseInitializerElement() { if (check(punctuators.get("{"))) { parseInitializer(); } else { parseExpression(); } }
    private void parseConstructor() { matchKeyword("constructor"); matchPunctuator("("); parseArgsList(); matchPunctuator(")"); parseBody(); }
    private void parseArgsList() { if (isArgStart()) { parseArg(); while (check(punctuators.get(","))) { nextToken(); parseArg(); } } }
    private boolean isArgStart() { return FIRST_DataType.contains(currentToken.classPart) || check("ID"); }
    private void parseArg() { parseArgType(); while (check(punctuators.get("["))) { nextToken(); matchPunctuator("]"); } match("ID"); if (check("OP_ASSIGN")) { nextToken(); parseExpression(); } }
    private void parseArgType() { if (FIRST_DataType.contains(currentToken.classPart) || check("ID")) nextToken(); else throw new SyntaxErrorException("Expected parameter type", currentToken.lineNo, currentToken.toString()); }
    private void parsePatternDecl() { matchKeyword("pattern"); match("ID"); matchPunctuator("{"); while (!check("RBRACE")) { while (ALL_MEMBER_MODIFIERS.contains(currentToken.classPart)) nextToken(); parseReturnType(); match("ID"); matchPunctuator("("); parseArgsList(); matchPunctuator(")"); matchPunctuator(";"); } matchPunctuator("}"); matchPunctuator(";"); }

    // --- Statement Parsing ---
    private void parseBody() { matchPunctuator("{"); parseStatements(Set.of("RBRACE")); matchPunctuator("}"); }
    private void parseStatements(Set<String> stopTokens) { while (!stopTokens.contains(currentToken.classPart) && !check("EOF") && !stopTokens.contains(currentToken.valuePart)) parseStatement(); }

    private void parseStatement() {
        if (checkKeyword("check")) { parseCheckStmt(); return; }
        if (checkKeyword("loopfor")) { parseLoopForStmt(); return; }
        if (checkKeyword("loopwhile")) { parseLoopWhileStmt(); return; }
        if (checkKeyword("pick")) { parsePickStmt(); return; }
        if (checkKeyword("rerun")) { parseRerunStmt(); return; }
        if (checkKeyword("halt") || checkKeyword("skip")) { parseHaltSkip(); return; }
        if (checkKeyword("giveback")) { parseReturnStmt(); return; }
        if (isDeclarationStart()) { parseDeclaration(false); } else { parseExpression(); matchPunctuator(";"); }
    }

    private boolean isDeclarationStart() {
        if (ALL_MEMBER_MODIFIERS.contains(currentToken.classPart) || FIRST_DataType.contains(currentToken.classPart) || checkKeyword("bundle") || checkKeyword("void")) {
            return true;
        }
        if (check("ID")) {
            return peek(1).classPart.equals("ID") || (peek(1).classPart.equals("LBRACKET") && peek(2).classPart.equals("RBRACKET"));
        }
        return false;
    }

    private void parseCheckStmt() { matchKeyword("check"); matchPunctuator("("); parseExpression(); matchPunctuator(")"); parseBody(); if (checkKeyword("otherwise")) { nextToken(); parseBody(); } }
    private void parseLoopForStmt() { matchKeyword("loopfor"); matchPunctuator("("); parseInitStmt(); parseCondStmt(); matchPunctuator(";"); parseIncStmt(); matchPunctuator(")"); parseBody(); }
    private void parseInitStmt() { if (check("SEMICOLON")) { nextToken(); return; } parseStatement(); }
    private void parseCondStmt() { if (!check("SEMICOLON")) parseExpression(); }
    private void parseIncStmt() { if (!check("RPAREN")) parseExpression(); }
    private void parseLoopWhileStmt() { matchKeyword("loopwhile"); matchPunctuator("("); parseExpression(); matchPunctuator(")"); parseBody(); }
    private void parsePickStmt() { matchKeyword("pick"); matchPunctuator("("); parseExpression(); matchPunctuator(")"); matchPunctuator("{"); while (checkKeyword("case")) parseCaseBlock(); if (checkKeyword("default")) parseDefaultCase(); matchPunctuator("}"); }
    private void parseCaseBlock() { matchKeyword("case"); matchKeyword("range"); matchPunctuator("["); parseExpression(); matchPunctuator(","); parseExpression(); matchPunctuator("]"); matchPunctuator(":"); parseStatements(Set.of("KW_CASE", "KW_DEFAULT", "RBRACE")); }
    private void parseDefaultCase() { matchKeyword("default"); matchPunctuator(":"); parseStatements(Set.of("RBRACE")); }
    private void parseRerunStmt() { matchKeyword("rerun"); parseBody(); matchKeyword("until"); matchPunctuator("("); parseExpression(); matchPunctuator(")"); matchPunctuator(";"); }
    private void parseHaltSkip() { if (checkKeyword("halt") || checkKeyword("skip")) { nextToken(); matchPunctuator(";"); } }
    private void parseReturnStmt() { matchKeyword("giveback"); if (isExpressionStart()) parseExpression(); matchPunctuator(";"); }

    // --- Expression Parsing Hierarchy ---
    private boolean isExpressionStart() { return FIRST_Expression.contains(currentToken.classPart); }
    private void parseExpression() { parseAssignment(); }
    private void parseAssignment() { parseOrExpr(); if (check("OP_ASSIGN") || check("OP_UPDATE")) { nextToken(); parseAssignment(); } }
    private void parseOrExpr() { parseAndExpr(); while (check("OP_OR")) { nextToken(); parseAndExpr(); } }
    private void parseAndExpr() { parseRelExpr(); while (check("OP_AND")) { nextToken(); parseRelExpr(); } }
    private void parseRelExpr() { parseAddExpr(); if (check("OP_REL1") || check("OP_REL2")) { nextToken(); parseAddExpr(); } }
    private void parseAddExpr() { parseMulExpr(); while (check("OP_PM")) { nextToken(); parseMulExpr(); } }
    private void parseMulExpr() { parsePostfixExpression(); while (check("OP_MOM")) { nextToken(); parsePostfixExpression(); } }

    // ** THE FIX IS HERE: A robust, unified postfix expression parser **
    private void parsePostfixExpression() {
        parsePrimary(); // First, parse the base of the expression
        // Then, loop to parse any chain of postfix operators
        while (true) {
            if (check("LPAREN")) {
                nextToken();
                parseArgValues();
                matchPunctuator(")");
            } else if (check("LBRACKET")) {
                nextToken();
                parseExpression();
                matchPunctuator("]");
            } else if (check("DOT")) {
                parseDotChain();
            } else if (check("OP_INCDEC")){
                nextToken(); // Postfix ++ or --
            } else {
                break; // No more postfix operators
            }
        }
    }

    private void parsePrimary() {
        if (check("OP_INCDEC") || check("OP_NOT") || check("OP_PM")) { // Pre-increment, NOT, unary minus/plus
            parseUnary();
        } else if (check("KW_NEW")) {
            parseNewObject();
        } else if (FIRST_Constant.contains(currentToken.classPart)) {
            nextToken();
        } else if (check("LPAREN")) {
            nextToken();
            parseExpression();
            matchPunctuator(")");
        } else if (checkKeyword("self") || checkKeyword("parent") || check("ID")) {
            nextToken(); // This handles a simple ID, or the start of a self/parent chain
        } else {
            throw new SyntaxErrorException("Unexpected token in expression", currentToken.lineNo, currentToken.toString());
        }
    }

    private void parseUnary() {
        nextToken(); // Consume unary operator
        parsePostfixExpression(); // The operator applies to the whole postfix expression that follows
    }

    private void parseDotChain() {
        match("DOT");
        if (checkKeyword("constructor")) {
            nextToken();
        } else {
            match("ID");
        }
    }

    private void parseNewObject() {
        matchKeyword("new");
        // A 'new' can be for a custom type (ID) or a primitive array (DT_...).
        if (FIRST_DataType.contains(currentToken.classPart) || check("ID")) {
            nextToken();
        } else {
            throw new SyntaxErrorException("Expected data type or class ID after 'new'", currentToken.lineNo, currentToken.toString());
        }

        if (check("LBRACKET")) { // Array creation: new type[size]
            match("LBRACKET");
            parseExpression();
            match("RBRACKET");
        } else if (check("LPAREN")) { // Object instantiation: new Type(...)
            match("LPAREN");
            parseArgValues();
            match("RPAREN");
        } else {
            // Assumes array creation or constructor call is mandatory after 'new Type'
            throw new SyntaxErrorException("Expected '(' for constructor call or '[' for array creation after new type", currentToken.lineNo, currentToken.toString());
        }
    }
    private void parseArgValues() { if (isExpressionStart()) { parseExpression(); while (check("COMMA")) { nextToken(); parseExpression(); } } }

    public static void main(String[] args) {
        String filename = "C:\\Users\\malai\\IdeaProjects\\compiler\\code.txt";
        if (args.length > 0) { filename = args[0]; System.out.println("Using filename from command line: " + filename); } else { System.out.println("Using default filename: " + filename); }
        File f = new File(filename);
        System.out.println("Attempting to process file: " + f.getAbsolutePath());
        if (!f.exists()) { System.err.println("\n--- File Error --- \nFATAL: File not found at the specified path."); return; }
        List<Token> tokens = null; boolean lexicalSuccess = false, syntaxSuccess = false;
        try {
            System.out.println("\n--- Lexical Analysis Starting ---"); tokens = lexicalAnalysis(filename); System.out.println("--- Lexical Analysis Finished ---"); lexicalSuccess = true; System.out.println("\n--- Lexical Analysis Tokens ---"); System.out.println("... (" + tokens.size() + " tokens generated, including EOF) ...");
        } catch (Exception e) { System.err.println("\n--- Unexpected Error During Lexical Analysis ---"); e.printStackTrace(); }
        if (lexicalSuccess) {
            try {
                System.out.println("\n--- Syntax Analysis Starting ---"); LexicalSyntaxAnalyzer parser = new LexicalSyntaxAnalyzer(tokens); parser.parse(); syntaxSuccess = true; System.out.println("--- Syntax Analysis Finished ---");
            } catch (SyntaxErrorException e) { System.err.println("\n--- Syntax Error Detected --- \n" + e.getMessage()); }
            catch (Exception e) { System.err.println("\n--- Unexpected Internal Error During Parsing ---"); e.printStackTrace(); }
        } else { System.err.println("\nSyntax Analysis skipped due to errors in Lexical Analysis."); }
        System.out.println("\n============================");
        if (syntaxSuccess) { System.out.println("  Result: Syntax is Valid"); } else { System.out.println("  Result: Syntax is Invalid"); }
        System.out.println("============================");
    }
}