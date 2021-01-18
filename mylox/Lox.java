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

        Scanner scanner = new Scanner(source);
        List<Token> tokens = scanner.scanTokens();

        // For now, just print the tokens.
        for (Token token : tokens) {
            System.out.println(token);
        }
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
