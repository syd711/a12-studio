package de.a12.studio.ui.editors.formmodel.formtree.nodeeditors;

import de.a12.studio.models.formmodel.FieldConfigEntry;
import de.a12.studio.models.formmodel.FieldConfigEntry.AttachmentConfig;
import de.a12.studio.ui.editors.AbstractPropertyEditor;
import de.a12.studio.ui.util.StudioBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.util.StringConverter;
import org.jspecify.annotations.NonNull;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.function.Consumer;

/**
 * "Attachment Settings" property editor (SME docs, "Attachment Settings"): the placeholder icon shown before a
 * file is uploaded, the default action of a click on an uploaded file, and the MIME types the file picker
 * suggests. They are stored in {@link FieldConfigEntry#getAttachmentConfig()} of the entry whose {@code
 * elementRef} is the attachment <em>group</em> (usage type {@code attachment}) and therefore apply to every
 * input of that attachment. Shown for a Control bound to such a group ({@link
 * FormNodeEditorControlPanelController}) and for the attachment group in the Data Configuration tab.
 * <p>
 * Absent means the default ("default" placeholder icon, "replace" action, no accept filter), so an untouched
 * panel writes nothing and an {@code attachmentConfig} that ends up with no setting at all is removed again.
 * Not tied to a document-model {@code Element}, so it follows the model-header pattern: a plain {@link
 * #setEntry} entry point and {@code bindComboBox}/{@code bindTextField}'s built-in commit.
 */
public class AttachmentSettingsPanelController extends AbstractPropertyEditor implements Initializable {

  // Wire value -> bundle key; null (absent) is the "default" entry of each combo.
  private static final String[] PLACEHOLDER_ICONS = {null, "image", "text", "spreadsheet", "pdf", "video", "sound", "none"};
  private static final String[] DEFAULT_ACTIONS = {null, "download"};

  @FXML
  private ComboBox<String> placeholderIconCombo;
  @FXML
  private ComboBox<String> defaultActionCombo;
  @FXML
  private TextField acceptField;

  private FieldConfigEntry entry;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    super.initialize(location, resources);

    placeholderIconCombo.getItems().setAll(PLACEHOLDER_ICONS);
    placeholderIconCombo.setConverter(converter("attachment_placeholder_icon."));
    defaultActionCombo.getItems().setAll(DEFAULT_ACTIONS);
    defaultActionCombo.setConverter(converter("attachment_default_action."));

    bindComboBox(placeholderIconCombo, (el, value) -> update(config -> config.setPlaceholderIcon(value)));
    bindComboBox(defaultActionCombo, (el, value) -> update(config -> config.setDefaultAction(value)));
    bindTextField(acceptField, (el, value) -> update(config -> config.setAccept(value == null || value.isBlank() ? null : value.strip())));
  }

  /** Shows or hides the whole panel, e.g. when the selected element isn't an attachment group. */
  public void setVisible(boolean visible) {
    setEditorVisible(visible);
  }

  public void setEntry(@NonNull FieldConfigEntry entry) {
    this.entry = entry;
    AttachmentConfig config = entry.getAttachmentConfig();
    // "default" and "replace" are what an absent value means; show them as the default entry.
    setFieldValue(placeholderIconCombo, config == null ? null : nullIfDefault(config.getPlaceholderIcon(), "default"));
    setFieldValue(defaultActionCombo, config == null ? null : nullIfDefault(config.getDefaultAction(), "replace"));
    setFieldValue(acceptField, config == null || config.getAccept() == null ? "" : config.getAccept());
  }

  private void update(Consumer<AttachmentConfig> change) {
    AttachmentConfig config = entry.getAttachmentConfig();
    if (config == null) {
      config = new AttachmentConfig();
      entry.setAttachmentConfig(config);
    }
    change.accept(config);
    if (config.isBlank()) {
      entry.setAttachmentConfig(null);
    }
  }

  private static String nullIfDefault(String value, String defaultValue) {
    return value == null || value.equals(defaultValue) ? null : value;
  }

  private static StringConverter<String> converter(String keyPrefix) {
    return new StringConverter<>() {
      @Override
      public String toString(String value) {
        return StudioBundle.get(keyPrefix + (value == null ? "default" : value));
      }

      @Override
      public String fromString(String displayName) {
        return null;
      }
    };
  }
}
