package de.a12.studio.modelsvalidation.validators.content;

import de.a12.studio.models.documentmodel.FieldType;
import de.a12.studio.models.documentmodel.StringFieldType;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Map;

/**
 * What each form element of a Content Model (Text Line, Checkbox, Select, ...) can show, as SME's element modules
 * declare it: the Document Model elements whose data type the form element supports, and the data context rule that
 * they may not be repeated more often than the position of the form element. Used by the validator and by the picker
 * of the editor, so both offer and accept exactly the same.
 */
public final class ContentFormElementTypes {

  private static final List<String> BOOLEAN_OR_CONFIRM = List.of("BooleanType", "ConfirmType");
  private static final List<String> BOOLEAN_OR_ENUMERATION = List.of("BooleanType", "EnumerationType");
  private static final List<String> DATES = List.of("DateType", "DateTimeType", "TimeType", "DateFragmentType", "DateRangeType");

  // The field types each form element type can show; the two multi-select elements take a multi-select group instead.
  private static final Map<String, List<String>> FIELD_TYPES = Map.of(
      "TextLine", List.of("StringType", "NumberType", "CustomFieldType"),
      "TextArea", List.of("StringType", "CustomFieldType"),
      "Checkbox", BOOLEAN_OR_CONFIRM,
      "Switch", BOOLEAN_OR_CONFIRM,
      "DatePicker", DATES,
      "Select", BOOLEAN_OR_ENUMERATION,
      "Radio", BOOLEAN_OR_ENUMERATION);

  private static final String AUTO_COMPLETE = "AutoComplete";
  private static final List<String> MULTI_SELECT_GROUPS = List.of("MultiSelect", "CheckboxGroup");

  private ContentFormElementTypes() {
  }

  /** Whether {@code type} is one of the form elements that shows a Document Model element (all but the message ones). */
  public static boolean isInputElement(@Nullable String type) {
    return type != null && (FIELD_TYPES.containsKey(type) || AUTO_COMPLETE.equals(type) || MULTI_SELECT_GROUPS.contains(type));
  }

  /**
   * What a form element of {@code type} cannot show about {@code node}, as the list of what it does support (for a
   * message); {@code null} if it can show it.
   */
  public static @Nullable String unsupported(String type, DocumentStructure.Node node) {
    if (MULTI_SELECT_GROUPS.contains(type)) {
      return node.isMultiSelectGroup() ? null : "multi-select groups";
    }
    FieldType fieldType = node.isField() ? node.fieldType() : null;
    String actual = fieldType != null ? fieldType.getType() : null;
    if (AUTO_COMPLETE.equals(type)) {
      boolean hintList = fieldType instanceof StringFieldType string && string.getStringType() != null
          && string.getStringType().getHintList() != null && !string.getStringType().getHintList().isEmpty();
      return "EnumerationType".equals(actual) || hintList ? null : "EnumerationType, StringType with a hint list";
    }
    List<String> accepted = FIELD_TYPES.get(type);
    return actual != null && accepted.contains(actual) ? null : String.join(", ", accepted);
  }

  public static boolean accepts(String type, DocumentStructure.Node node) {
    return unsupported(type, node) == null;
  }

  /**
   * The element may not be more repeated than its data context: the same repeated groups as far as both go, and no
   * additional ones. {@code context} is the group the form element is relative to, {@code null} at the top.
   */
  public static boolean fitsContext(DocumentStructure.@Nullable Node context, DocumentStructure.Node node) {
    List<DocumentStructure.Node> contextGroups = DocumentStructure.granularity(context);
    List<DocumentStructure.Node> nodeGroups = DocumentStructure.granularity(node);
    for (int i = 0; i < Math.min(contextGroups.size(), nodeGroups.size()); i++) {
      if (contextGroups.get(i) != nodeGroups.get(i)) {
        return false;
      }
    }
    return nodeGroups.size() <= contextGroups.size();
  }

  /** The Document Model elements a form element of {@code type} can be bound to at {@code context}, in document order. */
  public static List<DocumentStructure.Node> candidates(String type, DocumentStructure structure, DocumentStructure.@Nullable Node context) {
    return structure.all().stream().filter(node -> accepts(type, node) && fitsContext(context, node)).toList();
  }
}
