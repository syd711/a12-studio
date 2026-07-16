package de.a12.studio.ui.projecttree;

import de.a12.studio.commons.util.WidgetFactory;
import de.a12.studio.dataservices.services.documentmodel.features.validation.ElementValidationError;
import de.a12.studio.ui.util.Icons;
import javafx.beans.value.ChangeListener;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.jspecify.annotations.NonNull;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

class ProjectTreeCell extends TreeCell<ProjectItemViewModel> {

  private static final Map<String, Image> MODEL_ICON_CACHE = new HashMap<>();

  private final Consumer<ProjectItemViewModel> onOpen;
  private final ProjectTreeMenuActions menuFactory;

  private final FontIcon icon = new FontIcon();
  private final ImageView modelIcon = new ImageView();

  private final ChangeListener<Boolean> expandedListener = (observable, wasExpanded, expanded) ->
      icon.setIconLiteral(expanded ? Icons.FOLDER_OPEN_OUTLINE : Icons.FOLDER_OUTLINE);
  private TreeItem<ProjectItemViewModel> boundTreeItem;

  ProjectTreeCell(@NonNull Consumer<ProjectItemViewModel> onOpen, @NonNull ProjectTreeMenuActions menuFactory) {
    this.onOpen = onOpen;
    this.menuFactory = menuFactory;

    icon.getStyleClass().add("tree-icon");
    modelIcon.getStyleClass().add("tree-icon");
    modelIcon.setFitWidth(18);
    modelIcon.setFitHeight(18);
    modelIcon.setPreserveRatio(true);
    setOnMouseClicked(event -> {
      if (event.getClickCount() == 2 && !isEmpty() && getItem() != null) {
        onOpen.accept(getItem());
      }
    });
  }

  private static Image loadModelIcon(@NonNull String iconPath) {
    return MODEL_ICON_CACHE.computeIfAbsent(iconPath,
        path -> new Image(ProjectTreeCell.class.getResourceAsStream(path), 18, 18, true, true));
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
      return;
    }

    setText(item.toString());
    boolean missingModel = !item.isFolder() && !item.hasModel();
    setContextMenu(missingModel ? null : menuFactory.createTreeItemContextMenu(item));
    if (missingModel) {
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
      setTooltip(WidgetFactory.createTooltip(item.getName()));
    }
    else {
      if (!getStyleClass().contains("validation-error")) {
        getStyleClass().add("validation-error");
      }
      String messages = validationErrors.stream().map(ElementValidationError::message).collect(Collectors.joining("\n"));
      setTooltip(WidgetFactory.createTooltip(item.getName() + "\n" + messages));
    }
    if (item.isFolder()) {
      boundTreeItem = getTreeItem();
      icon.setIconLiteral(boundTreeItem.isExpanded() ? Icons.FOLDER_OPEN : Icons.FOLDER);
      icon.setIconSize(18);
      boundTreeItem.expandedProperty().addListener(expandedListener);
      setGraphic(icon);
    }
    else {
      String iconPath = item.getIconPath();
      if (iconPath != null) {
        modelIcon.setImage(loadModelIcon(iconPath));
        setGraphic(modelIcon);
      }
      else {
        icon.setIconSize(18);
        icon.setIconLiteral(Icons.FILE_OUTLINE);
        setGraphic(icon);
      }
    }
  }
}
