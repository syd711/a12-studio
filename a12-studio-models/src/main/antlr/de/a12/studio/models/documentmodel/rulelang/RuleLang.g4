grammar RuleLang;

@header {
package de.a12.studio.models.documentmodel.rulelang;
}

// Syntax-only grammar for the a12 kernel's Rule/Computation condition language - RuleElement.errorCondition,
// ComputationAlternative.precondition/operation, ComputationConfig.commonPrecondition (see
// documentation/2606-06-doc/kernel-kernel-documentation-ba-en.md sections 2-5). Ported at the syntax level
// only, same scope limitation as QL.g4/QueryLanguageEmitter: no field/type resolution against a real Document
// Model, no semantic checks (arg counts, field existence, type compatibility) - just enough structure to
// reproduce the kernel's own parser-level error categories (section "8.1 Grammar": MVK_INCOMPLETE_INPUT,
// MVK_EXPECTED_TOKEN_NOT_FOUND, MVK_UNEXPECTED_TOKEN, MVK_LEXER_STANDARD_ERROR) - see RuleLanguageSyntaxChecker.
//
// A bare (unbracketed) path is only accepted where the language actually allows a Field/Group/FieldList
// reference - as a call argument, or after Having/In/@From - never as a freestanding term. This is deliberate:
// it's what makes a lone "NumberOfFilledFields" (no parens) a syntax error rather than a valid path, matching
// the kernel doc's own canonical MVK_EXPECTED_TOKEN_NOT_FOUND example.

program: suppressWarningHint* expression EOF;

suppressWarningHint: AT_SUPPRESS_WARNING B_OPEN_PAREN IDENT B_CLOSE_PAREN;

expression
    : andExpression
    | orExpression
    | atom
    ;

andExpression: atom (K_AND atom)+;
orExpression: atom (K_OR atom)+;

atom
    : comparison
    | term
    | B_OPEN_PAREN expression B_CLOSE_PAREN
    ;

comparison: term comparisonOperator term;

comparisonOperator
    : S_EQ | S_NEQ | S_GE | S_LE | S_GT | S_LT
    | K_TOLERANCE_1 | K_TOLERANCE_2 | K_TOLERANCE_5 | K_TOLERANCE_10
    | K_PATTERN_MATCHED | K_PATTERN_VIOLATED
    ;

// Arithmetic/string term - a12's own curly-brace grouping ({ }) is distinct from the round brackets used for
// call args and boolean grouping (see Table 8 in the kernel doc).
term
    : primary                             # primaryTerm
    | B_OPEN_CURLY term B_CLOSE_CURLY     # curlyGroupTerm
    | MINUS term                          # unaryMinusTerm
    | term S_POW term                     # powTerm
    | term (S_STAR | S_SLASH) term        # mulDivTerm
    | term (PLUS | MINUS) term            # addSubTerm
    ;

primary
    : literal
    | fieldValue
    | callExpression
    | bareConstant
    ;

fieldValue: B_OPEN_SQUARE path B_CLOSE_SQUARE;

// Today/Now are the only bare (parenthesis-, bracket-less) zero-arg constants observed used directly as a
// comparison operand in this repo's fixtures (e.g. "[OrderingDate] > Today"). RuleGroup/BaseYear/FirstDay/
// LastDay are likewise bare identifiers per the kernel doc, but only ever appear in call-argument position
// (e.g. "GroupFilled(RuleGroup)") - already covered by pathArgument below, no separate token needed for those.
bareConstant: K_TODAY | K_NOW;

callExpression: IDENT B_OPEN_PAREN arguments? B_CLOSE_PAREN;

arguments: argument (S_COMMA argument)*;

argument
    : pathArgument
    | expression
    ;

// A Field/Group/FieldList reference in argument position, optionally filtered (Having), checked against a
// value list (In) or given an iteration source (@From) - see kernel doc sections 3/4 on Iteration and the
// "In"/"Having"/"@From" operators. Exactly one of these trailing clauses is allowed per the corpus; allowing
// only one (not a repeating suffix) is a deliberate simplification - no fixture combines more than one.
pathArgument: path (K_HAVING expression | K_IN valueItem (S_COMMA valueItem)* | AT_FROM path)?;

valueItem: literal | path;

// Field/Group path: optional leading "$" (the Having-clause "current repetition" operator), optional leading
// "/" for an absolute path, "/"-separated segments (each optionally "*"-suffixed for "all repetitions"), and
// optional trailing category ("->") or semantic-index ("For") accessors - usable both bare (call-argument
// position) and inside the [ ] field-value operator (e.g. "[OrderInformation/ProductType -> Cat]").
path: S_DOLLAR? S_SLASH? segment (S_SLASH segment)* pathAccessor*;

pathAccessor
    : S_ARROW pathSegmentName
    | K_FOR (STRING_LITERAL | path)
    ;

segment: (IDENT | S_DOTDOT | QUOTED_SEGMENT) S_STAR?;

pathSegmentName: IDENT | QUOTED_SEGMENT;

literal
    : STRING_LITERAL
    | NUMBER_LITERAL
    | booleanLiteral
    ;

booleanLiteral: C_TRUE | C_FALSE;

// Keywords - declared ahead of IDENT so they take priority on an equal-length match. Case-insensitive where
// this repo's fixtures actually show case variation (And/AND/and, Having, For/for, In/in); exact-case
// (matching the kernel doc's own spelling) for names with no observed variation.
K_AND: [Aa][Nn][Dd];
K_OR: [Oo][Rr];
K_HAVING: [Hh][Aa][Vv][Ii][Nn][Gg];
K_FOR: [Ff][Oo][Rr];
K_IN: [Ii][Nn];

K_TODAY: 'Today';
K_NOW: 'Now';
C_TRUE: 'True';
C_FALSE: 'False';

K_TOLERANCE_1: 'DiffersWithToleranceRange1';
K_TOLERANCE_2: 'DiffersWithToleranceRange2';
K_TOLERANCE_5: 'DiffersWithToleranceRange5';
K_TOLERANCE_10: 'DiffersWithToleranceRange10';
K_PATTERN_MATCHED: 'PatternMatched';
K_PATTERN_VIOLATED: 'PatternViolated';

AT_SUPPRESS_WARNING: '@SuppressWarning';
AT_FROM: '@From';

S_EQ: '==';
S_NEQ: '!=';
S_GE: '>=';
S_LE: '<=';
S_GT: '>';
S_LT: '<';
S_POW: '^';
S_STAR: '*';
S_SLASH: '/';
S_COMMA: ',';
S_ARROW: '->';
S_DOLLAR: '$';
S_DOTDOT: '..';

PLUS: '+';
MINUS: '-';

B_OPEN_PAREN: '(';
B_CLOSE_PAREN: ')';
B_OPEN_SQUARE: '[';
B_CLOSE_SQUARE: ']';
B_OPEN_CURLY: '{';
B_CLOSE_CURLY: '}';

// Raw newlines are legal inside a string literal (a12 condition text can build a multi-line value via string
// concatenation, e.g. an address block) - "." matches newlines too, same as QL.g4's STRING_LITERAL.
STRING_LITERAL: '"' ( '\\"' | . | [\n\r\t] )*? '"';

NUMBER_LITERAL
  : INTEGER_PART (DOT DIGIT*)?
  | DOT DIGIT+
  ;

fragment INTEGER_PART: ZERO | NON_ZERO_DIGIT DIGIT*;
fragment ZERO: '0';
fragment NON_ZERO_DIGIT: [1-9];
fragment DIGIT: [0-9];

DOT: '.';

// Escapes a Field/Group name that would otherwise collide with a keyword (e.g. a Field literally named "Date"),
// per the kernel doc's single-quote convention - not observed in this repo's fixtures, but cheap to support.
QUOTED_SEGMENT: '\'' (~['\r\n])* '\'';

IDENT: [A-Za-z_] [A-Za-z0-9_]*;

// ";;" runs to end of line and may appear before/after/between partial conditions, not just on its own line.
LINE_COMMENT: ';;' ~[\r\n]* -> skip;

WS: [ \t\r\n]+ -> skip;
