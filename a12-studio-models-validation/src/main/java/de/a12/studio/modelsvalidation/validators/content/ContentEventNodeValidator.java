package de.a12.studio.modelsvalidation.validators.content;

import de.a12.studio.models.contentmodel.ContentElement;
import de.a12.studio.models.contentmodel.ContentElementLibrary;

import java.util.List;

/**
 * The actions an element can run itself (its {@code onClick} / {@code onRowClick} event node, or such an element in the
 * tree): Save, Commit, Cancel and Delete Row work on the bound document and are an error without a Document Model, as in
 * SME. The Add Row action's group is checked by {@link ContentGroupReferenceValidator}.
 */
public final class ContentEventNodeValidator extends AbstractContentNodeValidator {

  private static final List<String> NEED_DOCUMENT_MODEL = List.of("SaveAction", "CommitAction", "CancelAction", "DeleteRowAction");

  @Override
  void check(ContentNodeWalker.NodeInfo info, Findings findings) {
    ContentElement element = info.element();
    if (ContentElementLibrary.NAMESPACE.equals(ContentNodeWalker.namespace(element)) && element.getType() != null
        && NEED_DOCUMENT_MODEL.contains(element.getType()) && info.document().structure() == null) {
      findings.error("validation.contentReference.noDocumentModel");
    }
  }
}
