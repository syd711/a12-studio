package de.a12.studio.ui.editors.typedefinitionmodel;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.ModelType;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.documentmodel.TypeDefinition;
import de.a12.studio.dataservices.services.documentmodel.features.validation.DMValidationService;
import de.a12.studio.ui.editors.AbstractEditorController;
import de.a12.studio.ui.editors.dialogs.EditorDialogs;
import de.a12.studio.ui.editors.documentmodel.ElementEditorController;
import de.a12.studio.ui.util.ProjectDocumentModels;
import de.a12.studio.ui.util.SystemUtil;
import de.a12.studio.ui.util.localsettings.BaseTableSettings;
import de.a12.studio.ui.util.localsettings.LocalUISettings;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.shape.Circle;
import org.jspecify.annotations.NonNull;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class TypeDefintionEditorController extends AbstractEditorController implements Initializable {

  private static final String TABLE_SETTINGS_ID = ModelType.TYPEDEFINITION.getValue();

  private static final String MAIN_DIVIDER_ID = "mainDivider";

  private static final String FIELD_EDITOR_FXML = "typedefinition-model-field-editor.fxml";

  private static final String DEFAULT_SETTINGS_TOOLTIP = "Model Settings";

  private static final DMValidationService VALIDATION_SERVICE = new DMValidationService();

  @FXML
  private SplitPane splitPane;

  @FXML
  private BorderPane editorContainer;

  @FXML
  private Circle settingsErrorBadge;

  @FXML
  private Tooltip settingsButtonTooltip;

  @FXML
  private TypeDefinitionTableController typeDefinitionsTableController;

  private TypeDefinitionModelFieldEditorController currentFieldEditorController;

  @FXML
  public void onFileOpen(ActionEvent e) {
    File file = projectItem.getFile();
    SystemUtil.openFile(file);
  }

  @FXML
  public void onFileEdit(ActionEvent e) {
    File file = projectItem.getFile();
    SystemUtil.editFile(file);
  }

  @FXML
  public void onSettings(ActionEvent e) {
    EditorDialogs.openSettings();
    updateSettingsErrorBadge();
  }

  public void loadModel(@NonNull A12Model model) {
    load((DocumentModel) model);
    updateSettingsErrorBadge();
  }

  private void updateSettingsErrorBadge() {
    List<String> issues = projectItem.getModel() instanceof DocumentModel documentModel
        ? VALIDATION_SERVICE.getSettingsIssueMessages(documentModel, ProjectDocumentModels.getOtherDocumentModels(projectItem))
        : List.of();

    settingsErrorBadge.setVisible(!issues.isEmpty());
    settingsButtonTooltip.setText(issues.isEmpty() ? DEFAULT_SETTINGS_TOOLTIP : String.join("\n\n", issues));
  }

  private void load(@NonNull DocumentModel documentModel) {
    typeDefinitionsTableController.load(documentModel);
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    BaseTableSettings tableSettings = LocalUISettings.getTablePreference(TABLE_SETTINGS_ID);
    applyDividerPosition(tableSettings);
    splitPane.getDividers().get(0).positionProperty().addListener((observable, oldValue, newValue) ->
        saveDividerPosition(newValue.doubleValue()));
    typeDefinitionsTableController.setSelectionListener(this::onTypeDefinitionSelectionChanged);
    typeDefinitionsTableController.setOnItemAdded(this::focusNameField);
  }

  private void onTypeDefinitionSelectionChanged(TypeDefinition selected) {
    if (selected == null) {
      editorContainer.setCenter(null);
      currentFieldEditorController = null;
      return;
    }

    editorContainer.setCenter(loadEditor(FIELD_EDITOR_FXML, selected));
  }

  private void focusNameField() {
    if (currentFieldEditorController != null) {
      currentFieldEditorController.focusNameField();
    }
  }

  private Node loadEditor(@NonNull String fxml, @NonNull TypeDefinition selected) {
    try {
      FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
      Node node = loader.load();
      if (loader.getController() instanceof ElementEditorController elementEditorController) {
        elementEditorController.setElement(new TypeDefinitionFieldElement(selected), List.of());
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
    BaseTableSettings tableSettings = LocalUISettings.getTablePreference(TABLE_SETTINGS_ID);
    if (tableSettings == null) {
      return;
    }
    tableSettings.getDividerPositions().put(MAIN_DIVIDER_ID, position);
    tableSettings.save();
  }
}
