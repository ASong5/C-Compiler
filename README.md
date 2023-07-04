# C-Compiler
Implementation of a compiler for a minified C language. Made by Andrew Song and Brendan Yawney as a project for CIS4650 at the University of Guelph.

This compiler encompasses the three fundamental layers of the modern-day compiler:
- Lexical and Syntactic Analysis (scanner and parser)
- Semantic Analysis (type-checking and symbol table)
- Code Generation

The program has the ability to report both syntactic and semantic errors, as well as dynamically generate syntax trees for the input program. It can perform code generation and run minified C programs by transforming them into machine code and feeding the result into a simulator (TM Simulator).

# Technologies
- Java
- Scanner/Lexical-Analyzer made with JFlex
- Parser/Grammar construction made with CUP

