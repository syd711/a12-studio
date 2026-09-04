package de.a12.studio.ui.editors.applicationmodel.dialogs;

import de.a12.studio.models.applicationmodel.Layout;
import de.a12.studio.models.applicationmodel.Region;
import de.a12.studio.models.util.JsonSettings;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.components.DialogController;
import de.a12.studio.ui.util.WidgetFactory;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Add/edit dialog for a single {@link Region} entry of {@link
 * de.a12.studio.ui.editors.applicationmodel.SubregionsPanelController}: name, the mandatory {@link Layout} name
 * (SME default layouts: ApplicationFrame, MasterDetail, Dashboard, Stack, Null - any other value may also be
 * entered, so the combo is editable) and the layout's free-form settings (a JSON object), mirroring how {@link
 * de.a12.studio.ui.editors.applicationmodel.LayoutPanelController}/{@link DirectiveDialogController} edit a
 * {@link Layout}. Always builds a brand new {@link Region} instance on OK (see {@link #getResult()}) rather than
 * mutating the one passed to {@link #init}; the caller is responsible for copying its name/layout onto the
 * existing subregion (edit, preserving its own {@code subRegions}) or appending it (add).
 */
public class SubregionDialogController implements DialogController {

  private static final List<String> LAYOUTS = List.of("ApplicationFrame", "MasterDetail", "Dashboard", "Stack", "Null");

  private static final String INVALID_JSON_MESSAGE = "Please enter a valid JSON object, e.g. {\"key\": \"value\"}.";

  @FXML
  private TextField nameField;

  @FXML
  private ComboBox<String> layoutCombo;

  @FXML
  private TextArea settingsArea;

  @FXML
  private Button okButton;

  @FXML
  private Button cancelButton;

  private Stage stage;

  private Region built;

  private Optional<ButtonType> result = Optional.of(ButtonType.CANCEL);

  @FXML
  private void initialize() {
    layoutCombo.getItems().addAll(LAYOUTS);
    okButton.disableProperty().bind(Bindings.createBooleanBinding(
        () -> isBlank(nameField.getText()) || isBlank(layoutCombo.getValue()),
        nameField.textProperty(), layoutCombo.valueProperty()));
    nameField.requestFocus();
  }

  @Override
  public void onDialogCancel() {
    stage.close();
  }

  @FXML
  private void onDialogSubmit() {
    Region region = buildRegion();
    if (region == null) {
      return;
    }
    built = region;
    result = Optional.of(ButtonType.OK);
    stage.close();
  }

  private Region buildRegion() {
    Map<String, Object> settings = parseJsonObject(settingsArea.getText());
    if (settings == null) {
      WidgetFactory.showAlert(Studio.stage, INVALID_JSON_MESSAGE);
      return null;
    }
    Region region = new Region();
    region.setName(nameField.getText().trim());
    Layout layout = new Layout();
    layout.setName(layoutCombo.getValue());
    layout.setSettings(settings);
    region.setLayout(layout);
    return region;
  }

  void init(Stage stage, Region existing) {
    this.stage = stage;
    Layout layout = existing != null ? existing.getLayout() : null;
    nameField.setText(existing != null ? existing.getName() : "");
    layoutCombo.setValue(layout != null ? layout.getName() : null);
    settingsArea.setText(layout != null ? toText(layout.getSettings()) : "{}");
  }

  Optional<Region> getResult() {
    if (result.isPresent() && result.get() == ButtonType.OK) {
      return Optional.ofNullable(built);
    }
    return Optional.empty();
  }

  private static boolean isBlank(String value) {
    return value == null || value.isBlank();
  }

  private static String toText(Map<String, Object> settings) {
    if (settings == null || settings.isEmpty()) {
      return "{}";
    }
    return JsonSettings.objectMapper.writeValueAsString(settings);
  }

  private static Map<String, Object> parseJsonObject(String text) {
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
