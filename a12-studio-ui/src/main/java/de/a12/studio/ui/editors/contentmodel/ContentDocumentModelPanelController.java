package de.a12.studio.ui.editors.contentmodel;

import de.a12.studio.models.contentmodel.ContentModel;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.ui.editors.AbstractPropertyEditor;
import de.a12.studio.ui.util.ProjectDocumentModels;
import de.a12.studio.ui.util.StudioBundle;
import de.a12.studio.ui.util.WidgetFactory;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.net.URL;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Consumer;

/**
 * Edits the Document Model a {@link ContentModel} is bound to (SME's Content Model setting "Document Model", its
 * {@code technicalFields.documentModel}). The binding is not a content field but the header's reference to a Document
 * Model, which {@link ContentModel#setDocumentModelId} maintains in the shape SME writes; this panel is why the generic
 * Model References panel is not shown for Content Models (see {@code ModelSettingsDialog}). Optional: the model
 * can be cleared again. Not bound to a single {@link de.a12.studio.models.documentmodel.Element}, so it follows the
 * model-header pattern (only {@link #setModel}, {@link #commitHeaderChange()}).
 */
public class ContentDocumentModelPanelController extends AbstractPropertyEditor implements Initializable {

  @FXML
  private ComboBox<String> documentModelField;

  @FXML
  private Button openDocumentModelButton;

  @FXML
  private Button clearDocumentModelButton;

  @FXML
  private Label infoIcon;

  private ContentModel model;

  // Set while the combo box is being repopulated from the model, so that is not mistaken for a user edit.
  private boolean updatingFromModel;

  private Consumer<String> onChange = documentModelId -> {
  };

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    super.initialize(location, resources);

    WidgetFactory.createHelpIcon(infoIcon, StudioBundle.get("content_document_model_panel.info"));
    openDocumentModelButton.disableProperty().bind(documentModelField.valueProperty().isNull());
    clearDocumentModelButton.disableProperty().bind(documentModelField.valueProperty().isNull());
    documentModelField.valueProperty().addListener((observable, oldValue, documentModelId) -> {
      if (updatingFromModel || model == null) {
        return;
      }
      model.setDocumentModelId(documentModelId);
      validate();
      commitHeaderChange();
      onChange.accept(documentModelId);
    });
  }

  @FXML
  private void onOpenDocumentModel() {
    String documentModelId = documentModelField.getValue();
    if (documentModelId != null) {
      ProjectDocumentModels.openModelInEditor(documentModelId);
    }
  }

  @FXML
  private void onClearDocumentModel() {
    documentModelField.setValue(null);
  }

  /** Hides this panel entirely, for the model types that have no Document Model setting of this kind. */
  public void setVisible(boolean visible) {
    setEditorVisible(visible);
  }

  /**
   * Called after every user-driven change of the binding (not while {@link #setModel} repopulates the panel) with the
   * new Document Model id, or {@code null} once it was removed, so the panels that depend on it can follow.
   */
  public void setOnChange(@NonNull Consumer<@Nullable String> onChange) {
    this.onChange = onChange;
  }

  /**
   * @param documentModels what the model can be bound to: the Document Models of the project and the Document Models
   *                       that stand in for its Combination Models (see {@link
   *                       ProjectDocumentModels#getOtherDocumentModelsWithCombinations})
   */
  public void setModel(@NonNull ContentModel model, @NonNull List<DocumentModel> documentModels) {
    this.model = model;
    updatingFromModel = true;
    try {
      documentModelField.getItems().setAll(documentModels.stream()
          .map(DocumentModel::getId)
          .sorted(Comparator.naturalOrder())
          .toList());
      documentModelField.setValue(model.getDocumentModelId());
    }
    finally {
      updatingFromModel = false;
    }
    validate();
  }

  /** The combo box shows a stored id it has no item for as if it were a valid choice, so say that it is not. */
  private void validate() {
    String documentModelId = documentModelField.getValue();
    if (documentModelId != null && !documentModelField.getItems().contains(documentModelId)) {
      showError("ERROR", StudioBundle.get("content_document_model_panel.not_found", documentModelId));
    }
    else {
      hideError();
    }
  }
}
