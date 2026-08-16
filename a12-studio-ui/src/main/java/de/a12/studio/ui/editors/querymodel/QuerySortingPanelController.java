package de.a12.studio.ui.editors.querymodel;

import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.models.querymodel.QueryModel;
import de.a12.studio.models.querymodel.QueryModelContent;
import de.a12.studio.models.querymodel.QuerySort;
import de.a12.studio.models.querymodel.QuerySortBy;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.editors.AbstractPropertyEditor;
import de.a12.studio.ui.editors.propertyeditors.RowFactory;
import de.a12.studio.ui.editors.querymodel.dialogs.Dialogs;
import de.a12.studio.ui.util.Icons;
import de.a12.studio.ui.util.StudioBundle;
import de.a12.studio.ui.util.WidgetFactory;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.input.DataFormat;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.jspecify.annotations.NonNull;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Edits a {@link QueryModel}'s {@code content.sort}: one reorderable row per {@link QuerySort}, summarizing its
 * relationship traversal, field, direction, case sensitivity and null handling as plain, single-click labels.
 * Not bound to a single {@link de.a12.studio.models.documentmodel.Element}, so it follows the model-header
 * pattern used by e.g. {@link de.a12.studio.ui.editors.overviewmodel.OverviewSortingPanelController}. Clicking a
 * row (or its Edit button) opens {@link Dialogs#showSortForEdit}, the full sort entry editor - mirroring {@link
 * de.a12.studio.ui.editors.overviewmodel.OverviewColumnsPanelController}'s use of the same pattern for columns;
 * the Add button opens the same editor via {@link Dialogs#showSortForAdd}, only adding the new entry to {@code
 * content.sort} once it's confirmed.
 */
public class QuerySortingPanelController extends AbstractPropertyEditor {

  // Identifies a row-reorder drag; the dragboard content is the dragged row's current index into getSort().
  private static final DataFormat SORT_INDEX = new DataFormat("application/x-a12-query-sort-index");

  @FXML
  private HBox sortingHeaderRow;
  @FXML
  private VBox sortingRows;
  @FXML
  private Label sortingEmptyLabel;

  private ProjectItem projectItem;
  private QueryModel model;

  public void load(@NonNull ProjectItem projectItem, @NonNull QueryModel model) {
    this.projectItem = projectItem;
    this.model = model;
    rebuildSortingRows();
  }

  private QueryModelContent content() {
    return model.getContent();
  }

  private List<QuerySort> getSort() {
    return content().getSort();
  }

  @FXML
  private void onAddSort() {
    Dialogs.showSortForAdd(Studio.stage, projectItem).ifPresent(sort -> {
      getSort().add(sort);
      rebuildSortingRows();
      commitHeaderChange();
    });
  }

  private void rebuildSortingRows() {
    sortingRows.getChildren().clear();

    List<QuerySort> sort = getSort();
    boolean empty = sort.isEmpty();
    sortingHeaderRow.setVisible(!empty);
    sortingHeaderRow.setManaged(!empty);
    sortingEmptyLabel.setVisible(empty);
    sortingEmptyLabel.setManaged(empty);

    List<QueryTraversalOption> traversalOptions = QueryTraversalOption.options(projectItem);
    for (int index = 0; index < sort.size(); index++) {
      sortingRows.getChildren().add(createSortRow(sort.get(index), index, sort.size(), traversalOptions));
    }
  }

  private HBox createSortRow(@NonNull QuerySort sort, int index, int rowCount, @NonNull List<QueryTraversalOption> traversalOptions) {
    FontIcon dragHandle = RowFactory.createDragHandle();

    HBox traversalCell = createTraversalCell(sort, traversalOptions);
    Label fieldLabel = createRowLabel(sort.getSortBy().getField(), 200.0, sort);
    fieldLabel.getStyleClass().add("path-text");
    Label directionLabel = createRowLabel(directionDisplay(sort.getSortBy().getDirection()), 110.0, sort);
    Label ignoreCaseLabel = createRowLabel(Boolean.TRUE.equals(sort.getSortBy().getIgnoreCase()) ? "Yes" : "No", 90.0, sort);
    Label nullHandlingLabel = createRowLabel(nullHandlingDisplay(sort.getSortBy().getNullHandling()), 140.0, sort);

    HBox row = new HBox(10.0, dragHandle, traversalCell, fieldLabel, directionLabel, ignoreCaseLabel, nullHandlingLabel, createSortActionsBox(sort, index, rowCount));
    row.setAlignment(Pos.CENTER_LEFT);
    row.getStyleClass().add("module-row");
    RowFactory.setupRowDragAndDrop(row, dragHandle, SORT_INDEX, index, this::moveSort);
    return row;
  }

  private HBox createTraversalCell(QuerySort sort, List<QueryTraversalOption> traversalOptions) {
    QueryTraversalOption current = new QueryTraversalOption(sort.getRelationshipModel(), sort.getTargetRole());
    boolean unresolved = !traversalOptions.contains(current);

    Label label = createRowLabel(current.display(), 220.0, sort);
    if (unresolved) {
      label.getStyleClass().add("validation-error");
      label.setTooltip(WidgetFactory.createTooltip(StudioBundle.get("relationship_could_not_be_resolved")));
    }

    HBox cell = new HBox(label);
    HBox.setHgrow(cell, Priority.ALWAYS);
    return cell;
  }

  private Label createRowLabel(String text, double width, QuerySort sort) {
    Label label = new Label(text);
    label.setPrefWidth(width);
    label.setCursor(Cursor.HAND);
    label.setOnMouseClicked(event -> {
      if (event.getClickCount() == 1) {
        openEditDialog(sort);
      }
    });
    return label;
  }

  private void openEditDialog(QuerySort sort) {
    Dialogs.showSortForEdit(Studio.stage, projectItem, sort);
    rebuildSortingRows();
  }

  private HBox createSortActionsBox(@NonNull QuerySort sort, int index, int rowCount) {
    VBox moveButtonsBox = RowFactory.createMoveButtonsBox(index, rowCount, this::moveSortRow);

    Button editButton = RowFactory.createActionButton(Icons.PENCIL, "Edit", () -> openEditDialog(sort));

    Button deleteButton = RowFactory.createActionButton(Icons.TRASH, StudioBundle.get("delete"), () -> {
      Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, StudioBundle.get("delete_this_sorting_entry"), null, null, "Delete");
      if (result.isPresent() && result.get() == ButtonType.OK) {
        getSort().remove(sort);
        rebuildSortingRows();
        commitHeaderChange();
      }
    });

    HBox actionsBox = new HBox(4.0, moveButtonsBox, editButton, deleteButton);
    actionsBox.setAlignment(Pos.CENTER_LEFT);
    return actionsBox;
  }

  private void moveSort(int fromIndex, int insertBeforeIndex) {
    if (RowFactory.reorder(getSort(), fromIndex, insertBeforeIndex)) {
      rebuildSortingRows();
      commitHeaderChange();
    }
  }

  private void moveSortRow(int fromIndex, int toIndex) {
    Collections.swap(getSort(), fromIndex, toIndex);
    rebuildSortingRows();
    commitHeaderChange();
  }

  private static String directionDisplay(String direction) {
    return QuerySortBy.DIRECTION_DESC.equals(direction) ? StudioBundle.get("descending") : StudioBundle.get("ascending");
  }

  private static String nullHandlingDisplay(String nullHandling) {
    if (QuerySortBy.NULLS_FIRST.equals(nullHandling)) {
      return StudioBundle.get("nulls_first");
    }
    if (QuerySortBy.NULLS_LAST.equals(nullHandling)) {
      return StudioBundle.get("nulls_last");
    }
    return StudioBundle.get("null_handling_default");
  }
}
