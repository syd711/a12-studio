package de.a12.studio.ui.editors.relationshipmodel.dialogs;

import de.a12.studio.models.relationshipmodel.EntityCharacteristic;
import de.a12.studio.models.relationshipmodel.Multiplicity;
import de.a12.studio.ui.components.DialogController;
import de.a12.studio.ui.editors.PropertyEditorSaveMode;
import de.a12.studio.ui.editors.propertyeditors.LocalizedTextPanelController;
import de.a12.studio.ui.editors.relationshipmodel.EntityCharacteristicSupport;
import de.a12.studio.ui.editors.relationshipmodel.EntityCharacteristicsPanelController;
import de.a12.studio.ui.editors.relationshipmodel.LinkConstraintsPanelController;
import de.a12.studio.ui.util.StudioBundle;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.Optional;

/**
 * Add/edit dialog for a single {@link EntityCharacteristic}, opened from {@link
 * de.a12.studio.ui.editors.relationshipmodel.RelatedEntitiesPanelController} by clicking an entity row or its
 * Edit button, or by the Add Entity button. Unlike a standalone top-level dialog (e.g. {@code
 * OverviewColumnDialogController}), {@link #onDialogSubmit} doesn't itself persist anything: the {@link
 * EntityCharacteristic} is mutated live by the three embedded panels (Entity Characteristics, Link Constraints,
 * Labels) and the owning panel's own commit does the actual save, in one go, once this dialog is confirmed.
 * Mirrors {@code de.a12.studio.ui.editors.overviewmodel.dialogs.FilterItemDialogController}.
 */
public class EntityCharacteristicDialogController implements DialogController {

  @FXML
  private EntityCharacteristicsPanelController entityCharacteristicsController;

  @FXML
  private LinkConstraintsPanelController linkConstraintsController;

  @FXML
  private LocalizedTextPanelController labelsController;

  @FXML
  private Button okButton;

  // Shared by the three embedded panels so their commits aren't persisted while this dialog is open: the owning
  // RelatedEntitiesPanelController persists everything itself, in one go, once the entity is added/edited.
  private final PropertyEditorSaveMode.Deferred saveMode = new PropertyEditorSaveMode.Deferred();

  private Stage stage;

  private EntityCharacteristic entity;

  private EntityCharacteristicSnapshot snapshot;

  private Optional<ButtonType> result = Optional.of(ButtonType.CANCEL);

  @FXML
  private void initialize() {
    entityCharacteristicsController.setSaveMode(saveMode);
    linkConstraintsController.setSaveMode(saveMode);
    labelsController.configureCustom("labels", StudioBundle.get("labels"));
    labelsController.setSaveMode(saveMode);

    entityCharacteristicsController.setOnChange(this::validate);
    linkConstraintsController.setOnChange(this::validate);
  }

  void init(Stage stage, @NonNull EntityCharacteristic entity, @NonNull List<String> documentModelOptions) {
    this.stage = stage;
    this.entity = entity;
    this.snapshot = new EntityCharacteristicSnapshot(entity);

    entityCharacteristicsController.setDocumentModelOptions(documentModelOptions);
    entityCharacteristicsController.setEntity(entity);
    linkConstraintsController.setEntity(entity);
    labelsController.setCustom(entity::getLabels);

    validate();
  }

  /** Unregisters the embedded Labels panel once this dialog is closed - see {@link Dialogs#showEntity}, which
   * calls this from the stage's {@code onHidden} handler. */
  void destroy() {
    labelsController.destroy();
  }

  @Override
  public void onDialogCancel() {
    snapshot.restore();
    stage.close();
  }

  @FXML
  private void onDialogSubmit() {
    result = Optional.of(ButtonType.OK);
    stage.close();
  }

  boolean isConfirmed() {
    return result.isPresent() && result.get() == ButtonType.OK;
  }

  private void validate() {
    boolean roleOk = entity.getRole() != null && !entity.getRole().isBlank();
    boolean documentModelOk = entity.getDocumentModel() != null && !entity.getDocumentModel().isBlank();
    Multiplicity multiplicity = EntityCharacteristicSupport.getMultiplicity(entity);
    boolean unbounded = multiplicity != null && Boolean.TRUE.equals(multiplicity.getUnbounded());
    boolean upperLimitOk = unbounded || (multiplicity != null && multiplicity.getUpperLimit() != null);
    okButton.setDisable(!roleOk || !documentModelOk || !upperLimitOk);
  }
}
