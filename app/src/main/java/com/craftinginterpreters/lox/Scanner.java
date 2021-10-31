package com.craftinginterpreters.lox;

import java.util.ArrayList;
import java.util.List;

class Scanner {
  private final String source;
  private final List<Token> tokens = new ArrayList<>();

  private int start = 0; // points to start of current lexeme
  private int current = 0; // current character under consideration
  private int line = 1;

  Scanner(String source) {
    this.source = source;
  }

  List<Token> scanTokens() {
    while (!isAtEnd()) {
      start = current;
      scanToken();
    }

    tokens.add(new Token(TokenType.EOF, "", null, line));
    return tokens;
  }

  private void scanToken() {
    char c = advance();
    switch (c) {
      case '(':
        addToken(TokenType.LEFT_PAREN);
        break;

      case ')':
        addToken(TokenType.RIGHT_PAREN);
        break;

      case '{':
        addToken(TokenType.LEFT_BRACE);
        break;

      case '}':
        addToken(TokenType.RIGHT_BRACE);
        break;

      case ',':
        addToken(TokenType.COMMA);
        break;

      case '.':
        addToken(TokenType.DOT);
        break;

      case '-':
        addToken(TokenType.MINUS);
        break;

      case '+':
        addToken(TokenType.PLUS);
        break;

      case ';':
        addToken(TokenType.SEMICOLON);
        break;

      case '*':
        addToken(TokenType.STAR);
        break;

      // everything else is an error
      default:
        Lox.error(line, "Unexpected character.");
        break;
    }
  }

  private boolean isAtEnd() {
    return current >= source.length();
  }

  /*
   * returns the current character under consideration, pointed to by the 'current' member, and
   * advances the 'current' offset.
   */
  private char advance() {
    char underConsideration = source.charAt(current);
    current++;
    return underConsideration;
  }

  private void addToken(TokenType type) {
    addToken(type, null);
  }

  /*
   * adds a token of type 'type' and literal value 'literal' to the list of tokens maintained by the
   * Scanner. The created token object also contains the token's text content and the source line
   * number the token appears on.
   *
   */
  private void addToken(TokenType type, Object literal) {
    String tokenText = source.substring(start, current);
    tokens.add(new Token(type, tokenText, literal, line));
  }
}
