package Lexer;

import java.util.List;
import Exception.CustomException;

public class TestLexicalAnalyzer {
    public static void main(String[] args) {

        String inputFileName = "t1.txt";
        LexicalAnalyser scanner = new LexicalAnalyser(inputFileName);
        List<Token> tokens;
        try {
            tokens = scanner.scan();

            // Print the generated tokens
            for (Token token : tokens) {
                System.out.println("<" + token.type + ", " + token.value + ">");
            }
        } catch (CustomException e) {
            System.out.println(e.getMessage());
        }


    }
}
