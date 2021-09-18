package mylox;

import java.util.List;

public class LoxFunction implements LoxCallable {
    // declaration stores the function statements to be executed
    private final Stmt.Function declaration;
    // closure stores any enclosing scope of outer functions or global scope by
    // default
    private final Environment closure;
    private final boolean isInitializer;

    LoxFunction(Stmt.Function declaration, Environment closure, boolean isInitializer) {
        this.declaration = declaration;
        this.closure = closure;
        this.isInitializer = isInitializer;
    }

    LoxFunction bind(LoxInstance instance) {
        Environment environment = new Environment(closure);
        environment.define("this", instance);
        return new LoxFunction(declaration, environment, isInitializer);
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        // use the function call's enclosed environment rather than global scope
        Environment environment = new Environment(closure);
        int size = declaration.params.size();

        // define all arguments passed into functions with the names from
        // the function declaration parameters
        for (int i = 0; i < size; i++) {
            environment.define(declaration.params.get(i).lexeme, arguments.get(i));
        }

        // execute the function body
        // and return the return value from call or null
        try {
            interpreter.executeBlock(declaration.body, environment);
        } catch (Return returnValue) {
            if (isInitializer) return closure.getAt(0, "this");
            return returnValue.value;
        }

        if (isInitializer) return closure.getAt(0, "this");
        // never reached return stmt, so exit with implicit null return
        return null;
    }

    @Override
    public int arity() {
        return declaration.params.size();
    }

    @Override
    public String toString() {
        return "<fn " + declaration.name.lexeme + ">";
    }
}
