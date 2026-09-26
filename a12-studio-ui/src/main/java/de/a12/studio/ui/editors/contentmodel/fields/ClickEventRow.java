package de.a12.studio.ui.editors.contentmodel.fields;

import de.a12.studio.models.contentmodel.ContentProps;
import de.a12.studio.ui.editors.contentmodel.ContentReferences;
import de.a12.studio.ui.util.StudioBundle;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.jspecify.annotations.NonNull;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * SME's click event setting ({@code onClick}, a table's {@code onRowClick}): None, an Event Name (a plain string the
 * hosting application reacts to) or an Event Node (a nested element such as a Save or Commit action that the
 * content engine runs itself), plus, with {@code confirmation="true"}, the optional confirmation dialog stored next
 * to it as {@code props.confirmation} ({@code title}, {@code message}, {@code confirmLabel}, {@code cancelLabel}).
 */
public class ClickEventRow extends SettingRow {

  private enum Kind { NONE, EVENT_NAME, EVENT_NODE }

  private static final String NAMESPACE = "com.mgmtp.a12.contentengine";
  // The event modules of the content engine's element library (the ones SME offers under "Add event").
  private static final List<String> EVENT_TYPES = List.of("CancelAction", "CommitAction", "SaveAction",
      "AddRowAction", "DeleteRowAction");
  private static final String CONFIRMATION = "confirmation";
  private static final List<String> CONFIRMATION_FIELDS = List.of("title", "message", "confirmLabel", "cancelLabel");

  private final ToggleGroup kinds = new ToggleGroup();
  private final ToggleButton none = segment("content_settings.click_none", Kind.NONE, "first");
  private final ToggleButton eventName = segment("content_settings.click_event_name", Kind.EVENT_NAME, null);
  private final ToggleButton eventNode = segment("content_settings.click_event_node", Kind.EVENT_NODE, "last");
  private final TextField nameField = new TextField();
  private final ComboBox<String> addNode = new ComboBox<>();
  private final Label nodeLabel = new Label();
  private final Button removeNode = new Button();
  private final HBox nodeBox = new HBox(6);
  private final CheckBox confirmationSwitch = new CheckBox();
  private final VBox confirmationBox = new VBox(4);
  private final Map<String, TextField> confirmationFields = new LinkedHashMap<>();
  private final VBox details = new VBox(4);
  // An Add Row Action needs the group it adds a row to: only repeated groups, picked from the Document Model.
  private final ComboBox<String> groupPicker = new ComboBox<>();
  private final Map<String, String> groupLabels = new java.util.HashMap<>();
  private final HBox groupBox = new HBox(6);

  private boolean confirmation;
  private boolean updating;

  public ClickEventRow() {
    HBox segments = new HBox(none, eventName, eventNode);
    segments.getStyleClass().add("content-setting-segments");
    for (ToggleButton button : List.of(none, eventName, eventNode)) {
      button.setMinWidth(javafx.scene.layout.Region.USE_PREF_SIZE);
    }

    nameField.setPromptText(StudioBundle.get("content_settings.click_event_name_prompt"));
    nameField.textProperty().addListener((observable, oldValue, text) ->
        edited(props -> props.set(getPath(), text.isEmpty() ? null : text)));

    addNode.getItems().setAll(EVENT_TYPES);
    addNode.setPromptText(StudioBundle.get("content_settings.click_add_event"));
    addNode.valueProperty().addListener((observable, oldValue, type) -> {
      if (!updating && type != null) {
        edited(props -> {
          props.set(getPath(), newEventNode(type));
          refreshGroupPicker(props);
        });
        showNode(type);
      }
    });
    removeNode.setText(StudioBundle.get("content_settings.click_remove_event"));
    removeNode.setOnAction(event -> {
      edited(props -> {
        props.remove(getPath());
        refreshGroupPicker(props);
      });
      showNode(null);
    });
    nodeLabel.setMaxWidth(Double.MAX_VALUE);
    HBox.setHgrow(nodeLabel, Priority.ALWAYS);
    nodeBox.setAlignment(Pos.CENTER_LEFT);
    nodeBox.getChildren().addAll(addNode, nodeLabel, removeNode);

    confirmationSwitch.setText(StudioBundle.get("content_settings.confirmation"));
    confirmationSwitch.selectedProperty().addListener((observable, oldValue, selected) -> {
      confirmationBox.setVisible(selected);
      confirmationBox.setManaged(selected);
      edited(props -> {
        if (selected) {
          props.set(CONFIRMATION, currentConfirmation());
        }
        else {
          props.remove(CONFIRMATION);
        }
      });
    });
    for (String field : CONFIRMATION_FIELDS) {
      TextField textField = new TextField();
      textField.textProperty().addListener((observable, oldValue, text) ->
          edited(props -> props.set(CONFIRMATION, currentConfirmation())));
      confirmationFields.put(field, textField);
      Label label = new Label(StudioBundle.get("content_settings.confirmation_" + field));
      label.setMinWidth(96);
      HBox line = new HBox(8, label, textField);
      line.setAlignment(Pos.CENTER_LEFT);
      HBox.setHgrow(textField, Priority.ALWAYS);
      confirmationBox.getChildren().add(line);
    }
    confirmationBox.setPadding(new Insets(0, 0, 0, 12));

    groupPicker.setMaxWidth(Double.MAX_VALUE);
    HBox.setHgrow(groupPicker, Priority.ALWAYS);
    groupPicker.setConverter(new javafx.util.StringConverter<>() {
      @Override
      public String toString(String id) {
        return id == null ? "" : groupLabels.getOrDefault(id, id);
      }

      @Override
      public String fromString(String string) {
        return string;
      }
    });
    groupPicker.valueProperty().addListener((observable, oldValue, id) -> {
      if (!updating) {
        edited(props -> eventProps(props).put("groupId", id == null ? "" : id));
      }
    });
    Label groupLabel = new Label(StudioBundle.get("content_settings.event_group"));
    groupBox.setAlignment(Pos.CENTER_LEFT);
    groupBox.getChildren().addAll(groupLabel, groupPicker);
    setShown(groupBox, false);

    details.setPadding(new Insets(4, 0, 0, 0));
    details.getChildren().addAll(segments, nameField, nodeBox, groupBox, confirmationSwitch, confirmationBox);
    addBelow(details);

    kinds.selectedToggleProperty().addListener((observable, oldToggle, newToggle) -> {
      if (updating) {
        return;
      }
      if (newToggle == null) {
        if (oldToggle != null) {
          kinds.selectToggle(oldToggle);
        }
        return;
      }
      Kind kind = (Kind) newToggle.getUserData();
      showKind(kind);
      if (kind == Kind.NONE) {
        edited(props -> props.remove(getPath()));
      }
    });
  }

  public boolean isConfirmation() {
    return confirmation;
  }

  public void setConfirmation(boolean confirmation) {
    this.confirmation = confirmation;
  }

  private ToggleButton segment(String key, Kind kind, String position) {
    ToggleButton button = new ToggleButton(StudioBundle.get(key));
    button.setUserData(kind);
    button.setToggleGroup(kinds);
    button.getStyleClass().add("content-setting-segment");
    if (position != null) {
      button.getStyleClass().add(position);
    }
    return button;
  }

  private Map<String, Object> currentConfirmation() {
    Map<String, Object> value = new LinkedHashMap<>();
    confirmationFields.forEach((field, textField) -> {
      if (!textField.getText().isEmpty()) {
        value.put(field, textField.getText());
      }
    });
    return value;
  }

  private static Map<String, Object> newEventNode(String type) {
    Map<String, Object> node = new LinkedHashMap<>();
    node.put("id", UUID.randomUUID().toString().replace("-", "").substring(0, 8));
    node.put("type", type);
    node.put("namespace", NAMESPACE);
    Map<String, Object> props = new LinkedHashMap<>();
    if ("AddRowAction".equals(type)) {
      props.put("groupId", "");
    }
    node.put("props", props);
    node.put("children", new java.util.ArrayList<>());
    return node;
  }

  private void showKind(Kind kind) {
    setShown(nameField, kind == Kind.EVENT_NAME);
    setShown(nodeBox, kind == Kind.EVENT_NODE);
    boolean withConfirmation = confirmation && kind != Kind.NONE;
    setShown(confirmationSwitch, withConfirmation);
    setShown(confirmationBox, withConfirmation && confirmationSwitch.isSelected());
  }

  private void showNode(String type) {
    boolean present = type != null;
    updating = true;
    try {
      addNode.setValue(null);
    }
    finally {
      updating = false;
    }
    nodeLabel.setText(present ? type : "");
    setShown(addNode, !present);
    setShown(nodeLabel, present);
    setShown(removeNode, present);
  }

  /** The event node's own props ({@code props.<path>.props}), created if the node has none. */
  @SuppressWarnings("unchecked")
  private Map<String, Object> eventProps(ContentProps props) {
    Map<String, Object> node = (Map<String, Object>) props.get(getPath());
    return (Map<String, Object>) node.computeIfAbsent("props", key -> new LinkedHashMap<String, Object>());
  }

  /** Shows the group picker for an Add Row Action node, with the repeated groups the element may add rows to. */
  private void refreshGroupPicker(ContentProps props) {
    boolean addRow = props.get(getPath()) instanceof Map<?, ?> node && "AddRowAction".equals(node.get("type"));
    setShown(groupBox, addRow);
    if (!addRow) {
      return;
    }
    ContentReferences references = context() != null ? context().references() : null;
    boolean bound = references != null && references.isBound();
    String stored = eventProps(props).get("groupId") instanceof String id && !id.isBlank() ? id : null;
    groupLabels.clear();
    java.util.List<String> ids = new java.util.ArrayList<>();
    if (bound) {
      for (ContentReferences.Choice choice : references.groups(props.getElement(), true)) {
        groupLabels.put(choice.id(), choice.label());
        ids.add(choice.id());
      }
    }
    if (stored != null && !groupLabels.containsKey(stored)) {
      groupLabels.put(stored, StudioBundle.get("content_settings.reference_unavailable", bound ? references.labelOf(stored) : stored));
      ids.add(stored);
    }
    boolean wasUpdating = updating;
    updating = true;
    try {
      groupPicker.getItems().setAll(ids);
      groupPicker.setValue(stored);
    }
    finally {
      updating = wasUpdating;
    }
    groupPicker.setDisable(!bound && stored == null);
    groupPicker.setPromptText(StudioBundle.get(bound ? "content_settings.reference_select" : "content_settings.reference_needs_document_model"));
  }

  private static void setShown(javafx.scene.Node node, boolean shown) {
    node.setVisible(shown);
    node.setManaged(shown);
  }

  @Override
  @SuppressWarnings("unchecked")
  protected void load(@NonNull ContentProps props) {
    Object value = props.get(getPath());
    Kind kind = value == null ? Kind.NONE : value instanceof Map<?, ?> ? Kind.EVENT_NODE : Kind.EVENT_NAME;
    updating = true;
    try {
      kinds.selectToggle(kind == Kind.NONE ? none : kind == Kind.EVENT_NAME ? eventName : eventNode);
      nameField.setText(value instanceof String text ? text : "");
      Map<String, Object> saved = props.getMap(CONFIRMATION);
      confirmationSwitch.setSelected(saved != null);
      confirmationFields.forEach((field, textField) ->
          textField.setText(saved != null && saved.get(field) instanceof String text ? text : ""));
    }
    finally {
      updating = false;
    }
    showNode(value instanceof Map<?, ?> node && node.get("type") instanceof String type ? type : null);
    refreshGroupPicker(props);
    showKind(kind);
  }
}
