package Lexer;

/**

   Defines the categories of tokens in RPAL that can be identified during lexical analysis.

 **/

public enum TokenType {
    KEYWORD,
    IDENTIFIER,
    INTEGER,
    OPERATOR,
    STRING,
    PUNCTUATION,
    DELETE,
    EndOfTokens;

    private TokenType() {
    }
}