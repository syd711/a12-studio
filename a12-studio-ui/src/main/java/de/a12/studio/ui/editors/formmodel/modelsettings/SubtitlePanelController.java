package de.a12.studio.ui.editors.formmodel.modelsettings;

import de.a12.studio.models.formmodel.FormModel;
import de.a12.studio.models.formmodel.FormModelContent;
import de.a12.studio.ui.editors.AbstractPropertyEditor;
import de.a12.studio.ui.editors.PropertyEditorSaveMode;
import de.a12.studio.ui.editors.propertyeditors.LocalizedTextTypePanelController;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import org.jspecify.annotations.NonNull;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Edits {@link FormModelContent#getSubtitle()} (SME's {@code FormModelFrame-form.json} {@code section-093cb},
 * "Subtitle"): a per-locale text or expression, delegated entirely to the nested {@link
 * LocalizedTextTypePanelController}.
 */
public class SubtitlePanelController extends AbstractPropertyEditor implements Initializable {

  @FXML
  private LocalizedTextTypePanelController subtitleTextController;

  private FormModel model;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    super.initialize(location, resources);

    subtitleTextController.configureCustom("subtitle", "");
  }

  @Override
  public void setSaveMode(@NonNull PropertyEditorSaveMode saveMode) {
    super.setSaveMode(saveMode);
    subtitleTextController.setSaveMode(saveMode);
  }

  /** Hides this panel entirely for model types other than {@link FormModel}. */
  public void setVisible(boolean visible) {
    setEditorVisible(visible);
  }

  public void setModel(@NonNull FormModel model) {
    this.model = model;
    subtitleTextController.setCustom(() -> getContent().getSubtitle(), value -> getContent().setSubtitle(value));
  }

  private FormModelContent getContent() {
    FormModelContent content = model.getContent();
    if (content == null) {
      content = new FormModelContent();
      model.setContent(content);
    }
    return content;
  }
}
