package de.a12.studio.ui.editors.propertyeditors;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.applicationmodel.Flow;
import de.a12.studio.models.applicationmodel.Module;
import de.a12.studio.models.applicationmodel.Scene;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.validators.application.ApplicationUniqueNamesValidator;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.editors.AbstractPropertyEditor;
import de.a12.studio.ui.editors.applicationmodel.dialogs.Dialogs;
import de.a12.studio.ui.util.Icons;
import de.a12.studio.ui.util.WidgetFactory;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableRow;
import javafx.scene.control.TreeTableView;
import javafx.scene.layout.HBox;
import org.jspecify.annotations.NonNull;
import org.kordamp.ikonli.javafx.FontIcon;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Displays {@link Module#getFlows()} as a tree grid, each {@link Flow} expandable to its {@link Scene}s, with
 * NAME and DESCRIPTION columns (only Scenes carry a description). Not bound to a single Element (flows live on
 * the module), so it follows the model-header pattern used by e.g. {@link ChildMenuPanelController}. Add/Edit/
 * Delete are offered as a per-row context menu (a Flow row offers "Add Flow"/"Add Scene", a Scene row only
 * "Add Scene"; Edit/Delete open a Flow- or Scene-specific dialog depending on the selected node), plus a
 * tree-level context menu for the empty area below the rows offering "Add Flow". The toolbar's Edit/Delete
 * buttons delegate to the same logic. Flow add/edit ({@link Dialogs#showFlowForAdd} & co.) reuses the generic
 * {@link de.a12.studio.ui.util.WidgetFactory#showInputDialog} since a Flow only carries a name; Scene add/edit
 * has its own dialog.
 */
public class FlowsPanelController extends AbstractPropertyEditor {

  @FXML
  private TreeTableView<Object> flowsTree;

  @FXML
  private TreeTableColumn<Object, String> nameColumn;

  @FXML
  private TreeTableColumn<Object, String> descriptionColumn;

  @FXML
  private Button editButton;

  @FXML
  private Button deleteButton;

  private Module module;

  public void setModule(@NonNull Module module) {
    this.module = module;
    rebuildTree();
  }

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    super.initialize(location, resources);
    flowsTree.setShowRoot(false);
    nameColumn.setCellValueFactory(param -> new ReadOnlyStringWrapper(nameOf(param.getValue().getValue())));
    nameColumn.setCellFactory(column -> createNameCell());
    descriptionColumn.setCellValueFactory(param -> new ReadOnlyStringWrapper(descriptionOf(param.getValue().getValue())));
    flowsTree.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> updateToolbarState());
    flowsTree.setRowFactory(treeTable -> createRow());
    flowsTree.setContextMenu(createEmptyAreaContextMenu());
    updateToolbarState();
  }

  @FXML
  private void onEdit() {
    editSelected();
  }

  @FXML
  private void onDelete() {
    deleteSelected();
  }

  private void updateToolbarState() {
    boolean hasSelection = flowsTree.getSelectionModel().getSelectedItem() != null;
    editButton.setDisable(!hasSelection);
    deleteButton.setDisable(!hasSelection);
  }

  private TreeTableCell<Object, String> createNameCell() {
    return new TreeTableCell<>() {
      @Override
      protected void updateItem(String name, boolean empty) {
        super.updateItem(name, empty);
        if (empty || name == null) {
          setText(null);
          setGraphic(null);
          return;
        }
        if (getTreeTableRow().getItem() instanceof Scene) {
          Label nameLabel = new Label(name);
          nameLabel.getStyleClass().add("tree-cell-name-label");
          FontIcon icon = WidgetFactory.createIcon(Icons.SCENE);
          icon.getStyleClass().add("tree-icon");
          HBox graphic = new HBox(4, icon, nameLabel);
          graphic.setAlignment(Pos.CENTER_LEFT);
          setText(null);
          setGraphic(graphic);
        } else {
          setText(name);
          setGraphic(null);
        }
      }
    };
  }

  private TreeTableRow<Object> createRow() {
    TreeTableRow<Object> row = new TreeTableRow<>() {
      @Override
      protected void updateItem(Object item, boolean empty) {
        super.updateItem(item, empty);
        setContextMenu(empty || item == null ? null : createRowContextMenu(item));
      }
    };
    row.setOnMouseClicked(event -> {
      if (event.getClickCount() == 2 && !row.isEmpty()) {
        Object item = row.getItem();
        if (item instanceof Flow flow) {
          editFlow(flow);
        } else if (item instanceof Scene scene) {
          editScene(findParentFlow(scene), scene);
        }
      }
    });
    return row;
  }

  private ContextMenu createEmptyAreaContextMenu() {
    ContextMenu contextMenu = new ContextMenu();
    contextMenu.getItems().add(createMenuItem("Add Flow", Icons.PLUS, this::addFlow));
    return contextMenu;
  }

  private ContextMenu createRowContextMenu(@NonNull Object item) {
    ContextMenu contextMenu = new ContextMenu();
    if (item instanceof Flow flow) {
      contextMenu.getItems().addAll(
          createMenuItem("Add Flow", Icons.PLUS, this::addFlow),
          createMenuItem("Add Scene", Icons.PLUS, () -> addScene(flow)),
          new SeparatorMenuItem(),
          createMenuItem("Edit Flow", Icons.PENCIL, () -> editFlow(flow)),
          createMenuItem("Delete Flow", Icons.TRASH, () -> deleteFlow(flow)));
    } else if (item instanceof Scene scene) {
      Flow parentFlow = findParentFlow(scene);
      contextMenu.getItems().addAll(
          createMenuItem("Add Scene", Icons.PLUS, () -> addScene(parentFlow)),
          new SeparatorMenuItem(),
          createMenuItem("Edit Scene", Icons.PENCIL, () -> editScene(parentFlow, scene)),
          createMenuItem("Delete Scene", Icons.TRASH, () -> deleteScene(parentFlow, scene)));
    }
    return contextMenu;
  }

  private static MenuItem createMenuItem(@NonNull String text, @NonNull String iconLiteral, @NonNull Runnable action) {
    MenuItem menuItem = new MenuItem(text, WidgetFactory.createIcon(iconLiteral));
    menuItem.setOnAction(event -> action.run());
    return menuItem;
  }

  private void editSelected() {
    Object selected = selectedValue();
    if (selected instanceof Flow flow) {
      editFlow(flow);
    } else if (selected instanceof Scene scene) {
      editScene(findParentFlow(scene), scene);
    }
  }

  private void deleteSelected() {
    Object selected = selectedValue();
    if (selected instanceof Flow flow) {
      deleteFlow(flow);
    } else if (selected instanceof Scene scene) {
      deleteScene(findParentFlow(scene), scene);
    }
  }

  private Object selectedValue() {
    TreeItem<Object> selectedItem = flowsTree.getSelectionModel().getSelectedItem();
    return selectedItem == null ? null : selectedItem.getValue();
  }

  private void addFlow() {
    Dialogs.showFlowForAdd(Studio.stage).ifPresent(flow -> {
      module.getFlows().add(flow);
      rebuildTree();
      commitChange();
    });
  }

  private void addScene(Flow flow) {
    if (flow == null) {
      return;
    }
    Dialogs.showSceneForAdd(Studio.stage, flow).ifPresent(scene -> {
      flow.getScenes().add(scene);
      rebuildTree();
      commitChange();
    });
  }

  private void editFlow(@NonNull Flow flow) {
    if (Dialogs.showFlowForEdit(Studio.stage, flow)) {
      rebuildTree();
      commitChange();
    }
  }

  private void editScene(Flow parentFlow, @NonNull Scene scene) {
    if (parentFlow == null) {
      return;
    }
    if (Dialogs.showSceneForEdit(Studio.stage, parentFlow, scene)) {
      rebuildTree();
      commitChange();
    }
  }

  private void deleteFlow(@NonNull Flow flow) {
    Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, "Delete this flow?", null, null, "Delete");
    if (result.isPresent() && result.get() == ButtonType.OK) {
      module.getFlows().remove(flow);
      rebuildTree();
      commitChange();
    }
  }

  private void deleteScene(Flow parentFlow, @NonNull Scene scene) {
    if (parentFlow == null) {
      return;
    }
    Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, "Delete this scene?", null, null, "Delete");
    if (result.isPresent() && result.get() == ButtonType.OK) {
      parentFlow.getScenes().remove(scene);
      rebuildTree();
      commitChange();
    }
  }

  private Flow findParentFlow(@NonNull Scene scene) {
    for (Flow flow : module.getFlows()) {
      if (flow.getScenes().contains(scene)) {
        return flow;
      }
    }
    return null;
  }

  private void rebuildTree() {
    TreeItem<Object> root = new TreeItem<>();
    for (Flow flow : module.getFlows()) {
      TreeItem<Object> flowItem = new TreeItem<>(flow);
      for (Scene scene : flow.getScenes()) {
        flowItem.getChildren().add(new TreeItem<>(scene));
      }
      flowItem.setExpanded(true);
      root.getChildren().add(flowItem);
    }
    flowsTree.setRoot(root);
    refreshNameUniquenessError();
  }

  /**
   * Not bound to an {@link de.a12.studio.models.documentmodel.Element}, so the base class's element-keyed
   * validation plumbing never runs for this panel; queries {@link ApplicationUniqueNamesValidator}'s
   * dedicated flow- and scene-name element ids (both scoped to {@link #module}, since this panel shows that
   * whole module's flow/scene tree at once) directly instead. Called from {@link #rebuildTree} (itself called
   * by every mutation here, plus {@link #setModule}), so this always reflects the tree as currently shown.
   */
  private void refreshNameUniquenessError() {
    ProjectItem projectItem = Studio.getSelectedProjectItem();
    A12Model<?> model = projectItem == null ? null : projectItem.getModel();
    if (model == null) {
      hideError();
      return;
    }
    List<ModelValidationError> errors = new ArrayList<>();
    errors.addAll(Studio.getValidationService().validateElement(model, ApplicationUniqueNamesValidator.flowsElementId(module.getName())));
    errors.addAll(Studio.getValidationService().validateElement(model, ApplicationUniqueNamesValidator.scenesElementId(module.getName())));
    if (errors.isEmpty()) {
      hideError();
    } else {
      showError(errors.get(0).severity(), errors.get(0).message());
    }
  }

  private static String nameOf(@NonNull Object item) {
    if (item instanceof Flow flow) {
      return flow.getName();
    }
    if (item instanceof Scene scene) {
      return scene.getName();
    }
    return null;
  }

  private static String descriptionOf(@NonNull Object item) {
    if (item instanceof Scene scene) {
      return scene.getDescription();
    }
    return null;
  }
}
