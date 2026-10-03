package de.a12.studio.models.structuralmappingmodel;

import java.util.ArrayList;
import java.util.List;

/**
 * Helpers for the full names a Structural Mapping Model refers to elements with: {@code /Person/Addresses/City},
 * the element names from the root group down, separated by slashes (SME's {@code ModelPath}).
 */
public final class SmmPath {

  private SmmPath() {
  }

  /** {@code /A/B} to {@code [A, B]}; {@code null}, empty and {@code /} to the empty list. */
  public static List<String> parse(String path) {
    List<String> segments = new ArrayList<>();
    if (path != null) {
      for (String segment : path.split("/")) {
        if (!segment.isEmpty()) {
          segments.add(segment);
        }
      }
    }
    return segments;
  }

  public static String format(List<String> segments) {
    return segments.isEmpty() ? "" : "/" + String.join("/", segments);
  }

  /** Whether {@code path} lies strictly below {@code ancestor}: {@code isBelow(/a/b/c, /a/b)} but not {@code isBelow(/a/b, /a/b)}. */
  public static boolean isBelow(List<String> path, List<String> ancestor) {
    return path.size() > ancestor.size() && path.subList(0, ancestor.size()).equals(ancestor);
  }

  /** Whether {@code path} is {@code ancestor} or lies below it. */
  public static boolean isSameOrBelow(List<String> path, List<String> ancestor) {
    return path.size() >= ancestor.size() && path.subList(0, ancestor.size()).equals(ancestor);
  }

  /** The proper ancestors of {@code path}, longest first, without the empty root: {@code /a/b/c} gives {@code /a/b}, {@code /a}. */
  public static List<List<String>> ancestors(List<String> path) {
    List<List<String>> ancestors = new ArrayList<>();
    for (int length = path.size() - 1; length >= 1; length--) {
      ancestors.add(path.subList(0, length));
    }
    return ancestors;
  }

  /**
   * {@code path} relative to {@code parent}, the way SME labels a mapping tag: {@code .} if both are the same,
   * the remainder if {@code path} lies below {@code parent}, else the full path.
   */
  public static String relativeTo(List<String> parent, List<String> path) {
    if (parent.equals(path)) {
      return ".";
    }
    if (isBelow(path, parent)) {
      String remainder = format(path.subList(parent.size(), path.size()));
      return parent.isEmpty() ? remainder : remainder.substring(1);
    }
    return format(path);
  }
}
