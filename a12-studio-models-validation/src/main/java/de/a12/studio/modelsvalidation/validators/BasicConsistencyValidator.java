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
import de.a12.studio.models.documentmodel.TypeDefFieldType;
import de.a12.studio.modelsvalidation.ElementProperty;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.Severity;
import de.a12.studio.modelsvalidation.ValidationContext;

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
        errors.add(error(model, element.getId(), ElementProperty.GENERAL, "Element on path '" + index.getPath(element) + "': The id is empty."));
      }
      if (isBlank(element.getName())) {
        errors.add(error(model, element.getId(), ElementProperty.GENERAL, "Element with id '" + element.getId() + "': The name is empty."));
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
    return errors;
  }

  private static void checkEnumerationOrTypeDef(A12Model<?> model, ElementIndex index, FieldElement field, List<ModelValidationError> errors) {
    FieldType fieldType = field.getField().getFieldType();
    if (fieldType instanceof EnumerationFieldType enumType && enumType.getEnumerationType() != null) {
      Set<String> seenValues = new HashSet<>();
      var values = enumType.getEnumerationType().getValues();
      if (values != null) {
        for (var value : values) {
          if (value.getValue() != null && !seenValues.add(value.getValue())) {
            errors.add(error(model, field.getId(), ElementProperty.DATA_TYPE,
                "Field with id '" + field.getId() + "': The enumeration contains the value '" + value.getValue() + "' multiple times."));
          }
        }
      }
      Set<String> seenCategories = new HashSet<>();
      var categories = enumType.getEnumerationType().getCategories();
      if (categories != null) {
        for (var category : categories) {
          if (category.getName() != null && !seenCategories.add(category.getName())) {
            errors.add(error(model, field.getId(), ElementProperty.DATA_TYPE, "Field with id '" + field.getId()
                + "': The enumeration contains the category '" + category.getName() + "' multiple times."));
          }
        }
      }
    }
    if (fieldType instanceof TypeDefFieldType typeDefType
        && (typeDefType.getTypeDefType() == null || isBlank(typeDefType.getTypeDefType().getTypeDefinitionId()))) {
      errors.add(error(model, field.getId(), ElementProperty.TYPE,
          "Field with id '" + field.getId() + "': The id of the referenced type definition is not set properly."));
    }
  }

  private static void checkRule(A12Model<?> model, RuleElement rule, List<ModelValidationError> errors) {
    var ruleConfig = rule.getRule();
    if (isBlank(ruleConfig.getErrorCode())) {
      errors.add(error(model, rule.getId(), ElementProperty.GENERAL, "Rule with id '" + rule.getId() + "': The error code is empty."));
    }
    if (isBlank(ruleConfig.getErrorCondition())) {
      errors.add(error(model, rule.getId(), ElementProperty.GENERAL, "Rule with id '" + rule.getId() + "': The error condition is empty."));
    }
    if (isBlank(ruleConfig.getErrorEntityRelPath())) {
      errors.add(error(model, rule.getId(), ElementProperty.GENERAL, "Rule with id '" + rule.getId() + "': The error entity is not set properly."));
    }
  }

  private static void checkComputation(A12Model<?> model, ComputationElement computation, List<ModelValidationError> errors) {
    var computationConfig = computation.getComputation();
    if (isBlank(computationConfig.getComputedFieldRelPath())) {
      errors.add(error(model, computation.getId(), ElementProperty.GENERAL,
          "Computation with id '" + computation.getId() + "': The computed field is not set properly."));
    }
    if (computationConfig.getComputationAlternatives() != null
        && computationConfig.getComputationAlternatives().stream().anyMatch(a -> isBlank(a.getOperation()))) {
      errors.add(error(model, computation.getId(), ElementProperty.GENERAL,
          "Computation with id '" + computation.getId() + "': One of the computation operations is empty."));
    }
  }

  private static void checkGroupIndexField(A12Model<?> model, GroupElement group, List<ModelValidationError> errors) {
    String indexFieldName = group.getGroup().getIndexFieldName();
    if (indexFieldName != null && isBlank(indexFieldName)) {
      errors.add(error(model, group.getId(), ElementProperty.GROUP_PROPERTIES,
          "Group with id '" + group.getId() + "': The name of the index field is not set properly."));
    }
  }

  private static boolean isBlank(String value) {
    return value == null || value.isBlank();
  }

  private static ModelValidationError error(A12Model<?> model, String elementId, String property, String message) {
    return new ModelValidationError(model, elementId, property, message, Severity.ERROR.name());
  }
}
