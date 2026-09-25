package de.a12.studio.models.typesettingmodel;

/**
 * One of the curated "special pattern" rules the print engine team supports. {@code label} is what the editor
 * shows, {@code regex} what is stored in {@link PreventLineBreakRule#getPattern()}.
 */
public record SpecialPattern(String label, String regex, String example) {

  /** {@code §{digit} Abs. {digit}}, e.g. for referencing German legal texts. */
  public static final SpecialPattern PARAGRAPH_SECTION =
      new SpecialPattern("§{digit} Abs. {digit}", "§\\d+ Abs\\. \\d+", "§3 Abs. 7");

  /** {@code {digit}({digit})({letter})}, e.g. for referring to document sections. */
  public static final SpecialPattern DOCUMENT_SECTION =
      new SpecialPattern("{digit}({digit})({letter})", "\\d+\\(\\d+\\)\\([a-zA-Z]\\)", "219(1)(a)");
}
