import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.misc.ParseCancellationException;
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
        lexer.removeErrorListeners();
        lexer.addErrorListener(ThrowingErrorListener.INSTANCE);
        CommonTokenStream tokens = null;
        try {
            tokens = new CommonTokenStream(lexer);
        } catch(ParseCancellationException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }

        //parsing
        GoParser parser = new GoParser( tokens );
        parser.removeErrorListeners();
        parser.addErrorListener(ThrowingErrorListener.INSTANCE);
        ParseTree tree = null;
        try {
            tree = parser.program();
        } catch(ParseCancellationException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }

        //AST creation
        ParseTreeWalker walker = new ParseTreeWalker();
        VisitorAstCreator astCreator =  new VisitorAstCreator();
        try {
            walker.walk(astCreator, tree);
        } catch (Exception e) {
            System.err.println("Parsing failed at AST creation.");
            System.exit(1);
        }

        //symbol table creation
        SymbolTableCreator symbolTableCreator = new SymbolTableCreator();
        walker.walk(symbolTableCreator, tree);
        try {
            symbolTableCreator.problems();
        } catch(ParseException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }

        //typechecking
        TypeChecker typeChecker = new TypeChecker(symbolTableCreator);
        try {
            typeChecker.check(astCreator.AST);
        } catch (ParseException | TypeCheckException  e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }

        System.out.println("Parsing success.");
        System.out.println("Typechecking success.");

        //optional AST to std.out
        if(args.length > 1 && args[1].equals("ast")) {
            System.out.println("\nAbstract Syntax Tree :\n");
            System.out.println(astCreator.AST.toString());
        }
    }
}
