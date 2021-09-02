package mylox;

import java.util.ArrayList;
import java.util.List;

import mylox.Expr.Assign;
import mylox.Expr.Binary;
import mylox.Expr.Call;
import mylox.Expr.Grouping;
import mylox.Expr.Literal;
import mylox.Expr.Logical;
import mylox.Expr.Unary;
import mylox.Expr.Variable;
import mylox.Stmt.Block;
import mylox.Stmt.Function;
import mylox.Stmt.If;
import mylox.Stmt.Return;
import mylox.Stmt.Var;
import mylox.Stmt.While;

/**
 * This class supports the interpreting of expressions built from the Parser
 * abstract syntax trees of this lox language.
 * 
 * @author Patrick
 */
public class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Void> {

    // interpreter has its global environment
    final Environment globals = new Environment();
    private Environment environment = globals;

    Interpreter() {
        // define global function for use in interpreter
        globals.define("clock", new LoxCallable() {
            public int arity() {
                return 0;
            }

            public Object call(Interpreter interpreter, List<Object> arguments) {
                return (double) System.currentTimeMillis() / 1000.0;
            }

            public String toString() {
                return "<native fn>";
            }
        });

        globals.define("out", new LoxCallable() {
            public int arity() {
                return 1;
            }

            public Object call(Interpreter interpreter, List<Object> arguments) {
                System.out.println(arguments.get(0));
                return null;
            }

            public String toString() {
                return "<native fn>";
            }
        });
    }

    /**
     * Interface method for interpreting statement lists.
     * 
     * @param expression
     */
    void interpret(List<Stmt> statements) {
        try {
            for (Stmt statement : statements) {
                execute(statement);
            }
        } catch (RuntimeError error) {
            Lox.runtimeError(error);
        } catch (BreakException breakExcpt) {
            Lox.error(breakExcpt.getMyBreak().breakToken.line, "Error found break exception outside of control flow.");
        }
    }

    /**
     * This method returns the string representation of the evaluated expression.
     * 
     * @param object the value of the expression that was evaluated
     * @return the stringified expression
     */
    private String stringify(Object object) {
        if (object == null)
            return "nil";

        // handles number printing
        if (object instanceof Double) {
            String text = object.toString();

            // cuts off decimals for integers in lox
            if (text.endsWith(".0")) {
                text = text.substring(0, text.length() - 2);
            }
            return text;
        }

        // default object printing
        return object.toString();
    }

    /**
     * This method evaluates a binary expression and returns the resulting value.
     * 
     * @return the value of the evaluated expression
     */
    @Override
    public Object visitBinaryExpr(Binary expr) {
        // left to right evaluation
        Object left = evaluate(expr.left);
        Object right = evaluate(expr.right);

        // return expression based on operator type
        // also check for runtime errors based on types
        switch (expr.operator.type) {
            case MINUS:
                checkNumberOperands(expr.operator, left, right);
                return (double) left - (double) right;
            // plus operator handles addition and string concatenation
            case PLUS:
                if (left instanceof Double && right instanceof Double) {
                    return (double) left + (double) right;
                }
                // overloaded + operator for concat
                if (left instanceof String && right instanceof String) {
                    return (String) left + (String) right;
                }

                // two cases to handle string concatenation with numbers
                if (left instanceof String && right instanceof Double) {
                    return (String) left + stringify(right);
                }

                if (left instanceof Double && right instanceof String) {
                    return stringify(left) + (String) right;
                }

                // otherwise throw error
                throw new RuntimeError(expr.operator, "Operands must be two numbers or two strings.");

            // mult and division
            case SLASH:
                checkNumberOperands(expr.operator, left, right);
                if ((double) right == 0)
                    throw new RuntimeError(expr.operator, "Division by zero.");

                return (double) left / (double) right;
            case STAR:
                checkNumberOperands(expr.operator, left, right);
                return (double) left * (double) right;

            // comparison operations
            case GREATER:
                checkNumberOperands(expr.operator, left, right);
                return (double) left > (double) right;
            case GREATER_EQUAL:
                checkNumberOperands(expr.operator, left, right);
                return (double) left >= (double) right;
            case LESS:
                checkNumberOperands(expr.operator, left, right);
                return (double) left < (double) right;
            case LESS_EQUAL:
                checkNumberOperands(expr.operator, left, right);
                return (double) left <= (double) right;

            // equality operations
            case BANG_EQUAL:
                return !isEqual(left, right);
            case EQUAL_EQUAL:
                return isEqual(left, right);
        }

        // unreachable
        return null;
    }

    /**
     * This method evaluates grouped expressions
     */
    @Override
    public Object visitGroupingExpr(Grouping expr) {
        return evaluate(expr.expression);
    }

    /**
     * Fundamental method for evaluating expressions for the interpreter. This
     * method calls the initial accept method on each expressions.
     * 
     * @param expr the expression to be evaluated
     * @return the value of the expression evaluated
     */
    private Object evaluate(Expr expr) {
        return expr.accept(this);
    }

    /**
     * Interprets recursively the stmt and its expressions.
     * 
     * @param stmt
     */
    private void execute(Stmt stmt) {
        stmt.accept(this);
    }

    /**
     * 
     * @param statements
     * @param environment
     */
    void executeBlock(List<Stmt> statements, Environment environment) {
        // save the previous global environment
        Environment previous = this.environment;

        try {
            // set interpreters current scope to inner scope of statement
            this.environment = environment;

            // execute all statements in list
            for (Stmt statement : statements) {
                execute(statement);
            }
        } finally {
            // restore global environment to interpreter
            this.environment = previous;
        }
    }

    /**
     * Converts literal expressions into its corresponding runtime value.
     */
    @Override
    public Object visitLiteralExpr(Literal expr) {
        return expr.value;
    }

    /**
     * This method evalutes a unary expression and return the object value.
     * 
     * @return the object value of the unary expression
     */
    @Override
    public Object visitUnaryExpr(Unary expr) {
        // Post-Order traversal evaluation of tree
        Object right = evaluate(expr.right);

        // evaluate based on unary operator types
        switch (expr.operator.type) {
            case BANG:
                return !isTruthy(right);
            case MINUS:
                checkNumberOperand(expr.operator, right); // error handling for double operations
                return -(double) right;
        }

        // unreachable
        return null;
    }

    /**
     * Checks that the right hand operand of the unary expression is also a Double.
     * Otherwise, it throws a runtime error.
     * 
     * @param operator the operator for the expression
     * @param operand  the right operand for the expression
     */
    private void checkNumberOperand(Token operator, Object operand) {
        if (operand instanceof Double)
            return;
        throw new RuntimeError(operator, "Operand must be a number");
    }

    /**
     * Checks that the left and right hand operands are valid Doubles, otherwise a
     * runtime exception is thrown.
     * 
     * @param operator
     * @param left
     * @param right
     */
    private void checkNumberOperands(Token operator, Object left, Object right) {
        // checks that both left and right operands are numbers
        if (left instanceof Double && right instanceof Double)
            return;

        // else throw runtime error
        throw new RuntimeError(operator, "Operands must be numbers.");
    }

    /**
     * Checks the equality of two operands for the binary expression.
     * 
     * @param a first object to check
     * @param b second object to check
     * @return whether a equals b based on builtin Object method from Java
     */
    private boolean isEqual(Object a, Object b) {
        if (a == null && b == null)
            return true;
        if (a == null)
            return false;

        // TODO could potentially add further equality measures
        // e.g. Do I want string equality checking?
        // Do I want more rigorous checking than the equals method?

        return a.equals(b);
    }

    /**
     * Determines whether the object is a truthy expression or not. Null objects and
     * false booleans are falsey. All other expressions are truthy.
     * 
     * @param object the expression to be checked for truthiness
     * @return the truthiness of the object
     */
    private boolean isTruthy(Object object) {
        if (object == null)
            return false;
        if (object instanceof Boolean)
            return (boolean) object;
        // empty strings are false
        if (object instanceof String) {
            return ((String) object).length() != 0;
            // if (((String)object).length() == 0) return false;
            // else return true;
        }
        // number zero is false
        if (object instanceof Double) {
            return ((Double) object).doubleValue() != 0;
            // if (((Double)object).doubleValue() == 0) return false;
            // else return true;
        }
        return true;
    }

    public Void visitExpressionStmt(Stmt.Expression stmt) {
        Object exprResult = evaluate(stmt.expression);

        // for evaluating simple expressions in interpreter
        if (exprResult != null) {
            // System.out.println(exprResult);
        }

        return null;
    }

    public Void visitPrintStmt(Stmt.Print stmt) {
        Object value = evaluate(stmt.expression);
        System.out.println(stringify(value));
        return null;
    }

    /**
     * This is for declaration plus initialization statements.
     * 
     */
    @Override
    public Void visitVarStmt(Var stmt) {
        Object value = null;
        if (stmt.initializer != null) {
            value = evaluate(stmt.initializer);
        }

        // add to global scope
        environment.define(stmt.name.lexeme, value);

        return null;
    }

    /**
     * This accesses the data stored at the variable in the environment scope.
     * 
     */
    @Override
    public Object visitVariableExpr(Variable expr) {
        // lookup id in environment
        Object valueAtVarId = environment.get(expr.name);

        // return value if initialized / contains data
        if (valueAtVarId != null) {
            return valueAtVarId;
        }

        // throw an error if identifier is not initialized to anything
        throw new RuntimeError(expr.name, "Error: variable '" + expr.name.lexeme + "' is not initialized.");
    }

    @Override
    public Object visitAssignExpr(Assign expr) {
        Object value = evaluate(expr.value);
        environment.assign(expr.name, value);
        return value;
    }

    @Override
    public Void visitBlockStmt(Block stmt) {
        executeBlock(stmt.statements, new Environment(environment));

        return null;
    }

    @Override
    public Void visitIfStmt(If stmt) {
        if (isTruthy(evaluate(stmt.condition))) {
            execute(stmt.thenBranch);
        }
        // check that else branch present
        else if (stmt.elseBranch != null) {
            execute(stmt.elseBranch);
        }

        return null;
    }

    @Override
    public Object visitLogicalExpr(Logical expr) {
        Object left = evaluate(expr.left);

        // or can short circuit if left evaluates to true
        if (expr.operator.type == TokenType.OR) {
            if (isTruthy(left))
                return left;
        } else {
            // and short circuits if false
            if (!isTruthy(left))
                return left;
        }

        return evaluate(expr.right);
    }

    @Override
    public Void visitWhileStmt(While stmt) {
        while (isTruthy(evaluate(stmt.condition))) {
            try {
                execute(stmt.body);
            } catch (BreakException be) {
                break;
            }
        }

        return null;
    }

    @Override
    public Void visitBreakStmt(Stmt.Break stmt) {
        throw new BreakException(stmt);
    }

    @Override
    public Object visitCallExpr(Call expr) {
        Object callee = evaluate(expr.callee);

        List<Object> arguments = new ArrayList<>();
        for (Expr argument : expr.arguments) {
            arguments.add(evaluate(argument));
        }

        if (!(callee instanceof LoxCallable)) {
            throw new RuntimeError(expr.paren, "Can only call functions and classes.");
        }

        LoxCallable function = (LoxCallable) callee;

        if (arguments.size() != function.arity()) {
            throw new RuntimeError(expr.paren, "Expected " + function.arity() + " but got " + arguments.size() + ".");
        }
        return function.call(this, arguments);
    }

    @Override
    public Void visitFunctionStmt(Function stmt) {
        // create the function object and bind it to the function stmts identifier
        // pass in the current environment of interpter to function stmt declaration for
        // closure
        LoxFunction function = new LoxFunction(stmt, environment);
        environment.define(stmt.name.lexeme, function);
        return null;
    }

    @Override
    public Void visitReturnStmt(Stmt.Return stmt) {
        Object value = null;
        if (stmt.value != null)
            value = evaluate(stmt.value);

        throw new mylox.Return(value);
    }

    @Override
    public Void visitAnonFunctionExpr(Expr.AnonFunction expr) {
            

        return null;
    }
}
