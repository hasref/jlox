package com.craftinginterpreters.lox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class Scanner {
  private final String source;
  private final List<Token> tokens = new ArrayList<>();
  private static final Map<String, TokenType> keywords;

  static {
    keywords = new HashMap<>();
    keywords.put("and", TokenType.AND);
    keywords.put("class", TokenType.CLASS);
    keywords.put("else", TokenType.ELSE);
    keywords.put("false", TokenType.FALSE);
    keywords.put("for", TokenType.FOR);
    keywords.put("fun", TokenType.FUN);
    keywords.put("if", TokenType.IF);
    keywords.put("nil", TokenType.NIL);
    keywords.put("or", TokenType.OR);
    keywords.put("print", TokenType.PRINT);
    keywords.put("return", TokenType.RETURN);
    keywords.put("super", TokenType.SUPER);
    keywords.put("this", TokenType.THIS);
    keywords.put("true", TokenType.TRUE);
    keywords.put("var", TokenType.VAR);
    keywords.put("while", TokenType.WHILE);
  }

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

      case '?':
        addToken(TokenType.QUESTION);
        break;

      case ':':
        addToken(TokenType.COLON);
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
        } else if (match('*')) {
          multilineComment();
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
        // this is here since we do not want to check for each decimal digit in separate
        // cases
        if (isDigit(c)) {
          number();
        } else if (isAlpha(c)) {
          // in the beginning assume that any keyword is an identifier. This is because
          // otherwise, we could scan 'orchid' as the reserved keyword 'or' followed by
          // 'chid' which would not make sense. This follows the maximum munch princple.
          identifier();
        } else {
          Lox.error(line, "Unexpected character.");
          break;
        }
    }
  }

  /**
   * Extracts an identifier from the source and adds a token.
   */
  private void identifier() {
    while (isAlphaNumeric(peek())) {
      advance();
    }

    String text = source.substring(start, current);
    // if in map, the current lexeme is a keyword
    TokenType type = keywords.get(text);
    if (type == null) {
      type = TokenType.IDENTIFIER;
    }

    // addToken will automatically add the lexeme to the token
    addToken(type);
  }

  /**
   * Consumes a number from the source and adds a token to the list.
   * Lox only supports floating point numbers.
   */
  private void number() {
    while (isDigit(peek())) {
      // no need to check for end of source since peek does that already
      advance();
    }

    if (peek() == '.' && isDigit(peekNext())) {
      advance();

      while (isDigit(peek())) {
        advance();
      }
    }
    addToken(TokenType.NUMBER, Double.parseDouble(source.substring(start, current)));
  }

  /**
   * Consume and add a string token to the list. Shows errors for
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
      Lox.error(line, "Unterminated string. Did you perhaps miss the closing '\"' ?");
      return;
    }

    // 2. reached end of string, consume ending quote '"'
    advance();

    String value = source.substring(start + 1, current - 1);
    addToken(TokenType.STRING, value);
  }

  private void multilineComment() {
    int commentTokenPairs = 1;
    while (!isAtEnd()) {
      if (peek() == '/' && peekNext() == '*') {
        commentTokenPairs++;
      } else if (peek() == '*' && peekNext() == '/') {
        commentTokenPairs--;

        if (commentTokenPairs == 0) {
          break;
        }

      }

      if (peek() == '\n') {
        line++;
      }
      advance();
    }

    if (isAtEnd()) {
      Lox.error(line, "Unterminated multi-line comment. Did you perhaps miss the closing '*/' ?");
      return;
    }

    // else we found the terminating part of the comment and need to advance beyond
    // it
    advance();
    advance();
    return;
  }

  /**
   * Matches the next unconsumed character with 'expected' and if it matches,
   * consumes it by advancing 'current'.
   */
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
   * @return the next unconsumed character (indexed by 'current') without
   *         consuming it. Signals end of source by returning the null terminator.
   */
  private char peek() {
    if (isAtEnd()) {
      return '\0';
    }
    return source.charAt(current);
  }

  /**
   * @return the character after the next unconsumed character without consuming.
   *         Signals end of source by returning the null terminator.
   */
  private char peekNext() {
    if (current + 1 >= source.length()) {
      return '\0';
    }
    return source.charAt(current + 1);
  }

  /**
   * @ return true if c is in [a-zA-z_]
   * Lox does not currently support identifiers in utf8 encoding (?)
   */
  private boolean isAlpha(char c) {
    return (c >= 'a' && c <= 'z') ||
        (c >= 'A' && c <= 'Z') ||
        c == '_';
  }

  private boolean isAlphaNumeric(char c) {
    return isAlpha(c) || isDigit(c);
  }

  private boolean isDigit(char c) {
    return c >= '0' && c <= '9';
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
   * Add a token to the list with literal value implicitly 'null'
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
