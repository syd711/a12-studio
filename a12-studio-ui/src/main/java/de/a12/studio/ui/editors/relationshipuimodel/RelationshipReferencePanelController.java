package de.a12.studio.ui.editors.relationshipuimodel;

import de.a12.studio.models.relationshipmodel.EntityCharacteristic;
import de.a12.studio.models.relationshipmodel.RelationshipModel;
import de.a12.studio.models.relationshipuimodel.RelationshipUiModel;
import de.a12.studio.ui.components.ErrorContainerController;
import de.a12.studio.ui.util.StudioBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import org.jspecify.annotations.NonNull;

import java.net.URL;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Edits a {@link RelationshipUiModel}'s {@code content.relationshipName}/{@code content.targetRole}: which
 * {@link RelationshipModel} this UI presents, and which of its entity roles ({@link
 * EntityCharacteristic#getRole()}) is the "target" (the side the picker adds/removes links to). Not wired
 * through {@link de.a12.studio.ui.editors.AbstractPropertyEditor} for the same reason as {@link
 * de.a12.studio.ui.editors.maindetailmodel.MainModelReferencePanelController}: it edits two content fields
 * directly, not a document-model {@link de.a12.studio.models.documentmodel.Element}.
 */
public class RelationshipReferencePanelController implements Initializable {

  @FXML
  private ComboBox<String> relationshipField;
  @FXML
  private ComboBox<String> targetRoleField;
  @FXML
  private ErrorContainerController errorContainerController;

  private RelationshipUiModel model;
  private List<RelationshipModel> relationshipModels = List.of();

  // Set while fields are being repopulated from the model, so those programmatic updates aren't mistaken for
  // user edits and don't trigger onChange/a save.
  private boolean updatingFromModel;

  private Runnable onChange = () -> {
  };

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    relationshipField.valueProperty().addListener((observable, oldValue, newValue) -> {
      if (updatingFromModel) {
        return;
      }
      boolean wasUpdating = updatingFromModel;
      updatingFromModel = true;
      try {
        // The target role only makes sense for the newly selected relationship, so it's reset rather than kept.
        populateTargetRoleOptions(newValue, null);
      }
      finally {
        updatingFromModel = wasUpdating;
      }
      applySelection();
      onChange.run();
    });
    targetRoleField.valueProperty().addListener((observable, oldValue, newValue) -> {
      validate();
      if (updatingFromModel) {
        return;
      }
      applySelection();
      onChange.run();
    });
  }

  public void setOnChange(@NonNull Runnable onChange) {
    this.onChange = onChange;
  }

  public void load(@NonNull RelationshipUiModel model, @NonNull List<RelationshipModel> relationshipModels) {
    this.model = model;
    this.relationshipModels = relationshipModels;

    updatingFromModel = true;
    try {
      relationshipField.getItems().setAll(relationshipModels.stream()
          .map(RelationshipModel::getId)
          .sorted(Comparator.naturalOrder())
          .toList());
      relationshipField.setValue(model.getContent().getRelationshipName());
      populateTargetRoleOptions(model.getContent().getRelationshipName(), model.getContent().getTargetRole());
    }
    finally {
      updatingFromModel = false;
    }
    validate();
  }

  private void populateTargetRoleOptions(String relationshipName, String valueToSelect) {
    List<String> roles = findRelationship(relationshipName)
        .map(relationship -> relationship.getContent().getEntityCharacteristics().stream()
            .map(EntityCharacteristic::getRole)
            .toList())
        .orElse(List.of());
    targetRoleField.getItems().setAll(roles);
    targetRoleField.setValue(valueToSelect);
  }

  private Optional<RelationshipModel> findRelationship(String id) {
    return relationshipModels.stream().filter(relationship -> relationship.getId().equals(id)).findFirst();
  }

  private void applySelection() {
    model.getContent().setRelationshipName(relationshipField.getValue());
    model.getContent().setTargetRole(targetRoleField.getValue());
  }

  private void validate() {
    if (relationshipField.getValue() == null) {
      errorContainerController.show("ERROR", StudioBundle.get("select_a_relationship"));
    }
    else if (targetRoleField.getValue() == null) {
      errorContainerController.show("ERROR", StudioBundle.get("select_a_target_role"));
    }
    else {
      errorContainerController.hide();
    }
  }
}
