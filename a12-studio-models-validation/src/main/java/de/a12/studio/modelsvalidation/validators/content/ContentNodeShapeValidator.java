package de.a12.studio.modelsvalidation.validators.content;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.contentmodel.ContentElementLibrary;
import de.a12.studio.models.contentmodel.ContentModel;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.Severity;
import de.a12.studio.modelsvalidation.ValidationContext;
import de.a12.studio.modelsvalidation.validators.ModelValidator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * What SME's JSON schema of the Content Model demands of every element: an {@code id}, a {@code type}, a {@code
 * namespace} and {@code props} (an element without props is invalid even if it needs none). And what the Content Engine
 * checks when it renders the model: the version of the Content Engine's own namespace in {@code
 * content.configuration.namespaceVersions} has to be the engine's current one - it compares it with the version of its
 * element library and logs a "namespace version mismatch" otherwise, and SME offers to migrate such a model (a warning,
 * reported once, on the first element of that namespace).
 */
public final class ContentNodeShapeValidator implements ModelValidator {

  @Override
  public List<ModelValidationError> validate(A12Model<?> model, ValidationContext context) {
    if (!(model instanceof ContentModel contentModel)) {
      return List.of();
    }
    List<ModelValidationError> errors = new ArrayList<>();
    Set<String> reported = new HashSet<>();
    ContentTree.forEach(contentModel, (element, ancestors) -> {
      if (element.getId() == null || element.getId().isBlank()) {
        // Nothing to report it against; the model-level root validator covers a root without id.
        return;
      }
      if (element.getType() == null || element.getType().isBlank()) {
        errors.add(ContentTree.finding(contentModel, element, Severity.ERROR, "validation.contentNode.missingProperty", "type"));
      }
      if (element.getNamespace() == null || element.getNamespace().isBlank()) {
        errors.add(ContentTree.finding(contentModel, element, Severity.ERROR, "validation.contentNode.missingProperty", "namespace"));
      }
      else if (ContentElementLibrary.NAMESPACE.equals(element.getNamespace()) && reported.add(element.getNamespace())) {
        String version = version(contentModel);
        if (version == null) {
          errors.add(ContentTree.finding(contentModel, element, Severity.WARNING, "validation.contentNode.missingNamespaceVersion",
              element.getNamespace(), ContentElementLibrary.NAMESPACE_VERSION));
        }
        else if (!version.equals(ContentElementLibrary.NAMESPACE_VERSION)) {
          errors.add(ContentTree.finding(contentModel, element, Severity.WARNING, "validation.contentNode.otherNamespaceVersion",
              element.getNamespace(), version, ContentElementLibrary.NAMESPACE_VERSION));
        }
      }
      if (element.getProps() == null) {
        errors.add(ContentTree.finding(contentModel, element, Severity.ERROR, "validation.contentNode.missingProperty", "props"));
      }
    });
    return errors;
  }

  private static String version(ContentModel model) {
    return model.getContent().getConfiguration() != null && model.getContent().getConfiguration().getNamespaceVersions() != null
        ? model.getContent().getConfiguration().getNamespaceVersions().get(ContentElementLibrary.NAMESPACE)
        : null;
  }
}
