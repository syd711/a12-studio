package de.a12.studio.modelsvalidation.validators.transformer;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.transformermodel.CodeLists;
import de.a12.studio.models.transformermodel.TransformerConfiguration;
import de.a12.studio.models.transformermodel.TransformerModel;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.Severity;
import de.a12.studio.modelsvalidation.ValidationContext;
import de.a12.studio.modelsvalidation.ValidationMessages;
import de.a12.studio.modelsvalidation.validators.ModelValidator;

import java.util.ArrayList;
import java.util.List;

/**
 * The parts of a Transformer Model that SME's editor has no UI for but whose rules its meta model still enforces
 * (they apply to files written by hand), ported from {@code TransformerConfigModel} (29.4.0):
 * <ul>
 *   <li>{@code OnlyOneSupportedCharactersSourceGiven}: {@code Configuration.supportedCharacters} and
 *       {@code supportedCharactersTypeInXsd} exclude each other (the transformer fails otherwise)</li>
 *   <li>{@code uriVersionListValidationRule} / {@code elementNamesInXsdValidationRule}: once {@code CodeLists} holds
 *       anything, a {@code UriVersionList} and the {@code ElementNamesInXsd} are required</li>
 * </ul>
 */
public final class TransformerConfigurationValidator implements ModelValidator {

  @Override
  public List<ModelValidationError> validate(A12Model<?> model, ValidationContext context) {
    if (!(model instanceof TransformerModel transformerModel) || transformerModel.getContent() == null) {
      return List.of();
    }
    List<ModelValidationError> errors = new ArrayList<>();

    TransformerConfiguration configuration = transformerModel.getContent().getConfiguration();
    if (configuration != null && isFilled(configuration.getSupportedCharacters())
        && isFilled(configuration.getSupportedCharactersTypeInXsd())) {
      errors.add(error(model, TransformerElementIds.SUPPORTED_CHARACTERS,
          ValidationMessages.get("validation.transformer.configuration.supportedCharactersExclusive")));
    }

    CodeLists codeLists = transformerModel.getContent().getCodeLists();
    if (codeLists != null && isFilled(codeLists)) {
      if (isEmpty(codeLists.getUriVersionList())) {
        errors.add(error(model, TransformerElementIds.CODE_LISTS_URI_VERSION_LIST,
            ValidationMessages.get("validation.transformer.codeLists.uriVersionListMissing")));
      }
      if (isEmpty(codeLists.getElementNamesInXsd())) {
        errors.add(error(model, TransformerElementIds.CODE_LISTS_ELEMENT_NAMES,
            ValidationMessages.get("validation.transformer.codeLists.elementNamesMissing")));
      }
    }
    return errors;
  }

  /** SME's {@code GroupFilled(CodeLists)}: any list has an entry. */
  private static boolean isFilled(CodeLists codeLists) {
    return !isEmpty(codeLists.getCodeIdentifiers()) || !isEmpty(codeLists.getValueIdentifiersDe())
        || !isEmpty(codeLists.getValueIdentifiersEn()) || !isEmpty(codeLists.getElementNamesInXsd())
        || !isEmpty(codeLists.getUriVersionList());
  }

  private static boolean isEmpty(List<?> list) {
    return list == null || list.isEmpty();
  }

  private static boolean isFilled(String value) {
    return value != null && !value.isBlank();
  }

  private static ModelValidationError error(A12Model<?> model, String elementId, String message) {
    return new ModelValidationError(model, elementId, message, Severity.ERROR.name());
  }
}
