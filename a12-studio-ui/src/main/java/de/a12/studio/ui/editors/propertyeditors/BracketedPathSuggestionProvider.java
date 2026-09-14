package de.a12.studio.ui.editors.propertyeditors;

import de.a12.studio.modelsvalidation.validators.ElementIndex;
import de.a12.studio.modelsvalidation.validators.FieldPathSuggestions;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.Optional;

/**
 * Suggests absolute field paths (e.g. {@code /Group/fieldName}) for the caret's current position inside an
 * unclosed {@code [...]} - the QL grammar's {@code I_FIELD} token ({@code
 * a12-studio-models/src/main/antlr/.../ql/QL.g4}: {@code [} + one or more {@code /}-prefixed name segments +
 * {@code ]}), used by a QL filter definition. Also matches the Overview "Expression" language's {@code
 * [FieldName]} syntax closely enough as a trigger shape, but that language wants bare, scope-relative field
 * names rather than absolute paths - see {@code ExpressionScopeSuggestionProvider} for that case instead.
 */
public class BracketedPathSuggestionProvider implements SuggestionProvider {

  private final ElementIndex index;

  public BracketedPathSuggestionProvider(@NonNull ElementIndex index) {
    this.index = index;
  }

  @Override
  public Optional<CompletionResult> suggest(String text, int caretPosition) {
    String beforeCaret = text.substring(0, caretPosition);
    int openBracket = beforeCaret.lastIndexOf('[');
    int closeBracket = beforeCaret.lastIndexOf(']');
    if (openBracket < 0 || openBracket < closeBracket) {
      // No "[" before the caret, or the nearest one is already closed by a "]" - not inside a field reference.
      return Optional.empty();
    }
    String prefix = beforeCaret.substring(openBracket + 1);
    List<Suggestion> suggestions = FieldPathSuggestions.absolutePaths(index).stream()
        .filter(entry -> entry.path().toLowerCase().contains(prefix.toLowerCase()))
        .map(entry -> new Suggestion(entry.path(), entry.path(), entry.documentation()))
        .toList();
    if (suggestions.isEmpty()) {
      return Optional.empty();
    }
    return Optional.of(new CompletionResult(openBracket + 1, caretPosition, suggestions));
  }
}
