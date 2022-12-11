import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class GoCompiler {
    public static void main(String[] args) throws IOException {
        InputStream input = new FileInputStream("visit.txt");

        GoLexer lexer = new GoLexer(CharStreams.fromStream(input));
        CommonTokenStream tokens = new CommonTokenStream( lexer );

        GoParser parser = new GoParser( tokens );
        ParseTree tree = parser.program();
        ParseTreeWalker walker = new ParseTreeWalker();


        GoVisitorAstCreator astCreator =  new GoVisitorAstCreator();
        walker.walk(astCreator, tree);
        System.out.println(astCreator.AST.toString());

        SymbolTableCreator symbolTableCreator = new SymbolTableCreator();
        walker.walk(symbolTableCreator, tree);

        boolean typeError = false;
        GoTypeChecker typeChecker = new GoTypeChecker(symbolTableCreator);
        try {
            typeChecker.check(astCreator.AST);
        } catch (TypeCheckException e) {
            System.err.println(e.getMessage());
            typeError = true;
        }

        if(!typeError) System.out.println("TypeChecking success.");

        //System.out.println(parser.creator.programTree.toString());

//        visitor.varMap.forEach((k, v) -> {
//            System.out.println("ID: "+k+" Type: "+v.getName());
//        });
    }
}
