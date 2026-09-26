package de.a12.studio.ui.editors.contentmodel.fields;

import de.a12.studio.models.contentmodel.ContentElement;
import de.a12.studio.models.contentmodel.ContentProps;
import de.a12.studio.ui.editors.contentmodel.ContentReferences;
import de.a12.studio.ui.util.StudioBundle;
import de.a12.studio.ui.util.WidgetFactory;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A setting that is a list of strings stored as an array (the Fields, Groups and Rules a Message Group Container reports
 * about): one line per entry with a delete button, an Add button below. Entries are either picked from what the Document
 * Model offers ({@link Kind#FIELD}, {@link Kind#GROUP}, shown by path, stored by id) or typed ({@link Kind#TEXT}). An id
 * that is not among the choices stays selected and marked, like {@link ReferenceRow} does. The array is kept when it is
 * empty: SME creates these elements with empty lists and its export keeps them.
 */
public class ListRow extends SettingRow {

  /** What an entry is. */
  public enum Kind {
    /** A field of the Document Model. */
    FIELD,
    /** A group of the Document Model. */
    GROUP,
    /** Free text. */
    TEXT
  }

  private final VBox entries = new VBox(4);
  private final Button add = new Button();

  private Kind kind = Kind.TEXT;
  private List<String> current = new ArrayList<>();
  private Map<String, String> labels = new HashMap<>();
  private List<String> choiceIds = List.of();

  public ListRow() {
    add.setText(StudioBundle.get("content_settings.list_add"));
    add.getStyleClass().add("default-button");
    add.setOnAction(event -> edited(props -> {
      current.add("");
      write(props);
      rebuild();
    }));
    HBox addLine = new HBox(add);
    addLine.setAlignment(Pos.CENTER_LEFT);
    addBelow(entries);
    addBelow(addLine);
  }

  public Kind getKind() {
    return kind;
  }

  public void setKind(Kind kind) {
    this.kind = kind;
  }

  @Override
  protected void load(@NonNull ContentProps props) {
    current = new ArrayList<>();
    if (props.get(getPath()) instanceof List<?> list) {
      list.forEach(entry -> current.add(entry instanceof String text ? text : ""));
    }
    loadChoices(props.getElement());
    rebuild();
  }

  private void loadChoices(ContentElement element) {
    labels = new HashMap<>();
    choiceIds = List.of();
    ContentReferences references = context() != null ? context().references() : null;
    boolean bound = references != null && references.isBound();
    if (kind == Kind.TEXT || !bound) {
      return;
    }
    List<ContentReferences.Choice> choices = kind == Kind.FIELD ? references.listableFields(element) : references.listableGroups(element);
    choiceIds = choices.stream().map(ContentReferences.Choice::id).toList();
    choices.forEach(choice -> labels.put(choice.id(), choice.label()));
  }

  private void rebuild() {
    entries.getChildren().clear();
    for (int i = 0; i < current.size(); i++) {
      entries.getChildren().add(line(i));
    }
    add.setDisable(kind != Kind.TEXT && !isBound());
  }

  private boolean isBound() {
    return context() != null && context().references() != null && context().references().isBound();
  }

  private HBox line(int index) {
    HBox line = new HBox(6);
    line.setAlignment(Pos.CENTER_LEFT);
    String value = current.get(index);
    if (kind == Kind.TEXT) {
      TextField text = new TextField(value);
      HBox.setHgrow(text, Priority.ALWAYS);
      text.textProperty().addListener((observable, oldValue, typed) -> edited(props -> {
        current.set(index, typed == null ? "" : typed);
        write(props);
      }));
      line.getChildren().add(text);
    }
    else {
      line.getChildren().add(picker(index, value));
    }
    Button remove = new Button();
    remove.setGraphic(WidgetFactory.createIcon("mdi2t-trash-can-outline", 14, null));
    remove.getStyleClass().add("default-button");
    remove.setTooltip(WidgetFactory.createTooltip(StudioBundle.get("content_settings.list_remove")));
    remove.setOnAction(event -> edited(props -> {
      current.remove(index);
      write(props);
      rebuild();
    }));
    line.getChildren().add(remove);
    return line;
  }

  private ComboBox<String> picker(int index, String value) {
    Map<String, String> shown = new HashMap<>(labels);
    List<String> ids = new ArrayList<>(choiceIds);
    boolean hasValue = !value.isBlank();
    if (hasValue && !shown.containsKey(value)) {
      ContentReferences references = context() != null ? context().references() : null;
      shown.put(value, StudioBundle.get("content_settings.reference_unavailable",
          references != null && references.isBound() ? references.labelOf(value) : value));
      ids.add(value);
    }
    ComboBox<String> combo = new ComboBox<>();
    combo.setMaxWidth(Double.MAX_VALUE);
    HBox.setHgrow(combo, Priority.ALWAYS);
    combo.setConverter(new StringConverter<>() {
      @Override
      public String toString(String id) {
        return id == null ? "" : shown.getOrDefault(id, id);
      }

      @Override
      public String fromString(String string) {
        return string;
      }
    });
    combo.getItems().setAll(ids);
    combo.setValue(hasValue ? value : null);
    combo.setDisable(!isBound() && !hasValue);
    combo.setPromptText(StudioBundle.get(isBound() ? "content_settings.reference_select" : "content_settings.reference_needs_document_model"));
    combo.valueProperty().addListener((observable, oldValue, id) -> edited(props -> {
      current.set(index, id == null ? "" : id);
      write(props);
    }));
    return combo;
  }

  private void write(ContentProps props) {
    props.set(getPath(), new ArrayList<>(current));
  }
}
