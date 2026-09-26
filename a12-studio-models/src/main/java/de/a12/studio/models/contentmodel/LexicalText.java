package de.a12.studio.models.contentmodel;

import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The plain text of a Paragraph or Heading, whose {@code props} hold a Lexical editor state ({@code tree}) next to
 * its HTML export ({@code html}). SME edits it rich on its canvas; this lets a property field edit just the words.
 * Every block (paragraph/heading) of the tree is one line of the text.
 *
 * <p>Only <em>plain</em> trees are editable: blocks that contain nothing but text runs. A tree with links, field
 * references or line breaks is shown ({@link #getText}) but {@link #setText} refuses it, since a flat text could not
 * keep those nodes. An edit changes as little as possible: blocks whose text is unchanged stay untouched, and within
 * a changed block only the differing characters move, so the run formatting (bold, size, color) around them survives.
 * The {@code html} is regenerated in the shape Lexical exports.
 */
public final class LexicalText {

  private static final int BOLD = 1;
  private static final int ITALIC = 2;
  private static final int STRIKETHROUGH = 4;
  private static final int UNDERLINE = 8;
  private static final int CODE = 16;
  private static final int SUBSCRIPT = 32;
  private static final int SUPERSCRIPT = 64;

  private LexicalText() {
  }

  /** Whether {@code element} is a type whose text lives in a Lexical tree. */
  public static boolean supports(@NonNull ContentElement element) {
    return "Paragraph".equals(element.getType()) || "Heading".equals(element.getType());
  }

  /**
   * Whether {@link #setText} can edit the element: it is a {@linkplain #supports supported} type and its tree, if
   * it has one, has only plain blocks.
   */
  public static boolean isEditable(@NonNull ContentElement element) {
    if (!supports(element)) {
      return false;
    }
    List<Object> blocks = blocks(element);
    return blocks == null || isPlain(blocks);
  }

  /** The text of the element, blocks separated by a newline; links and field references show as their content. */
  public static @NonNull String getText(@NonNull ContentElement element) {
    List<Object> blocks = blocks(element);
    if (blocks == null) {
      return "";
    }
    List<String> lines = new ArrayList<>();
    for (Object block : blocks) {
      lines.add(displayText(block));
    }
    return String.join("\n", lines);
  }

  /**
   * Replaces the element's text, updating {@code tree} and {@code html}. Returns {@code false}, changing nothing,
   * when the element is not {@linkplain #isEditable editable}. An element without a tree gets a default one first.
   */
  public static boolean setText(@NonNull ContentElement element, @NonNull String text) {
    if (!isEditable(element)) {
      return false;
    }
    List<Object> blocks = blocks(element);
    if (blocks == null || blocks.isEmpty()) {
      blocks = createEmptyTree(element);
    }
    List<String> newLines = List.of(text.replace("\r\n", "\n").replace('\r', '\n').split("\n", -1));
    List<String> oldLines = new ArrayList<>();
    blocks.forEach(block -> oldLines.add(blockText(asMap(block))));
    if (oldLines.equals(newLines)) {
      return true;
    }
    applyLines(blocks, oldLines, newLines);
    element.getProps().put("html", toHtml(blocks));
    return true;
  }

  // ---- reading the tree ----

  private static List<Object> blocks(ContentElement element) {
    Map<String, Object> props = element.getProps();
    if (props == null || !(props.get("tree") instanceof Map<?, ?> tree) || !(tree.get("root") instanceof Map<?, ?> root)
        || !(root.get("children") instanceof List<?> children)) {
      return null;
    }
    @SuppressWarnings("unchecked")
    List<Object> blocks = (List<Object>) children;
    return blocks;
  }

  private static boolean isPlain(List<Object> blocks) {
    for (Object block : blocks) {
      if (!(block instanceof Map<?, ?> map) || !("paragraph".equals(map.get("type")) || "heading".equals(map.get("type")))) {
        return false;
      }
      if (map.get("children") instanceof List<?> children) {
        for (Object child : children) {
          if (!(child instanceof Map<?, ?> run) || !"text".equals(run.get("type")) || !(run.get("text") instanceof String)) {
            return false;
          }
        }
      }
    }
    return true;
  }

  /** Any node's text for display: text runs, field references as {@code [path]}, line breaks as a newline. */
  private static String displayText(Object node) {
    if (!(node instanceof Map<?, ?> map)) {
      return "";
    }
    Object type = map.get("type");
    if ("text".equals(type)) {
      return map.get("text") instanceof String text ? text : "";
    }
    if ("linebreak".equals(type)) {
      return "\n";
    }
    if (map.get("children") instanceof List<?> children) {
      StringBuilder builder = new StringBuilder();
      children.forEach(child -> builder.append(displayText(child)));
      return builder.toString();
    }
    if (map.get("fieldPath") instanceof String path) {
      return "[" + path + "]";
    }
    return "";
  }

  private static String blockText(Map<String, Object> block) {
    StringBuilder builder = new StringBuilder();
    runs(block).forEach(run -> builder.append((String) run.get("text")));
    return builder.toString();
  }

  private static List<Map<String, Object>> runs(Map<String, Object> block) {
    List<Map<String, Object>> runs = new ArrayList<>();
    if (block.get("children") instanceof List<?> children) {
      children.forEach(child -> runs.add(asMap(child)));
    }
    return runs;
  }

  // ---- writing the tree ----

  /** Gives the element an empty tree of its own type (a default one when it has none) and returns its blocks. */
  private static List<Object> createEmptyTree(ContentElement element) {
    Map<String, Object> defaults = ContentElementDefaults.defaultProps(element.getType());
    if (element.getProps() == null) {
      element.setProps(new LinkedHashMap<>());
    }
    element.getProps().put("tree", defaults.get("tree"));
    element.getProps().put("html", defaults.get("html"));
    List<Object> blocks = blocks(element);
    blocks.forEach(block -> asMap(block).put("children", new ArrayList<>()));
    element.getProps().put("html", toHtml(blocks));
    return blocks;
  }

  /** Brings {@code blocks} from {@code oldLines} to {@code newLines}: unchanged head/tail blocks stay, the middle is edited. */
  private static void applyLines(List<Object> blocks, List<String> oldLines, List<String> newLines) {
    int oldCount = oldLines.size();
    int newCount = newLines.size();
    int head = 0;
    while (head < oldCount && head < newCount && oldLines.get(head).equals(newLines.get(head))) {
      head++;
    }
    int tail = 0;
    while (tail < oldCount - head && tail < newCount - head
        && oldLines.get(oldCount - 1 - tail).equals(newLines.get(newCount - 1 - tail))) {
      tail++;
    }
    int removed = oldCount - head - tail;
    int added = newCount - head - tail;
    int paired = Math.min(removed, added);
    Map<String, Object> template = copy(asMap(blocks.get(Math.max(0, Math.min(oldCount - 1, head + paired - 1)))));

    for (int i = 0; i < paired; i++) {
      editBlock(asMap(blocks.get(head + i)), oldLines.get(head + i), newLines.get(head + i));
    }
    for (int i = paired; i < removed; i++) {
      blocks.remove(head + paired);
    }
    for (int i = paired; i < added; i++) {
      blocks.add(head + i, newBlock(template, newLines.get(head + i)));
    }
  }

  /** A new block shaped like {@code template}, holding {@code line} in the template's first run's formatting. */
  private static Map<String, Object> newBlock(Map<String, Object> template, String line) {
    Map<String, Object> block = copy(template);
    List<Map<String, Object>> runs = runs(block);
    List<Object> children = new ArrayList<>();
    if (!line.isEmpty()) {
      Map<String, Object> run = runs.isEmpty() ? newRun(block, line) : runs.get(0);
      run.put("text", line);
      children.add(run);
    }
    block.put("children", children);
    return block;
  }

  /**
   * Changes one block's text from {@code oldText} to {@code newText} by replacing only the differing middle: the
   * characters between the common prefix and suffix are removed from whichever runs hold them and the new ones go
   * into the run at the change (the earlier run when it sits on a boundary, so typing continues its formatting).
   */
  private static void editBlock(Map<String, Object> block, String oldText, String newText) {
    List<Map<String, Object>> runs = runs(block);
    int prefix = 0;
    int limit = Math.min(oldText.length(), newText.length());
    while (prefix < limit && oldText.charAt(prefix) == newText.charAt(prefix)) {
      prefix++;
    }
    int suffix = 0;
    while (suffix < limit - prefix
        && oldText.charAt(oldText.length() - 1 - suffix) == newText.charAt(newText.length() - 1 - suffix)) {
      suffix++;
    }
    int deleteEnd = oldText.length() - suffix;
    String inserted = newText.substring(prefix, newText.length() - suffix);

    Map<String, Object> insertInto = null;
    int position = 0;
    for (Map<String, Object> run : runs) {
      int length = ((String) run.get("text")).length();
      if (insertInto == null && position < prefix && prefix <= position + length) {
        insertInto = run;
      }
      position += length;
    }
    if (insertInto == null && !runs.isEmpty()) {
      insertInto = runs.get(0);
    }

    List<Object> kept = new ArrayList<>();
    Map<String, Object> lastRemoved = null;
    position = 0;
    for (Map<String, Object> run : runs) {
      String text = (String) run.get("text");
      int from = clamp(prefix - position, text.length());
      int to = clamp(deleteEnd - position, text.length());
      String result = text.substring(0, from) + (run == insertInto ? inserted : "") + text.substring(to);
      position += text.length();
      if (result.isEmpty()) {
        lastRemoved = run;
      }
      else {
        run.put("text", result);
        kept.add(run);
      }
    }
    if (kept.isEmpty() && !newText.isEmpty()) {
      kept.add(newRun(block, newText));
    }
    if (kept.isEmpty() && lastRemoved != null) {
      // Like Lexical: an emptied block remembers the formatting the next typed text should get.
      block.put("textFormat", lastRemoved.getOrDefault("format", 0));
      block.put("textStyle", lastRemoved.getOrDefault("style", ""));
    }
    block.put("children", kept);
  }

  private static int clamp(int value, int max) {
    return Math.max(0, Math.min(value, max));
  }

  private static Map<String, Object> newRun(Map<String, Object> block, String text) {
    Map<String, Object> run = new LinkedHashMap<>();
    run.put("detail", 0);
    run.put("format", block.getOrDefault("textFormat", 0));
    run.put("mode", "normal");
    run.put("style", block.getOrDefault("textStyle", ""));
    run.put("text", text);
    run.put("type", "text");
    run.put("version", 1);
    return run;
  }

  // ---- HTML export ----

  private static String toHtml(List<Object> blocks) {
    StringBuilder html = new StringBuilder();
    for (Object node : blocks) {
      Map<String, Object> block = asMap(node);
      String tag = "heading".equals(block.get("type")) && block.get("tag") instanceof String heading ? heading : "p";
      String cssClass = "p".equals(tag) ? "editor-paragraph" : "editor-heading-" + tag;
      html.append('<').append(tag).append(" class=\"").append(cssClass).append('"');
      if (block.get("direction") instanceof String direction && !direction.isEmpty()) {
        html.append(" dir=\"").append(direction).append('"');
      }
      if (block.get("format") instanceof String format && !format.isEmpty()) {
        html.append(" style=\"text-align: ").append(format).append(";\"");
      }
      html.append('>');
      List<Map<String, Object>> runs = runs(block);
      if (runs.isEmpty()) {
        html.append("<br>");
      }
      runs.forEach(run -> html.append(runHtml(run)));
      html.append("</").append(tag).append('>');
    }
    return html.toString();
  }

  private static String runHtml(Map<String, Object> run) {
    int format = run.get("format") instanceof Number number ? number.intValue() : 0;
    String tag = (format & CODE) != 0 ? "code" : (format & SUBSCRIPT) != 0 ? "sub" : (format & SUPERSCRIPT) != 0 ? "sup"
        : (format & BOLD) != 0 ? "strong" : (format & ITALIC) != 0 ? "em" : "span";
    List<String> classes = new ArrayList<>();
    if ((format & BOLD) != 0) {
      classes.add("editor-text-bold");
    }
    if ((format & ITALIC) != 0) {
      classes.add("editor-text-italic");
    }
    if ((format & UNDERLINE) != 0 && (format & STRIKETHROUGH) != 0) {
      classes.add("editor-text-underlineStrikethrough");
    }
    else if ((format & UNDERLINE) != 0) {
      classes.add("editor-text-underline");
    }
    else if ((format & STRIKETHROUGH) != 0) {
      classes.add("editor-text-strikethrough");
    }
    if ((format & CODE) != 0) {
      classes.add("editor-text-code");
    }

    StringBuilder style = new StringBuilder();
    if (run.get("style") instanceof String css) {
      for (String declaration : css.split(";")) {
        if (!declaration.isBlank()) {
          style.append(declaration.strip()).append("; ");
        }
      }
    }
    style.append("white-space: pre-wrap;");

    StringBuilder html = new StringBuilder();
    html.append('<').append(tag);
    if (!classes.isEmpty()) {
      html.append(" class=\"").append(String.join(" ", classes)).append('"');
    }
    html.append(" style=\"").append(escape(style.toString())).append("\">")
        .append(escape((String) run.get("text"))).append("</").append(tag).append('>');

    String result = html.toString();
    // Lexical wraps the styled element in <b>, <i>, <s>, <u>, innermost first.
    if ((format & BOLD) != 0) {
      result = "<b>" + result + "</b>";
    }
    if ((format & ITALIC) != 0) {
      result = "<i>" + result + "</i>";
    }
    if ((format & STRIKETHROUGH) != 0) {
      result = "<s>" + result + "</s>";
    }
    if ((format & UNDERLINE) != 0) {
      result = "<u>" + result + "</u>";
    }
    return result;
  }

  private static String escape(String text) {
    return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
  }

  // ---- helpers ----

  @SuppressWarnings("unchecked")
  private static Map<String, Object> asMap(Object node) {
    return (Map<String, Object>) node;
  }

  /** A deep copy of a JSON-shaped tree (maps, lists, scalars). */
  private static Map<String, Object> copy(Map<String, Object> source) {
    Map<String, Object> result = new LinkedHashMap<>();
    source.forEach((key, value) -> result.put(key, copyValue(value)));
    return result;
  }

  @SuppressWarnings("unchecked")
  private static Object copyValue(Object value) {
    if (value instanceof Map<?, ?> map) {
      return copy((Map<String, Object>) map);
    }
    if (value instanceof List<?> list) {
      List<Object> result = new ArrayList<>();
      list.forEach(item -> result.add(copyValue(item)));
      return result;
    }
    return value;
  }
}
