parser grammar GoParser;

options {
    tokenVocab=GoLexer;
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
        KEY_FUNC ID SNTX_PARANT_L func_param SNTX_PARANT_R func_ret_type instruction_block optnl func
        | ;

func_param:
        ID VAR_TYPE func_param2
        | ;

func_param2:
        SNTX_COMMA ID VAR_TYPE func_param2
        | ;

func_invoc:
        ID SNTX_PARANT_L expr SNTX_PARANT_R
        | ID SNTX_PARANT_L var_assign SNTX_PARANT_R
        | ID SNTX_DOT func_invoc ;

func_return:
        KEY_RET expr ;

func_ret_type:
        VAR_TYPE
        | ;


//>>>>>>>>>>instructions
instruction_block:
        optnl SNTX_BRACE_L optnl instruction optnl SNTX_BRACE_R optnl
        | ;

instruction:
        optnl instruction_dec nl instruction
        | nl
        | ;

instruction_dec:
        if_statement
        | for_loop
        | var_init
        | var_assign
        | func_invoc
        | func_return ;
        //| expr {creator.expr();} ;


//>>>>>>>>>>variables
var_init:
        KEY_VAR ID VAR_TYPE KEY_VAR_ASSGN expr ;

var_assign:
        ID KEY_VAR_ASSGN expr ;


//>>>>>>>>>>if statement
if_statement:
        KEY_IF expr instruction_block else_statement ;

else_statement:
        KEY_ELSE instruction_block
        | ;


//>>>>>>>>>>for loop
for_loop:
        KEY_FOR expr instruction_block ;


//>>>>>>>>>>expressions
expr:
        bexpr
        | aexpr ;

//boolean expression
bexpr:
        bexpr LGC_OR bterm
        | bterm ;
bterm:
        bterm LGC_AND bcomp
        | bcomp ;
bcomp:
        bcomp CMP_SMBL bfactor
        | bfactor ;
bfactor:
        LGC_NOT bfactor
        | SNTX_PARANT_L bexpr SNTX_PARANT_R
        | LIT_BOOL
        | ID
        | aexpr CMP_SMBL aexpr
        | aexpr ;

//arithmetic expression
aexpr:
        aexpr OP_ADD aterm
        | aexpr OP_SUB aterm
        | aterm ;
aterm:
        aterm OP_MULT afactor
        | aterm OP_DIV afactor
        | aterm OP_MOD afactor
        | afactor ;
afactor:
        OP_ADD afactor
        | OP_SUB afactor
        | SNTX_PARANT_L aexpr SNTX_PARANT_R
        | expr_param ;

//indirect terminal rules
expr_param:
        LIT_INT
        | LIT_FLOAT
        | LIT_STR
        | LIT_BOOL
        | ID
        | func_invoc ;


//>>>>>>>>>>newlines
nl:
        SNTX_NEWLINE
        | SNTX_NEWLINE nl ;
optnl:
        nl
        | ;