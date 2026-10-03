package de.a12.studio.ui.editors.structuralmappingmodel.dialogs;

import de.a12.studio.models.structuralmappingmodel.ResolutionStrategy;
import de.a12.studio.models.structuralmappingmodel.ResolutionStrategyType;
import de.a12.studio.models.structuralmappingmodel.SmmNode;
import de.a12.studio.models.structuralmappingmodel.SmmOperations;
import de.a12.studio.models.structuralmappingmodel.SmmPath;
import de.a12.studio.models.structuralmappingmodel.StructuralMappingModel;
import de.a12.studio.models.util.JsonSettings;
import de.a12.studio.modelsvalidation.kernel.StructuralMappingContext;
import de.a12.studio.ui.components.DialogController;
import de.a12.studio.ui.util.StudioBundle;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Modal editor of one resolution strategy (SME's {@code ResolutionStrategyEditor}): how the repetition of the target
 * group is chosen - Fold appends a new repetition, Slice looks one up by a field - and which source group
 * repeats. What can be chosen is not up to the dialog but to the kernel, which is asked for the valid types, source
 * groups and slice fields of the strategy as it looks with the values chosen so far (on a working copy, so
 * Cancel needs no undo); the real strategy is only written in {@link #onDialogSubmit}.
 */
@Slf4j
public class ResolutionStrategyDialogController implements DialogController {

  @FXML
  private TextField targetGroupField;

  @FXML
  private VBox parentLayersBox;

  @FXML
  private ListView<String> parentLayersList;

  @FXML
  private ComboBox<ResolutionStrategyType> typeCombo;

  @FXML
  private ComboBox<String> sourceGroupCombo;

  @FXML
  private VBox sliceBox;

  @FXML
  private ComboBox<String> sliceSourceFieldCombo;

  @FXML
  private ComboBox<String> sliceTargetFieldCombo;

  @FXML
  private Button okButton;

  private Stage stage;

  private StructuralMappingContext context;

  // The model as it would look with the values chosen so far; the kernel is asked about this one.
  private StructuralMappingModel workingCopy;

  private ResolutionStrategy workingStrategy;

  private ResolutionStrategy strategy;

  private String pointer;

  // Set while the option lists are replaced, so that doing so is not taken for the user choosing something.
  private boolean updatingOptions;

  private boolean confirmed;

  @FXML
  private void initialize() {
    typeCombo.setConverter(new StringConverter<>() {
      @Override
      public String toString(ResolutionStrategyType type) {
        if (type == null) {
          return "";
        }
        return StudioBundle.get(type == ResolutionStrategyType.SLICE ? "structural_mapping.rs_type_slice" : "structural_mapping.rs_type_fold");
      }

      @Override
      public ResolutionStrategyType fromString(String string) {
        return null;
      }
    });
    typeCombo.valueProperty().addListener((observable, oldValue, newValue) -> onChosen());
    sourceGroupCombo.valueProperty().addListener((observable, oldValue, newValue) -> onChosen());
    sliceSourceFieldCombo.valueProperty().addListener((observable, oldValue, newValue) -> onChosen());
    sliceTargetFieldCombo.valueProperty().addListener((observable, oldValue, newValue) -> onChosen());
    sliceBox.managedProperty().bind(sliceBox.visibleProperty());
  }

  /**
   * @param node a resolution strategy node of {@code model}
   */
  void initDialog(@NonNull Stage stage, @NonNull StructuralMappingContext context, @NonNull StructuralMappingModel model, @NonNull SmmNode node) {
    this.stage = stage;
    this.context = context;
    this.pointer = node.pointer();
    this.strategy = node.resolutionStrategy();
    this.workingCopy = copyOf(model);
    this.workingStrategy = workingCopy.getContent().getMappingBlocks().get(node.blockIndex()).getResolutionStrategies().get(node.index());

    targetGroupField.setText(strategy.getTargetGroupFullName());
    parentLayersList.setItems(FXCollections.observableArrayList(parentLayersAsPaths(node)));
    parentLayersBox.setVisible(!parentLayersList.getItems().isEmpty());
    parentLayersBox.setManaged(!parentLayersList.getItems().isEmpty());

    updatingOptions = true;
    typeCombo.setValue(strategy.getType());
    sourceGroupCombo.setValue(strategy.getSourceGroupFullName());
    if (strategy.getSlice() != null) {
      sliceSourceFieldCombo.setValue(blankToNull(strategy.getSlice().getSourceFieldFullName()));
      sliceTargetFieldCombo.setValue(blankToNull(strategy.getSlice().getTargetFieldFullName()));
    }
    updatingOptions = false;
    refreshOptions();
  }

  private static List<String> parentLayersAsPaths(SmmNode node) {
    List<String> layers = new ArrayList<>();
    for (SmmNode parent = node.parent(); parent != null; parent = parent.parent()) {
      layers.add(0, parent.kind() + ": " + SmmPath.format(parent.sourcePath()) + " -> "
          + SmmPath.format(parent.targetPath()));
    }
    return layers;
  }

  private static String blankToNull(String value) {
    return value == null || value.isBlank() ? null : value;
  }

  private static StructuralMappingModel copyOf(StructuralMappingModel model) {
    try {
      return JsonSettings.objectMapper.readValue(JsonSettings.objectMapper.writeValueAsString(model), StructuralMappingModel.class);
    }
    catch (Exception e) {
      throw new IllegalStateException("The structural mapping model cannot be copied: " + e.getMessage(), e);
    }
  }

  private void onChosen() {
    if (updatingOptions) {
      return;
    }
    SmmOperations.updateResolutionStrategy(workingStrategy, typeCombo.getValue() != null ? typeCombo.getValue() : workingStrategy.getType(),
        sourceGroupCombo.getValue(), sliceSourceFieldCombo.getValue(), sliceTargetFieldCombo.getValue());
    refreshOptions();
  }

  /** Asks the kernel what is valid for the strategy as it is now, and offers exactly that. */
  private void refreshOptions() {
    updatingOptions = true;
    try {
      boolean slice = typeCombo.getValue() == ResolutionStrategyType.SLICE;
      sliceBox.setVisible(slice);

      try {
        setOptions(typeCombo, context.validTypes(workingCopy, pointer));
        setOptions(sourceGroupCombo, context.validSourceGroups(workingCopy, pointer));
        if (slice) {
          setOptions(sliceSourceFieldCombo, context.validSliceSourceFields(workingCopy, pointer));
          setOptions(sliceTargetFieldCombo, context.validSliceTargetFields(workingCopy, pointer));
        }
      }
      catch (StructuralMappingContext.Unavailable e) {
        // Keep what is shown: the user can still cancel, and the editor shows the problem.
        log.warn("The valid values of the resolution strategy {} cannot be determined: {}", pointer, e.getMessage());
      }
    }
    finally {
      updatingOptions = false;
    }
    okButton.setDisable(!isComplete());
  }

  /** Replaces the items, keeping the current value even if the kernel no longer lists it (so it is not lost silently). */
  private static <T> void setOptions(ComboBox<T> combo, Collection<T> options) {
    T current = combo.getValue();
    List<T> items = new ArrayList<>(options);
    if (current != null && !items.contains(current)) {
      items.add(0, current);
    }
    combo.setItems(FXCollections.observableArrayList(items));
    combo.setValue(current);
  }

  private boolean isComplete() {
    if (typeCombo.getValue() == null || sourceGroupCombo.getValue() == null) {
      return false;
    }
    return typeCombo.getValue() != ResolutionStrategyType.SLICE
        || (sliceSourceFieldCombo.getValue() != null && sliceTargetFieldCombo.getValue() != null);
  }

  @Override
  public void onDialogCancel() {
    stage.close();
  }

  @FXML
  private void onDialogSubmit() {
    SmmOperations.updateResolutionStrategy(strategy, typeCombo.getValue(), sourceGroupCombo.getValue(),
        sliceSourceFieldCombo.getValue(), sliceTargetFieldCombo.getValue());
    confirmed = true;
    stage.close();
  }

  boolean isConfirmed() {
    return confirmed;
  }
}
