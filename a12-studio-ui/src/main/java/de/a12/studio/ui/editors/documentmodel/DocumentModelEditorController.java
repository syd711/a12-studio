package de.a12.studio.ui.editors.documentmodel;

import de.a12.studio.ui.util.localsettings.BaseTableSettings;
import de.a12.studio.models.A12Model;
import de.a12.studio.models.ModelType;
import de.a12.studio.models.documentmodel.ComputationElement;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.documentmodel.Element;
import de.a12.studio.models.documentmodel.GroupConfig;
import de.a12.studio.models.documentmodel.GroupElement;
import de.a12.studio.models.documentmodel.ModelRoot;
import de.a12.studio.models.documentmodel.RuleElement;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.editors.AbstractEditorController;
import de.a12.studio.ui.editors.dialogs.EditorDialogs;
import de.a12.studio.ui.editors.documentmodel.dialogs.DocumentModelDialogs;
import de.a12.studio.ui.events.ModelClosedEvent;
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
  private static final String INCLUDE_EDITOR_FXML = "document-model-include-editor.fxml";
  private static final String ATTACHMENT_EDITOR_FXML = "document-model-attachment-editor.fxml";
  private static final String VALIDATION_RULE_EDITOR_FXML = "document-model-validation-rule-editor.fxml";
  private static final String COMPUTATION_RULE_EDITOR_FXML = "document-model-computation-rule-editor.fxml";

  private static final String DEFAULT_SETTINGS_TOOLTIP = "Model Settings";

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

  private ElementEditorController currentElementEditorController;

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
    List<String> issues = projectItem.getModel() != null
        ? Studio.getValidationService().getSettingsIssueMessages(projectItem.getModel())
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
    if (currentElementEditorController != null) {
      currentElementEditorController.destroy();
      currentElementEditorController = null;
    }

    if (selectedElements.size() != 1) {
      editorContainer.setCenter(null);
      return;
    }

    Element selected = selectedElements.get(0);
    String editorFxml;
    if (selected instanceof GroupElement groupElement) {
      editorFxml = groupEditorFxml(groupElement);
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

  /**
   * A group is an Include (a reference to another Document Model) if its {@link GroupConfig} carries an
   * {@code includeConfig}, distinct from the "attachment" {@code usageType} groups and from plain groups,
   * which have neither.
   */
  private String groupEditorFxml(@NonNull GroupElement groupElement) {
    GroupConfig config = groupElement.getGroup();
    if (config == null) {
      return GROUP_EDITOR_FXML;
    }
    if (config.getIncludeConfig() != null) {
      return INCLUDE_EDITOR_FXML;
    }
    if (GroupConfig.USAGE_TYPE_ATTACHMENT.equals(config.getUsageType())) {
      return ATTACHMENT_EDITOR_FXML;
    }
    return GROUP_EDITOR_FXML;
  }

  private Node loadEditor(@NonNull String fxml, @NonNull Element selected) {
    try {
      FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
      Node node = loader.load();
      if (loader.getController() instanceof ElementEditorController elementEditorController) {
        elementEditorController.setElement(selected, elementsTreeController.getAncestors(selected));
        currentElementEditorController = elementEditorController;
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

  /**
   * In addition to unregistering this editor itself (see {@link AbstractEditorController#modelClosed}), tears
   * down whichever element editor panel is currently displayed in {@code editorContainer}, since it isn't
   * otherwise reached by {@link #onElementSelectionChanged} once the tab is gone.
   */
  @Override
  public void modelClosed(@NonNull ModelClosedEvent event) {
    super.modelClosed(event);
    if (currentElementEditorController != null && event.getItem().equals(projectItem)) {
      currentElementEditorController.destroy();
      currentElementEditorController = null;
    }
  }
}
