grammar ExpressionLang;

@header {
package de.a12.studio.models.expressionlang;
}

// Syntax-only grammar for the a12 platform's "Expression" language (documentation/2606-06-doc/expression-
// expression-docs.md, section "Grammar" - a text/label-templating language, not the boolean Rule/Computation
// condition language RuleLang.g4 covers). Used wherever a display text is built from Document Model field
// values with conditional branches and locale-specific literals - confirmed by running the syntax checker
// against every real Overview Model expression column in testing/workspaces (13 of them, e.g. Company_OM's
// siblings' "kontext(Product){...}" columns): RuleLang.g4 rejects every one of them (they use "kontext"/
// "case" constructs RuleLang has no notion of), while this grammar - transcribed directly from the doc's own
// ANTLR declaration - accepts them all. Ported at the syntax level only, matching RuleLang.g4/QL.g4's own
// scope limitation: no field/type resolution against a real Document Model, just enough structure to tell a
// syntactically valid expression from a broken one.
program: element* EOF;

element
    : tokenElement
    | valueElement
    | operationElement
    ;

tokenElement
    : NEWPARAGRAPH
    | NEWLINE
    ;

valueElement
    : stringValue
    | fieldValue
    | multilingualValue
    ;

stringValue: QUOTE_STRING;

fieldValue: B_OPEN_SQUARE fieldName B_CLOSE_SQUARE;

operationElement
    : groupOperation
    | caseOperation
    ;

groupOperation: K_KONTEXT B_OPEN_PAREN fieldName delimiterOption B_CLOSE_PAREN B_OPEN_CURLY element+ B_CLOSE_CURLY;

delimiterOption: (S_COMMA K_DELIMITER S_EQ stringValue)?;

caseOperation: K_CASE fieldValue operator stringValue B_OPEN_CURLY element+ B_CLOSE_CURLY;

operator: S_NEQ | S_EQ;

multilingualValue: B_OPEN_PAREN localizedTexts B_CLOSE_PAREN;

localizedTexts: localizedText (S_COMMA localizedText)*;

localizedText: locale S_COLON stringValue;

locale: localeChar+;

localeChar
    : CHAR
    | DIGIT
    | UNDERSCORE
    ;

fieldName: fieldNameChar+;

fieldNameChar
    : CHAR
    | DIGIT
    | UNDERSCORE
    | DASH
    ;

// Keywords, declared ahead of the bare CHAR+ identifiers they'd otherwise be swallowed by so they take
// priority on an equal-length match (same convention as RuleLang.g4's K_* tokens).
K_KONTEXT: 'kontext';
K_CASE: 'case';
K_DELIMITER: 'delimiter';

CHAR: [A-Za-z];
DIGIT: [0-9];
DASH: '-';
UNDERSCORE: '_';

S_EQ: '=';
S_NEQ: '!=';
S_COMMA: ',';
S_COLON: ':';

B_OPEN_PAREN: '(';
B_CLOSE_PAREN: ')';
B_OPEN_SQUARE: '[';
B_CLOSE_SQUARE: ']';
B_OPEN_CURLY: '{';
B_CLOSE_CURLY: '}';

// Same pattern as RuleLang.g4/QL.g4's own string literal - "." already matches a raw newline in ANTLR4, the
// explicit [\n\r\t] alternative is carried over verbatim from the documented grammar regardless.
QUOTE_STRING: '"' ( '\\"' | . | [\n\r\t] )*? '"';

NEWPARAGRAPH: '\n' ([ \t]* '\n') ([ \t]* '\n')+;
NEWLINE: '\n' [ \t]* '\n';

SKIPPED_WHITESPACE: [ \t\r\n] -> skip;

INVALID: .;
