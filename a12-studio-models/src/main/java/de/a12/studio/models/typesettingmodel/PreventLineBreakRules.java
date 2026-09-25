package de.a12.studio.models.typesettingmodel;

import java.util.List;

/**
 * Tells the three kinds of {@link PreventLineBreakRule} apart and converts between what a rule stores (a regular
 * expression) and what the editor shows/edits (a plain value), ported from SME's {@code print-typesetting}
 * library ({@code rule-conversion.ts}, {@code rule-regex.ts}, {@code special-pattern.ts}). Every rule is a
 * regex in the file:
 * <ul>
 *   <li><b>Special pattern</b>: exactly one of {@link #SPECIAL_PATTERNS}' regexes.</li>
 *   <li><b>Number unit</b>: {@link #NUMBER_UNIT_PREFIX} followed by the regex-escaped unit, e.g. {@code Km}.</li>
 *   <li><b>Character sequence</b>: anything else - the regex-escaped word, e.g. {@code T-shirt}.</li>
 * </ul>
 * The order of those checks matters (a unit rule never equals a special pattern, but a character sequence is
 * simply "neither"), so {@link #classify} must be used rather than testing a single kind in isolation.
 */
public final class PreventLineBreakRules {

  /** A number (with optional sign, thousands separators and decimals), then one space, then the unit. */
  public static final String NUMBER_UNIT_PREFIX = "[+-]?(\\d{1,3}(?:[.,]\\d{3})+|\\d+)(?:[.,]\\d+)? ";

  public static final List<SpecialPattern> SPECIAL_PATTERNS =
      List.of(SpecialPattern.PARAGRAPH_SECTION, SpecialPattern.DOCUMENT_SECTION);

  // The characters SME's escapeRegex/unescapeRegex handle: /[.*+?^${}()|[\]\\]/
  private static final String REGEX_SPECIAL_CHARACTERS = ".*+?^${}()|[]\\";

  private PreventLineBreakRules() {
  }

  /** The kind of rule {@code pattern} is; a missing pattern counts as an (empty) character sequence. */
  public static PreventLineBreakRuleType classify(String pattern) {
    if (pattern == null) {
      return PreventLineBreakRuleType.CHARACTER_SEQUENCE;
    }
    if (isEmpty(pattern, PreventLineBreakRuleType.SPECIAL_PATTERN) || findSpecialByRegex(pattern) != null) {
      return PreventLineBreakRuleType.SPECIAL_PATTERN;
    }
    if (isEmpty(pattern, PreventLineBreakRuleType.NUMBER_UNIT) || pattern.startsWith(NUMBER_UNIT_PREFIX)) {
      return PreventLineBreakRuleType.NUMBER_UNIT;
    }
    return PreventLineBreakRuleType.CHARACTER_SEQUENCE;
  }

  /**
   * What the editor shows for {@code pattern} of the given {@code type}: the plain word, the plain unit, or the
   * special pattern's label. Empty for a rule with no value yet and, for a special pattern, for a regex that is
   * not one of the curated ones.
   */
  public static String toValue(PreventLineBreakRuleType type, String pattern) {
    if (pattern == null || isEmpty(pattern, type)) {
      return "";
    }
    return switch (type) {
      case CHARACTER_SEQUENCE -> unescapeRegex(pattern);
      case NUMBER_UNIT -> pattern.startsWith(NUMBER_UNIT_PREFIX)
          ? unescapeRegex(pattern.substring(NUMBER_UNIT_PREFIX.length())) : "";
      case SPECIAL_PATTERN -> {
        SpecialPattern special = findSpecialByRegex(pattern);
        yield special == null ? "" : special.label();
      }
    };
  }

  /** The regex to store for {@code value} of the given {@code type}, the inverse of {@link #toValue}. */
  public static String toPattern(PreventLineBreakRuleType type, String value) {
    String text = value == null ? "" : value;
    return switch (type) {
      case CHARACTER_SEQUENCE -> escapeRegex(text);
      case NUMBER_UNIT -> NUMBER_UNIT_PREFIX + escapeRegex(text);
      case SPECIAL_PATTERN -> {
        SpecialPattern special = findSpecialByLabel(text);
        yield special == null ? "" : special.regex();
      }
    };
  }

  /** The curated special pattern stored as {@code regex}, or {@code null}. */
  public static SpecialPattern findSpecialByRegex(String regex) {
    return SPECIAL_PATTERNS.stream().filter(special -> special.regex().equals(regex)).findFirst().orElse(null);
  }

  private static SpecialPattern findSpecialByLabel(String label) {
    return SPECIAL_PATTERNS.stream().filter(special -> special.label().equals(label)).findFirst().orElse(null);
  }

  private static boolean isEmpty(String pattern, PreventLineBreakRuleType type) {
    return type.emptyPattern().equals(pattern);
  }

  /** Backslash-escapes every regex metacharacter, like SME's {@code escapeRegex}. */
  static String escapeRegex(String text) {
    StringBuilder escaped = new StringBuilder(text.length() + 4);
    for (char character : text.toCharArray()) {
      if (REGEX_SPECIAL_CHARACTERS.indexOf(character) >= 0) {
        escaped.append('\\');
      }
      escaped.append(character);
    }
    return escaped.toString();
  }

  /** Undoes {@link #escapeRegex}: drops the backslash in front of a regex metacharacter. */
  static String unescapeRegex(String escapedText) {
    StringBuilder text = new StringBuilder(escapedText.length());
    for (int index = 0; index < escapedText.length(); index++) {
      char character = escapedText.charAt(index);
      if (character == '\\' && index + 1 < escapedText.length()
          && REGEX_SPECIAL_CHARACTERS.indexOf(escapedText.charAt(index + 1)) >= 0) {
        index++;
        character = escapedText.charAt(index);
      }
      text.append(character);
    }
    return text.toString();
  }
}
