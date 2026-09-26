package de.a12.studio.modelsvalidation.validators.content;

import de.a12.studio.models.contentmodel.ContentElement;
import de.a12.studio.models.contentmodel.ContentElementLibrary;
import de.a12.studio.models.contentmodel.ContentNodes;

import java.util.List;
import java.util.Map;

/**
 * The field references of a Content Model, as SME's {@code fieldReferenceValidator} checks them: a Field Output's
 * {@code fieldId}, the {@code fieldId} of every condition of a Conditional, and the field and group references inside
 * the text of any element (Paragraph, Heading, ...). Each needs the Document Model, must name a field of it (a group,
 * for a group reference in a text), and that field must be reachable from the data context of the element: the fields of
 * the base group or the enclosing Repeatable Group and of the groups above it, without descending into repeated groups.
 */
public final class ContentFieldReferenceValidator extends AbstractContentNodeValidator {

  @Override
  void check(ContentNodeWalker.NodeInfo info, Findings findings) {
    ContentElement element = info.element();
    if (info.owner() == null && ContentElementLibrary.NAMESPACE.equals(ContentNodeWalker.namespace(element))) {
      if ("FieldOutput".equals(element.getType())) {
        checkField(info, findings, element.getProps() != null ? element.getProps().get("fieldId") : null);
      }
      else if ("Conditional".equals(element.getType())) {
        conditionFieldIds(element).forEach(fieldId -> checkField(info, findings, fieldId));
      }
    }
    if (info.owner() == null) {
      checkTextReferences(info, findings);
    }
  }

  private static List<Object> conditionFieldIds(ContentElement element) {
    if (element.getProps() == null || !(element.getProps().get("conditions") instanceof List<?> conditions)) {
      return List.of();
    }
    return conditions.stream().<Object>map(condition -> condition instanceof Map<?, ?> map ? map.get("fieldId") : null).toList();
  }

  private void checkField(ContentNodeWalker.NodeInfo info, Findings findings, Object value) {
    String fieldId = value instanceof String string ? string : null;
    if (fieldId == null || fieldId.isBlank()) {
      findings.error("validation.contentFieldReference.missing");
      return;
    }
    DocumentStructure structure = info.document().structure();
    if (structure == null) {
      findings.error("validation.contentReference.noDocumentModel");
      return;
    }
    DocumentStructure.Node field = structure.find(fieldId);
    if (field == null) {
      findings.error("validation.contentFieldReference.notFound", fieldId, info.document().documentModelId());
    }
    else if (!field.isField()) {
      findings.error("validation.contentFieldReference.notAField", fieldId);
    }
    else if (!DocumentStructure.isCandidateField(info.context(), field)) {
      findings.error("validation.contentFieldReference.notInContext", fieldId, field.path(),
          info.context() != null ? info.context().path() : "/");
    }
  }

  private void checkTextReferences(ContentNodeWalker.NodeInfo info, Findings findings) {
    List<ContentNodes.LexicalReference> references = ContentNodes.lexicalReferences(info.element());
    if (references.isEmpty()) {
      return;
    }
    DocumentStructure structure = info.document().structure();
    if (structure == null) {
      findings.error("validation.contentReference.noDocumentModel");
      return;
    }
    for (ContentNodes.LexicalReference reference : references) {
      if (!reference.group()) {
        checkField(info, findings, reference.fieldId());
        continue;
      }
      DocumentStructure.Node group = structure.find(reference.fieldId());
      if (group == null) {
        findings.error("validation.contentGroupReference.notFound", reference.fieldId(), info.document().documentModelId());
      }
      else if (!group.isGroup()) {
        findings.error("validation.contentGroupReference.notAGroup", reference.fieldId());
      }
      else if (!DocumentStructure.isCandidateGroup(info.context(), group)) {
        findings.error("validation.contentGroupReference.notInContext", reference.fieldId(),
            info.context() != null ? info.context().path() : "/");
      }
    }
  }
}
