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
 * <p>Only <em>plain</em> trees are editable: blocks that contain nothing but text runs and field or group references.
 * A tree with links or line breaks is shown ({@link #getText}) but {@link #setText} refuses it, since a flat text could
 * not keep those nodes. A reference shows in the text as SME's editor labels it, {@code [path]} for a field and {@code
 * IndexOf(path)} for a group, and is one unit: deleting all of its label removes it, changing part of it turns it into
 * plain text (the words were edited), and {@link #insertReference} adds one at a position of the text. An edit changes as
 * little as possible: blocks whose text is unchanged stay untouched, and within a changed block only the differing
 * characters move, so the run formatting (bold, size, color) around them survives. The {@code html} is regenerated in
 * the shape Lexical exports.
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
          if (!(child instanceof Map<?, ?> run) || !(isTextRun(run) || isReference(run))) {
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
    if (isReference(map)) {
      return referenceLabel(map);
    }
    return "";
  }

  private static boolean isTextRun(Map<?, ?> run) {
    return "text".equals(run.get("type")) && run.get("text") instanceof String;
  }

  private static boolean isReference(Map<?, ?> run) {
    return ContentNodes.LEXICAL_FIELD_REFERENCE.equals(run.get("type"));
  }

  /** How the editor shows a reference: {@code [path]} for a field, {@code IndexOf(path)} for a group. */
  public static @NonNull String referenceLabel(@NonNull String path, boolean group) {
    return group ? "IndexOf(" + path + ")" : "[" + path + "]";
  }

  private static String referenceLabel(Map<?, ?> reference) {
    return referenceLabel(reference.get("fieldPath") instanceof String path ? path : "", Boolean.TRUE.equals(reference.get("isGroup")));
  }

  /** The text a run stands for in the flat text: its words, or a reference's label. */
  private static String unitText(Map<String, Object> run) {
    return isReference(run) ? referenceLabel(run) : (String) run.get("text");
  }

  private static String blockText(Map<String, Object> block) {
    StringBuilder builder = new StringBuilder();
    runs(block).forEach(run -> builder.append(unitText(run)));
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
    List<Object> children = new ArrayList<>();
    if (!line.isEmpty()) {
      // The formatting of the template's first run of words; a reference is never carried over into a new block.
      Map<String, Object> run = runs(block).stream().filter(candidate -> !isReference(candidate)).findFirst()
          .orElseGet(() -> newRun(block, line));
      run.put("text", line);
      children.add(run);
    }
    block.put("children", children);
    return block;
  }

  /**
   * Changes one block's text from {@code oldText} to {@code newText}. The block's references are anchors: a reference
   * whose label is still in the new text (in the same order) stays as it is, and the words between anchors are edited
   * one stretch at a time. A reference whose label is not there any more - deleted, or changed - is turned into plain
   * words first, so what is left of it is edited like any other text.
   */
  private static void editBlock(Map<String, Object> block, String oldText, String newText) {
    List<Map<String, Object>> runs = runs(block);
    List<Object> result = new ArrayList<>();
    List<Map<String, Object>> stretch = new ArrayList<>();
    Map<String, Object> lastRemoved = null;
    int cursor = 0;
    for (Map<String, Object> run : runs) {
      if (isReference(run)) {
        String label = unitText(run);
        int found = newText.indexOf(label, cursor);
        if (found >= 0) {
          Edited edited = editWords(block, stretch, newText.substring(cursor, found));
          result.addAll(edited.kept());
          lastRemoved = edited.lastRemoved() != null ? edited.lastRemoved() : lastRemoved;
          stretch = new ArrayList<>();
          result.add(run);
          cursor = found + label.length();
          continue;
        }
        run.put("text", label);
        run.put("type", "text");
        run.keySet().removeAll(List.of("fieldId", "fieldPath", "isGroup", "displayOption", "missingValueText"));
      }
      stretch.add(run);
    }
    Edited edited = editWords(block, stretch, newText.substring(cursor));
    result.addAll(edited.kept());
    lastRemoved = edited.lastRemoved() != null ? edited.lastRemoved() : lastRemoved;
    if (result.isEmpty() && lastRemoved != null) {
      // Like Lexical: an emptied block remembers the formatting the next typed text should get.
      block.put("textFormat", lastRemoved.getOrDefault("format", 0));
      block.put("textStyle", lastRemoved.getOrDefault("style", ""));
    }
    block.put("children", result);
  }

  /** The runs left of an edit, and the last run that was emptied by it (for the formatting to remember). */
  private record Edited(List<Object> kept, Map<String, Object> lastRemoved) {
  }

  /**
   * Turns the text runs {@code runs} (which hold {@code oldText} together) into {@code newText} by replacing only the
   * differing middle: the characters between the common prefix and suffix are removed from whichever runs hold them and
   * the new ones go into the run at the change (the earlier run when it sits on a boundary, so typing continues its
   * formatting). Words with no run to go into get one of their own.
   */
  private static Edited editWords(Map<String, Object> block, List<Map<String, Object>> runs, String newText) {
    String oldText = runs.stream().map(run -> (String) run.get("text")).reduce("", String::concat);
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
    return new Edited(kept, lastRemoved);
  }

  /** How a field reference in a text is shown: its label, {@code value-only} or {@code label-value}, and the text for no value. */
  public record ReferenceOptions(String label, String displayOption, String missingValueText) {
  }

  /** The value-only display of a field reference: just the value of the field. */
  public static final String VALUE_ONLY = "value-only";

  /** The display of a field reference with the label of the field before its value. */
  public static final String LABEL_VALUE = "label-value";

  /** The options of the field references (group references have none) of an {@linkplain #isEditable editable} text, in order. */
  public static @NonNull List<ReferenceOptions> fieldReferenceOptions(@NonNull ContentElement element) {
    List<ReferenceOptions> result = new ArrayList<>();
    for (Map<String, Object> reference : fieldReferences(element)) {
      result.add(new ReferenceOptions(referenceLabel(reference),
          reference.get("displayOption") instanceof String option ? option : VALUE_ONLY,
          reference.get("missingValueText") instanceof String missing ? missing : ""));
    }
    return result;
  }

  /**
   * Sets how the {@code index}-th field reference (counted like {@link #fieldReferenceOptions}) is shown; an empty
   * {@code missingValueText} removes it. Returns {@code false}, changing nothing, when there is no such reference.
   */
  public static boolean setReferenceOptions(@NonNull ContentElement element, int index, @NonNull String displayOption,
      @NonNull String missingValueText) {
    List<Map<String, Object>> references = fieldReferences(element);
    if (index < 0 || index >= references.size()) {
      return false;
    }
    Map<String, Object> reference = references.get(index);
    reference.put("displayOption", displayOption);
    if (missingValueText.isEmpty()) {
      reference.remove("missingValueText");
    }
    else {
      reference.put("missingValueText", missingValueText);
    }
    element.getProps().put("html", toHtml(blocks(element)));
    return true;
  }

  /** Whether the text of the element holds a field or group reference. */
  public static boolean hasReferences(@NonNull ContentElement element) {
    List<Object> blocks = blocks(element);
    if (blocks != null) {
      for (Object block : blocks) {
        if (runs(asMap(block)).stream().anyMatch(LexicalText::isReference)) {
          return true;
        }
      }
    }
    return false;
  }

  private static List<Map<String, Object>> fieldReferences(ContentElement element) {
    List<Map<String, Object>> result = new ArrayList<>();
    List<Object> blocks = isEditable(element) ? blocks(element) : null;
    if (blocks != null) {
      for (Object block : blocks) {
        for (Map<String, Object> run : runs(asMap(block))) {
          if (isReference(run) && !Boolean.TRUE.equals(run.get("isGroup"))) {
            result.add(run);
          }
        }
      }
    }
    return result;
  }

  /**
   * Inserts a reference to the field ({@code group == false}) or group with id {@code fieldId} into the text at {@code
   * offset} of the flat text {@link #getText} gives (a newline counts as one character; out of range is clamped). The text
   * run there is split; the reference takes the formatting of what precedes it. Returns {@code false}, changing
   * nothing, when the element is not {@linkplain #isEditable editable}.
   *
   * @param fieldPath the path shown in the reference's label
   */
  @SuppressWarnings("unchecked")
  public static boolean insertReference(@NonNull ContentElement element, int offset, @NonNull String fieldId,
      @NonNull String fieldPath, boolean group) {
    if (!isEditable(element)) {
      return false;
    }
    List<Object> blocks = blocks(element);
    if (blocks == null || blocks.isEmpty()) {
      blocks = createEmptyTree(element);
    }
    int remaining = Math.max(0, offset);
    Map<String, Object> target = asMap(blocks.get(blocks.size() - 1));
    for (int i = 0; i < blocks.size(); i++) {
      Map<String, Object> block = asMap(blocks.get(i));
      int length = blockText(block).length();
      if (remaining <= length || i == blocks.size() - 1) {
        target = block;
        remaining = Math.min(remaining, length);
        break;
      }
      remaining -= length + 1;
    }
    if (!(target.get("children") instanceof List<?>)) {
      target.put("children", new ArrayList<>());
    }
    List<Object> children = (List<Object>) target.get("children");

    int index = children.size();
    int position = 0;
    for (int i = 0; i < children.size(); i++) {
      Map<String, Object> run = asMap(children.get(i));
      int length = unitText(run).length();
      if (remaining == position) {
        index = i;
        break;
      }
      if (remaining > position && remaining < position + length) {
        if (isReference(run)) {
          index = i + 1;
        }
        else {
          String text = (String) run.get("text");
          Map<String, Object> after = copy(run);
          run.put("text", text.substring(0, remaining - position));
          after.put("text", text.substring(remaining - position));
          children.add(i + 1, after);
          index = i + 1;
        }
        break;
      }
      position += length;
    }

    Map<String, Object> before = index > 0 ? asMap(children.get(index - 1)) : null;
    Map<String, Object> node = new LinkedHashMap<>();
    node.put("detail", 0);
    node.put("format", before != null ? before.getOrDefault("format", 0) : target.getOrDefault("textFormat", 0));
    node.put("mode", "normal");
    node.put("style", before != null ? before.getOrDefault("style", "") : target.getOrDefault("textStyle", ""));
    node.put("text", "");
    node.put("type", ContentNodes.LEXICAL_FIELD_REFERENCE);
    node.put("version", 1);
    node.put("fieldId", fieldId);
    node.put("fieldPath", fieldPath);
    if (group) {
      node.put("isGroup", true);
    }
    else {
      node.put("displayOption", VALUE_ONLY);
    }
    children.add(index, node);
    element.getProps().put("html", toHtml(blocks));
    return true;
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
    boolean reference = isReference(run);
    if (reference) {
      classes.add("ce-lexical-element");
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
    if (reference) {
      html.append(" data-ce-field-ref-id=\"").append(escape(String.valueOf(run.get("fieldId")))).append("\" contenteditable=\"false\"")
          .append(" data-ce-field-ref-path=\"").append(escape(String.valueOf(run.getOrDefault("fieldPath", "")))).append('"');
      if (run.get("displayOption") instanceof String option) {
        html.append(" data-ce-field-ref-display-option=\"").append(escape(option)).append('"');
      }
      if (run.get("missingValueText") instanceof String missing) {
        html.append(" data-ce-field-ref-missing-value-text=\"").append(escape(missing)).append('"');
      }
      if (Boolean.TRUE.equals(run.get("isGroup"))) {
        html.append(" data-ce-field-ref-is-group=\"true\"");
      }
    }
    html.append(" style=\"").append(escape(style.toString())).append("\">")
        .append(escape(unitText(run))).append("</").append(tag).append('>');

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
