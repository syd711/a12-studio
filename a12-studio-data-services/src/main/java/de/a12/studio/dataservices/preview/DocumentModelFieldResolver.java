package de.a12.studio.dataservices.preview;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.Label;
import de.a12.studio.models.ModelReference;
import de.a12.studio.models.ModelType;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.documentmodel.Element;
import de.a12.studio.models.documentmodel.FieldElement;
import de.a12.studio.models.documentmodel.GroupElement;
import de.a12.studio.models.projects.ProjectItem;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Shared elementRef-resolution helpers for the wireframe previews ({@link ApplicationModelPreviewService},
 * {@link FormModelPreviewService}): both resolve a Document Model field reference (Overview Model column,
 * Form Model Control/repeat column) against the referenced Document Model's element tree to show a real
 * label/type instead of just the raw elementRef.
 */
public final class DocumentModelFieldResolver {

  private DocumentModelFieldResolver() {
  }

  /**
   * Resolves the Document Model referenced from {@code model}'s header via a {@code modelReferences} entry
   * with the given {@code purpose} (e.g. {@code document-model-for-overview}, {@link
   * ModelReference#PURPOSE_DATA_BINDING}), or {@code null} if no such reference exists or it doesn't resolve
   * to a Document Model within {@code contextItem}'s project. Shared by every preview service that needs to
   * look up "the Document Model this model is bound to" before resolving elementRefs against it.
   */
  public static DocumentModel resolveReferencedDocumentModel(A12Model<?> model, String purpose, ProjectItem contextItem) {
    if (model.getModelReferences() == null) {
      return null;
    }
    String documentModelId = model.getModelReferences().stream()
        .filter(reference -> reference.getModelType() == ModelType.DOCUMENT && purpose.equals(reference.getPurpose()))
        .map(ModelReference::getReference)
        .findFirst()
        .orElse(null);
    if (documentModelId == null) {
      return null;
    }
    ProjectItem documentItem = contextItem.findByModelId(documentModelId);
    return documentItem != null && documentItem.getModel() instanceof DocumentModel documentModel ? documentModel : null;
  }

  /**
   * Indexes every element of {@code documentModel} (recursively through groups) by id, or an empty map if
   * {@code documentModel} is {@code null}.
   */
  public static Map<String, Element> index(DocumentModel documentModel) {
    Map<String, Element> elementsById = new HashMap<>();
    if (documentModel != null) {
      indexElements(documentModel.getContent().getModelRoot().getRootGroups(), elementsById);
    }
    return elementsById;
  }

  private static void indexElements(List<? extends Element> elements, Map<String, Element> target) {
    for (Element element : elements) {
      target.put(element.getId(), element);
      if (element instanceof GroupElement groupElement) {
        indexElements(groupElement.getGroup().getElements(), target);
      }
    }
  }

  public static String fieldType(Element element) {
    if (element instanceof FieldElement fieldElement && fieldElement.getField().getFieldType() != null) {
      return fieldElement.getField().getFieldType().getType();
    }
    return null;
  }

  public static String fieldLabel(Element element) {
    if (element instanceof FieldElement fieldElement) {
      return firstLabelText(fieldElement.getField().getLabel());
    }
    return null;
  }

  public static String firstLabelText(List<Label> labels) {
    return labels != null && !labels.isEmpty() ? labels.get(0).getText() : null;
  }
}
