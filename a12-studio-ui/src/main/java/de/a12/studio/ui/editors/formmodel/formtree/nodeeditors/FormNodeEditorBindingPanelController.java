package de.a12.studio.ui.editors.formmodel.formtree.nodeeditors;

import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.formmodel.Binding;
import de.a12.studio.models.formmodel.BindingContent;
import de.a12.studio.models.formmodel.BindingDetails;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.models.relationshipmodel.EntityCharacteristic;
import de.a12.studio.models.relationshipmodel.RelationshipModel;
import de.a12.studio.ui.editors.AbstractPropertyEditor;
import de.a12.studio.ui.util.ProjectDocumentModels;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Editor for a selected {@link Binding} node (created by dragging a row from the Form Model editor's
 * Relationships panel onto the tree - see {@code RelationshipModelPanelController}/{@code
 * FormModelTreeController#dropRelationshipModel}): its display name ({@link BindingDetails#getName()}), which
 * {@link RelationshipModel} it's wired to, and which of that relationship's entity roles ({@link
 * EntityCharacteristic#getRole()}) it shows/edits - the two fields {@code
 * FormBindingRelationshipReferenceValidator}/{@code FormBindingTargetRoleValidator} require. Mirrors {@code
 * RelationshipReferencePanelController} (Relationship UI Model's own relationship/target-role picker), adapted
 * to this tree's {@link AbstractPropertyEditor}-based node editor convention (see {@code
 * FormNodeEditorTextCellPanelController}) instead of a model-header panel.
 */
public class FormNodeEditorBindingPanelController extends AbstractPropertyEditor implements Initializable {

  @FXML
  private TextField nameField;
  @FXML
  private ComboBox<String> relationshipCombo;
  @FXML
  private ComboBox<String> targetRoleCombo;

  private Binding binding;
  private List<RelationshipModel> relationshipModels = List.of();

  // Guards relationshipCombo/targetRoleCombo's own listeners below (they're wired manually, not through
  // bindComboBox, since selecting a relationship must also reset the target role combo's items as one atomic
  // step) - separate from AbstractPropertyEditor's own private "updating from model" flag, which still guards
  // nameField via bindTextField/setFieldValue as usual.
  private boolean updatingFromModel;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    super.initialize(location, resources);
    bindTextField(nameField, (el, value) -> details().setName(value));

    relationshipCombo.valueProperty().addListener((observable, oldValue, newValue) -> {
      if (updatingFromModel) {
        return;
      }
      // The target role only makes sense for the newly selected relationship, so it's reset rather than kept.
      withModelUpdateGuard(() -> populateTargetRoleOptions(newValue, null));
      applySelection();
    });
    targetRoleCombo.valueProperty().addListener((observable, oldValue, newValue) -> {
      if (updatingFromModel) {
        return;
      }
      applySelection();
    });
  }

  public void setBinding(@NonNull Binding binding, @Nullable DocumentModel documentModel, @NonNull ProjectItem projectItem) {
    this.binding = binding;
    this.relationshipModels = ProjectDocumentModels.getRelationshipModelsConnectedTo(
        projectItem, documentModel == null ? null : documentModel.getId());
    BindingDetails details = details();

    setFieldValue(nameField, details.getName());
    withModelUpdateGuard(() -> {
      relationshipCombo.getItems().setAll(relevantRelationshipIds(details.getRelationshipName()));
      relationshipCombo.setValue(details.getRelationshipName());
      populateTargetRoleOptions(details.getRelationshipName(), details.getTargetRole());
    });
  }

  /**
   * The candidate relationship ids to offer, sorted: every {@link RelationshipModel} connected to the bound
   * Document Model (same list {@code RelationshipModelPanelController} drags rows from), plus - if it isn't
   * already among them - the currently stored {@code relationshipName}, so a dangling/unrelated reference
   * (already flagged by {@code FormBindingRelationshipReferenceValidator}) stays visible and selectable instead
   * of silently vanishing from the combo.
   */
  private List<String> relevantRelationshipIds(@Nullable String currentRelationshipName) {
    List<String> ids = new ArrayList<>(relationshipModels.stream().map(RelationshipModel::getId).toList());
    if (currentRelationshipName != null && !currentRelationshipName.isBlank() && !ids.contains(currentRelationshipName)) {
      ids.add(currentRelationshipName);
    }
    ids.sort(Comparator.naturalOrder());
    return ids;
  }

  private void populateTargetRoleOptions(@Nullable String relationshipName, @Nullable String valueToSelect) {
    List<String> roles = findRelationship(relationshipName)
        .map(relationship -> relationship.getContent().getEntityCharacteristics().stream()
            .map(EntityCharacteristic::getRole)
            .toList())
        .orElse(List.of());
    targetRoleCombo.getItems().setAll(roles);
    targetRoleCombo.setValue(valueToSelect);
  }

  private Optional<RelationshipModel> findRelationship(@Nullable String id) {
    return relationshipModels.stream().filter(relationship -> relationship.getId().equals(id)).findFirst();
  }

  private void applySelection() {
    details().setRelationshipName(relationshipCombo.getValue());
    details().setTargetRole(targetRoleCombo.getValue());
    commitChange();
  }

  private void withModelUpdateGuard(@NonNull Runnable action) {
    boolean previous = updatingFromModel;
    updatingFromModel = true;
    try {
      action.run();
    }
    finally {
      updatingFromModel = previous;
    }
  }

  private BindingDetails details() {
    BindingContent content = binding.getBinding();
    if (content == null) {
      content = new BindingContent();
      binding.setBinding(content);
    }
    if (content.getDetails() == null) {
      content.setDetails(new BindingDetails());
    }
    return content.getDetails();
  }
}
