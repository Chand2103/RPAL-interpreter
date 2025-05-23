package Lexer;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.regex.*;

import Exception.CustomException;

/**

  Lexical Analyzer for tokenizing source code files.
  Recognizes identifiers, keywords, integers, operators, strings, punctuation, and comments.

 **/
public class LexicalAnalyser {
    private final String inputFileName;
    private final List<Token> tokens = new ArrayList<>();

    // Regular expression patterns for matching different token types
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

    // Compiled patterns for better performance
    private static final Pattern IDENTIFIER_PATTERN = Pattern.compile(IDENTIFIER);
    private static final Pattern INTEGER_PATTERN = Pattern.compile(INTEGER);
    private static final Pattern OPERATOR_PATTERN = Pattern.compile(OPERATOR);
    private static final Pattern STRING_PATTERN = Pattern.compile(STRING);
    private static final Pattern PUNCTUATION_PATTERN = Pattern.compile(PUNCTUATION);
    private static final Pattern SPACES_PATTERN = Pattern.compile(SPACES);
    private static final Pattern COMMENT_PATTERN = Pattern.compile(COMMENT);

    // Reserved keywords in RPAL language
    private static final Set<String> KEYWORDS = Set.of(
            "let", "in", "fn", "where", "aug", "or", "not", "gr", "ge", "ls",
            "le", "eq", "ne", "true", "false", "nil", "dummy", "within", "and", "rec"
    );

    public LexicalAnalyser(String inputFileName) {
        this.inputFileName = inputFileName;
    }

    /**

       Scans the input file and returns a list of tokens.
       @return List of tokens found in the file
       @throws CustomException if tokenization fails

     **/
    public List<Token> scan() throws CustomException {
        int lineNumber = 0;

        try (BufferedReader reader = new BufferedReader(new FileReader(inputFileName))) {
            String line;

            while ((line = reader.readLine()) != null) {
                lineNumber++;
                try {
                    tokenizeLine(line, lineNumber);
                } catch (CustomException e) {
                    throw new CustomException(e.getMessage() + " in line: " + lineNumber + "\nERROR in Lexer.");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return tokens;
    }

    /**

      Tokenizes a single line of input by matching against different token patterns.
      @param line The line to tokenize
      @param lineNumber Current line number for error reporting
      @throws CustomException if an unrecognized character is encountered

     **/
    private void tokenizeLine(String line, int lineNumber) throws CustomException {
        int currentIndex = 0;

        while (currentIndex < line.length()) {
            char currentChar = line.charAt(currentIndex);
            String remaining = line.substring(currentIndex);

            Matcher matcher;

            // Skip comments - everything after "//" is ignored
            matcher = COMMENT_PATTERN.matcher(remaining);
            if (matcher.lookingAt()) {
                currentIndex += matcher.group().length();
                continue;
            }

            // Skip whitespace and tabs
            matcher = SPACES_PATTERN.matcher(remaining);
            if (matcher.lookingAt()) {
                currentIndex += matcher.group().length();
                continue;
            }

            // Match identifiers and keywords
            matcher = IDENTIFIER_PATTERN.matcher(remaining);
            if (matcher.lookingAt()) {
                String match = matcher.group();
                // Check if identifier is actually a keyword
                tokens.add(new Token(KEYWORDS.contains(match) ? TokenType.KEYWORD : TokenType.IDENTIFIER, match));
                currentIndex += match.length();
                continue;
            }

            // Match integer literals
            matcher = INTEGER_PATTERN.matcher(remaining);
            if (matcher.lookingAt()) {
                tokens.add(new Token(TokenType.INTEGER, matcher.group()));
                currentIndex += matcher.group().length();
                continue;
            }

            // Match operators (can be multi-character)
            matcher = OPERATOR_PATTERN.matcher(remaining);
            if (matcher.lookingAt()) {
                tokens.add(new Token(TokenType.OPERATOR, matcher.group()));
                currentIndex += matcher.group().length();
                continue;
            }

            // Match string literals (enclosed in single quotes)
            matcher = STRING_PATTERN.matcher(remaining);
            if (matcher.lookingAt()) {
                tokens.add(new Token(TokenType.STRING, matcher.group()));
                currentIndex += matcher.group().length();
                continue;
            }

            // Match single-character punctuation
            matcher = PUNCTUATION_PATTERN.matcher(Character.toString(currentChar));
            if (matcher.matches()) {
                tokens.add(new Token(TokenType.PUNCTUATION, Character.toString(currentChar)));
                currentIndex++;
                continue;
            }

            // If no pattern matches, throw an error
            throw new CustomException("Cannot tokenize the Character: " + currentChar + " at Index: " + currentIndex);
        }
    }
}