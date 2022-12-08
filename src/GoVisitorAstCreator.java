import java.util.List;

public class GoVisitorAstCreator extends GoParserBaseListener{
    public AstNode ast = new AstNode("program", AstNodeType.NON_TERMINAL);

    @Override
    public void exitFunc(GoParser.FuncContext ctx) {
        ast.addChild(
                new AstNode(List.of(
                        new AstNode(ctx.ID().getText(), AstNodeType.ID),
                        funcParam(ctx.func_param()),
                        funcRetType(ctx.func_ret_type()),
                        instructionBlock(ctx.instruction_block())
                ), "func")
        );
    }

    //func_param -> nullable
    private AstNode funcParam(GoParser.Func_paramContext ctx){
        if(ctx.ID() == null) return new AstNode();
        AstNode funcParamNode = new AstNode("func_param", AstNodeType.NON_TERMINAL);

        funcParamNode.addChild(new AstNode(ctx.ID().getText(), AstNodeType.ID));
        funcParamNode.addChild(new AstNode(ctx.VAR_TYPE().getText(), AstNodeType.VAR_TYPE));

        if(ctx.func_param2().ID() != null) {
            funcParamNode.addChild(funcParam2(ctx.func_param2()));
        }

        return funcParamNode;
    }
    //func_param2 -> nullable
    private AstNode funcParam2(GoParser.Func_param2Context ctx){
        AstNode funcParam2Node = new AstNode(List.of(
                new AstNode(ctx.ID().getText(), AstNodeType.ID),
                new AstNode(ctx.VAR_TYPE().getText(), AstNodeType.VAR_TYPE)
        ), "func_param2");

        if(ctx.func_param2().ID() != null) {
            funcParam2Node.addChild(funcParam2(ctx.func_param2()));
        }

        return funcParam2Node;
    }
    //func_ret_type
    private AstNode funcRetType(GoParser.Func_ret_typeContext ctx){
        return new AstNode();
    }

    //instruction

    //instruction_block
    private AstNode instructionBlock(GoParser.Instruction_blockContext ctx) {
        return new AstNode();
    }
}
