/*
  Created by: Brendan Yawney and Andrew Song
  File Name: Main.java
*/

import java.io.*;
import absyn.*;

class Main {
  public static boolean print_syntax = false;
  public static boolean print_symbol = false;
  public static boolean print_code = false;


  static public void main(String[] argv) {
    if (argv.length < 2) {
      System.out.println("---No command line arguments provided---");
      System.out.println("Possible arguments are:");
      System.out.println("    -a (print syntax tree)");
      System.out.println("    -s (print symbol tree)");
      System.out.println("    -c (print code generation)");

    }
    else {
      for (String s : argv) {
        if (s.equals("-a"))
          print_syntax = true;
        if (s.equals("-s"))
          print_symbol = true;
        if (s.equals("-c"))
          print_code = true;
      }
    }

    /* Start the parser */
    try {
      parser p = new parser(new Lexer(new FileReader(argv[0])));
      Absyn result = (Absyn)(p.parse().value);

      if (result != null)
      {
        if (print_syntax) {
          PrintStream o = new PrintStream(new File(argv[0].replace(".cm", ".abs")));
          System.setOut(o);
          System.out.println("The abstract syntax tree is:\n");
          ShowTreeVisitor visitor = new ShowTreeVisitor();
          result.accept(visitor, 0, false);
        }

        if (print_symbol) {
          PrintStream o = new PrintStream(new File(argv[0].replace(".cm", ".sym")));
          System.setOut(o);
        }
        else {
          PrintStream o = new PrintStream(OutputStream.nullOutputStream());
          System.setOut(o);
        }

        System.out.println("The symbol tree is:\n");
        System.out.println("Entering the global scope:");
        SemanticAnalyzer analyzer = new SemanticAnalyzer();
        result.accept(analyzer, 0, false);
        analyzer.printScope(0);
        System.out.println("Leaving the global scope");

        if (print_code) {
          PrintStream o = new PrintStream(new File(argv[0].replace(".cm", ".tm")));
          System.setOut(o);
          CodeGenerator generator = new CodeGenerator();
          generator.visit(result);
        }
      }
    } catch (Exception e) {
      /* do cleanup here -- possibly rethrow e */
      e.printStackTrace();
    }
  }
}
