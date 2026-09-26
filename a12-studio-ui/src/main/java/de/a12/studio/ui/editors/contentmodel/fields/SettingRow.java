package de.a12.studio.ui.editors.contentmodel.fields;

import de.a12.studio.models.contentmodel.ContentProps;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.jspecify.annotations.NonNull;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Consumer;

/**
 * One labeled setting of a Content Model element, bound to one {@link #getPath() path} in the element's
 * {@code props} (like SME's setting panel rows). The row shows the element's current value through {@link
 * #show(ContentProps)} and writes user edits straight back into the same {@link ContentProps}, then tells its
 * listener via {@link #setOnEdit(Runnable)} so the owner can save. Declared in FXML with {@code path}, {@code label}
 * and optionally {@code types}, the comma separated element types the row applies to (all types when empty).
 */
public abstract class SettingRow extends VBox {

  /** Width of the label column; rows that add sub-rows via {@link #addBelow} use it to keep their inputs aligned. */
  static final double LABEL_WIDTH = 118;
  static final double LINE_SPACING = 8;

  private final Label labelNode = new Label();
  private final HBox line = new HBox(LINE_SPACING);
  private final HBox controls = new HBox(6);

  private String path;
  private Set<String> types = Set.of();
  private String showWhen;
  private String enabledWhen;
  private ContentProps target;
  private Runnable onEdit = () -> {
  };
  private boolean loading;

  protected SettingRow() {
    getStyleClass().add("content-setting-row");
    labelNode.getStyleClass().add("content-setting-label");
    labelNode.setMinWidth(LABEL_WIDTH);
    labelNode.setPrefWidth(LABEL_WIDTH);
    labelNode.setWrapText(true);
    setLabel(null);
    controls.setAlignment(Pos.CENTER_LEFT);
    HBox.setHgrow(controls, Priority.ALWAYS);
    line.setAlignment(Pos.CENTER_LEFT);
    line.getChildren().addAll(labelNode, controls);
    getChildren().add(line);
    setPadding(new Insets(2, 0, 2, 0));
  }

  /** The area right of the label that subclasses put their editing controls into. */
  protected HBox controls() {
    return controls;
  }

  /** Adds {@code node} below the label line, spanning the full row width (e.g. the per-side fields of a spacing). */
  protected void addBelow(@NonNull Node node) {
    getChildren().add(node);
  }

  public String getLabel() {
    return labelNode.getText();
  }

  public void setLabel(String label) {
    labelNode.setText(label);
    // An empty label (e.g. a full-width text area) gives the whole line to the control.
    boolean hasLabel = label != null && !label.isBlank();
    labelNode.setVisible(hasLabel);
    labelNode.setManaged(hasLabel);
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public String getTypes() {
    return String.join(",", types);
  }

  public void setTypes(String types) {
    Set<String> parsed = new LinkedHashSet<>();
    if (types != null) {
      Arrays.stream(types.split(",")).map(String::trim).filter(type -> !type.isEmpty()).forEach(parsed::add);
    }
    this.types = parsed;
  }

  public boolean appliesTo(String type) {
    return types.isEmpty() || types.contains(type);
  }

  public String getShowWhen() {
    return showWhen;
  }

  /** A condition on the element's props ({@link #evaluate}) that must hold for the row to be shown. */
  public void setShowWhen(String showWhen) {
    this.showWhen = showWhen;
  }

  public String getEnabledWhen() {
    return enabledWhen;
  }

  /** A condition on the element's props ({@link #evaluate}) that must hold for the row to be enabled. */
  public void setEnabledWhen(String enabledWhen) {
    this.enabledWhen = enabledWhen;
  }

  public boolean isShownFor(@NonNull ContentProps props) {
    return evaluate(showWhen, props);
  }

  public boolean isEnabledFor(@NonNull ContentProps props) {
    return evaluate(enabledWhen, props);
  }

  /**
   * Evaluates a row condition: empty is always true; {@code "path"} is true when the value at the path is present and
   * not blank; {@code "path=a|b"} is true when it equals one of the alternatives (an absent value never does).
   */
  static boolean evaluate(String condition, @NonNull ContentProps props) {
    if (condition == null || condition.isBlank()) {
      return true;
    }
    int equals = condition.indexOf('=');
    if (equals < 0) {
      String value = props.getString(condition.trim());
      return value != null && !value.isBlank();
    }
    String value = props.getString(condition.substring(0, equals).trim());
    return value != null && Arrays.asList(condition.substring(equals + 1).split("\\|")).contains(value);
  }

  public void setOnEdit(@NonNull Runnable onEdit) {
    this.onEdit = onEdit;
  }

  /** Shows {@code props} in this row. Programmatic: never reported as a user edit. */
  public final void show(@NonNull ContentProps props) {
    loading = true;
    try {
      target = props;
      load(props);
    }
    finally {
      loading = false;
    }
  }

  /** Drops the reference to the shown element, e.g. when no element is selected any more. */
  public final void clear() {
    target = null;
  }

  /** Repopulates the row's controls from {@code props}. Called with edit notifications suppressed. */
  protected abstract void load(@NonNull ContentProps props);

  /** Whether the row is currently being repopulated from the model, i.e. control changes are not user edits. */
  protected boolean isLoading() {
    return loading;
  }

  /**
   * Applies a user edit: runs {@code write} against the shown element and notifies the owner. Ignored while
   * loading or when no element is shown.
   */
  protected final void edited(@NonNull Consumer<ContentProps> write) {
    if (loading || target == null) {
      return;
    }
    write.accept(target);
    onEdit.run();
  }
}
