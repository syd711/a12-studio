package de.a12.studio.ui.editors.combineddocumentmodel;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.ModelType;
import de.a12.studio.models.combineddocumentmodel.CombinationStep;
import de.a12.studio.models.combineddocumentmodel.CombinationStepType;
import de.a12.studio.models.combineddocumentmodel.CombinedDocumentModel;
import de.a12.studio.models.combineddocumentmodel.DocumentModelIdRef;
import de.a12.studio.models.combineddocumentmodel.SelectionModelIdRef;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.editors.AbstractPropertyEditor;
import de.a12.studio.ui.editors.combineddocumentmodel.dialogs.Dialogs;
import de.a12.studio.ui.editors.propertyeditors.RowFactory;
import de.a12.studio.ui.util.Icons;
import de.a12.studio.ui.util.ProjectDocumentModels;
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
import javafx.scene.layout.VBox;
import org.jspecify.annotations.NonNull;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Edits a {@link CombinedDocumentModel}'s {@code content.CombinationSteps}: one draggable, reorderable row per
 * {@link CombinationStep}, summarizing its Type and whichever of Model for Addition/Selection/Decoration
 * applies to that type. Not bound to a single {@link de.a12.studio.models.documentmodel.Element}, so it follows
 * the model-header pattern used by {@link de.a12.studio.ui.editors.mappingmodel.SourceModelsPanelController}.
 * Clicking a row opens {@link Dialogs#showCombinationStepForEdit} (Add uses
 * {@link Dialogs#showCombinationStepForAdd}) to edit its Type and referenced models.
 */
public class CombinationStepsPanelController extends AbstractPropertyEditor {

  // Matches ModelValidationError#elementId() as produced by the combination validators (see
  // de.a12.studio.modelsvalidation.validators.combination), one per content.CombinationSteps index.
  private static final String STEP_ELEMENT_ID_PREFIX = "content/combinationSteps/";

  // Identifies a row-reorder drag; the dragboard content is the dragged row's current index into getSteps().
  private static final DataFormat COMBINATION_STEP_INDEX = new DataFormat("application/x-a12-combination-step-index");

  @FXML
  private HBox combinationStepHeaders;

  @FXML
  private VBox combinationStepRows;

  @FXML
  private Label combinationStepsEmptyLabel;

  private CombinedDocumentModel model;

  // Invoked after every add/remove/reorder/edit that may have changed a step's referenced models, so the
  // owning CombinedDocumentModelEditorController can resync the header's model references and save. This panel
  // has no direct save of its own (unlike e.g. commitHeaderChange() elsewhere) because that resync must happen
  // first - see CombinedDocumentModelEditorController#onCombinationStepsChanged.
  private Runnable onChange = () -> {
  };

  public void setModel(@NonNull CombinedDocumentModel model) {
    this.model = model;
    rebuildRows();
    refreshValidation();
  }

  public void setOnChange(@NonNull Runnable onChange) {
    this.onChange = onChange;
  }

  /**
   * Re-checks this panel's structural validators (see {@link de.a12.studio.modelsvalidation.validators.combination})
   * and shows the first hit, if any, in this panel's own error container. Called after every mutation made
   * through this panel, and exposed so the owning editor can re-check after something changed elsewhere too
   * (e.g. a step's referenced model was renamed or deleted in another tab).
   */
  public void refreshValidation() {
    ProjectItem projectItem = Studio.getSelectedProjectItem();
    if (model == null || projectItem == null) {
      hideError();
      return;
    }
    List<ModelValidationError> stepErrors = Studio.getValidationService().validate(model).stream()
        .filter(error -> error.elementId() != null && error.elementId().startsWith(STEP_ELEMENT_ID_PREFIX))
        .toList();
    if (stepErrors.isEmpty()) {
      hideError();
    }
    else {
      ModelValidationError first = stepErrors.get(0);
      showError(first.severity(), stepMessage(first));
    }
  }

  private static String stepMessage(ModelValidationError error) {
    String indexPart = error.elementId().substring(STEP_ELEMENT_ID_PREFIX.length());
    try {
      int stepNumber = Integer.parseInt(indexPart) + 1;
      return "Step " + stepNumber + ": " + error.message();
    }
    catch (NumberFormatException e) {
      return error.message();
    }
  }

  @FXML
  private void onAdd() {
    Dialogs.showCombinationStepForAdd(Studio.stage, documentModelIds(), selectionModelIds()).ifPresent(step -> {
      getSteps().add(step);
      changed();
    });
  }

  private List<CombinationStep> getSteps() {
    return model.getContent().getCombinationSteps();
  }

  private static List<String> documentModelIds() {
    ProjectItem projectItem = Studio.getSelectedProjectItem();
    if (projectItem == null) {
      return List.of();
    }
    return ProjectDocumentModels.getOtherDocumentModels(projectItem).stream()
        .map(DocumentModel::getId)
        .sorted(Comparator.naturalOrder())
        .toList();
  }

  private static List<String> selectionModelIds() {
    ProjectItem projectItem = Studio.getSelectedProjectItem();
    if (projectItem == null) {
      return List.of();
    }
    return ProjectDocumentModels.getOtherModelsOfType(projectItem, ModelType.SELECTION).stream()
        .map(A12Model::getId)
        .sorted(Comparator.naturalOrder())
        .toList();
  }

  private void rebuildRows() {
    if (model == null) {
      return;
    }
    combinationStepRows.getChildren().clear();

    List<CombinationStep> steps = getSteps();
    boolean empty = steps.isEmpty();
    combinationStepHeaders.setVisible(!empty);
    combinationStepHeaders.setManaged(!empty);
    combinationStepsEmptyLabel.setVisible(empty);
    combinationStepsEmptyLabel.setManaged(empty);

    for (int index = 0; index < steps.size(); index++) {
      combinationStepRows.getChildren().add(createRow(steps.get(index), index, steps.size()));
    }
  }

  private HBox createRow(CombinationStep step, int index, int rowCount) {
    FontIcon dragHandle = RowFactory.createDragHandle();

    Label typeLabel = createRowLabel(displayName(step.getType()), "combinationStepType-" + index, 180.0, step);
    Label additiveLabel = createRowLabel(nullToDash(dmIdOf(step.getAdditiveModel())), "combinationStepAdditive-" + index, 180.0, step);
    Label selectionLabel = createRowLabel(nullToDash(smIdOf(step.getSelectionModel())), "combinationStepSelection-" + index, 180.0, step);
    Label decorationLabel = createRowLabel(nullToDash(dmIdOf(step.getDecorationModel())), "combinationStepDecoration-" + index, 180.0, step);

    HBox row = new HBox(10.0, dragHandle, typeLabel, additiveLabel, selectionLabel, decorationLabel, createActionsBox(step, index, rowCount));
    row.setAlignment(Pos.CENTER_LEFT);
    row.getStyleClass().add("module-row");
    RowFactory.setupRowDragAndDrop(row, dragHandle, COMBINATION_STEP_INDEX, index, this::moveCombinationStep);
    return row;
  }

  private static String dmIdOf(DocumentModelIdRef ref) {
    return ref != null ? ref.getDmId() : null;
  }

  private static String smIdOf(SelectionModelIdRef ref) {
    return ref != null ? ref.getSmId() : null;
  }

  private static String displayName(CombinationStepType type) {
    if (type == null) {
      return "";
    }
    return switch (type) {
      case ADDITION -> StudioBundle.get("combination_step_type.addition");
      case SELECTION -> StudioBundle.get("combination_step_type.selection");
      case DECORATION_FOR_FIELDS -> StudioBundle.get("combination_step_type.decoration_for_fields");
      case DECORATION_FOR_GROUPS -> StudioBundle.get("combination_step_type.decoration_for_groups");
    };
  }

  private static String nullToDash(String value) {
    return value != null ? value : "–";
  }

  private Label createRowLabel(String text, String id, double width, CombinationStep step) {
    Label label = new Label(text);
    label.setId(id);
    label.setPrefWidth(width);
    label.setCursor(Cursor.HAND);
    label.setOnMouseClicked(event -> {
      if (event.getClickCount() == 1) {
        openEditDialog(step);
      }
    });
    return label;
  }

  private void openEditDialog(CombinationStep step) {
    if (Dialogs.showCombinationStepForEdit(Studio.stage, step, documentModelIds(), selectionModelIds())) {
      changed();
    }
  }

  private void moveCombinationStep(int fromIndex, int insertBeforeIndex) {
    if (RowFactory.reorder(getSteps(), fromIndex, insertBeforeIndex)) {
      changed();
    }
  }

  private HBox createActionsBox(CombinationStep step, int index, int rowCount) {
    VBox moveButtonsBox = RowFactory.createMoveButtonsBox(index, rowCount, this::moveRow);

    Button editButton = RowFactory.createActionButton(Icons.PENCIL, "Edit", () -> openEditDialog(step));

    Button openModelButton = RowFactory.createActionButton(Icons.OPEN_IN_NEW, "Open Model", () -> openModel(step));
    openModelButton.setDisable(referencedModelId(step) == null);

    Button deleteButton = RowFactory.createActionButton(Icons.TRASH, "Delete", () -> {
      Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, StudioBundle.get("delete_this_combination_step"), null, null, "Delete");
      if (result.isPresent() && result.get() == ButtonType.OK) {
        getSteps().remove(step);
        changed();
      }
    });

    HBox actionsBox = new HBox(4.0, moveButtonsBox, editButton, openModelButton, deleteButton);
    actionsBox.setAlignment(Pos.CENTER_LEFT);
    return actionsBox;
  }

  /**
   * Opens whichever of a step's three possible references is actually set, in an editor tab - selecting its
   * tab instead if it's already open, or showing "not supported yet" for a Selection Model reference (no
   * Selection Model editor exists yet, see {@code ModelType#SELECTION}), same as {@link
   * de.a12.studio.ui.editors.mappingmodel.SourceModelsPanelController#openModel}.
   */
  private static String referencedModelId(CombinationStep step) {
    String additiveId = dmIdOf(step.getAdditiveModel());
    if (additiveId != null) {
      return additiveId;
    }
    String decorationId = dmIdOf(step.getDecorationModel());
    if (decorationId != null) {
      return decorationId;
    }
    return smIdOf(step.getSelectionModel());
  }

  private void openModel(CombinationStep step) {
    String modelId = referencedModelId(step);
    if (modelId != null) {
      ProjectDocumentModels.openModelInEditor(modelId);
    }
  }

  private void moveRow(int fromIndex, int toIndex) {
    Collections.swap(getSteps(), fromIndex, toIndex);
    changed();
  }

  private void changed() {
    rebuildRows();
    refreshValidation();
    onChange.run();
  }
}
