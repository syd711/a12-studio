package de.a12.studio.ui.editors.propertyeditors;

import de.a12.studio.models.documentmodel.Element;
import de.a12.studio.modelsvalidation.validators.ElementIndex;
import de.a12.studio.modelsvalidation.validators.FieldPathSuggestions;
import org.jspecify.annotations.NonNull;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Optional;

/**
 * Suggests bare field/group names, scoped to the innermost enclosing {@code kontext(...)  { }} block, for the
 * Overview/Form "Expression" language ({@code docs/2606-06-doc/expression-expression-docs.md}'s grammar) -
 * unlike {@link BracketedPathSuggestionProvider}'s QL-style absolute paths, this language's {@code [FieldName]}
 * and {@code groupOperation: 'kontext' '(' fieldName ... ')'} productions only ever name a *direct* child of
 * whatever group the caret is currently nested inside, never a path. There is no grammar/parser dependency
 * available for this language (see the autocomplete plan) - {@link #currentScope} is a small hand-rolled
 * brace/keyword scanner, not a real parser, good enough to track {@code kontext(...)} nesting for this purpose.
 * <p>
 * Two triggers share this provider:
 * <ul>
 *   <li>right after an unclosed {@code [} - suggests the current scope's direct field names</li>
 *   <li>right after an unclosed {@code kontext(} (before any {@code ,delimiter=...} option) - suggests the
 *   current scope's direct child group names, to descend into</li>
 * </ul>
 */
public class ExpressionScopeSuggestionProvider implements SuggestionProvider {

  private final ElementIndex index;

  public ExpressionScopeSuggestionProvider(@NonNull ElementIndex index) {
    this.index = index;
  }

  @Override
  public Optional<CompletionResult> suggest(String text, int caretPosition) {
    String beforeCaret = text.substring(0, caretPosition);
    int openBracket = beforeCaret.lastIndexOf('[');
    int closeBracket = beforeCaret.lastIndexOf(']');
    int openParen = beforeCaret.lastIndexOf('(');
    int closeParen = beforeCaret.lastIndexOf(')');

    boolean bracketOpen = openBracket >= 0 && openBracket > closeBracket;
    boolean parenOpen = openParen >= 0 && openParen > closeParen && precededByKontext(beforeCaret, openParen);

    if (bracketOpen && (!parenOpen || openBracket > openParen)) {
      return suggestNames(text, openBracket, caretPosition, FieldPathSuggestions::fieldNames);
    }
    if (parenOpen && !beforeCaret.substring(openParen + 1).contains(",")) {
      return suggestNames(text, openParen, caretPosition, FieldPathSuggestions::groupNames);
    }
    return Optional.empty();
  }

  private Optional<CompletionResult> suggestNames(String text, int openMarker, int caretPosition,
      java.util.function.Function<List<Element>, List<FieldPathSuggestions.Entry>> namesOf) {
    String prefix = text.substring(openMarker + 1, caretPosition);
    List<Element> children = index.directChildren(currentScope(text, openMarker));
    List<Suggestion> suggestions = namesOf.apply(children).stream()
        .filter(entry -> entry.path().toLowerCase().contains(prefix.toLowerCase()))
        .map(entry -> new Suggestion(entry.path(), entry.path(), entry.documentation()))
        .toList();
    if (suggestions.isEmpty()) {
      return Optional.empty();
    }
    return Optional.of(new CompletionResult(openMarker + 1, caretPosition, suggestions));
  }

  /** Whether the identifier immediately before {@code openParenIndex} (skipping whitespace) is {@code kontext}. */
  private static boolean precededByKontext(String text, int openParenIndex) {
    int i = openParenIndex - 1;
    while (i >= 0 && Character.isWhitespace(text.charAt(i))) {
      i--;
    }
    int end = i + 1;
    while (i >= 0 && isIdentifierChar(text.charAt(i))) {
      i--;
    }
    return "kontext".equals(text.substring(i + 1, end));
  }

  /**
   * The chain of enclosing {@code kontext(name)} group names active at {@code position}, outermost first -
   * i.e. what {@code position} is nested inside, ignoring {@code case [...] {...}} blocks (those don't change
   * field scope, per the grammar) and anything inside string literals. Empty means the model's own root
   * scope.
   */
  static List<String> currentScope(String text, int position) {
    List<String> scope = new ArrayList<>();
    Deque<Boolean> braceIntroducesScope = new ArrayDeque<>();
    int i = 0;
    while (i < position) {
      char c = text.charAt(i);
      if (c == '"') {
        i = skipStringLiteral(text, i, position);
        continue;
      }
      if (isIdentifierChar(c)) {
        int start = i;
        while (i < position && isIdentifierChar(text.charAt(i))) {
          i++;
        }
        if (text.substring(start, i).equals("kontext")) {
          int afterOpeningBrace = tryEnterKontextScope(text, i, position, scope, braceIntroducesScope);
          if (afterOpeningBrace >= 0) {
            i = afterOpeningBrace;
          }
        }
        continue;
      }
      if (c == '{') {
        braceIntroducesScope.push(false);
        i++;
        continue;
      }
      if (c == '}') {
        if (!braceIntroducesScope.isEmpty() && braceIntroducesScope.pop() && !scope.isEmpty()) {
          scope.remove(scope.size() - 1);
        }
        i++;
        continue;
      }
      i++;
    }
    return scope;
  }

  /**
   * If the {@code kontext} keyword ending at {@code afterKeyword} is followed by {@code (<name>...)  {}, pushes
   * {@code <name>} onto {@code scope}/{@code braceIntroducesScope} and returns the position just past the
   * {@code {}. Returns {@code -1} (nothing pushed) for a malformed/incomplete {@code kontext} - e.g. one the
   * user is still in the middle of typing - leaving the caller to resume scanning right after the keyword.
   */
  private static int tryEnterKontextScope(String text, int afterKeyword, int limit, List<String> scope, Deque<Boolean> braceIntroducesScope) {
    int j = skipWhitespace(text, afterKeyword, limit);
    if (j >= limit || text.charAt(j) != '(') {
      return -1;
    }
    j = skipWhitespace(text, j + 1, limit);
    int nameStart = j;
    while (j < limit && isIdentifierChar(text.charAt(j))) {
      j++;
    }
    String name = text.substring(nameStart, j);
    int closeParen = findChar(text, j, limit, ')');
    if (closeParen < 0) {
      return -1;
    }
    int afterParen = skipWhitespace(text, closeParen + 1, limit);
    if (afterParen >= limit || text.charAt(afterParen) != '{') {
      return -1;
    }
    scope.add(name);
    braceIntroducesScope.push(true);
    return afterParen + 1;
  }

  private static boolean isIdentifierChar(char c) {
    return Character.isLetterOrDigit(c) || c == '_' || c == '-';
  }

  private static int skipWhitespace(String text, int from, int limit) {
    int i = from;
    while (i < limit && Character.isWhitespace(text.charAt(i))) {
      i++;
    }
    return i;
  }

  private static int findChar(String text, int from, int limit, char target) {
    for (int i = from; i < limit; i++) {
      if (text.charAt(i) == target) {
        return i;
      }
    }
    return -1;
  }

  private static int skipStringLiteral(String text, int quoteIndex, int limit) {
    int i = quoteIndex + 1;
    while (i < limit) {
      char c = text.charAt(i);
      if (c == '\\') {
        i += 2;
        continue;
      }
      if (c == '"') {
        return i + 1;
      }
      i++;
    }
    return limit;
  }
}
