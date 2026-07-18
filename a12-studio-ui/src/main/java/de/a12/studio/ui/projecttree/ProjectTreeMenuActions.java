package de.a12.studio.ui.projecttree;

import de.a12.studio.ui.components.StudioFolderChooser;
import de.a12.studio.ui.util.WidgetFactory;
import de.a12.studio.ui.util.zip.ZipUtil;
import de.a12.studio.dataservices.models.ModelType;
import de.a12.studio.dataservices.models.NewModelFactory;
import de.a12.studio.dataservices.projects.ProjectItem;
import de.a12.studio.ui.projecttree.NewModelDialogController.NewModelInput;
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

    Menu newMenu = new Menu("_New...");
    MenuItem newFolder = new MenuItem("_Folder");
    newFolder.setOnAction(event -> onCreateNewFolder(projectItem));
    newFolder.setGraphic(withMenuIconStyle(WidgetFactory.createIcon("mdi2f-folder-plus-outline")));
    MenuItem newModel = new MenuItem("_Model");
    newModel.setOnAction(event -> onCreateNewModel(projectItem));
    newModel.setGraphic(withMenuIconStyle(WidgetFactory.createIcon("mdi2f-file-document-plus-outline")));
    newMenu.getItems().addAll(newFolder, newModel);

    MenuItem open = new MenuItem("_Open");
    open.setDisable(viewModel.isFolder());
    open.setOnAction(event -> onOpen.accept(viewModel));

    MenuItem rename = new MenuItem("_Rename");
    rename.setDisable(projectItem.isRoot());
    rename.setOnAction(event -> onRenameItem(projectItem));

    MenuItem createCopy = new MenuItem("_Create Copy");
    createCopy.setGraphic(withMenuIconStyle(WidgetFactory.createIcon(Icons.COPY)));
    createCopy.setDisable(projectItem.isRoot());
    createCopy.setOnAction(event -> onCreateCopy(projectItem));

    MenuItem zipFolder = new MenuItem("_Zip Folder");
    zipFolder.setGraphic(withMenuIconStyle(WidgetFactory.createIcon(Icons.ZIP)));
    zipFolder.setVisible(projectItem.isRoot());
    zipFolder.setOnAction(event -> onZipFolder(projectItem));

    MenuItem delete = new MenuItem("_Delete");
    delete.setGraphic(withMenuIconStyle(WidgetFactory.createIcon(Icons.TRASH)));
    delete.setDisable(projectItem.isRoot());
    delete.setOnAction(event -> onDeleteItem(projectItem));

    return new ContextMenu(newMenu, open, rename, createCopy, new SeparatorMenuItem(), zipFolder, delete);
  }

  private static Node withMenuIconStyle(@NonNull Node icon) {
    icon.getStyleClass().add("menu-icon");
    return icon;
  }

  void onCreateNewFolder(@NonNull ProjectItem parent) {
    String name = WidgetFactory.showInputDialog(getStage(), "New Folder", "New Folder", null, null, null);
    if (name == null || name.isBlank()) {
      return;
    }

    try {
      parent.createChildFolder(name.trim());
      onReload.run();
    }
    catch (IOException e) {
      showError("Could not create '" + name + "'", e);
    }
  }

  void onCreateNewModel(@NonNull ProjectItem parent) {
    Optional<NewModelInput> input = NewModelDialogController.show(getStage());
    if (input.isEmpty()) {
      return;
    }

    ModelType modelType = input.get().modelType();
    String name = input.get().name();
    try {
      NewModelFactory.createModel(parent, modelType, name);
      onReload.run();
    }
    catch (IOException e) {
      showError("Could not create '" + name + "'", e);
    }
  }

  void onRenameItem(@NonNull ProjectItem item) {
    String name = WidgetFactory.showInputDialog(getStage(), "Rename", "Rename", null, null, item.getName());
    if (name == null || name.isBlank() || name.equals(item.getName())) {
      return;
    }

    try {
      item.renameTo(name.trim());
      onReload.run();
    }
    catch (IOException e) {
      showError("Could not rename to '" + name + "'", e);
    }
  }

  void onCreateCopy(@NonNull ProjectItem item) {
    try {
      item.createCopy();
      onReload.run();
    }
    catch (IOException e) {
      showError("Could not copy '" + item.getName() + "'", e);
    }
  }

  void onZipFolder(@NonNull ProjectItem item) {
    StudioFolderChooser chooser = new StudioFolderChooser();
    chooser.setTitle("Choose Destination Folder");
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
      showError("Could not zip '" + item.getName() + "'", e);
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
      showError("Could not move '" + source.getName() + "' to '" + targetFolder.getName() + "'", e);
    }
  }

  void onDeleteItem(@NonNull ProjectItem item) {
    Optional<ButtonType> result = WidgetFactory.showConfirmation(getStage(), "Delete '" + item.getName() + "'?", null, null, "Delete");
    if (result.isPresent() && result.get() == ButtonType.OK) {
      try {
        item.delete();
        onReload.run();
      }
      catch (IOException e) {
        showError("Could not delete '" + item.getName() + "'", e);
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
