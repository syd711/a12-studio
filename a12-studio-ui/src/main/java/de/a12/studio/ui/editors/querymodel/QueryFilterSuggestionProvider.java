package de.a12.studio.ui.editors.querymodel;

import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.modelsvalidation.validators.query.QueryFilterCompletion;
import de.a12.studio.modelsvalidation.validators.query.QueryFilterCompletion.Proposal;
import de.a12.studio.ui.editors.propertyeditors.BracketedPathSuggestionProvider;
import de.a12.studio.ui.editors.propertyeditors.CompletionResult;
import de.a12.studio.ui.editors.propertyeditors.Suggestion;
import de.a12.studio.ui.editors.propertyeditors.SuggestionProvider;

import java.util.Optional;

/**
 * Completion for a Query filter definition (SME's qmm editor proposer): field paths while the caret is inside
 * an unclosed {@code [...]} ({@link BracketedPathSuggestionProvider}), everything else - functions, operators,
 * values, {@code Has} relationships and roles, {@code and}/{@code or} - from {@link QueryFilterCompletion}.
 */
final class QueryFilterSuggestionProvider implements SuggestionProvider {

  private static final String CARET_MARKER = "$0";

  private final BracketedPathSuggestionProvider fieldPaths;
  private final QueryFilterCompletion completion;
  private final DocumentModel scope;

  QueryFilterSuggestionProvider(BracketedPathSuggestionProvider fieldPaths, QueryFilterCompletion completion,
      DocumentModel scope) {
    this.fieldPaths = fieldPaths;
    this.completion = completion;
    this.scope = scope;
  }

  @Override
  public Optional<CompletionResult> suggest(String text, int caretPosition) {
    String beforeCaret = text.substring(0, caretPosition);
    if (beforeCaret.lastIndexOf('[') > beforeCaret.lastIndexOf(']')) {
      return fieldPaths.suggest(text, caretPosition);
    }
    return completion.complete(text, caretPosition, scope).map(result -> new CompletionResult(
        result.replaceStart(), result.replaceEnd(), result.proposals().stream().map(QueryFilterSuggestionProvider::toSuggestion).toList()));
  }

  private static Suggestion toSuggestion(Proposal proposal) {
    String insertText = proposal.insertText();
    int caret = insertText.indexOf(CARET_MARKER);
    if (caret < 0) {
      return new Suggestion(proposal.label(), insertText, proposal.detail());
    }
    String plain = insertText.replace(CARET_MARKER, "");
    return new Suggestion(proposal.label(), plain, proposal.detail(), plain.length() - caret);
  }
}
