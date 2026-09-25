package de.a12.studio.ui.editors.treemodel;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.ModelType;
import de.a12.studio.models.treemodel.TreeModel;
import de.a12.studio.ui.editors.AbstractEditorController;
import de.a12.studio.ui.editors.overviewmodel.StylesPanelController;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import org.jspecify.annotations.NonNull;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Edits a {@link TreeModel}: wires the panels of its three tabs - Columns ({@link TreeRootPanelController},
 * {@link TreeColumnsPanelController} incl. the hierarchical column, {@link TreeNodeTypesPanelController}),
 * Configuration ({@link TreeConfigurationPanelController}, expansion strategy) and Layout ({@link
 * TreeAccessibilityPanelController} and the Overview editor's {@link StylesPanelController}, bound to {@code
 * content.styles}) - each of which owns and persists its own slice of {@link
 * de.a12.studio.models.treemodel.TreeModelContent}. {@link TreeNodeTypesPanelController#setOnChange} keeps the
 * Root panel's choices, which come from the node types' child relationship configurations, in sync.
 */
public class TreeModelEditorController extends AbstractEditorController implements Initializable {

  @FXML
  private TreeRootPanelController rootPanelController;

  @FXML
  private TreeColumnsPanelController columnsPanelController;

  @FXML
  private TreeNodeTypesPanelController nodeTypesPanelController;

  @FXML
  private TreeConfigurationPanelController configurationPanelController;

  @FXML
  private TreeAccessibilityPanelController accessibilityPanelController;

  @FXML
  private StylesPanelController stylesPanelController;

  @Override
  public void initialize(URL url, ResourceBundle resources) {
    nodeTypesPanelController.setOnChange(() -> rootPanelController.refresh());
  }

  @Override
  public void loadModel(@NonNull A12Model<?> model) {
    load((TreeModel) model);
    updateSettingsErrorBadge();
  }

  private void load(@NonNull TreeModel model) {
    columnsPanelController.setModel(model);
    nodeTypesPanelController.setModel(model, projectItem);
    rootPanelController.setModel(model);
    configurationPanelController.setModel(model);
    accessibilityPanelController.setModel(model);
    stylesPanelController.setCustom(model.getContent()::getStyles);
  }

  @Override
  public @NonNull ModelType getModelType() {
    return ModelType.TREE;
  }
}
