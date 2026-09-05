grammar QL;

program
	: expression EOF
	;

expression
    : andExpression
    | orExpression
    | atom
    ;

andExpression
    : atom L_AND atom (L_AND atom)*
    ;

orExpression
    : atom L_OR atom (L_OR atom)*
    ;

atom
    : primaryExpression
    | L_NOT B_OPEN_PAREN expression B_CLOSE_PAREN
    | B_OPEN_PAREN expression B_CLOSE_PAREN
    ;

// Primary Expression

primaryExpression
    : binaryExpression
    | callExpression
    ;

// Binary Expression

binaryExpression: fieldRef binaryOperator valueExpression;
binaryOperator: S_BINARY_OPERATOR;

valueExpression
  : literal
  | callExpression
  ;

// Call Expression

callExpression
  : callee B_OPEN_PAREN arguments? B_CLOSE_PAREN
  ;

arguments: argument (S_COMMA argument)* ;

callee: I_CALLEE ;
argument: literal | fieldRef | expression;

fieldRef : I_FIELD;

I_FIELD: B_OPEN_SQUARE (SLASH ALPHA (ALPHA | DIGIT)*)+ B_CLOSE_SQUARE;

// Literals

literal
	: nullLiteral
	| booleanLiteral
	| stringLiteral
	| numberLiteral
	;

nullLiteral: C_NULL;
booleanLiteral: C_TRUE | C_FALSE;

stringLiteral: STRING_LITERAL;
STRING_LITERAL : '"' ( '\\"' | . | [\n\r\t] )*? '"';

numberLiteral: NUMBER_LITERAL;

NUMBER_LITERAL
  : MINUS? UNSIGNED_NUMBER_LITERAL
  ;

UNSIGNED_NUMBER_LITERAL
  : INTEGER_LITERAL DOT DIGIT*
  | DOT DIGIT+
  | INTEGER_LITERAL
  ;

INTEGER_LITERAL
  : ZERO
  | NON_ZERO_DIGIT DIGIT*
  ;

ZERO: '0' ;
NON_ZERO_DIGIT : [1-9] ;
DIGIT: ZERO | NON_ZERO_DIGIT ;

// Symbols

S_BINARY_OPERATOR: '>=' | '<=' | '!=' | '==' | '~' | '!~';
S_COLON: ':';
S_COMMA: ',';

// Constants

C_TRUE: 'True';
C_FALSE: 'False';
C_NULL: 'Null';

// Logical operators

L_OR: 'or' | 'Or' | 'OR';
L_AND: 'and' | 'And' | 'AND';
L_NOT: '!';

// Brackets

B_OPEN_PAREN: '(';
B_CLOSE_PAREN: ')';
B_OPEN_SQUARE: '[';
B_CLOSE_SQUARE: ']';

// Identifiers

I_CALLEE: [A-Z][A-Za-z]* ;

I_ARGUMENT_NAME: [a-z][a-zA-Z]* ;


MINUS: '-' ;

DOT: '.' ;

ALPHA: [A-Za-z_] +;

UNDERSCORE: '_' ;

SLASH: '/' ;

SKIPPED_WHITESPACE: [ \t\r\n] -> skip ;

INVALID: . ;
