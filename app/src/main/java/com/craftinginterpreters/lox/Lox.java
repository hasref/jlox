package com.craftinginterpreters.lox;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Lox {
  static boolean hadError = false; // who sets this flag? Probably the scanner

  public static void main(String[] args) throws IOException {
    if (args.length > 1) {
      System.out.println("Usgae: jlox [script]");
      System.exit(64);
    } else if (args.length == 1) {
      runFile(args[0]);
    } else {
      runPrompt();
    }
  }

  private static void runFile(String path) throws IOException {
    // readAllBytes is not suitable for large files acc. to docs
    byte[] bytes = Files.readAllBytes(Paths.get(path));
    run(new String(bytes, Charset.defaultCharset()));

    if (hadError) {
      System.exit(65);
    }
  }

  private static void runPrompt() throws IOException {
    InputStreamReader input = new InputStreamReader(System.in);
    BufferedReader reader = new BufferedReader(input);

    for (;;) {
      System.out.print("> ");
      String line = reader.readLine();
      if (line == null) {
        break;
      }
      run(line);
      // reset flag since an error should not end the user's session }
      hadError = false;
    }
  }

  private static void run(String source) {
    Scanner scanner = new Scanner(source);
    List<Token> tokens = scanner.scanTokens();

    // for now, just echo out the tokens
    for (Token token : tokens) {
      System.out.println(token);
    }
  }

  /**
   * Report an error to the user by printing to System.err and sets
   * hadError.
   */
  static void error(int line, String message) {
    report(line, "", message);
    hadError = true;
  }

  private static void report(int line, String where, String message) {
    System.err.println("[line " + line + "] Error" + where + ": " + message);
  }
}
