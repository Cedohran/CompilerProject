import java.util.*;

public class CodeGenerator {
    private final String className;
    //symbol table
    SymbolTableCreator symbolTable;
    private String currentFunc = "";
    private StringBuilder codeBuilder;
    private AstNode ast;
    //Map for variables to ID (starts at: 1)
    private Map<String, Integer> varToIdTable = new HashMap<>();
    //TODO: counter for each function?
    private int varCounter = 0;
    //if else label counters (starts at: 1)
    int ifElseCounter = 0;
    int ifElseTemp = 0;
    int ifNestedCounter = 0;
    //for loop
    int forLoopCounter = -1;
    int forLoopNestedCounter = 0;
    //counter for boolean values (eg. comparisons)
    int boolCounter = 0;

    //string format stuff
    String preTab = "";
    int tabIt = 0;


    CodeGenerator(AstNode root, SymbolTableCreator symbolTableCreator, String className) {
        this.ast = root;
        this.symbolTable = symbolTableCreator;
        this.className = className;
        //static code
        codeBuilder = new StringBuilder(".class public " + className + "\n" +
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
                "\n");
    }

    public String code() {
        visit(ast);
        return codeBuilder.toString();
    }

    private void visit(AstNode node) {
        //enter node
        //things to do
        boolean skipChildren = false;
        switch (node.getText()) {
            case "func" -> {
                currentFunc = node.children().get(0).getText();
                varCounter = 0;
                funcGen(node);
            }
            case "func_invoc" -> {
                funcInvocGen(node);
                skipChildren = true;
            }
            case "func_return" -> {
                funcReturnGen(node);
                skipChildren = true;
            }
            case "if_statement" -> ifStatementGen(node);
            case "else_statement" -> elseGen();
            case "for_loop" -> forLoopGen(node);
            case "var_init" -> {
                varInitGen(node);
                skipChildren = true;
            }
            case "var_assign" -> {
                varAssignGen(node);
                skipChildren = true;
            }
        }
        if(!skipChildren) {
            for (AstNode child : node.children()) {
                visit(child);
            }
        }
        //exit node
        switch (node.getText()) {
            case "func" -> exitFunc(node);
            //end of if_statement (for goto else_skip)
            case "if_statement" -> elseSkip();
            case "for_loop" -> forLoopEnd();
        }
    }


    private void funcGen(AstNode funcNode) {
        String funcId = funcNode.children().get(0).getText();

        if(funcId.equals("main")) {
            //skip args param
            varCounter = 1;
            codeBuilder.append("""
                    ;
                    ; main()
                    ;
                    .method public static main([Ljava/lang/String;)V
                        ; set limits used by this method
                        .limit locals 255
                        .limit stack 255
                    """).append("    ");
        } else {
            String paramString = getFuncParams(funcId);
            String returnType = getFuncReturnTypeAsString(funcId);
            codeBuilder.append(".method public static ").append(funcId)
                    .append("(").append(paramString).append(")")
                    .append(returnType).append("\n")
                    .append("    ; set limits used by this method\n")
                    .append("    .limit locals 255\n")
                    .append("    .limit stack 255\n").append("    ");
            //init params as variables
            List<String> paramNames = symbolTable.symbolTableFuncParamName.get(funcId);
            for(String varId : paramNames) {
                varToIdTable.put(funcId + varId, varCounter);
                varCounter++;
            }
        }
        tabInc();
    }

    private void exitFunc(AstNode funcNode) {
        //pls java, there is a return, now shut up
        codeBuilder.append("return\n");
        codeBuilder.append(".end method\n\n\n");
        tabDec();
    }

    private void funcReturnGen(AstNode funcReturnNode){
        AstNode returnExpr = new AstNode();
        if(funcReturnNode.hasChild()) {
            returnExpr = funcReturnNode.children().get(0);
            exprGen(returnExpr);
        }
        if(symbolTable.symbolTableFuncReturn.get(currentFunc) != null) {
            DataType retType = symbolTable.symbolTableFuncReturn.get(currentFunc);
            //cast
            if(retType == DataType.FLOAT && returnExpr.dataType() == DataType.INT){
                codeBuilder.append("i2f\n").append(preTab);
            }
            String retTypePrefix = getTypePrefix(retType);
            //return
            codeBuilder.append(retTypePrefix).append("return\n").append(preTab);
        }
    }

    private void funcInvocGen(AstNode funcInvocNode) {
        String funcId = funcInvocNode.children().get(0).getText();
        AstNode invocParamNode = new AstNode();
        String invocParams = getFuncParams(funcId);
        List<DataType> funcParamTypes = symbolTable.symbolTableFuncParamType.get(funcId);
        int i = 0;
        //check params and generate param code
        if(funcInvocNode.children().size() == 2) {
            invocParamNode = funcInvocNode.children().get(1);
            exprGen(invocParamNode.children().get(0));
            if(funcParamTypes != null) {
                //cast
                if(funcParamTypes.get(i) == DataType.FLOAT && invocParamNode.children().get(0).dataType() == DataType.INT){
                    codeBuilder.append("i2f\n").append(preTab);
                }
                i++;
            }
            while(invocParamNode.children().size() == 2) {
                invocParamNode = invocParamNode.children().get(1);
                exprGen(invocParamNode.children().get(0));
                //cast
                if(funcParamTypes != null) {
                    //cast
                    if(funcParamTypes.get(i) == DataType.FLOAT && invocParamNode.children().get(0).dataType() == DataType.INT){
                        codeBuilder.append("i2f\n").append(preTab);
                    }
                    i++;
                }
            }
            //reset node
            invocParamNode = funcInvocNode.children().get(1);
        }
        //generate code for Println(), only one parameter
        if(funcId.equals("Println")) {
            //push System.out
            codeBuilder.append("getstatic java/lang/System/out Ljava/io/PrintStream;\n").append(preTab);
            //swap std out and previously generated param
            codeBuilder.append("swap\n").append(preTab);
            //invoke println()
            DataType exprType = invocParamNode.children().get(0).dataType();
            switch(exprType) {
                case INT -> codeBuilder.append("invokevirtual java/io/PrintStream/println(I)V\n").append(preTab);
                case STR -> codeBuilder.append("invokevirtual java/io/PrintStream/println(Ljava/lang/String;)V\n").append(preTab);
                case FLOAT -> codeBuilder.append("invokevirtual java/io/PrintStream/println(F)V\n").append(preTab);
                case BOOL -> codeBuilder.append("invokevirtual java/io/PrintStream/println(Z)V\n").append(preTab);
            }
        } else {
            codeBuilder.append("invokestatic ").append(className).append("/").append(funcId)
                    .append("(").append(invocParams).append(")")
                    .append(getFuncReturnTypeAsString(funcId)).append("\n").append(preTab);
        }
        //invoc done -> delete for later
        funcInvocNode.setText("done_invoc");
    }

    private String getFuncParams(String funcId) {
        StringBuilder paramString = new StringBuilder();
        List<DataType> funcParams = new ArrayList<>();
        if(symbolTable.symbolTableFuncParamType.get(funcId) != null) {
            funcParams = symbolTable.symbolTableFuncParamType.get(funcId);
        }
        if(!funcParams.isEmpty()) {
            int i = 0;
            for(DataType param : funcParams) {
                switch(param) {
                    //[ : array
                    case INT -> paramString.append("I");
                    case FLOAT -> paramString.append("F");
                    case STR -> paramString.append("Ljava/lang/String;");
                    case BOOL -> paramString.append("Z");
                }
            }
        }
        return paramString.toString();
    }

    public String getFuncReturnTypeAsString(String funcId) {
        DataType retType = symbolTable.symbolTableFuncReturn.get(funcId);
        switch (retType){
            case INT -> {
                return "I";
            }
            case FLOAT -> {
                return "F";
            }
            case STR -> {
                return "Ljava/lang/String;";
            }
            case BOOL -> {
                return "Z";
            }
            case UNDEF -> {
                return "V";
            }
        }
        return "V";
    }

    private void ifStatementGen(AstNode ifNode) {
        ifNestedCounter++;
        ifElseTemp++;
        AstNode ifExpr = ifNode.children().get(0);
        exprGen(ifExpr);
        //is there an else?
        if(ifNode.children().size() == 3) {
            //check if ifExpr is false -> jump to else
            codeBuilder.append("ldc 0\n").append(preTab);
            codeBuilder.append("if_icmpeq else").append(ifNestedCounter+ifElseCounter).append("\n");
        } //no else
        else {
            //check if ifExpr is false -> jump to else_skip
            codeBuilder.append("ldc 0\n").append(preTab);
            codeBuilder.append("if_icmpeq else_skip").append(ifNestedCounter+ifElseCounter).append("\n");
        }
        tabInc();
        codeBuilder.append(preTab);
    }

    private void elseGen() {
        //add else-skip for previous if
        codeBuilder.append("goto else_skip").append(ifNestedCounter+ifElseCounter).append("\n");
        tabDec();
        codeBuilder.append(preTab);
        //add else label for previous if goto
        codeBuilder.append("else").append(ifNestedCounter+ifElseCounter).append(":\n");
        tabInc();
        codeBuilder.append(preTab);
    }

    private void elseSkip() {
        //the else skip goto jump 3000
        codeBuilder.append("else_skip").append(ifNestedCounter+ifElseCounter).append(":\n");
        ifNestedCounter--;
        if(ifNestedCounter == 0) {
            ifElseCounter += ifElseTemp;
            ifElseTemp = 0;
        }
        tabDec();
        codeBuilder.append(preTab);
    }

    private void forLoopGen(AstNode forLoopNode) {
        forLoopCounter++;
        forLoopNestedCounter++;
        codeBuilder.append("for_start").append(forLoopNestedCounter + forLoopCounter).append(":\n").append(preTab);
        AstNode forExpr = forLoopNode.children().get(0);
        exprGen(forExpr);
        //check if forExpr is false -> skip loop body
        codeBuilder.append("ldc 0\n").append(preTab);
        codeBuilder.append("if_icmpeq for_end").append(forLoopNestedCounter + forLoopCounter).append("\n");
        tabInc();
        codeBuilder.append(preTab);
    }

    private void forLoopEnd() {
        //the for loop skip goto jump 3000
        codeBuilder.append("goto for_start").append(forLoopNestedCounter + forLoopCounter).append("\n").append(preTab);
        codeBuilder.append("for_end").append(forLoopNestedCounter + forLoopCounter).append(":\n");
        forLoopNestedCounter--;
        tabDec();
        codeBuilder.append(preTab);
    }

    private void varInitGen(AstNode varInitNode) {
        DataType varInitType = varInitNode.dataType();
        String typePrefix = getTypePrefix(varInitType);

        String varId = varInitNode.children().get(0).getText();
        varToIdTable.put(currentFunc+varId, varCounter);
        AstNode varExprNode = varInitNode.children().get(2);
        exprGen(varExprNode);
        //cast
        if(varInitType == DataType.FLOAT && varExprNode.dataType() == DataType.INT){
            codeBuilder.append("i2f\n").append(preTab);
        }
        codeBuilder.append(typePrefix).append("store ")
                .append(varToIdTable.get(currentFunc+varId))
                .append("\n").append(preTab);
        varCounter++;
    }

    private void varAssignGen(AstNode assignNode) {
        DataType assignType = assignNode.dataType();
        String typePrefix = getTypePrefix(assignType);

        String varId = assignNode.children().get(0).getText();
        AstNode varExprNode = assignNode.children().get(1);
        exprGen(varExprNode);
        //cast
        if(assignType == DataType.FLOAT && varExprNode.dataType() == DataType.INT){
            codeBuilder.append("i2f\n").append(preTab);
        }
        codeBuilder.append(typePrefix).append("store ")
                .append(varToIdTable.get(currentFunc+varId))
                .append("\n").append(preTab);
    }

    private void exprGen(AstNode exprNode) {
        //TODO: func_invoc casts + var_assign casts
        DataType exprType = exprNode.dataType();
        String typePrefix = getTypePrefix(exprType);
        //func_invoc
        if(exprNode.getText().equals("func_invoc")) {
            funcInvocGen(exprNode);
            return;
        }
        //Terminal node (id or literal)
        if(!exprNode.hasChild()) {
            if (exprNode.nodeType() == AstNodeType.LIT) {
                //true - 1 ; false - 0
                if(exprType == DataType.BOOL) {
                    switch (exprNode.getText()) {
                        case "true" -> codeBuilder.append("ldc 1\n").append(preTab);
                        case "false" -> codeBuilder.append("ldc 0\n").append(preTab);
                    }
                } else {
                    codeBuilder.append("ldc ")
                            .append(exprNode.getText())
                            .append("\n").append(preTab);
                }
            } else if (exprNode.nodeType() == AstNodeType.ID) {
                codeBuilder.append(typePrefix).append("load ")
                        .append(varToIdTable.get(currentFunc+exprNode.getText()))
                        .append("\n").append(preTab);
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
                    case "false" -> codeBuilder.append("ldc 1\n").append(preTab);
                    case "true" -> codeBuilder.append("ldc 0\n").append(preTab);
                }
            }
            codeBuilder.append("ldc ")
                    .append(exprNode.children().get(0).getText())
                    .append(exprNode.children().get(1).getText())
                    .append("\n").append(preTab);
        }
        //operator
        else if(exprNode.children().size() == 3) {
            AstNode leftOp = exprNode.children().get(0);
            AstNode rightOp = exprNode.children().get(2);
            AstNode operator = exprNode.children().get(1);
            //left operand
            exprGen(leftOp);
            //cast int to float for implicit typecast
            if(exprType == DataType.FLOAT && leftOp.dataType() == DataType.INT) {
                codeBuilder.append("i2f\n").append(preTab);
            }
            //cast int to float for comparison
            else if(operator.nodeType() == AstNodeType.CMP_SMBL && leftOp.dataType() == DataType.INT) {
                codeBuilder.append("i2f\n").append(preTab);
            }
            //right operand
            exprGen(rightOp);
            //cast int to float for implicit typecast
            if(exprType == DataType.FLOAT && rightOp.dataType() == DataType.INT) {
                codeBuilder.append("i2f\n").append(preTab);
            }
            //cast int to float for comparison
            else if((operator.nodeType() == AstNodeType.CMP_SMBL) && rightOp.dataType() == DataType.INT) {
                codeBuilder.append("i2f\n").append(preTab);
            }
            switch (operator.getText()) {
                case "||" -> codeBuilder.append("ior\n").append(preTab);
                case "&&" -> codeBuilder.append("iand\n").append(preTab);
                //fcmpl:
                //-1 - left kleiner ; 0 - gleich ; 1 - rechts kleiner
                case "<", ">", "==", "!=", "<=", ">=" -> {
                    //string is special >:(
                    if(leftOp.dataType() == DataType.STR) {
                        boolCounter++;
                        if(operator.getText().equals("==")) {
                            codeBuilder.append("if_acmpeq true").append(boolCounter).append("\n").append(preTab);
                        } else {
                            codeBuilder.append("if_acmpne true").append(boolCounter).append("\n").append(preTab);
                        }
                        //load false
                        codeBuilder.append("ldc 0\n").append(preTab)
                                .append("goto skip_true").append(boolCounter).append("\n").append(preTab)
                                .append("true").append(boolCounter).append(":\n").append(preTab)
                                //load true
                                .append("ldc 1\n").append(preTab)
                                .append("skip_true").append(boolCounter).append(":\n").append(preTab);
                    } else {
                        codeBuilder.append("fcmpl\n").append(preTab);
                        comparisonGen(operator.getText());
                    }
                }
                case "*" -> codeBuilder.append(typePrefix).append("mul\n").append(preTab);
                case "/" -> codeBuilder.append(typePrefix).append("div\n").append(preTab);
                case "+" -> {
                    if(exprType == DataType.STR) {
                        strConcatGen();
                    } else {
                        codeBuilder.append(typePrefix).append("add\n").append(preTab);
                    }
                }
                case "-" -> codeBuilder.append(typePrefix).append("sub\n").append(preTab);
            }

        }
    }

    private void strConcatGen() {
        codeBuilder.append("astore ").append(varCounter).append("\n").append(preTab);
        codeBuilder.append("astore ").append(varCounter+1).append("\n").append(preTab);
        //solution found on http://www2.cs.uidaho.edu/~jeffery/courses/445/code-jasmin.html 21.01.2023
        codeBuilder.append("new java/lang/StringBuilder\n").append(preTab)
                .append("dup\n").append(preTab)
                .append("invokespecial java/lang/StringBuilder/<init>()V\n").append(preTab)
                .append("aload ").append(varCounter+1).append("\n").append(preTab)
                .append("invokevirtual java/lang/StringBuilder/append(Ljava/lang/String;)Ljava/lang/StringBuilder;\n").append(preTab)
                .append("aload ").append(varCounter).append("\n").append(preTab)
                .append("invokevirtual java/lang/StringBuilder/append(Ljava/lang/String;)Ljava/lang/StringBuilder;\n").append(preTab)
                .append("invokevirtual java/lang/StringBuilder/toString()Ljava/lang/String;\n").append(preTab);
    }

    private void comparisonGen(String operator) {
        boolCounter++;
        switch(operator) {
            case "<=", ">=" -> {
                if(operator.equals("<="))
                    codeBuilder.append("ifgt skip_true").append(boolCounter).append("\n").append(preTab);
                else
                    codeBuilder.append("iflt skip_true").append(boolCounter).append("\n").append(preTab);
                //load true
                codeBuilder.append("ldc 1\n").append(preTab);
                codeBuilder.append("goto skip_false").append(boolCounter).append("\n").append(preTab);
                //load false
                codeBuilder.append("skip_true").append(boolCounter).append(":\n").append(preTab)
                        .append("ldc 0\n").append(preTab)
                        .append("skip_false").append(boolCounter).append(":\n").append(preTab);
                return;
            }
            case "<" -> codeBuilder.append("iflt true").append(boolCounter).append("\n").append(preTab);
            case ">" -> codeBuilder.append("ifgt true").append(boolCounter).append("\n").append(preTab);
            case "==" -> codeBuilder.append("ifeq true").append(boolCounter).append("\n").append(preTab);
            case "!=" -> codeBuilder.append("ifneq true").append(boolCounter).append("\n").append(preTab);
        }
        codeBuilder
                //load false
                .append("ldc 0\n").append(preTab)
                .append("goto skip_true").append(boolCounter).append("\n").append(preTab)
                .append("true").append(boolCounter).append(":\n").append(preTab)
                //load true
                .append("ldc 1\n").append(preTab)
                .append("skip_true").append(boolCounter).append(":\n").append(preTab);
    }

    private String getTypePrefix(DataType type) {
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
        return "";
    }

    private void tabInc() {
        tabIt++;
        preTab = "";
        for(int i = 1; i <= tabIt; i++){
            preTab += "    ";
        }
    }

    private void tabDec(){
        tabIt--;
        preTab = "";
        for(int i = 1; i <= tabIt; i++){
            preTab += "    ";
        }
    }
}
