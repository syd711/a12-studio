package de.a12.studio.ui.projecttree;

import de.a12.studio.ui.util.WidgetFactory;
import de.a12.studio.dataservices.services.documentmodel.features.validation.ElementValidationError;
import de.a12.studio.ui.util.Icons;
import javafx.beans.value.ChangeListener;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import org.jspecify.annotations.NonNull;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.stream.Collectors;

class ProjectTreeCell extends TreeCell<ProjectItemViewModel> {

  private static final DataFormat PROJECT_ITEM_PATH = new DataFormat("application/x-a12-project-item-path");

  private final Consumer<ProjectItemViewModel> onOpen;
  private final ProjectTreeMenuActions menuFactory;
  private final AtomicReference<ProjectItemViewModel> dragSource;

  private final FontIcon icon = new FontIcon();

  private final ChangeListener<Boolean> expandedListener = (observable, wasExpanded, expanded) -> {
    if (isLockedFolder(getItem())) {
      icon.setIconLiteral(Icons.FOLDER_LOCK_OUTLINE);
    }
    else {
      icon.setIconLiteral(expanded ? Icons.FOLDER_OPEN_OUTLINE : Icons.FOLDER_OUTLINE);
    }
  };
  private TreeItem<ProjectItemViewModel> boundTreeItem;

  ProjectTreeCell(@NonNull Consumer<ProjectItemViewModel> onOpen, @NonNull ProjectTreeMenuActions menuFactory,
                  @NonNull AtomicReference<ProjectItemViewModel> dragSource) {
    this.onOpen = onOpen;
    this.menuFactory = menuFactory;
    this.dragSource = dragSource;

    icon.getStyleClass().add("tree-icon");
    setOnMouseClicked(event -> {
      if (event.getClickCount() == 2 && !isEmpty() && getItem() != null) {
        onOpen.accept(getItem());
      }
    });
    setOnDragDetected(this::onDragDetected);
    setOnDragOver(event -> {
      if (isValidDropTarget()) {
        event.acceptTransferModes(TransferMode.MOVE);
      }
      event.consume();
    });
    setOnDragEntered(event -> {
      if (isValidDropTarget()) {
        getStyleClass().add("drag-over-target");
      }
    });
    setOnDragExited(event -> getStyleClass().remove("drag-over-target"));
    setOnDragDropped(this::onDragDropped);
    setOnDragDone(event -> dragSource.set(null));
  }

  private void onDragDetected(MouseEvent event) {
    if (isEmpty() || getItem() == null || getItem().getProjectItem().isRoot()) {
      return;
    }

    dragSource.set(getItem());
    Dragboard dragboard = startDragAndDrop(TransferMode.MOVE);
    ClipboardContent content = new ClipboardContent();
    content.put(PROJECT_ITEM_PATH, getItem().getProjectItem().getPath());
    dragboard.setContent(content);
    event.consume();
  }

  private void onDragDropped(DragEvent event) {
    boolean success = isValidDropTarget();
    if (success) {
      menuFactory.onMoveItem(dragSource.get().getProjectItem(), getItem().getProjectItem());
    }
    event.setDropCompleted(success);
    event.consume();
  }

  private boolean isValidDropTarget() {
    ProjectItemViewModel source = dragSource.get();
    if (source == null || isEmpty() || getItem() == null) {
      return false;
    }
    return menuFactory.canMoveItem(source.getProjectItem(), getItem().getProjectItem());
  }

  @Override
  protected void updateItem(ProjectItemViewModel item, boolean empty) {
    super.updateItem(item, empty);

    if (boundTreeItem != null) {
      boundTreeItem.expandedProperty().removeListener(expandedListener);
      boundTreeItem = null;
    }

    if (empty || item == null) {
      setText(null);
      setGraphic(null);
      setTooltip(null);
      setContextMenu(null);
      getStyleClass().remove("model-missing");
      getStyleClass().remove("validation-error");
      getStyleClass().remove("drag-over-target");
      return;
    }

    setText(item.getDisplayName());
    boolean locked = isLockedFolder(item);
    boolean missingModel = !item.isFolder() && !item.hasModel();
    setContextMenu(missingModel ? null : menuFactory.createTreeItemContextMenu(item));
    if (missingModel || locked) {
      if (!getStyleClass().contains("model-missing")) {
        getStyleClass().add("model-missing");
      }
    }
    else {
      getStyleClass().remove("model-missing");
    }

    List<ElementValidationError> validationErrors = item.getValidationErrors();
    if (validationErrors.isEmpty()) {
      getStyleClass().remove("validation-error");
      setTooltip(WidgetFactory.createTooltip(item.getDisplayName()));
    }
    else {
      if (!getStyleClass().contains("validation-error")) {
        getStyleClass().add("validation-error");
      }
      String messages = validationErrors.stream().map(ElementValidationError::message).collect(Collectors.joining("\n"));
      setTooltip(WidgetFactory.createTooltip(item.getDisplayName() + "\n" + messages));
    }
    if (item.isFolder()) {
      boundTreeItem = getTreeItem();
      if (locked) {
        icon.setIconLiteral(Icons.FOLDER_LOCK_OUTLINE);
      }
      else {
        icon.setIconLiteral(boundTreeItem.isExpanded() ? Icons.FOLDER_OPEN : Icons.FOLDER);
      }
      icon.setIconSize(18);
      boundTreeItem.expandedProperty().addListener(expandedListener);
      setGraphic(icon);
    }
    else {
      String iconPath = item.getIconPath();
      if (iconPath != null) {
        ImageView modelIcon = WidgetFactory.createModelIcon(iconPath);
        setGraphic(modelIcon);
      }
      else {
        icon.setIconSize(18);
        icon.setIconLiteral(Icons.FILE_OUTLINE);
        setGraphic(icon);
      }
    }
  }

  private static boolean isLockedFolder(ProjectItemViewModel item) {
    if (item == null || !item.isFolder()) {
      return false;
    }
    String name = item.getName();
    return "data".equalsIgnoreCase(name) || "resources".equalsIgnoreCase(name);
  }
}
