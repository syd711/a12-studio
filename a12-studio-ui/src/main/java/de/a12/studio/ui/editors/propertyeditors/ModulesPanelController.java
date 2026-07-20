package de.a12.studio.ui.editors.propertyeditors;

import de.a12.studio.models.applicationmodel.ApplicationModel;
import de.a12.studio.models.applicationmodel.ApplicationModelContent;
import de.a12.studio.models.applicationmodel.Module;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.editors.AbstractPropertyEditor;
import de.a12.studio.ui.editors.applicationmodel.dialogs.ModuleDialogController;
import de.a12.studio.ui.util.Icons;
import de.a12.studio.ui.util.WidgetFactory;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import org.jspecify.annotations.NonNull;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Edits {@link ApplicationModelContent#getModules()}: a list of module names, each reorderable (move up/down),
 * editable and copyable via {@link ModuleDialogController}, and deletable. Not bound to a single Element (modules
 * live on the model's content), so it follows the model-header pattern used by e.g. {@link ActivityPanelController}.
 */
public class ModulesPanelController extends AbstractPropertyEditor {

  @FXML
  private GridPane modulesGrid;

  private ApplicationModel model;

  public void setModel(@NonNull ApplicationModel model) {
    this.model = model;
    rebuildRows();
  }

  @FXML
  private void onAdd() {
    ModuleDialogController.showForAdd(Studio.stage).ifPresent(name -> {
      Module module = new Module();
      module.setName(name);
      getModules().add(module);
      rebuildRows();
      commitChange();
    });
  }

  private List<Module> getModules() {
    return model.getContent().getModules();
  }

  private void rebuildRows() {
    modulesGrid.getChildren().removeIf(node -> {
      Integer rowIndex = GridPane.getRowIndex(node);
      return rowIndex != null && rowIndex > 0;
    });

    List<Module> modules = getModules();
    for (int index = 0; index < modules.size(); index++) {
      addRow(modules.get(index), index, modules.size());
    }
  }

  private void addRow(Module module, int index, int rowCount) {
    Label nameLabel = new Label(module.getName());
    nameLabel.setId("module-" + index);
    nameLabel.setMaxWidth(Double.MAX_VALUE);

    modulesGrid.addRow(index + 1, nameLabel, createActionsBox(module, index, rowCount));
  }

  private HBox createActionsBox(Module module, int index, int rowCount) {
    Button moveUpButton = createActionButton(Icons.ARROW_UP, "Move Up", () -> moveRow(index, index - 1));
    moveUpButton.setDisable(index == 0);

    Button moveDownButton = createActionButton(Icons.ARROW_DOWN, "Move Down", () -> moveRow(index, index + 1));
    moveDownButton.setDisable(index == rowCount - 1);

    Button editButton = createActionButton(Icons.PENCIL, "Edit", () ->
        ModuleDialogController.showForEdit(Studio.stage, module.getName()).ifPresent(name -> {
          module.setName(name);
          rebuildRows();
          commitChange();
        }));

    Button copyButton = createActionButton(Icons.COPY, "Copy", () -> {
      Module copy = new Module();
      copy.setName(module.getName());
      copy.setMenu(module.getMenu());
      copy.setFlows(new ArrayList<>(module.getFlows()));
      getModules().add(getModules().indexOf(module) + 1, copy);
      rebuildRows();
      commitChange();
    });

    Button deleteButton = createActionButton(Icons.TRASH, "Delete", () -> {
      Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, "Delete this module?", null, null, "Delete");
      if (result.isPresent() && result.get() == ButtonType.OK) {
        getModules().remove(module);
        rebuildRows();
        commitChange();
      }
    });

    HBox actionsBox = new HBox(4.0, moveUpButton, moveDownButton, editButton, copyButton, deleteButton);
    actionsBox.setAlignment(Pos.CENTER_LEFT);
    return actionsBox;
  }

  private void moveRow(int fromIndex, int toIndex) {
    Collections.swap(getModules(), fromIndex, toIndex);
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
