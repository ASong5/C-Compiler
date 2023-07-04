JAVA=java
JAVAC=javac
JFLEX=jflex
#CLASSPATH=-cp /usr/share/java/cup.jar:.
# CUP=cup
CLASSPATH=-cp ~/projects/java-cup-11b.jar:.
CUP=$(JAVA) $(CLASSPATH) java_cup.Main

all: Main.class

Main.class: absyn/*.java parser.java sym.java Lexer.java ShowTreeVisitor.java SemanticAnalyzer.java CodeGenerator.java Scanner.java Main.java

%.class: %.java
	$(JAVAC) $(CLASSPATH) -g $^

Lexer.java: cm.flex
	$(JFLEX) cm.flex

parser.java: cm.cup
	#$(CUP) -dump -expect 3 c-.cup
	$(CUP) -expect 3 cm.cup

clean:
	rm -f parser.java Lexer.java sym.java *.class absyn/*.class *.abs *.sym *.tm *~
