package de.a12.studio.ui.editors.propertyeditors;

import de.a12.studio.models.applicationmodel.Menu;
import de.a12.studio.models.applicationmodel.Module;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.editors.AbstractPropertyEditor;
import de.a12.studio.ui.editors.applicationmodel.dialogs.ChildMenuDialogController;
import de.a12.studio.ui.editors.applicationmodel.dialogs.Dialogs;
import de.a12.studio.ui.util.Icons;
import de.a12.studio.ui.util.WidgetFactory;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.jspecify.annotations.NonNull;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Edits {@link Menu#getChildren()} of a {@link Module}'s menu: a list of child menu names, each reorderable
 * (move up/down) and deletable, with a full Add/Edit dialog ({@link ChildMenuDialogController}) for every
 * other child menu detail (activity descriptor, label, roles). Not bound to a single Element (child menus live
 * on the module's menu), so it follows the model-header pattern used by e.g. {@link ModulesPanelController}.
 */
public class ChildMenuPanelController extends AbstractPropertyEditor {

  @FXML
  private VBox childMenuList;

  private Module module;

  public void setModule(@NonNull Module module) {
    this.module = module;
    rebuildRows();
  }

  @FXML
  private void onAdd() {
    Dialogs.showChildMenuForAdd(Studio.stage).ifPresent(menu -> {
      getChildMenus().add(menu);
      rebuildRows();
      commitChange();
    });
  }

  private List<Menu> getChildMenus() {
    return module.getOrCreateMenu().getChildren();
  }

  private void rebuildRows() {
    childMenuList.getChildren().clear();

    List<Menu> children = module.getMenu() != null ? module.getMenu().getChildren() : List.of();
    if (children.isEmpty()) {
      Label emptyLabel = new Label("No child menus found.");
      emptyLabel.getStyleClass().add("placeholder-label");
      childMenuList.getChildren().add(emptyLabel);
      return;
    }

    for (int index = 0; index < children.size(); index++) {
      childMenuList.getChildren().add(createRow(children.get(index), index, children.size()));
    }
  }

  private HBox createRow(Menu menu, int index, int rowCount) {
    Label nameLabel = new Label(menu.getName());
    nameLabel.setId("child-menu-" + index);
    nameLabel.setMaxWidth(Double.MAX_VALUE);
    nameLabel.setCursor(Cursor.HAND);
    HBox.setHgrow(nameLabel, Priority.ALWAYS);
    nameLabel.setOnMouseClicked(event -> {
      if (event.getClickCount() == 1) {
        editMenu(menu);
      }
    });

    HBox row = new HBox(10.0, nameLabel, createActionsBox(menu, index, rowCount));
    row.setAlignment(Pos.CENTER_LEFT);
    row.getStyleClass().add("module-row");
    row.setOnMouseClicked(event -> {
      if (event.getClickCount() == 2) {
        editMenu(menu);
      }
    });
    return row;
  }

  private void editMenu(Menu menu) {
    if (Dialogs.showChildMenuForEdit(Studio.stage, menu)) {
      rebuildRows();
    }
  }

  private HBox createActionsBox(Menu menu, int index, int rowCount) {
    VBox moveButtonsBox = createMoveButtonsBox(index, rowCount);

    Button editButton = createActionButton(Icons.PENCIL, "Edit", () -> editMenu(menu));

    Button deleteButton = createActionButton(Icons.TRASH, "Delete", () -> {
      Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, "Delete this child menu?", null, null, "Delete");
      if (result.isPresent() && result.get() == ButtonType.OK) {
        getChildMenus().remove(menu);
        rebuildRows();
        commitChange();
      }
    });

    HBox actionsBox = new HBox(4.0, moveButtonsBox, editButton, deleteButton);
    actionsBox.setAlignment(Pos.CENTER_LEFT);
    return actionsBox;
  }

  // Move up/down stacked in a VBox instead of side by side in the HBox: each button is half-height (see the
  // "move-button" style class), so the pair together takes up the same width/height as a single normal button.
  private VBox createMoveButtonsBox(int index, int rowCount) {
    Button moveUpButton = createActionButton(Icons.ARROW_UP, "Move Up", () -> moveRow(index, index - 1));
    moveUpButton.setDisable(index == 0);
    moveUpButton.getStyleClass().addAll("move-button", "move-button-top");

    Button moveDownButton = createActionButton(Icons.ARROW_DOWN, "Move Down", () -> moveRow(index, index + 1));
    moveDownButton.setDisable(index == rowCount - 1);
    moveDownButton.getStyleClass().addAll("move-button", "move-button-bottom");

    return new VBox(1, moveUpButton, moveDownButton);
  }

  private void moveRow(int fromIndex, int toIndex) {
    Collections.swap(getChildMenus(), fromIndex, toIndex);
    rebuildRows();
    commitChange();
  }

  private static Button createActionButton(String iconLiteral, String tooltip, Runnable action) {
    FontIcon icon = new FontIcon(iconLiteral);
    icon.setIconSize(16);
    icon.getStyleClass().add("toolbar-icon");

    Button button = new Button();
    button.getStyleClass().add("default-button");
    button.setGraphic(icon);
    button.setTooltip(new Tooltip(tooltip));
    button.setOnAction(event -> action.run());
    return button;
  }
}
