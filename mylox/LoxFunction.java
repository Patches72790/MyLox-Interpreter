package mylox;

import java.util.List;

public class LoxFunction implements LoxCallable {
    private final Stmt.Function declaration;
    private final Environment closure;

    LoxFunction(Stmt.Function declaration, Environment closure) {
        this.declaration = declaration;
        this.closure = closure;
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
            return returnValue.value;
        }
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
