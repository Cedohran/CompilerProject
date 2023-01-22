public class ifElseFlowChecker {
    private boolean ifReturn = false,
            stdReturn = false,
            inIf = false, inElse = false;

    private final AstNode ifNode;

    ifElseFlowChecker(AstNode ifNode) {
        this.ifNode = ifNode;
    }

    boolean checkForReturn() throws FlowControlException {
        visit(ifNode);
        return stdReturn;
    }

    private void visit(AstNode node) throws FlowControlException {
        //enter node
        switch (node.getText()) {
            case "if_statement" -> {
                if(inIf) {
                    ifReturn = new ifElseFlowChecker(node).checkForReturn();
                } //never executed?
                else if (inElse) {
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
        for(AstNode child : node.children()) {
            visit(child);
        }
        //exit node
        switch (node.getText()) {
            case "if_statement" -> inIf = false;
            case "else_statement" -> {
                inElse = false;
                ifReturn = false;
            }
        }
    }
}
