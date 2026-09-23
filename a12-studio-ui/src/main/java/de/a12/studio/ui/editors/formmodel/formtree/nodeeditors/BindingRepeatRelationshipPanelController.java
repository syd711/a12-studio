package de.a12.studio.ui.editors.formmodel.formtree.nodeeditors;

import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.formmodel.BindingContent;
import de.a12.studio.models.formmodel.BindingDetails;
import de.a12.studio.models.formmodel.BindingRepeat;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.models.relationshipmodel.EntityCharacteristic;
import de.a12.studio.models.relationshipmodel.RelationshipModel;
import de.a12.studio.ui.editors.AbstractPropertyEditor;
import de.a12.studio.ui.editors.propertyeditors.BindingComponentPanelController;
import de.a12.studio.ui.util.ProjectDocumentModels;
import de.a12.studio.ui.util.StudioBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
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
 * Editor for a selected {@link BindingRepeat} node's relationship linkage and UI-component configuration - the
 * same fields {@code FormNodeEditorBindingPanelController} edits for a plain {@link
 * de.a12.studio.models.formmodel.Binding}, adapted here for {@link BindingRepeat#getBinding()}, which (unlike
 * {@code Binding#getBinding()}) is nullable and lazily created on first edit.
 */
public class BindingRepeatRelationshipPanelController extends AbstractPropertyEditor implements Initializable {

  @FXML
  private TextField nameField;
  @FXML
  private ComboBox<String> relationshipCombo;
  @FXML
  private ComboBox<String> targetRoleCombo;
  @FXML
  private CheckBox isFixedRelationshipCheckBox;
  @FXML
  private CheckBox cdmChildActivitiesEnabledCheckBox;
  @FXML
  private BindingComponentPanelController mainComponentController;
  @FXML
  private BindingComponentPanelController editModalComponentController;

  private BindingRepeat bindingRepeat;
  private List<RelationshipModel> relationshipModels = List.of();

  private boolean updatingFromModel;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    super.initialize(location, resources);
    bindTextField(nameField, (el, value) -> details().setName(value));
    bindCheckBox(isFixedRelationshipCheckBox, (el, value) -> details().setIsFixedRelationship(value));
    bindCheckBox(cdmChildActivitiesEnabledCheckBox, (el, value) -> details().setCdmChildActivitiesEnabled(value));
    mainComponentController.configure(StudioBundle.get("binding_component_panel.main_component"), ".mainComponent");
    editModalComponentController.configure(StudioBundle.get("binding_component_panel.edit_modal_component"), ".editModalComponent");

    relationshipCombo.valueProperty().addListener((observable, oldValue, newValue) -> {
      if (updatingFromModel) {
        return;
      }
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

  public void setBindingRepeat(@NonNull BindingRepeat bindingRepeat, @Nullable DocumentModel documentModel, @NonNull ProjectItem projectItem) {
    this.bindingRepeat = bindingRepeat;
    this.relationshipModels = ProjectDocumentModels.getRelationshipModelsConnectedTo(
        projectItem, documentModel == null ? null : documentModel.getId());
    BindingDetails details = details();

    setFieldValue(nameField, details.getName());
    setFieldValue(isFixedRelationshipCheckBox, Boolean.TRUE.equals(details.getIsFixedRelationship()));
    setFieldValue(cdmChildActivitiesEnabledCheckBox, Boolean.TRUE.equals(details.getCdmChildActivitiesEnabled()));
    withModelUpdateGuard(() -> {
      relationshipCombo.getItems().setAll(relevantRelationshipIds(details.getRelationshipName()));
      relationshipCombo.setValue(details.getRelationshipName());
      populateTargetRoleOptions(details.getRelationshipName(), details.getTargetRole());
    });
    mainComponentController.setComponent(details::getMainComponent, details::setMainComponent, projectItem);
    editModalComponentController.setComponent(details::getEditModalComponent, details::setEditModalComponent, projectItem);
  }

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

  private BindingContent content() {
    BindingContent content = bindingRepeat.getBinding();
    if (content == null) {
      content = new BindingContent();
      content.setType("relationship");
      bindingRepeat.setBinding(content);
    }
    return content;
  }

  private BindingDetails details() {
    BindingContent content = content();
    if (content.getDetails() == null) {
      content.setDetails(new BindingDetails());
    }
    return content.getDetails();
  }
}
