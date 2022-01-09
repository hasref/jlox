package com.craftinginterpreters.lox;

/**
 * Prints a tree representation of an Expression as a string.
 * 
 * Example Usage:
 * 
 * <pre>
 * {@code
 *      Expr some_expresion = new BinaryExpr(...);
 *      System.out.println(new AstPrinter.print(some_expression));
 * }
 * </pre>
 */
public class AstPrinter implements Expr.Visitor<String> {
    /**
     * Public entry method for the visitor. Clients of this class should just call
     * this with an expression.
     * 
     * @param expr expression to print
     * @return a tree (string) representation of the expr
     */
    String print(Expr expr) {
        return expr.accept(this);
    }

    @Override
    public String visitBinaryExpr(Expr.Binary expr) {
        return parenthesize(expr.operator.lexeme, expr.left, expr.right);
    }

    @Override
    public String visitGroupingExpr(Expr.Grouping expr) {
        return parenthesize("group", expr.expression);
    }

    @Override
    public String visitLiteralExpr(Expr.Literal expr) {
        if (expr.value == null) {
            return "nil";
        }
        return expr.value.toString();
    }

    @Override
    public String visitUnaryExpr(Expr.Unary expr) {
        return parenthesize(expr.operator.lexeme, expr.right);
    }

    /**
     * returns a string representation of name (usually an operators) applied to
     * sub-expressions exprs wrapped in paratheses e.g. (+ 2 3)
     * 
     * @param name  of a lexeme e.g. +, -
     * @param exprs 0 or more expressions
     */
    private String parenthesize(String name, Expr... exprs) {
        StringBuilder builder = new StringBuilder();
        builder.append("(").append(name);

        for (Expr expr : exprs) {
            builder.append(" ");
            builder.append(expr.accept(this)); // visit the subexpressions
        }
        builder.append(")");

        return builder.toString();
    }
}
