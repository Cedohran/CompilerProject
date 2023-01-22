public class FlowControlChecker {
    private final SymbolTableCreator symbolTable;
    private boolean ifReturn = false,
            stdReturn = false,
            inIf = false,
            inElse = false,
            deepSearchSkip = false;

    private final AstNode ast;

    FlowControlChecker(AstNode ast, SymbolTableCreator symbolTable) {
        this.ast = ast;
        this.symbolTable = symbolTable;
    }

    void check() throws FlowControlException {
        visit(ast);
    }

    private void visit(AstNode node) throws FlowControlException {
        //enter node
        switch (node.getText()) {
            case "func" -> {
                ifReturn = false;
                stdReturn = false;
                inIf = false;
                inElse = false;
            }
            case "if_statement" -> {
                if(inIf) {
                    deepSearchSkip = true;
                    ifReturn = new ifElseFlowChecker(node).checkForReturn();
                } //never executed?
                else if (inElse) {
                    deepSearchSkip = true;
                    //else has return
                    if(new ifElseFlowChecker(node).checkForReturn() && ifReturn){
                        stdReturn = true;
                    }
                }
                inIf = true;
            }
            case "else_statement" -> inElse = true;
            case "func_return" -> {
                if(inElse) {
                    //if + else contain return
                    if(ifReturn) {
                        stdReturn = true;
                    }
                } else if(inIf) {
                    ifReturn = true;
                } else {
                    stdReturn = true;
                }
            }
        }
        if(!deepSearchSkip) {
            for (AstNode child : node.children()) {
                visit(child);
            }
        }
        //exit node
        switch (node.getText()) {
            case "if_statement" -> inIf = false;
            case "else_statement" -> {
                inElse = false;
                ifReturn = false;
            }
            case "func" -> {
                //void no return needed
                if(symbolTable.symbolTableFuncReturn.get(node.children().get(0).getText()) != DataType.UNDEF) {
                    if (!stdReturn) {
                        throw new FlowControlException("missing return statement in function " + node.children().get(0).getText());
                    }
                }
            }
        }
        deepSearchSkip = false;
    }
}
