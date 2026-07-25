package de.a12.studio.modelsvalidation.validators;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.documentmodel.Element;
import de.a12.studio.models.documentmodel.FieldElement;
import de.a12.studio.models.documentmodel.FieldType;
import de.a12.studio.models.documentmodel.GroupConfig;
import de.a12.studio.models.documentmodel.GroupElement;
import de.a12.studio.models.documentmodel.RuleElement;
import de.a12.studio.models.documentmodel.StringFieldType;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.Severity;
import de.a12.studio.modelsvalidation.ValidationContext;

import java.util.ArrayList;
import java.util.List;

/**
 * Port of the kernel's AttachmentGroupRule (decompiled from kernel-md-model, EUPL-1.2 dual-licensed): an
 * attachment-usage group must have the fixed set of attachment fields and rules the kernel's attachment
 * handling relies on.
 */
public final class AttachmentGroupValidator implements ModelValidator {

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

  @Override
  public List<ModelValidationError> validate(A12Model<?> model, ValidationContext context) {
    if (!(model instanceof DocumentModel documentModel)) {
      return List.of();
    }

    ElementIndex index = new ElementIndex(documentModel);
    boolean german = documentModel.getContent().getModelConfig() != null
        && documentModel.getContent().getModelConfig().getConditionLanguage() != null
        && "de".equalsIgnoreCase(documentModel.getContent().getModelConfig().getConditionLanguage().getCode());
    List<String> requiredRules = german ? ATTACHMENT_REQUIRED_RULES_DE : ATTACHMENT_REQUIRED_RULES_EN;

    List<ModelValidationError> errors = new ArrayList<>();
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
        errors.add(error(model, groupElement.getId(),
            "Missing fields [" + String.join(", ", missingFields) + "] for attachment with group [" + groupElement.getName() + "]."));
      }
      List<String> missingRules = new ArrayList<>(requiredRules);
      missingRules.removeAll(rulesPresent);
      if (!missingRules.isEmpty()) {
        errors.add(error(model, groupElement.getId(),
            "Missing rules [" + String.join(", ", missingRules) + "] for attachment with group [" + groupElement.getName() + "]."));
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
                  errors.add(error(model, groupElement.getId(), "Field [content] in group [" + groupElement.getName()
                      + "] must specify both \"noValueValidation\" and \"linebreaksPermitted\"."));
                }
              }
            });
      }
    }
    return errors;
  }

  private static ModelValidationError error(A12Model<?> model, String elementId, String message) {
    return new ModelValidationError(model, elementId, message, Severity.ERROR.name());
  }
}
