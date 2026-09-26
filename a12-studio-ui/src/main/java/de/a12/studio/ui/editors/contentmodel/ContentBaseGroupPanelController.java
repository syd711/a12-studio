package de.a12.studio.ui.editors.contentmodel;

import de.a12.studio.models.contentmodel.ContentConfiguration;
import de.a12.studio.models.contentmodel.ContentModel;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.documentmodel.Element;
import de.a12.studio.models.documentmodel.GroupElement;
import de.a12.studio.modelsvalidation.validators.ElementIndex;
import de.a12.studio.ui.editors.AbstractPropertyEditor;
import de.a12.studio.ui.util.StudioBundle;
import de.a12.studio.ui.util.WidgetFactory;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.util.StringConverter;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.net.URL;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Edits {@link ContentConfiguration#getBaseGroupId()}, SME's Content Model setting "Base Group": the group of the bound
 * Document Model the whole Content Model is relative to. It offers every group of the Document Model (SME's {@code
 * getCandidateBaseGroups}: all Group elements at any depth, shown by their path, stored by their id) and only exists
 * while the model is bound to a Document Model - SME hides it otherwise and its export drops the value, as {@link
 * ContentModel#setDocumentModelId} does. A stored group the Document Model does not have (any more) stays selected and
 * is reported, like SME's "Invalid Reference" rule. Follows the model-header pattern.
 */
public class ContentBaseGroupPanelController extends AbstractPropertyEditor implements Initializable {

  @FXML
  private ComboBox<String> baseGroupField;

  @FXML
  private Button clearBaseGroupButton;

  @FXML
  private Label infoIcon;

  private ContentModel model;

  private DocumentModel documentModel;

  private ElementIndex index;

  // Set while the combo box is being repopulated from the model, so that is not mistaken for a user edit.
  private boolean updatingFromModel;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    super.initialize(location, resources);

    WidgetFactory.createHelpIcon(infoIcon, StudioBundle.get("content_base_group_panel.info"));
    baseGroupField.setConverter(new StringConverter<>() {
      @Override
      public String toString(String groupId) {
        return groupId == null ? "" : (index != null ? index.resolveDisplayPath(groupId) : groupId);
      }

      @Override
      public String fromString(String string) {
        return string;
      }
    });
    clearBaseGroupButton.disableProperty().bind(baseGroupField.valueProperty().isNull());
    baseGroupField.valueProperty().addListener((observable, oldValue, groupId) -> {
      if (updatingFromModel || model == null) {
        return;
      }
      if (model.getContent().getConfiguration() == null) {
        model.getContent().setConfiguration(new ContentConfiguration());
      }
      model.getContent().getConfiguration().setBaseGroupId(groupId);
      validate();
      commitHeaderChange();
    });
  }

  @FXML
  private void onClearBaseGroup() {
    baseGroupField.setValue(null);
  }

  /** Hides this panel entirely, for the model types that have no Base Group. */
  public void setVisible(boolean visible) {
    setEditorVisible(visible);
  }

  /**
   * @param documentModel the Document Model {@code model} is bound to, or {@code null} if it is not bound or the model
   *                      is not in the project; the panel is only shown while the model is bound at all
   */
  public void setModel(@NonNull ContentModel model, @Nullable DocumentModel documentModel) {
    this.model = model;
    setDocumentModel(documentModel);
  }

  /**
   * Follows a change of the bound Document Model: rebuilds the choice of groups and shows or hides the panel. The
   * stored base group is kept, see the class comment.
   */
  public void setDocumentModel(@Nullable DocumentModel documentModel) {
    this.documentModel = documentModel;
    index = documentModel != null && documentModel.getContent() != null && documentModel.getContent().getModelRoot() != null
        ? new ElementIndex(documentModel)
        : null;

    updatingFromModel = true;
    try {
      baseGroupField.getItems().setAll(index == null ? List.of() : index.allElements().stream()
          .filter(GroupElement.class::isInstance)
          .filter(group -> group.getId() != null)
          .sorted(Comparator.comparing(index::getPath))
          .map(Element::getId)
          .toList());
      ContentConfiguration configuration = model.getContent().getConfiguration();
      baseGroupField.setValue(configuration != null ? configuration.getBaseGroupId() : null);
    }
    finally {
      updatingFromModel = false;
    }
    setEditorVisible(model.getDocumentModelId() != null);
    validate();
  }

  /**
   * The combo box shows a stored id it has no item for as if it were a valid choice, so say that it is not. Nothing to
   * check against while the Document Model itself is missing; that is reported by its own panel.
   */
  private void validate() {
    String groupId = baseGroupField.getValue();
    if (groupId != null && documentModel != null && !baseGroupField.getItems().contains(groupId)) {
      showError("ERROR", StudioBundle.get("content_base_group_panel.not_found", groupId, documentModel.getId()));
    }
    else {
      hideError();
    }
  }
}
