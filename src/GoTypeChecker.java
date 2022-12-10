import java.util.Map;

public class GoTypeChecker {

    Map<String, DataType> symbolTableDataType;

    GoTypeChecker(Map<String, DataType> symbolTableDataType) {
        this.symbolTableDataType = symbolTableDataType;
    }

    public void visit(AstNode node) throws TypeCheckException {
        for(AstNode child : node.children()) {
            if(child.getText().equals("var_init")) {
                varInitCheck(child);
            } else if(child.getText().equals("if_statement")) {
                ifStatementCheck(child);
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

    private void varInitCheck(AstNode node) throws TypeCheckException {
        AstNode varId = node.children().get(0);
        AstNode varType = node.children().get(1);
        AstNode varExpr = node.children().get(2);
        DataType varShouldBe = varType.dataType();
        if(varShouldBe != exprCheck(varExpr)) {
            throw new TypeCheckException("wrong type by 'var "+ varId.getText() +" "+ varType.getText() +"' can't assign "+ exprCheck(varExpr) +" value");
        }
    }

    private DataType exprCheck(AstNode node) throws TypeCheckException {
        //reached terminal node
        if(node.children().isEmpty()) {
            //id check
            if(node.nodeType() == AstNodeType.ID) {
                return symbolTableDataType.get(node.getText());
            } else {
                return node.dataType();
            }
        }
        //reached non-terminal node with one child (just one literal or id)
        if(node.children().size() == 1) {
            return exprCheck(node.children().get(0));
        }
        //reached non-terminal node with two children (one literal or id with prefix)
        //prefix check for '!' not needed because of grammar
        if(node.children().size() == 2) {
            DataType op2Type = exprCheck(node.children().get(1));
            if((node.children().get(0).nodeType() == AstNodeType.OP) &&
                    (op2Type == DataType.INT || op2Type == DataType.FLOAT)) {
                return op2Type;
            } else {
                throw new TypeCheckException("Can't prefix boolean with "+node.children().get(0).getText());
            }
        }
        //still non-terminal node
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
