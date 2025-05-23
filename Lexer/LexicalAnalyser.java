package Lexer;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.regex.*;

import Exception.CustomException;

public class LexicalAnalyser {
    private final String inputFileName;
    private final List<Token> tokens = new ArrayList<>();

    private static final String LETTER = "[a-zA-Z]";
    private static final String DIGIT = "[0-9]";
    private static final String ESCAPE = "(\\\\'|\\\\t|\\\\n|\\\\\\\\)";
    private static final String OPERATOR_SYMBOL = "[+\\-*/<>&.@/:=~|$!#%^_\\[\\]{}\"`\\?]";
    private static final String IDENTIFIER = LETTER + "(" + LETTER + "|" + DIGIT + "|_)*";
    private static final String INTEGER = DIGIT + "+";
    private static final String OPERATOR = OPERATOR_SYMBOL + "+";
    private static final String PUNCTUATION = "[(),;]";
    private static final String SPACES = "(\\s|\\t)+";
    private static final String COMMENT = "//.*";
    private static final String STRING = "'(" +
            LETTER + "|" + DIGIT + "|" + OPERATOR_SYMBOL + "|" + ESCAPE + "|" + PUNCTUATION + "|" + SPACES + ")*'";

    private static final Pattern IDENTIFIER_PATTERN = Pattern.compile(IDENTIFIER);
    private static final Pattern INTEGER_PATTERN = Pattern.compile(INTEGER);
    private static final Pattern OPERATOR_PATTERN = Pattern.compile(OPERATOR);
    private static final Pattern STRING_PATTERN = Pattern.compile(STRING);
    private static final Pattern PUNCTUATION_PATTERN = Pattern.compile(PUNCTUATION);
    private static final Pattern SPACES_PATTERN = Pattern.compile(SPACES);
    private static final Pattern COMMENT_PATTERN = Pattern.compile(COMMENT);

    private static final Set<String> KEYWORDS = Set.of(
            "let", "in", "fn", "where", "aug", "or", "not", "gr", "ge", "ls",
            "le", "eq", "ne", "true", "false", "nil", "dummy", "within", "and", "rec"
    );

    public LexicalAnalyser(String inputFileName) {
        this.inputFileName = inputFileName;
    }

    public List<Token> scan() throws CustomException {
        int lineNumber = 0;

        try (BufferedReader reader = new BufferedReader(new FileReader(inputFileName))) {
            String line;

            while ((line = reader.readLine()) != null) {
                lineNumber++;
                try {
                    tokenizeLine(line, lineNumber);
                } catch (CustomException e) {
                    throw new CustomException(e.getMessage() + " in LINE: " + lineNumber + "\nERROR in Lexer.");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return tokens;
    }

    private void tokenizeLine(String line, int lineNumber) throws CustomException {
        int currentIndex = 0;

        while (currentIndex < line.length()) {
            char currentChar = line.charAt(currentIndex);
            String remaining = line.substring(currentIndex);

            Matcher matcher;

            matcher = COMMENT_PATTERN.matcher(remaining);
            if (matcher.lookingAt()) {
                currentIndex += matcher.group().length();
                continue;
            }

            matcher = SPACES_PATTERN.matcher(remaining);
            if (matcher.lookingAt()) {
                currentIndex += matcher.group().length();
                continue;
            }

            matcher = IDENTIFIER_PATTERN.matcher(remaining);
            if (matcher.lookingAt()) {
                String match = matcher.group();
                tokens.add(new Token(KEYWORDS.contains(match) ? TokenType.KEYWORD : TokenType.IDENTIFIER, match));
                currentIndex += match.length();
                continue;
            }

            matcher = INTEGER_PATTERN.matcher(remaining);
            if (matcher.lookingAt()) {
                tokens.add(new Token(TokenType.INTEGER, matcher.group()));
                currentIndex += matcher.group().length();
                continue;
            }

            matcher = OPERATOR_PATTERN.matcher(remaining);
            if (matcher.lookingAt()) {
                tokens.add(new Token(TokenType.OPERATOR, matcher.group()));
                currentIndex += matcher.group().length();
                continue;
            }

            matcher = STRING_PATTERN.matcher(remaining);
            if (matcher.lookingAt()) {
                tokens.add(new Token(TokenType.STRING, matcher.group()));
                currentIndex += matcher.group().length();
                continue;
            }

            matcher = PUNCTUATION_PATTERN.matcher(Character.toString(currentChar));
            if (matcher.matches()) {
                tokens.add(new Token(TokenType.PUNCTUATION, Character.toString(currentChar)));
                currentIndex++;
                continue;
            }

            throw new CustomException("Cannot tokenize the Character: " + currentChar + " at Index: " + currentIndex);
        }
    }
}
