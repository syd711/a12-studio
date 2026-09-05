package de.a12.studio.ui.editors.overviewmodel;

import de.a12.studio.models.ModelReference;
import de.a12.studio.models.ModelType;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.overviewmodel.OverviewModel;
import de.a12.studio.models.querymodel.QueryModel;
import de.a12.studio.ui.components.ErrorContainerController;
import de.a12.studio.ui.util.ProjectDocumentModels;
import de.a12.studio.ui.util.StudioBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import org.jspecify.annotations.NonNull;

import java.net.URL;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Edits an {@link OverviewModel}'s "Overview Reference": the choice between backing the Overview Model
 * directly by a Document Model, or indirectly by a Query Model (whose own {@code targetDocumentModel} is
 * then kept in sync as the header's {@link ModelReference#PURPOSE_DOCUMENT_MODEL_FOR_OVERVIEW} reference,
 * since every "element reference" picker elsewhere in {@link de.a12.studio.ui.editors.overviewmodel.OverviewModelEditorController}
 * still resolves fields through that Document Model regardless of which mode is active). Isn't wired
 * through {@link de.a12.studio.ui.editors.AbstractPropertyEditor} for the same reason as {@link
 * de.a12.studio.ui.editors.applicationmodel.MatchConditionsPanelController}: it edits header {@link ModelReference}s directly, not a document-model
 * {@link de.a12.studio.models.documentmodel.Element}, and its owning editor already has its own
 * updatingFromModel/commitChange save cycle to fold this into.
 */
public class OverviewReferencePanelController implements Initializable {

  @FXML
  private RadioButton documentModelReferenceField;
  @FXML
  private RadioButton queryModelReferenceField;
  @FXML
  private ComboBox<String> overviewReferenceField;
  @FXML
  private Button editReferenceButton;
  @FXML
  private Label queryModelReferenceInfoLabel;

  @FXML
  private ErrorContainerController errorContainerController;

  private OverviewModel model;
  private List<DocumentModel> documentModels = List.of();
  private List<QueryModel> queryModels = List.of();

  // Set while fields are being repopulated from the model, so that programmatic updates aren't mistaken
  // for user edits and don't trigger setOnChange's callback.
  private boolean updatingFromModel;

  private Runnable onChange = () -> {
  };

  @Override
  public void initialize(URL url, ResourceBundle resources) {
    documentModelReferenceField.selectedProperty().addListener((observable, oldValue, isSelected) -> {
      if (isSelected) {
        onModeChanged();
      }
    });
    queryModelReferenceField.selectedProperty().addListener((observable, oldValue, isSelected) -> {
      if (isSelected) {
        onModeChanged();
      }
    });
    overviewReferenceField.valueProperty().addListener((observable, oldValue, newValue) -> {
      validate();
      if (updatingFromModel || model == null) {
        return;
      }
      syncModelReferences();
      onChange.run();
    });
    editReferenceButton.disableProperty().bind(overviewReferenceField.valueProperty().isNull());
  }

  /**
   * Opens whichever model is currently selected in the combo box - a Query Model or a Document Model,
   * depending on the active mode - in an editor tab, selecting its tab instead if it's already open (see
   * {@code TabPaneController#modelOpened}). Only enabled once a selection exists, via {@link
   * #editReferenceButton}'s disable binding.
   */
  @FXML
  private void onEditReference() {
    String selectedId = overviewReferenceField.getValue();
    if (selectedId == null) {
      return;
    }

    ProjectDocumentModels.openModelInEditor(selectedId);
  }

  /**
   * Invoked after every user-driven mode switch or reference selection (not while {@link #load} is
   * repopulating the fields), so the owning editor can rebuild its element-reference index and every
   * dependent picker, then save.
   */
  public void setOnChange(@NonNull Runnable onChange) {
    this.onChange = onChange;
  }

  public void load(@NonNull OverviewModel model, @NonNull List<DocumentModel> documentModels, @NonNull List<QueryModel> queryModels) {
    this.model = model;
    this.documentModels = documentModels;
    this.queryModels = queryModels;

    updatingFromModel = true;
    try {
      String queryModelId = currentReferenceId(ModelReference.PURPOSE_QUERY_MODEL_FOR_OVERVIEW);
      boolean queryMode = queryModelId != null && !queryModelId.isBlank();
      documentModelReferenceField.setSelected(!queryMode);
      queryModelReferenceField.setSelected(queryMode);
      updateInfoLabelVisibility(queryMode);
      populateCombo(queryMode, queryMode ? queryModelId : currentReferenceId(ModelReference.PURPOSE_DOCUMENT_MODEL_FOR_OVERVIEW));
    }
    finally {
      updatingFromModel = false;
    }
    validate();
  }

  /** Toggling the radio group switches the combobox between Query Models and Document Models. */
  private void onModeChanged() {
    boolean queryMode = queryModelReferenceField.isSelected();
    updateInfoLabelVisibility(queryMode);

    if (updatingFromModel || model == null) {
      return;
    }
    boolean wasUpdating = updatingFromModel;
    updatingFromModel = true;
    try {
      populateCombo(queryMode, null);
    }
    finally {
      updatingFromModel = wasUpdating;
    }
    validate();
    syncModelReferences();
    onChange.run();
  }

  private void populateCombo(boolean queryMode, String valueToSelect) {
    List<String> ids = queryMode
        ? queryModels.stream().map(QueryModel::getId).sorted(Comparator.naturalOrder()).toList()
        : documentModels.stream().map(DocumentModel::getId).sorted(Comparator.naturalOrder()).toList();
    overviewReferenceField.getItems().setAll(ids);
    overviewReferenceField.setValue(valueToSelect);
  }

  private void updateInfoLabelVisibility(boolean queryMode) {
    queryModelReferenceInfoLabel.setVisible(queryMode);
    queryModelReferenceInfoLabel.setManaged(queryMode);
  }

  private String currentReferenceId(String purpose) {
    if (model.getModelReferences() == null) {
      return null;
    }
    return model.getModelReferences().stream()
        .filter(reference -> purpose.equals(reference.getPurpose()))
        .map(ModelReference::getReference)
        .findFirst()
        .orElse(null);
  }

  /**
   * Rebuilds the header's Overview Reference. Document Model mode writes a single {@code
   * document-model-for-overview} reference; Query Model mode writes a {@code query-model-for-overview}
   * reference plus a {@code document-model-for-overview} reference auto-resolved from the selected Query
   * Model's own {@code targetDocumentModel}, so every element-reference picker elsewhere keeps working off
   * a Document Model without having to know which mode is active.
   */
  private void syncModelReferences() {
    List<ModelReference> references = model.getModelReferences();
    references.removeIf(reference -> ModelReference.PURPOSE_DOCUMENT_MODEL_FOR_OVERVIEW.equals(reference.getPurpose())
        || ModelReference.PURPOSE_QUERY_MODEL_FOR_OVERVIEW.equals(reference.getPurpose()));

    String selectedId = overviewReferenceField.getValue();
    if (selectedId == null || selectedId.isBlank()) {
      return;
    }

    if (queryModelReferenceField.isSelected()) {
      references.add(newReference(ModelReference.PURPOSE_QUERY_MODEL_FOR_OVERVIEW, ModelType.QUERY, "QM", selectedId));
      String targetDocumentModelId = queryModels.stream()
          .filter(queryModel -> selectedId.equals(queryModel.getId()))
          .findFirst()
          .map(queryModel -> queryModel.getContent().getTargetDocumentModel())
          .orElse(null);
      if (targetDocumentModelId != null && !targetDocumentModelId.isBlank()) {
        references.add(newReference(ModelReference.PURPOSE_DOCUMENT_MODEL_FOR_OVERVIEW, ModelType.DOCUMENT, "DM", targetDocumentModelId));
      }
    }
    else {
      references.add(newReference(ModelReference.PURPOSE_DOCUMENT_MODEL_FOR_OVERVIEW, ModelType.DOCUMENT, "DM", selectedId));
    }
  }

  private static ModelReference newReference(String purpose, ModelType modelType, String alias, String referenceId) {
    ModelReference reference = new ModelReference();
    reference.setPurpose(purpose);
    reference.setModelType(modelType);
    reference.setAlias(alias);
    reference.setReference(referenceId);
    return reference;
  }

  /** The combo box reference is required regardless of which radio button is selected. */
  private void validate() {
    if (overviewReferenceField.getValue() == null) {
      String missing = queryModelReferenceField.isSelected()
          ? StudioBundle.get("overview_reference_panel.query_model_option")
          : StudioBundle.get("overview_reference_panel.document_model_option");
      errorContainerController.show("ERROR", StudioBundle.get("overview_reference_panel.selection_required", missing));
    }
    else {
      errorContainerController.hide();
    }
  }
}
