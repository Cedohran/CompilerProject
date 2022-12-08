import java.util.ArrayList;
import java.util.List;

public class AstCreator {
    //prog tree
    AstNode programTree = new AstNode("program", AstNodeType.NON_TERMINAL);
    //func trees
    AstNode funcInvocTree, funcReturnTree;
    //if / for trees
    AstNode forTree, ifTree, elseTree;
    //instruction trees
    int recCounter = -1;
    ArrayList<AstNode> instDecTrees = new ArrayList<>();
    AstNode instructionBlockTree,
            varInitTree, varAssignTree;
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
        recCounter++;
        instDecTrees.add(new AstNode("instruction_dec", AstNodeType.NON_TERMINAL));
    }
    void instructionBlockExit() {
        instructionBlockTree = new AstNode(instDecTrees.get(recCounter).children, "instruction_block");
        instDecTrees.remove(recCounter);
        recCounter--;
    }

    //instruction_dec -> gather all instructions in one instruction_block
    //indirect
    void ifStatement() {
        if(elseTree != null) {
            ifTree.addChild(elseTree);
        }
        instDecTrees.get(recCounter).addChild(ifTree);
    }
    //direct
    void forLoop() {
        forTree = new AstNode(List.of(exprTree, instructionBlockTree), "for_loop");
        instDecTrees.get(recCounter).addChild(forTree);
    }
    //direct
    void varInit(GoParser.Instruction_decContext ctx) {
        GoParser.Var_initContext localCtx = ctx.var_init();
        varInitTree = new AstNode(List.of(new AstNode(localCtx.ID().getText(), AstNodeType.ID),
                new AstNode(localCtx.VAR_TYPE().getText(), AstNodeType.VAR_TYPE),
                exprTree),
                "var_init");
        instDecTrees.get(recCounter).addChild(varInitTree);
    }
    //direct
    void varAssign(GoParser.Instruction_decContext ctx) {
        GoParser.Var_assignContext localCtx = ctx.var_assign();
        varAssignTree = new AstNode(List.of(new AstNode(localCtx.ID().getText(), AstNodeType.ID),
                exprTree),
                "var_assign");
        instDecTrees.get(recCounter).addChild(varAssignTree);
    }
    //indirect
    void funcInvoc() {
        instDecTrees.get(recCounter).addChild(funcInvocTree);
    }
    //direct
    void funcReturn() {
        funcReturnTree = new AstNode(List.of(exprTree), "func_return");
        instDecTrees.get(recCounter).addChild(funcReturnTree);
    }
    //direct
    void expr() {
        instDecTrees.get(recCounter).addChild(exprTree);
    }


    //if_statement
    void ifStatementBlock() {
        if(instructionBlockTree != null) {
            ifTree.addChild(instructionBlockTree);
        }
    }
    void ifStatementBExpr() {
        ifTree = new AstNode(List.of(bexprTree), "if_statement");
    }
    //connects to ifStatment
    void elseStatmentBlock() {
        //elseTree = new AstNode(instructionBlockTree.children, "else_statement");
        elseTree = new AstNode(List.of(instructionBlockTree), "else_statement");
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

    //aexpr
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
    void funcInvocExprParam(GoParser.Expr_paramContext ctx) {
        paramTree = new AstNode(List.of(funcInvocTree), "func_invoc");
    }

}
