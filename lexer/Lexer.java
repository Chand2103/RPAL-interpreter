package lexer;

import java.util.*;
import java.util.regex.*;

public class Lexer {
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
            LETTER + "|" + DIGIT + "|" + OPERATOR_SYMBOL + "|" + ESCAPE + "|" + PUNCTUATION + "|" + SPACES +
            ")*'";

    private static final Pattern identifierPattern = Pattern.compile(IDENTIFIER);
    private static final Pattern integerPattern = Pattern.compile(INTEGER);
    private static final Pattern operatorPattern = Pattern.compile(OPERATOR);
    private static final Pattern stringPattern = Pattern.compile(STRING);
    private static final Pattern punctuationPattern = Pattern.compile(PUNCTUATION);
    private static final Pattern spacesPattern = Pattern.compile(SPACES);
    private static final Pattern commentPattern = Pattern.compile(COMMENT);

    public static List<Token> tokenize(String input) {
        List<Token> tokens = new ArrayList<>();

        int currentIndex = 0;
        int lineNumber = 1;
        int lineStartIndex = 0;
        while (currentIndex < input.length()) {
            char currentChar = input.charAt(currentIndex);

            // Track line numbers
            if (currentChar == '\n') {
                lineNumber++;
                lineStartIndex = currentIndex + 1;
            }

            // Skip comments
            Matcher commentMatcher = commentPattern.matcher(input.substring(currentIndex));
            if (commentMatcher.lookingAt()) {
                currentIndex += commentMatcher.group().length();
                continue;
            }

            // Skip spaces
            Matcher spaceMatcher = spacesPattern.matcher(input.substring(currentIndex));
            if (spaceMatcher.lookingAt()) {
                currentIndex += spaceMatcher.group().length();
                continue;
            }

            // Identifier or keyword
            Matcher matcher = identifierPattern.matcher(input.substring(currentIndex));
            if (matcher.lookingAt()) {
                String identifier = matcher.group();
                List<String> keywords = List.of(
                        "let", "in", "fn", "where", "aug", "or", "not", "gr", "ge", "ls",
                        "le", "eq", "ne", "true", "false", "nil", "dummy", "within", "and", "rec"
                );
                if (keywords.contains(identifier)) {
                    tokens.add(new Token(TokenType.KEYWORD, identifier));
                } else {
                    tokens.add(new Token(TokenType.IDENTIFIER, identifier));
                }
                currentIndex += identifier.length();
                continue;
            }

            // Integer
            matcher = integerPattern.matcher(input.substring(currentIndex));
            if (matcher.lookingAt()) {
                String integer = matcher.group();
                tokens.add(new Token(TokenType.INTEGER, integer));
                currentIndex += integer.length();
                continue;
            }

            // Operator
            matcher = operatorPattern.matcher(input.substring(currentIndex));
            if (matcher.lookingAt()) {
                String operator = matcher.group();
                tokens.add(new Token(TokenType.OPERATOR, operator));
                currentIndex += operator.length();
                continue;
            }

            // String
            matcher = stringPattern.matcher(input.substring(currentIndex));
            if (matcher.lookingAt()) {
                String str = matcher.group();
                tokens.add(new Token(TokenType.STRING, str));
                currentIndex += str.length();
                continue;
            }

            // Punctuation
            matcher = punctuationPattern.matcher(Character.toString(currentChar));
            if (matcher.matches()) {
                tokens.add(new Token(TokenType.PUNCTUATION, Character.toString(currentChar)));
                currentIndex++;
                continue;
            }

            // If no match, throw exception
            int columnNumber = currentIndex - lineStartIndex + 1;
            throw new RuntimeException("Unable to tokenize character: '" + currentChar +
                    "' at line: " + lineNumber + ", column: " + columnNumber);
        }

//        tokens.add(new Token(TokenType.EndOfTokens, "EOF"));
        return tokens;
    }

//    public static void main(String[] args) {
//        String input = "let x = 10 + 5; // This is a comment";
//        List<Token> tokens = tokenize(input);
//        for (Token token : tokens) {
//            System.out.println(token);
//        }
//    }
}
