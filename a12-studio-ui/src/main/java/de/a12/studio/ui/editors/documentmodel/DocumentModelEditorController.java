package de.a12.studio.ui.editors.documentmodel;

import de.a12.studio.ui.util.StudioBundle;

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
import de.a12.studio.ui.editors.AbstractEditorController;
import de.a12.studio.ui.events.ModelClosedEvent;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.BorderPane;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

@Slf4j
public class DocumentModelEditorController extends AbstractEditorController implements Initializable {
  private static final String MAIN_DIVIDER_ID = "mainDivider";

  private static final String FIELD_EDITOR_FXML = "document-model-field-editor.fxml";
  private static final String GROUP_EDITOR_FXML = "document-model-group-editor.fxml";
  private static final String INCLUDE_EDITOR_FXML = "document-model-include-editor.fxml";
  private static final String ATTACHMENT_EDITOR_FXML = "document-model-attachment-editor.fxml";
  private static final String VALIDATION_RULE_EDITOR_FXML = "document-model-validation-rule-editor.fxml";
  private static final String COMPUTATION_RULE_EDITOR_FXML = "document-model-computation-rule-editor.fxml";

  @FXML
  private SplitPane splitPane;

  @FXML
  private BorderPane editorContainer;

  @FXML
  private DocumentModelElementsTreeController elementsTreeController;

  // Loaded editor Nodes/controllers, keyed by editorFxml (FIELD_EDITOR_FXML, GROUP_EDITOR_FXML, ...), reused
  // across tree selections of the same kind instead of re-loading the FXML (and re-running every embedded
  // property editor panel's initialize()) on every single click. Populated lazily in loadEditor() and torn
  // down (via ElementEditorController#destroy) only when this model's tab closes, in modelClosed().
  private final Map<String, Node> editorNodeCache = new HashMap<>();
  private final Map<String, ElementEditorController> editorControllerCache = new HashMap<>();

  private ElementEditorController currentElementEditorController;

  @FXML
  public void onTypeDefinitions(ActionEvent e) {
    de.a12.studio.ui.editors.documentmodel.dialogs.Dialogs.openTypeDefinitions();
  }

  public void loadModel(@NonNull A12Model<?> model) {
    load(((DocumentModel) model).getContent().getModelRoot());
    updateSettingsErrorBadge();
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
    long startTime = System.currentTimeMillis();

    if (selectedElements.size() != 1) {
      editorContainer.setCenter(null);
      currentElementEditorController = null;
      return;
    }

    Element selected = selectedElements.get(0);
    // The synthetic "Base Model" node an Additive Document Model's tree injects for editing context (see
    // DocumentModelElementsTreeController#baseModelNode) looks like a real Include to groupEditorFxml, but
    // has no backing entry in this model's content to persist an edit against - show nothing instead.
    if (elementsTreeController.isBaseModelNode(selected)) {
      editorContainer.setCenter(null);
      currentElementEditorController = null;
      return;
    }
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
    log.info("Rendered '{}' for element '{}' in {}ms", editorFxml, selected.getId(), System.currentTimeMillis() - startTime);
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
    Node node = editorNodeCache.get(fxml);
    ElementEditorController controller = editorControllerCache.get(fxml);
    if (node == null) {
      try {
        long loadStart = System.currentTimeMillis();
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
        loader.setResources(StudioBundle.getBundle());
        node = loader.load();
        log.info("  loaded '{}' in {}ms", fxml, System.currentTimeMillis() - loadStart);
        if (loader.getController() instanceof ElementEditorController elementEditorController) {
          controller = elementEditorController;
          editorControllerCache.put(fxml, controller);
        }
        editorNodeCache.put(fxml, node);
      }
      catch (IOException e) {
        throw new UncheckedIOException(e);
      }
    }
    if (controller != null) {
      long bindStart = System.currentTimeMillis();
      controller.setElement(selected, elementsTreeController.getAncestors(selected));
      currentElementEditorController = controller;
      log.info("  bound '{}' in {}ms", fxml, System.currentTimeMillis() - bindStart);
    }
    return node;
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
   * down every cached element editor panel (see {@link #editorControllerCache}), not just whichever one is
   * currently displayed in {@code editorContainer}, since none of them are otherwise reached by {@link
   * #onElementSelectionChanged} once the tab is gone.
   */
  @Override
  public void modelClosed(@NonNull ModelClosedEvent event) {
    super.modelClosed(event);
    if (event.getItem().equals(projectItem)) {
      editorControllerCache.values().forEach(ElementEditorController::destroy);
      editorControllerCache.clear();
      editorNodeCache.clear();
      currentElementEditorController = null;
    }
  }
}
