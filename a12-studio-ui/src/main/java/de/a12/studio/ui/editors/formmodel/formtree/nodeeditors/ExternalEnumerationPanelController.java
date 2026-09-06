package de.a12.studio.ui.editors.formmodel.formtree.nodeeditors;

import de.a12.studio.models.formmodel.ExternalEnumeration;
import de.a12.studio.models.formmodel.FieldConfigEntry;
import de.a12.studio.ui.editors.AbstractPropertyEditor;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import org.jspecify.annotations.NonNull;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * "External Enumeration" property editor for a {@link FieldConfigEntry}: sources the bound field's
 * enumeration options from an external URL instead of the Document Model's own enum definition, mirroring
 * SME's {@code externalEnumeration} field configuration section. Reused both from the Control node editor
 * ({@link FormNodeEditorControlPanelController}, entry resolved via {@link FieldConfigEntryHelper}) and from
 * the model-wide Data Configuration tab ({@code DataConfigurationPanelController}), which lets a field be
 * configured even when no Control in the tree currently binds to it yet.
 */
public class ExternalEnumerationPanelController extends AbstractPropertyEditor implements Initializable {

  @FXML
  private TextField srcField;
  @FXML
  private CheckBox customValuesAllowedCheckBox;
  @FXML
  private CheckBox caseSensitiveCheckBox;

  private FieldConfigEntry entry;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    super.initialize(location, resources);

    bindTextField(srcField, (el, value) -> getOrCreate().setSrc(value == null || value.isBlank() ? null : value));
    bindCheckBox(customValuesAllowedCheckBox, (el, value) -> getOrCreate().setCustomValuesAllowed(value ? Boolean.TRUE : null));
    bindCheckBox(caseSensitiveCheckBox, (el, value) -> getOrCreate().setCaseSensitive(value ? Boolean.TRUE : null));
  }

  public void setEntry(@NonNull FieldConfigEntry entry) {
    this.entry = entry;

    ExternalEnumeration externalEnumeration = entry.getExternalEnumeration();
    setFieldValue(srcField, externalEnumeration == null ? null : externalEnumeration.getSrc());
    setFieldValue(customValuesAllowedCheckBox,
        externalEnumeration != null && Boolean.TRUE.equals(externalEnumeration.getCustomValuesAllowed()));
    setFieldValue(caseSensitiveCheckBox,
        externalEnumeration != null && Boolean.TRUE.equals(externalEnumeration.getCaseSensitive()));
  }

  private ExternalEnumeration getOrCreate() {
    if (entry.getExternalEnumeration() == null) {
      entry.setExternalEnumeration(new ExternalEnumeration());
    }
    return entry.getExternalEnumeration();
  }
}
