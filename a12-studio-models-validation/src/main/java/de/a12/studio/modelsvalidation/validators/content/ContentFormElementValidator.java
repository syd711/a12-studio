package de.a12.studio.modelsvalidation.validators.content;

import de.a12.studio.models.contentmodel.ContentElement;
import de.a12.studio.models.contentmodel.ContentElementLibrary;

/**
 * The form elements of a Content Model (Text Line, Checkbox, Select, ...), as SME's shared form element validator and
 * the check of each element type make them: the element needs the Document Model and an {@code elementId} that names an
 * element of it, whose data type the form element can show, and that is not repeated more often than the data context
 * of the form element's position ("The Document Model element ... is not compatible with the current data context").
 * A form element cannot have children (a warning).
 */
public final class ContentFormElementValidator extends AbstractContentNodeValidator {

  @Override
  void check(ContentNodeWalker.NodeInfo info, Findings findings) {
    ContentElement element = info.element();
    if (info.owner() != null || !ContentElementLibrary.FORM_ELEMENTS_NAMESPACE.equals(ContentNodeWalker.namespace(element))
        || !ContentFormElementTypes.isInputElement(element.getType())) {
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
    if (!ContentFormElementTypes.fitsContext(info.context(), node)) {
      findings.error("validation.contentFormElement.incompatibleContext", node.path(),
          info.context() != null ? info.context().path() : "/");
      return;
    }
    String supported = ContentFormElementTypes.unsupported(element.getType(), node);
    if (supported != null) {
      findings.error("validation.contentFormElement.wrongType", node.path(), supported);
    }
  }
}
