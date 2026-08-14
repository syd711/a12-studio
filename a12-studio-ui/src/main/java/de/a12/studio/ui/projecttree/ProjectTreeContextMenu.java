package de.a12.studio.ui.projecttree;

import de.a12.studio.ui.util.StudioBundle;
import de.a12.studio.ui.util.WidgetFactory;
import de.a12.studio.models.ModelType;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.ui.util.Icons;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import org.jspecify.annotations.NonNull;

class ProjectTreeContextMenu {

  private final ProjectTreeMenuActions actions;

  ProjectTreeContextMenu(@NonNull ProjectTreeMenuActions actions) {
    this.actions = actions;
  }

  ContextMenu create(@NonNull ProjectItemViewModel viewModel) {
    ProjectItem projectItem = viewModel.getProjectItem();

    Menu newMenu = new Menu(StudioBundle.get("new"));
    for (ModelType modelType : ModelType.values()) {
      MenuItem modelItem = new MenuItem(modelType.getDisplayName());
      modelItem.setGraphic(WidgetFactory.createModelIcon(Icons.forModelType(modelType)));
      modelItem.setOnAction(event -> actions.onCreateNewModel(projectItem, modelType));
      newMenu.getItems().add(modelItem);
    }
    newMenu.getItems().add(new SeparatorMenuItem());
    MenuItem newFolder = new MenuItem(StudioBundle.get("new_folder"));
    newFolder.setOnAction(event -> actions.onCreateNewFolder(projectItem));
    newFolder.setGraphic(withMenuIconStyle(WidgetFactory.createIcon(Icons.FOLDER_OUTLINE)));
    newMenu.getItems().add(newFolder);

    MenuItem open = new MenuItem(StudioBundle.get("open"));
    open.setDisable(viewModel.isFolder());
    open.setOnAction(event -> actions.onOpenItem(viewModel));

    MenuItem rename = new MenuItem(StudioBundle.get("rename"));
    rename.setDisable(projectItem.isRoot() || viewModel.isSettings() || viewModel.isAuthFile());
    rename.setOnAction(event -> actions.onRenameItem(projectItem));

    MenuItem createCopy = new MenuItem(StudioBundle.get("create_copy"));
    createCopy.setGraphic(withMenuIconStyle(WidgetFactory.createIcon(Icons.COPY)));
    createCopy.setDisable(projectItem.isRoot() || viewModel.isSettings() || viewModel.isAuthFile());
    createCopy.setOnAction(event -> actions.onCreateCopy(projectItem));

    MenuItem zipFolder = new MenuItem(StudioBundle.get("zip_folder"));
    zipFolder.setGraphic(withMenuIconStyle(WidgetFactory.createIcon(Icons.ZIP)));
    zipFolder.setVisible(projectItem.isRoot());
    zipFolder.setOnAction(event -> actions.onZipFolder(projectItem));

    MenuItem delete = new MenuItem(StudioBundle.get("delete"));
    delete.setGraphic(withMenuIconStyle(WidgetFactory.createIcon(Icons.TRASH)));
    delete.setDisable(projectItem.isRoot() || viewModel.isSettings() || viewModel.isAuthFile());
    delete.setOnAction(event -> actions.onDeleteItem(projectItem));

    return new ContextMenu(newMenu, open, rename, createCopy, new SeparatorMenuItem(), zipFolder, delete);
  }

  private static Node withMenuIconStyle(@NonNull Node icon) {
    icon.getStyleClass().add("menu-icon");
    return icon;
  }
}
