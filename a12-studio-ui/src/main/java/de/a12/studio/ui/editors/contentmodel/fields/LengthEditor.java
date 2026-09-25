package de.a12.studio.ui.editors.contentmodel.fields;

import de.a12.studio.ui.util.StudioBundle;
import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.util.StringConverter;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The value control of SME's length settings: a dropdown of keywords ({@code auto}, {@code fit-content}, ...) and
 * numeric units ({@code px}, {@code %}, {@code rem}) to the right of a number field that is shown while a unit is chosen.
 * The value is the CSS text ({@code "100%"}, {@code "auto"}) or {@code null} for "unspecified" (the key is then
 * omitted from the props, like SME does). A value that fits none of the configured keywords/units (say
 * {@code calc(...)}) is kept as-is under "Custom" and shown in the field so it is never silently rewritten.
 */
public class LengthEditor extends HBox {

  /** A numeric unit and the number used when the user switches to it. */
  public record Unit(@NonNull String name, double defaultValue) {
  }

  private record Choice(String id, String label) {
  }

  private static final String UNSPECIFIED = "::unspecified";
  private static final String CUSTOM = "::custom";
  private static final String ERROR_STYLE = "content-setting-invalid";
  private static final double CHOICES_WIDTH = 110;

  private final ComboBox<Choice> choices = new ComboBox<>();
  private final TextField number = new TextField();

  private List<String> keywords = List.of();
  private List<Unit> units = List.of();
  private List<Choice> extras = List.of();
  private boolean allowUnspecified = true;
  private Pattern valuePattern = Pattern.compile("^$");

  private boolean updating;
  private Consumer<String> onValue = value -> {
  };
  private Consumer<String> onExtra = id -> {
  };

  public LengthEditor() {
    super(4);
    setAlignment(Pos.CENTER_LEFT);
    choices.setConverter(new StringConverter<>() {
      @Override
      public String toString(Choice choice) {
        return choice == null ? "" : choice.label();
      }

      @Override
      public Choice fromString(String string) {
        return null;
      }
    });
    // A fixed dropdown width keeps the dropdowns of all rows the same size whether or not the number field is shown;
    // the number field takes the remaining space.
    choices.setMinWidth(CHOICES_WIDTH);
    choices.setPrefWidth(CHOICES_WIDTH);
    choices.setMaxWidth(CHOICES_WIDTH);
    number.setPrefColumnCount(5);
    number.setMinWidth(56);
    number.setMaxWidth(Double.MAX_VALUE);
    number.setAlignment(Pos.CENTER_RIGHT);
    HBox.setHgrow(number, Priority.ALWAYS);
    getChildren().addAll(number, choices);

    choices.valueProperty().addListener((observable, oldValue, choice) -> {
      if (!updating && choice != null) {
        onChoice(choice);
      }
    });
    number.textProperty().addListener((observable, oldValue, text) -> {
      if (!updating) {
        onNumberTyped(text);
      }
    });
    configure(List.of(), List.of(), true, List.of());
  }

  /**
   * Sets what the dropdown offers: the {@code keywords} first, then the numeric {@code units}, then {@code extras}
   * (entries that do not carry a value, reported through {@link #setOnExtra}, e.g. "Mixed").
   */
  public void configure(@NonNull List<String> keywords, @NonNull List<Unit> units, boolean allowUnspecified,
      @NonNull List<String> extras) {
    this.keywords = List.copyOf(keywords);
    this.units = List.copyOf(units);
    this.allowUnspecified = allowUnspecified;
    this.extras = extras.stream().map(extra -> new Choice(extra, extra)).toList();
    String unitAlternatives = String.join("|", units.stream().map(unit -> Pattern.quote(unit.name())).toList());
    valuePattern = units.isEmpty()
        ? Pattern.compile("^$")
        : Pattern.compile("^\\s*(-?(?:\\d+\\.?\\d*|\\.\\d+))\\s*(" + unitAlternatives + ")\\s*$");
    rebuildChoices();
  }

  private void rebuildChoices() {
    updating = true;
    try {
      List<Choice> items = new ArrayList<>();
      if (allowUnspecified) {
        items.add(new Choice(UNSPECIFIED, StudioBundle.get("content_settings.unspecified")));
      }
      keywords.forEach(keyword -> items.add(new Choice(keyword, capitalize(keyword))));
      units.forEach(unit -> items.add(new Choice(unit.name(), unit.name())));
      items.addAll(extras);
      choices.getItems().setAll(items);
    }
    finally {
      updating = false;
    }
  }

  public void setOnValue(@NonNull Consumer<String> onValue) {
    this.onValue = onValue;
  }

  public void setOnExtra(@NonNull Consumer<String> onExtra) {
    this.onExtra = onExtra;
  }

  /** Shows {@code value} without reporting it as an edit. {@code null} shows "unspecified". */
  public void setValue(@Nullable String value) {
    updating = true;
    try {
      if (value == null || value.isBlank()) {
        select(allowUnspecified ? UNSPECIFIED : (keywords.isEmpty() ? null : keywords.get(0)), "");
        return;
      }
      String trimmed = value.trim();
      if (keywords.contains(trimmed)) {
        select(trimmed, "");
        return;
      }
      Matcher matcher = valuePattern.matcher(trimmed);
      if (matcher.matches()) {
        select(matcher.group(2), format(Double.parseDouble(matcher.group(1))));
        return;
      }
      if ("0".equals(trimmed) && !units.isEmpty()) {
        select(units.get(0).name(), "0");
        return;
      }
      addCustomChoice();
      select(CUSTOM, trimmed);
    }
    finally {
      updating = false;
    }
  }

  /** Puts the dropdown on one of the configured extras without reporting it (e.g. "Mixed"). */
  public void selectExtra(@NonNull String extra) {
    updating = true;
    try {
      select(extra, "");
    }
    finally {
      updating = false;
    }
  }

  /** The current CSS value, or {@code null} when unspecified or not (yet) valid. */
  public @Nullable String getValue() {
    Choice choice = choices.getValue();
    if (choice == null || UNSPECIFIED.equals(choice.id()) || isExtra(choice.id())) {
      return null;
    }
    if (CUSTOM.equals(choice.id())) {
      return number.getText().isBlank() ? null : number.getText().trim();
    }
    if (keywords.contains(choice.id())) {
      return choice.id();
    }
    Double parsed = parse(number.getText());
    return parsed == null ? null : format(parsed) + choice.id();
  }

  private void select(String id, String text) {
    Choice choice = choices.getItems().stream().filter(item -> item.id().equals(id)).findFirst().orElse(null);
    choices.setValue(choice);
    number.setText(text);
    number.getStyleClass().remove(ERROR_STYLE);
    updateNumberVisibility(choice);
  }

  private void addCustomChoice() {
    if (choices.getItems().stream().noneMatch(item -> CUSTOM.equals(item.id()))) {
      choices.getItems().add(new Choice(CUSTOM, StudioBundle.get("content_settings.custom")));
    }
  }

  private void updateNumberVisibility(Choice choice) {
    boolean visible = choice != null && (CUSTOM.equals(choice.id()) || isUnit(choice.id()));
    // Stays managed so it keeps its space while hidden and the dropdowns of all rows line up on the right.
    number.setVisible(visible);
  }

  private void onChoice(Choice choice) {
    updateNumberVisibility(choice);
    number.getStyleClass().remove(ERROR_STYLE);
    if (isExtra(choice.id())) {
      onExtra.accept(choice.id());
      return;
    }
    if (UNSPECIFIED.equals(choice.id())) {
      onValue.accept(null);
      return;
    }
    if (keywords.contains(choice.id())) {
      onValue.accept(choice.id());
      return;
    }
    if (CUSTOM.equals(choice.id())) {
      onValue.accept(getValue());
      return;
    }
    units.stream().filter(unit -> unit.name().equals(choice.id())).findFirst().ifPresent(unit -> {
      updating = true;
      try {
        number.setText(format(unit.defaultValue()));
      }
      finally {
        updating = false;
      }
      onValue.accept(getValue());
    });
  }

  private void onNumberTyped(String text) {
    Choice choice = choices.getValue();
    if (choice == null) {
      return;
    }
    if (CUSTOM.equals(choice.id())) {
      onValue.accept(getValue());
      return;
    }
    boolean valid = parse(text) != null;
    number.getStyleClass().remove(ERROR_STYLE);
    if (!valid) {
      number.getStyleClass().add(ERROR_STYLE);
      return;
    }
    onValue.accept(getValue());
  }

  private boolean isUnit(String id) {
    return units.stream().anyMatch(unit -> unit.name().equals(id));
  }

  private boolean isExtra(String id) {
    return extras.stream().anyMatch(extra -> extra.id().equals(id));
  }

  private static Double parse(String text) {
    if (text == null || text.isBlank()) {
      return null;
    }
    try {
      double parsed = Double.parseDouble(text.trim());
      return Double.isFinite(parsed) ? parsed : null;
    }
    catch (NumberFormatException e) {
      return null;
    }
  }

  static String format(double value) {
    return BigDecimal.valueOf(value).stripTrailingZeros().toPlainString();
  }

  private static String capitalize(String keyword) {
    return keyword.isEmpty() ? keyword : keyword.substring(0, 1).toUpperCase(Locale.ROOT) + keyword.substring(1);
  }

  /** Parses {@code "px:400,%:100"} (unit and its default number) as used in FXML. */
  public static List<Unit> parseUnits(@Nullable String spec) {
    List<Unit> parsed = new ArrayList<>();
    if (spec == null || spec.isBlank()) {
      return parsed;
    }
    for (String entry : spec.split(",")) {
      String[] parts = entry.trim().split(":");
      double defaultValue = parts.length > 1 ? Double.parseDouble(parts[1].trim()) : 0;
      parsed.add(new Unit(parts[0].trim(), defaultValue));
    }
    return parsed;
  }

  /** Parses a comma separated keyword list as used in FXML. */
  public static List<String> parseKeywords(@Nullable String spec) {
    if (spec == null || spec.isBlank()) {
      return List.of();
    }
    return java.util.Arrays.stream(spec.split(",")).map(String::trim).filter(keyword -> !keyword.isEmpty()).toList();
  }
}
