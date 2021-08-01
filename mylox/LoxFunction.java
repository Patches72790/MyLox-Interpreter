package mylox;

import java.util.List;

public class LoxFunction implements LoxCallable {
    private final Stmt.Function declaration;

    LoxFunction(Stmt.Function declaration) {
        this.declaration = declaration;
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        Environment environment = new Environment(interpreter.globals);
        int size = declaration.params.size();

        // define all arguments passed into functions with the names from
        // the function declaration parameters
        for (int i = 0; i < size; i++) {
            environment.define(declaration.params.get(i).lexeme, arguments.get(i));
        }

        // execute the function body
        interpreter.executeBlock(declaration.body, environment);
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
