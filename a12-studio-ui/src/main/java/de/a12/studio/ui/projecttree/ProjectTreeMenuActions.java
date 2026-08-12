package de.a12.studio.ui.projecttree;

import de.a12.studio.ui.components.StudioFolderChooser;
import de.a12.studio.ui.util.StudioBundle;
import de.a12.studio.ui.util.WidgetFactory;
import de.a12.studio.ui.util.zip.ZipUtil;
import de.a12.studio.models.ModelType;
import de.a12.studio.models.NewModelFactory;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.ui.events.StudioEventManager;
import de.a12.studio.ui.projecttree.dialogs.NewModelDialogController;
import de.a12.studio.ui.projecttree.dialogs.NewModelDialogController.NewModelInput;
import de.a12.studio.ui.util.Icons;
import javafx.scene.Node;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.stage.Stage;
import org.jspecify.annotations.NonNull;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ProjectTreeMenuActions {

  private final Supplier<Stage> stageSupplier;
  private final Runnable onReload;
  private final Consumer<ProjectItemViewModel> onOpen;

  public ProjectTreeMenuActions(@NonNull Supplier<Stage> stageSupplier, @NonNull Runnable onReload,
                                @NonNull Consumer<ProjectItemViewModel> onOpen) {
    this.stageSupplier = stageSupplier;
    this.onReload = onReload;
    this.onOpen = onOpen;
  }

  public ContextMenu createTreeItemContextMenu(@NonNull ProjectItemViewModel viewModel) {
    ProjectItem projectItem = viewModel.getProjectItem();

    Menu newMenu = new Menu(StudioBundle.get("new"));
    MenuItem newFolder = new MenuItem(StudioBundle.get("new_folder"));
    newFolder.setOnAction(event -> onCreateNewFolder(projectItem));
    newFolder.setGraphic(withMenuIconStyle(WidgetFactory.createIcon("mdi2f-folder-plus-outline")));
    MenuItem newModel = new MenuItem(StudioBundle.get("new_model"));
    newModel.setOnAction(event -> onCreateNewModel(projectItem));
    newModel.setGraphic(withMenuIconStyle(WidgetFactory.createIcon("mdi2f-file-document-plus-outline")));
    newMenu.getItems().addAll(newFolder, newModel);

    MenuItem open = new MenuItem(StudioBundle.get("open"));
    open.setDisable(viewModel.isFolder());
    open.setOnAction(event -> onOpen.accept(viewModel));

    MenuItem rename = new MenuItem(StudioBundle.get("rename"));
    rename.setDisable(projectItem.isRoot() || viewModel.isSettings() || viewModel.isAuthFile());
    rename.setOnAction(event -> onRenameItem(projectItem));

    MenuItem createCopy = new MenuItem(StudioBundle.get("create_copy"));
    createCopy.setGraphic(withMenuIconStyle(WidgetFactory.createIcon(Icons.COPY)));
    createCopy.setDisable(projectItem.isRoot() || viewModel.isSettings() || viewModel.isAuthFile());
    createCopy.setOnAction(event -> onCreateCopy(projectItem));

    MenuItem zipFolder = new MenuItem(StudioBundle.get("zip_folder"));
    zipFolder.setGraphic(withMenuIconStyle(WidgetFactory.createIcon(Icons.ZIP)));
    zipFolder.setVisible(projectItem.isRoot());
    zipFolder.setOnAction(event -> onZipFolder(projectItem));

    MenuItem delete = new MenuItem(StudioBundle.get("delete"));
    delete.setGraphic(withMenuIconStyle(WidgetFactory.createIcon(Icons.TRASH)));
    delete.setDisable(projectItem.isRoot() || viewModel.isSettings() || viewModel.isAuthFile());
    delete.setOnAction(event -> onDeleteItem(projectItem));

    return new ContextMenu(newMenu, open, rename, createCopy, new SeparatorMenuItem(), zipFolder, delete);
  }

  private static Node withMenuIconStyle(@NonNull Node icon) {
    icon.getStyleClass().add("menu-icon");
    return icon;
  }

  void onCreateNewFolder(@NonNull ProjectItem parent) {
    String title = StudioBundle.get("new_folder_title");
    String name = WidgetFactory.showInputDialog(getStage(), title, title, null, null, null);
    if (name == null || name.isBlank()) {
      return;
    }

    try {
      parent.createChildFolder(name.trim());
      onReload.run();
    }
    catch (IOException e) {
      showError(StudioBundle.get("could_not_create_item", name), e);
    }
  }

  void onCreateNewModel(@NonNull ProjectItem parent) {
    onCreateNewModel(parent, null);
  }

  void onCreateNewModel(@NonNull ProjectItem parent, ModelType preselectedType) {
    Optional<NewModelInput> input = NewModelDialogController.show(getStage(), parent, preselectedType);
    if (input.isEmpty()) {
      return;
    }

    ModelType modelType = input.get().modelType();
    String name = input.get().name();
    try {
      ProjectItem item = NewModelFactory.createModel(parent, modelType, name);
      onReload.run();
      onOpen.accept(new ProjectItemViewModel(item, Map.of()));
    }
    catch (IOException e) {
      showError(StudioBundle.get("could_not_create_item", name), e);
    }
  }

  void onRenameItem(@NonNull ProjectItem item) {
    String title = StudioBundle.get("rename_title");
    String name = WidgetFactory.showInputDialog(getStage(), title, title, null, null, item.getName());
    if (name == null || name.isBlank() || name.equals(item.getName())) {
      return;
    }

    String oldPath = item.getPath();
    try {
      item.renameTo(name.trim());
      StudioEventManager.getInstance().fireModelRenamedEvent(oldPath, item);
      onReload.run();
    }
    catch (IOException e) {
      showError(StudioBundle.get("could_not_rename_to", name), e);
    }
  }

  void onCreateCopy(@NonNull ProjectItem item) {
    try {
      item.createCopy();
      onReload.run();
    }
    catch (IOException e) {
      showError(StudioBundle.get("could_not_copy_item", item.getName()), e);
    }
  }

  void onZipFolder(@NonNull ProjectItem item) {
    StudioFolderChooser chooser = new StudioFolderChooser();
    chooser.setTitle(StudioBundle.get("choose_destination_folder"));
    File destinationFolder = chooser.showOpenDialog(getStage());
    if (destinationFolder == null) {
      return;
    }

    String dateSuffix = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HHmmss"));
    File zipFile = new File(destinationFolder, item.getName() + "_" + dateSuffix + ".zip");

    try {
      ZipUtil.zipFolder(item.getFile(), zipFile, (file, path) -> { });
    }
    catch (IOException e) {
      showError(StudioBundle.get("could_not_zip_item", item.getName()), e);
    }
  }

  boolean canMoveItem(@NonNull ProjectItem source, @NonNull ProjectItem targetFolder) {
    if (source.isRoot() || !targetFolder.isFolder()) {
      return false;
    }
    if (source.equals(targetFolder) || source.isAncestorOf(targetFolder)) {
      return false;
    }
    return !targetFolder.equals(source.getParent());
  }

  void onMoveItem(@NonNull ProjectItem source, @NonNull ProjectItem targetFolder) {
    if (!canMoveItem(source, targetFolder)) {
      return;
    }

    try {
      source.moveTo(targetFolder);
      onReload.run();
    }
    catch (IOException e) {
      showError(StudioBundle.get("could_not_move_item", source.getName(), targetFolder.getName()), e);
    }
  }

  void onDeleteItem(@NonNull ProjectItem item) {
    Optional<ButtonType> result = WidgetFactory.showConfirmation(getStage(),
        StudioBundle.get("confirm_delete_item", item.getName()), null, null, StudioBundle.get("delete"));
    if (result.isPresent() && result.get() == ButtonType.OK) {
      try {
        item.delete();
        StudioEventManager.getInstance().fireModelDeletedEvent(item);
        onReload.run();
      }
      catch (IOException e) {
        showError(StudioBundle.get("could_not_delete_item", item.getName()), e);
      }
    }
  }

  private void showError(@NonNull String message, @NonNull Exception e) {
    WidgetFactory.showAlert(getStage(), message, e.getMessage());
  }

  private Stage getStage() {
    return stageSupplier.get();
  }
}
