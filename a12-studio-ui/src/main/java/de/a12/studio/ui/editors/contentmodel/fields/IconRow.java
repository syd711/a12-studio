package de.a12.studio.ui.editors.contentmodel.fields;

import de.a12.studio.models.contentmodel.ContentProps;
import de.a12.studio.ui.util.StudioBundle;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import org.jspecify.annotations.NonNull;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * An icon setting: the icon's {@code name} (a Material icon such as {@code emoji_emotions}) and its {@code theme},
 * stored as the {@code {name, theme}} object SME's icon picker writes. An empty name removes the icon.
 */
public class IconRow extends SettingRow {

  private static final List<String> THEMES = List.of("filled", "outlined", "custom");

  private final TextField name = new TextField();
  private final ComboBox<String> theme = new ComboBox<>();

  public IconRow() {
    name.setPromptText(StudioBundle.get("content_settings.icon_name_prompt"));
    theme.getItems().setAll(THEMES);
    theme.setEditable(true);
    theme.setPrefWidth(96);
    HBox.setHgrow(name, Priority.ALWAYS);
    controls().getChildren().addAll(name, theme);
    name.textProperty().addListener((observable, oldValue, text) -> write());
    theme.valueProperty().addListener((observable, oldValue, value) -> write());
  }

  private void write() {
    edited(props -> {
      String iconName = name.getText() == null ? "" : name.getText().trim();
      if (iconName.isEmpty()) {
        props.remove(getPath());
        return;
      }
      Map<String, Object> icon = new LinkedHashMap<>();
      icon.put("name", iconName);
      String iconTheme = theme.getValue();
      icon.put("theme", iconTheme == null || iconTheme.isBlank() ? THEMES.get(0) : iconTheme.trim());
      props.set(getPath(), icon);
    });
  }

  @Override
  protected void load(@NonNull ContentProps props) {
    Map<String, Object> icon = props.getMap(getPath());
    name.setText(icon != null && icon.get("name") instanceof String text ? text : "");
    theme.setValue(icon != null && icon.get("theme") instanceof String text ? text : THEMES.get(0));
  }
}
