import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.*;

public class SymbolTableCreator extends GoParserBaseListener {
    //symbol table for variables with corresponding data type
    //Map<String, DataType> symbolTableVar = new HashMap<>();
    //return type of function
    Map<String, DataType> symbolTableFuncReturn = new HashMap<>();
    //parameters(types as list) of function
    Map<String, List<DataType>> symbolTableFuncParam = new HashMap<>();
    //parameters(types + names as map) of function
    Map<String, Map<String, DataType>> symbolTableFuncParamNameType = new HashMap<>();
    //all initialised variables(name,type) and parameters(name,type) available in function scope
    Map<String, Map<String, DataType>> funcScopeTable = new HashMap<>();
    //current function for scope
    private String currentFunc = "";

    //can't overwrite function signature -> save exception for later throw
    GoParseException exception = null;

    //throw manually
    public void problems() throws GoParseException {
        if(exception != null)
            throw exception;
    }

    @Override
    public void enterFunc(GoParser.FuncContext ctx) {
        if(ctx.ID() == null) return;
        currentFunc = ctx.ID().getText();
        //if function already declared throw error
        if(funcScopeTable.get(currentFunc) != null) {
            if(exception == null)
                exception = new GoParseException("function "+currentFunc+" already declared.");
            return;
        }
        symbolTableFuncParam.put(currentFunc, new ArrayList<>());
        symbolTableFuncParamNameType.put(currentFunc, new HashMap<>());
        funcScopeTable.put(currentFunc, new HashMap<>());
    }

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

    @Override
    public void enterFunc_param(GoParser.Func_paramContext ctx) {
        //function without parameters
        if(ctx.VAR_TYPE() == null) return;
        String varId = ctx.ID().getText();
        DataType type = getVarDataType(ctx.VAR_TYPE());
        Map<String, DataType> currentVarTable = funcScopeTable.get(currentFunc);
        //variable name already used
        if(currentVarTable.get(varId) != null) {
            exception = new GoParseException("variable "+varId+" already declared in function "+currentFunc);
            return;
        }
        currentVarTable.put(varId, type);
        symbolTableFuncParam.get(currentFunc).add(type);
        symbolTableFuncParamNameType.get(currentFunc).put(varId, type);
    }

    @Override
    public void enterFunc_param2(GoParser.Func_param2Context ctx) {
        //only one param
        if(ctx.VAR_TYPE() == null) return;
        String varId = ctx.ID().getText();
        DataType type = getVarDataType(ctx.VAR_TYPE());
        Map<String, DataType> currentVarTable = funcScopeTable.get(currentFunc);
        //variable name already used
        if(currentVarTable.get(varId) != null) {
            exception = new GoParseException("variable "+varId+" already declared in function "+currentFunc);
            return;
        }
        currentVarTable.put(varId, type);
        symbolTableFuncParam.get(currentFunc).add(type);
        symbolTableFuncParamNameType.get(currentFunc).put(varId, type);
    }

    @Override
    public void enterVar_init(GoParser.Var_initContext ctx) {
        String varId = ctx.ID().getText();
        DataType type = getVarDataType(ctx.VAR_TYPE());
        Map<String, DataType> currentVarTable = funcScopeTable.get(currentFunc);
        //variable name already used
        if(currentVarTable.get(varId) != null) {
            exception = new GoParseException("variable "+varId+" already declared in function "+currentFunc);
            return;
        }
        currentVarTable.put(varId, type);
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
