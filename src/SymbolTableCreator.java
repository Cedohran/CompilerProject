import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.*;

public class SymbolTableCreator extends GoParserBaseListener {
    //symbol table for variables with corresponding data type
    Map<String, DataType> symbolTableVar = new HashMap<>();
    Map<String, DataType> symbolTableFuncReturn = new HashMap<>();

    //symbolTableFuncReturn
    @Override
    public void exitFunc(GoParser.FuncContext ctx) {
        if(ctx.func_ret_type() != null) {
            if(ctx.func_ret_type().VAR_TYPE() == null) {
                symbolTableFuncReturn.put(ctx.ID().getText(), DataType.UNDEF);
            } else {
                symbolTableFuncReturn.put(ctx.ID().getText(), getVarDataType(ctx.func_ret_type().VAR_TYPE()));
            }
        }
    }

    //symbolTableVar (variable and parameter name+type)
    @Override
    public void exitVar_init(GoParser.Var_initContext ctx) {
        DataType type = getVarDataType(ctx.VAR_TYPE());
        symbolTableVar.put(ctx.ID().getText(), type);
    }

    @Override
    public void exitFunc_param(GoParser.Func_paramContext ctx) {
        //function without parameters
        if(ctx.VAR_TYPE() == null) return;
        DataType type = getVarDataType(ctx.VAR_TYPE());
        symbolTableVar.put(ctx.ID().getText(), type);
    }

    @Override
    public void exitFunc_param2(GoParser.Func_param2Context ctx) {
        //function without parameters
        if(ctx.VAR_TYPE() == null) return;
        DataType type = getVarDataType(ctx.VAR_TYPE());
        symbolTableVar.put(ctx.ID().getText(), type);
    }

    private DataType getVarDataType(TerminalNode varType){
        DataType type = DataType.UNDEF;
        switch (varType.getText()) {
            case "int" -> type = DataType.INT;
            case "bool" -> type = DataType.BOOL;
            case "float64" -> type = DataType.FLOAT;
            case "string" -> type = DataType.STR;
        }
        return type;
    }
}
