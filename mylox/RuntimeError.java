package mylox;

/**
 * This class handles runtime errors within the interpreter
 * class of the lox interpreter.
 */
public class RuntimeError extends RuntimeException {
    
    final Token token;

    RuntimeError(Token token, String message) {
        super(message);
        this.token = token;
    }
}
