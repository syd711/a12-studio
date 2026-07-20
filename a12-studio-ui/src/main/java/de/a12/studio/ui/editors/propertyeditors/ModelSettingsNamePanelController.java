package de.a12.studio.ui.editors.propertyeditors;

import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.documentmodel.Element;
import de.a12.studio.ui.editors.AbstractPropertyEditor;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import org.jspecify.annotations.NonNull;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Edits the model's name ({@link DocumentModel#getId()}) and internal description, and shows the model
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

  private DocumentModel model;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    super.initialize(location, resources);

    bindTextField(nameField, (element, value) -> model.setId(value));
    bindTextArea(descriptionArea, (element, value) -> model.setDescription(value));
  }

  public void setModel(@NonNull DocumentModel model) {
    this.model = model;
    setFieldValue(nameField, model.getId());
    versionField.setText(model.getModelVersion());
    setFieldValue(descriptionArea, model.getDescription());
  }
}
