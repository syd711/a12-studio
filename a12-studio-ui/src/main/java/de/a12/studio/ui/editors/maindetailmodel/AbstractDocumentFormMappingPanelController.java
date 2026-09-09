package de.a12.studio.ui.editors.maindetailmodel;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.ModelType;
import de.a12.studio.models.masterdetailmodel.FormMapping;
import de.a12.studio.models.masterdetailmodel.MasterDetailModel;
import de.a12.studio.models.projects.ProjectItem;
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
 * Shared "one row per Document Model, a Form Model combobox in the second column" grid editor behind {@link
 * MainDetailFormMappingPanelController}, {@link RelationshipEditorsPanelController} and {@link
 * LinkDocumentEditorsPanelController}: reconciles a {@link FormMapping} list against a supplied set of
 * Document Model ids (preserving any already-chosen Form Model per Document Model), rebuilds the grid rows,
 * and requires a Form Model selection on every row. Isn't wired through the Element-bound half of {@link
 * AbstractPropertyEditor} for the same reason as {@link MainModelReferencePanelController}: subclasses edit
 * a list directly on {@link MasterDetailModel}'s content, and the owning editor is expected to call {@link
 * #load} again whenever the master model reference changes so the row set can be reconciled against the new
 * set of referenced Document Models.
 */
public abstract class AbstractDocumentFormMappingPanelController extends AbstractPropertyEditor {

  @FXML
  private GridPane formMappingGrid;

  protected MasterDetailModel model;
  protected ProjectItem projectItem;

  /** Hides this panel entirely for {@code content.type == "overview"}, where it doesn't apply. */
  public void setVisible(boolean visible) {
    setEditorVisible(visible);
  }

  /**
   * Reconciles this panel's mapping list against {@code documentModelIds}, preserving any already-chosen
   * Form Model per Document Model, then rebuilds the grid rows.
   */
  public void load(@NonNull MasterDetailModel model, @NonNull ProjectItem projectItem, @NonNull List<String> documentModelIds) {
    this.model = model;
    this.projectItem = projectItem;

    List<FormMapping> existing = currentMappings();
    List<FormMapping> reconciled = new ArrayList<>();
    for (String documentModelId : documentModelIds) {
      FormMapping existingMapping = existing.stream()
          .filter(mapping -> documentModelId.equals(mapping.getDocumentModel()))
          .findFirst()
          .orElse(null);
      FormMapping mapping = new FormMapping();
      mapping.setDocumentModel(documentModelId);
      mapping.setFormModel(existingMapping != null ? existingMapping.getFormModel() : null);
      reconciled.add(mapping);
    }
    applyMappings(reconciled);

    rebuildRows();
    validate();
  }

  /** The list of {@link FormMapping}s this panel edits, e.g. {@code model.getContent().getFormMapping()}. */
  protected abstract List<FormMapping> currentMappings();

  /** Writes a reconciled mapping list back into {@link #model}'s content. */
  protected abstract void applyMappings(@NonNull List<FormMapping> mappings);

  /** Distinguishes this panel's row comboboxes' node ids from its sibling panels' for UI-test automation. */
  protected abstract String rowIdPrefix();

  private void rebuildRows() {
    formMappingGrid.getChildren().removeIf(node -> {
      Integer rowIndex = GridPane.getRowIndex(node);
      return rowIndex != null && rowIndex > 0;
    });

    List<FormMapping> mappings = currentMappings();
    for (int index = 0; index < mappings.size(); index++) {
      addRow(mappings.get(index), index);
    }
  }

  private void addRow(@NonNull FormMapping mapping, int index) {
    Label documentModelLabel = new Label(mapping.getDocumentModel());
    documentModelLabel.setMaxWidth(Double.MAX_VALUE);
    HBox.setHgrow(documentModelLabel, Priority.ALWAYS);

    ComboBox<String> formModelField = new ComboBox<>();
    formModelField.setId(rowIdPrefix() + "-" + index);
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
    List<String> missingDocumentModels = currentMappings().stream()
        .filter(mapping -> mapping.getFormModel() == null)
        .map(FormMapping::getDocumentModel)
        .toList();
    if (!missingDocumentModels.isEmpty()) {
      showError("ERROR", "A Form Model must be selected for: " + String.join(", ", missingDocumentModels) + ".");
    }
    else {
      hideError();
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
