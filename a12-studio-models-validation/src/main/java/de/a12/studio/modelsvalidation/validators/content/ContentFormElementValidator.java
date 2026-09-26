package de.a12.studio.modelsvalidation.validators.content;

import de.a12.studio.models.contentmodel.ContentElement;
import de.a12.studio.models.contentmodel.ContentElementLibrary;
import de.a12.studio.models.documentmodel.FieldType;
import de.a12.studio.models.documentmodel.StringFieldType;

import java.util.List;
import java.util.Map;

/**
 * The form elements of a Content Model (Text Line, Checkbox, Select, ...), as SME's shared form element validator and
 * the check of each element type make them: the element needs the Document Model and an {@code elementId} that names an
 * element of it, whose data type the form element can show, and that is not repeated more often than the data context
 * of the form element's position ("The Document Model element ... is not compatible with the current data context").
 * A form element cannot have children (a warning).
 */
public final class ContentFormElementValidator extends AbstractContentNodeValidator {

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

  @Override
  void check(ContentNodeWalker.NodeInfo info, Findings findings) {
    ContentElement element = info.element();
    if (info.owner() != null || !ContentElementLibrary.FORM_ELEMENTS_NAMESPACE.equals(ContentNodeWalker.namespace(element))
        || element.getType() == null || !isInputElement(element.getType())) {
      return;
    }
    if (element.getChildren() != null && !element.getChildren().isEmpty()) {
      findings.warning("validation.contentFormElement.children");
    }
    Object value = element.getProps() != null ? element.getProps().get("elementId") : null;
    String elementId = value instanceof String string ? string : null;
    if (elementId == null || elementId.isBlank()) {
      findings.error("validation.contentFormElement.missing");
      return;
    }
    DocumentStructure structure = info.document().structure();
    if (structure == null) {
      findings.error("validation.contentReference.noDocumentModel");
      return;
    }
    DocumentStructure.Node node = structure.find(elementId);
    if (node == null) {
      findings.error("validation.contentFormElement.notFound", elementId, info.document().documentModelId());
      return;
    }
    if (!isCompatible(DocumentStructure.granularity(info.context()), DocumentStructure.granularity(node))) {
      findings.error("validation.contentFormElement.incompatibleContext", node.path(),
          info.context() != null ? info.context().path() : "/");
      return;
    }
    String supported = unsupportedMessage(element.getType(), node);
    if (supported != null) {
      findings.error("validation.contentFormElement.wrongType", node.path(), supported);
    }
  }

  private static boolean isInputElement(String type) {
    return FIELD_TYPES.containsKey(type) || AUTO_COMPLETE.equals(type) || MULTI_SELECT_GROUPS.contains(type);
  }

  /** The element may not be more repeated than its context: same repeated groups as far as both go, and no additional ones. */
  private static boolean isCompatible(List<DocumentStructure.Node> context, List<DocumentStructure.Node> element) {
    for (int i = 0; i < Math.min(context.size(), element.size()); i++) {
      if (context.get(i) != element.get(i)) {
        return false;
      }
    }
    return element.size() <= context.size();
  }

  /** What the form element type accepts, if {@code node} is not that; else null. */
  private static String unsupportedMessage(String type, DocumentStructure.Node node) {
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
}
