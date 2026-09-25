package de.a12.studio.modelsvalidation.validators.typesetting;

/**
 * The {@link de.a12.studio.modelsvalidation.ModelValidationError#elementId()}s the typesetting validators
 * report, shared with the editor so it can put an error on the row/field it is about.
 */
public final class TypesettingElementIds {

  /** {@code content/orphan}: the orphan limit field. */
  public static final String ORPHAN = "content/orphan";

  /** {@code content/widow}: the widow limit field. */
  public static final String WIDOW = "content/widow";

  private static final String RULE_PREFIX = "content/preventLineBreakRules/";

  private TypesettingElementIds() {
  }

  /**
   * The prevent-line-break rule at {@code index} of {@code content.preventLineBreakRules} (its position in the
   * file, not its row within one of the three tables the editor splits them into).
   */
  public static String rule(int index) {
    return RULE_PREFIX + index;
  }

  /** The index a {@link #rule} id refers to, or -1 if {@code elementId} is not a rule id. */
  public static int ruleIndex(String elementId) {
    if (elementId == null || !elementId.startsWith(RULE_PREFIX)) {
      return -1;
    }
    try {
      return Integer.parseInt(elementId.substring(RULE_PREFIX.length()));
    }
    catch (NumberFormatException e) {
      return -1;
    }
  }
}
