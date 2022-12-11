public class GoTypeChecker {
    //symbol table for variables with corresponding data type
    SymbolTableCreator creator;
    private String funcContext = "";

    GoTypeChecker(SymbolTableCreator symbolTableCreator) {
        this.creator = symbolTableCreator;
    }

    public void check(AstNode astRoot) throws TypeCheckException {
        //check if first function is main
        if(!astRoot.children().get(0).children().get(0).getText().equals("main")) {
            throw new TypeCheckException("first function must be 'main'");
        }
        visit(astRoot);
    }

    private void visit(AstNode node) throws TypeCheckException {
        for(AstNode child : node.children()) {
            switch (child.getText()) {
                case "func" -> funcContext = child.children().get(0).getText();
                case "if_statement" -> ifStatementCheck(child);
                case "for_loop" -> forLoopCheck(child);
                case "var_init" -> varInitCheck(child);
                case "var_assign" -> varAssignCheck(child);
                case "func_invoc" -> funcInvocCheck(child);
                case "func_return" -> funcReturnCheck(child);
            }
            visit(child);
        }
    }

    private void ifStatementCheck(AstNode node) throws TypeCheckException {
        AstNode ifExpr = node.children().get(0);
        DataType ifExprType = exprCheck(ifExpr);
        if(ifExprType != DataType.BOOL) {
            throw new TypeCheckException("if statement needs a boolean expression.");
        }
    }

    private void forLoopCheck(AstNode node) throws TypeCheckException {
        AstNode forExpr = node.children().get(0);
        DataType forExprType = exprCheck(forExpr);
        if(forExprType != DataType.BOOL) {
            throw new TypeCheckException("for loop needs a boolean expression.");
        }
    }

    private void varInitCheck(AstNode node) throws TypeCheckException {
        AstNode varId = node.children().get(0);
        AstNode varType = node.children().get(1);
        AstNode varExpr = node.children().get(2);
        DataType varShouldBe = varType.dataType();
        if(varShouldBe != exprCheck(varExpr)) {
            throw new TypeCheckException("wrong type by 'var "+ varId.getText() +" "+ varType.getText() +"' unable to assign "+ exprCheck(varExpr) +" value");
        }
    }

    private void varAssignCheck(AstNode node) throws TypeCheckException {
        DataType varIdType = creator.symbolTableVar.get(node.children().get(0).getText());
        DataType exprType = exprCheck(node.children().get(1));
        if(varIdType != exprType) {
            throw new TypeCheckException("unable to assign "+ exprType +" value to "+ varIdType +" variable.");
        }
    }

    private void funcInvocCheck(AstNode node) {

    }

    private void funcReturnCheck(AstNode node) throws TypeCheckException {
        DataType retExprType = exprCheck(node.children().get(0));
        if(retExprType != creator.symbolTableFuncReturn.get(funcContext)) {
            throw new TypeCheckException("wrong return type for return in function "+funcContext);
        }
    }

    private DataType exprCheck(AstNode node) throws TypeCheckException {
        //>>>>>>func_invoc -> get return type of func
        if(node.getText().equals("func_invoc")) {
            String funcName = node.children().get(0).getText();
            return creator.symbolTableFuncReturn.get(funcName);
        }
        //>>>>>>reached terminal node
        if(node.children().isEmpty()) {
            //id check
            if(node.nodeType() == AstNodeType.ID) {
                return creator.symbolTableVar.get(node.getText());
            } else {
                return node.dataType();
            }
        }
        //>>>>>>reached non-terminal node with one child (just one literal or id)
        if(node.children().size() == 1) {
            return exprCheck(node.children().get(0));
        }
        //>>>>>>unary operators
        if(node.children().size() == 2) {
            DataType op2Type = exprCheck(node.children().get(1));
            if((node.children().get(0).nodeType() == AstNodeType.OP) &&
                    (op2Type == DataType.INT || op2Type == DataType.FLOAT)) {
                return op2Type;
            } else if(node.children().get(0).nodeType() == AstNodeType.LGC_SMBL && op2Type == DataType.BOOL) {
                return op2Type;
            } else {
                throw new TypeCheckException("wrong use of unary operator '"+node.children().get(0).getText()+"'");
            }
        }
        //>>>>>>still non-terminal node
        AstNode op1 = node.children().get(0);
        AstNode exprOp = node.children().get(1);
        AstNode op2 = node.children().get(2);
        DataType op1Type = exprCheck(op1), op2Type = exprCheck(op2);

        //both operands need to be numbers (arithmetic operation)
        if(exprOp.nodeType() == AstNodeType.OP) {
            if(DataType.NUMBERS.contains(op1Type) && DataType.NUMBERS.contains(op2Type)) {
                if(op1Type == DataType.FLOAT || op2Type == DataType.FLOAT) {
                    return DataType.FLOAT;
                }
                else {
                    return DataType.INT;
                }
            }
            else {
                throw new TypeCheckException("arithmetic operation with values other than float64 or int not possible.");
            }
        }
        //both operands need to be numbers (arithmetic comparison)
        if(AstNodeType.ARIT_CMP_SMBLS.contains(exprOp.getText())) {
            if(DataType.NUMBERS.contains(op1Type) && DataType.NUMBERS.contains(op2Type)) {
                return DataType.BOOL;
            } else {
                throw new TypeCheckException("arithmetic comparison with values other than float64 or int not possible.");
            }
        }
        //both operands need to be boolean
        if(exprOp.nodeType() == AstNodeType.LGC_SMBL){
            if(op1Type == DataType.BOOL && op2Type == DataType.BOOL) {
                return DataType.BOOL;
            } else {
                throw new TypeCheckException("boolean expression with non-boolean operators not possible.");
            }
        }
        //equation, both operands need to be the same, returns boolean
        if(AstNodeType.EQ_SMBLS.contains(exprOp.getText())) {
            if(op1Type == op2Type ||
                    (DataType.NUMBERS.contains(op1Type) && DataType.NUMBERS.contains(op2Type))) {
                return DataType.BOOL;
            } else {
                throw new TypeCheckException("equation with two different types not possible.");
            }
        }

        return DataType.UNDEF;
    }
}
