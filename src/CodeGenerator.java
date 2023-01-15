import java.util.HashMap;
import java.util.Map;

public class CodeGenerator {
    //symbol table
    SymbolTableCreator creator;
    private String currentFunc = "";
    private StringBuilder codeBuilder = new StringBuilder();
    private AstNode ast;
    //Map for variables to ID
    Map<String, Integer> varToIdTable = new HashMap<>();

    CodeGenerator(AstNode root, SymbolTableCreator symbolTableCreator) {
        this.ast = root;
        this.creator = symbolTableCreator;
    }

    public String code() {
        visit(ast);
        return codeBuilder.toString();
    }

    private void visit(AstNode node) {
        for(AstNode child : node.children()) {
            switch (child.getText()) {
                case "func" -> {
                    currentFunc = child.children().get(0).getText();
                    varToIdTable = new HashMap<>();
                }
                case "func_invoc" -> funcInvocGen(child);
                case "var_init" -> varInitGen(child);
            }
            visit(child);
        }
    }

    private void varInitGen(AstNode varInitNode) {
        String varId = varInitNode.children().get(0).getText();
        AstNode varExprNode = varInitNode.children().get(2);
        exprGen(varExprNode);
    }

    private void exprGen(AstNode exprNode) {
        DataType exprType = exprNode.dataType();
    }

    private void funcInvocGen(AstNode node) {
        String funcId = node.children().get(0).getText();
        AstNode funcParamNode = node.children().get(1);
        String funcInvocParamValue = getValueAsString(funcParamNode.children().get(0));
        if(funcId.equals("Println")) {
            //push System.out
            codeBuilder.append("getstatic java/lang/System/out Ljava/io/PrintStream;\n");
            //push String
            codeBuilder.append("ldc " + funcInvocParamValue + " \n");
            //invoke println()
            codeBuilder.append("invokevirtual java/io/PrintStream/println(Ljava/lang/String;)V\n");
        }
    }

    private String getValueAsString(AstNode node) {
        if(node.hasChild())
            return getValueAsString(node.children().get(0));
        return node.getText();
    }

}
