package com.craftinginterpreters.tool;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;

/**
 * This class generates AST classes for us. The reason we are generating
 * these classes is that our AST nodes are just 'dumb' classes
 * that only store data. This is because our AST classes exist at the interface
 * between the parser and interpreter - what sort of methods should they
 * contain? Writing all of these classes out would
 * actually be way more verbose than writing a single class that
 * can generate the AST classes (e.g. base Expr, Unary, Binary,
 * Grouping) for us.
 */
public class GenerateAst {
    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.err.println("Usage: generate_ast <output directory>");
            System.exit(64);
        }
        String outputDir = args[0];

        // generate AST classes
        defineAst(outputDir, "Expr", Arrays.asList(
                "Binary    : Expr left, Token operator, Expr right",
                "Grouping  : Expr expression",
                "Literal   : Object value",
                "Unary     : Token operator, Expr right"));
    }

    /**
     * @param outputDir for the ast classes
     * @param baseName  name of the abstract base class (e.g. Expr) for the
     *                  generated classes
     * @param types     array of strings that define the classes to generate (in a
     *                  BNF like form)
     */
    private static void defineAst(String outputDir, String baseName, List<String> types) throws IOException {
        String path = outputDir + "/" + baseName + ".java";
        PrintWriter writer = new PrintWriter(path, "UTF-8");

        writer.println("package com.craftinginterpreters.lox;");
        writer.println();
        writer.println("import java.util.List;");
        writer.println();
        writer.println("abstract class " + baseName + " {");

        /**
         * Generate the AST classes as part of the base class. The type strings are of
         * the form:
         * "Binary: Expr left, Token Operator, Expr Right"
         * i.e. class name followed by colon and the type and name of the constituent
         * members/classes.
         */
        for (String type : types) {
            String[] split = type.split(":");
            String className = split[0].trim();
            String fields = split[1].trim();
            defineType(writer, baseName, className, fields);
        }

        writer.println("}");
        writer.close();
    }

    /**
     * Define a particular AST class.
     */
    private static void defineType(
            PrintWriter writer, String baseName,
            String className, String fieldList) {

        writer.println("  static class " + className + " extends " + baseName + " {");

        // constructor
        writer.println("    " + className + "(" + fieldList + ") {");

        // put constructor parameters in fields
        String[] fields = fieldList.split(", ");
        for (String field : fields) {
            String name = field.split(" ")[1];
            writer.println("    this." + name + " = " + name + ";");
        }

        writer.println("  }");

        // Fields
        writer.println();
        for (String field : fields) {
            writer.print("    final " + field + ";");
        }
        writer.println();
        writer.println("  }");
    }
}