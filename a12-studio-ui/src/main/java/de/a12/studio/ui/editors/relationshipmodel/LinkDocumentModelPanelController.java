package de.a12.studio.ui.editors.relationshipmodel;

import de.a12.studio.models.relationshipmodel.RelationshipModel;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.validators.relationship.RelationshipLinkDocumentModelValidator;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.editors.AbstractPropertyEditor;
import de.a12.studio.ui.util.WidgetFactory;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import org.jspecify.annotations.NonNull;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * Edits a {@link RelationshipModel}'s optional Link Document Model and its "Duplicates Allowed" flag. Not
 * bound to a single {@link de.a12.studio.models.documentmodel.Element}, so it follows the model-header pattern
 * used by e.g. {@link RelatedEntitiesPanelController}. Both fields are only meaningful for n:n relationships;
 * {@link RelationshipLinkDocumentModelValidator} warns otherwise, surfaced in this panel's own error container
 * after every change (see {@link #refreshError}), mirroring {@link
 * RelatedEntitiesPanelController#refreshEntityCountError()}.
 */
public class LinkDocumentModelPanelController extends AbstractPropertyEditor implements Initializable {

  @FXML
  private ComboBox<String> linkDocumentModelField;

  @FXML
  private CheckBox duplicatesAllowedField;

  @FXML
  private Label duplicatesAllowedInfoIcon;

  private RelationshipModel model;

  // Set while fields are being repopulated from the model, so that programmatic updates aren't mistaken
  // for user edits and don't trigger a save.
  private boolean updatingFromModel;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    super.initialize(location, resources);
    WidgetFactory.createHelpIcon(duplicatesAllowedInfoIcon, "Multiple links between the same two documents are allowed.");

    linkDocumentModelField.valueProperty().addListener((observable, oldValue, newValue) -> {
      if (updatingFromModel || model == null) {
        return;
      }
      model.getContent().setLinkDocumentModelValue(newValue == null || newValue.isBlank() ? null : newValue);
      notifyChanged();
    });

    duplicatesAllowedField.selectedProperty().addListener((observable, oldValue, newValue) -> {
      if (updatingFromModel || model == null) {
        return;
      }
      model.getContent().setDuplicatesAllowed(newValue);
      notifyChanged();
    });
  }

  public void setModel(@NonNull RelationshipModel model, @NonNull List<String> documentModelOptions) {
    this.model = model;

    updatingFromModel = true;
    try {
      List<String> options = new ArrayList<>();
      // Empty entry so the optional link document model can be cleared again.
      options.add("");
      options.addAll(documentModelOptions);
      linkDocumentModelField.getItems().setAll(options);
      linkDocumentModelField.setValue(model.getContent().getLinkDocumentModelValue());

      duplicatesAllowedField.setSelected(Boolean.TRUE.equals(model.getContent().getDuplicatesAllowed()));
    }
    finally {
      updatingFromModel = false;
    }
    refreshError();
  }

  private void notifyChanged() {
    commitHeaderChange();
    refreshError();
  }

  /**
   * Not bound to an {@link de.a12.studio.models.documentmodel.Element}, so the base class's element-keyed
   * validation plumbing never runs for this panel; queries {@link RelationshipLinkDocumentModelValidator}'s
   * element id directly instead. Both of that validator's warnings (link document model and duplicates
   * allowed) can be present at once, so they're joined into a single message.
   */
  private void refreshError() {
    List<ModelValidationError> errors =
        Studio.getValidationService().validateElement(model, RelationshipLinkDocumentModelValidator.ELEMENT_ID);
    if (errors.isEmpty()) {
      hideError();
    } else {
      String message = errors.stream().map(ModelValidationError::message).collect(Collectors.joining("\n"));
      showError(errors.get(0).severity(), message);
    }
  }
}
