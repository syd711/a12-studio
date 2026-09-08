package de.a12.studio.ui.editors.combineddocumentmodel.dialogs;

import de.a12.studio.models.combineddocumentmodel.CombinationStep;
import de.a12.studio.models.combineddocumentmodel.CombinationStepType;
import de.a12.studio.models.combineddocumentmodel.DocumentModelIdRef;
import de.a12.studio.models.combineddocumentmodel.SelectionModelIdRef;
import de.a12.studio.ui.components.DialogController;
import de.a12.studio.ui.util.StudioBundle;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.Optional;

/**
 * Modal Add/Edit dialog for a single {@link CombinationStep}, opened from {@link
 * de.a12.studio.ui.editors.combineddocumentmodel.CombinationStepsPanelController} (via {@link Dialogs}) by
 * clicking a row or its Edit/Add button. Same "mutate only in onDialogSubmit" shape as
 * {@link de.a12.studio.ui.editors.mappingmodel.dialogs.SourceModelDialogController} - Cancel needs no
 * snapshot/undo since {@code combinationStep} is only ever written once, on OK.
 * <p>
 * The three model fields (Addition/Selection/Decoration) are shown/enabled based on the selected {@link
 * CombinationStepType}, mirroring SME's {@code dependentField} readonly rules in {@code CombinationEditor.json}:
 * only Addition needs the Additive model; Selection and both Decoration types need the Selection model; only
 * the Decoration types also need the Decoration model. Switching {@link #typeField} always clears whichever
 * field(s) just became irrelevant, so a step built through this dialog can never violate the
 * {@code *_NOT_ALLOWED} rules in {@code de.a12.studio.modelsvalidation.validators.combination}.
 */
public class CombinationStepDialogController implements DialogController {

  @FXML
  private ComboBox<CombinationStepType> typeField;

  @FXML
  private VBox additiveModelBox;
  @FXML
  private ComboBox<String> additiveModelField;

  @FXML
  private VBox selectionModelBox;
  @FXML
  private ComboBox<String> selectionModelField;

  @FXML
  private VBox decorationModelBox;
  @FXML
  private ComboBox<String> decorationModelField;

  @FXML
  private Button okButton;

  private Stage stage;

  private CombinationStep combinationStep;

  private Optional<ButtonType> result = Optional.of(ButtonType.CANCEL);

  @FXML
  private void initialize() {
    typeField.setConverter(new StringConverter<>() {
      @Override
      public String toString(CombinationStepType type) {
        return type == null ? "" : displayName(type);
      }

      @Override
      public CombinationStepType fromString(String string) {
        return null;
      }
    });
  }

  private static String displayName(CombinationStepType type) {
    return switch (type) {
      case ADDITION -> StudioBundle.get("combination_step_type.addition");
      case SELECTION -> StudioBundle.get("combination_step_type.selection");
      case DECORATION_FOR_FIELDS -> StudioBundle.get("combination_step_type.decoration_for_fields");
      case DECORATION_FOR_GROUPS -> StudioBundle.get("combination_step_type.decoration_for_groups");
    };
  }

  public void initDialog(Stage stage, @NonNull CombinationStep combinationStep, @NonNull List<String> documentModelIds,
      @NonNull List<String> selectionModelIds) {
    this.stage = stage;
    this.combinationStep = combinationStep;

    typeField.getItems().setAll(CombinationStepType.values());
    additiveModelField.getItems().setAll(documentModelIds);
    selectionModelField.getItems().setAll(selectionModelIds);
    decorationModelField.getItems().setAll(documentModelIds);

    CombinationStepType type = combinationStep.getType() != null ? combinationStep.getType() : CombinationStepType.ADDITION;
    typeField.setValue(type);
    if (combinationStep.getAdditiveModel() != null) {
      additiveModelField.setValue(combinationStep.getAdditiveModel().getDmId());
    }
    if (combinationStep.getSelectionModel() != null) {
      selectionModelField.setValue(combinationStep.getSelectionModel().getSmId());
    }
    if (combinationStep.getDecorationModel() != null) {
      decorationModelField.setValue(combinationStep.getDecorationModel().getDmId());
    }

    updateFieldVisibility(type);
    typeField.valueProperty().addListener((observable, oldValue, newValue) -> updateFieldVisibility(newValue));

    okButton.disableProperty().bind(Bindings.createBooleanBinding(this::isInvalid,
        typeField.valueProperty(), additiveModelField.valueProperty(), selectionModelField.valueProperty(), decorationModelField.valueProperty()));
  }

  /**
   * Shows/enables only the field(s) relevant to {@code type} and clears the value of every field that becomes
   * irrelevant - called both while populating the dialog and on every user-driven Type change, so a step whose
   * hand-edited JSON carries a now-irrelevant reference is self-healed as soon as its dialog is opened.
   */
  private void updateFieldVisibility(CombinationStepType type) {
    boolean needsAdditive = type == CombinationStepType.ADDITION;
    boolean needsSelection = type == CombinationStepType.SELECTION
        || type == CombinationStepType.DECORATION_FOR_FIELDS || type == CombinationStepType.DECORATION_FOR_GROUPS;
    boolean needsDecoration = type == CombinationStepType.DECORATION_FOR_FIELDS || type == CombinationStepType.DECORATION_FOR_GROUPS;

    setRowVisible(additiveModelBox, additiveModelField, needsAdditive);
    setRowVisible(selectionModelBox, selectionModelField, needsSelection);
    setRowVisible(decorationModelBox, decorationModelField, needsDecoration);
  }

  private static void setRowVisible(VBox box, ComboBox<String> field, boolean visible) {
    box.setVisible(visible);
    box.setManaged(visible);
    if (!visible) {
      field.setValue(null);
    }
  }

  private boolean isInvalid() {
    CombinationStepType type = typeField.getValue();
    if (type == null) {
      return true;
    }
    return switch (type) {
      case ADDITION -> additiveModelField.getValue() == null;
      case SELECTION -> selectionModelField.getValue() == null;
      case DECORATION_FOR_FIELDS, DECORATION_FOR_GROUPS -> selectionModelField.getValue() == null || decorationModelField.getValue() == null;
    };
  }

  @Override
  public void onDialogCancel() {
    stage.close();
  }

  @FXML
  private void onDialogSubmit() {
    CombinationStepType type = typeField.getValue();
    combinationStep.setType(type);
    combinationStep.setAdditiveModel(type == CombinationStepType.ADDITION ? documentModelRef(additiveModelField.getValue()) : null);
    boolean needsSelection = type == CombinationStepType.SELECTION
        || type == CombinationStepType.DECORATION_FOR_FIELDS || type == CombinationStepType.DECORATION_FOR_GROUPS;
    combinationStep.setSelectionModel(needsSelection ? selectionModelRef(selectionModelField.getValue()) : null);
    boolean needsDecoration = type == CombinationStepType.DECORATION_FOR_FIELDS || type == CombinationStepType.DECORATION_FOR_GROUPS;
    combinationStep.setDecorationModel(needsDecoration ? documentModelRef(decorationModelField.getValue()) : null);

    result = Optional.of(ButtonType.OK);
    stage.close();
  }

  private static DocumentModelIdRef documentModelRef(String dmId) {
    DocumentModelIdRef ref = new DocumentModelIdRef();
    ref.setDmId(dmId);
    return ref;
  }

  private static SelectionModelIdRef selectionModelRef(String smId) {
    SelectionModelIdRef ref = new SelectionModelIdRef();
    ref.setSmId(smId);
    return ref;
  }

  boolean isConfirmed() {
    return result.isPresent() && result.get() == ButtonType.OK;
  }
}
