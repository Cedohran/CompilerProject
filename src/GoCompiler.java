import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class GoCompiler {
    public static void main(String[] args) throws IOException {
        if(args.length == 0) {
            System.err.println("Argument for file missing.");
            System.exit(1);
        }
        InputStream input = new FileInputStream(args[0]);

        //lexing
        GoLexer lexer = new GoLexer(CharStreams.fromStream(input));
        CommonTokenStream tokens = new CommonTokenStream( lexer );

        //parsing
        GoParser parser = new GoParser( tokens );
        ParseTree tree = parser.program();
        ParseTreeWalker walker = new ParseTreeWalker();

        //AST creation
        GoVisitorAstCreator astCreator =  new GoVisitorAstCreator();
        try {
            walker.walk(astCreator, tree);
        } catch (Exception e) {
            System.err.println("Parsing failed.");
            System.exit(1);
        }
        System.out.println("Parsing success.");

        //symbol table creation
        SymbolTableCreator symbolTableCreator = new SymbolTableCreator();
        walker.walk(symbolTableCreator, tree);

        //typechecking
        GoTypeChecker typeChecker = new GoTypeChecker(symbolTableCreator);
        try {
            typeChecker.check(astCreator.AST);
        } catch (TypeCheckException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
        System.out.println("Typechecking success.");

        //optional AST to std.out
        if(args.length > 1 && args[1].equals("ast")) {
            System.out.println("\nAbstract Syntax Tree :\n");
            System.out.println(astCreator.AST.toString());
        }
    }
}
