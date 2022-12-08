import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class Main {
    public static void main(String[] args) throws IOException {
        InputStream input = new FileInputStream("visit.txt");

        GoLexer lexer = new GoLexer(CharStreams.fromStream(input));
        CommonTokenStream tokens = new CommonTokenStream( lexer );

        GoParser parser = new GoParser( tokens );
        ParseTree tree = parser.program();
        ParseTreeWalker walker = new ParseTreeWalker();
        //System.out.println(tree.toStringTree());

        GoVisitor visitor = new GoVisitor();
        walker.walk(visitor, tree);

        GoVisitorAstCreator creator =  new GoVisitorAstCreator();
        walker.walk(creator, tree);
        System.out.println(creator.ast.toString());


        //System.out.println(parser.creator.programTree.toString());

//        visitor.varMap.forEach((k, v) -> {
//            System.out.println("ID: "+k+" Type: "+v.getName());
//        });
    }
}
