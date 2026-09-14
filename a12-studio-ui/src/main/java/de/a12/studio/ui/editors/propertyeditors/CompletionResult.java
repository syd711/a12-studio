package de.a12.studio.ui.editors.propertyeditors;

import java.util.List;

/**
 * What a {@link SuggestionProvider} found at the current caret position: {@code replaceStart}/{@code
 * replaceEnd} is the character range (into the editor's plain text) that committing a {@link Suggestion}
 * replaces - typically the partial identifier/path the user is mid-typing - and {@code suggestions} is the
 * (already prefix-filtered) list to show, in display order.
 */
public record CompletionResult(int replaceStart, int replaceEnd, List<Suggestion> suggestions) {
}
