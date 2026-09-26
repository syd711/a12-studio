package de.a12.studio.ui.editors.contentmodel.fields;

import de.a12.studio.models.contentmodel.ContentProps;
import de.a12.studio.ui.util.StudioBundle;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;

/**
 * A CSS shorthand setting with one value or one value per side (Padding, Margin) or per corner (Border Radius),
 * SME's "oriented" setting. The dropdown offers the units (and keywords such as {@code auto} for margins) plus
 * "Mixed"; choosing Mixed opens four fields, top/right/bottom/left ({@code corners="true"}: top-left/top-right/
 * bottom-right/bottom-left), and the value is then written as the four-value shorthand exactly like SME does. A
 * shorthand with 2 or 3 values loads as its 4-value expansion.
 */
public class SpacingRow extends SettingRow {

  private static final String MIXED = "Mixed";
  private static final String[] EDGE_KEYS = {"content_settings.edge.top", "content_settings.edge.right",
      "content_settings.edge.bottom", "content_settings.edge.left"};
  private static final String[] CORNER_KEYS = {"content_settings.corner.top_left", "content_settings.corner.top_right",
      "content_settings.corner.bottom_right", "content_settings.corner.bottom_left"};

  private final LengthEditor simple = new LengthEditor();
  private final VBox sidesBox = new VBox(4);
  private final List<LengthEditor> sides = new ArrayList<>();
  private final List<Label> sideLabels = new ArrayList<>();

  private String keywords = "";
  private String units = "px:0,%:0,rem:0";
  private boolean corners;
  private boolean mixed;
  private String lastSimple = "0px";

  public SpacingRow() {
    HBox.setHgrow(simple, Priority.ALWAYS);
    controls().getChildren().add(simple);
    sidesBox.setPadding(new Insets(4, 0, 2, 0));
    sidesBox.setVisible(false);
    sidesBox.setManaged(false);
    for (int i = 0; i < 4; i++) {
      Label sideLabel = new Label();
      // Same column width and gap as the label line above so the four fields line up with the main input and the
      // label text starts at the same left edge as the row's own label.
      sideLabel.setMinWidth(LABEL_WIDTH);
      sideLabel.setPrefWidth(LABEL_WIDTH);
      LengthEditor side = new LengthEditor();
      HBox.setHgrow(side, Priority.ALWAYS);
      side.setOnValue(value -> writeSides());
      HBox line = new HBox(LINE_SPACING, sideLabel, side);
      line.setAlignment(Pos.CENTER_LEFT);
      sideLabels.add(sideLabel);
      sides.add(side);
      sidesBox.getChildren().add(line);
    }
    addBelow(sidesBox);
    simple.setOnValue(this::onSimpleValue);
    simple.setOnExtra(extra -> onMixedChosen());
    reconfigure();
  }

  public String getKeywords() {
    return keywords;
  }

  public void setKeywords(String keywords) {
    this.keywords = keywords;
    reconfigure();
  }

  public String getUnits() {
    return units;
  }

  public void setUnits(String units) {
    this.units = units;
    reconfigure();
  }

  public boolean isCorners() {
    return corners;
  }

  public void setCorners(boolean corners) {
    this.corners = corners;
    reconfigure();
  }

  private void reconfigure() {
    List<String> keywordList = LengthEditor.parseKeywords(keywords);
    List<LengthEditor.Unit> unitList = LengthEditor.parseUnits(units);
    simple.configure(keywordList, unitList, true, List.of(MIXED));
    String[] keys = corners ? CORNER_KEYS : EDGE_KEYS;
    for (int i = 0; i < sides.size(); i++) {
      sides.get(i).configure(keywordList, unitList, false, List.of());
      sideLabels.get(i).setText(StudioBundle.get(keys[i]));
    }
  }

  private void onSimpleValue(String value) {
    setMixed(false);
    if (value != null) {
      lastSimple = value;
    }
    edited(props -> props.set(getPath(), value));
  }

  private void onMixedChosen() {
    setMixed(true);
    for (LengthEditor side : sides) {
      side.setValue(lastSimple);
    }
    writeSides();
  }

  private void writeSides() {
    List<String> values = sides.stream().map(LengthEditor::getValue).toList();
    if (values.contains(null)) {
      return;
    }
    String shorthand = String.join(" ", values);
    edited(props -> props.set(getPath(), shorthand));
  }

  private void setMixed(boolean mixed) {
    this.mixed = mixed;
    sidesBox.setVisible(mixed);
    sidesBox.setManaged(mixed);
  }

  @Override
  protected void load(@NonNull ContentProps props) {
    String raw = props.getString(getPath());
    String[] tokens = raw == null || raw.isBlank() ? new String[0] : raw.trim().split("\\s+");
    if (tokens.length <= 1) {
      setMixed(false);
      simple.setValue(tokens.length == 0 ? null : tokens[0]);
      if (tokens.length == 1) {
        lastSimple = tokens[0];
      }
      return;
    }
    String[] expanded = expand(tokens);
    setMixed(true);
    simple.selectExtra(MIXED);
    for (int i = 0; i < sides.size(); i++) {
      sides.get(i).setValue(expanded[i]);
    }
  }

  /** CSS shorthand expansion: 2 values are (vertical horizontal), 3 are (top horizontal bottom). */
  private static String[] expand(String[] tokens) {
    return switch (tokens.length) {
      case 2 -> new String[]{tokens[0], tokens[1], tokens[0], tokens[1]};
      case 3 -> new String[]{tokens[0], tokens[1], tokens[2], tokens[1]};
      default -> new String[]{tokens[0], tokens[1], tokens[2], tokens[3]};
    };
  }

  boolean isMixed() {
    return mixed;
  }
}
