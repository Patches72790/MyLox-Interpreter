package mylox;

import java.util.List;
import static mylox.TokenType.*;


public class Parser {

    // Private class for checking parse errors in parser
    private static class ParseError extends RuntimeException {}

    private final List<Token> tokens;
    private int current = 0;

    Parser(List<Token> tokens) {
        this.tokens = tokens;
    }
//////////////////////////////////////////////////////
//          Parsing utility operations              //
//  match, check, advance, peek, previous, isAtEnd  //
//////////////////////////////////////////////////////


    /**
     * Checks and consumes tokens for the given list of token types
     * as arguments.
     * 
     * @param types vararg list of tokent types
     * @return true when the types match the next token's type, false otherwise
     */
    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }

        return false;
    }

    /**
     * Similar to advance, but also may throw error for incomplete
     * expression.
     * 
     * @param type the token to be checked
     * @param message a message associated with error
     * @return the token checked for
     */
    private Token consume(TokenType type, String message) {
        if (check(type)) return advance();

        throw error(peek(), message);
    }

    /**
     * This method returns a new parse error for the token given
     * with its associated message by using the Lox error handler.
     * 
     * @param token the token with the error
     * @param message the message associated with the errant token
     * @return a ParseError indicating that something went wrong in parsing 
     *  this expression
     */
    private ParseError error(Token token, String message) {
        Lox.error(token, message);

        return new ParseError();
    }

    /**
     * This method synchronizes the parser at the next statement
     * and resumes parsing for catching any further syntax errors.
     * 
     */
    private void synchronize() {
        advance();

        while (!isAtEnd()) {
            // end of an expression
            if (previous().type == SEMICOLON) return;

            // next token is new reserved word
            switch(peek().type) {
                case CLASS:
                case FUN:
                case VAR:
                case FOR:
                case IF:
                case WHILE:
                case PRINT:
                case RETURN:
                    return;
            }

            // consume next expression
            advance();
        }
    }

    /**
     * Peek at the next token's type to match the given paramater type.
     * 
     * @param type the type of the current token
     * @return true if type matches the next token's type
     */
    private boolean check(TokenType type) {
        if (isAtEnd()) return false;
        return peek().type == type; // doesn't consume the token, but looksahead
    }

    /**
     * Consume the current token and return it. Otherwise, return the previous
     * token. 
     * @return
     */
    private Token advance() {
        if (!isAtEnd()) current++;
        return previous();
    }

    /**
     * Checks that the parser is at the end of the list of tokens
     * @return true if token equals EOF, false otherwise
     */ 
    private boolean isAtEnd() {
        return peek().type == EOF;
    }

    /**
     * Peeks at without consuming the curren token in the token list
     * @return the current token unconsumed
     */
    private Token peek() {
        return tokens.get(current);
    }

    /**
     * Finds the previous token in the list without moving backward.
     * 
     * @return the previous token from current
     */
    private Token previous() {
        return tokens.get(current - 1);
    }


//////////////////////////////////////////////////////
//    Methods for parsing expressions below         //
//////////////////////////////////////////////////////

    Expr parse() {
        try {
            return expression();
        } catch (ParseError error) {
            return null;
        }
    }


    private Expr expression() {
        return equality();
    }

    private Expr equality() {
        Expr expr = comparison();

        while (match(BANG_EQUAL, EQUAL_EQUAL)) {
            Token operator = previous();
            Expr right = comparison();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr comparison() {
        Expr expr = term();

        // this method achieves left to right associativty for comparison
        // operators
        while (match( GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) {
            Token operator = previous();
            Expr right = term();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr term() {
        Expr expr = factor();

        while ( match(MINUS, PLUS) ) {
            Token operator = previous();
            Expr right = factor();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr factor() {
        Expr expr = unary();

        while (match(SLASH, STAR)) {
            Token operator = previous();
            Expr right = unary();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr unary() {
        if (match(BANG, BANG_EQUAL)) {
            Token operator = previous();
            Expr right = unary();
            return new Expr.Unary(operator, right);
        }

        return primary();
    }

    private Expr primary() {
        if ( match(FALSE)) return new Expr.Literal(false);
        if ( match(TRUE))  return new Expr.Literal(true);
        if ( match(NIL))   return new Expr.Literal(null);

        if (match(NUMBER, STRING)) {
            return new Expr.Literal(previous().literal);
        }

        if (match(LEFT_PAREN)) {
            Expr expr = expression();
            consume(RIGHT_PAREN, "Expect ')' after expression.");
            return new Expr.Grouping(expr);
        }

        throw error(peek(), "Expect expression.");
    }
}
