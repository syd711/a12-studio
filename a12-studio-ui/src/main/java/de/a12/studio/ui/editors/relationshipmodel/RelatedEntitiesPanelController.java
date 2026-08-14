package de.a12.studio.ui.editors.relationshipmodel;

import de.a12.studio.models.relationshipmodel.EntityCharacteristic;
import de.a12.studio.models.relationshipmodel.RelationshipModel;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.validators.relationship.RelationshipEntityCountValidator;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.editors.AbstractPropertyEditor;
import de.a12.studio.ui.editors.propertyeditors.RowFactory;
import de.a12.studio.ui.editors.relationshipmodel.dialogs.Dialogs;
import de.a12.studio.ui.util.Icons;
import de.a12.studio.ui.util.ProjectDocumentModels;
import de.a12.studio.ui.util.StudioBundle;
import de.a12.studio.ui.util.WidgetFactory;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.Optional;

/**
 * Edits a {@link RelationshipModel}'s {@code content.entityCharacteristics}: one row per {@link
 * EntityCharacteristic}, summarizing its Role, Document Model, Upper Limit and a computed, human-readable
 * description of it (see {@link EntityCharacteristicSupport#describeUpperLimitExplanation} - not a stored
 * field, mirroring SME's own expression-driven overview column), and Orderable flag. Not bound to a single
 * {@link de.a12.studio.models.documentmodel.Element}, so it follows the model-header pattern used by e.g. {@link
 * de.a12.studio.ui.editors.overviewmodel.OverviewColumnsPanelController}. Clicking a row (or its Edit button)
 * opens {@link Dialogs#showEntityForEdit}, the Entity Characteristics / Link Constraints / Labels editor for
 * that entity; the Add Entity button opens the same editor via {@link Dialogs#showEntityForAdd}, only adding
 * the new entity to {@code content.entityCharacteristics} once it's confirmed. Reordering isn't offered (SME's
 * own relationship model editor disables it too), and the Add Entity button hides itself once two entities
 * exist - a relationship model always connects exactly two, enforced by {@link RelationshipEntityCountValidator}
 * and surfaced in this panel's own error container (see {@link #refreshEntityCountError}) whenever it isn't.
 */
public class RelatedEntitiesPanelController extends AbstractPropertyEditor {

  private static final int MAX_ENTITIES = 2;

  @FXML
  private HBox entityHeaders;

  @FXML
  private VBox entityRows;

  @FXML
  private Label entitiesEmptyLabel;

  @FXML
  private Button addButton;

  private RelationshipModel model;

  private List<String> documentModelOptions = List.of();

  // Notified after every structural or field change, so the owning editor can re-sync the header's Document
  // Model references from the current entities.
  private Runnable onChange = () -> {
  };

  public void setModel(@NonNull RelationshipModel model) {
    this.model = model;
    rebuildRows();
  }

  public void setDocumentModelOptions(@NonNull List<String> documentModelOptions) {
    this.documentModelOptions = documentModelOptions;
  }

  public void setOnChange(@NonNull Runnable onChange) {
    this.onChange = onChange;
  }

  @FXML
  private void onAdd() {
    Dialogs.showEntityForAdd(Studio.stage, documentModelOptions).ifPresent(entity -> {
      getEntities().add(entity);
      rebuildRows();
      notifyChanged();
    });
  }

  private List<EntityCharacteristic> getEntities() {
    return model.getContent().getEntityCharacteristics();
  }

  private void rebuildRows() {
    if (model == null) {
      return;
    }
    refreshEntityCountError();
    entityRows.getChildren().clear();

    List<EntityCharacteristic> entities = getEntities();
    boolean empty = entities.isEmpty();
    entityHeaders.setVisible(!empty);
    entityHeaders.setManaged(!empty);
    entitiesEmptyLabel.setVisible(empty);
    entitiesEmptyLabel.setManaged(empty);

    boolean canAdd = entities.size() < MAX_ENTITIES;
    addButton.setVisible(canAdd);
    addButton.setManaged(canAdd);

    for (EntityCharacteristic entity : entities) {
      entityRows.getChildren().add(createRow(entity));
    }
  }

  private HBox createRow(EntityCharacteristic entity) {
    Label roleLabel = createRowLabel(entity.getRole() != null ? entity.getRole() : "", 140.0, entity);
    Label documentModelLabel = createRowLabel(entity.getDocumentModel() != null ? entity.getDocumentModel() : "", 160.0, entity);
    Label upperLimitLabel = createRowLabel(EntityCharacteristicSupport.describeUpperLimit(entity), 90.0, entity);
    Label descriptionLabel = createRowLabel(EntityCharacteristicSupport.describeUpperLimitExplanation(entity), -1.0, entity);
    descriptionLabel.setWrapText(true);
    descriptionLabel.setMaxWidth(Double.MAX_VALUE);
    HBox.setHgrow(descriptionLabel, Priority.ALWAYS);
    Label orderableLabel = createRowLabel(Boolean.TRUE.equals(entity.getOrdered()) ? "Yes" : "No", 90.0, entity);

    HBox row = new HBox(10.0, roleLabel, documentModelLabel, upperLimitLabel, descriptionLabel, orderableLabel, createActionsBox(entity));
    row.setAlignment(Pos.CENTER_LEFT);
    row.getStyleClass().add("module-row");
    return row;
  }

  private Label createRowLabel(String text, double width, EntityCharacteristic entity) {
    Label label = new Label(text);
    if (width > 0) {
      label.setPrefWidth(width);
    }
    label.setCursor(Cursor.HAND);
    label.setOnMouseClicked(event -> {
      if (event.getClickCount() == 1) {
        openEditDialog(entity);
      }
    });
    return label;
  }

  private void openEditDialog(EntityCharacteristic entity) {
    boolean changed = Dialogs.showEntityForEdit(Studio.stage, documentModelOptions, entity);
    rebuildRows();
    if (changed) {
      notifyChanged();
    }
  }

  private HBox createActionsBox(EntityCharacteristic entity) {
    Button editButton = RowFactory.createActionButton(Icons.PENCIL, "Edit", () -> openEditDialog(entity));

    Button openModelButton = RowFactory.createActionButton(Icons.OPEN_IN_NEW, "Open Model", () -> openModel(entity));
    openModelButton.setDisable(entity.getDocumentModel() == null || entity.getDocumentModel().isBlank());

    Button deleteButton = RowFactory.createActionButton(Icons.TRASH, StudioBundle.get("delete"), () -> {
      Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, StudioBundle.get("delete_this_entity"), null, null, StudioBundle.get("delete"));
      if (result.isPresent() && result.get() == ButtonType.OK) {
        getEntities().remove(entity);
        rebuildRows();
        notifyChanged();
      }
    });

    HBox actionsBox = new HBox(4.0, editButton, openModelButton, deleteButton);
    actionsBox.setAlignment(Pos.CENTER_LEFT);
    return actionsBox;
  }

  /**
   * Opens the Document Model referenced by {@code entity}'s {@code documentModel} in an editor tab,
   * selecting its tab instead if it's already open, mirroring {@link
   * de.a12.studio.ui.editors.mappingmodel.SourceModelsPanelController#openModel}.
   */
  private void openModel(EntityCharacteristic entity) {
    ProjectDocumentModels.openModelInEditor(entity.getDocumentModel());
  }

  private void notifyChanged() {
    onChange.run();
    commitHeaderChange();
  }

  /**
   * Not bound to an {@link de.a12.studio.models.documentmodel.Element}, so the base class's element-keyed
   * validation plumbing never runs for this panel; queries {@link RelationshipEntityCountValidator}'s
   * element id directly instead. Called from {@link #rebuildRows} (itself called by every mutation here,
   * plus {@link #setModel}), so this always reflects the entity list as currently shown.
   */
  private void refreshEntityCountError() {
    List<ModelValidationError> errors =
        Studio.getValidationService().validateElement(model, RelationshipEntityCountValidator.ELEMENT_ID);
    if (errors.isEmpty()) {
      hideError();
    } else {
      showError(errors.get(0).severity(), errors.get(0).message());
    }
  }
}
