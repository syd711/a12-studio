package de.a12.studio.ui.projecttree;

import de.a12.studio.plugin.manager.ICreateItemMenuEntry;
import de.a12.studio.plugin.manager.PluginManager;
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
import java.util.List;
import org.jspecify.annotations.NonNull;
import org.kordamp.ikonli.javafx.FontIcon;

class ProjectTreeContextMenu {

  private final ProjectTreeMenuActions actions;

  ProjectTreeContextMenu(@NonNull ProjectTreeMenuActions actions) {
    this.actions = actions;
  }

  ContextMenu create(@NonNull ProjectItemViewModel viewModel) {
    ProjectItem projectItem = viewModel.getProjectItem();

    Menu newMenu = new Menu(StudioBundle.get("new"));
    for (ModelType modelType : ModelType.values()) {
      if (modelType == ModelType.DOCUMENT) {
        // Replace the plain Document Model entry with a submenu that offers additional import options.
        Menu documentMenu = new Menu(modelType.getDisplayName());
        documentMenu.setGraphic(WidgetFactory.createModelIcon(Icons.forModelType(modelType)));

        MenuItem createBlank = new MenuItem(StudioBundle.get("new_document_model.create_blank"));
        createBlank.setOnAction(event -> actions.onCreateNewModel(projectItem, ModelType.DOCUMENT));
        documentMenu.getItems().add(createBlank);

        // Append plugin-contributed "createMenu" extension points as import options.
        List<ICreateItemMenuEntry> pluginEntries = PluginManager.getInstance().getCreateMenuEntries();
        if (!pluginEntries.isEmpty()) {
          documentMenu.getItems().add(new SeparatorMenuItem());
          for (ICreateItemMenuEntry entry : pluginEntries) {
            MenuItem pluginItem = new MenuItem(entry.getMenuLabel());
            javafx.scene.Node graphic = entry.getMenuGraphic();
            if (graphic != null) {
              withMenuIconStyle(graphic);
              pluginItem.setGraphic(graphic);
            }
            pluginItem.setOnAction(event -> actions.executePluginEntry(entry, projectItem));
            documentMenu.getItems().add(pluginItem);
          }
        }

        if (documentMenu.getItems().size() == 1) {
          // No import options were contributed; render the blank-create entry directly instead of a single-entry submenu.
          createBlank.setGraphic(WidgetFactory.createModelIcon(Icons.forModelType(modelType)));
          newMenu.getItems().add(createBlank);
        }
        else {
          newMenu.getItems().add(documentMenu);
        }
      }
      else {
        MenuItem modelItem = new MenuItem(modelType.getDisplayName());
        modelItem.setGraphic(WidgetFactory.createModelIcon(Icons.forModelType(modelType)));
        modelItem.setOnAction(event -> actions.onCreateNewModel(projectItem, modelType));
        newMenu.getItems().add(modelItem);
      }
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
