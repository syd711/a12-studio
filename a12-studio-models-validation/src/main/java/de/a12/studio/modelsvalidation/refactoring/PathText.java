package de.a12.studio.modelsvalidation.refactoring;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * The text form of an element path as it appears in a Rule/Computation condition, an error-message parameter or a
 * relative-path property such as {@code errorEntityRelPath}: an optional leading {@code /} (absolute path), then
 * {@code /}-separated segments where a segment is either an upward reference ({@code ..}, optionally directly
 * followed by the name of the group it lands on - the kernel doc's "turning group", e.g. {@code ..Order}) or a
 * (possibly single-quoted) element name with an optional trailing {@code *} ("all repetitions").
 *
 * <p>Deliberately independent of the ANTLR grammar: {@link ConditionPathLocator} cuts the path text out of a
 * condition and this class only interprets that substring, so the same code also serves the properties whose whole
 * value is one path.
 */
final class PathText {

  private static final Pattern PLAIN_NAME = Pattern.compile("[A-Za-z_][A-Za-z0-9_]*");

  // RuleLang.g4 keywords that lex as a keyword token instead of IDENT, so a Field/Group named like one has to be
  // written in single quotes. Lower-cased; the case-insensitive ones are matched via toLowerCase(), which also
  // (harmlessly) quotes e.g. "today" - the grammar only recognises the exact-case forms, and quoting is always valid.
  private static final Set<String> KEYWORDS = Set.of("and", "or", "having", "for", "in", "today", "now", "true", "false");

  /**
   * One {@code /}-separated part of a path. For an upward segment {@code name} is the optional turning-group name
   * (never a child to descend into); for a downward segment it is the element name without quotes.
   */
  record Segment(boolean up, String name, boolean quoted, boolean star) {

    static Segment up(String turningGroupName) {
      return new Segment(true, turningGroupName, false, false);
    }

    static Segment down(String name, boolean quoted, boolean star) {
      return new Segment(false, name, quoted, star);
    }

    /** The segment as it is written in a path. */
    String text() {
      if (up) {
        return name == null ? ".." : ".." + quoteIfNeeded(name, false);
      }
      return quoteIfNeeded(name, quoted) + (star ? "*" : "");
    }
  }

  private final boolean absolute;
  private final List<Segment> segments;

  private PathText(boolean absolute, List<Segment> segments) {
    this.absolute = absolute;
    this.segments = segments;
  }

  boolean absolute() {
    return absolute;
  }

  List<Segment> segments() {
    return segments;
  }

  /** Empty for anything that isn't a well-formed path (empty segments, a starred {@code ..}, a stray {@code /}). */
  static Optional<PathText> parse(String text) {
    if (text == null) {
      return Optional.empty();
    }
    String trimmed = text.strip();
    boolean absolute = trimmed.startsWith("/");
    String body = absolute ? trimmed.substring(1) : trimmed;
    if (body.isEmpty()) {
      return Optional.empty();
    }
    List<Segment> segments = new ArrayList<>();
    for (String part : body.split("/", -1)) {
      Optional<Segment> segment = parseSegment(part.strip());
      if (segment.isEmpty()) {
        return Optional.empty();
      }
      segments.add(segment.get());
    }
    return Optional.of(new PathText(absolute, segments));
  }

  private static Optional<Segment> parseSegment(String part) {
    if (part.isEmpty() || part.equals("*")) {
      return Optional.empty();
    }
    if (part.startsWith("..")) {
      String turningGroup = part.substring(2);
      if (turningGroup.endsWith("*")) {
        return Optional.empty();
      }
      if (turningGroup.isEmpty()) {
        return Optional.of(Segment.up(null));
      }
      return unquote(turningGroup).map(name -> Segment.up(name));
    }
    boolean star = part.endsWith("*");
    String name = star ? part.substring(0, part.length() - 1) : part;
    boolean quoted = name.length() >= 2 && name.startsWith("'") && name.endsWith("'");
    String plain = quoted ? name.substring(1, name.length() - 1) : name;
    if (plain.isEmpty() || plain.contains("'")) {
      return Optional.empty();
    }
    return Optional.of(Segment.down(plain, quoted, star));
  }

  private static Optional<String> unquote(String name) {
    boolean quoted = name.length() >= 2 && name.startsWith("'") && name.endsWith("'");
    String plain = quoted ? name.substring(1, name.length() - 1) : name;
    return plain.isEmpty() || plain.contains("'") ? Optional.empty() : Optional.of(plain);
  }

  /** {@code name}, in single quotes if it was written that way or could not be lexed as a plain identifier. */
  static String quoteIfNeeded(String name, boolean alreadyQuoted) {
    if (alreadyQuoted || !PLAIN_NAME.matcher(name).matches() || KEYWORDS.contains(name.toLowerCase())) {
      return "'" + name + "'";
    }
    return name;
  }
}
