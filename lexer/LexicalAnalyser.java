package lexer;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static lexer.Lexer.tokenize;

public class LexicalAnalyser {
    List<Token> tokens;

    public LexicalAnalyser(String filename) {
        tokens = new ArrayList<>();
        try {
            // Read entire file as one string to handle multi-line constructs
            String content = Files.readString(Paths.get(filename));
            tokens.addAll(tokenize(content));
        } catch (IOException e) {
            throw new RuntimeException("Error reading file: " + filename, e);
        } catch (RuntimeException e) {
            throw new RuntimeException("Tokenization error in file: " + filename + " - " + e.getMessage(), e);
        }
    }

    public List<Token> getTokens() {
        return tokens;
    }

    public static void main(String[] args) {
        try {
            LexicalAnalyser analyser = new LexicalAnalyser("t2.txt");
            List<Token> tokens = analyser.getTokens();

            for (Token token : tokens) {
                //System.out.println(token);
                System.out.println("<" + token.type + ", " + token.value + ">");
            }
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}