package de.a12.studio.ui.editors.propertyeditors;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.ModelType;
import de.a12.studio.models.masterdetailmodel.FormMapping;
import de.a12.studio.models.masterdetailmodel.MasterDetailModel;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.ui.components.ErrorContainerController;
import de.a12.studio.ui.editors.AbstractPropertyEditor;
import de.a12.studio.ui.util.ProjectDocumentModels;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Edits a {@link MasterDetailModel}'s {@link FormMapping} list: one row per Document Model the currently
 * selected master model (Overview or Tree) references (mirroring SME's {@code formMappingMiddleware}), each
 * showing that Document Model in the first column and a combobox of the Form Models available for it in the
 * second. Isn't wired through the Element-bound half of {@link AbstractPropertyEditor} for the same reason as
 * {@link MainModelReferencePanelController}: it edits {@code content.formMapping} directly, and the owning
 * editor is expected to call {@link #load} again whenever the master model reference changes so the row set
 * can be reconciled against the new set of referenced Document Models.
 */
public class MainDetailFormMappingPanelController extends AbstractPropertyEditor {

  @FXML
  private GridPane formMappingGrid;

  @FXML
  private ErrorContainerController errorContainerController;

  private MasterDetailModel model;
  private ProjectItem projectItem;

  /**
   * Reconciles {@code content.formMapping} against {@code documentModelIds}, preserving any already-chosen
   * Form Model per Document Model, then rebuilds the grid rows.
   */
  public void load(@NonNull MasterDetailModel model, @NonNull ProjectItem projectItem, @NonNull List<String> documentModelIds) {
    this.model = model;
    this.projectItem = projectItem;

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

    rebuildRows();
    validate();
  }

  private void rebuildRows() {
    formMappingGrid.getChildren().removeIf(node -> {
      Integer rowIndex = GridPane.getRowIndex(node);
      return rowIndex != null && rowIndex > 0;
    });

    List<FormMapping> formMapping = model.getContent().getFormMapping();
    for (int index = 0; index < formMapping.size(); index++) {
      addRow(formMapping.get(index), index);
    }
  }

  private void addRow(@NonNull FormMapping mapping, int index) {
    Label documentModelLabel = new Label(mapping.getDocumentModel());
    documentModelLabel.setMaxWidth(Double.MAX_VALUE);
    HBox.setHgrow(documentModelLabel, Priority.ALWAYS);

    ComboBox<String> formModelField = new ComboBox<>();
    formModelField.setId("formMappingFormModel-" + index);
    formModelField.setMaxWidth(Double.MAX_VALUE);
    formModelField.getItems().setAll(formModelOptionsFor(mapping.getDocumentModel()));
    formModelField.setValue(mapping.getFormModel());
    formModelField.valueProperty().addListener((observable, oldValue, newValue) -> {
      mapping.setFormModel(newValue);
      validate();
      commitHeaderChange();
    });
    HBox.setHgrow(formModelField, Priority.ALWAYS);

    HBox row = new HBox(10.0, documentModelLabel, formModelField);
    row.setAlignment(Pos.CENTER_LEFT);
    row.getStyleClass().add("module-row");

    formMappingGrid.add(row, 0, index + 1, 2, 1);
  }

  /** Every row's Form Model selection is required. */
  private void validate() {
    List<String> missingDocumentModels = model.getContent().getFormMapping().stream()
        .filter(mapping -> mapping.getFormModel() == null)
        .map(FormMapping::getDocumentModel)
        .toList();
    if (!missingDocumentModels.isEmpty()) {
      errorContainerController.show("ERROR",
          "A Form Model must be selected for: " + String.join(", ", missingDocumentModels) + ".");
    }
    else {
      errorContainerController.hide();
    }
  }

  private List<String> formModelOptionsFor(@NonNull String documentModelId) {
    return ProjectDocumentModels.getOtherModelsOfType(projectItem, ModelType.FORM).stream()
        .filter(formModel -> formModel.getModelReferences().stream()
            .anyMatch(reference -> reference.getModelType() == ModelType.DOCUMENT && documentModelId.equals(reference.getReference())))
        .map(A12Model::getId)
        .sorted(Comparator.naturalOrder())
        .toList();
  }
}
