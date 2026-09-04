package de.a12.studio.ui.editors.applicationmodel;

import de.a12.studio.models.applicationmodel.ApplicationModel;
import de.a12.studio.models.applicationmodel.ApplicationModelContent;
import de.a12.studio.models.applicationmodel.Layout;
import de.a12.studio.models.applicationmodel.Region;
import de.a12.studio.models.documentmodel.Element;
import de.a12.studio.models.util.JsonSettings;
import de.a12.studio.ui.editors.AbstractPropertyEditor;
import de.a12.studio.ui.util.Debouncer;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import org.jspecify.annotations.NonNull;

import java.net.URL;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * Edits {@link Region#getLayout()} of the application model's top-level region: the layout name via {@link
 * #layoutCombo}, and its free-form settings (a JSON object) via {@link #settingsArea}. Not bound to a single
 * {@link Element} (the layout lives on the model's content), so it follows the model-header pattern used by e.g.
 * {@link ActivityPanelController}. The settings text needs its own JSON-validity error, which {@link
 * #bindTextArea} would clobber on every commit (it always clears the error container for header panels, see
 * {@link AbstractPropertyEditor#commitChange(javafx.scene.Node)}), so that field is wired manually instead,
 * following {@link SupportedCharactersPanelController}.
 */
public class LayoutPanelController extends AbstractPropertyEditor implements Initializable {

  private static final List<String> LAYOUTS = List.of("ApplicationFrame", "MasterDetail", "Dashboard", "Stack", "Null");

  private static final int COMMIT_DEBOUNCE_MS = 150;

  private static final String INVALID_JSON_MESSAGE = "Please enter a valid JSON object, e.g. {\"key\": \"value\"}.";

  private final Debouncer debouncer = new Debouncer();

  @FXML
  private ComboBox<String> layoutCombo;

  @FXML
  private TextArea settingsArea;

  private ApplicationModel model;

  // Set while setModel() is repopulating settingsArea from the model, so the listener below doesn't mistake
  // that programmatic change for a user edit and write it straight back.
  private boolean updatingFromModel;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    super.initialize(location, resources);
    layoutCombo.getItems().addAll(LAYOUTS);

    bindComboBox(layoutCombo, (element, value) -> getOrCreateLayout().setName(value));
    settingsArea.textProperty().addListener((observable, oldValue, newValue) -> {
      if (!updatingFromModel) {
        commitSettingsChange(newValue);
      }
    });
  }

  public void setModel(@NonNull ApplicationModel model) {
    this.model = model;
    Layout layout = getLayout();
    setFieldValue(layoutCombo, layout != null ? layout.getName() : null);
    updatingFromModel = true;
    try {
      settingsArea.setText(layout != null ? toText(layout.getSettings()) : "");
    } finally {
      updatingFromModel = false;
    }
    hideError();
  }

  private void commitSettingsChange(String text) {
    if (model == null) {
      return;
    }

    Map<String, Object> parsed = parseText(text);
    if (parsed == null) {
      showError("ERROR", INVALID_JSON_MESSAGE);
      return;
    }
    hideError();
    getOrCreateLayout().setSettings(parsed);

    debouncer.debounce(settingsArea.getId(), this::commitHeaderChange, COMMIT_DEBOUNCE_MS, true);
  }

  private Layout getLayout() {
    if (model == null || model.getContent() == null || model.getContent().getRegion() == null) {
      return null;
    }
    return model.getContent().getRegion().getLayout();
  }

  private Layout getOrCreateLayout() {
    ApplicationModelContent content = model.getContent();
    if (content == null) {
      content = new ApplicationModelContent();
      model.setContent(content);
    }
    Region region = content.getRegion();
    if (region == null) {
      region = new Region();
      content.setRegion(region);
    }
    Layout layout = region.getLayout();
    if (layout == null) {
      layout = new Layout();
      region.setLayout(layout);
    }
    return layout;
  }

  private static String toText(Map<String, Object> settings) {
    if (settings == null || settings.isEmpty()) {
      return "{}";
    }
    return JsonSettings.objectMapper.writeValueAsString(settings);
  }

  private static Map<String, Object> parseText(String text) {
    if (text == null || text.isBlank()) {
      return new LinkedHashMap<>();
    }
    try {
      Map<?, ?> raw = JsonSettings.objectMapper.readValue(text, Map.class);
      Map<String, Object> result = new LinkedHashMap<>();
      for (Map.Entry<?, ?> entry : raw.entrySet()) {
        if (!(entry.getKey() instanceof String key)) {
          return null;
        }
        result.put(key, entry.getValue());
      }
      return result;
    } catch (RuntimeException e) {
      return null;
    }
  }
}
