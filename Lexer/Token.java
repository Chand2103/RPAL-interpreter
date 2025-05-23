package Lexer;

/**

  Represents a token identified during lexical analysis.
  Contains the token type and its string value from the source code.

 **/

public class Token {
    public TokenType type;
    public String value;

    /**

      Creates a new token with the specified type and value.
      @param type The category of the token (KEYWORD, IDENTIFIER, etc.)
      @param value The actual string value from the source code

     */
    public Token(TokenType type, String value) {
        this.type = type;
        this.value = value;
    }

    public TokenType getType() {
        return type;
    }

    public String getValue() {
        return value;
    }
}