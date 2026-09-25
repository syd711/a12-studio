package de.a12.studio.ui.editors.contentmodel;

import de.a12.studio.models.contentmodel.ContentElement;
import de.a12.studio.models.contentmodel.ContentProps;
import de.a12.studio.ui.editors.contentmodel.fields.SettingRow;
import de.a12.studio.ui.util.StudioBundle;
import de.a12.studio.ui.util.WidgetFactory;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * SME's settings of a Media Query element: the operator combining the queries (OR: any matches, AND: all match;
 * only meaningful with two or more) and the list of queries, each a screen size (xs/sm/md/lg) with a condition type
 * (exactly that size, that size and larger, that size and smaller), with add and delete.
 */
public class MediaQueryPanelController extends ContentSettingsPanelController {

  private static final List<String> SIZES = List.of("xs", "sm", "md", "lg");
  private static final List<String> KINDS = List.of("exact", "from", "to");

  @FXML
  private VBox queriesBox;

  @FXML
  private Button addQueryButton;

  @FXML
  private SettingRow operatorRow;

  @Override
  public void initialize(java.net.URL location, java.util.ResourceBundle resources) {
    super.initialize(location, resources);
    addQueryButton.setOnAction(event -> addQuery());
  }

  @Override
  protected void populate(@NonNull ContentElement element) {
    super.populate(element);
    rebuild();
  }

  private List<Map<String, Object>> queries() {
    ContentElement element = currentElement();
    Object existing = element.getProps() == null ? null : element.getProps().get("queries");
    if (existing instanceof List<?>) {
      @SuppressWarnings("unchecked")
      List<Map<String, Object>> list = (List<Map<String, Object>>) existing;
      return list;
    }
    List<Map<String, Object>> created = new ArrayList<>();
    new ContentProps(element).set("queries", created);
    return created;
  }

  private void rebuild() {
    queriesBox.getChildren().clear();
    ContentElement element = currentElement();
    if (element == null) {
      return;
    }
    List<Map<String, Object>> queries = queries();
    for (int i = 0; i < queries.size(); i++) {
      queriesBox.getChildren().add(queryCard(queries, i));
    }
    applyConditions();
  }

  /** The operator only means something with at least two queries. */
  @Override
  protected void applyConditions() {
    super.applyConditions();
    if (currentElement() != null) {
      operatorRow.setDisable(queries().size() < 2);
    }
  }

  private Node queryCard(List<Map<String, Object>> queries, int index) {
    Map<String, Object> query = queries.get(index);
    ComboBox<String> size = combo(SIZES, "content_settings.query_size_", query.get("size"));
    size.valueProperty().addListener((observable, oldValue, value) -> update(query, "size", value));
    ComboBox<String> kind = combo(KINDS, "content_settings.query_kind_", query.get("kind"));
    kind.valueProperty().addListener((observable, oldValue, value) -> update(query, "kind", value));
    Button remove = new Button();
    remove.setGraphic(WidgetFactory.createIcon("mdi2d-delete-outline", 14, null));
    remove.setTooltip(WidgetFactory.createTooltip(StudioBundle.get("content_settings.query_delete")));
    remove.setOnAction(event -> deleteQuery(index));

    Label title = new Label(StudioBundle.get("content_settings.query_number", index + 1));
    title.getStyleClass().add("content-setting-heading");
    title.setMaxWidth(Double.MAX_VALUE);
    HBox.setHgrow(title, Priority.ALWAYS);
    HBox header = new HBox(6, title, remove);
    header.setAlignment(Pos.CENTER_LEFT);
    VBox card = new VBox(4, header,
        labeled(StudioBundle.get("content_settings.query_screen_size"), size),
        labeled(StudioBundle.get("content_settings.query_condition_type"), kind));
    card.getStyleClass().add("content-setting-card");
    card.setPadding(new Insets(6));
    return card;
  }

  private static ComboBox<String> combo(List<String> values, String keyPrefix, Object current) {
    ComboBox<String> combo = new ComboBox<>();
    combo.getItems().setAll(values);
    combo.setConverter(new StringConverter<>() {
      @Override
      public String toString(String value) {
        return value == null ? "" : StudioBundle.get(keyPrefix + value);
      }

      @Override
      public String fromString(String string) {
        return string;
      }
    });
    combo.setValue(current instanceof String text ? text : null);
    combo.setMaxWidth(Double.MAX_VALUE);
    HBox.setHgrow(combo, Priority.ALWAYS);
    return combo;
  }

  private static HBox labeled(String text, Node control) {
    Label label = new Label(text);
    label.setMinWidth(110);
    HBox line = new HBox(8, label, control);
    line.setAlignment(Pos.CENTER_LEFT);
    return line;
  }

  private void update(Map<String, Object> query, String key, String value) {
    if (value != null && !value.equals(query.get(key))) {
      query.put(key, value);
      changed();
    }
  }

  void addQuery() {
    Map<String, Object> query = new LinkedHashMap<>();
    query.put("kind", "exact");
    query.put("size", "md");
    queries().add(query);
    changed();
    rebuild();
  }

  void deleteQuery(int index) {
    queries().remove(index);
    changed();
    rebuild();
  }
}
