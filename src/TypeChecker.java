import java.util.ArrayList;
import java.util.List;

public class TypeChecker {
    //symbol table for variables with corresponding data type
    SymbolTableCreator creator;
    private String currentFunc = "";

    TypeChecker(SymbolTableCreator symbolTableCreator) {
        this.creator = symbolTableCreator;
    }

    //checks for typing errors and returns type setted AST
    public AstNode checkAndSet(AstNode astRoot) throws TypeCheckException, GoParseException {
        //check if first function is main
        if(!astRoot.children().get(0).children().get(0).getText().equals("main")) {
            throw new GoParseException("Function 'main' in package main not found.");
        }
        visit(astRoot);
        return astRoot;
    }

    private void visit(AstNode node) throws TypeCheckException, GoParseException {
        for(AstNode child : node.children()) {
            switch (child.getText()) {
                case "func" -> currentFunc = child.children().get(0).getText();
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

    private void ifStatementCheck(AstNode ifNode) throws TypeCheckException, GoParseException {
        AstNode ifExpr = ifNode.children().get(0);
        DataType ifExprType = exprCheck(ifExpr);
        if(ifExprType != DataType.BOOL) {
            throw new TypeCheckException("If statement needs a boolean expression.");
        }
        //TypeSetting
        ifExpr.setDataType(DataType.BOOL);
    }

    private void forLoopCheck(AstNode node) throws TypeCheckException, GoParseException {
        AstNode forExpr = node.children().get(0);
        DataType forExprType = exprCheck(forExpr);
        if(forExprType != DataType.BOOL) {
            throw new TypeCheckException("For loop needs a boolean expression.");
        }
        //TypeSetting
        forExpr.setDataType(DataType.BOOL);
    }

    private void varInitCheck(AstNode varInitNode) throws TypeCheckException, GoParseException {
        //TODO: check int x = float64 cast
        AstNode varIdNode = varInitNode.children().get(0);
        AstNode varTypeNode = varInitNode.children().get(1);
        AstNode varExprNode = varInitNode.children().get(2);
        DataType varType = varTypeNode.dataType();
        DataType exprType = exprCheck(varExprNode);
        //TypeSetting
        varExprNode.setDataType(varType);
        //implicit typecast int->float
        //check typecast
        if(varType==DataType.FLOAT && numCastPossible(varType, exprType)) {
            return;
        }
        if(varType != exprType) {
            throw new TypeCheckException("Wrong type at 'var "+ varIdNode.getText() +" "+ varTypeNode.getText() +"' unable to assign "+ exprType +" value");
        }
    }

    private DataType varAssignCheck(AstNode varAssignNode) throws TypeCheckException, GoParseException {
        DataType varType = creator.funcScopeTable.get(currentFunc).get(varAssignNode.children().get(0).getText());
        if(varType == null) {
            throw new GoParseException("Unknown variable "+varAssignNode.children().get(0).getText());
        }
        AstNode varAssignExprNode = varAssignNode.children().get(1);
        DataType exprType = exprCheck(varAssignExprNode);
        //TypeSetting
        varAssignExprNode.setDataType(varType);
        //implicit typecast int->float
        //check typecast
        if(varType==DataType.FLOAT && numCastPossible(varType, exprType)) {
            return DataType.FLOAT;
        }
        if(varType != exprType) {
            throw new TypeCheckException("Unable to assign "+ exprType +" value to "+ varType +" variable.");
        }
        return varType;
    }

    private void funcInvocCheck(AstNode funcInvocNode) throws GoParseException, TypeCheckException {
        //TypeSetting
        AstNode setNode = funcInvocNode.children().get(1);
        while(setNode != null) {
            setNode.setDataType(exprCheck(setNode.children().get(0)));
            if(setNode.children().size() < 2) {
                setNode = null;
            } else {
                setNode = setNode.children().get(1);
            }
        }

        String funcId = funcInvocNode.children().get(0).getText();
        List<DataType> funcInvocParamList = new ArrayList<>();
        List<DataType> actualParamList = creator.symbolTableFuncParam.get(funcId);
        if(actualParamList == null) actualParamList = new ArrayList<>();

        //collect all parameters
        if(funcInvocNode.children().size() == 2) {
            funcInvocParamList.addAll(getFuncInvocParam(funcInvocNode.children().get(1)));
        }
        //check # of parameters
        //Println()
        if(funcId.equals("Println")) {
            if(funcInvocParamList.size() != 1) {
                throw new GoParseException("Wrong number of parameters at function call " + funcId + "()");
            } else {
                return;
            }
        }
        if(actualParamList.size() != funcInvocParamList.size()) {
            throw new GoParseException("Wrong number of parameters at function call "+funcId+"()");
        }
        //compare types of parameters
        for(int i = 0; i < actualParamList.size(); i++) {
            //both numbers
            if(numCastPossible(actualParamList.get(i), funcInvocParamList.get(i))) {
                return;
            }
            else if(actualParamList.get(i) != funcInvocParamList.get(i)) {
                throw new TypeCheckException("Wrong parameter type at function call "+funcId+"()");
            }
        }
    }

    private List<DataType> getFuncInvocParam(AstNode funcInvocParamNode) throws GoParseException, TypeCheckException {
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

    private void funcReturnCheck(AstNode funcReturnNode) throws TypeCheckException, GoParseException {
        DataType retExprType = DataType.UNDEF;
        DataType funcRetType = DataType.UNDEF;
        //check if return type is empty
        if(creator.symbolTableFuncReturn.get(currentFunc) != null) {
            funcRetType = creator.symbolTableFuncReturn.get(currentFunc);
        }
        //check if return is empty
        if(!funcReturnNode.children().isEmpty()) {
            retExprType = exprCheck(funcReturnNode.children().get(0));
        }
        //check int float cast
        if(numCastPossible(retExprType, funcRetType)) {
            return;
        }
        if(retExprType != funcRetType) {
            throw new TypeCheckException("Wrong return type for return in function "+ currentFunc);
        }
    }

    private DataType exprCheck(AstNode exprNode) throws TypeCheckException, GoParseException {
        //>>>>>>reached expr_param node
        //func_invoc_dot -> get return type of func_invoc child
        if(exprNode.getText().equals("func_invoc_dot")) {
            return exprCheck(exprNode.children().get(1));
        }
        //func_invoc -> get return type of func
        if(exprNode.getText().equals("func_invoc")) {
            String funcName = exprNode.children().get(0).getText();
            if(creator.symbolTableFuncReturn.get(funcName) == null) {
                return DataType.UNDEF;
            }
            DataType funcReturnDataType = creator.symbolTableFuncReturn.get(funcName);
            //TypeSetting
            exprNode.setDataType(funcReturnDataType);
            return funcReturnDataType;
        }
        //id or literal
        if(exprNode.children().isEmpty()) {
            //id check
            if(exprNode.nodeType() == AstNodeType.ID) {
                if(creator.funcScopeTable.get(currentFunc).get(exprNode.getText()) == null) {
                    throw new GoParseException("Unknown variable "+exprNode.getText());
                } else {
                    DataType idDataType = creator.funcScopeTable.get(currentFunc).get(exprNode.getText());
                    //TypeSetting
                    exprNode.setDataType(idDataType);
                    return idDataType;
                }
            } else {
                //TypeSetting
                exprNode.setDataType(exprNode.dataType());
                return exprNode.dataType();
            }
        }

        //>>>>>>reached non-terminal node with one child (just one literal or id)
        if(exprNode.children().size() == 1) {
            DataType retDataType = exprCheck(exprNode.children().get(0));
            //TypeSetting
            exprNode.setDataType(retDataType);
            return retDataType;
        }

        //>>>>>>unary operators
        if(exprNode.children().size() == 2) {
            DataType op2Type = exprCheck(exprNode.children().get(1));
            if((exprNode.children().get(0).nodeType() == AstNodeType.OP) &&
                    (DataType.NUMBERS.contains(op2Type))) {
                //TypeSetting
                exprNode.setDataType(op2Type);
                return op2Type;
            } else if(exprNode.children().get(0).nodeType() == AstNodeType.LGC_SMBL && op2Type == DataType.BOOL) {
                //TypeSetting
                exprNode.setDataType(op2Type);
                return op2Type;
            } else {
                throw new TypeCheckException("Wrong use of unary operator '"+exprNode.children().get(0).getText()+"'");
            }
        }

        //>>>>>>still non-terminal node
        AstNode op1 = exprNode.children().get(0);
        AstNode exprOp = exprNode.children().get(1);
        AstNode op2 = exprNode.children().get(2);
        DataType op1Type = exprCheck(op1), op2Type = exprCheck(op2);
        //arithmetic operations
        if(exprOp.nodeType() == AstNodeType.OP) {
            //STR concat
            if(exprOp.getText().equals("+") && (op1Type == DataType.STR && op2Type == DataType.STR)) {
                //TypeSetting
                exprNode.setDataType(DataType.STR);
                return DataType.STR;
            }
            //numbers (typecast to float)
            if(numCastPossible(op1Type, op2Type)) {
                if(op1Type == DataType.FLOAT || op2Type == DataType.FLOAT) {
                    //TypeSetting
                    exprNode.setDataType(DataType.FLOAT);
                    return DataType.FLOAT;
                }
                else {
                    //TypeSetting
                    exprNode.setDataType(DataType.INT);
                    return DataType.INT;
                }
            }
            else {
                throw new TypeCheckException("Arithmetic operation '"+exprOp.getText()+"' not possbile with "+op1Type+" and "+op2Type);
            }
        }
        //arithmetic comparison: both operands need to be numbers
        if(AstNodeType.ARIT_CMP_SMBLS.contains(exprOp.getText())) {
            if(DataType.NUMBERS.contains(op1Type) && DataType.NUMBERS.contains(op2Type)) {
                //TypeSetting
                exprNode.setDataType(DataType.BOOL);
                return DataType.BOOL;
            } else {
                throw new TypeCheckException("Arithmetic comparison with values other than float64 or int not possible.");
            }
        }
        //logic comparison: both operands need to be boolean
        if(exprOp.nodeType() == AstNodeType.LGC_SMBL){
            if(op1Type == DataType.BOOL && op2Type == DataType.BOOL) {
                //TypeSetting
                exprNode.setDataType(DataType.BOOL);
                return DataType.BOOL;
            } else {
                throw new TypeCheckException("Boolean expression with non-boolean operators not possible.");
            }
        }
        //equation: both operands need to be of the same type, returns boolean
        if(AstNodeType.EQ_SMBLS.contains(exprOp.getText())) {
            if(op1Type == op2Type || (numCastPossible(op1Type, op2Type))) {
                //TypeSetting
                exprNode.setDataType(DataType.BOOL);
                return DataType.BOOL;
            } else {
                throw new TypeCheckException("Equation with two different types not possible.");
            }
        }

        //>>>>>>default return
        return DataType.UNDEF;
    }

    private boolean numCastPossible(DataType type1, DataType type2) {
        return DataType.NUMBERS.contains(type1) && DataType.NUMBERS.contains(type2);
    }
}
