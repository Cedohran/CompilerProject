import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.apache.commons.cli.*;
import org.apache.commons.cli.ParseException;

import java.io.*;
import java.util.Map;
import java.util.stream.Collectors;

public class StupsCompiler {
    public static void main(String[] args) throws IOException, InterruptedException {
        String goFile = "";
        String jasminFileName = "";
        String outputDir = "";
        boolean printAST = false;
        boolean isCompile = false, isLiveness = false;
        boolean toJb = false;

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
        Option option_toJb = Option.builder("jb")
                .required(false)
                .desc("Compile to Java Bytecode")
                .build();
        Option option_ast = Option.builder("ast")
                .required(false)
                .desc("Print AST")
                .build();
        Options options = new Options();
        CommandLineParser argParser = new DefaultParser();
        options.addOption(option_compile);
        options.addOption(option_liveness);
        options.addOption(option_toJb);
        options.addOption(option_ast);
        try {
            commandLine = argParser.parse(options, args);
            if (commandLine.hasOption("compile")) {
                goFile = commandLine.getOptionValue("compile");
                jasminFileName = goFile.split("/")[goFile.split("/").length-1].split("\\.")[0];
                int goFileNameLength = goFile.split("/")[goFile.split("/").length-1].length();
                outputDir = goFile.substring(0, goFile.length()-goFileNameLength);
                isCompile = true;
            } else if (commandLine.hasOption("liveness")) {
                goFile = commandLine.getOptionValue("liveness");
                isLiveness = true;
            } else {
                throw new ParseException("Missing compile/liveness argument");
            }
            if(commandLine.hasOption("jb")) {
                toJb = true;
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
        AstNode typecheckedTree = new AstNode();
        try {
            typecheckedTree = typeChecker.checkAndSet(astCreator.AST);
        } catch (GoParseException | TypeCheckException  e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
        System.out.println("Parsing and Typechecking success.");
        System.out.println();

        //flow control
        FlowControlChecker flowControlChecker = new FlowControlChecker(typecheckedTree, symbolTableCreator);
        try {
            flowControlChecker.check();
        } catch (FlowControlException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
        System.out.println("Flow control success.");
        System.out.println();

        //code generation
        File jasminFile = null;
        if(isCompile) {
            //generate jasmin bytecode
            CodeGenerator codeGenerator = new CodeGenerator(typecheckedTree, symbolTableCreator, jasminFileName);
            String jasminCode = codeGenerator.code();
            //generate file
            jasminFile = new File(outputDir+jasminFileName+".j");
            jasminFile.createNewFile();
            //write jasmin bytecode to file
            FileWriter writer = new FileWriter(jasminFile);
            writer.write(jasminCode);
            writer.close();
            System.out.println("Jasmin bytecode generated: "+jasminFile.getPath());
        }

        if(isLiveness) {
            System.out.println("Ouch! This feature is yet to be fully implemented! Our highly intelligent monkeys are working on it!");
            int maxVarSum = 0;
            for(Map.Entry<String, Map<String, DataType>> entry : symbolTableCreator.funcScopeTable.entrySet()) {
                int prevSum = maxVarSum;
                maxVarSum = entry.getValue().size();
                if(prevSum > maxVarSum)
                    maxVarSum = prevSum;
            }
            int registerCount = (maxVarSum+1) / 2;
            System.out.println("In the meantime, here is an educated guess:\nRegisters: "+registerCount);
        }

        //compile to java bytecode
        if(toJb) {
            if(jasminFile == null) {
                System.err.println("Java bytecode compilation error:\n.j file not found");
                System.exit(1);
            }
            String toJavaByteCode = "java -jar ./lib/jasmin.jar "+jasminFile.getPath()+" -d "+outputDir;
            System.out.println("executing: "+toJavaByteCode);
            Runtime rt = Runtime.getRuntime();
            Process pr = rt.exec(toJavaByteCode.split(" "));
            pr.waitFor();
            //java bytecode error handling
            String prError = pr.errorReader().lines().map(s -> s + "\n").collect(Collectors.joining());
            if(prError.equals("")) {
                System.out.println("Java bytecode generated: ./compiled_code/" + jasminFileName + ".class");
            } else {
                System.err.println("Java bytecode generation failed:\n"+prError);
            }
        }

        //optional AST to std.out
        if(printAST) {
            System.out.println("\nAbstract Syntax Tree :");
            System.out.println(astCreator.AST.toString());
        }
    }
}
