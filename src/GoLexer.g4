lexer grammar GoLexer;

//ignore comments and whitespaces
SNTX_WHITE_SPC   : (' ' | '\t') -> skip ;
COMMENT   : ('//' ~[\r\n]*) -> skip ;
COMMENT_BLOCK   : ('/*' ~[*]* '*/') -> skip ;

//literals
LIT_STR     : '"' ~["]* '"' ;
LIT_INT     : [0-9]+ ;
LIT_FLOAT   : [0-9]+'.'[0-9]+ ;
LIT_BOOL    : 'true' | 'false' ;

//arithmetic operators
OP_ADD    : '+' ;
OP_SUB    : '-' ;
OP_MULT   : '*' ;
OP_DIV    : '/' ;
OP_MOD    : '%' ;

//comparison symbols
CMP_SMBL  : '==' | '!=' | '<' | '>' | '<=' | '>=' ;

//boolean expression
LGC_OR    : 'or' | '||' ;
LGC_AND   : 'and' | '&&' ;
LGC_NOT   : 'not' | '!' ;

//syntax symbols
SNTX_NEWLINE     : [\r\n]+ ;
SNTX_PARANT_L    : '(' ;
SNTX_PARANT_R    : ')' ;
SNTX_BRACKET_L   : '[' ;
SNTX_BRACKET_R   : ']' ;
SNTX_BRACE_L     : '{' ;
SNTX_BRACE_R     : '}' ;
SNTX_DOT         : '.' ;
SNTX_COMMA       : ',' ;

//typing
VAR_TYPE  :  'int' | 'float64' | 'bool' | 'string' ;

//key words
KEY_IF          : 'if' ;
KEY_ELSE        : 'else';
KEY_FOR         : 'for' ;
KEY_VAR         : 'var' ;
KEY_PKG         : 'package' ;
KEY_IMPORT      : 'import' ;
KEY_FUNC        : 'func' ;
KEY_RET         : 'return' ;
KEY_VAR_ASSGN   : '=' ;

//identifier (must be last)
//like java
ID : ([a-z] | [A-Z] | '$' | '_') ([a-z] | [A-Z] | [0-9] | '$' | '_')* ;



