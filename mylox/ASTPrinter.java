package mylox;

import mylox.Expr.Assign;
import mylox.Expr.Binary;
import mylox.Expr.Grouping;
import mylox.Expr.Literal;
import mylox.Expr.Unary;
import mylox.Expr.Variable;

public class ASTPrinter implements Expr.Visitor<String> {

    String print(Expr expr) {
        return expr.accept(this);
    }

    @Override
    public String visitBinaryExpr(Binary expr) {
        return parenthesize(expr.operator.lexeme, expr.left, expr.right);
    }

    @Override
    public String visitGroupingExpr(Grouping expr) {
        return parenthesize("group", expr.expression);
    }

    @Override
    public String visitLiteralExpr(Literal expr) {
        if (expr.value == null) return "nil";
        return expr.value.toString();
    }

    @Override
    public String visitUnaryExpr(Unary expr) {
        return parenthesize(expr.operator.lexeme, expr.right);
    }
 
    private String parenthesize(String name, Expr... exprs) {
        StringBuilder builder = new StringBuilder();

        // the prefix version
        // builder.append("(").append(name);

        builder.append("(");
        for (Expr expr : exprs) {
            builder.append(" ");
            builder.append(expr.accept(this)); // pass in the visitor printer recursively to each expression
        }

        // the postfix version
        builder.append(" " + name);
        builder.append(")");
        return builder.toString();
    }


    public static void main(String[] args) {

        Expr expression = new Expr.Binary(
            new Expr.Binary(
                new Expr.Literal(1),
                new Token(TokenType.PLUS, "+", null, 1),
                new Expr.Literal(2)
            ),

            new Token(TokenType.STAR, "*", null, 1),
            
            new Expr.Binary(
                new Expr.Literal(3),
                new Token(TokenType.MINUS, "-", null, 1),
                new Expr.Literal(4)
            )
        );

        System.out.println(new ASTPrinter().print(expression));
    }

    @Override
    public String visitVariableExpr(Variable expr) {
        return null;
    }

    @Override
    public String visitAssignExpr(Assign expr) {
        // TODO Auto-generated method stub
        return null;
    }
}
