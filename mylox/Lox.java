package mylox;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;


public class Lox {

    // error flag for error handling in run
    static boolean hadError = false;

    /**
     * This function runs the Lox code from a file.
     * 
     * @param path the path to the file to be run
     * @throws IOException if error in reading file
     */
    private static void runFile(String path) throws IOException {

        byte[] bytes = Files.readAllBytes(Paths.get(path));

        run(new String(bytes, Charset.defaultCharset()));

        // if an error occurs in running a file, exit gracefully
        if (hadError) System.exit(65);
    }

    /**
     * This function runs a REPL prompt interpreter for running code
     * on the command line.
     * 
     * @throws IOException
     */
    private static void runPrompt() throws IOException {

        InputStreamReader input = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(input);

        for (;;) {
            System.out.print("mylox> ");
            String line = reader.readLine();
            if (line == null)
                break;
            run(line);
            hadError = false; // reset error flag
        }
    }


    /**
     * This function reads through the file or the line from the prompt
     * and scans the content into tokens.
     * 
     * @param source the line of text from a file or from the prompt
     */
    private static void run(String source) {

        // lexically analyze source text and produce list of tokens
        Scanner scanner = new Scanner(source);
        List<Token> tokens = scanner.scanTokens();

        // parse tokens and return single expression (for now)
        Parser parser = new Parser(tokens);
        Expr expression = parser.parse();

        // stop executing if there was an error encountered
        if (hadError) return;

        
        // For now, just print the tokens.
        for (Token token : tokens) {
            System.out.println(token);
        } 

        // print expression with AST printer
        System.out.println(new ASTPrinter().print(expression));
    }

    /**
     * This method reports an error with the appropriate message and line number.
     * 
     * @param line
     * @param message
     */
    static void error(int line, String message) {
        report(line, "", message);
    }

    /**
     * This reports an error at a given token. It shows the location
     * and the token itself.
     * 
     * @param token the syntactically offending token
     * @param message the message associated with the error
     */
    static void error(Token token, String message) {
        if (token.type == TokenType.EOF) {
            report(token.line, " at end", message);
        } else {
            report(token.line, " at '" + token.lexeme + "'", message);
        }
    }

    /**
     * This method reports an error printing to stderr the line number, where it happened
     * and the message associated with it.
     * 
     * @param line
     * @param where
     * @param message
     */
    private static void report(int line, String where, String message) {
        System.err.println("[line " + line + "] Error" + where + ": " + message);

        hadError = true;
    }

    /**
     * 
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {

        if (args.length > 1) {
            System.out.println("Usage: jlox [script]");
            System.exit(64);
        }

        else if (args.length == 1) {
            runFile(args[0]);
        }

        else {
            runPrompt();
        }
    }
}
