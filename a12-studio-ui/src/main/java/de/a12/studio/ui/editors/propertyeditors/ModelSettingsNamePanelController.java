package de.a12.studio.ui.editors.propertyeditors;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.documentmodel.Element;
import de.a12.studio.ui.editors.AbstractPropertyEditor;
import de.a12.studio.ui.util.NameConventionValidation;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import org.jspecify.annotations.NonNull;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Edits the model's name ({@link A12Model#getId()}) and internal description, and shows the model
 * version read-only. Not bound to a single {@link Element}
 * (these fields live on the model itself), so {@link #setElement} is never called and only {@link #setModel}
 * is used.
 */
public class ModelSettingsNamePanelController extends AbstractPropertyEditor implements Initializable {

  @FXML
  private TextField nameField;

  @FXML
  private TextField versionField;

  @FXML
  private TextArea descriptionArea;

  private A12Model<?> model;

  // Set while setModel() is repopulating nameField from the model, so the listener below doesn't mistake
  // that programmatic change for a user edit and re-validate/commit it.
  private boolean updatingFromModel;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    super.initialize(location, resources);

    nameField.textProperty().addListener((observable, oldValue, newValue) -> {
      if (updatingFromModel) {
        return;
      }
      model.setId(newValue);
      Optional<String> error = NameConventionValidation.validate("Model name", newValue);
      error.ifPresentOrElse(message -> showError("ERROR", message), this::hideError);
      commitHeaderChange();
    });
    bindTextArea(descriptionArea, (element, value) -> model.setDescription(value));
  }

  public void setModel(@NonNull A12Model<?> model) {
    this.model = model;
    updatingFromModel = true;
    try {
      nameField.setText(model.getId());
    } finally {
      updatingFromModel = false;
    }
    versionField.setText(model.getModelVersion());
    setFieldValue(descriptionArea, model.getDescription());
    hideError();
  }

  public void focusNameField() {
    Platform.runLater(() -> {
      nameField.requestFocus();
      nameField.selectAll();
    });
  }
}
