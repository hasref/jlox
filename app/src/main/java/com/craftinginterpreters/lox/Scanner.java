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

      case '!':
        addToken(match('=') ? TokenType.BANG_EQUAL : TokenType.BANG);
        break;

      case '=':
        addToken(match('=') ? TokenType.EQUAL_EQUAL : TokenType.EQUAL);
        break;

      case '<':
        addToken(match('=') ? TokenType.LESS_EQUAL : TokenType.LESS);
        break;

      case '>':
        addToken(match('=') ? TokenType.GREATER_EQUAL : TokenType.GREATER);
        break;

      case '/':
        if (match('/')) {
          while (peek() != '\n' && !isAtEnd()) { // is comment, consume entire line
            advance();
          }
        } else {
          addToken(TokenType.SLASH);
        }

      case ' ':
      case '\r':
      case '\t':
        break;

      case '\n':
        line++;
        break;

      case '"':
        string();
        break;
      // everything else is an error
      default:
        Lox.error(line, "Unexpected character.");
        break;
    }
  }

  /**
   * Consume and add a string token to the TokenList. Shows errors for
   * unterminated strings. Currently does not support nested strings.
   * 
   * @implNote Lox supports multi-line strings but no escape characters.
   */
  private void string() {
    while (peek() != '"' && !isAtEnd()) {
      if (peek() == '\n') {
        line++;
      }
      advance();
    }

    // could have broken out of loop for two reasons, 1. end of input
    if (isAtEnd()) {
      Lox.error(line, "Unterminated string");
      return;
    }

    // 2. reached end of string, consume ending quote '"'
    advance();

    String value = source.substring(start + 1, current - 1);
    addToken(TokenType.STRING, value);
  }

  private boolean match(char expected) {
    if (isAtEnd()) {
      return false;
    }

    if (source.charAt(current) != expected) {
      return false;
    }

    current++;
    return true;
  }

  /**
   * @return the next unconsumed character without consuming it.
   */
  private char peek() {
    if (isAtEnd()) {
      return '\0';
    }
    return source.charAt(current);
  }

  private boolean isAtEnd() {
    return current >= source.length();
  }

  /**
   * @return the next unconsumed character i.e. the 'current' character under
   *         consideration, and consumes it (advances 'current')
   * 
   */
  private char advance() {
    char underConsideration = source.charAt(current);
    current++;
    return underConsideration;
  }

  /**
   * Add a token to the TokenList with literal value implicitly 'null'
   */
  private void addToken(TokenType type) {
    addToken(type, null);
  }

  /**
   * Add a token with literal value to the TokenList.
   * 
   */

  private void addToken(TokenType type, Object literal) {
    String tokenText = source.substring(start, current);
    tokens.add(new Token(type, tokenText, literal, line));
  }
}
