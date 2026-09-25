package de.a12.studio.ui.editors.treemodel.dialogs;

import de.a12.studio.models.treemodel.ExpansionDepth;
import de.a12.studio.ui.components.DialogController;
import de.a12.studio.ui.util.WidgetFactory;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.stage.Stage;

import java.util.List;
import java.util.Optional;

/**
 * Add/edit dialog for a single {@link ExpansionDepth} of the Tree expansion strategy, opened from {@link
 * de.a12.studio.ui.editors.treemodel.TreeExpansionDepthsPanelController} by clicking a row or its Add button.
 * Like {@link TreeColumnDialogController} it always builds a brand new {@link ExpansionDepth} on OK (see
 * {@link #getResult()}); the caller copies the values onto the existing entry or appends the new one.
 */
public class TreeExpansionDepthDialogController implements DialogController {

  private static final int DEFAULT_MAX_DEPTH = 1;

  @FXML
  private ComboBox<String> relationshipField;

  @FXML
  private Spinner<Integer> maxDepthField;

  @FXML
  private Button okButton;

  @FXML
  private Button cancelButton;

  private Stage stage;

  private ExpansionDepth built;

  private Optional<ButtonType> result = Optional.of(ButtonType.CANCEL);

  @FXML
  private void initialize() {
    maxDepthField.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, Integer.MAX_VALUE, DEFAULT_MAX_DEPTH));
    WidgetFactory.restrictToNumericInput(maxDepthField.getEditor());

    okButton.disableProperty().bind(relationshipField.valueProperty().isNull());
  }

  @Override
  public void onDialogCancel() {
    stage.close();
  }

  @FXML
  private void onDialogSubmit() {
    // An edited-but-not-yet-committed spinner text must be applied before reading the value.
    maxDepthField.increment(0);

    ExpansionDepth depth = new ExpansionDepth();
    depth.setRelationshipModel(relationshipField.getValue());
    depth.setMaxDepth(maxDepthField.getValue());
    built = depth;
    result = Optional.of(ButtonType.OK);
    stage.close();
  }

  /**
   * @param relationships the relationship models to choose from; a current value that isn't among them is
   *                      still offered, so editing an entry with a dangling reference doesn't drop it
   */
  void init(Stage stage, List<String> relationships, ExpansionDepth existing) {
    this.stage = stage;
    relationshipField.getItems().setAll(relationships);
    if (existing != null && existing.getRelationshipModel() != null && !relationshipField.getItems().contains(existing.getRelationshipModel())) {
      relationshipField.getItems().add(existing.getRelationshipModel());
    }
    relationshipField.setValue(existing != null ? existing.getRelationshipModel() : null);
    maxDepthField.getValueFactory().setValue(existing != null && existing.getMaxDepth() != null ? existing.getMaxDepth() : DEFAULT_MAX_DEPTH);
  }

  Optional<ExpansionDepth> getResult() {
    if (result.isPresent() && result.get() == ButtonType.OK) {
      return Optional.ofNullable(built);
    }
    return Optional.empty();
  }
}
