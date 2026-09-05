package de.a12.studio.ui.editors.documentmodel;

import de.a12.studio.models.documentmodel.ComputationAlternative;
import de.a12.studio.models.documentmodel.ComputationElement;
import de.a12.studio.models.documentmodel.Element;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.editors.AbstractPropertyEditor;
import de.a12.studio.ui.editors.propertyeditors.RowFactory;
import de.a12.studio.ui.util.Icons;
import de.a12.studio.ui.util.StudioBundle;
import de.a12.studio.ui.util.WidgetFactory;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.jspecify.annotations.NonNull;

import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Edits a {@link ComputationElement}'s {@code Computation.computationAlternatives} - each row a {@link
 * ComputationAlternative} (precondition/operation pair), evaluated in order. {@code precondition}/{@code
 * operation} are plain multi-line text (no semantic condition-language validation - see the "Backend / kernel
 * capability map" correction in {@code docs/sme-reference-comparison.md}: a12-studio has no kernel dependency to
 * validate this expression text against), matching how {@link
 * de.a12.studio.ui.editors.propertyeditors.RichtextEditorController}-based expression fields elsewhere in this
 * codebase (e.g. {@code overviewmodel.Column.expression}) are edited today. Follows the same dynamic-row pattern
 * as {@link de.a12.studio.ui.editors.propertyeditors.AnnotationsPanelController} (plain controls built in Java,
 * not FXML-loaded per row).
 */
public class ComputationAlternativesPanelController extends AbstractPropertyEditor implements Initializable {

  @FXML
  private GridPane alternativesGrid;

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
    getAlternatives().add(new ComputationAlternative());
    rebuildRows();
    commitChange();
  }

  private List<ComputationAlternative> getAlternatives() {
    return ((ComputationElement) element).getComputation().getComputationAlternatives();
  }

  private void rebuildRows() {
    alternativesGrid.getChildren().removeIf(node -> {
      Integer rowIndex = GridPane.getRowIndex(node);
      return rowIndex != null && rowIndex > 0;
    });

    List<ComputationAlternative> alternatives = getAlternatives();
    boolean empty = alternatives.isEmpty();
    emptyLabel.setVisible(empty);
    emptyLabel.setManaged(empty);
    alternativesGrid.setVisible(!empty);
    alternativesGrid.setManaged(!empty);
    for (int index = 0; index < alternatives.size(); index++) {
      addRow(alternatives.get(index), index, alternatives.size());
    }
  }

  private void addRow(ComputationAlternative alternative, int index, int rowCount) {
    TextArea preconditionField = new TextArea();
    preconditionField.setId("alternativePrecondition-" + index);
    preconditionField.setWrapText(true);
    preconditionField.setPrefRowCount(3);
    preconditionField.setMaxWidth(Double.MAX_VALUE);
    setFieldValue(preconditionField, alternative.getPrecondition());
    bindTextArea(preconditionField, (el, value) -> alternative.setPrecondition(value.isBlank() ? null : value));

    TextArea operationField = new TextArea();
    operationField.setId("alternativeOperation-" + index);
    operationField.setWrapText(true);
    operationField.setPrefRowCount(3);
    operationField.setMaxWidth(Double.MAX_VALUE);
    setFieldValue(operationField, alternative.getOperation());
    bindTextArea(operationField, (el, value) -> alternative.setOperation(value.isBlank() ? null : value));

    alternativesGrid.addRow(index + 1, preconditionField, operationField, createActionsBox(alternative, index, rowCount));
  }

  private HBox createActionsBox(ComputationAlternative alternative, int index, int rowCount) {
    VBox moveButtonsBox = RowFactory.createMoveButtonsBox(index, rowCount, this::moveRow);

    Button deleteButton = RowFactory.createActionButton(Icons.TRASH, "Delete", () -> {
      Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, StudioBundle.get("delete_this_alternative"), null, null, "Delete");
      if (result.isPresent() && result.get() == ButtonType.OK) {
        getAlternatives().remove(alternative);
        rebuildRows();
        commitChange();
      }
    });

    HBox actionsBox = new HBox(4.0, moveButtonsBox, deleteButton);
    actionsBox.setAlignment(Pos.CENTER_LEFT);
    return actionsBox;
  }

  private void moveRow(int fromIndex, int toIndex) {
    Collections.swap(getAlternatives(), fromIndex, toIndex);
    rebuildRows();
    commitChange();
  }
}
