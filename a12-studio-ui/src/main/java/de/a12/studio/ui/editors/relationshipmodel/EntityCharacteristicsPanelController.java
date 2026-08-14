package de.a12.studio.ui.editors.relationshipmodel;

import de.a12.studio.models.relationshipmodel.EntityCharacteristic;
import de.a12.studio.ui.editors.AbstractPropertyEditor;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import org.jspecify.annotations.NonNull;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Edits an {@link EntityCharacteristic}'s Document Model, Role and Orderable flag. Embedded (alongside {@link
 * LinkConstraintsPanelController} and a Labels panel) in the entity add/edit dialog opened from {@link
 * RelatedEntitiesPanelController}. Not bound to a single {@link de.a12.studio.models.documentmodel.Element}, so
 * it follows the model-header binding pattern (manual listeners + an {@code updatingFromModel} guard) rather
 * than {@code bindTextField}/{@code bindComboBox}.
 */
public class EntityCharacteristicsPanelController extends AbstractPropertyEditor implements Initializable {

  @FXML
  private ComboBox<String> documentModelField;

  @FXML
  private TextField roleField;

  @FXML
  private CheckBox orderableField;

  private EntityCharacteristic entity;

  // Set while fields are being repopulated from the entity, so those programmatic updates aren't mistaken for
  // user edits and don't trigger a save.
  private boolean updatingFromModel;

  // Notified after every field edit, so the owning dialog can re-run its OK-button validation.
  private Runnable onChange = () -> {
  };

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    super.initialize(location, resources);

    documentModelField.valueProperty().addListener((observable, oldValue, newValue) -> {
      if (updatingFromModel || entity == null) {
        return;
      }
      entity.setDocumentModel(newValue);
      onChange.run();
      commitHeaderChange();
    });
    roleField.textProperty().addListener((observable, oldValue, newValue) -> {
      if (updatingFromModel || entity == null) {
        return;
      }
      entity.setRole(newValue == null || newValue.isBlank() ? null : newValue);
      onChange.run();
      commitHeaderChange();
    });
    orderableField.selectedProperty().addListener((observable, oldValue, newValue) -> {
      if (updatingFromModel || entity == null) {
        return;
      }
      entity.setOrdered(newValue);
      onChange.run();
      commitHeaderChange();
    });
  }

  public void setDocumentModelOptions(@NonNull List<String> options) {
    updatingFromModel = true;
    try {
      documentModelField.getItems().setAll(options);
    }
    finally {
      updatingFromModel = false;
    }
  }

  public void setEntity(@NonNull EntityCharacteristic entity) {
    this.entity = entity;
    updatingFromModel = true;
    try {
      documentModelField.setValue(entity.getDocumentModel());
      roleField.setText(entity.getRole() != null ? entity.getRole() : "");
      orderableField.setSelected(Boolean.TRUE.equals(entity.getOrdered()));
    }
    finally {
      updatingFromModel = false;
    }
  }

  public void setOnChange(@NonNull Runnable onChange) {
    this.onChange = onChange;
  }
}
