package de.a12.studio.ui.editors.documentmodel;

import de.a12.studio.commons.util.localsettings.BaseTableSettings;
import de.a12.studio.commons.util.localsettings.LocalUISettings;
import de.a12.studio.dataservices.models.A12Model;
import de.a12.studio.dataservices.models.ModelType;
import de.a12.studio.dataservices.models.documentmodel.DocumentModel;
import de.a12.studio.dataservices.models.documentmodel.Element;
import de.a12.studio.dataservices.models.documentmodel.GroupElement;
import de.a12.studio.dataservices.models.documentmodel.ModelRoot;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.editors.AbstractEditorController;
import de.a12.studio.ui.editors.documentmodel.dialogs.DocumentModelFactory;
import de.a12.studio.ui.util.SystemUtil;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.BorderPane;
import org.jspecify.annotations.NonNull;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class DocumentModelEditorController extends AbstractEditorController implements Initializable {

  private static final String TABLE_SETTINGS_ID = ModelType.DOCUMENT.getValue();

  private static final String MAIN_DIVIDER_ID = "mainDivider";

  private static final String FIELD_EDITOR_FXML = "document-model-field-editor.fxml";

  private static final String GROUP_EDITOR_FXML = "document-model-group-editor.fxml";

  @FXML
  private SplitPane splitPane;

  @FXML
  private BorderPane editorContainer;

  @FXML
  private DocumentModelElementsTreeController elementsTreeController;

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
    DocumentModelFactory.openSettings();
  }

  public void loadModel(@NonNull A12Model model) {
    load(((DocumentModel) model).getContent().getModelRoot());
  }

  private void load(@NonNull ModelRoot modelRoot) {
    elementsTreeController.load(projectItem, modelRoot);
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    BaseTableSettings tableSettings = LocalUISettings.getTablePreference(TABLE_SETTINGS_ID);
    applyDividerPosition(tableSettings);
    splitPane.getDividers().get(0).positionProperty().addListener((observable, oldValue, newValue) ->
        saveDividerPosition(newValue.doubleValue()));
    elementsTreeController.setSelectionListener(this::onElementSelectionChanged);
  }

  private void onElementSelectionChanged(@NonNull List<Element> selectedElements) {
    if (selectedElements.size() != 1) {
      editorContainer.setCenter(null);
      return;
    }

    Element selected = selectedElements.get(0);
    String editorFxml = selected instanceof GroupElement ? GROUP_EDITOR_FXML : FIELD_EDITOR_FXML;
    editorContainer.setCenter(loadEditor(editorFxml));
  }

  private Node loadEditor(@NonNull String fxml) {
    try {
      FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
      return loader.load();
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
