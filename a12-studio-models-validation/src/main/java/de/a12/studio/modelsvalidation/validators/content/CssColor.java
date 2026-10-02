package de.a12.studio.modelsvalidation.validators.content;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Port of the {@code color-string} 1.9.1 parser ({@code colorString.get}) that the Content Engine editor's color
 * controller uses to read a stored color ({@code contentengine-editor} {@code color-controller.ts}); a value it
 * returns {@code null} for makes SME's converter throw. Case-sensitivity and the exact grammars are kept as in the
 * original (e.g. {@code RGB(1,2,3)} and {@code Red} are invalid, hex digits are case-insensitive).
 */
final class CssColor {

  private static final Pattern HEX = Pattern.compile("^#([a-f0-9]{6})([a-f0-9]{2})?$", Pattern.CASE_INSENSITIVE);
  private static final Pattern ABBR = Pattern.compile("^#([a-f0-9]{3,4})$", Pattern.CASE_INSENSITIVE);
  private static final Pattern RGBA = Pattern.compile(
      "^rgba?\\(\\s*([+-]?\\d+)(?=[\\s,])\\s*(?:,\\s*)?([+-]?\\d+)(?=[\\s,])\\s*(?:,\\s*)?([+-]?\\d+)\\s*"
          + "(?:[,|/]\\s*([+-]?[\\d.]+)(%?)\\s*)?\\)$");
  private static final Pattern PERCENT = Pattern.compile(
      "^rgba?\\(\\s*([+-]?[\\d.]+)%\\s*,?\\s*([+-]?[\\d.]+)%\\s*,?\\s*([+-]?[\\d.]+)%\\s*"
          + "(?:[,|/]\\s*([+-]?[\\d.]+)(%?)\\s*)?\\)$");
  private static final Pattern KEYWORD = Pattern.compile("^(\\w+)$");
  private static final Pattern HSL = Pattern.compile(
      "^hsla?\\(\\s*([+-]?(?:\\d{0,3}\\.)?\\d+)(?:deg)?\\s*,?\\s*([+-]?[\\d.]+)%\\s*,?\\s*([+-]?[\\d.]+)%\\s*"
          + "(?:[,|/]\\s*([+-]?(?=\\.\\d|\\d)(?:0|[1-9]\\d*)?(?:\\.\\d*)?(?:[eE][+-]?\\d+)?)\\s*)?\\)$");
  private static final Pattern HWB = Pattern.compile(
      "^hwb\\(\\s*([+-]?\\d{0,3}(?:\\.\\d+)?)(?:deg)?\\s*,\\s*([+-]?[\\d.]+)%\\s*,\\s*([+-]?[\\d.]+)%\\s*"
          + "(?:,\\s*([+-]?(?=\\.\\d|\\d)(?:0|[1-9]\\d*)?(?:\\.\\d*)?(?:[eE][+-]?\\d+)?)\\s*)?\\)$");

  /** The CSS keyword colors of the {@code color-name} package (plus {@code transparent}, handled separately). */
  private static final Set<String> NAMES = Set.of(
      "aliceblue", "antiquewhite", "aqua", "aquamarine", "azure", "beige", "bisque", "black", "blanchedalmond",
      "blue", "blueviolet", "brown", "burlywood", "cadetblue", "chartreuse", "chocolate", "coral", "cornflowerblue",
      "cornsilk", "crimson", "cyan", "darkblue", "darkcyan", "darkgoldenrod", "darkgray", "darkgreen", "darkgrey",
      "darkkhaki", "darkmagenta", "darkolivegreen", "darkorange", "darkorchid", "darkred", "darksalmon",
      "darkseagreen", "darkslateblue", "darkslategray", "darkslategrey", "darkturquoise", "darkviolet", "deeppink",
      "deepskyblue", "dimgray", "dimgrey", "dodgerblue", "firebrick", "floralwhite", "forestgreen", "fuchsia",
      "gainsboro", "ghostwhite", "gold", "goldenrod", "gray", "green", "greenyellow", "grey", "honeydew", "hotpink",
      "indianred", "indigo", "ivory", "khaki", "lavender", "lavenderblush", "lawngreen", "lemonchiffon", "lightblue",
      "lightcoral", "lightcyan", "lightgoldenrodyellow", "lightgray", "lightgreen", "lightgrey", "lightpink",
      "lightsalmon", "lightseagreen", "lightskyblue", "lightslategray", "lightslategrey", "lightsteelblue",
      "lightyellow", "lime", "limegreen", "linen", "magenta", "maroon", "mediumaquamarine", "mediumblue",
      "mediumorchid", "mediumpurple", "mediumseagreen", "mediumslateblue", "mediumspringgreen", "mediumturquoise",
      "mediumvioletred", "midnightblue", "mintcream", "mistyrose", "moccasin", "navajowhite", "navy", "oldlace",
      "olive", "olivedrab", "orange", "orangered", "orchid", "palegoldenrod", "palegreen", "paleturquoise",
      "palevioletred", "papayawhip", "peachpuff", "peru", "pink", "plum", "powderblue", "purple", "rebeccapurple",
      "red", "rosybrown", "royalblue", "saddlebrown", "salmon", "sandybrown", "seagreen", "seashell", "sienna",
      "silver", "skyblue", "slateblue", "slategray", "slategrey", "snow", "springgreen", "steelblue", "tan", "teal",
      "thistle", "tomato", "turquoise", "violet", "wheat", "white", "whitesmoke", "yellow", "yellowgreen");

  private CssColor() {
  }

  /**
   * Whether SME's color controller can read {@code value}: an empty value is "no color", anything else must be
   * understood by {@code color-string}.
   */
  static boolean isValid(String value) {
    if (value.isEmpty()) {
      return true;
    }
    String prefix = value.substring(0, Math.min(3, value.length())).toLowerCase(java.util.Locale.ROOT);
    return switch (prefix) {
      case "hsl" -> HSL.matcher(value).matches();
      case "hwb" -> HWB.matcher(value).matches();
      default -> isValidRgb(value);
    };
  }

  private static boolean isValidRgb(String value) {
    if (HEX.matcher(value).matches() || ABBR.matcher(value).matches() || RGBA.matcher(value).matches()) {
      return true;
    }
    if (PERCENT.matcher(value).matches()) {
      return true;
    }
    Matcher keyword = KEYWORD.matcher(value);
    return keyword.matches() && ("transparent".equals(value) || NAMES.contains(value));
  }
}
