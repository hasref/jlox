package com.craftinginterpreters.lox;

import java.util.List;

import static com.craftinginterpreters.lox.TokenType.*;

/**
 * A recursive descent parser i.e. in effect, we will directly translate the
 * expression grammar into functions, going from lower to higher precedence.
 */
class Parser {
    // a sentinel class for parse errors
    private static class ParseError extends RuntimeException {
    }

    private final List<Token> tokens;
    private int current = 0;

    Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    Expr parse() {
        try {
            return commaExpr();
        } catch (ParseError error) {
            return null;
        }
    }

    // lowest precedence, left associative
    // comma-expr --> expression ( "," expression )*
    private Expr commaExpr() {
        Expr expr = ternary();
        while (match(COMMA)) {
            Token operator = previous();
            Expr right = ternary();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    // ternary is higher precedence than comma
    // ternary --> expr '?' expr ':' ( ( expr '?' expr ':')* | expr )
    private Expr ternary() {
        Expr expr = expression();
        if (match(QUESTION)) {
            Expr ifTrue = expression();
            if (!match(COLON)) {
                throw error(previous(), "Expected colon ':'.");
            }
            expr = new Expr.Ternary(expr, ifTrue, ternary()); // this code looks so weird
        }
        return expr;
    }

    // expression --> equality
    private Expr expression() {
        return equality();
    }

    // TODO: the code for equality, comparison, term and factor is virtually
    // identicaly. We can probably use a helper method here.

    // equality --> comparison ( ( "!= " | "==" ) comparison )*
    private Expr equality() {
        Expr expr = comparison(); // this is why left recursion in a recursive descent parser is problematic - we
                                  // would hit stackoverflow

        while (match(BANG_EQUAL, EQUAL_EQUAL)) {
            Token operator = previous(); // TODO: this is a little bit awkward, ideally match should return a pair
            Expr right = comparison();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    // comparison --> term ( ( ">" | ">=" | "<" | "<=" ) term)*
    private Expr comparison() {
        Expr expr = term();
        while (match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) {
            Token operator = previous(); // matched by match
            Expr right = term();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    // term --> factor ( ( "-" | "+" ) factor)*
    private Expr term() {
        Expr expr = factor();

        while (match(MINUS, PLUS)) {
            Token operator = previous();
            Expr right = factor();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    // factor --> unary ( ( "/" | "*" ) unar)*
    private Expr factor() {
        Expr expr = unary();

        while (match(SLASH, STAR)) {
            Token operator = previous();
            Expr right = unary();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    // unary --> ( "!" | "-" ) unary | primary
    private Expr unary() {
        if (match(BANG, MINUS)) {
            Token operator = previous();
            Expr right = unary();
            return new Expr.Unary(operator, right);
        }
        return primary();
    }

    private Expr primary() {
        if (match(FALSE)) {
            return new Expr.Literal(false);
        }

        if (match(TRUE)) {
            return new Expr.Literal(true);
        }

        if (match(NIL)) {
            return new Expr.Literal(null);
        }

        if (match(NUMBER, STRING)) {
            return new Expr.Literal(previous().literal);
        }

        if (match(LEFT_PAREN)) {
            Expr expr = expression();
            consume(RIGHT_PAREN, "Expect ')' after expression.");
            return new Expr.Grouping(expr);
        }

        throw error(peek(), "Expected expression.");
    }

    /**
     * Checks if the current token has any of the given types. If found, it consumes
     * the token and return true.
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
     * Checks that the type of the next token is 'type' and consumes it. This is
     * different from check. If the type is not 'type', then this function throws an
     * error with 'message'.
     */
    private Token consume(TokenType type, String message) {
        if (check(type)) {
            return advance();
        }

        throw error(peek(), message); // we are using Java's exception to synchronize the parser in case of errors
                                      // (we'll catch the exception at the point where we want to synchronize)
    }

    /**
     * Checks that the type of the next token is 'type' without consuming it.
     */
    private boolean check(TokenType type) {
        if (isAtEnd()) {
            return false;
        }

        return peek().type == type;
    }

    private Token advance() {
        if (!isAtEnd()) {
            current++;
        }
        return previous();
    }

    private boolean isAtEnd() {
        return peek().type == EOF;
    }

    /**
     * @return current token
     */
    private Token peek() {
        return tokens.get(current);
    }

    /**
     * @return the last consumed token. (Consumption is decided by the value of
     *         'current' member variable)
     */
    private Token previous() {
        return tokens.get(current - 1);
    }

    private ParseError error(Token token, String message) {
        Lox.error(token, message);
        return new ParseError();
    }

    /**
     * Synchronize after encountering an error by discarding tokens until we find
     * a token that could be the beginning of the next statement. We do this so that
     * the parser does not stop after encountering the first error but tries to
     * report as many errors as possible.
     * 
     * TODO: This is not used yet since we do not have statements yet.
     */
    private void synchronize() {
        advance();

        while (!isAtEnd()) {
            if (previous().type == SEMICOLON) {
                return;
            }
            switch (peek().type) {
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
            advance();
        }
    }

}
