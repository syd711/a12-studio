package de.a12.studio.ui.editors.documentmodel;

import de.a12.studio.ui.util.localsettings.BaseTableSettings;
import de.a12.studio.ui.util.localsettings.LocalUISettings;
import de.a12.studio.models.A12Model;
import de.a12.studio.models.ModelType;
import de.a12.studio.models.documentmodel.ComputationElement;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.documentmodel.Element;
import de.a12.studio.models.documentmodel.GroupConfig;
import de.a12.studio.models.documentmodel.GroupElement;
import de.a12.studio.models.documentmodel.ModelRoot;
import de.a12.studio.models.documentmodel.RuleElement;
import de.a12.studio.dataservices.services.documentmodel.features.validation.DMValidationService;
import de.a12.studio.ui.editors.AbstractEditorController;
import de.a12.studio.ui.editors.dialogs.EditorDialogs;
import de.a12.studio.ui.editors.documentmodel.dialogs.DocumentModelDialogs;
import de.a12.studio.ui.util.ProjectDocumentModels;
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

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class DocumentModelEditorController extends AbstractEditorController implements Initializable {
  private static final String MAIN_DIVIDER_ID = "mainDivider";

  private static final String FIELD_EDITOR_FXML = "document-model-field-editor.fxml";
  private static final String GROUP_EDITOR_FXML = "document-model-group-editor.fxml";
  private static final String ATTACHMENT_EDITOR_FXML = "document-model-attachment-editor.fxml";
  private static final String VALIDATION_RULE_EDITOR_FXML = "document-model-validation-rule-editor.fxml";
  private static final String COMPUTATION_RULE_EDITOR_FXML = "document-model-computation-rule-editor.fxml";

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
  private DocumentModelElementsTreeController elementsTreeController;


  @FXML
  public void onSettings(ActionEvent e) {
    EditorDialogs.openSettings();
    updateSettingsErrorBadge();
  }

  @FXML
  public void onTypeDefinitions(ActionEvent e) {
    DocumentModelDialogs.openTypeDefinitions();
  }

  public void loadModel(@NonNull A12Model<?> model) {
    load(((DocumentModel) model).getContent().getModelRoot());
    updateSettingsErrorBadge();
  }

  private void updateSettingsErrorBadge() {
    List<String> issues = projectItem.getModel() instanceof DocumentModel documentModel
        ? VALIDATION_SERVICE.getSettingsIssueMessages(documentModel, ProjectDocumentModels.getOtherDocumentModels(projectItem))
        : List.of();

    settingsErrorBadge.setVisible(!issues.isEmpty());
    settingsButtonTooltip.setText(issues.isEmpty() ? DEFAULT_SETTINGS_TOOLTIP : String.join("\n\n", issues));
  }

  private void load(@NonNull ModelRoot modelRoot) {
    elementsTreeController.load(projectItem, modelRoot);
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    BaseTableSettings tableSettings = getBaseTableSettings();
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
    String editorFxml;
    if (selected instanceof GroupElement groupElement) {
      editorFxml = groupElement.getGroup() != null
          && GroupConfig.USAGE_TYPE_ATTACHMENT.equals(groupElement.getGroup().getUsageType())
          ? ATTACHMENT_EDITOR_FXML
          : GROUP_EDITOR_FXML;
    }
    else if (selected instanceof RuleElement) {
      editorFxml = VALIDATION_RULE_EDITOR_FXML;
    }
    else if (selected instanceof ComputationElement) {
      editorFxml = COMPUTATION_RULE_EDITOR_FXML;
    }
    else {
      editorFxml = FIELD_EDITOR_FXML;
    }
    Node node = loadEditor(editorFxml, selected);
    editorContainer.setCenter(node);
  }

  private Node loadEditor(@NonNull String fxml, @NonNull Element selected) {
    try {
      FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
      Node node = loader.load();
      if (loader.getController() instanceof ElementEditorController elementEditorController) {
        elementEditorController.setElement(selected, elementsTreeController.getAncestors(selected));
      }
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
    return ModelType.DOCUMENT;
  }
}
