import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.apache.commons.cli.*;
import org.apache.commons.cli.ParseException;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class GoCompiler {
    public static void main(String[] args) throws IOException {
        String goFile = "";
        boolean printAST = false;
        boolean isCompile = false, isLiveness = false;

        //parse commandline arguments
        CommandLine commandLine;
        Option option_compile = Option.builder("compile")
                .required(false)
                .desc("compile the code")
                .hasArg()
                .build();
        Option option_liveness = Option.builder("liveness")
                .required(false)
                .desc("liveness")
                .hasArg()
                .build();
        Option option_ast = Option.builder("ast")
                .required(false)
                .desc("Print AST")
                .build();
        Options options = new Options();
        CommandLineParser argParser = new DefaultParser();
        options.addOption(option_compile);
        options.addOption(option_liveness);
        options.addOption(option_ast);
        try {
            commandLine = argParser.parse(options, args);
            if (commandLine.hasOption("compile")) {
                goFile = commandLine.getOptionValue("compile");
                isCompile = true;
            } else if (commandLine.hasOption("liveness")) {
                goFile = commandLine.getOptionValue("liveness");
                isLiveness = true;
            } else {
                throw new ParseException("Missing compile/liveness argument");
            }
            if (commandLine.hasOption("ast")) {
                printAST = true;
            }
        } catch (ParseException exception) {
            System.err.println("Argument error: ");
            System.err.println(exception.getMessage());
            System.exit(1);
        }

        //lexing
        InputStream input = new FileInputStream(goFile);
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
        } catch(GoParseException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }

        //typechecking
        TypeChecker typeChecker = new TypeChecker(symbolTableCreator);
        try {
            typeChecker.check(astCreator.AST);
        } catch (GoParseException | TypeCheckException  e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }

        System.out.println("Parsing success.");
        System.out.println("Typechecking success.");

        //TODO: Kontrollflussanalyse

        //optional AST to std.out
        if(printAST) {
            System.out.println("\nAbstract Syntax Tree :");
            System.out.println(astCreator.AST.toString());
        }
    }
}
