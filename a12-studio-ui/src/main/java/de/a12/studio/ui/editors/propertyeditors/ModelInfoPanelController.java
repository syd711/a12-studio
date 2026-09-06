package de.a12.studio.ui.editors.propertyeditors;

import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.documentmodel.DocumentModelContent;
import de.a12.studio.models.documentmodel.ModelInfo;
import de.a12.studio.ui.editors.AbstractPropertyEditor;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextArea;
import org.jspecify.annotations.NonNull;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.function.Consumer;

/**
 * Edits {@link ModelInfo#getImmutable()} and {@link ModelInfo#getComment()}. Not bound to a single {@link
 * de.a12.studio.models.documentmodel.Element} (both live on the model's own {@link ModelInfo}), same as {@link
 * TimezonePanelController}/{@link ModelConfigPanelController} - {@code setElement} is never called, only
 * {@link #setModel}.
 *
 * <p>Deliberately does NOT expose {@link ModelInfo#getName()}: every fixture in this repo has it exactly equal
 * to the model's own {@code header.id} (e.g. {@code Company_DM.json}'s {@code modelInfo.name} is literally
 * {@code "Company_DM"}), confirming it's meant to mirror the id, not be independently authored - letting a user
 * edit it here would just let it drift out of sync. {@code ProjectItem.renameTo}/{@code createCopy} (and
 * {@code NewModelFactory}) now keep it in sync automatically instead, the same way they already do for
 * {@code header.id} itself.
 */
public class ModelInfoPanelController extends AbstractPropertyEditor implements Initializable {

  @FXML
  private CheckBox immutableCheckBox;

  @FXML
  private TextArea commentField;

  private DocumentModel model;

  // Set while setModel() is repopulating the fields from the model, so the listeners below don't mistake that
  // programmatic change for a user edit - same guard as TimezonePanelController/ModelConfigPanelController.
  private boolean updatingFromModel;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    super.initialize(location, resources);

    immutableCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
      if (!updatingFromModel) {
        withModelInfo(info -> info.setImmutable(newValue ? true : null));
      }
    });
    commentField.textProperty().addListener((observable, oldValue, newValue) -> {
      if (!updatingFromModel) {
        withModelInfo(info -> info.setComment(newValue == null || newValue.isBlank() ? null : newValue));
      }
    });
  }

  /** Hides this panel entirely for model types that have no {@link ModelInfo} concept (only a
   * {@link DocumentModel} is ever passed to {@link #setModel}). */
  public void setVisible(boolean visible) {
    setEditorVisible(visible);
  }

  public void setModel(@NonNull DocumentModel model) {
    this.model = model;
    ModelInfo modelInfo = getModelInfo(model);
    updatingFromModel = true;
    try {
      immutableCheckBox.setSelected(modelInfo != null && Boolean.TRUE.equals(modelInfo.getImmutable()));
      commentField.setText(modelInfo != null && modelInfo.getComment() != null ? modelInfo.getComment() : "");
    } finally {
      updatingFromModel = false;
    }
  }

  private void withModelInfo(Consumer<ModelInfo> mutator) {
    if (model == null) {
      return;
    }
    ModelInfo modelInfo = getModelInfo(model);
    if (modelInfo == null) {
      return;
    }
    mutator.accept(modelInfo);
    commitChange();
  }

  private static ModelInfo getModelInfo(DocumentModel model) {
    DocumentModelContent content = model.getContent();
    return content != null ? content.getModelInfo() : null;
  }
}
