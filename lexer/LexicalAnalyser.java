package lexer;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static lexer.Lexer.tokenize;

public class LexicalAnalyser {
    List<Token> tokens;
    public  LexicalAnalyser(String filename){
        tokens = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            int lineCount=0;
            while ((line = reader.readLine()) != null) {
                lineCount++;
                try {
                    tokens.addAll(tokenize(line));
                } catch (RuntimeException e) {
                    throw new RuntimeException(e.getMessage() + " in file line: " + lineCount, e);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<Token> getTokens() {
        return tokens;
    }
}
