import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.*;

public class SymbolTableCreator extends GoParserBaseListener {
    //symbol table for variables with corresponding data type
    //Map<String, DataType> symbolTableVar = new HashMap<>();
    //return type of function
    Map<String, DataType> symbolTableFuncReturn = new HashMap<>();
    //parameters(types as list) of function
    Map<String, List<DataType>> symbolTableFuncParam = new HashMap<>();
    //all initialised variables(name,type) and parameters(name,type) available in function scope
    Map<String, Map<String, DataType>> funcScopeTable = new HashMap<>();

    //helper
    String currentFunc = "";

    @Override
    public void enterFunc(GoParser.FuncContext ctx) {
        if(ctx.ID() == null) return;
        currentFunc = ctx.ID().getText();
        //if function already declared throw error
        if(funcScopeTable.get(currentFunc) != null) {
            //throw new ParseException("function "+currentFunc+" already declared.");
        }

        symbolTableFuncParam.put(currentFunc, new ArrayList<>());
        funcScopeTable.put(currentFunc, new HashMap<>());
    }

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

    //add var to function variable table
    @Override
    public void enterVar_init(GoParser.Var_initContext ctx) {
        String varId = ctx.ID().getText();
        DataType type = getVarDataType(ctx.VAR_TYPE());
        Map<String, DataType> currentVarTable = funcScopeTable.get(currentFunc);

        currentVarTable.put(varId, type);
    }

    @Override
    public void enterFunc_param(GoParser.Func_paramContext ctx) {
        //function without parameters
        if(ctx.VAR_TYPE() == null) return;
        String varId = ctx.ID().getText();
        DataType type = getVarDataType(ctx.VAR_TYPE());
        Map<String, DataType> currentVarTable = funcScopeTable.get(currentFunc);

        currentVarTable.put(varId, type);
        symbolTableFuncParam.get(currentFunc).add(type);
    }

    @Override
    public void enterFunc_param2(GoParser.Func_param2Context ctx) {
        //only one param
        if(ctx.VAR_TYPE() == null) return;
        String varId = ctx.ID().getText();
        DataType type = getVarDataType(ctx.VAR_TYPE());
        Map<String, DataType> currentVarTable = funcScopeTable.get(currentFunc);

        currentVarTable.put(varId, type);
        symbolTableFuncParam.get(currentFunc).add(type);
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
