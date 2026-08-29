package de.a12.studio.ui.editors.overviewmodel.dialogs;

import de.a12.studio.models.overviewmodel.FilterGroup;
import de.a12.studio.models.overviewmodel.FilterItem;
import de.a12.studio.modelsvalidation.validators.ElementIndex;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.editors.overviewmodel.OverviewElementOptions;
import de.a12.studio.ui.editors.propertyeditors.RowFactory;
import de.a12.studio.ui.util.Icons;
import de.a12.studio.ui.util.StudioBundle;
import de.a12.studio.ui.util.WidgetFactory;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.jspecify.annotations.NonNull;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Edits a {@link FilterGroup}'s {@code filterItems}: one row per {@link FilterItem}, opening {@link
 * FilterItemDialogController} (via {@link Dialogs#showFilterItemForAdd}/{@link Dialogs#showFilterItemForEdit})
 * to add or edit an item. Extracted from {@link FilterGroupDialogController}, whose Add button and rows this
 * used to own directly. Not a document-model {@link de.a12.studio.models.documentmodel.Element} editor and not
 * wired through {@link de.a12.studio.ui.editors.AbstractPropertyEditor} - it mutates the {@link FilterGroup}'s
 * own list directly, and the owning dialog persists everything itself in one go once OK is pressed (see
 * {@link FilterGroupDialogController}'s deferred save mode), so there's nothing to commit per-change here.
 */
public class FilterItemsPanelController {

  @FXML
  private HBox filterItemColumnHeaders;
  @FXML
  private VBox filterItemRows;
  @FXML
  private Label filterItemsEmptyLabel;

  private Stage stage;

  private ElementIndex documentModelIndex;

  private FilterGroup group;

  void init(@NonNull Stage stage, ElementIndex documentModelIndex, @NonNull FilterGroup group) {
    this.stage = stage;
    this.documentModelIndex = documentModelIndex;
    this.group = group;
    rebuildRows();
  }

  @FXML
  private void onAddFilterItem() {
    FilterItem item = new FilterItem();
    item.setId("filter-item-" + shortId());
    if (Dialogs.showFilterItemForAdd(stage, documentModelIndex, item)) {
      group.getFilterItems().add(item);
      rebuildRows();
    }
  }

  private void rebuildRows() {
    filterItemRows.getChildren().clear();

    List<FilterItem> items = group.getFilterItems();
    boolean empty = items.isEmpty();
    filterItemColumnHeaders.setVisible(!empty);
    filterItemColumnHeaders.setManaged(!empty);
    filterItemsEmptyLabel.setVisible(empty);
    filterItemsEmptyLabel.setManaged(empty);

    for (int index = 0; index < items.size(); index++) {
      filterItemRows.getChildren().add(createRow(items.get(index), index, items.size()));
    }
  }

  private HBox createRow(FilterItem item, int index, int rowCount) {
    Label summaryLabel = new Label(summary(item));
    summaryLabel.setId("filterItemSummary-" + index);
    summaryLabel.setMaxWidth(Double.MAX_VALUE);
    summaryLabel.setCursor(Cursor.HAND);
    summaryLabel.setOnMouseClicked(event -> {
      if (event.getClickCount() == 1) {
        openEditDialog(item);
      }
    });
    HBox.setHgrow(summaryLabel, Priority.ALWAYS);

    HBox actionsBox = createActionsBox(item, index, rowCount);

    HBox row = new HBox(10.0, summaryLabel, actionsBox);
    row.setAlignment(Pos.CENTER_LEFT);
    row.getStyleClass().add("module-row");
    return row;
  }

  private String summary(FilterItem item) {
    String label = item.getLabel().stream()
        .map(de.a12.studio.models.Label::getText)
        .filter(text -> text != null && !text.isBlank())
        .findFirst()
        .orElse(null);
    if (label != null) {
      return label;
    }
    if (item.getOptions() != null && item.getOptions().getFieldId() != null) {
      return OverviewElementOptions.displayPath(documentModelIndex, item.getOptions().getFieldId());
    }
    return "";
  }

  private void openEditDialog(FilterItem item) {
    if (Dialogs.showFilterItemForEdit(stage, documentModelIndex, item)) {
      rebuildRows();
    }
  }

  private HBox createActionsBox(FilterItem item, int index, int rowCount) {
    VBox moveButtonsBox = RowFactory.createMoveButtonsBox(index, rowCount, this::moveRow);

    Button editButton = RowFactory.createActionButton(Icons.PENCIL, "Edit", () -> openEditDialog(item));

    Button deleteButton = RowFactory.createActionButton(Icons.TRASH, StudioBundle.get("delete"), () -> {
      Optional<ButtonType> confirmResult = WidgetFactory.showConfirmation(Studio.stage, StudioBundle.get("delete_this_filter_item"), null, null, StudioBundle.get("delete"));
      if (confirmResult.isPresent() && confirmResult.get() == ButtonType.OK) {
        group.getFilterItems().remove(item);
        rebuildRows();
      }
    });

    HBox actionsBox = new HBox(4.0, moveButtonsBox, editButton, deleteButton);
    actionsBox.setAlignment(Pos.CENTER_LEFT);
    return actionsBox;
  }

  private void moveRow(int fromIndex, int toIndex) {
    Collections.swap(group.getFilterItems(), fromIndex, toIndex);
    rebuildRows();
  }

  private static String shortId() {
    return UUID.randomUUID().toString().replace("-", "").substring(0, 5);
  }
}
