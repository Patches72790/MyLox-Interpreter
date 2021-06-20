package mylox;

/**
 * Emulates the break mechanism in control flow loops
 * by immediately exiting the call stack while in 
 * the midst of loops via the runtime exception pattern.
 */
public class BreakException extends RuntimeException {

    private Stmt.Break myBreak;

    public BreakException(Stmt.Break myBreak) {
        super(null, null, false, false);
        this.myBreak = myBreak;
    }

    public Stmt.Break getMyBreak() {
        return myBreak;
    }
}