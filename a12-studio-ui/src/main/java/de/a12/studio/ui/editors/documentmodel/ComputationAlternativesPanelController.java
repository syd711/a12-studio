package de.a12.studio.ui.editors.documentmodel;

import de.a12.studio.models.documentmodel.ComputationAlternative;
import de.a12.studio.models.documentmodel.ComputationElement;
import de.a12.studio.models.documentmodel.Element;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.editors.AbstractPropertyEditor;
import de.a12.studio.ui.editors.documentmodel.dialogs.Dialogs;
import de.a12.studio.ui.editors.propertyeditors.RowFactory;
import de.a12.studio.ui.util.Icons;
import de.a12.studio.ui.util.StudioBundle;
import de.a12.studio.ui.util.WidgetFactory;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.input.DataFormat;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.jspecify.annotations.NonNull;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Edits a {@link ComputationElement}'s {@code Computation.computationAlternatives} - each row a {@link
 * ComputationAlternative} (precondition/operation pair), evaluated in order. {@code precondition}/{@code
 * operation} are plain multi-line text (no semantic condition-language validation - see the "Backend / kernel
 * capability map" correction in {@code docs/sme-reference-comparison.md}: a12-studio has no kernel dependency to
 * validate this expression text against), matching how {@link
 * de.a12.studio.ui.editors.propertyeditors.RichtextEditorController}-based expression fields elsewhere in this
 * codebase (e.g. {@code overviewmodel.Column.expression}) are edited today. Each row is a read-only,
 * draggable/reorderable summary (matching {@link
 * de.a12.studio.ui.editors.combineddocumentmodel.CombinationStepsPanelController}'s module-row pattern); the
 * actual fields are edited in {@link Dialogs#showComputationAlternativeForAdd}/{@link
 * Dialogs#showComputationAlternativeForEdit}, opened via Add/Edit or a single click on a row.
 */
public class ComputationAlternativesPanelController extends AbstractPropertyEditor {

  // Identifies a row-reorder drag; the dragboard content is the dragged row's current index into getAlternatives().
  private static final DataFormat ALTERNATIVE_INDEX = new DataFormat("application/x-a12-computation-alternative-index");

  @FXML
  private HBox alternativesHeader;

  @FXML
  private VBox alternativesList;

  @FXML
  private Label emptyLabel;

  @Override
  public void setElement(@NonNull Element element) {
    super.setElement(element);
    rebuildRows();
  }

  // No validationProperty(): ComputationConfig's checks (missing computedFieldRelPath, empty operation) are
  // both tagged ElementProperty.COMPUTATION_PROPERTIES, owned by the sibling TargetFieldPanelController
  // (configureComputedField()) - claiming the same tag here would just duplicate whichever message
  // ownError() finds first.

  @FXML
  private void onAdd() {
    ComputationAlternative alternative = new ComputationAlternative();
    if (Dialogs.showComputationAlternativeForAdd(Studio.stage, alternative)) {
      getAlternatives().add(alternative);
      rebuildRows();
      commitChange();
    }
  }

  private List<ComputationAlternative> getAlternatives() {
    return ((ComputationElement) element).getComputation().getComputationAlternatives();
  }

  private void rebuildRows() {
    alternativesList.getChildren().clear();

    List<ComputationAlternative> alternatives = getAlternatives();
    boolean empty = alternatives.isEmpty();
    emptyLabel.setVisible(empty);
    emptyLabel.setManaged(empty);
    alternativesHeader.setVisible(!empty);
    alternativesHeader.setManaged(!empty);

    for (int index = 0; index < alternatives.size(); index++) {
      alternativesList.getChildren().add(createRow(alternatives.get(index), index, alternatives.size()));
    }
  }

  private HBox createRow(ComputationAlternative alternative, int index, int rowCount) {
    Node dragHandle = RowFactory.createDragHandle();

    Label preconditionLabel = createRowLabel(summarize(alternative.getPrecondition()), "alternativePrecondition-" + index, alternative);
    Label operationLabel = createRowLabel(summarize(alternative.getOperation()), "alternativeOperation-" + index, alternative);

    HBox row = new HBox(10.0, dragHandle, preconditionLabel, operationLabel, createActionsBox(alternative, index, rowCount));
    row.setAlignment(Pos.CENTER_LEFT);
    row.getStyleClass().add("module-row");
    RowFactory.setupRowDragAndDrop(row, dragHandle, ALTERNATIVE_INDEX, index, this::moveAlternativeViaDrag);
    return row;
  }

  /** Collapses multi-line text to its first line, marking with an ellipsis whenever more content follows. */
  private static String summarize(String text) {
    if (text == null || text.isBlank()) {
      return "–";
    }
    String trimmed = text.strip();
    int newlineIndex = trimmed.indexOf('\n');
    return newlineIndex < 0 ? trimmed : trimmed.substring(0, newlineIndex).stripTrailing() + "…";
  }

  private Label createRowLabel(String text, String id, ComputationAlternative alternative) {
    Label label = new Label(text);
    label.setId(id);
    label.setMaxWidth(Double.MAX_VALUE);
    HBox.setHgrow(label, Priority.ALWAYS);
    label.setCursor(Cursor.HAND);
    label.setOnMouseClicked(event -> {
      if (event.getClickCount() == 1) {
        openEditDialog(alternative);
      }
    });
    return label;
  }

  private void openEditDialog(ComputationAlternative alternative) {
    if (Dialogs.showComputationAlternativeForEdit(Studio.stage, alternative)) {
      rebuildRows();
      commitChange();
    }
  }

  private void moveAlternativeViaDrag(int fromIndex, int insertBeforeIndex) {
    if (RowFactory.reorder(getAlternatives(), fromIndex, insertBeforeIndex)) {
      rebuildRows();
      commitChange();
    }
  }

  private HBox createActionsBox(ComputationAlternative alternative, int index, int rowCount) {
    VBox moveButtonsBox = RowFactory.createMoveButtonsBox(index, rowCount, this::moveRow);

    Button editButton = RowFactory.createActionButton(Icons.PENCIL, "Edit", () -> openEditDialog(alternative));

    Button deleteButton = RowFactory.createActionButton(Icons.TRASH, "Delete", () -> {
      Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, StudioBundle.get("delete_this_alternative"), null, null, "Delete");
      if (result.isPresent() && result.get() == ButtonType.OK) {
        getAlternatives().remove(alternative);
        rebuildRows();
        commitChange();
      }
    });

    HBox actionsBox = new HBox(4.0, moveButtonsBox, editButton, deleteButton);
    actionsBox.setAlignment(Pos.CENTER_LEFT);
    return actionsBox;
  }

  private void moveRow(int fromIndex, int toIndex) {
    Collections.swap(getAlternatives(), fromIndex, toIndex);
    rebuildRows();
    commitChange();
  }
}
