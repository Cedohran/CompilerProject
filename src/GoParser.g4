parser grammar GoParser;

options {
    tokenVocab=GoLexer;
}
@header {

}
@members {
AstCreator creator = new AstCreator();
}

//TODO: newlines und whitespaces

//>>>>>>>>>>main frame
//main funktion zu erst! import und package static!
program:
        pkg impt /*func_main*/ func EOF;

pkg:
        KEY_PKG ID nl;

impt:
        KEY_IMPORT LIT_STR nl
        | KEY_IMPORT LIT_STR nl impt
        | optnl;


//>>>>>>>>>>functions
/*func_main:
        KEY_FUNC ws KEY_MAIN SNTX_PARANT_L SNTX_PARANT_R ws SNTX_BRACE_L instruction SNTX_BRACE_R nl;
*/
func:   // func name(param0 type0, param1 type1, ...) returntype {...}
        KEY_FUNC ID SNTX_PARANT_L func_param SNTX_PARANT_R func_ret_type instruction_block {creator.func(_localctx);} optnl func
        | ;

func_param:
        ID VAR_TYPE func_param2
        | ;

func_param2:
        SNTX_COMMA ID VAR_TYPE func_param2
        | ;

func_invoc:
        ID SNTX_PARANT_L expr SNTX_PARANT_R {creator.funcInvocExpr(_localctx);}
        | ID SNTX_PARANT_L var_assign SNTX_PARANT_R {creator.funcInvocVarAssign(_localctx);}
        | ID SNTX_DOT func_invoc {creator.funcInvocDot(_localctx);} ;

func_return:
        KEY_RET expr ;

func_ret_type:
        VAR_TYPE
        | ;


//>>>>>>>>>>instructions
instruction_block:
        {creator.instructionBlockEnter();} optnl SNTX_BRACE_L optnl instruction optnl SNTX_BRACE_R optnl {creator.instructionBlockExit();}
        | ;

instruction:
        optnl instruction_dec nl instruction
        | nl
        | ;

instruction_dec:
        {creator.ifStatementEnter();} if_statement {creator.ifStatementExit();}
        | {creator.forLoopEnter();} for_loop {creator.forLoopExit();}
        | var_init {creator.varInit(_localctx);}
        | var_assign {creator.varAssign(_localctx);}
        | func_invoc {creator.funcInvoc();}
        | func_return {creator.funcReturn();} ;
        //| expr {creator.expr();} ;


//>>>>>>>>>>variables
var_init:
        KEY_VAR ID VAR_TYPE KEY_VAR_ASSGN expr ;

var_assign:
        ID KEY_VAR_ASSGN expr ;


//>>>>>>>>>>if statement
if_statement:
        KEY_IF bexpr {creator.ifStatementBExpr();} instruction_block {creator.ifStatementBlock();} else_statement ;

else_statement:
        KEY_ELSE instruction_block {creator.elseStatmentBlock();}
        | ;


//>>>>>>>>>>for loop
for_loop:
        KEY_FOR bexpr {creator.forLoopBExpr();} instruction_block {creator.forLoopBlock();} ;


//>>>>>>>>>>expressions
expr:
        bexpr {creator.bExpr();}
        | aexpr {creator.aExpr();};

//boolean expression
bexpr:
        bexpr LGC_OR bterm {creator.bExprLgcOr(_localctx);}
        | bterm {creator.bTerm();} ;
bterm:
        bterm LGC_AND bfactor {creator.bTermLgcAnd(_localctx);}
        | bcomp {creator.bComp();} ;
bcomp:
        bcomp CMP_SMBL bfactor {creator.bCompCmpSmbl(_localctx);}
        | bfactor {creator.bFactor();} ;
bfactor:
        LGC_NOT bfactor {creator.lgcNotFactor();}
        | SNTX_PARANT_L bexpr SNTX_PARANT_R {creator.parantBExpr();}
        | LIT_BOOL {creator.bfactorBool(_localctx);}
        | ID {creator.bfactorID(_localctx);}
        | aexpr {creator.cmpSmblLeftAExpr();} CMP_SMBL aexpr {creator.cmpSmblRightAExpr();} {creator.bFactorCmpSmbl(_localctx);} ;

//arithmetic expression
aexpr:
        aexpr {creator.opAddLeft();} OP_ADD aterm {creator.opAdd();}
        | aexpr OP_SUB aterm {creator.opSub();}
        | aterm {creator.aTerm();};
aterm:
        aterm OP_MULT afactor {creator.opMult();}
        | aterm OP_DIV afactor {creator.opDiv();}
        | aterm OP_MOD afactor {creator.opMod();}
        | afactor {creator.aFactor();};
afactor:
        OP_ADD afactor {creator.opAddFactor();}
        | OP_SUB afactor {creator.opSubFactor();}
        | SNTX_PARANT_L aexpr SNTX_PARANT_R {creator.parantAExpr();}
        | expr_param {creator.exprParam();} ;

//indirect terminal rules
expr_param:
        LIT_INT {creator.litInt(_localctx);}
        | LIT_FLOAT {creator.litFloat(_localctx);}
        | LIT_STR {creator.litStr(_localctx);}
        | LIT_BOOL {creator.litBool(_localctx);}
        | ID {creator.exprParamID(_localctx);}
        | func_invoc {creator.funcInvocExprParam();} ;


//>>>>>>>>>>newlines
nl:
        SNTX_NEWLINE
        | SNTX_NEWLINE nl ;
optnl:
        nl
        | ;