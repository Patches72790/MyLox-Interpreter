package tool;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;

public class GenerateAST {
    
    public static void main(String[] args) throws IOException {

        if (args.length != 1) {
            System.err.println("Usage: generate_ast <output-directory>");
            System.exit(64);
        }

        String outputDir = args[0];

        defineAst(outputDir, "Expr", Arrays.asList(
            "Binary   : Expr left, Token operator, Expr right",
            "Grouping : Expr expression",
            "Literal  : Object value",
            "Logical  : Expr left, Token operator, Expr right",
            "Unary    : Token operator, Expr right",
            "Variable : Token name",
            "Assign   : Token name, Expr value"
        ), null);

        defineAst(outputDir, "Stmt", Arrays.asList(
            "If         : Expr condition, Stmt thenBranch," +
                        " Stmt elseBranch",
            "Block      : List<Stmt> statements",
            "While      : Expr condition, Stmt body",
            "Expression : Expr expression",
            "Print      : Expr expression",
            "Var        : Token name, Expr initializer",
            "Break      : none",
            "Continue   : none"
        ),
        Arrays.asList(
            "hadBreak",
            "hadContinue"
        ));
    }

    private static void defineAst(String outputDir, String baseName, List<String> types, List<String> optionalSharedFields) throws IOException {

        String path = outputDir + "/" + baseName + ".java";
        PrintWriter writer = new PrintWriter(path, "UTF-8");

        writer.println("package mylox;");
        writer.println();
        writer.println("import java.util.List;");
        writer.println();
        writer.println("abstract class " + baseName + " {");

        // define visitor method
        defineVisitor(writer, baseName, types);

        // write in optional shared fields
        if (optionalSharedFields != null) {
            defineSharedFields(writer, optionalSharedFields);
        }

        // ast classes
        for (String type : types) {
            String className = type.split(":")[0].trim();
            String fields = type.split(":")[1].trim();
            defineType(writer, baseName, className, fields);
        }

        // base accept() method
        writer.println();
        writer.println("  abstract <R> R accept(Visitor<R> visitor);");


        writer.println("}");
        writer.close();
    }

    private static void defineType(PrintWriter writer, String baseName, String className, String fieldList) {

        writer.println("  static class " + className + " extends " + baseName + " {");

        // store params in fields
        String[] fields = fieldList.split(", ");

        // optional if the class has no fields (for break and continue)
        if (!fieldList.equals("none")) {
            // constructor
            writer.println("    " + className + "(" + fieldList + ") {");

            for (String field : fields) {
                String name = field.split(" ")[1];
                writer.println("      this." + name + " = " + name + ";");
            }
            writer.println("    }");
        }
        
        // define visitor pattern for each subclass
        writer.println();
        writer.println("    @Override");
        writer.println("    <R> R accept(Visitor<R> visitor) {");
        writer.println("      return visitor.visit" + className + baseName + "(this);");
        writer.println("    }");

        // optional for break and continue
        if (!fieldList.equals("none")) {
            // fields
            writer.println();
            for (String field : fields) {
                writer.println("    final " + field + ";");
            }
        }
        writer.println("  }");
    }

    // defines the visitor interface for each subclasses
    private static void defineVisitor(PrintWriter writer, String baseName, List<String> types) {
        writer.println("  interface Visitor<R> {");

        for (String type : types) {
            String typeName = type.split(":")[0].trim();
            writer.println("    R visit" + typeName + baseName + "("
            + typeName + " " + baseName.toLowerCase() + ");");
        }

        writer.println("  }");
    }

    private static void defineSharedFields(PrintWriter writer, List<String> optionalSharedFields) {
        writer.println();

        for (String field : optionalSharedFields) {
            writer.println("  private boolean " + field + " = false;");
            writer.println("  boolean " + field + "() {\n    return " + field + ";\n  }");
            String fieldMethodName = field.substring(0, 1).toUpperCase() + field.substring(1, field.length());
            writer.println("  void set" + fieldMethodName + "() {\n    this." + field + " = true;\n  }");
        }

        writer.println();
    }
}

