package de.a12.studio.ui.editors.documentmodel;

import de.a12.studio.models.Label;
import de.a12.studio.models.documentmodel.ComputationConfig;
import de.a12.studio.models.documentmodel.ComputationElement;
import de.a12.studio.models.documentmodel.Element;
import de.a12.studio.models.documentmodel.EnumerationFieldType;
import de.a12.studio.models.documentmodel.EnumerationTypeOptions;
import de.a12.studio.models.documentmodel.EnumerationValue;
import de.a12.studio.models.documentmodel.FieldConfig;
import de.a12.studio.models.documentmodel.FieldElement;
import de.a12.studio.models.documentmodel.GroupConfig;
import de.a12.studio.models.documentmodel.GroupElement;
import de.a12.studio.models.documentmodel.ModelRoot;
import de.a12.studio.models.documentmodel.NumberFieldType;
import de.a12.studio.models.documentmodel.RequirednessConfig;
import de.a12.studio.models.documentmodel.RuleConfig;
import de.a12.studio.models.documentmodel.RuleElement;
import de.a12.studio.models.documentmodel.StringFieldType;
import de.a12.studio.models.documentmodel.StringTypeOptions;
import org.jspecify.annotations.NonNull;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * Creates the {@link Element} subtypes offered in the document model tree's "add" menu, including the
 * fixed child elements of the "attachment" and "multi-select" usage-type groups.
 */
public class DocumentModelElementFactory {

  private static final String ID_PREFIX_GROUP = "group";
  private static final String ID_PREFIX_FIELD = "field";
  private static final String ID_PREFIX_RULE = "rule";
  private static final String ID_PREFIX_COMPUTATION = "computation";
  private static final String ID_PREFIX_ATTACHMENT = "attachment";
  private static final String ID_PREFIX_MULTI_SELECT = "multi-select";
  private static final String ID_PREFIX_MULTI_SELECT_CHILD = "multiSelectChild";
  private static final String ID_PREFIX_INCLUDE = "include";

  private static final Random ID_RANDOM = new SecureRandom();

  private DocumentModelElementFactory() {
  }

  public static Element newGroupElement(@NonNull List<Element> siblings, @NonNull ModelRoot modelRoot) {
    GroupElement group = new GroupElement();
    group.setId(generateId(ID_PREFIX_GROUP, modelRoot));
    group.setName(uniqueName("Group", siblings));
    GroupConfig config = new GroupConfig();
    config.setRepeatability(1);
    group.setGroup(config);
    return group;
  }

  public static Element newFieldElement(@NonNull List<Element> siblings, @NonNull ModelRoot modelRoot) {
    FieldElement field = new FieldElement();
    field.setId(generateId(ID_PREFIX_FIELD, modelRoot));
    field.setName(uniqueName("Field", siblings));
    field.setField(newStringFieldConfig());
    return field;
  }

  public static Element newRuleElement(@NonNull List<Element> siblings, @NonNull ModelRoot modelRoot) {
    RuleElement rule = new RuleElement();
    String id = generateId(ID_PREFIX_RULE, modelRoot);
    rule.setId(id);
    rule.setName(uniqueName("ValidationRule", siblings));
    RuleConfig config = new RuleConfig();
    config.setErrorCode("Error " + id);
    rule.setRule(config);
    return rule;
  }

  public static Element newComputationElement(@NonNull List<Element> siblings, @NonNull ModelRoot modelRoot) {
    ComputationElement computation = new ComputationElement();
    computation.setId(generateId(ID_PREFIX_COMPUTATION, modelRoot));
    computation.setName(uniqueName("ComputationRule", siblings));
    computation.setComputation(new ComputationConfig());
    return computation;
  }

  /**
   * A group with a fixed set of children (attachment). Field/rule names, error conditions and
   * messages mirror what the kernel expects for the "attachment" usage type.
   */
  public static Element newAttachmentElement(@NonNull List<Element> siblings, @NonNull ModelRoot modelRoot) {
    GroupElement attachment = new GroupElement();
    attachment.setId(generateId(ID_PREFIX_ATTACHMENT, modelRoot));
    attachment.setName(uniqueName("Attachment", siblings));
    GroupConfig config = new GroupConfig();
    config.setRepeatability(1);
    config.setUsageType(GroupConfig.USAGE_TYPE_ATTACHMENT);
    config.getElements().addAll(createAttachmentFixedChildren(modelRoot));
    attachment.setGroup(config);
    return attachment;
  }

  private static List<Element> createAttachmentFixedChildren(@NonNull ModelRoot modelRoot) {
    List<Element> children = new ArrayList<>();
    children.add(newFixedField("original_filename", newStringFieldConfig(), modelRoot));
    children.add(newFixedField("internal_filename", newStringFieldConfig(), modelRoot));
    children.add(newFixedField("content", newAttachmentContentFieldConfig(), modelRoot));
    children.add(newFixedField("attachment_id", newStringFieldConfig(), modelRoot));
    children.add(newFixedField("size", newNumberFieldConfig(), modelRoot));
    children.add(newFixedField("mime_type", newStringFieldConfig(), modelRoot));
    children.add(newFixedField("category", newStringFieldConfig(), modelRoot));
    children.add(newFixedField("description", newStringFieldConfig(), modelRoot));
    children.add(newAttachmentRule("AttachmentInternalFilenameRequired", "../internal_filename",
        "GroupFilled(RuleGroup) and FieldNotFilled(internal_filename)",
        "Internal Error: Field $internal_filename$ of customType attachment is not filled.", modelRoot));
    children.add(newAttachmentRule("AttachmentMimeTypeRequired", "../mime_type",
        "GroupFilled(RuleGroup) and FieldNotFilled(mime_type)",
        "Internal Error: Field $mime_type$ of customType attachment is not filled.", modelRoot));
    children.add(newAttachmentRule("AttachmentIdOrContentFilled", "../content",
        "GroupFilled(RuleGroup) and NotExactlyOneFieldFilled(attachment_id, content)",
        "Internal Error: Either attachment_id or content must be filled in a customType attachment, but not both.", modelRoot));
    children.add(newAttachmentRule("SizeOfContentFilled", "../content",
        "FieldFilled(content) and FieldNotFilled(size)",
        "Internal Error: If the content is filled, the size must be also filled.", modelRoot));
    return children;
  }

  private static RuleElement newAttachmentRule(@NonNull String name, @NonNull String errorEntityRelPath,
                                                @NonNull String errorCondition, @NonNull String errorMessageText,
                                                @NonNull ModelRoot modelRoot) {
    RuleElement rule = new RuleElement();
    String id = generateId(ID_PREFIX_RULE, modelRoot);
    rule.setId(id);
    rule.setName(name);
    RuleConfig config = new RuleConfig();
    config.setErrorEntityRelPath(errorEntityRelPath);
    config.setErrorCode("Error " + id);
    config.setErrorCondition(errorCondition);
    config.setSeverity("ERROR");
    config.getErrorMessage().add(newLabel("en", errorMessageText));
    config.getErrorMessage().add(newLabel("de", errorMessageText));
    rule.setRule(config);
    return rule;
  }

  /**
   * A group with a fixed single "value" enumeration child, forced to the maximum repeatability
   * (999999) that the kernel treats as "unbounded" for the "multi-select" usage type.
   */
  public static Element newMultiSelectElement(@NonNull List<Element> siblings, @NonNull ModelRoot modelRoot) {
    GroupElement multiSelect = new GroupElement();
    multiSelect.setId(generateId(ID_PREFIX_MULTI_SELECT, modelRoot));
    multiSelect.setName(uniqueName("Multi-Select", siblings));
    GroupConfig config = new GroupConfig();
    config.setRepeatability(999_999);
    config.setUsageType(GroupConfig.USAGE_TYPE_MULTI_SELECT);
    config.getElements().add(newMultiSelectFixedChild(modelRoot));
    multiSelect.setGroup(config);
    return multiSelect;
  }

  private static FieldElement newMultiSelectFixedChild(@NonNull ModelRoot modelRoot) {
    FieldElement field = new FieldElement();
    field.setId(generateId(ID_PREFIX_MULTI_SELECT_CHILD, modelRoot));
    field.setName("value");
    FieldConfig config = new FieldConfig();
    RequirednessConfig requirednessConfig = new RequirednessConfig();
    requirednessConfig.setMode(RequirednessConfig.MODE_REQUIRED);
    config.setRequirednessConfig(requirednessConfig);
    EnumerationTypeOptions options = new EnumerationTypeOptions();
    options.getValues().add(newEnumerationValue("key1"));
    options.getValues().add(newEnumerationValue("key2"));
    EnumerationFieldType fieldType = new EnumerationFieldType();
    fieldType.setEnumerationType(options);
    config.setFieldType(fieldType);
    field.setField(config);
    return field;
  }

  private static EnumerationValue newEnumerationValue(@NonNull String value) {
    EnumerationValue enumerationValue = new EnumerationValue();
    enumerationValue.setValue(value);
    return enumerationValue;
  }

  /**
   * A group referencing another Document Model. The reference itself ({@code modelAlias}) has no
   * picker in the UI yet, so it's left unset here, same as a plain group, until that reference can
   * be assigned (surfaces as a "Missing Include Reference" validation error until then).
   */
  public static Element newIncludeElement(@NonNull List<Element> siblings, @NonNull ModelRoot modelRoot) {
    GroupElement include = new GroupElement();
    include.setId(generateId(ID_PREFIX_INCLUDE, modelRoot));
    include.setName(uniqueName("New Include", siblings));
    GroupConfig config = new GroupConfig();
    config.setRepeatability(1);
    include.setGroup(config);
    return include;
  }

  private static FieldElement newFixedField(@NonNull String name, @NonNull FieldConfig config, @NonNull ModelRoot modelRoot) {
    FieldElement field = new FieldElement();
    field.setId(generateId(ID_PREFIX_FIELD, modelRoot));
    field.setName(name);
    field.setField(config);
    return field;
  }

  private static FieldConfig newStringFieldConfig() {
    FieldConfig config = new FieldConfig();
    config.setFieldType(new StringFieldType());
    return config;
  }

  /**
   * The attachment "content" field holds the base64-encoded file payload, so it must permit line
   * breaks and skip the kernel's normal value validation, matching what the SME reference
   * implementation sets for this field.
   */
  private static FieldConfig newAttachmentContentFieldConfig() {
    FieldConfig config = new FieldConfig();
    StringFieldType fieldType = new StringFieldType();
    StringTypeOptions options = new StringTypeOptions();
    options.setLineBreaksPermitted(true);
    options.setNoValueValidation(true);
    fieldType.setStringType(options);
    config.setFieldType(fieldType);
    return config;
  }

  private static FieldConfig newNumberFieldConfig() {
    FieldConfig config = new FieldConfig();
    config.setFieldType(new NumberFieldType());
    return config;
  }

  private static Label newLabel(@NonNull String locale, @NonNull String text) {
    Label label = new Label();
    label.setLocale(locale);
    label.setText(text);
    return label;
  }

  private static String uniqueName(@NonNull String baseName, @NonNull List<Element> siblings) {
    Set<String> usedNames = new HashSet<>();
    for (Element sibling : siblings) {
      usedNames.add(sibling.getName());
    }
    if (!usedNames.contains(baseName)) {
      return baseName;
    }
    int suffix = 2;
    while (usedNames.contains(baseName + "_" + suffix)) {
      suffix++;
    }
    return baseName + "_" + suffix;
  }

  private static String generateId(@NonNull String prefix, @NonNull ModelRoot modelRoot) {
    Set<String> usedIds = collectIds(modelRoot);
    String id;
    do {
      id = prefix + "_" + String.format("%05x", ID_RANDOM.nextInt(0x100000));
    } while (usedIds.contains(id));
    return id;
  }

  private static Set<String> collectIds(@NonNull ModelRoot modelRoot) {
    Set<String> ids = new HashSet<>();
    for (GroupElement rootGroup : modelRoot.getRootGroups()) {
      collectIds(rootGroup, ids);
    }
    return ids;
  }

  private static void collectIds(@NonNull Element element, @NonNull Set<String> ids) {
    ids.add(element.getId());
    if (element instanceof GroupElement groupElement && groupElement.getGroup() != null) {
      for (Element child : groupElement.getGroup().getElements()) {
        collectIds(child, ids);
      }
    }
  }
}
