package de.a12.studio.dataservices.services.documentmodel.features.validation;

import de.a12.studio.models.documentmodel.ComputationElement;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.documentmodel.Element;
import de.a12.studio.models.documentmodel.EnumerationFieldType;
import de.a12.studio.models.documentmodel.FieldElement;
import de.a12.studio.models.documentmodel.FieldType;
import de.a12.studio.models.documentmodel.GroupConfig;
import de.a12.studio.models.documentmodel.GroupElement;
import de.a12.studio.models.documentmodel.NumberFieldType;
import de.a12.studio.models.documentmodel.RuleElement;
import de.a12.studio.models.documentmodel.StringFieldType;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Clean-room port of the a12 kernel's DocumentModelConsistencyRulesProvider rule set (decompiled from
 * kernel-md-model, EUPL-1.2 dual-licensed) onto {@code de.a12.studio.models.documentmodel} types. Ports 7 of
 * the kernel's 8 rules; the 8th, CodeGenerationRule, compiles every rule/computation expression through the
 * kernel's condition-language compiler (catching expression syntax errors, undefined field references, and
 * calc cycles) and is not reproducible without reimplementing that compiler, so it is intentionally omitted.
 */
final class DocumentModelConsistencyRules {

  private static final int MAX_DIGITS = 15;
  private static final Pattern VERSION_PATTERN = Pattern.compile("(\\d+)\\.(\\d+\\.\\d+)");
  private static final String COMPATIBLE_SCHEMA_VERSION = "28.4.0";

  private static final String[] ATTACHMENT_REQUIRED_FIELDS =
      {"original_filename", "internal_filename", "content", "attachment_id", "size", "mime_type", "category", "description"};
  private static final List<String> ATTACHMENT_REQUIRED_RULES_EN = List.of(
      "GroupFilled(RuleGroup) and NotExactlyOneFieldFilled(attachment_id, content)",
      "FieldFilled(content) and FieldNotFilled(size)",
      "GroupFilled(RuleGroup) and FieldNotFilled(internal_filename)",
      "GroupFilled(RuleGroup) and FieldNotFilled(mime_type)");
  private static final List<String> ATTACHMENT_REQUIRED_RULES_DE = List.of(
      "KontextAngegeben(RegelKontext) und NichtGenauEinFeldAngegeben(attachment_id, content)",
      "FeldAngegeben(content) und FeldNichtAngegeben(size)",
      "KontextAngegeben(RegelKontext) und FeldNichtAngegeben(internal_filename)",
      "KontextAngegeben(RegelKontext) und FeldNichtAngegeben(mime_type)");

  private DocumentModelConsistencyRules() {
  }

  static List<ValidationProblem> checkAll(DocumentModel model, ElementIndex index) {
    List<ValidationProblem> problems = new ArrayList<>();
    problems.addAll(checkSchemaVersion(model));
    problems.addAll(checkIdUnique(index));
    problems.addAll(checkNumberFieldValueLimits(index));
    problems.addAll(checkMultiSelectGroups(index));
    problems.addAll(checkAttachmentGroups(model, index));
    problems.addAll(checkBasicConsistency(index));
    return problems;
  }

  // --- IdUniqueRule ---

  private static List<ValidationProblem> checkIdUnique(ElementIndex index) {
    Map<String, List<Element>> byId = new HashMap<>();
    for (Element element : index.allElements()) {
      if (element.getId() != null) {
        byId.computeIfAbsent(element.getId(), k -> new ArrayList<>()).add(element);
      }
    }
    List<ValidationProblem> problems = new ArrayList<>();
    byId.forEach((id, elements) -> {
      if (elements.size() > 1) {
        String elementPaths = elements.stream().map(index::getPath).reduce((a, b) -> a + ", " + b).orElse("");
        for (Element element : elements) {
          problems.add(new ValidationProblem(element.getId(),
              "The id [" + id + "] of element on path '" + index.getPath(element) + "' is not unique: Used by [" + elementPaths + "].",
              Severity.ERROR));
        }
      }
    });
    return problems;
  }

  // --- NumberFieldValueLimitRule ---

  private static List<ValidationProblem> checkNumberFieldValueLimits(ElementIndex index) {
    List<ValidationProblem> problems = new ArrayList<>();
    for (Element element : index.allElements()) {
      if (!(element instanceof FieldElement field) || field.getField() == null) {
        continue;
      }
      FieldType effectiveType = index.effectiveFieldType(field.getField().getFieldType());
      if (!(effectiveType instanceof NumberFieldType numberFieldType) || numberFieldType.getNumberType() == null) {
        continue;
      }
      var numberType = numberFieldType.getNumberType();
      int maxDecimalPlaces = numberType.getMaxFractionalDigits() == null ? 0 : numberType.getMaxFractionalDigits();
      double maxAllowedValue = Math.pow(10.0, MAX_DIGITS - maxDecimalPlaces) - Math.pow(10.0, -maxDecimalPlaces);
      if (numberType.getMaxValue() != null && numberType.getMaxValue() > maxAllowedValue) {
        problems.add(new ValidationProblem(field.getId(),
            "The maximum value of the number type in field [id: " + field.getId() + "] is specified with '" + numberType.getMaxValue()
                + "'. It may not exceed '" + printLimit(maxAllowedValue, maxDecimalPlaces) + "'.",
            Severity.ERROR));
      }
      if (numberType.getMinValue() != null && Math.abs(numberType.getMinValue()) > maxAllowedValue) {
        problems.add(new ValidationProblem(field.getId(),
            "The minimum value of the number type in field [id: " + field.getId() + "] is specified with '" + numberType.getMinValue()
                + "'. It may not exceed '-" + printLimit(maxAllowedValue, maxDecimalPlaces) + "'.",
            Severity.ERROR));
      }
    }
    return problems;
  }

  private static String printLimit(double value, int maxDecimalPlaces) {
    DecimalFormat df = (DecimalFormat) NumberFormat.getInstance();
    df.setMaximumFractionDigits(maxDecimalPlaces);
    df.setMaximumIntegerDigits(MAX_DIGITS - maxDecimalPlaces);
    df.setRoundingMode(RoundingMode.DOWN);
    df.setGroupingUsed(false);
    DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance();
    symbols.setDecimalSeparator('.');
    df.setDecimalFormatSymbols(symbols);
    return df.format(value);
  }

  // --- MultiSelectGroupRule ---

  private static List<ValidationProblem> checkMultiSelectGroups(ElementIndex index) {
    List<ValidationProblem> problems = new ArrayList<>();
    for (Element element : index.allElements()) {
      if (!(element instanceof GroupElement groupElement) || groupElement.getGroup() == null) {
        continue;
      }
      GroupConfig group = groupElement.getGroup();
      if (!GroupConfig.USAGE_TYPE_MULTI_SELECT.equals(group.getUsageType())) {
        continue;
      }
      Integer repeatability = group.getRepeatability();
      if (repeatability == null || repeatability <= 1) {
        problems.add(new ValidationProblem(groupElement.getId(),
            "The multi-select group [" + groupElement.getName() + "] must be repeatable.", Severity.ERROR));
      }
      List<FieldElement> fieldsInGroup = group.getElements() == null ? List.of()
          : group.getElements().stream().filter(FieldElement.class::isInstance).map(FieldElement.class::cast).toList();
      if (fieldsInGroup.size() != 1) {
        problems.add(new ValidationProblem(groupElement.getId(),
            "In the multi-select group [" + groupElement.getName() + "], there must be only one field.", Severity.ERROR));
      } else {
        checkMultiSelectField(groupElement, group, fieldsInGroup.get(0), index, problems);
      }
      boolean otherElementsPresent = group.getElements() != null
          && group.getElements().stream().anyMatch(e -> !(e instanceof FieldElement) && !(e instanceof RuleElement));
      if (otherElementsPresent) {
        problems.add(new ValidationProblem(groupElement.getId(),
            "Besides rules and one field, other elements are not allowed in the multi-select group [" + groupElement.getName() + "].",
            Severity.ERROR));
      }
    }
    return problems;
  }

  private static void checkMultiSelectField(
      GroupElement groupElement, GroupConfig group, FieldElement field, ElementIndex index, List<ValidationProblem> problems) {
    if (field.getField() == null || field.getField().getRequirednessConfig() == null) {
      problems.add(new ValidationProblem(field.getId(),
          "The field [" + field.getName() + "] in the multi-select group [" + groupElement.getName() + "] must be marked as required.",
          Severity.ERROR));
    }
    if (field.getName() != null && field.getName().equals(group.getIndexFieldName())) {
      problems.add(new ValidationProblem(field.getId(),
          "The field [" + field.getName() + "] in the multi-select group [" + groupElement.getName() + "] may not be defined as index field.",
          Severity.ERROR));
    }
    FieldType effectiveType = field.getField() == null ? null : index.effectiveFieldType(field.getField().getFieldType());
    if (!(effectiveType instanceof EnumerationFieldType) && !(effectiveType instanceof StringFieldType)) {
      problems.add(new ValidationProblem(field.getId(),
          "The field [" + field.getName() + "] in the multi-select group [" + groupElement.getName() + "] must be an enumeration or a string.",
          Severity.ERROR));
    }
  }

  // --- AttachmentGroupRule ---

  private static List<ValidationProblem> checkAttachmentGroups(DocumentModel model, ElementIndex index) {
    List<ValidationProblem> problems = new ArrayList<>();
    boolean german = model.getContent().getModelConfig() != null
        && model.getContent().getModelConfig().getConditionLanguage() != null
        && "de".equalsIgnoreCase(model.getContent().getModelConfig().getConditionLanguage().getCode());
    List<String> requiredRules = german ? ATTACHMENT_REQUIRED_RULES_DE : ATTACHMENT_REQUIRED_RULES_EN;

    for (Element element : index.allElements()) {
      if (!(element instanceof GroupElement groupElement) || groupElement.getGroup() == null) {
        continue;
      }
      GroupConfig group = groupElement.getGroup();
      if (!GroupConfig.USAGE_TYPE_ATTACHMENT.equals(group.getUsageType())) {
        continue;
      }
      List<Element> elements = group.getElements() == null ? List.of() : group.getElements();
      List<String> fieldNames = elements.stream().filter(FieldElement.class::isInstance).map(Element::getName).toList();
      List<String> rulesPresent = elements.stream()
          .filter(RuleElement.class::isInstance).map(RuleElement.class::cast)
          .map(r -> r.getRule() == null ? null : r.getRule().getErrorCondition())
          .toList();

      List<String> missingFields = new ArrayList<>(List.of(ATTACHMENT_REQUIRED_FIELDS));
      missingFields.removeAll(fieldNames);
      if (!missingFields.isEmpty()) {
        problems.add(new ValidationProblem(groupElement.getId(),
            "Missing fields [" + String.join(", ", missingFields) + "] for attachment with group [" + groupElement.getName() + "].",
            Severity.ERROR));
      }
      List<String> missingRules = new ArrayList<>(requiredRules);
      missingRules.removeAll(rulesPresent);
      if (!missingRules.isEmpty()) {
        problems.add(new ValidationProblem(groupElement.getId(),
            "Missing rules [" + String.join(", ", missingRules) + "] for attachment with group [" + groupElement.getName() + "].",
            Severity.ERROR));
      }

      if (fieldNames.contains("content")) {
        elements.stream()
            .filter(e -> "content".equals(e.getName()) && e instanceof FieldElement)
            .map(FieldElement.class::cast)
            .findFirst()
            .ifPresent(contentField -> {
              FieldType fieldType = contentField.getField() == null ? null : contentField.getField().getFieldType();
              if (fieldType instanceof StringFieldType stringFieldType && stringFieldType.getStringType() != null) {
                var options = stringFieldType.getStringType();
                boolean noValueValidation = Boolean.TRUE.equals(options.getNoValueValidation());
                boolean lineBreaksPermitted = Boolean.TRUE.equals(options.getLineBreaksPermitted());
                if (!noValueValidation || !lineBreaksPermitted) {
                  problems.add(new ValidationProblem(groupElement.getId(),
                      "Field [content] in group [" + groupElement.getName()
                          + "] must specify both \"noValueValidation\" and \"linebreaksPermitted\".",
                      Severity.ERROR));
                }
              }
            });
      }
    }
    return problems;
  }

  // --- DocumentSchemaVersionRule / DocumentSchemaVersionPatternRule ---
  // Model-sourced (elementId == null): DMValidationService drops these, mirroring how the kernel's
  // getElementProblems only ever surfaced element-sourced problems to the UI. Kept for completeness.

  private static List<ValidationProblem> checkSchemaVersion(DocumentModel model) {
    List<ValidationProblem> problems = new ArrayList<>();
    String version = model.getModelVersion();
    if (version == null || !VERSION_PATTERN.matcher(version).matches()) {
      problems.add(new ValidationProblem(null,
          "Document model version " + version + " does not match proper version schema. Expected version is " + COMPATIBLE_SCHEMA_VERSION
              + ".",
          Severity.ERROR));
      return problems;
    }
    Matcher actual = VERSION_PATTERN.matcher(version);
    Matcher compatible = VERSION_PATTERN.matcher(COMPATIBLE_SCHEMA_VERSION);
    actual.matches();
    compatible.matches();
    boolean compatibleVersion = Integer.parseInt(actual.group(1)) == Integer.parseInt(compatible.group(1))
        && Double.parseDouble(compatible.group(2)) >= Double.parseDouble(actual.group(2));
    if (!compatibleVersion) {
      problems.add(new ValidationProblem(null,
          "Version mismatch: Document model version is " + version + " and application version is " + COMPATIBLE_SCHEMA_VERSION + ".",
          Severity.ERROR));
    }
    return problems;
  }

  // --- BasicConsistencyCheckFromSerializerRule (-> BasicConsistencyCheck) ---
  // Two of the original checks have no equivalent in this simplified DTO shape and are intentionally not
  // ported: sort-field-name-empty (GroupConfig here has no sortFields concept at all) and null-localized-text
  // (Label here is a sparse list of present locales, so a locale simply being absent isn't the same failure
  // as the kernel's fixed-size map holding an explicit null for it).

  private static List<ValidationProblem> checkBasicConsistency(ElementIndex index) {
    List<ValidationProblem> problems = new ArrayList<>();
    for (Element element : index.allElements()) {
      if (isBlank(element.getId())) {
        problems.add(new ValidationProblem(element.getId(), "Element on path '" + index.getPath(element) + "': The id is empty.",
            Severity.ERROR));
      }
      if (isBlank(element.getName())) {
        problems.add(new ValidationProblem(element.getId(), "Element with id '" + element.getId() + "': The name is empty.",
            Severity.ERROR));
      }
      if (element instanceof FieldElement field && field.getField() != null) {
        checkEnumerationOrTypeDef(index, field, problems);
      } else if (element instanceof RuleElement rule && rule.getRule() != null) {
        checkRule(index, rule, problems);
      } else if (element instanceof ComputationElement computation && computation.getComputation() != null) {
        checkComputation(computation, problems);
      } else if (element instanceof GroupElement group && group.getGroup() != null) {
        checkGroupIndexField(index, group, problems);
      }
    }
    return problems;
  }

  private static void checkEnumerationOrTypeDef(ElementIndex index, FieldElement field, List<ValidationProblem> problems) {
    FieldType fieldType = field.getField().getFieldType();
    if (fieldType instanceof EnumerationFieldType enumType && enumType.getEnumerationType() != null) {
      Set<String> seenValues = new HashSet<>();
      var values = enumType.getEnumerationType().getValues();
      if (values != null) {
        for (var value : values) {
          if (value.getValue() != null && !seenValues.add(value.getValue())) {
            problems.add(new ValidationProblem(field.getId(),
                "Field with id '" + field.getId() + "': The enumeration contains the value '" + value.getValue() + "' multiple times.",
                Severity.ERROR));
          }
        }
      }
      Set<String> seenCategories = new HashSet<>();
      var categories = enumType.getEnumerationType().getCategories();
      if (categories != null) {
        for (var category : categories) {
          if (category.getName() != null && !seenCategories.add(category.getName())) {
            problems.add(new ValidationProblem(field.getId(),
                "Field with id '" + field.getId() + "': The enumeration contains the category '" + category.getName() + "' multiple times.",
                Severity.ERROR));
          }
        }
      }
    }
    if (fieldType instanceof de.a12.studio.models.documentmodel.TypeDefFieldType typeDefType
        && (typeDefType.getTypeDefType() == null || isBlank(typeDefType.getTypeDefType().getTypeDefinitionId()))) {
      problems.add(new ValidationProblem(field.getId(),
          "Field with id '" + field.getId() + "': The id of the referenced type definition is not set properly.", Severity.ERROR));
    }
  }

  private static void checkRule(ElementIndex index, RuleElement rule, List<ValidationProblem> problems) {
    var ruleConfig = rule.getRule();
    if (isBlank(ruleConfig.getErrorCode())) {
      problems.add(new ValidationProblem(rule.getId(), "Rule with id '" + rule.getId() + "': The error code is empty.", Severity.ERROR));
    }
    if (isBlank(ruleConfig.getErrorCondition())) {
      problems.add(new ValidationProblem(rule.getId(), "Rule with id '" + rule.getId() + "': The error condition is empty.", Severity.ERROR));
    }
    if (isBlank(ruleConfig.getErrorEntityRelPath())) {
      problems.add(new ValidationProblem(rule.getId(), "Rule with id '" + rule.getId() + "': The error entity is not set properly.",
          Severity.ERROR));
    }
  }

  private static void checkComputation(ComputationElement computation, List<ValidationProblem> problems) {
    var computationConfig = computation.getComputation();
    if (isBlank(computationConfig.getComputedFieldRelPath())) {
      problems.add(new ValidationProblem(computation.getId(),
          "Computation with id '" + computation.getId() + "': The computed field is not set properly.", Severity.ERROR));
    }
    if (computationConfig.getComputationAlternatives() != null
        && computationConfig.getComputationAlternatives().stream().anyMatch(a -> isBlank(a.getOperation()))) {
      problems.add(new ValidationProblem(computation.getId(),
          "Computation with id '" + computation.getId() + "': One of the computation operations is empty.", Severity.ERROR));
    }
  }

  private static void checkGroupIndexField(ElementIndex index, GroupElement group, List<ValidationProblem> problems) {
    String indexFieldName = group.getGroup().getIndexFieldName();
    if (indexFieldName != null && isBlank(indexFieldName)) {
      problems.add(new ValidationProblem(group.getId(),
          "Group with id '" + group.getId() + "': The name of the index field is not set properly.", Severity.ERROR));
    }
  }

  private static boolean isBlank(String value) {
    return value == null || value.isBlank();
  }
}
