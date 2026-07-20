package de.a12.studio.ui.editors.propertyeditors;

import de.a12.studio.models.documentmodel.Element;
import de.a12.studio.ui.editors.AbstractPropertyEditor;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import org.jspecify.annotations.NonNull;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class FieldInformationPanelController extends AbstractPropertyEditor implements Initializable {

  private static final List<String> DATA_TYPES = List.of("String", "Number", "Boolean", "Date", "Object");

  @FXML
  private TextField nameField;

  @FXML
  private TextField idField;

  @FXML
  private TextField pathField;

  private List<Element> ancestors = List.of();

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    super.initialize(url, resourceBundle);
    bindTextField(nameField, Element::setName);
    nameField.textProperty().addListener((observable, oldValue, newValue) -> updatePathField(newValue));
  }

  public void setAncestors(@NonNull List<Element> ancestors) {
    this.ancestors = ancestors;
  }

  public void focusNameField() {
    Platform.runLater(() -> {
      nameField.requestFocus();
      nameField.selectAll();
    });
  }

  @Override
  public void setElement(@NonNull Element element) {
    super.setElement(element);
    setFieldValue(nameField, element.getName());
    idField.setText(element.getId());
    updatePathField(element.getName());
  }

  private void updatePathField(String name) {
    StringBuilder path = new StringBuilder();
    for (Element ancestor : ancestors) {
      path.append("/").append(ancestor.getName());
    }
    path.append("/").append(name == null ? "" : name);
    pathField.setText(path.toString());
  }
}
