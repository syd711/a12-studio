package de.a12.studio.ui.editors.applicationmodel;

import de.a12.studio.models.applicationmodel.ApplicationModel;
import de.a12.studio.models.applicationmodel.ApplicationModelContent;
import de.a12.studio.models.applicationmodel.Layout;
import de.a12.studio.models.applicationmodel.Region;
import de.a12.studio.modelsvalidation.refactoring.ApplicationModelStructuralRefactoring;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.editors.AbstractPropertyEditor;
import de.a12.studio.ui.editors.applicationmodel.dialogs.Dialogs;
import de.a12.studio.ui.editors.applicationmodel.dialogs.SubregionDialogController;
import de.a12.studio.ui.editors.propertyeditors.RowFactory;
import de.a12.studio.ui.util.Icons;
import de.a12.studio.ui.util.WidgetFactory;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.input.DataFormat;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.jspecify.annotations.NonNull;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import de.a12.studio.ui.util.StudioBundle;

/**
 * Edits {@link ApplicationModelContent#getRegion()}'s {@link Region#getSubRegions()} tree: every subregion at any
 * depth is a row, indented under its parent, reorderable within its own siblings (drag or move up/down), editable
 * and copyable (deep copy) via {@link SubregionDialogController}, deletable, and can get nested subregions of its
 * own. Same row-based layout as {@link ModulesPanelController}, with an additional read-only "Layout" column showing
 * each subregion's {@link Layout#getName()}. Not bound to a single {@link de.a12.studio.models.documentmodel.Element}
 * (the region lives on the model's content), so it follows the model-header pattern used by e.g. {@link
 * RegionPanelController}.
 */
public class SubregionsPanelController extends AbstractPropertyEditor {

  // Identifies a row-reorder drag; the dragboard content is the dragged row's current index into getSubRegions().
  private static final DataFormat SUBREGION_INDEX = new DataFormat("application/x-a12-subregion-index");
  private static final double INDENT_PER_LEVEL = 24.0;

  @FXML
  private HBox columnHeaders;

  @FXML
  private VBox subregionsList;

  private ApplicationModel model;

  // The sibling list a row drag started in: the dragboard only carries an index, so drops into another level of
  // the tree are refused rather than interpreted against the wrong list.
  private List<Region> draggedSiblings;

  public void setModel(@NonNull ApplicationModel model) {
    this.model = model;
    rebuildRows();
  }

  @FXML
  private void onAdd() {
    Dialogs.showSubregionForAdd(Studio.stage).ifPresent(subregion -> {
      getOrCreateSubRegions().add(subregion);
      rebuildRows();
      commitChange();
    });
  }

  private Region getRegion() {
    return model == null || model.getContent() == null ? null : model.getContent().getRegion();
  }

  private List<Region> getSubRegions() {
    Region region = getRegion();
    return region != null ? region.getSubRegions() : List.of();
  }

  private List<Region> getOrCreateSubRegions() {
    ApplicationModelContent content = model.getContent();
    if (content == null) {
      content = new ApplicationModelContent();
      model.setContent(content);
    }
    Region region = content.getRegion();
    if (region == null) {
      region = new Region();
      content.setRegion(region);
    }
    return region.getSubRegions();
  }

  private void rebuildRows() {
    subregionsList.getChildren().clear();

    List<Region> subregions = getSubRegions();
    columnHeaders.setVisible(!subregions.isEmpty());
    columnHeaders.setManaged(!subregions.isEmpty());
    if (subregions.isEmpty()) {
      Label emptyLabel = new Label("No subregions configured.");
      emptyLabel.getStyleClass().add("placeholder-label");
      subregionsList.getChildren().add(emptyLabel);
      return;
    }

    addRows(subregions, 0, "");
  }

  private void addRows(List<Region> siblings, int depth, String idPrefix) {
    for (int index = 0; index < siblings.size(); index++) {
      Region subregion = siblings.get(index);
      String rowId = idPrefix + index;
      subregionsList.getChildren().add(createRow(siblings, subregion, index, depth, rowId));
      addRows(subregion.getSubRegions(), depth + 1, rowId + "-");
    }
  }

  private HBox createRow(List<Region> siblings, Region subregion, int index, int depth, String rowId) {
    FontIcon dragHandle = RowFactory.createDragHandle();
    dragHandle.addEventFilter(MouseEvent.DRAG_DETECTED, event -> draggedSiblings = siblings);

    Pane indent = new Pane();
    indent.setMinWidth(depth * INDENT_PER_LEVEL);
    indent.setPrefWidth(depth * INDENT_PER_LEVEL);
    indent.setMaxWidth(depth * INDENT_PER_LEVEL);

    Label nameLabel = new Label(subregion.getName());
    nameLabel.setId("subregion-" + rowId);
    nameLabel.setMaxWidth(Double.MAX_VALUE);
    nameLabel.setCursor(Cursor.HAND);
    HBox.setHgrow(nameLabel, Priority.ALWAYS);
    nameLabel.setOnMouseClicked(event -> {
      if (event.getClickCount() == 1) {
        editSubregion(subregion);
      }
    });

    Label layoutLabel = new Label(subregion.getLayout() != null ? subregion.getLayout().getName() : "");
    layoutLabel.setId("subregion-layout-" + rowId);
    layoutLabel.setPrefWidth(140.0);

    HBox row = new HBox(10.0, indent, dragHandle, nameLabel, layoutLabel, createActionsBox(siblings, subregion, index));
    row.setAlignment(Pos.CENTER_LEFT);
    row.getStyleClass().add("module-row");
    RowFactory.setupRowDragAndDrop(row, dragHandle, SUBREGION_INDEX, index,
        (fromIndex, insertBeforeIndex) -> draggedSiblings == siblings,
        (fromIndex, insertBeforeIndex) -> moveSubregion(siblings, fromIndex, insertBeforeIndex));
    return row;
  }

  private void moveSubregion(List<Region> siblings, int fromIndex, int insertBeforeIndex) {
    if (draggedSiblings != siblings) {
      return;
    }
    draggedSiblings = null;
    if (RowFactory.reorder(siblings, fromIndex, insertBeforeIndex)) {
      rebuildRows();
      commitChange();
    }
  }

  private void editSubregion(Region subregion) {
    String oldName = subregion.getName();
    Dialogs.showSubregionForEdit(Studio.stage, subregion).ifPresent(edited -> {
      subregion.setName(edited.getName());
      subregion.setLayout(edited.getLayout());
      ApplicationModelStructuralRefactoring.renameRegion(model, oldName, edited.getName());
      rebuildRows();
      commitChange();
    });
  }

  private HBox createActionsBox(List<Region> siblings, Region subregion, int index) {
    VBox moveButtonsBox = RowFactory.createMoveButtonsBox(index, siblings.size(),
        (fromIndex, toIndex) -> moveRow(siblings, fromIndex, toIndex));

    Button editButton = RowFactory.createActionButton(Icons.PENCIL, StudioBundle.get("row_action.edit"), () -> editSubregion(subregion));

    Button copyButton = RowFactory.createActionButton(Icons.COPY, StudioBundle.get("copy"), () -> {
      siblings.add(siblings.indexOf(subregion) + 1, deepCopy(subregion));
      rebuildRows();
      commitChange();
    });

    Button addNestedButton = RowFactory.createActionButton(Icons.PLUS, StudioBundle.get("add_nested_subregion"),
        () -> Dialogs.showSubregionForAdd(Studio.stage).ifPresent(nested -> {
          subregion.getSubRegions().add(nested);
          rebuildRows();
          commitChange();
        }));

    Button deleteButton = RowFactory.createActionButton(Icons.TRASH, StudioBundle.get("row_action.delete"), () -> {
      Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, StudioBundle.get("delete_this_subregion"), null, null, "Delete");
      if (result.isPresent() && result.get() == ButtonType.OK) {
        siblings.remove(subregion);
        deleteRegionReferences(subregion);
        rebuildRows();
        commitChange();
      }
    });

    HBox actionsBox = new HBox(4.0, moveButtonsBox, editButton, addNestedButton, copyButton, deleteButton);
    actionsBox.setAlignment(Pos.CENTER_LEFT);
    return actionsBox;
  }

  private void moveRow(List<Region> siblings, int fromIndex, int toIndex) {
    Collections.swap(siblings, fromIndex, toIndex);
    rebuildRows();
    commitChange();
  }

  // Deleting a subregion removes its whole subtree, so references to the nested regions are cleared too.
  private void deleteRegionReferences(Region region) {
    ApplicationModelStructuralRefactoring.deleteRegion(model, region.getName());
    for (Region nested : region.getSubRegions()) {
      deleteRegionReferences(nested);
    }
  }

  // The copy must not share nested Region instances with the original, now that nested rows are editable.
  private static Region deepCopy(Region region) {
    Region copy = new Region();
    copy.setName(region.getName());
    copy.setLayout(region.getLayout());
    List<Region> nestedCopies = new ArrayList<>();
    for (Region nested : region.getSubRegions()) {
      nestedCopies.add(deepCopy(nested));
    }
    copy.setSubRegions(nestedCopies);
    return copy;
  }
}
