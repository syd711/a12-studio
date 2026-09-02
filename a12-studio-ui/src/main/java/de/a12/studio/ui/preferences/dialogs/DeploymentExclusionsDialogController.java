package de.a12.studio.ui.preferences.dialogs;

import de.a12.studio.models.auth.AuthFileType;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.ui.components.DialogController;
import de.a12.studio.ui.util.StudioBundle;
import de.a12.studio.ui.util.WidgetFactory;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Controller for the "Edit Deployment Exclusions" dialog.
 *
 * <p>Displays the project tree as a {@link TreeTableView} containing only model files
 * (JSON models, no {@code settings.json}, no {@code roles.yaml} / {@code users.yaml}).
 * Each row has a checkbox; ticking it marks the corresponding project path for exclusion
 * from deployment. Folder nodes are shown to provide structure; ticking a folder marks all
 * of its descendant model files as excluded.
 */
@Slf4j
public class DeploymentExclusionsDialogController implements DialogController {

  // -------------------------------------------------------------------------
  // FXML fields
  // -------------------------------------------------------------------------

  @FXML
  private TreeTableView<ProjectItem> modelTree;

  @FXML
  private TreeTableColumn<ProjectItem, Boolean> checkColumn;

  @FXML
  private TreeTableColumn<ProjectItem, String> nameColumn;

  @FXML
  private Button okButton;

  @FXML
  private Button cancelButton;

  // -------------------------------------------------------------------------
  // State
  // -------------------------------------------------------------------------

  private Stage stage;

  private Optional<ButtonType> result = Optional.of(ButtonType.CANCEL);

  /**
   * Selected state per project path. Populated from the existing exclusion list on open;
   * mutated by checkbox interactions.
   */
  private final Map<String, SimpleBooleanProperty> checkedByPath = new HashMap<>();

  // -------------------------------------------------------------------------
  // Initialisation
  // -------------------------------------------------------------------------

  @FXML
  private void initialize() {
    checkColumn.setCellValueFactory(param -> {
      ProjectItem item = param.getValue().getValue();
      if (item == null) {
        return new SimpleBooleanProperty(false);
      }
      return checkedByPath.computeIfAbsent(item.getPath(), p -> new SimpleBooleanProperty(false));
    });

    checkColumn.setCellFactory(col -> new TreeTableCell<>() {
      private final CheckBox checkBox = new CheckBox();

      {
        checkBox.setFocusTraversable(false);
        checkBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
          ProjectItem item = getTableRow() == null ? null : getTableRow().getItem();
          if (item == null || isEmpty()) {
            return;
          }
          setCheckedRecursive(getTreeTableRow().getTreeItem(), newVal);
        });
      }

      @Override
      protected void updateItem(Boolean value, boolean empty) {
        super.updateItem(value, empty);
        if (empty || value == null) {
          setGraphic(null);
          return;
        }
        ProjectItem item = getTableRow() == null ? null : getTableRow().getItem();
        if (item == null) {
          setGraphic(null);
          return;
        }
        SimpleBooleanProperty prop = checkedByPath.computeIfAbsent(item.getPath(), p -> new SimpleBooleanProperty(false));
        checkBox.selectedProperty().unbind();
        checkBox.setSelected(prop.get());
        prop.addListener((o, ov, nv) -> checkBox.setSelected(nv));
        setGraphic(checkBox);
      }
    });

    nameColumn.setCellValueFactory(param -> {
      ProjectItem item = param.getValue().getValue();
      if (item == null) {
        return new javafx.beans.property.ReadOnlyStringWrapper("");
      }
      String name = item.getName();
      if (!item.isFolder()) {
        int dot = name.lastIndexOf('.');
        if (dot > 0) {
          name = name.substring(0, dot);
        }
      }
      return new javafx.beans.property.ReadOnlyStringWrapper(name);
    });

    modelTree.setShowRoot(true);
  }

  // -------------------------------------------------------------------------
  // Public API
  // -------------------------------------------------------------------------

  /**
   * Populates the tree from the given project root item and pre-selects the paths
   * that are already excluded.
   */
  public void setProject(@NonNull ProjectItem projectRoot,
                         @NonNull List<String> currentExclusions) {
    for (String path : currentExclusions) {
      checkedByPath.put(path, new SimpleBooleanProperty(true));
    }
    TreeItem<ProjectItem> root = buildTree(projectRoot);
    if (root != null) {
      modelTree.setRoot(root);
      expandAll(root);
    }
  }

  /**
   * Returns the paths the user has ticked (only non-folder paths, since folders are
   * structural only and not stored in the exclusion list themselves).
   */
  @NonNull
  public List<String> getSelectedPaths() {
    List<String> paths = new ArrayList<>();
    for (Map.Entry<String, SimpleBooleanProperty> entry : checkedByPath.entrySet()) {
      if (entry.getValue().get()) {
        paths.add(entry.getKey());
      }
    }
    return paths;
  }

  // -------------------------------------------------------------------------
  // Event handlers
  // -------------------------------------------------------------------------

  @FXML
  private void onDialogSubmit() {
    result = Optional.of(ButtonType.OK);
    stage.close();
  }

  @Override
  public void onDialogCancel() {
    stage.close();
  }

  // -------------------------------------------------------------------------
  // Tree building
  // -------------------------------------------------------------------------

  /**
   * Builds a {@link TreeItem} subtree from {@code item}, including only:
   * <ul>
   *   <li>Folders (always included to provide structure), pruned if they contain no
   *       eligible children after filtering.</li>
   *   <li>JSON model files — i.e. {@code .json} files that are <em>not</em>
   *       {@code settings.json} and not an auth file ({@code roles.yaml},
   *       {@code users.yaml}).</li>
   * </ul>
   */
  @Nullable
  private TreeItem<ProjectItem> buildTree(@NonNull ProjectItem item) {
    if (item.isFolder()) {
      TreeItem<ProjectItem> treeItem = new TreeItem<>(item);
      for (ProjectItem child : item.getChildren()) {
        TreeItem<ProjectItem> childItem = buildTree(child);
        if (childItem != null) {
          treeItem.getChildren().add(childItem);
        }
      }
      // Keep folders only if they contain at least one eligible descendant.
      return treeItem.getChildren().isEmpty() ? null : treeItem;
    }

    // Filter: include only JSON model files (exclude settings.json and auth files).
    if (isModelFile(item)) {
      return new TreeItem<>(item);
    }
    return null;
  }

  private boolean isModelFile(@NonNull ProjectItem item) {
    if (item.isFolder()) {
      return false;
    }
    String name = item.getName();
    // Exclude settings.json at the project root and auth files (roles.yaml, users.yaml).
    if ("settings.json".equals(name)) {
      return false;
    }
    if (AuthFileType.fromFileName(name) != null) {
      return false;
    }
    return name.endsWith(".json");
  }

  private void expandAll(@NonNull TreeItem<ProjectItem> item) {
    item.setExpanded(true);
    for (TreeItem<ProjectItem> child : item.getChildren()) {
      expandAll(child);
    }
  }

  /**
   * When a folder checkbox is toggled, propagates the value to all descendant model files.
   */
  private void setCheckedRecursive(@NonNull TreeItem<ProjectItem> treeItem, boolean checked) {
    ProjectItem item = treeItem.getValue();
    if (item != null) {
      checkedByPath.computeIfAbsent(item.getPath(), p -> new SimpleBooleanProperty(false)).set(checked);
    }
    for (TreeItem<ProjectItem> child : treeItem.getChildren()) {
      setCheckedRecursive(child, checked);
    }
  }

  // -------------------------------------------------------------------------
  // Static factory
  // -------------------------------------------------------------------------

  /**
   * Opens the dialog modally and returns the selected paths, or empty if cancelled.
   *
   * @param owner          the owner stage
   * @param projectRoot    the root {@link ProjectItem} of the open project
   * @param currentExclusions the currently saved exclusion paths
   * @return the new exclusion list, or empty if the dialog was cancelled
   */
  @NonNull
  public static Optional<List<String>> show(@NonNull Stage owner,
                                            @NonNull ProjectItem projectRoot,
                                            @NonNull List<String> currentExclusions) {
    try {
      FXMLLoader loader = new FXMLLoader(
          DeploymentExclusionsDialogController.class.getResource(
              "/de/a12/studio/ui/preferences/dialogs/dialog-deployment-exclusions.fxml"));
      loader.setResources(StudioBundle.getBundle());
      Stage stage = WidgetFactory.createDialogStage(
          "dialog-deployment-exclusions", loader, owner,
          StudioBundle.get("deployment_exclusions_dialog_title"));
      DeploymentExclusionsDialogController controller = loader.getController();
      controller.stage = stage;
      controller.setProject(projectRoot, currentExclusions);
      stage.showAndWait();

      if (controller.result.isPresent() && controller.result.get() == ButtonType.OK) {
        return Optional.of(controller.getSelectedPaths());
      }
      return Optional.empty();
    }
    catch (IOException e) {
      log.error("Failed to open deployment exclusions dialog", e);
      return Optional.empty();
    }
  }
}
