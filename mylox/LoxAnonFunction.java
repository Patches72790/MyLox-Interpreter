package mylox;

import java.util.List;

public class LoxAnonFunction implements LoxCallable {
    // declaration stores the function statements to be executed
    private final Expr.AnonFunction functionExpression;
    // closure stores any enclosing scope of outer functions or global scope by
    // default
    private final Environment closure;

    LoxAnonFunction(Expr.AnonFunction functionExpression, Environment closure) {
        this.functionExpression = functionExpression;
        this.closure = closure;
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        // use the function call's enclosed environment rather than global scope
        Environment environment = new Environment(closure);
        int size = functionExpression.params.size();

        // define all arguments passed into functions with the names from
        // the function declaration parameters
        for (int i = 0; i < size; i++) {
            environment.define(functionExpression.params.get(i).lexeme, arguments.get(i));
        }

        // execute the function body
        // and return the return value from call or null
        try {
            interpreter.executeBlock(functionExpression.body, environment);
        } catch (Return returnValue) {
            return returnValue.value;
        }
        // never reached return stmt, so exit with implicit null return
        return null;
    }

    @Override
    public int arity() {
        return functionExpression.params.size();
    }

    @Override
    public String toString() {
        return "<anon fn expr>";
    }
}
