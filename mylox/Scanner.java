package mylox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static mylox.TokenType.*;

public class Scanner {
    
    private final String source;
    private final List<Token> tokens;
    private static final Map<String, TokenType> keywords;
    private int start = 0;
    private int current = 0;
    private int line = 1;

    // block for initializing hash map with reserved keywords
    static {
        keywords = new HashMap<>();
        keywords.put("and",    AND);
        keywords.put("class",  CLASS);
        keywords.put("else",   ELSE);
        keywords.put("false",  FALSE);
        keywords.put("for",    FOR);
        keywords.put("fun",    FUN);
        keywords.put("if",     IF);
        keywords.put("nil",    NIL);
        keywords.put("or",     OR);
        keywords.put("print",  PRINT);
        keywords.put("return", RETURN);
        keywords.put("super",  SUPER);
        keywords.put("this",   THIS);
        keywords.put("true",   TRUE);
        keywords.put("var",    VAR);
        keywords.put("while",  WHILE);
    }

    Scanner(String source) {
        this.source = source;
        this.tokens = new ArrayList<>();
    }

    /**
     * Wrapper method for scanning the entire source string
     * and adding all tokens to list.
     * 
     * @return the list of tokens
     */
    List<Token> scanTokens() {
        while (!isAtEnd()) {
            start = current;
            scanToken();
        }

        // add final EOF token before finishing
        tokens.add(new Token(TokenType.EOF, "", null, line));
        return tokens;
    }

    /**
     * This method reads a token at a time by moving forward one character
     * at a time and adds it to the list of tokens of the scanner. 
     * 
     */
    private void scanToken() {
        char c = advance();

        switch (c) {
            /* Single operator cases */
            case '(': addToken(LEFT_PAREN); break;
            case ')': addToken(RIGHT_PAREN); break;
            case '{': addToken(LEFT_BRACE); break;
            case '}': addToken(RIGHT_BRACE); break;
            case ',': addToken(COMMA); break;
            case '.': addToken(DOT); break;
            case '-': addToken(MINUS); break;
            case '+': addToken(PLUS); break;
            case ';': addToken(SEMICOLON); break;
            case '*': addToken(STAR); break; 
            // added cases for ternary operator
            case '?': addToken(QUESTION); break;
            case ':': addToken(COLON); break;

            /*  Deals with double char operators with single lookahead*/
            case '!':
                addToken(match('=') ? BANG_EQUAL : BANG );
                break;
            case '=':
                addToken(match('=') ? EQUAL_EQUAL : EQUAL);
                break;
            case '<':
                addToken(match('=') ? LESS_EQUAL : LESS);
                break;
            case '>':
                addToken(match('=') ? GREATER_EQUAL : GREATER);
                break;

            /* Disambiguate comments and division */ 
            case '/':
                if (match('/')) { // a single line comment
                    while (peek() != '\n' && !isAtEnd()) advance();
                }

                /* multi-line comment support */
                else if (match('*')) {
                    while (peek() != '*' && !isAtEnd()) {
                        if (peek() == '\n') 
                            line++;
                        advance();
                    }

                    // finds final piece of multiline comment
                    if (match('*') && !match('/'))  {
                        Lox.error(line, "Error with mutli-line comment.");
                    }
                }

                else { // the division operator
                    addToken(SLASH); 
                }
                break;


            /* Ignore whitespace characters */
            case ' ':
            case '\r':
            case '\t':
                break;
            case '\n':
                line++; // if newline, increment line and move on
                break;

            // this case handles string literals
            case '"':
                string();
                break;

            /*  Default cases deal with longer lexemes and identifiers */
            default:
                // handles number literals
                if (isDigit(c)) {
                    number();
                } 
                // handles alphabetic characters (i.e. identifiers or reserved words)
                else if (isAlpha(c)) {
                    identifier();
                } else {
                    Lox.error(line, "Unexpected character.");
                }
                break;
        }
    }

    /**
     * Moves the scanner forward by one move and returns the 
     * current char
     * 
     * @return the current char
     */
    private char advance() {
        current++;
        return source.charAt(current - 1);
    }

    /**
     * Helper function to return when the scanner has reached the end of
     * the source file
     * 
     * @return true if at end, false otherwise
     */
    private boolean isAtEnd() {
        return current >= source.length();
    }

    /**
     * Returns whether the character is a digit 0 - 9.
     * @param c
     * @return
     */
    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    /**
     * This function returns whether a character is an alphabetic character
     * from a-z, A-Z, or an underscore (_).
     * 
     * @param c the char to be recognized
     * @return true if alphabetic, false otherswise
     */
    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') ||
               (c >= 'A' && c <= 'Z') ||
               c == '_';
    }

    /**
     * Combines isDigit and isAlpha to check whether the char
     * is alphabetic or a number.
     * 
     * @param c the char to be checked
     * @return true if alphanumeric, false otherwise
     */
    private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }

    /**
     * Adds token to list of tokens without literal.
     * 
     * @param type the type of token to be added
     */
    private void addToken(TokenType type) {
        addToken(type, null);
    }

    /**
     * Adds token with specified object literal.
     * 
     * @param type the type of token
     * @param literal the literal value to be added
     */
    private void addToken(TokenType type, Object literal) {
        String text = source.substring(start, current);
        tokens.add(new Token(type, text, literal, line));
    }

    /**
     * Lookahead method that looks at the next character without moving
     * the current forward.
     * 
     * @return the next char in source string
     */
    private char peek() {
        if (isAtEnd()) return '\0';
        return source.charAt(current);
    }

    /**
     * This method performs lookahead by two to the character at current + 1
     * without consuming the character for checking decimal digits.
     * 
     * @return the char at current + 1
     */
    private char peekNext() {
        // check for end of file
        if (current + 1 >= source.length()) return '\0';

        // return current + 1
        return source.charAt(current + 1);
    }

    /**
     * This function checks the next character without advancing by
     * looking ahead one character.
     * @param expected the expected char to be checked
     * @return true if current matches expected (current - 1), false otherwise
     */
    private boolean match(char expected) {
        if (isAtEnd()) return false;
        // check if current char is expected
        if (source.charAt(current) != expected ) return false;

        // if true, consume it and return true
        current++;
        return true;
    }

    /**
     * This function handles identifiers and reserved keywords for the 
     * scanner
     */
    private void identifier() {
        // handles identifiers (variables, functions, etc)
        while(isAlphaNumeric(peek())) advance();

        // check for identifier within keywords table
        String text = source.substring(start, current);
        TokenType type = keywords.get(text);

        // if the type does not exist, then it is an identifier
        if (type == null) 
            type = IDENTIFIER;

        addToken(type);
    }

    /**
     * This function adds a string literal to the token list
     */
    private void string() {
        // continue through source until closing quotation found
        while (peek() != '"' && !isAtEnd()) {
            // this allows strings to cross over lines
            if (peek() == '\n') line++;
            advance();
        }

        // never terminated string
        if (isAtEnd()) {
            Lox.error(line, "Unterminated string.");
        }

        // closing "
        advance();

        // trim surrounding quotes and add to tokens
        String value = source.substring(start + 1, current - 1);
        addToken(STRING, value);
    }

    /**
     * This function adds number literal tokens by incrementally checking
     * if the lexemes encountered are digits or a decimal followed by digits.
     */
    private void number() {
        // consume digits until end of number or decimal
        while (isDigit(peek())) advance();

        // consume decimal if it exists
        if (peek() == '.' && isDigit(peekNext())) {
            advance();

            // finish consuming digits following decimal
            while (isDigit(peek())) advance();
        }

        addToken(NUMBER, source.substring(start, current));
    }
}
