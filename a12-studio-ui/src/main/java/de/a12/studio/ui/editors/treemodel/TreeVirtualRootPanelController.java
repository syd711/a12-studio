package de.a12.studio.ui.editors.treemodel;

import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.models.treemodel.TreeConfiguration;
import de.a12.studio.models.treemodel.TreeModel;
import de.a12.studio.models.treemodel.TreeNodeAction;
import de.a12.studio.models.treemodel.TreeVirtualRoot;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.editors.AbstractPropertyEditor;
import de.a12.studio.ui.editors.propertyeditors.LocalizedTextPanelController;
import de.a12.studio.ui.util.StudioBundle;
import de.a12.studio.ui.util.WidgetFactory;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.jspecify.annotations.NonNull;

import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Edits a {@link TreeModel}'s Virtual Root ({@code configuration.virtualRoot}, SME "Virtual Root" under Features): a node
 * drawn as the first row of the tree, with its own multilingual Label (shown in the hierarchical column), row actions and
 * context menu - the same panels as a node type's ({@link TreeNodeActionsPanelController}, {@link
 * TreeNodeContextMenuPanelController}), for its own events and only insert actions creating the Root's node type. The
 * feature is on while the key exists (SME's "Enable Virtual Root" is not stored); switching it off drops the Virtual Root,
 * which SME does not write while it is disabled either. Not bound to a single {@link
 * de.a12.studio.models.documentmodel.Element}, so it follows the model-header pattern.
 */
public class TreeVirtualRootPanelController extends AbstractPropertyEditor implements Initializable {

  @FXML
  private CheckBox enabledField;

  @FXML
  private Label infoLabel;

  @FXML
  private VBox content;

  @FXML
  private LocalizedTextPanelController labelController;

  @FXML
  private TreeNodeActionsPanelController actionsController;

  @FXML
  private TreeNodeContextMenuPanelController contextMenuController;

  private TreeModel model;

  // Set while the checkbox is being repopulated from the model, so that isn't mistaken for a user edit.
  private boolean updatingFromModel;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    super.initialize(location, resources);

    WidgetFactory.createHelpIcon(infoLabel, StudioBundle.get("tree_virtual_root_panel.info"));
    labelController.configureCustom("virtualRootLabel", StudioBundle.get("label"));
    enabledField.selectedProperty().addListener((observable, oldValue, enabled) -> {
      if (updatingFromModel || model == null) {
        return;
      }
      if (enabled) {
        ensureConfiguration().setVirtualRoot(new TreeVirtualRoot());
        bindPanels();
      }
      else if (!confirmDisabling()) {
        setEnabledWithoutEditing(true);
        return;
      }
      else {
        ensureConfiguration().setVirtualRoot(null);
      }
      updateVisibility();
      commitHeaderChange();
    });
  }

  public void setModel(@NonNull TreeModel model, @NonNull ProjectItem projectItem) {
    this.model = model;
    contextMenuController.setProjectItem(projectItem);
    contextMenuController.setModel(model);
    actionsController.configure(StudioBundle.get("actions"), ".virtualRootActions", projectItem,
        () -> virtualRoot() != null ? virtualRoot().getActions() : List.of(), () -> false, TreeVirtualRootPanelController::newAction,
        () -> TreeActionContext.forVirtualRoot(model));
    bindPanels();
    setEnabledWithoutEditing(virtualRoot() != null);
    updateVisibility();
  }

  /** Re-reads the Virtual Root, e.g. after the Root or a node type changed, which decide what its inserts may create. */
  public void refresh() {
    if (model != null) {
      bindPanels();
    }
  }

  private TreeVirtualRoot virtualRoot() {
    return model != null && model.getContent().getConfiguration() != null ? model.getContent().getConfiguration().getVirtualRoot() : null;
  }

  private TreeConfiguration ensureConfiguration() {
    if (model.getContent().getConfiguration() == null) {
      model.getContent().setConfiguration(new TreeConfiguration());
    }
    return model.getContent().getConfiguration();
  }

  /** Points the embedded panels at the current Virtual Root; without one they show nothing. */
  private void bindPanels() {
    TreeVirtualRoot virtualRoot = virtualRoot();
    if (virtualRoot != null) {
      labelController.setCustom(virtualRoot::getLabel);
    }
    actionsController.refresh();
    contextMenuController.configure(() -> virtualRoot() != null ? virtualRoot().getContextMenu() : null,
        menu -> {
          if (virtualRoot() != null) {
            virtualRoot().setContextMenu(menu);
          }
        }, () -> TreeActionContext.forVirtualRoot(model));
  }

  private void updateVisibility() {
    boolean enabled = virtualRoot() != null;
    content.setVisible(enabled);
    content.setManaged(enabled);
  }

  private void setEnabledWithoutEditing(boolean enabled) {
    updatingFromModel = true;
    try {
      enabledField.setSelected(enabled);
    }
    finally {
      updatingFromModel = false;
    }
  }

  /** Switching the Virtual Root off drops its label, actions and context menu; asks first when there is any. */
  private boolean confirmDisabling() {
    TreeVirtualRoot virtualRoot = virtualRoot();
    boolean hasContent = virtualRoot != null && (virtualRoot.getLabel().stream().anyMatch(label -> label.getText() != null && !label.getText().isBlank())
        || !virtualRoot.getActions().isEmpty() || virtualRoot.getContextMenu() != null);
    if (!hasContent) {
      return true;
    }
    Optional<ButtonType> answer = WidgetFactory.showConfirmation(Studio.stage, StudioBundle.get("tree_virtual_root_panel.confirm_disable"), null, null,
        StudioBundle.get("ok"));
    return answer.isPresent() && answer.get() == ButtonType.OK;
  }

  /** SME requires an action of the Virtual Root's row to have a priority (and writes Destructive with it). */
  private static TreeNodeAction newAction() {
    TreeNodeAction action = new TreeNodeAction();
    action.setPrimary(false);
    action.setDestructive(false);
    return action;
  }
}
