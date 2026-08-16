package de.a12.studio.ui.editors.propertyeditors;

import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.ui.components.ErrorContainerController;
import de.a12.studio.ui.util.ProjectDocumentModels;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import org.jspecify.annotations.NonNull;

import java.net.URL;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Edits a single Document Model reference via a plain combo box (e.g. a {@link
 * de.a12.studio.models.mappingmodel.MappingModel}'s {@code content.Target.dmId}, or a {@link
 * de.a12.studio.models.querymodel.QueryModel}'s {@code content.targetDocumentModel}). Isn't wired through
 * {@link de.a12.studio.ui.editors.AbstractPropertyEditor} for the same reason as {@link
 * de.a12.studio.ui.editors.maindetailmodel.MainModelReferencePanelController}: it edits a content field and a header {@link
 * de.a12.studio.models.ModelReference} directly, not a document-model {@link
 * de.a12.studio.models.documentmodel.Element}, and its owning editor already has its own
 * updatingFromModel/commitChange save cycle to fold this into. Lives in the shared {@code propertyeditors}
 * package (per this repo's "extract property editors" convention) since it now has two consumers.
 */
public class TargetModelPanelController implements Initializable {

  @FXML
  private ComboBox<String> targetModelField;

  @FXML
  private Button editReferenceButton;

  @FXML
  private ErrorContainerController errorContainerController;

  // Set while fields are being repopulated from the model, so that programmatic updates aren't mistaken
  // for user edits and don't trigger setOnChange's callback.
  private boolean updatingFromModel;

  private Runnable onChange = () -> {
  };

  @Override
  public void initialize(URL url, ResourceBundle resources) {
    targetModelField.valueProperty().addListener((observable, oldValue, newValue) -> {
      validate();
      if (updatingFromModel) {
        return;
      }
      onChange.run();
    });
    editReferenceButton.disableProperty().bind(targetModelField.valueProperty().isNull());
  }

  /**
   * Opens the Document Model referenced by the combo box in an editor tab, selecting its tab instead if it's
   * already open (see {@code TabPaneController#modelOpened}).
   */
  @FXML
  private void onEditReference(ActionEvent event) {
    String reference = targetModelField.getValue();
    if (reference == null) {
      return;
    }

    ProjectDocumentModels.openModelInEditor(reference);
  }

  /**
   * Invoked after every user-driven selection change (not while {@link #load} is repopulating the field),
   * so the owning editor can sync its header reference and save.
   */
  public void setOnChange(@NonNull Runnable onChange) {
    this.onChange = onChange;
  }

  public void load(@NonNull List<DocumentModel> documentModels, String selectedId) {
    updatingFromModel = true;
    try {
      targetModelField.getItems().setAll(documentModels.stream()
          .map(DocumentModel::getId)
          .sorted(Comparator.naturalOrder())
          .toList());
      targetModelField.setValue(selectedId);
    }
    finally {
      updatingFromModel = false;
    }
    validate();
  }

  public String getValue() {
    return targetModelField.getValue();
  }

  private void validate() {
    if (targetModelField.getValue() == null) {
      errorContainerController.show("ERROR", "A Target Model must be selected.");
    }
    else {
      errorContainerController.hide();
    }
  }
}
