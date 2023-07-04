# C-Compiler
Implementation of a compiler for a minified C language (C-). Made by Andrew Song and Brendan Yawney as a project for CIS4650 at the University of Guelph.

This compiler encompasses the three fundamental layers of the modern-day compiler:
- Lexical and Syntactic Analysis (scanner and parser)
- Semantic Analysis (type-checking and symbol table)
- Code Generation

The program has the ability to report both syntactic and semantic errors, as well as dynamically generate syntax trees for the input program. It can perform code generation and run minified C programs by transforming them into machine code and feeding the result into a simulator (TM Simulator).

# Technologies
- Java
- Scanner/Lexical-Analyzer made with JFlex
- Parser/Grammar construction made with CUP

The user must specify the proper paths to these dependencies in the makefile. In
particular, the CLASSPATH variable must be set properly. By default it is set to 
"CLASSPATH=-cp /usr/share/java/cup.jar:."

Once prerequistes have been installed, to build this program, run the make command in the same 
directory as the makefile. Be sure to run the makefile for the TMSimulator as well if you are looking to compile the .cm program.

# How to Run

Once the project has been successfully built, it can be run using the command:

java -cp /usr/share/java/cup.jar:. Main *.cm -[a,s,c]

Where *.cm is the name of a C- file and the flags:
-a performs syntactic analysis and outputs a .abs file (syntax tree),
-s performs type checking and outputs a .sym file (symbol table),
-c compiles the .cm file and outputs its corresponding assembly code as a .tm file
    - to run the TM simulator, cd into the TMSimulator folder and run the command: 
        ./tm *.cm 
        Where *.cm is the path to the C- file.
