import java.util.ArrayList;
import java.util.List;

public class TypeChecker {
    //symbol table for variables with corresponding data type
    SymbolTableCreator creator;
    private String funcContext = "";

    TypeChecker(SymbolTableCreator symbolTableCreator) {
        this.creator = symbolTableCreator;
    }

    public void check(AstNode astRoot) throws TypeCheckException, ParserException {
        //check if first function is main
        if(!astRoot.children().get(0).children().get(0).getText().equals("main")) {
            throw new TypeCheckException("first function must be 'main'");
        }
        visit(astRoot);
    }

    private void visit(AstNode node) throws TypeCheckException, ParserException {
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

    private void ifStatementCheck(AstNode node) throws TypeCheckException, ParserException {
        AstNode ifExpr = node.children().get(0);
        DataType ifExprType = exprCheck(ifExpr);
        if(ifExprType != DataType.BOOL) {
            throw new TypeCheckException("if statement needs a boolean expression.");
        }
    }

    private void forLoopCheck(AstNode node) throws TypeCheckException, ParserException {
        AstNode forExpr = node.children().get(0);
        DataType forExprType = exprCheck(forExpr);
        if(forExprType != DataType.BOOL) {
            throw new TypeCheckException("for loop needs a boolean expression.");
        }
    }

    private void varInitCheck(AstNode node) throws TypeCheckException, ParserException {
        AstNode varId = node.children().get(0);
        AstNode varType = node.children().get(1);
        AstNode varExpr = node.children().get(2);
        DataType varShouldBe = varType.dataType();
        DataType varExprType = exprCheck(varExpr);
        //implicit typecast int->float or float->int
        if(DataType.NUMBERS.contains(varShouldBe) && DataType.NUMBERS.contains(varExprType)) {
            return;
        }
        if(varShouldBe != varExprType) {
            throw new TypeCheckException("wrong type by 'var "+ varId.getText() +" "+ varType.getText() +"' unable to assign "+ varExprType +" value");
        }
    }

    private DataType varAssignCheck(AstNode node) throws TypeCheckException, ParserException {
        DataType varType = creator.symbolTableVar.get(node.children().get(0).getText());
        if(varType == null) {
            throw new ParserException("unknown variable "+node.children().get(0).getText());
        }
        DataType exprType = exprCheck(node.children().get(1));
        if(varType != exprType) {
            throw new TypeCheckException("unable to assign "+ exprType +" value to "+ varType +" variable.");
        }
        return varType;
    }

    private void funcInvocCheck(AstNode node) throws ParserException, TypeCheckException {
        String funcId = node.children().get(0).getText();
        //Println() joker
        if(funcId.equals("Println")) return;

        List<DataType> funcInvocParamList = new ArrayList<>();
        List<DataType> actualParamList = creator.symbolTableFuncParam.get(funcId);
        if(actualParamList == null) actualParamList = new ArrayList<>();

        if(node.children().size() == 2) {
            funcInvocParamList.addAll(getFuncInvocParam(node.children().get(1)));
        }
        if(actualParamList.size() != funcInvocParamList.size()) {
            throw new ParserException("wrong number of parameters at function call "+funcId+"()");
        }
        for(int i = 0; i < actualParamList.size(); i++) {
            //both numbers
            if(DataType.NUMBERS.contains(actualParamList.get(i)) && DataType.NUMBERS.contains(funcInvocParamList.get(i))) {
            }
            else if(actualParamList.get(i) != funcInvocParamList.get(i)) {
                throw new TypeCheckException("wrong parameter type at function call "+funcId+"()");
            }
        }

    }

    private List<DataType> getFuncInvocParam(AstNode funcInvocParamNode) throws ParserException, TypeCheckException {
        List<DataType> funcInvocParamList = new ArrayList<>();
        AstNode expr = funcInvocParamNode.children().get(0);
        if(expr.getText().equals("var_assign")) {
            funcInvocParamList.add(varAssignCheck(expr));
        } else {
            funcInvocParamList.add(exprCheck(expr));
        }
        if(funcInvocParamNode.children().size() == 2) {
            funcInvocParamList.addAll(getFuncInvocParam(funcInvocParamNode.children().get(1)));
        }
        return funcInvocParamList;
    }

    private void funcReturnCheck(AstNode node) throws TypeCheckException, ParserException {
        DataType retExprType = exprCheck(node.children().get(0));
        if(retExprType != creator.symbolTableFuncReturn.get(funcContext)) {
            throw new TypeCheckException("wrong return type for return in function "+funcContext);
        }
    }

    private DataType exprCheck(AstNode node) throws TypeCheckException, ParserException {
        //>>>>>>func_invoc_dot -> get return type of func_invoc child
        if(node.getText().equals("func_invoc_dot")) {
            return exprCheck(node.children().get(1));
        }
        //>>>>>>func_invoc -> get return type of func
        if(node.getText().equals("func_invoc")) {
            String funcName = node.children().get(0).getText();
            if(creator.symbolTableFuncReturn.get(funcName) == null) {
                return DataType.UNDEF;
            }
            return creator.symbolTableFuncReturn.get(funcName);
        }
        //>>>>>>reached terminal node
        if(node.children().isEmpty()) {
            //id check
            if(node.nodeType() == AstNodeType.ID) {
                if(creator.symbolTableVar.get(node.getText()) == null) {
                    throw new ParserException("unknown variable "+node.getText());
                } else {
                    return creator.symbolTableVar.get(node.getText());
                }
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
