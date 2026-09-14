package de.a12.studio.ui.editors.propertyeditors;

import java.util.Optional;

/**
 * Supplies {@link RuleEditorController}'s completion popup with proposals for the current editor state.
 * Implementations own both halves of "when to trigger" (RichTextFX itself has no completion API - see {@code
 * RICHTEXT.md} §3 - so each expression language decides its own trigger character/prefix) and "what a commit
 * replaces": a plain condition/computation field triggers on a trailing identifier and replaces just that
 * identifier, while a bracketed field reference (QL's {@code [/Path]}, the Overview expression language's
 * {@code [Field]}) triggers after {@code [} and replaces everything typed since.
 */
@FunctionalInterface
public interface SuggestionProvider {

  /**
   * @param text          the editor's full current plain text
   * @param caretPosition the caret's offset into {@code text}
   * @return empty to show no popup (or hide one already showing); otherwise the replace range and the
   * suggestions to display, already filtered/sorted by whatever prefix precedes the caret
   */
  Optional<CompletionResult> suggest(String text, int caretPosition);
}
