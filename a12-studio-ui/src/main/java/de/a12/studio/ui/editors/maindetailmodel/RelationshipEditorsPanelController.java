package de.a12.studio.ui.editors.maindetailmodel;

import de.a12.studio.models.masterdetailmodel.FormMapping;
import de.a12.studio.models.masterdetailmodel.MasterDetailModel;
import org.jspecify.annotations.NonNull;

import java.util.List;

/**
 * Edits a tree-type {@link MasterDetailModel}'s {@code relationshipEditors} list (mirrors SME's {@code
 * syncRelationshipEditors}): one row per Document Model referenced by a Tree Model node that carries an
 * {@code event_add_link} node action, each mapped to the Form Model (containing Relationship Binding views)
 * used to edit links to that node's children. Only relevant for {@code content.type == "tree"} — the owning
 * editor hides this panel and clears {@code content.relationshipEditors} otherwise.
 */
public class RelationshipEditorsPanelController extends AbstractDocumentFormMappingPanelController {

  @Override
  protected List<FormMapping> currentMappings() {
    List<FormMapping> relationshipEditors = model.getContent().getRelationshipEditors();
    return relationshipEditors != null ? relationshipEditors : List.of();
  }

  @Override
  protected void applyMappings(@NonNull List<FormMapping> mappings) {
    model.getContent().setRelationshipEditors(mappings);
  }

  @Override
  protected String rowIdPrefix() {
    return "relationshipEditorFormModel";
  }
}
