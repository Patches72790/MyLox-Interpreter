package mylox;

import java.util.List;

public interface LoxCallable {
    // prints the number of args for this callable object
    int arity();

    // executes the call of this function or class object
    Object call(Interpreter interpreter, List<Object> arguments);
}
