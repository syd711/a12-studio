package de.a12.studio.ui.editors.maindetailmodel;

import de.a12.studio.models.masterdetailmodel.FormMapping;
import de.a12.studio.models.masterdetailmodel.MasterDetailModel;
import org.jspecify.annotations.NonNull;

import java.util.List;

/**
 * Edits a tree-type {@link MasterDetailModel}'s {@code linkDocumentEditors} list, SME's "Additional Link
 * Fields" section (mirrors {@code syncLinkDocumentEditors}): one row per link Document Model declared by a
 * Relationship Model referenced by the Tree Model, each mapped to the Form Model used to edit that link's
 * fields. Only relevant for {@code content.type == "tree"} — the owning editor hides this panel and clears
 * {@code content.linkDocumentEditors} otherwise.
 */
public class LinkDocumentEditorsPanelController extends AbstractDocumentFormMappingPanelController {

  @Override
  protected List<FormMapping> currentMappings() {
    List<FormMapping> linkDocumentEditors = model.getContent().getLinkDocumentEditors();
    return linkDocumentEditors != null ? linkDocumentEditors : List.of();
  }

  @Override
  protected void applyMappings(@NonNull List<FormMapping> mappings) {
    model.getContent().setLinkDocumentEditors(mappings);
  }

  @Override
  protected String rowIdPrefix() {
    return "linkDocumentEditorFormModel";
  }
}
