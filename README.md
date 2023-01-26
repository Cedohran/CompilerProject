Java version: 19.0.1 (openJDK), ANTLR version: 4.11.1, OS: Win10,

Jasmin version: 2.4


how to execute:

1. ANTLR
   java -jar ./lib/antlr-4.11.1-complete.jar GoLexer.g4
   java -jar ./lib/antlr-4.11.1-complete.jar GoParser.g4

2. compile Java
   javac *.java -d ./out/ -cp ./lib/*;.

3. run Java
   java -cp ./lib/*;.;./out/ StupsCompiler -compile [path to .go file] -jb(optional) -ast(optional)

WIP:
java -cp ./lib/*;.;./out/ StupsCompiler -liveness [path to .go file]

Arguments:
-jb: compiliert direkt den Java Bytecode
-ast: gibt den AST aus

Wo die .j (ggf. auch .class) Datei landet:
Gleiches Verzeichnis wie die .go Datei
