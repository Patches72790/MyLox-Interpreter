package mylox;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

class Resolver implements Expr.Visitor<Void>, Stmt.Visitor<Void> {
    private final Interpreter interpreter;
    private final Stack<Map<String, Boolean>> scopes = new Stack<>();

    Resolver(Interpreter interpreter) {
        this.interpreter = interpreter;
    }

    // Wrapping method for visiting AST nodes as Resolver
    void resolve(List<Stmt> statements) {
        for (Stmt statement : statements) {
            resolve(statement);
        }
    }

    // dispatch method for Resolver to visit AST nodes
    private void resolve(Stmt stmt) {
        stmt.accept(this);
    }

    /**
     * Push a new map onto the stack of scopes currently
     * being analyzed.
     */ 
    private void beginScope() {
        scopes.push(new HashMap<String, Boolean>());
    }

    /**
     * Pop the map from the stack when the scope is finished.
     */ 
    private void endScope() {
        scopes.pop();
    }

    @Override
    public Void visitBlockStmt(Stmt.Block stmt) {
        beginScope();
        resolve(stmt.statements);
        endScope();
        return null;
    }

    @Override
    public Void visitVarStmt(Stmt.Var stmt) {
        return null; 
    }
}
