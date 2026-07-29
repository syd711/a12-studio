package de.a12.studio.ui.editors.documentmodel.dialogs;

import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.documentmodel.TypeDefinition;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.components.DialogController;
import de.a12.studio.ui.editors.PropertyEditorSaveMode;
import de.a12.studio.ui.editors.typedefinitionmodel.TypeDefinitionModelFieldEditorController;
import de.a12.studio.ui.editors.typedefinitionmodel.TypeDefinitionTableController;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Lets the user edit a {@link DocumentModel}'s {@code typeDefinitions} section from within its regular editor
 * tab (see {@code DocumentModelEditorController#onTypeDefinitions}), reusing the same table/field-editor panels
 * as the standalone type-definition-only editor ({@code TypeDefintionModelEditorController}). Unlike that
 * editor, this dialog never loads or saves a whole model of its own: it edits the {@code typeDefinitions} list
 * already living on the currently selected project item's {@link DocumentModel}, and only persists it to disk
 * once {@link #onSave} is pressed, via a {@link PropertyEditorSaveMode.Deferred} shared with every embedded
 * panel. {@link #onDialogCancel} undoes whatever was already applied to the live model via a {@link
 * TypeDefinitionsSnapshot} taken before any panel could touch it.
 */
public class TypeDefinitionSettingsDialog implements Initializable, DialogController {

  private static final String FIELD_EDITOR_FXML = "/de/a12/studio/ui/editors/typedefinitionmodel/typedefinition-model-field-editor.fxml";

  @FXML
  private BorderPane editorContainer;

  @FXML
  private TypeDefinitionTableController typeDefinitionsTableController;

  private final PropertyEditorSaveMode.Deferred saveMode = new PropertyEditorSaveMode.Deferred();

  private TypeDefinitionModelFieldEditorController currentFieldEditorController;

  private TypeDefinitionsSnapshot snapshot;

  private Stage stage;

  public void setStage(Stage stage) {
    this.stage = stage;
  }

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    typeDefinitionsTableController.setSaveMode(saveMode);
    typeDefinitionsTableController.setSelectionListener(this::onTypeDefinitionSelectionChanged);
    typeDefinitionsTableController.setOnItemAdded(this::focusNameField);

    ProjectItem projectItem = Studio.getSelectedProjectItem();
    if (projectItem != null && projectItem.getModel() instanceof DocumentModel documentModel) {
      snapshot = new TypeDefinitionsSnapshot(documentModel);
      typeDefinitionsTableController.load(documentModel);
    }
  }

  private void onTypeDefinitionSelectionChanged(TypeDefinition selected) {
    if (currentFieldEditorController != null) {
      currentFieldEditorController.destroy();
      currentFieldEditorController = null;
    }

    if (selected == null) {
      editorContainer.setCenter(null);
      return;
    }

    editorContainer.setCenter(loadEditor(selected));
  }

  private void focusNameField() {
    if (currentFieldEditorController != null) {
      currentFieldEditorController.focusNameField();
    }
  }

  private Node loadEditor(TypeDefinition selected) {
    try {
      FXMLLoader loader = new FXMLLoader(getClass().getResource(FIELD_EDITOR_FXML));
      Node node = loader.load();
      TypeDefinitionModelFieldEditorController fieldEditorController = loader.getController();
      fieldEditorController.setSaveMode(saveMode);
      fieldEditorController.setTypeDefinition(selected);
      currentFieldEditorController = fieldEditorController;
      return node;
    }
    catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  @FXML
  private void onSave() {
    saveMode.flush();
    stage.close();
  }

  @FXML
  private void onCancel() {
    onDialogCancel();
    stage.close();
  }

  @Override
  public void onDialogCancel() {
    if (snapshot != null) {
      snapshot.restore();
    }
  }

  /**
   * Tears down whichever field editor panel is currently displayed in {@code editorContainer}, called from the
   * stage's {@code onHidden} handler (see {@code Dialogs#openTypeDefinitions}) regardless of how the dialog was
   * closed (Save, Cancel, or the window's own close button).
   */
  public void destroy() {
    if (currentFieldEditorController != null) {
      currentFieldEditorController.destroy();
      currentFieldEditorController = null;
    }
  }
}
