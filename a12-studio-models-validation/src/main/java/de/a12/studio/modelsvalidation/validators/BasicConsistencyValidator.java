package de.a12.studio.modelsvalidation.validators;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.documentmodel.ComputationElement;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.documentmodel.Element;
import de.a12.studio.models.documentmodel.EnumerationFieldType;
import de.a12.studio.models.documentmodel.FieldElement;
import de.a12.studio.models.documentmodel.FieldType;
import de.a12.studio.models.documentmodel.GroupElement;
import de.a12.studio.models.documentmodel.RuleElement;
import de.a12.studio.models.documentmodel.TypeDefinition;
import de.a12.studio.modelsvalidation.ElementProperty;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.Severity;
import de.a12.studio.modelsvalidation.ValidationContext;
import de.a12.studio.modelsvalidation.ValidationMessages;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Port of the kernel's BasicConsistencyCheckFromSerializerRule (decompiled from kernel-md-model, EUPL-1.2
 * dual-licensed): required-field-not-empty and duplicate-value checks across every element kind. Two of the
 * original checks have no equivalent in this simplified DTO shape and are intentionally not ported:
 * sort-field-name-empty (GroupConfig here has no sortFields concept at all) and null-localized-text (Label
 * here is a sparse list of present locales, so a locale simply being absent isn't the same failure as the
 * kernel's fixed-size map holding an explicit null for it).
 */
public final class BasicConsistencyValidator implements ModelValidator {

  @Override
  public List<ModelValidationError> validate(A12Model<?> model, ValidationContext context) {
    if (!(model instanceof DocumentModel documentModel)) {
      return List.of();
    }

    ElementIndex index = new ElementIndex(documentModel);
    List<ModelValidationError> errors = new ArrayList<>();
    for (Element element : index.allElements()) {
      if (isBlank(element.getId())) {
        errors.add(error(model, element.getId(), ElementProperty.GENERAL,
            ValidationMessages.get("validation.basicConsistency.emptyId", index.getPath(element))));
      }
      if (isBlank(element.getName())) {
        errors.add(error(model, element.getId(), ElementProperty.GENERAL,
            ValidationMessages.get("validation.basicConsistency.emptyName", element.getId())));
      }
      if (element instanceof FieldElement field && field.getField() != null) {
        checkEnumerationOrTypeDef(model, index, field, errors);
      } else if (element instanceof RuleElement rule && rule.getRule() != null) {
        checkRule(model, rule, errors);
      } else if (element instanceof ComputationElement computation && computation.getComputation() != null) {
        checkComputation(model, computation, errors);
      } else if (element instanceof GroupElement group && group.getGroup() != null) {
        checkGroupIndexField(model, group, errors);
      }
    }

    // A model's own type definitions aren't reachable through allElements() (they live in
    // content.typeDefinitions, not the modelRoot tree) - without this second pass, an Enumeration type
    // definition's own duplicate value/category problems would never be caught until some other model's
    // field happens to reference it. TypeDefType is deliberately not checked here: a type definition's own
    // fieldType can never itself be a TypeDefType reference (the "Use Custom Type" option is hidden in the
    // Type Definition Model's field editor - see TypeDefinitionPanelController.setCustomTypeDisabled()).
    if (documentModel.getContent().getTypeDefinitions() != null) {
      for (TypeDefinition typeDefinition : documentModel.getContent().getTypeDefinitions()) {
        if (typeDefinition.getFieldType() instanceof EnumerationFieldType enumType && enumType.getEnumerationType() != null) {
          checkEnumerationDuplicates(model, typeDefinition.getId(), enumType, errors);
        }
      }
    }
    return errors;
  }

  /** {@code TypeDefFieldType}'s own reference-validity check moved to {@link MissingReferenceValidator}'s
   * {@code hasMissingTypeDef}, which already covered both the blank-id and unresolved-id cases - this used to
   * duplicate its blank-id half under a different message. */
  private static void checkEnumerationOrTypeDef(A12Model<?> model, ElementIndex index, FieldElement field, List<ModelValidationError> errors) {
    FieldType fieldType = field.getField().getFieldType();
    if (fieldType instanceof EnumerationFieldType enumType && enumType.getEnumerationType() != null) {
      checkEnumerationDuplicates(model, field.getId(), enumType, errors);
    }
  }

  private static void checkEnumerationDuplicates(A12Model<?> model, String elementId, EnumerationFieldType enumType,
      List<ModelValidationError> errors) {
    Set<String> seenValues = new HashSet<>();
    var values = enumType.getEnumerationType().getValues();
    if (values != null) {
      for (var value : values) {
        if (value.getValue() != null && !seenValues.add(value.getValue())) {
          errors.add(error(model, elementId, ElementProperty.DATA_TYPE,
              ValidationMessages.get("validation.basicConsistency.duplicateEnumValue", elementId, value.getValue())));
        }
      }
    }
    Set<String> seenCategories = new HashSet<>();
    var categories = enumType.getEnumerationType().getCategories();
    if (categories != null) {
      for (var category : categories) {
        if (category.getName() != null && !seenCategories.add(category.getName())) {
          errors.add(error(model, elementId, ElementProperty.DATA_TYPE,
              ValidationMessages.get("validation.basicConsistency.duplicateEnumCategory", elementId, category.getName())));
        }
      }
    }
  }

  private static void checkRule(A12Model<?> model, RuleElement rule, List<ModelValidationError> errors) {
    var ruleConfig = rule.getRule();
    if (isBlank(ruleConfig.getErrorCode())) {
      errors.add(error(model, rule.getId(), ElementProperty.RULE_PROPERTIES,
          ValidationMessages.get("validation.basicConsistency.emptyRuleErrorCode", rule.getId())));
    }
    if (isBlank(ruleConfig.getErrorCondition())) {
      errors.add(error(model, rule.getId(), ElementProperty.RULE_PROPERTIES,
          ValidationMessages.get("validation.basicConsistency.emptyRuleErrorCondition", rule.getId())));
    }
    if (isBlank(ruleConfig.getErrorEntityRelPath())) {
      errors.add(error(model, rule.getId(), ElementProperty.RULE_PROPERTIES,
          ValidationMessages.get("validation.basicConsistency.invalidRuleErrorEntity", rule.getId())));
    }
  }

  private static void checkComputation(A12Model<?> model, ComputationElement computation, List<ModelValidationError> errors) {
    var computationConfig = computation.getComputation();
    if (isBlank(computationConfig.getComputedFieldRelPath())) {
      errors.add(error(model, computation.getId(), ElementProperty.COMPUTATION_PROPERTIES,
          ValidationMessages.get("validation.basicConsistency.invalidComputedField", computation.getId())));
    }
    if (computationConfig.getComputationAlternatives() != null
        && computationConfig.getComputationAlternatives().stream().anyMatch(a -> isBlank(a.getOperation()))) {
      errors.add(error(model, computation.getId(), ElementProperty.COMPUTATION_PROPERTIES,
          ValidationMessages.get("validation.basicConsistency.emptyComputationOperation", computation.getId())));
    }
  }

  private static void checkGroupIndexField(A12Model<?> model, GroupElement group, List<ModelValidationError> errors) {
    String indexFieldName = group.getGroup().getIndexFieldName();
    if (indexFieldName != null && isBlank(indexFieldName)) {
      errors.add(error(model, group.getId(), ElementProperty.GROUP_PROPERTIES,
          ValidationMessages.get("validation.basicConsistency.invalidIndexFieldName", group.getId())));
    }
  }

  private static boolean isBlank(String value) {
    return value == null || value.isBlank();
  }

  private static ModelValidationError error(A12Model<?> model, String elementId, String property, String message) {
    return new ModelValidationError(model, elementId, property, message, Severity.ERROR.name());
  }
}
