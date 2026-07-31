package de.a12.studio.ui.editors.typedefinitionmodel;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.ModelType;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.ui.editors.AbstractEditorController;
import de.a12.studio.ui.editors.documentmodel.ElementEditorController;
import de.a12.studio.ui.events.ModelClosedEvent;
import de.a12.studio.ui.events.ModelSaveEvent;
import de.a12.studio.ui.util.localsettings.BaseTableSettings;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.BorderPane;
import org.jspecify.annotations.NonNull;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class TypeDefintionModelEditorController extends AbstractEditorController implements Initializable {

  private static final String MAIN_DIVIDER_ID = "mainDivider";

  private static final String FIELD_EDITOR_FXML = "typedefinition-model-field-editor.fxml";

  @FXML
  private SplitPane splitPane;

  @FXML
  private BorderPane editorContainer;

  @FXML
  private TypeDefinitionTableController typeDefinitionsTableController;

  private TypeDefinitionModelFieldEditorController currentFieldEditorController;

  public void loadModel(@NonNull A12Model<?> model) {
    load((DocumentModel) model);
    updateSettingsErrorBadge();
  }

  private void load(@NonNull DocumentModel documentModel) {
    typeDefinitionsTableController.load(documentModel);
  }

  @Override
  public void modelSaved(@NonNull ModelSaveEvent event) {
    super.modelSaved(event);
    if (projectItem == null || !projectItem.getPath().equals(event.getItem().getPath())) {
      return;
    }
    projectItem.reload();
    if (projectItem.getModel() != null) {
      loadModel(projectItem.getModel());
    }
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    BaseTableSettings tableSettings = getBaseTableSettings();
    applyDividerPosition(tableSettings);
    splitPane.getDividers().get(0).positionProperty().addListener((observable, oldValue, newValue) ->
        saveDividerPosition(newValue.doubleValue()));
    typeDefinitionsTableController.setSelectionListener(this::onTypeDefinitionSelectionChanged);
    typeDefinitionsTableController.setOnItemAdded(this::focusNameField);
  }

  private void onTypeDefinitionSelectionChanged(TypeDefinitionRow selected) {
    if (currentFieldEditorController != null) {
      currentFieldEditorController.destroy();
      currentFieldEditorController = null;
    }

    if (selected == null) {
      editorContainer.setCenter(null);
      return;
    }

    editorContainer.setCenter(loadEditor(FIELD_EDITOR_FXML, selected));
  }

  private void focusNameField() {
    if (currentFieldEditorController != null) {
      currentFieldEditorController.focusNameField();
    }
  }

  private Node loadEditor(@NonNull String fxml, @NonNull TypeDefinitionRow selected) {
    try {
      FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
      Node node = loader.load();
      if (loader.getController() instanceof ElementEditorController elementEditorController) {
        elementEditorController.setElement(new TypeDefinitionFieldElement(selected.typeDefinition()), List.of());
      }
      currentFieldEditorController = loader.getController() instanceof TypeDefinitionModelFieldEditorController fieldEditorController
          ? fieldEditorController
          : null;
      return node;
    }
    catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  private void applyDividerPosition(BaseTableSettings tableSettings) {
    if (tableSettings == null) {
      return;
    }
    double position = tableSettings.getDividerPosition(MAIN_DIVIDER_ID);
    if (position >= 0) {
      splitPane.setDividerPosition(0, position);
    }
  }

  private void saveDividerPosition(double position) {
    BaseTableSettings tableSettings = getBaseTableSettings();
    if (tableSettings == null) {
      return;
    }
    tableSettings.getDividerPositions().put(MAIN_DIVIDER_ID, position);
    tableSettings.save();
  }

  @Override
  public @NonNull ModelType getModelType() {
    return ModelType.TYPEDEFINITION;
  }

  /**
   * In addition to unregistering this editor itself (see {@link AbstractEditorController#modelClosed}), tears
   * down whichever field editor panel is currently displayed in {@code editorContainer}, since it isn't
   * otherwise reached by {@link #onTypeDefinitionSelectionChanged} once the tab is gone.
   */
  @Override
  public void modelClosed(@NonNull ModelClosedEvent event) {
    super.modelClosed(event);
    if (currentFieldEditorController != null && event.getItem().equals(projectItem)) {
      currentFieldEditorController.destroy();
      currentFieldEditorController = null;
    }
  }
}
