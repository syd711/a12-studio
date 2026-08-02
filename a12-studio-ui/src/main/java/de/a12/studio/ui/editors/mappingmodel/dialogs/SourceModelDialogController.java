package de.a12.studio.ui.editors.mappingmodel.dialogs;

import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.mappingmodel.MappingSource;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.components.DialogController;
import de.a12.studio.ui.util.ProjectDocumentModels;
import de.a12.studio.ui.util.WidgetFactory;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.jspecify.annotations.NonNull;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Modal Add/Edit dialog for a single {@link MappingSource}, opened from {@link
 * de.a12.studio.ui.editors.mappingmodel.SourceModelsPanelController} by clicking a row (or its Edit
 * button/Add button). Unlike e.g. {@link de.a12.studio.ui.editors.applicationmodel.dialogs.CaseDialogController},
 * {@code sourceModel} is only ever mutated once, in {@link #onDialogSubmit}, so Cancel needs no snapshot/undo -
 * an in-progress edit simply never gets applied.
 */
public class SourceModelDialogController implements DialogController {

  private static final int DEFAULT_REPETITIONS = 1;

  // Matches the "unbounded" repeatability cap used for Document Model groups (see
  // GroupPropertiesPanelController.MAX_REPEATABILITY).
  private static final int MAX_REPETITIONS = 999_999;

  @FXML
  private TextField nameField;

  @FXML
  private ComboBox<String> modelField;

  @FXML
  private Spinner<Integer> repetitionsField;

  @FXML
  private CheckBox skipValidationField;

  @FXML
  private Button okButton;

  private Stage stage;

  private MappingSource sourceModel;

  private Optional<ButtonType> result = Optional.of(ButtonType.CANCEL);

  @FXML
  private void initialize() {
    repetitionsField.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, MAX_REPETITIONS, DEFAULT_REPETITIONS));
    WidgetFactory.restrictToNumericInput(repetitionsField.getEditor());

    okButton.disableProperty().bind(nameField.textProperty().isEmpty().or(modelField.valueProperty().isNull()));
  }

  public void initDialog(Stage stage, @NonNull MappingSource sourceModel) {
    this.stage = stage;
    this.sourceModel = sourceModel;

    modelField.getItems().setAll(documentModelIds());
    nameField.setText(sourceModel.getName());
    modelField.setValue(sourceModel.getDmId());
    repetitionsField.getValueFactory().setValue(sourceModel.getMaxRepeat() != null ? sourceModel.getMaxRepeat() : DEFAULT_REPETITIONS);
    skipValidationField.setSelected(Boolean.TRUE.equals(sourceModel.getNoSourceValidation()));

    nameField.requestFocus();
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

  @Override
  public void onDialogCancel() {
    stage.close();
  }

  @FXML
  private void onDialogSubmit() {
    sourceModel.setName(nameField.getText().trim());
    sourceModel.setDmId(modelField.getValue());
    sourceModel.setMaxRepeat(repetitionsField.getValue());
    sourceModel.setNoSourceValidation(skipValidationField.isSelected());

    result = Optional.of(ButtonType.OK);
    stage.close();
  }

  boolean isConfirmed() {
    return result.isPresent() && result.get() == ButtonType.OK;
  }
}
