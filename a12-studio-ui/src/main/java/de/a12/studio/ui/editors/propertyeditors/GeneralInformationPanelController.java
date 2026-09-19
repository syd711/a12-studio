package de.a12.studio.ui.editors.propertyeditors;

import de.a12.studio.models.documentmodel.Element;
import de.a12.studio.modelsvalidation.ElementProperty;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.editors.AbstractPropertyEditor;
import de.a12.studio.ui.util.NameConventionValidation;
import de.a12.studio.ui.util.WidgetFactory;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;

public class GeneralInformationPanelController extends AbstractPropertyEditor {

  @FXML
  private TextField nameField;

  @FXML
  private TextField idField;

  @FXML
  private TextField pathField;

  private List<Element> ancestors = List.of();

  /** Applies a rename on this panel's behalf if set; see {@link #setRenameHandler}. Null = rename in place. */
  private BiConsumer<Element, String> renameHandler;

  @FXML
  private void onEditName(ActionEvent event) {
    String newName = WidgetFactory.showInputDialog(Studio.stage, "Rename", "Name", null, null, element.getName());
    if (newName == null || newName.equals(element.getName())) {
      return;
    }

    Optional<String> error = NameConventionValidation.validate("Name", newName);
    if (error.isPresent()) {
      WidgetFactory.showAlert(Studio.stage, "Invalid name", error.get());
      return;
    }

    if (renameHandler != null) {
      // The owner applies the rename as an undoable command, rewrites the references to the old name and saves.
      renameHandler.accept(element, newName);
      setFieldValue(nameField, newName);
      updatePathField(newName);
      return;
    }

    element.setName(newName);
    setFieldValue(nameField, newName);
    updatePathField(newName);
    commitChange();
  }

  @FXML
  private void onCopyPath(ActionEvent event) {
    ClipboardContent content = new ClipboardContent();
    content.putString(pathField.getText());
    Clipboard.getSystemClipboard().setContent(content);
  }

  public void setAncestors(@NonNull List<Element> ancestors) {
    this.ancestors = ancestors;
  }

  /**
   * Routes {@link #onEditName} through {@code renameHandler} instead of renaming the element in place, so the
   * owner of the model (the Document Model elements tree) can make it an undoable step that also rewrites the
   * references to the old name. Without one - e.g. in the Type Definition editor - the name is set directly.
   */
  public void setRenameHandler(@NonNull BiConsumer<Element, String> renameHandler) {
    this.renameHandler = renameHandler;
  }

  public void focusNameField() {
//    Platform.runLater(() -> {
//      nameField.requestFocus();
//      nameField.selectAll();
//    });
  }

  @Override
  public void setElement(@NonNull Element element) {
    super.setElement(element);
    setFieldValue(nameField, element.getName());
    idField.setText(element.getId());
    updatePathField(element.getName());
  }

  /**
   * This panel is present on every element type's editor (field, group, include, attachment, rule,
   * computation), so it doubles as the fallback home for structural element-level errors that have no more
   * specific panel of their own (e.g. a duplicate id, or an attachment group missing a required field).
   */
  @Override
  protected String validationProperty() {
    return ElementProperty.GENERAL;
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
