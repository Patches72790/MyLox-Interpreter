package mylox;

import java.util.ArrayList;
import java.util.Arrays;
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
     * Peek at the next token's type to match the given parameter type.
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

    /**
     * Finds the previouser token (current - 2) and returns it without 
     * regressing the parser.
     * 
     * This method is primarily used for error handling for dangling binary
     * operators.
     * 
     * @return the current - 2 token, if it exists.
     */
    private Token previouser() {
        return tokens.get(current - 2);
    }


///////////////////////////////////////////////////////////////
//    Methods for parsing declaration, stmt, and expr below  //
///////////////////////////////////////////////////////////////

    List<Stmt> parse() {
        List<Stmt> statements = new ArrayList<>();
        while (!isAtEnd()) {
            statements.add(declaration());
        }

        return statements;
    }

/////////////////////////////////////////////////
//
// Declarations below
//
/////////////////////////////////////////////////

    private Stmt declaration() {
        try {
            if (match(VAR)) return varDeclaration();

            return statement();
        } catch (ParseError error) {
            synchronize();
            return null;
        }
    }

    private Stmt varDeclaration() {
        Token name = consume(IDENTIFIER, "Expect variable name.");

        Expr initializer = null;
        if (match(EQUAL)) { // make sure initializing expression after
            initializer = expression(); // parse expression initialization
        }

        // consume semicolon and return stmt var ast node
        consume(SEMICOLON, "Expect ';' after variable declaration");
        return new Stmt.Var(name, initializer);
    }


/////////////////////////////////////////////////
//
// Statements below
//
/////////////////////////////////////////////////

    private Stmt statement() {
        if (match(FOR)) return forStatement();
        // if (match(BREAK)) return breakStatement();
        if (match(IF)) return ifStatement();
        if (match(PRINT)) return printStatement();
        if (match(WHILE)) return whileStatement();
        if (match(LEFT_BRACE)) return new Stmt.Block(block());

        return expressionStatement();
    }
    
    private Stmt breakStatement() {
        consume(SEMICOLON, "Expect ';' after break.");
        // TODO what to do for break statements in the interpreter?
        // should I return a special type of AST node that would
        // trigger a break from the while loop visitor?
        
        return null;
    }

    private Stmt forStatement() {
        consume(LEFT_PAREN, "Expect '(' after 'for'.");

        // parse initialization
        Stmt initializer;
        if (match(SEMICOLON)) {
            initializer = null;
        } else if (match(VAR)) {
            initializer = varDeclaration();
        } else {
            initializer = expressionStatement();
        }

        // parse condition part
        Expr condition = null;
        if (!check(SEMICOLON)) {
            condition = expression();
        }
        consume(SEMICOLON, "Expect ';' after loop condition.");

        // parse increment part
        Expr increment = null;
        if (!check(RIGHT_PAREN)) {
            increment = expression();    
        }
        consume(RIGHT_PAREN, "Expect ')' after for clauses.");
        
        // parse for body
        Stmt body = statement();

        // the increment is executed as final statement of block
        if (increment != null) {
            body = new Stmt.Block(
                Arrays.asList(
                    body,
                    new Stmt.Expression(increment)));
        }

        // wrap the body in a while loop with condition expression
        if (condition == null) condition = new Expr.Literal(true);
        body = new Stmt.While(condition, body);

        // the initializer if present wraps the while loop since
        // it is executed once at the beginning of the loop
        if (initializer != null) {
            body = new Stmt.Block(Arrays.asList(initializer, body));
        }

        return body;
    }


    private Stmt ifStatement() {
        consume(LEFT_PAREN, "Expect '(' after 'if'.");
        Expr condition = expression();
        consume(RIGHT_PAREN, "Expect ')' after if condition.");

        Stmt thenBranch = statement();
        Stmt elseBranch = null;

        // optional else statement
        // dangling else always chooses nearest if
        if (match(ELSE)) {
            elseBranch = statement();
        }

        return new Stmt.If(condition, thenBranch, elseBranch);
    }

    private Stmt whileStatement() {
        consume(LEFT_PAREN, "Expect '(' after 'while'.");
        Expr condition = expression();
        consume(RIGHT_PAREN, "Expect ')' after condition.");
        Stmt body = statement();

        return new Stmt.While(condition, body);
    }

    private List<Stmt> block() {
        List<Stmt> statements = new ArrayList<>();

        // continue to add declarations until reach right brace
        // or EOF
        while (!check(RIGHT_BRACE) && !isAtEnd()) {
            statements.add(declaration());
        }

        consume(RIGHT_BRACE, "Expect '}' after block.");
        return statements;
    }

    private Stmt printStatement() {
        Expr value = expression();
        consume(SEMICOLON, "Expect ';' after value.");
        return new Stmt.Print(value);
    }

    private Stmt expressionStatement() {
        Expr expr = expression();
        consume(SEMICOLON, "Expect ';' after value.");
        return new Stmt.Expression(expr);
    }

/////////////////////////////////////////////////
//
// Expressions below
//
/////////////////////////////////////////////////

    private Expr expression() {
        return assignment();
    }

    private Expr assignment() {
        Expr expr = or();

        // assignment 
        if (match(EQUAL)) {
            // capture assign token
            Token equals = previous();
            // parse r-value on RHS
            Expr value = assignment();

            // if lhs was a variable (i.e. L-value)
            if (expr instanceof Expr.Variable) {
                Token name = ((Expr.Variable)expr).name;

                /* TODO -- parsing for ternary operator
                // parsing for ternary operation
                if (match(QUESTION)) {
                    Expr first = equality();
                    Expr second;

                    if (match(COLON)) {
                        second = equality();

                        // just going to always return first until
                        // assignment and evaluation are implemented

                        // todo -- fix the correct assignment rule for ternary op
                    } else {
                        throw error(peek(), "Expected colon for ternary expression.");
                    }
                }
                */

                return new Expr.Assign(name, value);
            }

            // if something other than variable used as L-value
            error(equals, "Invalid assignment target.");
        }

        return expr;
    }

    private Expr or() {
        Expr expr = and();

        while (match(OR)) {
            Token operator = previous();
            Expr right = and();
            expr = new Expr.Logical(expr, operator, right);
        }

        return expr;
    }

    private Expr and() {
        Expr expr = equality();

        while (match(AND)) {
            Token operator = previous();
            Expr right = equality();
            expr = new Expr.Logical(expr, operator, right);
        }

        return expr;
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

        // parse literals
        if (match(NUMBER, STRING)) {
            return new Expr.Literal(previous().literal);
        }

        // parse identifier 
        if (match(IDENTIFIER)) {
            return new Expr.Variable(previous());
        }

        if (match(LEFT_PAREN)) {
            Expr expr = expression();
            consume(RIGHT_PAREN, "Expect ')' after expression.");
            return new Expr.Grouping(expr);
        }

        throw error(peek(), "Expect expression.");
    }
}
