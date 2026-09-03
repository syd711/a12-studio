package de.a12.studio.ui.projecttree;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.ModelType;
import de.a12.studio.models.NewModelFactory;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.models.util.ModelReferenceRewriter;
import de.a12.studio.plugin.manager.ICreateItemMenuEntry;
import de.a12.studio.ui.components.StudioFolderChooser;
import de.a12.studio.ui.events.StudioEventManager;
import de.a12.studio.ui.projecttree.dialogs.NewModelDialogController;
import de.a12.studio.ui.projecttree.dialogs.NewModelDialogController.NewModelInput;
import de.a12.studio.ui.editors.propertyeditors.RolesEditorPanelController;
import de.a12.studio.ui.util.ProjectModelFolders;
import de.a12.studio.ui.util.StudioBundle;
import de.a12.studio.ui.util.WidgetFactory;
import de.a12.studio.ui.util.zip.ZipUtil;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Slf4j
public class ProjectTreeMenuActions {

  private final Supplier<Stage> stageSupplier;
  private final Runnable onReload;
  private final Consumer<ProjectItemViewModel> onOpen;
  private final Supplier<ProjectItem> projectRootSupplier;

  public ProjectTreeMenuActions(@NonNull Supplier<Stage> stageSupplier, @NonNull Runnable onReload,
                                @NonNull Consumer<ProjectItemViewModel> onOpen,
                                @NonNull Supplier<ProjectItem> projectRootSupplier) {
    this.stageSupplier = stageSupplier;
    this.onReload = onReload;
    this.onOpen = onOpen;
    this.projectRootSupplier = projectRootSupplier;
  }

  void onOpenItem(@NonNull ProjectItemViewModel item) {
    onOpen.accept(item);
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
    ProjectItem targetFolder = parent.isRoot() ? ProjectModelFolders.resolveDefaultModelFolder(parent) : parent;
    Optional<NewModelInput> input = NewModelDialogController.show(getStage(), targetFolder, preselectedType);
    if (input.isEmpty()) {
      return;
    }

    ModelType modelType = input.get().modelType();
    String name = input.get().name();
    String documentModelId = input.get().documentModelId();
    List<String> roles = input.get().roles();
    ProjectItem selectedFolder = input.get().folder();
    boolean buildScreensFromFields = input.get().buildScreensFromFields();
    try {
      ProjectItem item = NewModelFactory.createModel(selectedFolder, modelType, name, documentModelId, buildScreensFromFields);
      if (!roles.isEmpty()) {
        RolesEditorPanelController.applyRoles(item.getModel(), roles);
        item.save();
      }
      onReload.run();
      onOpen.accept(new ProjectItemViewModel(item, Map.of()));
    }
    catch (IOException e) {
      showError(StudioBundle.get("could_not_create_item", name), e);
    }
  }

  void executePluginEntry(@NonNull ICreateItemMenuEntry entry, @NonNull ProjectItem targetFolder) {
    ProjectItem resolvedTargetFolder = targetFolder.isRoot() ? ProjectModelFolders.resolveDefaultModelFolder(targetFolder) : targetFolder;
    entry.execute(getStage(), resolvedTargetFolder);
  }

  void onRenameItem(@NonNull ProjectItem item) {
    String title = StudioBundle.get("rename_title");
    String name = WidgetFactory.showInputDialog(getStage(), title, title, null, null, item.getName());
    if (name == null || name.isBlank() || name.equals(item.getName())) {
      return;
    }

    String oldId = item.isFolder() ? null : item.getModel() != null ? item.getModel().getId() : null;
    String oldPath = item.getPath();

    try {
      item.renameTo(name.trim());
    }
    catch (IOException e) {
      showError(StudioBundle.get("could_not_rename_to", name), e);
      return;
    }

    // For model files: rewrite all cross-references in the project that pointed at the old id.
    if (!item.isFolder() && oldId != null && item.getModel() != null) {
      String newId = item.getModel().getId();
      if (!newId.equals(oldId)) {
        rewriteProjectReferences(oldId, newId);
      }
    }

    StudioEventManager.getInstance().fireModelRenamedEvent(oldPath, item);
    onReload.run();
  }

  /**
   * Walks the entire project tree and rewrites every cross-reference that pointed at {@code oldId}
   * to {@code newId} – both in {@code header.modelReferences} and in all content fields that the
   * model schemas use as model-id references (see {@link ModelReferenceRewriter#REFERENCE_FIELD_NAMES}).
   * Models that were changed are saved immediately.
   */
  private void rewriteProjectReferences(String oldId, String newId) {
    ProjectItem root = projectRootSupplier.get();
    if (root == null) {
      return;
    }
    Map<String, String> idMap = Map.of(oldId, newId);
    List<ProjectItem> allItems = new ArrayList<>();
    collectModelItems(root, allItems);
    for (ProjectItem candidate : allItems) {
      A12Model<?> model = candidate.getModel();
      if (model == null) {
        continue;
      }
      try {
        if (ModelReferenceRewriter.rewriteReferences(model, idMap)) {
          candidate.save();
          log.info("Updated reference {} -> {} in {}", oldId, newId, candidate.getPath());
        }
      }
      catch (Exception e) {
        log.warn("Failed to rewrite references in {}: {}", candidate.getPath(), e.getMessage(), e);
      }
    }
  }

  private static void collectModelItems(ProjectItem item, List<ProjectItem> result) {
    if (item.isFolder()) {
      for (ProjectItem child : item.getChildren()) {
        collectModelItems(child, result);
      }
    }
    else if (item.getModel() != null) {
      result.add(item);
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
