package de.a12.studio.ui.editors.contentmodel.fields;

import de.a12.studio.models.contentmodel.ContentElement;
import de.a12.studio.models.contentmodel.ContentProps;
import de.a12.studio.models.contentmodel.ContentTableColumns;
import de.a12.studio.ui.util.StudioBundle;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A setting that refers to one column of the shown {@code Table} by its column id (e.g. the screen reader column).
 * The choices are the table's columns, named by the text of their head cell (or {@code <id>} when it has none, as in
 * SME), plus "None", which removes the key. A reference to a column that no longer exists stays visible as its raw id
 * until another column is chosen. The columns are read again whenever the list opens, since the table's columns
 * panel can change them while this row keeps showing the table.
 */
public class ColumnRow extends SettingRow {

  /** One entry of the list; a null id is "None". */
  private record Choice(String id, String label) {
    @Override
    public String toString() {
      return label;
    }
  }

  private final ComboBox<Choice> input = new ComboBox<>();
  private ContentElement table;
  private boolean rebuilding;

  public ColumnRow() {
    input.setMaxWidth(Double.MAX_VALUE);
    HBox.setHgrow(input, Priority.ALWAYS);
    controls().getChildren().add(input);
    input.setOnShowing(event -> rebuild(currentId()));
    input.valueProperty().addListener((observable, oldValue, choice) -> {
      if (!rebuilding && choice != null) {
        edited(props -> props.set(getPath(), choice.id()));
      }
    });
  }

  public void setPrompt(String prompt) {
    input.setPromptText(prompt);
  }

  public String getPrompt() {
    return input.getPromptText();
  }

  private String currentId() {
    Choice value = input.getValue();
    return value == null ? null : value.id();
  }

  private void rebuild(String selectedId) {
    rebuilding = true;
    try {
      List<Choice> choices = new ArrayList<>();
      choices.add(new Choice(null, StudioBundle.get("content_settings.none")));
      boolean found = selectedId == null;
      // Read-only: merely showing a table must never add an empty "columns" list to it
      if (table != null && new ContentProps(table).get("columns") instanceof List<?> columns) {
        for (int i = 0; i < columns.size(); i++) {
          if (columns.get(i) instanceof Map<?, ?> column && column.get("id") instanceof String id) {
            String label = ContentTableColumns.headLabel(table, i);
            choices.add(new Choice(id, label != null && !label.isBlank() ? label : "<" + id + ">"));
            found |= id.equals(selectedId);
          }
        }
      }
      if (!found) {
        choices.add(new Choice(selectedId, selectedId));
      }
      input.getItems().setAll(choices);
      input.setValue(choices.stream().filter(choice -> selectedId == null ? choice.id() == null : selectedId.equals(choice.id()))
          .findFirst().orElse(null));
    }
    finally {
      rebuilding = false;
    }
  }

  @Override
  protected void load(@NonNull ContentProps props) {
    table = props.getElement();
    String value = props.getString(getPath());
    rebuild(value == null || value.isBlank() ? null : value);
    if (value == null || value.isBlank()) {
      // Nothing chosen: show the prompt rather than "None"
      rebuilding = true;
      input.setValue(null);
      rebuilding = false;
    }
  }
}
