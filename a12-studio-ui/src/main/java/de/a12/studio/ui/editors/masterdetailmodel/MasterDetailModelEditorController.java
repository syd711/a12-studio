package de.a12.studio.ui.editors.masterdetailmodel;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.ModelReference;
import de.a12.studio.models.ModelType;
import de.a12.studio.models.masterdetailmodel.FormMapping;
import de.a12.studio.models.masterdetailmodel.MasterDetailModel;
import de.a12.studio.ui.editors.AbstractEditorController;
import de.a12.studio.ui.editors.propertyeditors.FormWidthPanelController;
import de.a12.studio.ui.editors.propertyeditors.MasterModelReferencePanelController;
import de.a12.studio.ui.events.StudioEventManager;
import de.a12.studio.ui.util.ProjectDocumentModels;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import org.jspecify.annotations.NonNull;

import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Edits a {@link MasterDetailModel}: whether it presents an {@link de.a12.studio.models.overviewmodel.OverviewModel}
 * or a {@link de.a12.studio.models.treemodel.TreeModel} as the "master" list (and which one), the preferred
 * detail form width, and a {@link FormMapping} per Document Model the chosen master model references
 * (mirroring SME's {@code formMappingMiddleware}) — one row lets the user assign which Form Model edits that
 * Document Model's records.
 */
public class MasterDetailModelEditorController extends AbstractEditorController implements Initializable {

  @FXML
  private MasterModelReferencePanelController masterModelReferenceController;

  @FXML
  private FormWidthPanelController formWidthPanelController;

  @FXML
  private GridPane formMappingGrid;

  private MasterDetailModel model;

  @Override
  public void initialize(URL url, ResourceBundle resources) {
    masterModelReferenceController.setOnChange(() -> {
      refreshFormMapping();
      commitChange();
    });
  }

  @Override
  public void loadModel(@NonNull A12Model<?> model) {
    load((MasterDetailModel) model);
    updateSettingsErrorBadge();
  }

  private void load(@NonNull MasterDetailModel model) {
    this.model = model;

    masterModelReferenceController.load(model, overviewModelOptions(), treeModelOptions());

    formWidthPanelController.setModel(model);
    refreshFormMapping();
  }

  private List<String> overviewModelOptions() {
    return ProjectDocumentModels.getOtherModelsOfType(projectItem, ModelType.OVERVIEW).stream()
        .map(A12Model::getId)
        .sorted(Comparator.naturalOrder())
        .toList();
  }

  private List<String> treeModelOptions() {
    return ProjectDocumentModels.getOtherModelsOfType(projectItem, ModelType.TREE).stream()
        .map(A12Model::getId)
        .sorted(Comparator.naturalOrder())
        .toList();
  }

  /**
   * Reconciles {@code content.formMapping} against the Document Models the currently selected master model
   * (Overview or Tree, per {@code content.type}) references, preserving any already-chosen Form Model per
   * Document Model, then rebuilds the grid rows.
   */
  private void refreshFormMapping() {
    List<String> documentModelIds = referencedDocumentModelIds();

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

  /**
   * The Document Model ids referenced (via {@link ModelReference#PURPOSE_DOCUMENT_MODEL_FOR_OVERVIEW} or
   * {@link ModelReference#PURPOSE_DOCUMENT_MODEL_FOR_TREE}) by whichever master model is currently selected.
   */
  private List<String> referencedDocumentModelIds() {
    if ("tree".equals(model.getContent().getType())) {
      return referencedDocumentModelIds(model.getContent().getTreeModel(), ModelType.TREE, ModelReference.PURPOSE_DOCUMENT_MODEL_FOR_TREE);
    }
    return referencedDocumentModelIds(model.getContent().getOverviewModel(), ModelType.OVERVIEW, ModelReference.PURPOSE_DOCUMENT_MODEL_FOR_OVERVIEW);
  }

  private List<String> referencedDocumentModelIds(String masterModelId, ModelType masterModelType, String purpose) {
    if (masterModelId == null) {
      return List.of();
    }
    return ProjectDocumentModels.getOtherModelsOfType(projectItem, masterModelType).stream()
        .filter(masterModel -> masterModelId.equals(masterModel.getId()))
        .findFirst()
        .map(masterModel -> masterModel.getModelReferences().stream()
            .filter(reference -> reference.getModelType() == ModelType.DOCUMENT && purpose.equals(reference.getPurpose()))
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
    StudioEventManager.getInstance().fireModelSavedEvent(projectItem);
  }

  @Override
  public @NonNull ModelType getModelType() {
    return ModelType.MASTERDETAIL;
  }
}
