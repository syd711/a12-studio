package de.a12.studio.modelsvalidation.validators.content;

import de.a12.studio.models.contentmodel.ContentElement;
import de.a12.studio.models.contentmodel.ContentElementLibrary;
import de.a12.studio.models.contentmodel.ContentProps;

/**
 * The group references of a Content Model, as SME's {@code groupReferenceValidator} checks them: a Repeatable Group's
 * {@code groupId}, an Add Row Action's {@code groupId} (only groups that can be added to, i.e. repeatable ones) and an
 * Image's dynamic source ({@code src.dynamic}, the group of an attachment field). Each needs the Document Model, must
 * name a group of it, and that group must lie below the group the element is relative to (the base group or the closest
 * enclosing Repeatable Group).
 */
public final class ContentGroupReferenceValidator extends AbstractContentNodeValidator {

  @Override
  void check(ContentNodeWalker.NodeInfo info, Findings findings) {
    ContentElement element = info.element();
    if (!ContentElementLibrary.NAMESPACE.equals(ContentNodeWalker.namespace(element)) || element.getType() == null) {
      return;
    }
    boolean addRow = "AddRowAction".equals(element.getType());
    String groupId;
    switch (element.getType()) {
      case "Group", "AddRowAction" -> groupId = new ContentProps(element).getString("groupId");
      case "Image" -> {
        groupId = new ContentProps(element).getString("src.dynamic");
        if (groupId == null || groupId.isEmpty()) {
          return;
        }
      }
      default -> {
        return;
      }
    }
    if (groupId == null || groupId.isBlank()) {
      findings.error("validation.contentGroupReference.missing");
      return;
    }
    DocumentStructure structure = info.document().structure();
    if (structure == null) {
      findings.error("validation.contentReference.noDocumentModel");
      return;
    }
    DocumentStructure.Node group = structure.find(groupId);
    if (group == null) {
      findings.error("validation.contentGroupReference.notFound", groupId, info.document().documentModelId());
    }
    else if (!group.isGroup()) {
      findings.error("validation.contentGroupReference.notAGroup", groupId);
    }
    else if (!DocumentStructure.isCandidateGroup(info.context(), group)) {
      findings.error("validation.contentGroupReference.notInContext", groupId, info.context().path());
    }
    else if (addRow && !group.isRepeated()) {
      findings.error("validation.contentGroupReference.notRepeatable", groupId);
    }
  }
}
