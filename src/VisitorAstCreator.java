import java.util.ArrayList;
import java.util.List;

public class VisitorAstCreator extends GoParserBaseListener{
    public AstNode AST = new AstNode(new ArrayList<>(), "program", AstNodeType.NON_TERMINAL);

    @Override
    public void enterFunc(GoParser.FuncContext ctx) {
        if(ctx.ID() == null) return;
        AST.addChild(
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
        AstNode funcInvocNode;
        if(ctx.expr() != null) {
            funcInvocNode = new AstNode(List.of(
                    new AstNode(new ArrayList<>(),ctx.ID().getText(), AstNodeType.ID),
                    expr(ctx.expr())),
                    "func_invoc",
                    AstNodeType.NON_TERMINAL);
        } else if(ctx.var_assign() != null) {
            funcInvocNode = new AstNode(List.of(
                    new AstNode(new ArrayList<>(),ctx.ID().getText(), AstNodeType.ID),
                    varAssign(ctx.var_assign())),
                    "func_invoc",
                    AstNodeType.NON_TERMINAL);
        } else if(ctx.SNTX_DOT() != null){
            funcInvocNode = new AstNode(List.of(
                    new AstNode(new ArrayList<>(),ctx.ID().getText(), AstNodeType.ID),
                    funcInvoc(ctx.func_invoc())),
                    "func_invoc_dot",
                    AstNodeType.NON_TERMINAL);
        } else {
            funcInvocNode = new AstNode(List.of(
                    new AstNode(new ArrayList<>(),ctx.ID().getText(), AstNodeType.ID)),
                    "func_invoc",
                    AstNodeType.NON_TERMINAL);
        }
        return funcInvocNode;
    }
    //func_return
    private AstNode funcReturn(GoParser.Func_returnContext ctx) {
        return new AstNode(List.of(
                expr(ctx.expr())),
                "func_return",
                AstNodeType.NON_TERMINAL);
    }
    //func_ret_type -> nullable
    private AstNode funcRetType(GoParser.Func_ret_typeContext ctx){
        if(ctx.VAR_TYPE() == null) return AstNode.createNullNode();
        return new AstNode(List.of(
                new AstNode(new ArrayList<>(), ctx.VAR_TYPE().getText(), AstNodeType.VAR_TYPE)),
                "func_ret_type",
                AstNodeType.NON_TERMINAL);
    }


    //>>>>>>>>>>instructions

    //instruction_block -> nullable
    private AstNode instructionBlock(GoParser.Instruction_blockContext ctx) {
        if(ctx.instruction() == null) return AstNode.createNullNode();
        AstNode instructionBlockNode = new AstNode(new ArrayList<>(), "instruction_block", AstNodeType.NON_TERMINAL);
        instructionBlockNode.addChildren(instruction(ctx.instruction()));
        return instructionBlockNode;
    }
    //instruction -> nullable
    private List<AstNode> instruction(GoParser.InstructionContext ctx) {
        if(ctx.instruction_dec() == null) return List.of(AstNode.createNullNode());
        List<AstNode> instructionNode = new ArrayList<>();
        instructionNode.add(instructionDec(ctx.instruction_dec()));
        instructionNode.addAll(instruction(ctx.instruction()));
        return instructionNode;
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
                "var_assign",
                AstNodeType.NON_TERMINAL);
    }

    //>>>>>>>>>>if statement
    //if_statement
    private AstNode ifStatement(GoParser.If_statementContext ctx) {
        return new AstNode(List.of(
                expr(ctx.expr()),
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
                expr(ctx.expr()),
                instructionBlock(ctx.instruction_block())),
                "for_loop",
                AstNodeType.NON_TERMINAL);
    }


    //>>>>>>>>>>expressions
    //expr
    private AstNode expr(GoParser.ExprContext ctx) {
        AstNode exprNode;
        exprNode = bExpr(ctx.bexpr());

        //unabstract the abstraction (one layer down)
        if(exprNode.children().isEmpty()) {
            exprNode = new AstNode(List.of(exprNode), "expr", AstNodeType.NON_TERMINAL);
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
        AstNode bTermNode;
        if(ctx.bterm() != null) {
            bTermNode = new AstNode(List.of(
                    bTerm(ctx.bterm()),
                    new AstNode(new ArrayList<>(),ctx.LGC_AND().getText(), AstNodeType.LGC_SMBL),
                    bComp(ctx.bcomp())),
                    "bterm",
                    AstNodeType.NON_TERMINAL);
        } else {
            bTermNode = bComp(ctx.bcomp());
        }
        return bTermNode;
    }
    //bcomp
    private AstNode bComp(GoParser.BcompContext ctx) {
        AstNode bCompNode;
        if(ctx.bcomp() != null) {
            bCompNode = new AstNode(List.of(
                    bComp(ctx.bcomp()),
                    new AstNode(new ArrayList<>(),ctx.CMP_SMBL().getText(), AstNodeType.CMP_SMBL),
                    aExpr(ctx.aexpr())),
                    "bcomp",
                    AstNodeType.NON_TERMINAL);
        } else {
            bCompNode = aExpr(ctx.aexpr());
        }
        return bCompNode;
    }
    //arithmetic expression
    //aexpr
    private AstNode aExpr(GoParser.AexprContext ctx) {
        AstNode aExprNode;
        if(ctx.OP_ADD() != null) {
            aExprNode = new AstNode(List.of(
                    aExpr(ctx.aexpr()),
                    new AstNode(new ArrayList<>(),ctx.OP_ADD().getText(), AstNodeType.OP),
                    aTerm(ctx.aterm())),
                    "aexpr",
                    AstNodeType.NON_TERMINAL);
        } else if(ctx.OP_SUB() != null) {
            aExprNode = new AstNode(List.of(
                    aExpr(ctx.aexpr()),
                    new AstNode(new ArrayList<>(),ctx.OP_SUB().getText(), AstNodeType.OP),
                    aTerm(ctx.aterm())),
                    "aexpr",
                    AstNodeType.NON_TERMINAL);
        } else {
            aExprNode = aTerm(ctx.aterm());
        }
        return aExprNode;
    }
    //aterm
    private AstNode aTerm(GoParser.AtermContext ctx) {
        AstNode aTermNode;
        if(ctx.OP_MULT() != null) {
            aTermNode = new AstNode(List.of(
                    aTerm(ctx.aterm()),
                    new AstNode(new ArrayList<>(),ctx.OP_MULT().getText(), AstNodeType.OP),
                    aFactor(ctx.afactor())),
                    "aterm",
                    AstNodeType.NON_TERMINAL);
        } else if(ctx.OP_DIV() != null) {
            aTermNode = new AstNode(List.of(
                    aTerm(ctx.aterm()),
                    new AstNode(new ArrayList<>(),ctx.OP_DIV().getText(), AstNodeType.OP),
                    aFactor(ctx.afactor())),
                    "aterm",
                    AstNodeType.NON_TERMINAL);
        } else if(ctx.OP_MOD() != null) {
            aTermNode = new AstNode(List.of(
                    aTerm(ctx.aterm()),
                    new AstNode(new ArrayList<>(),ctx.OP_MOD().getText(), AstNodeType.OP),
                    aFactor(ctx.afactor())),
                    "aterm",
                    AstNodeType.NON_TERMINAL);
        } else {
            aTermNode = aFactor(ctx.afactor());
        }
        return aTermNode;
    }
    //afactor
    private AstNode aFactor(GoParser.AfactorContext ctx) {
        AstNode aFactorNode;
        if(ctx.LGC_NOT() != null) {
            aFactorNode = new AstNode(List.of(
                    new AstNode(new ArrayList<>(),ctx.LGC_NOT().getText(), AstNodeType.LGC_SMBL),
                    aFactor(ctx.afactor())),
                    "afactor",
                    AstNodeType.NON_TERMINAL);
        } else if(ctx.OP_ADD() != null) {
            aFactorNode = new AstNode(List.of(
                    new AstNode(new ArrayList<>(),ctx.OP_ADD().getText(), AstNodeType.OP),
                    aFactor(ctx.afactor())),
                    "afactor",
                    AstNodeType.NON_TERMINAL);
        } else if(ctx.OP_SUB() != null) {
            aFactorNode = new AstNode(List.of(
                    new AstNode(new ArrayList<>(),ctx.OP_SUB().getText(), AstNodeType.OP),
                    aFactor(ctx.afactor())),
                    "afactor",
                    AstNodeType.NON_TERMINAL);
        } else if(ctx.SNTX_PARANT_L() != null) {
            aFactorNode = bExpr(ctx.bexpr());
        } else {
            aFactorNode = exprParam(ctx.expr_param());
        }
        return aFactorNode;
    }
    //expr_param
    private AstNode exprParam(GoParser.Expr_paramContext ctx) {
        AstNode exprParamNode;
        String litValue = "";
        DataType dataType = DataType.UNDEF;
        if(ctx.LIT_INT() != null) {
            litValue = ctx.LIT_INT().getText();
            dataType = DataType.INT;
        }
        if(ctx.LIT_FLOAT() != null) {
            litValue = ctx.LIT_FLOAT().getText();
            dataType = DataType.FLOAT;
        }
        if(ctx.LIT_STR() != null) {
            litValue = ctx.LIT_STR().getText();
            dataType = DataType.STR;
        }
        if(ctx.LIT_BOOL() != null) {
            litValue = ctx.LIT_BOOL().getText();
            dataType = DataType.BOOL;
        }

        if(ctx.ID() != null) {
            exprParamNode = new AstNode(new ArrayList<>(), ctx.ID().getText(), AstNodeType.ID);
        } else if(ctx.func_invoc() != null) {
            exprParamNode = funcInvoc(ctx.func_invoc());
        } else {
            exprParamNode = new AstNode(new ArrayList<>(), litValue, AstNodeType.LIT);
            exprParamNode.setDataType(dataType);
        }
        return exprParamNode;
    }


}
