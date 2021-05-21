package mylox;

import java.util.HashMap;
import java.util.Map;

public class Environment {
    private final Map<String, Object> values = new HashMap<>();

    // add variable name with object to map
    void define(String name, Object value) {
        values.put(name, value);
    }

    Object get(Token name) {
        if (values.containsKey(name)) {
            return values.get(name);
        }

        throw new RuntimeError(name,
            "Undefined variable '" + name.lexeme + "'.");
    }
}
