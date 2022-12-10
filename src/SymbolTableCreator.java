import java.util.*;

public class SymbolTableCreator extends GoParserBaseListener {
    Map<String, DataType> symbolTable = new HashMap<>();

    //variable and parameter name+type
    @Override
    public void exitVar_init(GoParser.Var_initContext ctx) {
        DataType type = DataType.INT;
        switch (ctx.VAR_TYPE().getText()) {
            case "int" -> type = DataType.INT;
            case "bool" -> type = DataType.BOOL;
            case "float64" -> type = DataType.FLOAT;
            case "string" -> type = DataType.STR;
        }
        symbolTable.put(ctx.ID().getText(), type);
    }

    @Override
    public void exitFunc_param(GoParser.Func_paramContext ctx) {
        //function without parameters
        if(ctx.VAR_TYPE() == null) return;
        DataType type = DataType.INT;
        switch (ctx.VAR_TYPE().getText()) {
            case "int" -> type = DataType.INT;
            case "bool" -> type = DataType.BOOL;
            case "float64" -> type = DataType.FLOAT;
            case "string" -> type = DataType.STR;
        }
        symbolTable.put(ctx.ID().getText(), type);
    }

    @Override
    public void exitFunc_param2(GoParser.Func_param2Context ctx) {
        //function without parameters
        if(ctx.VAR_TYPE() == null) return;
        DataType type = DataType.INT;
        switch (ctx.VAR_TYPE().getText()) {
            case "int" -> type = DataType.INT;
            case "bool" -> type = DataType.BOOL;
            case "float64" -> type = DataType.FLOAT;
            case "string" -> type = DataType.STR;
        }
        symbolTable.put(ctx.ID().getText(), type);
    }
}
