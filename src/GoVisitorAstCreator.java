import java.util.ArrayList;
import java.util.List;

public class GoVisitorAstCreator extends GoParserBaseListener{
    public AstNode ast = new AstNode(new ArrayList<>(), "program", AstNodeType.NON_TERMINAL);

    @Override
    public void exitFunc(GoParser.FuncContext ctx) {
        ast.addChild(
                new AstNode(List.of(
                        new AstNode(new ArrayList<>(), ctx.ID().getText(), AstNodeType.ID),
                        funcParam(ctx.func_param()),
                        funcRetType(ctx.func_ret_type()),
                        instructionBlock(ctx.instruction_block())),
                        "func",
                        AstNodeType.NON_TERMINAL)
        );
    }


    //>>>>>>>>>>functions

    //func_param -> nullable
    private AstNode funcParam(GoParser.Func_paramContext ctx){
        if(ctx.ID() == null) return AstNode.createNullNode();
        return new AstNode(List.of(
                new AstNode(new ArrayList<>(), ctx.ID().getText(), AstNodeType.ID),
                new AstNode(new ArrayList<>(), ctx.VAR_TYPE().getText(), AstNodeType.VAR_TYPE),
                funcParam2(ctx.func_param2())),
                "func_param",
                AstNodeType.NON_TERMINAL);
    }
    //func_param2 -> nullable
    private AstNode funcParam2(GoParser.Func_param2Context ctx){
        if(ctx.ID() == null) return AstNode.createNullNode();
        return new AstNode(List.of(
                new AstNode(new ArrayList<>(), ctx.ID().getText(), AstNodeType.ID),
                new AstNode(new ArrayList<>(), ctx.VAR_TYPE().getText(), AstNodeType.VAR_TYPE),
                funcParam2(ctx.func_param2())),
                "func_param2",
                AstNodeType.NON_TERMINAL);
    }
    //func_invoc
    private AstNode funcInvoc(GoParser.Func_invocContext ctx) {
        return AstNode.createNullNode();
    }
    //func_return
    private AstNode funcReturn(GoParser.Func_returnContext ctx) {
        return AstNode.createNullNode();
    }
    //func_ret_type
    private AstNode funcRetType(GoParser.Func_ret_typeContext ctx){
        return AstNode.createNullNode();
    }


    //>>>>>>>>>>instructions

    //instruction_block -> nullable
    private AstNode instructionBlock(GoParser.Instruction_blockContext ctx) {
        if(ctx.instruction() == null) return AstNode.createNullNode();
        return instruction(ctx.instruction());
    }
    //instruction -> nullable
    private AstNode instruction(GoParser.InstructionContext ctx) {
        if(ctx.instruction_dec() == null) return AstNode.createNullNode();
        return new AstNode(List.of(
                instructionDec(ctx.instruction_dec()),
                instruction(ctx.instruction())),
                "instruction_block",
                AstNodeType.NON_TERMINAL);
    }
    //instruction_dec
    private AstNode instructionDec(GoParser.Instruction_decContext ctx) {
        AstNode instDecNode = null;
        if(ctx.if_statement() != null) {
            instDecNode = ifStatement(ctx.if_statement());
        } else if(ctx.for_loop() != null) {
            instDecNode = forLoop(ctx.for_loop());
        } else if(ctx.var_init() != null) {
            instDecNode = varInit(ctx.var_init());
        } else if(ctx.var_assign() != null) {
            instDecNode = varAssign(ctx.var_assign());
        } else if(ctx.func_invoc() != null) {
            instDecNode = funcInvoc(ctx.func_invoc());
        } else if(ctx.func_return() != null) {
            instDecNode = funcReturn(ctx.func_return());
        }
        return instDecNode;
    }


    //>>>>>>>>>>variables
    //var_init
    private AstNode varInit(GoParser.Var_initContext ctx) {
        return new AstNode(List.of(
                new AstNode(new ArrayList<>(), ctx.ID().getText(), AstNodeType.ID),
                new AstNode(new ArrayList<>(), ctx.VAR_TYPE().getText(), AstNodeType.VAR_TYPE),
                expr(ctx.expr())),
                "var_init",
                AstNodeType.NON_TERMINAL);
    }
    //var_assign
    private AstNode varAssign(GoParser.Var_assignContext ctx) {
        return new AstNode(List.of(
                new AstNode(new ArrayList<>(), ctx.ID().getText(), AstNodeType.ID),
                expr(ctx.expr())),
                "var_init",
                AstNodeType.NON_TERMINAL);
    }

    //>>>>>>>>>>if statement
    //if_statement
    private AstNode ifStatement(GoParser.If_statementContext ctx) {
        return new AstNode(List.of(
                bExpr(ctx.bexpr()),
                instructionBlock(ctx.instruction_block()),
                elseStatement(ctx.else_statement())),
                "if_statement",
                AstNodeType.NON_TERMINAL);
    }
    //else_statement -> nullable
    private AstNode elseStatement(GoParser.Else_statementContext ctx) {
        if(ctx.KEY_ELSE() == null) return AstNode.createNullNode();
        return new AstNode(List.of(
                instructionBlock(ctx.instruction_block())),
                "else_statement",
                AstNodeType.NON_TERMINAL);
    }


    //>>>>>>>>>>for loop
    //for_loop
    private AstNode forLoop(GoParser.For_loopContext ctx) {
        return new AstNode(List.of(
                bExpr(ctx.bexpr()),
                instructionBlock(ctx.instruction_block())),
                "for_loop",
                AstNodeType.NON_TERMINAL);
    }


    //>>>>>>>>>>expressions
    //expr
    private AstNode expr(GoParser.ExprContext ctx) {
        AstNode exprNode = null;
        if(ctx.bexpr() != null) {
            exprNode = bExpr(ctx.bexpr());
        } else if(ctx.aexpr() != null) {
            exprNode = aExpr(ctx.aexpr());
        }
        return exprNode;
    }
    //boolean expression
    //bexpr
    private AstNode bExpr(GoParser.BexprContext ctx) {
        AstNode bExprNode;
        if(ctx.bexpr() != null) {
            bExprNode = new AstNode(List.of(
                    bExpr(ctx.bexpr()),
                    new AstNode(new ArrayList<>(),ctx.LGC_OR().getText(), AstNodeType.LGC_SMBL),
                    bTerm(ctx.bterm())),
                    "bexpr",
                    AstNodeType.NON_TERMINAL);
        } else {
            bExprNode = bTerm(ctx.bterm());
        }
        return bExprNode;
    }
    //bterm
    private AstNode bTerm(GoParser.BtermContext ctx) {
        return AstNode.createNullNode();
    }
    //bcomp
    private AstNode bComp(GoParser.BcompContext ctx) {
        return AstNode.createNullNode();
    }
    //bfactor
    private AstNode bFactor(GoParser.BfactorContext ctx) {
        return AstNode.createNullNode();
    }
    //arithmetic expression
    //aexpr
    private AstNode aExpr(GoParser.AexprContext ctx) {
        return AstNode.createNullNode();
    }
    //aterm
    private AstNode aTerm(GoParser.AtermContext ctx) {
        return AstNode.createNullNode();
    }
    //afactor
    private AstNode aFactor(GoParser.AfactorContext ctx) {
        return AstNode.createNullNode();
    }
    //expr_param
    private AstNode exprParam(GoParser.Expr_paramContext ctx) {
        return AstNode.createNullNode();
    }


}
