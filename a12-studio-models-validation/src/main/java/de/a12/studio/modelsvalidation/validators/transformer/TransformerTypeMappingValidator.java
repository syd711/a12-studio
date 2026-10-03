package de.a12.studio.modelsvalidation.validators.transformer;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.transformermodel.A12DataType;
import de.a12.studio.models.transformermodel.TransformerModel;
import de.a12.studio.models.transformermodel.TypeMappingEntry;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.Severity;
import de.a12.studio.modelsvalidation.ValidationContext;
import de.a12.studio.modelsvalidation.ValidationMessages;
import de.a12.studio.modelsvalidation.validators.ModelValidator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The structure of the Transformation tab's Type Mappings, ported from SME's {@code TransformerConfigModel} (29.4.0):
 * <ul>
 *   <li>{@code xsdType}/{@code a12Type} are required (their {@code requirednessConfig}); an {@code a12Type} that is
 *       not one of SME's eleven data types is rejected like any value outside an enumeration</li>
 *   <li>{@code xsdTypeNotUnique}: an XSD type is mapped at most once - the transformer applies only the first entry
 *       of a repeated type and ignores the rest (see the doc's "Multiple Type Mappings With the Same XSD Type")</li>
 *   <li>{@code a12TypeNotStringButStringTypeFilled} / {@code ...NumberButNumberTypeFilled} /
 *       {@code ...EnumerationButEnumerationTypeFilled}: a configuration group may only hold values when the
 *       super type of the chosen A12 data type is String / Number / Enumeration</li>
 * </ul>
 * The cross-field and range rules inside the groups are {@link TransformerTypeMappingConfigValidator}'s.
 */
public final class TransformerTypeMappingValidator implements ModelValidator {

  @Override
  public List<ModelValidationError> validate(A12Model<?> model, ValidationContext context) {
    if (!(model instanceof TransformerModel transformerModel) || transformerModel.getContent() == null) {
      return List.of();
    }
    List<TypeMappingEntry> entries = transformerModel.getContent().getTypeMappingOrEmpty();
    List<ModelValidationError> errors = new ArrayList<>();
    Set<String> seenXsdTypes = new HashSet<>();

    for (int index = 0; index < entries.size(); index++) {
      TypeMappingEntry entry = entries.get(index);

      String xsdType = entry.getXsdType();
      if (xsdType == null || xsdType.isBlank()) {
        errors.add(error(model, TransformerElementIds.typeMapping(index, "xsdType"),
            ValidationMessages.get("validation.transformer.typeMapping.xsdTypeMissing")));
      }
      else if (!seenXsdTypes.add(xsdType)) {
        errors.add(error(model, TransformerElementIds.typeMapping(index, "xsdType"),
            ValidationMessages.get("validation.transformer.typeMapping.xsdTypeDuplicate", xsdType)));
      }

      String a12Type = entry.getA12Type();
      A12DataType dataType = entry.getDataType();
      if (a12Type == null || a12Type.isBlank()) {
        errors.add(error(model, TransformerElementIds.typeMapping(index, "a12Type"),
            ValidationMessages.get("validation.transformer.typeMapping.a12TypeMissing")));
        continue;
      }
      if (dataType == null) {
        errors.add(error(model, TransformerElementIds.typeMapping(index, "a12Type"),
            ValidationMessages.get("validation.transformer.typeMapping.a12TypeUnknown", a12Type)));
        continue;
      }

      if (entry.getStringType() != null && !entry.getStringType().isEmpty() && !dataType.acceptsStringConfig()) {
        errors.add(error(model, TransformerElementIds.typeMapping(index, "a12Type"),
            ValidationMessages.get("validation.transformer.typeMapping.stringConfigNotAllowed", a12Type)));
      }
      if (entry.getNumberType() != null && !entry.getNumberType().isEmpty() && !dataType.acceptsNumberConfig()) {
        errors.add(error(model, TransformerElementIds.typeMapping(index, "a12Type"),
            ValidationMessages.get("validation.transformer.typeMapping.numberConfigNotAllowed", a12Type)));
      }
      if (entry.getEnumerationType() != null && !entry.getEnumerationType().isEmpty() && !dataType.acceptsEnumerationConfig()) {
        errors.add(error(model, TransformerElementIds.typeMapping(index, "a12Type"),
            ValidationMessages.get("validation.transformer.typeMapping.enumerationConfigNotAllowed", a12Type)));
      }
    }
    return errors;
  }

  private static ModelValidationError error(A12Model<?> model, String elementId, String message) {
    return new ModelValidationError(model, elementId, message, Severity.ERROR.name());
  }
}
