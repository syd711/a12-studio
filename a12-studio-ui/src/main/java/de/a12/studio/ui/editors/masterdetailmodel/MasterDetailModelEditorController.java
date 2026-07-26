package de.a12.studio.ui.editors.masterdetailmodel;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.ModelReference;
import de.a12.studio.models.ModelType;
import de.a12.studio.models.masterdetailmodel.FormMapping;
import de.a12.studio.models.masterdetailmodel.MasterDetailModel;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.editors.AbstractEditorController;
import de.a12.studio.ui.editors.dialogs.Dialogs;
import de.a12.studio.ui.events.StudioEventManager;
import de.a12.studio.ui.util.ProjectDocumentModels;
import de.a12.studio.ui.util.WidgetFactory;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.shape.Circle;
import org.jspecify.annotations.NonNull;

import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Edits a {@link MasterDetailModel}: which {@link de.a12.studio.models.overviewmodel.OverviewModel} it
 * presents as the "master" list, the preferred detail form width, and a {@link FormMapping} per Document
 * Model the chosen Overview Model references (mirroring SME's {@code formMappingMiddleware}) — one row lets
 * the user assign which Form Model edits that Document Model's records.
 */
public class MasterDetailModelEditorController extends AbstractEditorController implements Initializable {

  private static final String DEFAULT_SETTINGS_TOOLTIP = "Model Settings";
  private static final int DEFAULT_FORM_WIDTH = 6;
  private static final int MAX_FORM_WIDTH = 11;

  @FXML
  private Tooltip settingsButtonTooltip;

  @FXML
  private Circle settingsErrorBadge;

  @FXML
  private ComboBox<String> overviewModelField;

  @FXML
  private Spinner<Integer> formWidthField;

  @FXML
  private GridPane formMappingGrid;

  private MasterDetailModel model;

  // Set while fields are being repopulated from the model, so that programmatic updates aren't mistaken
  // for user edits and don't trigger a save.
  private boolean updatingFromModel;

  @Override
  public void initialize(URL url, ResourceBundle resources) {
    formWidthField.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, MAX_FORM_WIDTH, DEFAULT_FORM_WIDTH));
    WidgetFactory.restrictToNumericInput(formWidthField.getEditor());

    overviewModelField.valueProperty().addListener((observable, oldValue, newValue) -> {
      if (updatingFromModel || model == null) {
        return;
      }
      model.getContent().setOverviewModel(newValue);
      refreshFormMapping();
      commitChange();
    });

    formWidthField.valueProperty().addListener((observable, oldValue, newValue) -> {
      if (updatingFromModel || model == null) {
        return;
      }
      model.getContent().setFormWidth(newValue);
      commitChange();
    });
  }

  @FXML
  public void onSettings(ActionEvent e) {
    Dialogs.openSettings();
    updateSettingsErrorBadge();
  }

  @Override
  public void loadModel(@NonNull A12Model<?> model) {
    load((MasterDetailModel) model);
    updateSettingsErrorBadge();
  }

  private void load(@NonNull MasterDetailModel model) {
    this.model = model;

    updatingFromModel = true;
    try {
      overviewModelField.getItems().setAll(overviewModelOptions());
      overviewModelField.setValue(model.getContent().getOverviewModel());
      formWidthField.getValueFactory().setValue(
          model.getContent().getFormWidth() != null ? model.getContent().getFormWidth() : DEFAULT_FORM_WIDTH);
    }
    finally {
      updatingFromModel = false;
    }

    refreshFormMapping();
  }

  private List<String> overviewModelOptions() {
    return ProjectDocumentModels.getOtherModelsOfType(projectItem, ModelType.OVERVIEW).stream()
        .map(A12Model::getId)
        .sorted(Comparator.naturalOrder())
        .toList();
  }

  /**
   * Reconciles {@code content.formMapping} against the Document Models the currently selected Overview
   * Model references (its header references with {@link ModelReference#PURPOSE_DOCUMENT_MODEL_FOR_OVERVIEW}),
   * preserving any already-chosen Form Model per Document Model, then rebuilds the grid rows.
   */
  private void refreshFormMapping() {
    List<String> documentModelIds = referencedDocumentModelIds(model.getContent().getOverviewModel());

    List<FormMapping> formMapping = model.getContent().getFormMapping();
    List<FormMapping> reconciled = new ArrayList<>();
    for (String documentModelId : documentModelIds) {
      FormMapping existing = formMapping.stream()
          .filter(mapping -> documentModelId.equals(mapping.getDocumentModel()))
          .findFirst()
          .orElse(null);
      FormMapping mapping = new FormMapping();
      mapping.setDocumentModel(documentModelId);
      mapping.setFormModel(existing != null ? existing.getFormModel() : null);
      reconciled.add(mapping);
    }
    formMapping.clear();
    formMapping.addAll(reconciled);

    rebuildFormMappingRows();
  }

  private List<String> referencedDocumentModelIds(String overviewModelId) {
    if (overviewModelId == null) {
      return List.of();
    }
    return ProjectDocumentModels.getOtherModelsOfType(projectItem, ModelType.OVERVIEW).stream()
        .filter(overviewModel -> overviewModelId.equals(overviewModel.getId()))
        .findFirst()
        .map(overviewModel -> overviewModel.getModelReferences().stream()
            .filter(reference -> reference.getModelType() == ModelType.DOCUMENT
                && ModelReference.PURPOSE_DOCUMENT_MODEL_FOR_OVERVIEW.equals(reference.getPurpose()))
            .map(ModelReference::getReference)
            .toList())
        .orElse(List.of());
  }

  private void rebuildFormMappingRows() {
    formMappingGrid.getChildren().removeIf(node -> {
      Integer rowIndex = GridPane.getRowIndex(node);
      return rowIndex != null && rowIndex > 0;
    });

    List<FormMapping> formMapping = model.getContent().getFormMapping();
    for (int index = 0; index < formMapping.size(); index++) {
      addFormMappingRow(formMapping.get(index), index);
    }
  }

  private void addFormMappingRow(FormMapping mapping, int index) {
    Label documentModelLabel = new Label(mapping.getDocumentModel());

    ComboBox<String> formModelField = new ComboBox<>();
    formModelField.setId("formMappingFormModel-" + index);
    formModelField.setMaxWidth(Double.MAX_VALUE);
    formModelField.getItems().setAll(formModelOptionsFor(mapping.getDocumentModel()));
    formModelField.setValue(mapping.getFormModel());
    formModelField.valueProperty().addListener((observable, oldValue, newValue) -> {
      mapping.setFormModel(newValue);
      commitChange();
    });

    formMappingGrid.addRow(index + 1, documentModelLabel, formModelField);
  }

  private List<String> formModelOptionsFor(String documentModelId) {
    return ProjectDocumentModels.getOtherModelsOfType(projectItem, ModelType.FORM).stream()
        .filter(formModel -> formModel.getModelReferences().stream()
            .anyMatch(reference -> reference.getModelType() == ModelType.DOCUMENT && documentModelId.equals(reference.getReference())))
        .map(A12Model::getId)
        .sorted(Comparator.naturalOrder())
        .toList();
  }

  private void commitChange() {
    projectItem.save();
    StudioEventManager.getInstance().fireModelSaveEvent(projectItem);
  }

  private void updateSettingsErrorBadge() {
    List<String> issues = projectItem.getModel() != null
        ? Studio.getValidationService().getSettingsIssueMessages(projectItem.getModel())
        : List.of();

    settingsErrorBadge.setVisible(!issues.isEmpty());
    settingsButtonTooltip.setText(issues.isEmpty() ? DEFAULT_SETTINGS_TOOLTIP : String.join("\n\n", issues));
  }

  @Override
  public @NonNull ModelType getModelType() {
    return ModelType.MASTERDETAIL;
  }
}
