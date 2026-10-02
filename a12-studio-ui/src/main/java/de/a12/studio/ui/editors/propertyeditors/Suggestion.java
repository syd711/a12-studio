package de.a12.studio.ui.editors.propertyeditors;

/**
 * One autocomplete proposal shown by {@link RuleEditorController}'s completion popup: {@code label} is
 * what the list row displays, {@code insertText} is what actually replaces the {@link
 * CompletionResult#replaceStart()}/{@link CompletionResult#replaceEnd()} range on commit (usually the same as
 * {@code label}, but kept separate in case a future provider wants to show a decorated label for a plainer
 * inserted value), and {@code documentation} is an optional short description rendered as a second, dimmer line
 * in the same row (e.g. a field's type and label) - {@code null}/blank hides that line. {@code caretFromEnd} is how
 * many characters before the end of the inserted text the caret is placed after committing (0 = at the end; 1
 * puts it inside the parentheses of an inserted {@code Has()}).
 */
public record Suggestion(String label, String insertText, String documentation, int caretFromEnd) {

  public Suggestion(String label, String insertText, String documentation) {
    this(label, insertText, documentation, 0);
  }

  @Override
  public String toString() {
    return label;
  }
}
