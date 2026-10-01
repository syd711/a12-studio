package de.a12.studio.modelsvalidation.validators.query;

/** SME's {@code string-utils.ts}. */
final class QlStrings {

  private QlStrings() {
  }

  static String quoted(String value) {
    return "\"" + value + "\"";
  }

  static String bracketed(String value) {
    return "[" + value + "]";
  }

  /** Plain Levenshtein distance over UTF-16 units. */
  static int levenshtein(String a, String b) {
    int[] previous = new int[b.length() + 1];
    int[] current = new int[b.length() + 1];
    for (int j = 0; j <= b.length(); j++) {
      previous[j] = j;
    }
    for (int i = 1; i <= a.length(); i++) {
      current[0] = i;
      for (int j = 1; j <= b.length(); j++) {
        int cost = a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1;
        current[j] = Math.min(Math.min(current[j - 1] + 1, previous[j] + 1), previous[j - 1] + cost);
      }
      int[] swap = previous;
      previous = current;
      current = swap;
    }
    return previous[b.length()];
  }
}
