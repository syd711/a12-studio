package de.a12.studio.models.typesettingmodel;

/**
 * The three kinds of {@link PreventLineBreakRule}, each shown in its own table by the editor. SME's ids
 * ({@code character}/{@code unit}/{@code special}) are what a still-empty rule of that kind is stored as, see
 * {@link #emptyPattern()}.
 */
public enum PreventLineBreakRuleType {

  CHARACTER_SEQUENCE("character"),
  NUMBER_UNIT("unit"),
  SPECIAL_PATTERN("special");

  private final String id;

  PreventLineBreakRuleType(String id) {
    this.id = id;
  }

  /**
   * The placeholder pattern of a rule of this type that has no value yet, {@code "{{unit}}"} etc. SME's editor
   * adds a row this way so it lands in the right table until the user fills it in; the "required" validation
   * then reports it. (An emptied character sequence row is {@code ""} and an emptied unit row is just the number
   * prefix, so those are recognized as their type too, see {@link PreventLineBreakRules#classify}.)
   */
  public String emptyPattern() {
    return "{{" + id + "}}";
  }
}
