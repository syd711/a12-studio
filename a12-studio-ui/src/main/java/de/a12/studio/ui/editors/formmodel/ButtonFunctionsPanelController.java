package de.a12.studio.ui.editors.formmodel;

import de.a12.studio.models.formmodel.Button;
import de.a12.studio.models.formmodel.ButtonType;
import de.a12.studio.models.formmodel.EventButton;
import de.a12.studio.models.formmodel.NavigationButton;
import de.a12.studio.ui.editors.AbstractPropertyEditor;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import org.jspecify.annotations.NonNull;

import java.net.URL;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.function.Consumer;

/**
 * Edits a {@link Button}'s {@code type}, {@code event}/{@code target} (mutually exclusive, depending on {@code
 * type}), {@code validation}, {@code scope} and (Event type only) {@code enablement} - SME's {@code
 * I_Button-form.json} "Button Functions" section. Embedded in {@link
 * de.a12.studio.ui.editors.formmodel.dialogs.FormButtonDialogController}.
 * <p>
 * Changing {@code type} can't be applied in place - {@link EventButton} and {@link NavigationButton} are
 * distinct classes (see {@link Button}'s {@code @JsonSubTypes}) - so this panel only reports the new type via
 * {@link #setOnTypeChanged}; the owning dialog is responsible for constructing the replacement instance
 * (carrying over the fields the two types share) and calling {@link #setButton} again with it.
 */
public class ButtonFunctionsPanelController extends AbstractPropertyEditor implements Initializable {

  private static final Map<String, String> TYPE_LABELS = new LinkedHashMap<>();
  private static final Map<String, String> EVENT_LABELS = new LinkedHashMap<>();
  private static final Map<String, String> VALIDATION_LABELS = new LinkedHashMap<>();
  private static final Map<String, String> SCOPE_LABELS = new LinkedHashMap<>();
  private static final Map<String, String> ENABLEMENT_LABELS = new LinkedHashMap<>();

  static {
    TYPE_LABELS.put(ButtonType.EVENT.getValue(), "Event");
    TYPE_LABELS.put(ButtonType.NAVIGATION.getValue(), "Navigation");

    EVENT_LABELS.put("CRUD:SAVE", "Save");
    EVENT_LABELS.put("event_submit", "Submit");
    EVENT_LABELS.put("event_cancel", "Cancel");

    VALIDATION_LABELS.put(null, "No Validation (default)");
    VALIDATION_LABELS.put("partial", "Partial Validation");
    VALIDATION_LABELS.put("full", "Full Validation");

    SCOPE_LABELS.put("ALWAYS", "Always");
    SCOPE_LABELS.put("DISABLED_IN_EDIT_MODE", "Disable Button in Edit Mode");
    SCOPE_LABELS.put("DISABLED_IN_READONLY_MODE", "Disable Button in Readonly Mode");
    SCOPE_LABELS.put("HIDDEN_IN_EDIT_MODE", "Hide Button in Edit Mode");
    SCOPE_LABELS.put("HIDDEN_IN_READONLY_MODE", "Hide Button in Readonly Mode");

    ENABLEMENT_LABELS.put(null, "Show enabled Button (default)");
    ENABLEMENT_LABELS.put("HIDDEN", "Hide Button");
    ENABLEMENT_LABELS.put("DISABLED", "Disable Button");
  }

  @FXML
  private ComboBox<String> typeCombo;

  @FXML
  private VBox eventBox;
  @FXML
  private ComboBox<String> eventCombo;

  @FXML
  private VBox targetBox;
  @FXML
  private ComboBox<String> targetCombo;

  @FXML
  private ComboBox<String> validationCombo;

  @FXML
  private ComboBox<String> scopeCombo;

  @FXML
  private VBox enablementBox;
  @FXML
  private ComboBox<String> enablementCombo;

  private Button button;

  private Consumer<ButtonType> onTypeChanged;

  // Set while fields are being repopulated from the model (setButton(), or the visibility toggle that follows
  // a programmatic type set), so the typeCombo listener doesn't mistake that for a user-initiated type change.
  private boolean updatingFromModel;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    super.initialize(location, resources);

    typeCombo.getItems().addAll(TYPE_LABELS.keySet());
    typeCombo.setConverter(displayConverter(TYPE_LABELS));
    typeCombo.valueProperty().addListener((observable, oldValue, newValue) -> {
      updateVisibility();
      if (!updatingFromModel && onTypeChanged != null) {
        onTypeChanged.accept(ButtonType.fromValue(newValue));
      }
    });

    eventCombo.getItems().addAll(EVENT_LABELS.keySet());
    eventCombo.setConverter(freeTextDisplayConverter(EVENT_LABELS));

    validationCombo.getItems().addAll(VALIDATION_LABELS.keySet());
    validationCombo.setConverter(displayConverter(VALIDATION_LABELS));
    bindComboBox(validationCombo, (el, value) -> button.setValidation(value));

    scopeCombo.getItems().addAll(SCOPE_LABELS.keySet());
    scopeCombo.setConverter(displayConverter(SCOPE_LABELS));
    bindComboBox(scopeCombo, (el, value) -> button.setScope(value));

    enablementCombo.getItems().addAll(ENABLEMENT_LABELS.keySet());
    enablementCombo.setConverter(displayConverter(ENABLEMENT_LABELS));
    bindComboBox(enablementCombo, (el, value) -> {
      if (button instanceof EventButton eventButton) {
        eventButton.setEnablement(value);
      }
    });

    bindComboBox(eventCombo, (el, value) -> button.setEvent(value == null || value.isEmpty() ? null : value));
    bindComboBox(targetCombo, (el, value) -> {
      if (button instanceof NavigationButton navigationButton) {
        navigationButton.setTarget(value);
      }
    });
  }

  public void setOnTypeChanged(@NonNull Consumer<ButtonType> handler) {
    this.onTypeChanged = handler;
  }

  public void setScreenIds(@NonNull List<String> screenIds) {
    targetCombo.getItems().setAll(screenIds);
  }

  public void setButton(@NonNull Button button) {
    this.button = button;
    updatingFromModel = true;
    try {
      ButtonType type = button.getType() != null ? button.getType() : ButtonType.EVENT;
      setFieldValue(typeCombo, type.getValue());
      setFieldValue(eventCombo, button.getEvent());
      setFieldValue(targetCombo, button instanceof NavigationButton navigationButton ? navigationButton.getTarget() : null);
      setFieldValue(scopeCombo, button.getScope());
      setFieldValue(validationCombo, button.getValidation());
      setFieldValue(enablementCombo, button instanceof EventButton eventButton ? eventButton.getEnablement() : null);
    }
    finally {
      updatingFromModel = false;
    }
    updateVisibility();
  }

  private void updateVisibility() {
    boolean navigation = ButtonType.NAVIGATION.getValue().equals(typeCombo.getValue());
    eventBox.setVisible(!navigation);
    eventBox.setManaged(!navigation);
    targetBox.setVisible(navigation);
    targetBox.setManaged(navigation);
    enablementBox.setVisible(!navigation);
    enablementBox.setManaged(!navigation);
  }

  private static StringConverter<String> displayConverter(Map<String, String> labels) {
    return new StringConverter<>() {
      @Override
      public String toString(String value) {
        return labels.getOrDefault(value, value);
      }

      @Override
      public String fromString(String displayName) {
        return labels.entrySet().stream()
            .filter(entry -> entry.getValue().equals(displayName))
            .map(Map.Entry::getKey)
            .findFirst()
            .orElse(null);
      }
    };
  }

  /**
   * Same as {@link #displayConverter} but for editable combo boxes whose value isn't restricted to the
   * proposed {@code labels} - e.g. {@code eventCombo}, where the proposed events are convenience shortcuts but
   * any custom event name is a valid value. Unlike {@link #displayConverter}, a typed value that doesn't match
   * one of the proposed display names is passed through as-is instead of being resolved to {@code null}.
   */
  private static StringConverter<String> freeTextDisplayConverter(Map<String, String> labels) {
    return new StringConverter<>() {
      @Override
      public String toString(String value) {
        return labels.getOrDefault(value, value);
      }

      @Override
      public String fromString(String displayName) {
        return labels.entrySet().stream()
            .filter(entry -> entry.getValue().equals(displayName))
            .map(Map.Entry::getKey)
            .findFirst()
            .orElse(displayName);
      }
    };
  }
}
