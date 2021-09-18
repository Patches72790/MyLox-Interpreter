package mylox;

import java.util.HashMap;
import java.util.Map;

class LoxInstance {
    private LoxClass klass;
    private Map<String, Object> fields = new HashMap<>();

    LoxInstance () {}
    
    LoxInstance(Map<String, Object> staticMethods) {
        this.fields = staticMethods;
    }

    LoxInstance(LoxClass klass) {
        this.klass = klass;
    }

    Object get(Token name) {
        if (fields.containsKey(name.lexeme)) {
            return fields.get(name.lexeme);
        }

        // edge case for undefined static methods
        if (klass == null) {
            throw new RuntimeError(name, "Undefined static method '" + name.lexeme + "'.");
        }

        LoxFunction method = klass.findMethod(name.lexeme);
        if (method != null) return method.bind(this);

        throw new RuntimeError(name, "Undefined property '" + name.lexeme + "'.");
    }

    void set(Token name, Object value) {
        fields.put(name.lexeme, value);
    }

    @Override
    public String toString() {
        return klass.name + " instance";
    }
}
