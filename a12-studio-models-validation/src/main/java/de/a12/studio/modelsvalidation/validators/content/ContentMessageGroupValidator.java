package de.a12.studio.modelsvalidation.validators.content;

import de.a12.studio.models.contentmodel.ContentElement;
import de.a12.studio.models.contentmodel.ContentElementLibrary;

import java.util.List;

/**
 * The Message Group Container, as SME's validator for it checks: the fields and groups it lists must exist in the
 * Document Model and be of the right kind (a field where a field is expected, a group where a group is), and listing
 * anything needs the Document Model. Unlike the references of other elements they need not lie in the data context of
 * the container: it only reports about them. The rules are free text, nothing to check.
 */
public final class ContentMessageGroupValidator extends AbstractContentNodeValidator {

  @Override
  void check(ContentNodeWalker.NodeInfo info, Findings findings) {
    ContentElement element = info.element();
    if (info.owner() != null || !"MessageGroupContainer".equals(element.getType())
        || !ContentElementLibrary.FORM_ELEMENTS_NAMESPACE.equals(ContentNodeWalker.namespace(element))) {
      return;
    }
    List<?> fields = list(element, "fields");
    List<?> groups = list(element, "groups");
    List<?> rules = list(element, "rules");
    if (fields.isEmpty() && groups.isEmpty() && rules.isEmpty()) {
      return;
    }
    DocumentStructure structure = info.document().structure();
    if (structure == null) {
      findings.error("validation.contentReference.noDocumentModel");
      return;
    }
    for (Object entry : fields) {
      DocumentStructure.Node node = resolve(entry, structure, info, findings, false);
      if (node != null && !node.isField()) {
        findings.error("validation.contentFieldReference.notAField", node.id);
      }
    }
    for (Object entry : groups) {
      DocumentStructure.Node node = resolve(entry, structure, info, findings, true);
      if (node != null && !node.isGroup()) {
        findings.error("validation.contentGroupReference.notAGroup", node.id);
      }
    }
  }

  private static DocumentStructure.Node resolve(Object entry, DocumentStructure structure, ContentNodeWalker.NodeInfo info,
      Findings findings, boolean group) {
    String id = entry instanceof String string ? string : null;
    if (id == null || id.isBlank()) {
      findings.error(group ? "validation.contentGroupReference.missing" : "validation.contentFieldReference.missing");
      return null;
    }
    DocumentStructure.Node node = structure.find(id);
    if (node == null) {
      findings.error(group ? "validation.contentGroupReference.notFound" : "validation.contentFieldReference.notFound", id,
          info.document().documentModelId());
    }
    return node;
  }

  private static List<?> list(ContentElement element, String key) {
    return element.getProps() != null && element.getProps().get(key) instanceof List<?> list ? list : List.of();
  }
}
