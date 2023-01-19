import java.util.HashMap;
import java.util.Map;

public class CodeGenerator {
    //symbol table
    SymbolTableCreator creator;
    private String currentFunc = "";
    private StringBuilder codeBuilder;
    private AstNode ast;
    //Map for variables to ID (starts at: 1)
    private Map<String, Integer> varToIdTable = new HashMap<>();
    private int varCounter = 0;
    //if else label counters (starts at: 1)
    int ifElseLabelCounter = 0;
    //if nest counter
    int ifNestCounter = -1;
    //counter for boolean values (eg. comparisons)
    int boolCounter = 0;

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
        //enter node
        for(AstNode child : node.children()) {
            switch (child.getText()) {
                case "func" -> {
                    currentFunc = child.children().get(0).getText();
                    varToIdTable = new HashMap<>();
                    varCounter = 0;
                }
                case "func_invoc" -> funcInvocGen(child);
                case "if_statement" -> ifStatementGen(child);
                case "else_statement" -> elseGen(child);
                case "var_init" -> varInitGen(child);
            }
            //prevNodeText = child.getText();
            visit(child);
        }
        //exit node
        //after else_statement (for goto else_skip)
        if(node.getText().equals("else_statement")) {
            elseSkip();
        }
    }


    private void ifStatementGen(AstNode ifNode) {
        ifElseLabelCounter++;
        ifNestCounter++;
        AstNode ifExpr = ifNode.children().get(0);
        exprGen(ifExpr);
        //check if ifExpr is false --> jump to else
        codeBuilder.append("ldc 0\n");
        codeBuilder.append("if_icmpeq else").append(ifElseLabelCounter).append("\n");
    }

    private void elseGen(AstNode elseNode) {
        //add else-skip for previous if
        codeBuilder.append("goto else_skip").append(ifElseLabelCounter-ifNestCounter).append("\n");
        //add else label for previous if goto
        codeBuilder.append("else").append(ifElseLabelCounter-ifNestCounter).append(":\n");
    }

    private void elseSkip() {
        //the else skip goto jump 3000
        codeBuilder.append("else_skip").append(ifElseLabelCounter-ifNestCounter).append(":\n");
    }

    private void varInitGen(AstNode varInitNode) {
        varCounter++;
        DataType varInitType = varInitNode.dataType();
        String typePrefix = setTypePrefix(varInitType);

        String varId = varInitNode.children().get(0).getText();
        varToIdTable.put(varId, varCounter);
        AstNode varExprNode = varInitNode.children().get(2);
        exprGen(varExprNode);
        codeBuilder.append(typePrefix).append("store ")
                .append(varToIdTable.get(varId))
                .append("\n");
    }

    private void exprGen(AstNode exprNode) {
        DataType exprType = exprNode.dataType();
        String typePrefix = setTypePrefix(exprType);
        //Terminal node (id or literal)
        if(!exprNode.hasChild()) {
            if (exprNode.nodeType() == AstNodeType.LIT) {
                //true - 1 ; false - 0
                if(exprType == DataType.BOOL) {
                    switch (exprNode.getText()) {
                        case "true" -> codeBuilder.append("ldc 1\n");
                        case "false" -> codeBuilder.append("ldc 0\n");
                    }
                } else {
                    codeBuilder.append("ldc ")
                            .append(exprNode.getText())
                            .append("\n");
                }
            } else if (exprNode.nodeType() == AstNodeType.ID) {
                codeBuilder.append(typePrefix).append("load ")
                        .append(varToIdTable.get(exprNode.getText()))
                        .append("\n");
            }
        }
        //one child -> skip
        else if(exprNode.children().size() == 1) {
            exprGen(exprNode.children().get(0));
        }
        //unary operator
        else if(exprNode.children().size() == 2) {
            //!true - 0 ; !false - 1
            if(exprType == DataType.BOOL) {
                switch (exprNode.getText()) {
                    case "false" -> codeBuilder.append("ldc 1\n");
                    case "true" -> codeBuilder.append("ldc 0\n");
                }
            }
            codeBuilder.append("ldc ")
                    .append(exprNode.children().get(0).getText())
                    .append(exprNode.children().get(1).getText())
                    .append("\n");
        }
        //operator
        else if(exprNode.children().size() == 3) {
            AstNode leftOp = exprNode.children().get(0);
            AstNode rightOp = exprNode.children().get(2);
            AstNode operator = exprNode.children().get(1);
            //left operand
            exprGen(leftOp);
            //cast int to float for comparison
            if(operator.nodeType() == AstNodeType.CMP_SMBL && leftOp.dataType() == DataType.INT) {
                codeBuilder.append("i2f\n");
            }
            //right operand
            exprGen(rightOp);
            //cast int to float for comparison
            if(operator.nodeType() == AstNodeType.CMP_SMBL && rightOp.dataType() == DataType.INT) {
                codeBuilder.append("i2f\n");
            }
            switch (operator.getText()) {
                case "||" -> codeBuilder.append("ior\n");
                case "&&" -> codeBuilder.append("iand\n");
                //TODO: equals
                //fcmpl:
                //-1 - left kleiner ; 0 - gleich ; 1 - rechts kleiner
                case "<", ">", "<=", ">=", "==", "!=" -> {
                    //string is special >:(
                    if(leftOp.dataType() == DataType.STR) {
                        boolCounter++;
                        if(operator.getText().equals("==")) {
                            codeBuilder.append("if_acmpeq true").append(boolCounter).append("\n");
                        } else {
                            codeBuilder.append("if_acmpne true").append(boolCounter).append("\n");
                        }
                        //load false
                        codeBuilder.append("ldc 0\n")
                                .append("goto skip_true").append(boolCounter).append("\n")
                                .append("true").append(boolCounter).append(":\n")
                                //load true
                                .append("ldc 1\n")
                                .append("skip_true").append(boolCounter).append(":\n");
                    } else {
                        codeBuilder.append("fcmpl\n");
                        comparisonGen(operator.getText());
                    }
                }
                case "*" -> codeBuilder.append(typePrefix).append("mul\n");
                case "/" -> codeBuilder.append(typePrefix).append("div\n");
                case "+" -> codeBuilder.append(typePrefix).append("add\n");
                case "-" -> codeBuilder.append(typePrefix).append("sub\n");
            }

        }
    }

    private void comparisonGen(String operator) {
        boolCounter++;
        switch(operator) {
            case "<" -> codeBuilder.append("iflt true").append(boolCounter).append("\n");
            case ">" -> codeBuilder.append("ifgt true").append(boolCounter).append("\n");
            case "<=" -> codeBuilder.append("iflt true").append(boolCounter).append("\n")
                    .append("ifeq true").append(boolCounter).append("\n");
            case ">=" -> codeBuilder.append("ifgt true").append(boolCounter).append("\n")
                    .append("ifeq true").append(boolCounter).append("\n");
            case "==" -> codeBuilder.append("ifeq true").append(boolCounter).append("\n");
            case "!=" -> codeBuilder.append("ifneq true").append(boolCounter).append("\n");
        }
        codeBuilder
                //load false
                .append("ldc 0\n")
                .append("goto skip_true").append(boolCounter).append("\n")
                .append("true").append(boolCounter).append(":\n")
                //load true
                .append("ldc 1\n")
                .append("skip_true").append(boolCounter).append(":\n");
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
                case FLOAT -> codeBuilder.append("invokevirtual java/io/PrintStream/println(F)V\n");
                case BOOL -> codeBuilder.append("invokevirtual java/io/PrintStream/println(Z)V\n");
            }

        }
    }

    private String setTypePrefix(DataType type) {
        switch(type) {
            case INT, BOOL -> {
                return "i";
            }
            case FLOAT -> {
                return "f";
            }
            case STR -> {
                return "a";
            }
        }
        return "i";
    }
}
