package de.a12.studio.ui.editors.treemodel;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.ModelType;
import de.a12.studio.models.overviewmodel.BoxElement;
import de.a12.studio.models.overviewmodel.ButtonElement;
import de.a12.studio.models.overviewmodel.ElementBox;
import de.a12.studio.models.treemodel.ExpansionStrategy;
import de.a12.studio.models.treemodel.TreeModel;
import de.a12.studio.models.treemodel.TreeModelContent;
import de.a12.studio.ui.editors.AbstractEditorController;
import de.a12.studio.ui.editors.overviewmodel.StylesPanelController;
import de.a12.studio.ui.editors.overviewmodel.SubheaderSlotPanelController;
import de.a12.studio.ui.editors.propertyeditors.EventButtonsPanelController;
import de.a12.studio.ui.util.StudioBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import org.jspecify.annotations.NonNull;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.UUID;

/**
 * Edits a {@link TreeModel}: wires the panels of its four tabs - Columns ({@link TreeRootPanelController},
 * {@link TreeColumnsPanelController} incl. the hierarchical column, {@link TreeNodeTypesPanelController}),
 * Configuration ({@link TreeConfigurationPanelController}, expansion strategy, plus {@link
 * TreeExpansionDepthsPanelController} while that strategy is "Tree", then {@link
 * TreeMultiSelectionPanelController} and {@link TreeDragAndDropPanelController}), Custom Actions (the
 * Overview Model's {@link SubheaderSlotPanelController} for {@code content.subHeaderBox} - Button, Multi-Selection
 * and Expand All PopUp elements - and {@link EventButtonsPanelController} for the Button-only {@code
 * content.footerBox}; Major maps to {@code rightSlot}, Minor to {@code leftSlot}, and unlike the Overview Model
 * there are no row actions or context menu) and Layout ({@link
 * TreeVirtualScrollingPanelController}, {@link TreeRowHeightActionColumnWidthPanelController}, {@link
 * TreeColumnsResizePanelController}, {@link TreeAccessibilityPanelController} and the Overview editor's {@link
 * StylesPanelController}, bound to {@code content.styles}) - each of which owns and persists its own slice of {@link
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
  private TreeExpansionDepthsPanelController expansionDepthsPanelController;

  @FXML
  private TreeMultiSelectionPanelController multiSelectionPanelController;

  @FXML
  private TreeDragAndDropPanelController dragAndDropPanelController;

  @FXML
  private SubheaderSlotPanelController subheaderMajorController;

  @FXML
  private SubheaderSlotPanelController subheaderMinorController;

  @FXML
  private EventButtonsPanelController footerMinorButtonsController;

  @FXML
  private EventButtonsPanelController footerMajorButtonsController;

  @FXML
  private TreeVirtualScrollingPanelController virtualScrollingPanelController;

  @FXML
  private TreeRowHeightActionColumnWidthPanelController rowHeightActionColumnWidthPanelController;

  @FXML
  private TreeColumnsResizePanelController columnsResizePanelController;

  @FXML
  private TreeAccessibilityPanelController accessibilityPanelController;

  @FXML
  private StylesPanelController stylesPanelController;

  @Override
  public void initialize(URL url, ResourceBundle resources) {
    nodeTypesPanelController.setOnChange(() -> rootPanelController.refresh());
    configurationPanelController.setOnStrategyChange(
        type -> expansionDepthsPanelController.setStrategyVisible(ExpansionStrategy.TREE.equals(type)));
    subheaderMajorController.setOnElementCreated(TreeModelEditorController::assignButtonId);
    subheaderMinorController.setOnElementCreated(TreeModelEditorController::assignButtonId);
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
    expansionDepthsPanelController.setModel(model);
    configurationPanelController.setModel(model);
    multiSelectionPanelController.setModel(model);
    dragAndDropPanelController.setModel(model);
    virtualScrollingPanelController.setModel(model);
    rowHeightActionColumnWidthPanelController.setModel(model);
    columnsResizePanelController.setModel(model);
    accessibilityPanelController.setModel(model);
    stylesPanelController.setCustom(model.getContent()::getStyles);
    loadCustomActions(model);
  }

  private void loadCustomActions(@NonNull TreeModel model) {
    TreeModelContent content = model.getContent();
    if (content.getSubHeaderBox() == null) {
      content.setSubHeaderBox(ElementBox.createEmpty());
    }
    if (content.getFooterBox() == null) {
      content.setFooterBox(ElementBox.createEmpty());
    }
    ElementBox subHeaderBox = content.getSubHeaderBox();
    subheaderMajorController.configure(StudioBundle.get("major_buttons"), ".subheaderMajor", subHeaderBox.getRightSlot(),
        SubheaderSlotPanelController.TREE_TYPES);
    subheaderMinorController.configure(StudioBundle.get("minor_buttons"), ".subheaderMinor", subHeaderBox.getLeftSlot(),
        SubheaderSlotPanelController.TREE_TYPES);

    ElementBox footerBox = content.getFooterBox();
    footerMinorButtonsController.configure(StudioBundle.get("minor_buttons"), ".footerMinor", footerBox.getLeftSlot(),
        TreeModelEditorController::newButton);
    footerMajorButtonsController.configure(StudioBundle.get("major_buttons"), ".footerMajor", footerBox.getRightSlot(),
        TreeModelEditorController::newButton);
  }

  // Tree Model buttons carry an id, e.g. "button-026dc" (Overview Model buttons don't).
  private static ButtonElement newButton() {
    ButtonElement button = new ButtonElement();
    assignButtonId(button);
    return button;
  }

  private static void assignButtonId(BoxElement element) {
    if (element instanceof ButtonElement button && button.getId() == null) {
      button.setId("button-" + UUID.randomUUID().toString().replace("-", "").substring(0, 5));
    }
  }

  @Override
  public @NonNull ModelType getModelType() {
    return ModelType.TREE;
  }
}
