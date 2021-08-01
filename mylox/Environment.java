package mylox;

import java.util.HashMap;
import java.util.Map;

public class Environment {
    // reference to next outer scope
    final Environment enclosing;

    // hash table for mapping identifiers to values
    private final Map<String, Object> values = new HashMap<>();

    Environment() {
        enclosing = null;
    }

    Environment(Environment enclosing) {
        this.enclosing = enclosing;
    }

    // add variable name with object to map
    void define(String name, Object value) {
        values.put(name, value);
    }

    Object get(Token name) {
        // look in current local scope first
        if (values.containsKey(name.lexeme)) {
            return values.get(name.lexeme);
        }

        // recursively search in outer scope until name found
        if (enclosing != null)
            return enclosing.get(name);

        throw new RuntimeError(name, "Undefined variable '" + name.lexeme + "'.");
    }

    void assign(Token name, Object value) {
        // start in innermost scope
        if (values.containsKey(name.lexeme)) {
            values.put(name.lexeme, value);
            return;
        }

        // recursively assign to next outer scope
        if (enclosing != null) {
            enclosing.assign(name, value);
            return;
        }

        throw new RuntimeError(name, "Undefined variable '" + name.lexeme + "'.");
    }
}
