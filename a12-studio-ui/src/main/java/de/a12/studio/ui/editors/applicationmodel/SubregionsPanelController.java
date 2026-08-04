package de.a12.studio.ui.editors.applicationmodel;

import de.a12.studio.models.applicationmodel.ApplicationModel;
import de.a12.studio.models.applicationmodel.ApplicationModelContent;
import de.a12.studio.models.applicationmodel.Layout;
import de.a12.studio.models.applicationmodel.Region;
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
import javafx.scene.layout.HBox;
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
 * Edits {@link ApplicationModelContent#getRegion()}'s {@link Region#getSubRegions()}: a list of subregions, each
 * reorderable (move up/down), editable and copyable via {@link SubregionDialogController}, and deletable. Same
 * row-based layout as {@link ModulesPanelController}, with an additional read-only "Layout" column showing each
 * subregion's {@link Layout#getName()}. Not bound to a single {@link de.a12.studio.models.documentmodel.Element}
 * (the region lives on the model's content), so it follows the model-header pattern used by e.g. {@link
 * RegionPanelController}.
 */
public class SubregionsPanelController extends AbstractPropertyEditor {

  // Identifies a row-reorder drag; the dragboard content is the dragged row's current index into getSubRegions().
  private static final DataFormat SUBREGION_INDEX = new DataFormat("application/x-a12-subregion-index");

  @FXML
  private HBox columnHeaders;

  @FXML
  private VBox subregionsList;

  private ApplicationModel model;

  public void setModel(@NonNull ApplicationModel model) {
    this.model = model;
    rebuildRows();
  }

  @FXML
  private void onAdd() {
    Dialogs.showSubregionForAdd(Studio.stage).ifPresent(name -> {
      Region subregion = new Region();
      subregion.setName(name);
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

    for (int index = 0; index < subregions.size(); index++) {
      subregionsList.getChildren().add(createRow(subregions.get(index), index, subregions.size()));
    }
  }

  private HBox createRow(Region subregion, int index, int rowCount) {
    FontIcon dragHandle = RowFactory.createDragHandle();

    Label nameLabel = new Label(subregion.getName());
    nameLabel.setId("subregion-" + index);
    nameLabel.setMaxWidth(Double.MAX_VALUE);
    nameLabel.setCursor(Cursor.HAND);
    HBox.setHgrow(nameLabel, Priority.ALWAYS);
    nameLabel.setOnMouseClicked(event -> {
      if (event.getClickCount() == 1) {
        editSubregion(subregion);
      }
    });

    Label layoutLabel = new Label(subregion.getLayout() != null ? subregion.getLayout().getName() : "");
    layoutLabel.setId("subregion-layout-" + index);
    layoutLabel.setPrefWidth(140.0);

    HBox row = new HBox(10.0, dragHandle, nameLabel, layoutLabel, createActionsBox(subregion, index, rowCount));
    row.setAlignment(Pos.CENTER_LEFT);
    row.getStyleClass().add("module-row");
    RowFactory.setupRowDragAndDrop(row, dragHandle, SUBREGION_INDEX, index, this::moveSubregion);
    return row;
  }

  private void moveSubregion(int fromIndex, int insertBeforeIndex) {
    if (RowFactory.reorder(getOrCreateSubRegions(), fromIndex, insertBeforeIndex)) {
      rebuildRows();
      commitChange();
    }
  }

  private void editSubregion(Region subregion) {
    Dialogs.showSubregionForEdit(Studio.stage, subregion.getName()).ifPresent(name -> {
      subregion.setName(name);
      rebuildRows();
      commitChange();
    });
  }

  private HBox createActionsBox(Region subregion, int index, int rowCount) {
    VBox moveButtonsBox = RowFactory.createMoveButtonsBox(index, rowCount, this::moveRow);

    Button editButton = RowFactory.createActionButton(Icons.PENCIL, "Edit", () -> editSubregion(subregion));

    Button copyButton = RowFactory.createActionButton(Icons.COPY, "Copy", () -> {
      Region copy = new Region();
      copy.setName(subregion.getName());
      copy.setLayout(subregion.getLayout());
      copy.setSubRegions(new ArrayList<>(subregion.getSubRegions()));
      List<Region> subregions = getOrCreateSubRegions();
      subregions.add(subregions.indexOf(subregion) + 1, copy);
      rebuildRows();
      commitChange();
    });

    Button deleteButton = RowFactory.createActionButton(Icons.TRASH, "Delete", () -> {
      Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, StudioBundle.get("delete_this_subregion"), null, null, "Delete");
      if (result.isPresent() && result.get() == ButtonType.OK) {
        getOrCreateSubRegions().remove(subregion);
        rebuildRows();
        commitChange();
      }
    });

    HBox actionsBox = new HBox(4.0, moveButtonsBox, editButton, copyButton, deleteButton);
    actionsBox.setAlignment(Pos.CENTER_LEFT);
    return actionsBox;
  }

  private void moveRow(int fromIndex, int toIndex) {
    Collections.swap(getOrCreateSubRegions(), fromIndex, toIndex);
    rebuildRows();
    commitChange();
  }
}
