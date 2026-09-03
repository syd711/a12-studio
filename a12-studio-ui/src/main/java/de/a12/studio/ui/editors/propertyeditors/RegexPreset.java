package de.a12.studio.ui.editors.propertyeditors;

import java.util.List;

/**
 * Predefined regex patterns offered as quick-pick options in the pattern combo box on the string
 * data type configuration panel. Each entry carries a short human-readable description shown in
 * the combo box cell alongside the expression itself.
 *
 * <p>Sources: numbers 1–5, 8–9 from
 * <a href="https://albertcolom.com/posts/top-10-most-used-regex-patterns/">albertcolom.com</a>
 * (6/7/10 excluded); numbers/letters/letters-only added locally.
 */
public record RegexPreset(String description, String pattern) {

  /** Formats the entry for display in the editable combo box. */
  @Override
  public String toString() {
    return description + "  —  " + pattern;
  }

  public static final List<RegexPreset> ALL = List.of(
      // ── Custom additions ───────────────────────────────────────────────────────
      new RegexPreset("Numbers only",                       "^[0-9]+$"),
      new RegexPreset("Letters only",                       "^[a-zA-Z]+$"),
      new RegexPreset("Letters and numbers only",           "^[a-zA-Z0-9]+$"),

      // ── From albertcolom.com (1–5, 8–9) ───────────────────────────────────────
      new RegexPreset("Email address",                      "^([a-zA-Z0-9._%-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6})*$"),
      new RegexPreset("URL (http / https)",                 "^(https?:\\/\\/)?([a-z0-9]+([\\-\\.]{1}[a-z0-9]+)*\\.[a-z]{2,5})(:[0-9]{1,5})?(\\/.*)? $"),
      new RegexPreset("Date YYYY-MM-DD",                   "^([12]\\d{3}-(0[1-9]|1[0-2])-(0[1-9]|[12]\\d|3[01]))$"),
      new RegexPreset("Date DD-MM-YYYY",                   "^((0[1-9]|[12]\\d|3[01])-(0[1-9]|1[0-2])-[12]\\d{3})$"),
      new RegexPreset("Time HH:mm AM/PM",                  "^(1[0-2]|0?[1-9]):[0-5][0-9] (AM|PM)$"),
      new RegexPreset("Time hh:mm:ss",                     "^(0[0-9]|1[0-9]|2[1-4]):(0[0-9]|[1-5][0-9]):(0[0-9]|[1-5][0-9])$"),
      new RegexPreset("Datetime YYYY-MM-DD hh:mm:ss",      "^([12]\\d{3}-(0[1-9]|1[0-2])-(0[1-9]|[12]\\d|3[01]) (0[0-9]|1[0-9]|2[1-4]):(0[0-9]|[1-5][0-9]):(0[0-9]|[1-5][0-9]))$"),
      new RegexPreset("Datetime DD-MM-YYYY hh:mm:ss",      "^((0[1-9]|[12]\\d|3[01])-(0[1-9]|1[0-2])-[12]\\d{3} (0[0-9]|1[0-9]|2[1-4]):(0[0-9]|[1-5][0-9]):(0[0-9]|[1-5][0-9]))$"),
      new RegexPreset("File: absolute path with extension","^((\\/|\\\\|\\/\\/|https?:\\\\\\\\|https?:\\/\\/)[a-z0-9_@\\-^!#$%&+={}.\\/ \\\\\\[\\]]+)+\\.[a-z]+$"),
      new RegexPreset("File: name with 3-char extension",  "^[\\w,\\s-]+\\.[A-Za-z]{3}$"),
      new RegexPreset("File: image or PDF extension",      "^[\\w,\\s-]+\\.(jpg|jpeg|png|gif|pdf)$"),
      new RegexPreset("Password (complex, ≥8 chars)",      "^(?=(.*[0-9]))(?=.*[\\!@#$%^&*()\\[\\]{}\\-_+=~`|:;\"'<>,.\\/?])(?=.*[a-z])(?=(.*[A-Z]))(?=(.*)).{8,}$"),
      new RegexPreset("Password (moderate, ≥8 chars)",     "^(?=(.*[0-9]))((?=.*[A-Za-z0-9])(?=.*[A-Z])(?=.*[a-z]))^.{8,}$")
  );
}
