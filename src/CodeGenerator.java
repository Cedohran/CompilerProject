import java.util.HashMap;
import java.util.Map;

public class CodeGenerator {
    //symbol table
    SymbolTableCreator creator;
    private String currentFunc = "";
    private StringBuilder codeBuilder;
    private AstNode ast;
    //Map for variables to ID
    private Map<String, Integer> varToIdTable = new HashMap<>();
    private int varCounter = 0;

    CodeGenerator(AstNode root, SymbolTableCreator symbolTableCreator) {
        this.ast = root;
        this.creator = symbolTableCreator;
        //static code
        codeBuilder = new StringBuilder(".class public MyGen\n" +
                ".super java/lang/Object\n" +
                "\n" +
                ";\n" +
                "; standard initializer (calls java.lang.Object's initializer)\n" +
                ";\n" +
                ".method public <init>()V\n" +
                "   aload_0\n" +
                "   invokenonvirtual java/lang/Object/<init>()V\n" +
                "   return\n" +
                ".end method\n" +
                "\n" +
                ";\n" +
                "; main()\n" +
                ";\n" +
                ".method public static main([Ljava/lang/String;)V\n" +
                "    ; set limits used by this method\n" +
                "    .limit locals 4\n" +
                "    .limit stack 3\n" +
                "    ;generated code\n");
    }

    public String code() {
        visit(ast);
        //static end code
        codeBuilder.append("""
                    ;done
                    return
                .end method""");
        return codeBuilder.toString();
    }

    private void visit(AstNode node) {
        for(AstNode child : node.children()) {
            switch (child.getText()) {
                case "func" -> {
                    currentFunc = child.children().get(0).getText();
                    varToIdTable = new HashMap<>();
                    varCounter = 0;
                }
                case "func_invoc" -> funcInvocGen(child);
                case "var_init" -> varInitGen(child);
            }
            visit(child);
        }
    }

    private void varInitGen(AstNode varInitNode) {
        varCounter++;
        String varId = varInitNode.children().get(0).getText();
        varToIdTable.put(varId, varCounter);
        AstNode varExprNode = varInitNode.children().get(2);
        exprGen(varExprNode);
        codeBuilder.append("istore ")
                .append(varToIdTable.get(varId))
                .append("\n");
    }

    private void exprGen(AstNode exprNode) {
        DataType exprType = exprNode.dataType();
        //intExprGen(exprNode);
        switch (exprType) {
            case INT -> intExprGen(exprNode);
        }
    }

    private void intExprGen(AstNode intExprNode) {
        //Terminal node (id or literal)
        if(!intExprNode.hasChild()) {
            if (intExprNode.nodeType() == AstNodeType.LIT) {
                codeBuilder.append("ldc ")
                        .append(intExprNode.getText())
                        .append("\n");
            } else if (intExprNode.nodeType() == AstNodeType.ID) {
                codeBuilder.append("iload ")
                        .append(varToIdTable.get(intExprNode.getText()))
                        .append("\n");
            }
        }
        //one child -> skip
        else if(intExprNode.children().size() == 1) {
            intExprGen(intExprNode.children().get(0));
        }
        //unary operator
        else if(intExprNode.children().size() == 2) {
            codeBuilder.append("ldc ")
                    .append(intExprNode.children().get(0).getText())
                    .append(intExprNode.children().get(1).getText())
                    .append("\n");
        }
        //operator
        else if(intExprNode.children().size() == 3) {
            //left operand
            intExprGen(intExprNode.children().get(0));
            //right operand
            intExprGen(intExprNode.children().get(2));
            String operator = intExprNode.children().get(1).getText();
            switch (operator) {
                case "*" -> codeBuilder.append("imul\n");
                case "/" -> codeBuilder.append("idiv\n");
                case "+" -> codeBuilder.append("iadd\n");
                case "-" -> codeBuilder.append("isub\n");
            }
        }
    }

    private void funcInvocGen(AstNode funcInvocNode) {
        String funcId = funcInvocNode.children().get(0).getText();
        AstNode invocParamNode = funcInvocNode.children().get(1);

        //generate code for Println(), only one parameter
        if(funcId.equals("Println")) {
            //push System.out
            codeBuilder.append("getstatic java/lang/System/out Ljava/io/PrintStream;\n");
            //generate code for invocation parameter, that should be printed
            AstNode invocParamExprNode = invocParamNode.children().get(0);
            exprGen(invocParamExprNode);
            //invoke println()
            switch(invocParamExprNode.dataType()) {
                case INT -> codeBuilder.append("invokevirtual java/io/PrintStream/println(I)V\n");
                case STR -> codeBuilder.append("invokevirtual java/io/PrintStream/println(Ljava/lang/String;)V\n");
                case FLOAT -> codeBuilder.append("invokevirtual java/io/PrintStream/println(D)V\n");
                case BOOL -> codeBuilder.append("invokevirtual java/io/PrintStream/println(B)V\n");
            }

        }
    }

    private String getValueAsString(AstNode node) {
        if(node.hasChild())
            return getValueAsString(node.children().get(0));
        return node.getText();
    }

}
