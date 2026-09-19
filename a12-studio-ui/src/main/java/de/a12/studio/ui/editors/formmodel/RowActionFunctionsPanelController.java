package de.a12.studio.ui.editors.formmodel;

import de.a12.studio.models.formmodel.RowAction;
import de.a12.studio.ui.editors.AbstractPropertyEditor;
import de.a12.studio.ui.util.StudioBundle;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.util.StringConverter;
import org.jspecify.annotations.NonNull;

import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * Edits a {@link RowAction}'s {@code event} and {@code scope} - SME's {@code I_SectionRowAction-form.json}
 * "Button Functions" section. Embedded in {@link
 * de.a12.studio.ui.editors.formmodel.dialogs.RowActionDialogController}. Not tied to a single {@code Element},
 * so it follows the model-header pattern (a plain {@link #setRowAction} entry point, {@code bindTextField}'s
 * built-in commit) rather than {@link #setElement}.
 */
public class RowActionFunctionsPanelController extends AbstractPropertyEditor implements Initializable {

  /** Wire value of {@link RowAction#getScope()} to the bundle key of its display name, in display order. */
  private static final Map<String, String> SCOPE_KEYS = new LinkedHashMap<>();

  static {
    SCOPE_KEYS.put("ALWAYS", "row_action_scope.always");
    SCOPE_KEYS.put("DISABLED_IN_EDIT_MODE", "row_action_scope.disabled_in_edit_mode");
    SCOPE_KEYS.put("DISABLED_IN_READONLY_MODE", "row_action_scope.disabled_in_readonly_mode");
    SCOPE_KEYS.put("HIDDEN_IN_EDIT_MODE", "row_action_scope.hidden_in_edit_mode");
    SCOPE_KEYS.put("HIDDEN_IN_READONLY_MODE", "row_action_scope.hidden_in_readonly_mode");
  }

  @FXML
  private TextField eventField;

  @FXML
  private ComboBox<String> scopeCombo;

  private RowAction rowAction;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    super.initialize(location, resources);

    scopeCombo.getItems().addAll(SCOPE_KEYS.keySet());
    scopeCombo.setConverter(new StringConverter<>() {
      @Override
      public String toString(String value) {
        String key = SCOPE_KEYS.get(value);
        return key != null ? StudioBundle.get(key) : value;
      }

      @Override
      public String fromString(String displayName) {
        return SCOPE_KEYS.entrySet().stream()
            .filter(entry -> StudioBundle.get(entry.getValue()).equals(displayName))
            .map(Map.Entry::getKey)
            .findFirst()
            .orElse("ALWAYS");
      }
    });

    bindTextField(eventField, (el, value) -> rowAction.setEvent(value.isEmpty() ? null : value));
    bindComboBox(scopeCombo, (el, value) -> rowAction.setScope(value));
  }

  public void setRowAction(@NonNull RowAction rowAction) {
    this.rowAction = rowAction;
    setFieldValue(eventField, rowAction.getEvent());
    setFieldValue(scopeCombo, rowAction.getScope() != null ? rowAction.getScope() : "ALWAYS");
  }

  /** Lets the owning dialog disable its OK button while the (required) Event field is blank. */
  public StringProperty eventProperty() {
    return eventField.textProperty();
  }
}
