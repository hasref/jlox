package com.craftinginterpreters.lox;

public class ReversePolishNotationPrinter implements Expr.Visitor<String> {
    String print(Expr expr) {
        return expr.accept(this); // dynamic dispatch on type of expr
    }

    @Override
    public String visitBinaryExpr(Expr.Binary expr) {
        return this.print(expr.left) + " " + this.print(expr.right) + " " + expr.operator.lexeme;
    }

    @Override
    public String visitGroupingExpr(Expr.Grouping expr) {
        return this.print(expr.expression);
    }

    @Override
    public String visitUnaryExpr(Expr.Unary expr) {
        return this.print(expr.right) + " " + expr.operator.lexeme;
    }

    @Override
    public String visitLiteralExpr(Expr.Literal expr) {
        return expr.value.toString();
    }

    // just for sanity check
    public static void main(String[] args) {
        Expr expression = new Expr.Binary(
                new Expr.Grouping(
                        new Expr.Binary(
                                new Expr.Literal(1),
                                new Token(TokenType.PLUS, "+", null, 1),
                                new Expr.Literal(2))),
                new Token(TokenType.STAR, "*", null, 1),
                new Expr.Grouping(
                        new Expr.Binary(
                                new Expr.Literal(4),
                                new Token(TokenType.MINUS, "-", null, 1),
                                new Expr.Literal(3))));
        System.out.println(new ReversePolishNotationPrinter().print(expression));
    }
}
