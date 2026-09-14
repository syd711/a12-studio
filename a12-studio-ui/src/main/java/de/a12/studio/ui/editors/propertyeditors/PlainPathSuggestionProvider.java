package de.a12.studio.ui.editors.propertyeditors;

import de.a12.studio.models.documentmodel.Element;
import de.a12.studio.modelsvalidation.validators.ElementIndex;
import de.a12.studio.modelsvalidation.validators.FieldPathSuggestions;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Suggests relative field paths (kernel {@code ElementPathUtils} convention, e.g. {@code "../fieldName"}) for
 * a bare identifier/path the user is typing - the Rule/Computation condition language's field-reference syntax
 * (no brackets, no grammar available - see {@code FieldPathSuggestions}). Triggers as soon as the caret is
 * preceded by at least one path character; the whole run is replaced on commit.
 */
public class PlainPathSuggestionProvider implements SuggestionProvider {

  // A run of path characters immediately before the caret: identifier chars, "/" (absolute-path segment
  // separator) and ".." (parent-segment marker) - deliberately excludes whitespace/operators so the trigger
  // resets between separate expressions in a longer condition.
  private static final Pattern PATH_RUN = Pattern.compile("[\\w./]*$");

  private final ElementIndex index;
  private final Element referencingElement;

  public PlainPathSuggestionProvider(@NonNull ElementIndex index, @NonNull Element referencingElement) {
    this.index = index;
    this.referencingElement = referencingElement;
  }

  @Override
  public Optional<CompletionResult> suggest(String text, int caretPosition) {
    String beforeCaret = text.substring(0, caretPosition);
    Matcher matcher = PATH_RUN.matcher(beforeCaret);
    if (!matcher.find() || matcher.start() == matcher.end()) {
      return Optional.empty();
    }
    String prefix = matcher.group();
    List<Suggestion> suggestions = FieldPathSuggestions.relativePaths(index, referencingElement).stream()
        .filter(entry -> entry.path().toLowerCase().contains(prefix.toLowerCase()))
        .map(entry -> new Suggestion(entry.path(), entry.path(), entry.documentation()))
        .toList();
    if (suggestions.isEmpty()) {
      return Optional.empty();
    }
    return Optional.of(new CompletionResult(matcher.start(), matcher.end(), suggestions));
  }
}
