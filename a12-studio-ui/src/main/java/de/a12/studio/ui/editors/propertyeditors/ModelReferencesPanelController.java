package de.a12.studio.ui.editors.propertyeditors;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.ModelReference;
import de.a12.studio.models.ModelType;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.editors.AbstractPropertyEditor;
import de.a12.studio.ui.util.Icons;
import de.a12.studio.ui.util.ProjectDocumentModels;
import de.a12.studio.ui.util.WidgetFactory;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.jspecify.annotations.NonNull;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Edits an {@link A12Model}'s header {@link A12Model#getModelReferences()}: a list of {@link ModelReference}s,
 * each naming an alias/purpose and pointing at another model of a chosen {@link ModelType}. Same row-based
 * layout as {@link AnnotationsPanelController}'s model-header mode, with two dependent combo boxes instead of
 * a single name/value pair: picking a Model Type repopulates the Reference combo box with every other model
 * of that type in the project (see {@link ProjectDocumentModels#getOtherModelsOfType}), and resets the
 * previously chosen reference since it no longer necessarily matches the new type.
 */
public class ModelReferencesPanelController extends AbstractPropertyEditor {

  @FXML
  private GridPane referencesGrid;

  private A12Model<?> model;

  // Application Models reference Master-Detail Models exclusively (see modelTypeOptions), and don't use
  // alias/purpose for that (mirrors SME, which locks these columns read-only in the Application Model's
  // Model References panel).
  private boolean applicationModel;

  public void setModel(@NonNull A12Model<?> model) {
    this.model = model;
    this.applicationModel = model.getModelType() == ModelType.APPLICATION;
    rebuildRows();
  }

  @FXML
  private void onAdd() {
    getModelReferences().add(new ModelReference());
    rebuildRows();
    commitChange();
  }

  private List<ModelReference> getModelReferences() {
    return model.getModelReferences();
  }

  private void rebuildRows() {
    referencesGrid.getChildren().removeIf(node -> {
      Integer rowIndex = GridPane.getRowIndex(node);
      return rowIndex != null && rowIndex > 0;
    });

    List<ModelReference> references = getModelReferences();
    setHeaderRowVisible(!references.isEmpty());
    if (references.isEmpty()) {
      Label emptyLabel = new Label("No model references found.");
      emptyLabel.getStyleClass().add("placeholder-label");
      referencesGrid.add(emptyLabel, 0, 1, 5, 1);
      return;
    }

    for (int index = 0; index < references.size(); index++) {
      addRow(references.get(index), index, references.size());
    }
  }

  // The column title Labels (Alias/Purpose/Model Type/Reference) live in row 0 of referencesGrid, defined in
  // FXML rather than built here, so they're found by row index instead of by fx:id.
  private void setHeaderRowVisible(boolean visible) {
    for (Node node : referencesGrid.getChildren()) {
      Integer rowIndex = GridPane.getRowIndex(node);
      if (rowIndex == null || rowIndex == 0) {
        node.setVisible(visible);
        node.setManaged(visible);
      }
    }
  }

  private void addRow(ModelReference reference, int index, int rowCount) {
    TextField aliasField = new TextField();
    aliasField.setId("modelReferenceAlias-" + index);
    aliasField.setMaxWidth(Double.MAX_VALUE);
    aliasField.setEditable(!applicationModel);
    setFieldValue(aliasField, reference.getAlias());
    bindTextField(aliasField, (el, value) -> reference.setAlias(value));

    TextField purposeField = new TextField();
    purposeField.setId("modelReferencePurpose-" + index);
    purposeField.setMaxWidth(Double.MAX_VALUE);
    purposeField.setEditable(!applicationModel);
    setFieldValue(purposeField, reference.getPurpose());
    bindTextField(purposeField, (el, value) -> reference.setPurpose(value));

    ComboBox<String> referenceField = new ComboBox<>();
    referenceField.setId("modelReferenceReference-" + index);
    referenceField.setMaxWidth(Double.MAX_VALUE);
    referenceField.getItems().setAll(referenceOptionsFor(reference.getModelType()));
    setFieldValue(referenceField, reference.getReference());
    bindComboBox(referenceField, (el, value) -> reference.setReference(value));

    ComboBox<String> modelTypeField = new ComboBox<>();
    modelTypeField.setId("modelReferenceModelType-" + index);
    modelTypeField.setMaxWidth(Double.MAX_VALUE);
    modelTypeField.getItems().setAll(modelTypeOptions());
    setFieldValue(modelTypeField, reference.getModelType() != null ? reference.getModelType().getValue() : null);
    bindComboBox(modelTypeField, (el, value) -> {
      reference.setModelType(value != null ? ModelType.fromValue(value) : null);
      reference.setReference(null);
      setComboBoxItems(referenceField, referenceOptionsFor(reference.getModelType()));
      setFieldValue(referenceField, null);
    });

    referencesGrid.addRow(index + 1, aliasField, purposeField, modelTypeField, referenceField, createActionsBox(reference, index, rowCount));
  }

  /**
   * The Model Type values selectable in this panel's dropdown. An Application Model's header references
   * exist to point at Master-Detail Models (which get synthesized into the app's modules), so it's the only
   * type offered there; conversely, referencing a Master-Detail Model only makes sense from an Application
   * Model, so every other model type's panel excludes it.
   */
  private List<String> modelTypeOptions() {
    return Arrays.stream(ModelType.values())
        .filter(type -> applicationModel ? type == ModelType.MASTERDETAIL : type != ModelType.MASTERDETAIL)
        .map(ModelType::getValue)
        .toList();
  }

  /**
   * Every other model of {@code modelType} in the current project, by id, sorted for a stable dropdown order.
   * Empty (rather than every model) until a Model Type is chosen, since a reference is only meaningful once
   * its type narrows down which models are valid targets.
   */
  private List<String> referenceOptionsFor(ModelType modelType) {
    if (modelType == null) {
      return List.of();
    }
    ProjectItem projectItem = Studio.getSelectedProjectItem();
    if (projectItem == null) {
      return List.of();
    }
    return ProjectDocumentModels.getOtherModelsOfType(projectItem, modelType).stream()
        .map(A12Model::getId)
        .sorted(Comparator.naturalOrder())
        .toList();
  }

  private HBox createActionsBox(ModelReference reference, int index, int rowCount) {
    VBox moveButtonsBox = RowFactory.createMoveButtonsBox(index, rowCount, this::moveRow);

    Button copyButton = RowFactory.createActionButton(Icons.COPY, "Copy", () -> {
      ModelReference copy = new ModelReference();
      copy.setAlias(reference.getAlias());
      copy.setPurpose(reference.getPurpose());
      copy.setModelType(reference.getModelType());
      copy.setReference(reference.getReference());
      List<ModelReference> references = getModelReferences();
      references.add(references.indexOf(reference) + 1, copy);
      rebuildRows();
      commitChange();
    });

    Button deleteButton = RowFactory.createActionButton(Icons.TRASH, "Delete", () -> {
      Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, "Delete this model reference?", null, null, "Delete");
      if (result.isPresent() && result.get() == ButtonType.OK) {
        getModelReferences().remove(reference);
        rebuildRows();
        commitChange();
      }
    });

    HBox actionsBox = new HBox(4.0, moveButtonsBox, copyButton, deleteButton);
    actionsBox.setAlignment(Pos.CENTER_LEFT);
    return actionsBox;
  }

  private void moveRow(int fromIndex, int toIndex) {
    Collections.swap(getModelReferences(), fromIndex, toIndex);
    rebuildRows();
    commitChange();
  }
}
