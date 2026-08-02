package de.a12.studio.ui.editors.propertyeditors;

import de.a12.studio.models.mappingmodel.MappingModel;
import de.a12.studio.models.mappingmodel.MappingSource;
import de.a12.studio.models.projects.Project;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.editors.AbstractPropertyEditor;
import de.a12.studio.ui.editors.mappingmodel.dialogs.Dialogs;
import de.a12.studio.ui.events.StudioEventManager;
import de.a12.studio.ui.util.Icons;
import de.a12.studio.ui.util.ProjectDocumentModels;
import de.a12.studio.ui.util.WidgetFactory;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.input.DataFormat;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.jspecify.annotations.NonNull;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Edits a {@link MappingModel}'s {@code content.Source}: one draggable, reorderable row per {@link
 * MappingSource}, summarizing its Name, Model, Repetitions and Skip Document Validation. Not bound to a
 * single {@link de.a12.studio.models.documentmodel.Element}, so it follows the model-header pattern used by
 * e.g. {@link OverviewColumnsPanelController}. Clicking a row opens {@link Dialogs#showSourceModelForEdit}
 * (Add uses {@link Dialogs#showSourceModelForAdd}) to edit its Name/Model/Repetitions/Skip Document Validation.
 */
public class SourceModelsPanelController extends AbstractPropertyEditor {

  // Identifies a row-reorder drag; the dragboard content is the dragged row's current index into getSource().
  private static final DataFormat SOURCE_MODEL_INDEX = new DataFormat("application/x-a12-source-model-index");

  @FXML
  private HBox sourceModelHeaders;

  @FXML
  private VBox sourceModelRows;

  @FXML
  private Label sourceModelsEmptyLabel;

  private MappingModel model;

  // Invoked after every add/remove/reorder/edit that may have changed a Source's dmId, so the owning
  // MappingModelEditorController can resync the header's document-model references and save. This panel has
  // no direct save of its own (unlike e.g. commitHeaderChange() elsewhere) because that resync must happen
  // first - see MappingModelEditorController#onSourceModelsChanged.
  private Runnable onChange = () -> {
  };

  public void setModel(@NonNull MappingModel model) {
    this.model = model;
    rebuildRows();
  }

  public void setOnChange(@NonNull Runnable onChange) {
    this.onChange = onChange;
  }

  @FXML
  private void onAdd() {
    Dialogs.showSourceModelForAdd(Studio.stage).ifPresent(sourceModel -> {
      getSource().add(sourceModel);
      rebuildRows();
      onChange.run();
    });
  }

  private List<MappingSource> getSource() {
    return model.getContent().getSource();
  }

  private void rebuildRows() {
    if (model == null) {
      return;
    }
    sourceModelRows.getChildren().clear();

    List<MappingSource> source = getSource();
    boolean empty = source.isEmpty();
    sourceModelHeaders.setVisible(!empty);
    sourceModelHeaders.setManaged(!empty);
    sourceModelsEmptyLabel.setVisible(empty);
    sourceModelsEmptyLabel.setManaged(empty);

    for (int index = 0; index < source.size(); index++) {
      sourceModelRows.getChildren().add(createRow(source.get(index), index, source.size()));
    }
  }

  private HBox createRow(MappingSource sourceModel, int index, int rowCount) {
    FontIcon dragHandle = RowFactory.createDragHandle();

    Label nameLabel = createRowLabel(nullToEmpty(sourceModel.getName()), "sourceModelName-" + index, 150.0, sourceModel);
    Label modelLabel = createRowLabel(nullToEmpty(sourceModel.getDmId()), "sourceModelModel-" + index, 200.0, sourceModel);
    Label repetitionsLabel = createRowLabel(sourceModel.getMaxRepeat() != null ? String.valueOf(sourceModel.getMaxRepeat()) : "", "sourceModelRepetitions-" + index, 100.0, sourceModel);
    Label skipValidationLabel = createRowLabel(Boolean.TRUE.equals(sourceModel.getNoSourceValidation()) ? "Yes" : "No", "sourceModelSkipValidation-" + index, 150.0, sourceModel);

    HBox row = new HBox(10.0, dragHandle, nameLabel, modelLabel, repetitionsLabel, skipValidationLabel, createActionsBox(sourceModel, index, rowCount));
    row.setAlignment(Pos.CENTER_LEFT);
    row.getStyleClass().add("module-row");
    RowFactory.setupRowDragAndDrop(row, dragHandle, SOURCE_MODEL_INDEX, index, this::moveSourceModel);
    return row;
  }

  private static String nullToEmpty(String value) {
    return value != null ? value : "";
  }

  private Label createRowLabel(String text, String id, double width, MappingSource sourceModel) {
    Label label = new Label(text);
    label.setId(id);
    label.setPrefWidth(width);
    label.setCursor(Cursor.HAND);
    label.setOnMouseClicked(event -> {
      if (event.getClickCount() == 1) {
        openEditDialog(sourceModel);
      }
    });
    return label;
  }

  private void openEditDialog(MappingSource sourceModel) {
    if (Dialogs.showSourceModelForEdit(Studio.stage, sourceModel)) {
      rebuildRows();
      onChange.run();
    }
  }

  private void moveSourceModel(int fromIndex, int insertBeforeIndex) {
    if (RowFactory.reorder(getSource(), fromIndex, insertBeforeIndex)) {
      rebuildRows();
      onChange.run();
    }
  }

  private HBox createActionsBox(MappingSource sourceModel, int index, int rowCount) {
    VBox moveButtonsBox = RowFactory.createMoveButtonsBox(index, rowCount, this::moveRow);

    Button editButton = RowFactory.createActionButton(Icons.PENCIL, "Edit", () -> openEditDialog(sourceModel));

    Button openModelButton = RowFactory.createActionButton(Icons.OPEN_IN_NEW, "Open Model", () -> openModel(sourceModel));
    openModelButton.setDisable(sourceModel.getDmId() == null || sourceModel.getDmId().isBlank());

    Button deleteButton = RowFactory.createActionButton(Icons.TRASH, "Delete", () -> {
      Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, "Delete this source model?", null, null, "Delete");
      if (result.isPresent() && result.get() == ButtonType.OK) {
        getSource().remove(sourceModel);
        rebuildRows();
        onChange.run();
      }
    });

    HBox actionsBox = new HBox(4.0, moveButtonsBox, editButton, openModelButton, deleteButton);
    actionsBox.setAlignment(Pos.CENTER_LEFT);
    return actionsBox;
  }

  /**
   * Opens the Document Model referenced by {@code sourceModel}'s {@code dmId} in an editor tab, selecting its
   * tab instead if it's already open (see {@code TabPaneController#modelOpened}), mirroring {@link
   * TargetModelPanelController#onEditReference}.
   */
  private void openModel(MappingSource sourceModel) {
    ProjectDocumentModels.findProjectItemByModelId(sourceModel.getDmId()).ifPresent(item -> {
      Project project = Studio.getCurrentProject();
      if (project != null) {
        project.getSettings().getUISettings().addOpenedFile(item.getPath());
        project.getSettings().getUISettings().save();
      }
      StudioEventManager.getInstance().fireModelOpenEvent(item);
    });
  }

  private void moveRow(int fromIndex, int toIndex) {
    Collections.swap(getSource(), fromIndex, toIndex);
    rebuildRows();
    onChange.run();
  }
}
