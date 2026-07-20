package de.a12.studio.ui.editors.propertyeditors;

import de.a12.studio.models.documentmodel.Element;
import de.a12.studio.ui.editors.AbstractPropertyEditor;
import de.a12.studio.ui.editors.dialogs.EditorDialogs;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import org.jspecify.annotations.NonNull;

import java.util.List;

public class GeneralInformationPanelController extends AbstractPropertyEditor {

  private static final List<String> DATA_TYPES = List.of("String", "Number", "Boolean", "Date", "Object");

  @FXML
  private TextField nameField;

  @FXML
  private TextField idField;

  @FXML
  private TextField pathField;

  private List<Element> ancestors = List.of();

  @FXML
  private void onEditName(ActionEvent event) {
    EditorDialogs.openSettings();
    setFieldValue(nameField, element.getName());
    updatePathField(element.getName());
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
