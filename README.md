Java version: 19.0.1 (openJDK), ANTLR version: 4.11.1, OS: Win10,

Jasmin version: http://jasmin.sourceforge.net/ (13.01.2023)


how to execute:

1. ANTLR
   java -jar ./lib/antlr-4.11.1-complete.jar GoLexer.g4
   java -jar ./lib/antlr-4.11.1-complete.jar GoParser.g4

2. compile Java
   javac *.java -d ./out/ -cp ./lib/*;.

3. run Java
   java -cp ./lib/*;.;./out/ GoCompiler -compile [path to .go file] -ast(optional)

WIP:
java -cp ./lib/*;.;./out/ GoCompiler -liveness [path to .go file] -ast(optional)

(-ast: print the AST to std out)
