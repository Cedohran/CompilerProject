import java.util.ArrayList;
import java.util.List;

public class AstCreator {
    //prog tree
    AstNode programTree = new AstNode("program", AstNodeType.NON_TERMINAL);

    //func trees
    AstNode funcInvocTree, funcReturnTree;

    //if / for trees
    //recursive counter and arrays
    int ifElseCounter = -1, forLoopCounter = -1;
    ArrayList<AstNode> ifStatementTree = new ArrayList<>(), elseStatementTree = new ArrayList<>(),
            forLoopTree = new ArrayList<>();

    //instruction trees
    AstNode instructionBlockTree,
            varInitTree, varAssignTree;
    //recursive counter and arrays
    int instBlockCounter = -1;
    ArrayList<AstNode> instDecTrees = new ArrayList<>();

    //expression trees
    AstNode exprTree,
            paramTree, afactorTree, atermTree, aexprTree,
            bfactorTree, bcompTree, btermTree, bexprTree,
            cmpSmblAExprLeftTree, cmpSmblAExprRightTree;


    //func
    void func(GoParser.FuncContext ctx) {
        AstNode newFunc = new AstNode(List.of(new AstNode(ctx.ID().getText(), AstNodeType.ID),
                instructionBlockTree),
                "func");
        programTree.addChild(newFunc);
    }


    //instruction_block -> copy whole block to instructionBlockTree and reset instDecTree for other block
    void instructionBlockEnter() {
        instBlockCounter++;
        instDecTrees.add(new AstNode("instruction_dec", AstNodeType.NON_TERMINAL));
    }
    void instructionBlockExit() {
        instructionBlockTree = new AstNode(instDecTrees.get(instBlockCounter).children, "instruction_block");
        instDecTrees.remove(instBlockCounter);
        instBlockCounter--;
    }

    //instruction_dec -> gather all instructions in one instruction_block
    //indirect
    void ifStatementEnter() {
        ifElseCounter++;
        ifStatementTree.add(new AstNode("if_statement", AstNodeType.NON_TERMINAL));
        elseStatementTree.add(null);
    }
    void ifStatementExit() {
        if(elseStatementTree.get(ifElseCounter) != null) {
            ifStatementTree.get(ifElseCounter).addChild(elseStatementTree.get(ifElseCounter));
        }
        instDecTrees.get(instBlockCounter).addChild(ifStatementTree.get(ifElseCounter));
        ifStatementTree.remove(ifElseCounter);
        elseStatementTree.remove(ifElseCounter);
        ifElseCounter--;
    }
    //indirect
    void forLoopEnter() {
        forLoopCounter++;
        forLoopTree.add(new AstNode("for_loop", AstNodeType.NON_TERMINAL));
    }
    void forLoopExit() {
        instDecTrees.get(instBlockCounter).addChild(forLoopTree.get(forLoopCounter));
        forLoopTree.remove(forLoopCounter);
        forLoopCounter--;
    }
    //direct
    void varInit(GoParser.Instruction_decContext ctx) {
        GoParser.Var_initContext localCtx = ctx.var_init();
        varInitTree = new AstNode(List.of(new AstNode(localCtx.ID().getText(), AstNodeType.ID),
                new AstNode(localCtx.VAR_TYPE().getText(), AstNodeType.VAR_TYPE),
                exprTree),
                "var_init");
        instDecTrees.get(instBlockCounter).addChild(varInitTree);
    }
    //direct
    void varAssign(GoParser.Instruction_decContext ctx) {
        GoParser.Var_assignContext localCtx = ctx.var_assign();
        varAssignTree = new AstNode(List.of(new AstNode(localCtx.ID().getText(), AstNodeType.ID),
                exprTree),
                "var_assign");
        instDecTrees.get(instBlockCounter).addChild(varAssignTree);
    }
    //indirect
    void funcInvoc() {
        instDecTrees.get(instBlockCounter).addChild(funcInvocTree);
    }
    //direct
    void funcReturn() {
        funcReturnTree = new AstNode(List.of(exprTree), "func_return");
        instDecTrees.get(instBlockCounter).addChild(funcReturnTree);
    }
    //direct
    void expr() {
        instDecTrees.get(instBlockCounter).addChild(exprTree);
    }


    //if_statement
    void ifStatementBlock() {
        if(instructionBlockTree != null) {
            ifStatementTree.get(ifElseCounter).addChild(instructionBlockTree);
        }
    }
    void ifStatementBExpr() {
        ifStatementTree.get(ifElseCounter).addChild(new AstNode(List.of(bexprTree), "if_bexpr"));
    }
    //connects to ifStatment
    void elseStatmentBlock() {
        //elseTree = new AstNode(instructionBlockTree.children, "else_statement");
        //remove null entry and add non-null entry
        elseStatementTree.remove(ifElseCounter);
        elseStatementTree.add(new AstNode(List.of(instructionBlockTree), "else_statement"));
    }

    //for_loop
    void forLoopBlock() {
        if(instructionBlockTree != null) {
            forLoopTree.get(forLoopCounter).addChild(instructionBlockTree);
        }
    }
    void forLoopBExpr() {
        forLoopTree.get(forLoopCounter).addChild(new AstNode(List.of(bexprTree), "for_bexpr"));
    }

    //func_invoc
    void funcInvocExpr(GoParser.Func_invocContext ctx) {
        funcInvocTree = new AstNode(List.of(new AstNode(ctx.ID().getText(), AstNodeType.ID),
                exprTree),
                "func_invoc");
    }
    void funcInvocVarAssign(GoParser.Func_invocContext ctx) {
        funcInvocTree = new AstNode(List.of(new AstNode(ctx.ID().getText(), AstNodeType.ID),
                varAssignTree),
                "func_invoc");
    }
    void funcInvocDot(GoParser.Func_invocContext ctx) {
        AstNode funcInvocTreeCopy = new AstNode(List.of(funcInvocTree), "func_invoc");
        funcInvocTree = new AstNode(List.of(new AstNode(ctx.ID().getText(), AstNodeType.ID),
                funcInvocTreeCopy),
                "func_invoc_dot");
    }

    //expr
    void bExpr() {
        exprTree = bexprTree;
    }
    void aExpr() {
        exprTree = aexprTree;
    }

    //aexpr TODO: operator node to root
    void opAdd() {
        aexprTree = new AstNode(List.of(aexprTree, new AstNode("+", AstNodeType.OP), atermTree), "aexpr");
    }
    void opSub() {
        aexprTree = new AstNode(List.of(aexprTree, new AstNode("-", AstNodeType.OP), atermTree), "aexpr");
    }
    void aTerm() {
        aexprTree = atermTree;
    }

    //aterm
    void opMult() {
        atermTree = new AstNode(List.of(atermTree, new AstNode("*", AstNodeType.OP), afactorTree), "aterm");
    }
    void opDiv() {
        atermTree = new AstNode(List.of(atermTree, new AstNode("/", AstNodeType.OP), afactorTree), "aterm");
    }
    void opMod() {
        atermTree = new AstNode(List.of(atermTree, new AstNode("%", AstNodeType.OP), afactorTree), "aterm");
    }
    void aFactor() {
        atermTree = afactorTree;
    }

    //afactor
    void opAddFactor() {
        AstNode afactorTreeCopy = new AstNode(List.of(afactorTree), "afactor");
        afactorTree = new AstNode(List.of(new AstNode("+", AstNodeType.OP), afactorTreeCopy),
                "afactor");
    }
    void opSubFactor() {
        AstNode afactorTreeCopy = new AstNode(List.of(afactorTree), "afactor");
        afactorTree = new AstNode(List.of(new AstNode("-", AstNodeType.OP), afactorTreeCopy),
                "afactor");
    }
    void exprParam() {
        afactorTree = paramTree;
    }
    void parantAExpr() {
        afactorTree = aexprTree;
    }

    //bexpr
    void bExprLgcOr(GoParser.BexprContext ctx) {
        bexprTree = new AstNode(List.of(bexprTree,
                new AstNode(ctx.LGC_OR().getText(), AstNodeType.LGC_SMBL),
                btermTree),
                "bexpr");
    }
    void bTerm() {
        bexprTree = btermTree;
    }

    //bterm
    void bTermLgcAnd(GoParser.BtermContext ctx) {
        btermTree = new AstNode(List.of(btermTree,
                new AstNode(ctx.LGC_AND().getText(), AstNodeType.LGC_SMBL),
                bfactorTree),
                "bterm");
    }
    void bComp() {
        btermTree = bcompTree;
    }

    //bcomp
    void bCompCmpSmbl(GoParser.BcompContext ctx) {
        bcompTree = new AstNode(List.of(bcompTree,
                new AstNode(ctx.CMP_SMBL().getText(), AstNodeType.CMP_SMBL),
                bfactorTree),
                "bcomp");
    }
    void bFactor() {
        bcompTree = bfactorTree;
    }

    //bfactor
    void lgcNotFactor() {
        bfactorTree = new AstNode(List.of(new AstNode("!", AstNodeType.LGC_SMBL), bfactorTree),
                "bfactor");
    }
    void parantBExpr() {
        bfactorTree = bexprTree;
    }
    void bfactorBool(GoParser.BfactorContext ctx){
        bfactorTree = new AstNode(ctx.LIT_BOOL().getText(), AstNodeType.LIT);
    }
    void bfactorID(GoParser.BfactorContext ctx){
        bfactorTree = new AstNode(List.of(new AstNode(ctx.ID().getText(), AstNodeType.ID)),
                "bfactor");
    }
    void cmpSmblLeftAExpr() {
        if(aexprTree.children.size() != 0) {
            cmpSmblAExprLeftTree = new AstNode(aexprTree.children, "aexprLeft");
        } else {
            cmpSmblAExprLeftTree = new AstNode(List.of(aexprTree), "aexprLeft");
        }
    }
    void cmpSmblRightAExpr() {
        if(aexprTree.children.size() != 0) {
            cmpSmblAExprRightTree = new AstNode(aexprTree.children, "aexprRight");
        } else {
            cmpSmblAExprRightTree = new AstNode(List.of(aexprTree), "aexprRight");
        }
    }
    void bFactorCmpSmbl(GoParser.BfactorContext ctx) {
        bfactorTree = new AstNode(List.of(cmpSmblAExprLeftTree,
                new AstNode(ctx.CMP_SMBL().getText(), AstNodeType.CMP_SMBL),
                cmpSmblAExprRightTree),
                "bfactor");
    }

    //expr_param
    void litInt(GoParser.Expr_paramContext ctx) {
        paramTree = new AstNode(ctx.LIT_INT().getText(), AstNodeType.LIT);
    }
    void litFloat(GoParser.Expr_paramContext ctx) {
        paramTree = new AstNode(ctx.LIT_FLOAT().getText(), AstNodeType.LIT);
    }
    void litStr(GoParser.Expr_paramContext ctx) {
        paramTree = new AstNode(ctx.LIT_STR().getText(), AstNodeType.LIT);
    }
    void litBool(GoParser.Expr_paramContext ctx) {
        paramTree = new AstNode(ctx.LIT_BOOL().getText(), AstNodeType.LIT);
    }
    void exprParamID(GoParser.Expr_paramContext ctx) {
        paramTree = new AstNode(ctx.ID().getText(), AstNodeType.ID);
    }
    void funcInvocExprParam() {
        paramTree = new AstNode(funcInvocTree.children, "func_invoc");
    }

}
