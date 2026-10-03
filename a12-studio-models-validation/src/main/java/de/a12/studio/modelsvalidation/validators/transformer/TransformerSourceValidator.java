package de.a12.studio.modelsvalidation.validators.transformer;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.projects.ProjectResources;
import de.a12.studio.models.transformermodel.TransformerCmd;
import de.a12.studio.models.transformermodel.TransformerModel;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.Severity;
import de.a12.studio.modelsvalidation.ValidationContext;
import de.a12.studio.modelsvalidation.ValidationMessages;
import de.a12.studio.modelsvalidation.validators.ModelValidator;

import java.util.ArrayList;
import java.util.List;

/**
 * The Transformation tab's "General" section: the XML Structure Definition File ({@code Cmd.mainXsd}) and the Root
 * Element ({@code Cmd.rootElement}) are required, as SME's {@code requirednessConfig} on both fields says. SME's
 * file field is an autocomplete that also accepts a name that is not (yet) a resource - the transformation then fails
 * and reports it; here a main XSD that is not among the project's XSD files is an error up front, because without it
 * the Document Model cannot be generated.
 */
public final class TransformerSourceValidator implements ModelValidator {

  private static final String XSD_EXTENSION = "xsd";

  @Override
  public List<ModelValidationError> validate(A12Model<?> model, ValidationContext context) {
    if (!(model instanceof TransformerModel transformerModel) || transformerModel.getContent() == null) {
      return List.of();
    }
    TransformerCmd cmd = transformerModel.getContent().getCmd();
    List<ModelValidationError> errors = new ArrayList<>();

    String mainXsd = cmd == null ? null : cmd.getMainXsd();
    if (mainXsd == null || mainXsd.isBlank()) {
      errors.add(error(model, TransformerElementIds.MAIN_XSD, ValidationMessages.get("validation.transformer.mainXsd.missing")));
    }
    else if (context.project() != null && context.project().getFolder() != null
        && ProjectResources.findByName(ProjectResources.findFiles(context.project().getFolder(), XSD_EXTENSION), mainXsd) == null) {
      errors.add(error(model, TransformerElementIds.MAIN_XSD, ValidationMessages.get("validation.transformer.mainXsd.notFound", mainXsd)));
    }

    String rootElement = cmd == null ? null : cmd.getRootElement();
    if (rootElement == null || rootElement.isBlank()) {
      errors.add(error(model, TransformerElementIds.ROOT_ELEMENT, ValidationMessages.get("validation.transformer.rootElement.missing")));
    }
    return errors;
  }

  private static ModelValidationError error(A12Model<?> model, String elementId, String message) {
    return new ModelValidationError(model, elementId, message, Severity.ERROR.name());
  }
}
