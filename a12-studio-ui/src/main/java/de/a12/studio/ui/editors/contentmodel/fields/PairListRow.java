package de.a12.studio.ui.editors.contentmodel.fields;

import de.a12.studio.models.contentmodel.ContentProps;
import de.a12.studio.ui.util.StudioBundle;
import de.a12.studio.ui.util.WidgetFactory;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A setting that is a list of two-field entries stored as an array of objects: the localized texts of a form element
 * ({@code [{locale, text}]}, the first field a choice among the model's locales) and its annotations ({@code [{name,
 * value}]}, two text fields). Declared with {@code keys="locale,text"} or {@code keys="name,value"}. Like SME's
 * controllers the array is removed when it has no entries, and the other keys of an entry are kept.
 */
public class PairListRow extends SettingRow {

  private final VBox entries = new VBox(4);
  private final Button add = new Button();

  private String firstKey = "name";
  private String secondKey = "value";
  private List<Map<String, Object>> current = new ArrayList<>();

  public PairListRow() {
    add.setText(StudioBundle.get("content_settings.list_add"));
    add.getStyleClass().add("default-button");
    add.setOnAction(event -> edited(props -> {
      Map<String, Object> entry = new LinkedHashMap<>();
      entry.put(firstKey, isLocale() ? unusedLocale() : "");
      entry.put(secondKey, "");
      current.add(entry);
      write(props);
    }));
    HBox addLine = new HBox(add);
    addLine.setAlignment(Pos.CENTER_LEFT);
    addBelow(entries);
    addBelow(addLine);
  }

  public String getKeys() {
    return firstKey + "," + secondKey;
  }

  public void setKeys(String keys) {
    String[] parts = keys.split(",");
    firstKey = parts[0].trim();
    secondKey = parts[1].trim();
  }

  private boolean isLocale() {
    return "locale".equals(firstKey);
  }

  private String unusedLocale() {
    List<String> locales = context() != null ? context().locales() : List.of();
    for (String locale : locales) {
      if (current.stream().noneMatch(entry -> locale.equals(entry.get(firstKey)))) {
        return locale;
      }
    }
    return locales.isEmpty() ? "" : locales.get(0);
  }

  @Override
  @SuppressWarnings("unchecked")
  protected void load(@NonNull ContentProps props) {
    current = new ArrayList<>();
    if (props.get(getPath()) instanceof List<?> list) {
      for (Object entry : list) {
        if (entry instanceof Map<?, ?> map) {
          current.add((Map<String, Object>) map);
        }
      }
    }
    rebuild();
  }

  private void rebuild() {
    entries.getChildren().clear();
    for (Map<String, Object> entry : current) {
      entries.getChildren().add(line(entry));
    }
  }

  private HBox line(Map<String, Object> entry) {
    HBox line = new HBox(6);
    line.setAlignment(Pos.CENTER_LEFT);
    String first = String.valueOf(entry.getOrDefault(firstKey, ""));
    if (isLocale()) {
      ComboBox<String> locale = new ComboBox<>();
      List<String> locales = new ArrayList<>(context() != null ? context().locales() : List.of());
      if (!first.isEmpty() && !locales.contains(first)) {
        locales.add(first);
      }
      locale.getItems().setAll(locales);
      locale.setValue(first.isEmpty() ? null : first);
      locale.setPrefWidth(80);
      locale.valueProperty().addListener((observable, oldValue, value) ->
          edited(props -> {
            entry.put(firstKey, value == null ? "" : value);
            write(props);
          }));
      line.getChildren().add(locale);
    }
    else {
      TextField name = new TextField(first);
      name.setPromptText(StudioBundle.get("content_settings.annotation_name"));
      name.setPrefWidth(100);
      name.textProperty().addListener((observable, oldValue, value) ->
          edited(props -> {
            entry.put(firstKey, value);
            write(props);
          }));
      line.getChildren().add(name);
    }
    TextField second = new TextField(String.valueOf(entry.getOrDefault(secondKey, "")));
    second.setPromptText(StudioBundle.get(isLocale() ? "content_settings.localized_text" : "content_settings.annotation_value"));
    HBox.setHgrow(second, Priority.ALWAYS);
    second.textProperty().addListener((observable, oldValue, value) ->
        edited(props -> {
          entry.put(secondKey, value);
          write(props);
        }));
    Button remove = new Button();
    remove.setGraphic(WidgetFactory.createIcon("mdi2t-trash-can-outline", 14, null));
    remove.getStyleClass().add("default-button");
    remove.setTooltip(WidgetFactory.createTooltip(StudioBundle.get("content_settings.list_remove")));
    remove.setOnAction(event -> edited(props -> {
      current.remove(entry);
      write(props);
      rebuild();
    }));
    line.getChildren().addAll(second, remove);
    return line;
  }

  /** Stores the entries; none at all removes the key, like SME's controllers. Rebuilds the lines after an added entry. */
  private void write(ContentProps props) {
    if (current.isEmpty()) {
      props.remove(getPath());
    }
    else {
      props.set(getPath(), new ArrayList<>(current));
    }
    if (entries.getChildren().size() != current.size()) {
      rebuild();
    }
  }
}
