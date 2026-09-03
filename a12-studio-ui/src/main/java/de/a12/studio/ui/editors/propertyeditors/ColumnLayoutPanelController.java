package de.a12.studio.ui.editors.propertyeditors;

import de.a12.studio.ui.editors.AbstractPropertyEditor;
import de.a12.studio.ui.util.StudioBundle;
import de.a12.studio.ui.util.WidgetFactory;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Edits a single {@code lg} (base/default breakpoint) column layout string, e.g. {@link
 * de.a12.studio.models.formmodel.ColumnLayout#getLg()}. Not bound to a single {@link
 * de.a12.studio.models.documentmodel.Element} - the value is read/written via a caller-supplied {@code
 * Supplier}/{@code Consumer} pair (see {@link #setCustom}), mirroring {@link
 * de.a12.studio.ui.editors.formmodel.NamePanelController}. Shared across model editors (per the project's
 * package-placement convention), unlike {@link de.a12.studio.ui.editors.formmodel.FlexLayoutPanelController}
 * (which edits the same kind of field but is bound directly to a {@code MultiColumnSection} and carries a
 * "required" tooltip, whereas this field defaults to "12" when left empty).
 *
 * <p>Renders as an editable {@link ComboBox} pre-populated with common two- and three-column layout
 * presets. Each dropdown item shows a small proportional preview of the column widths alongside the
 * layout string so modellers can pick the right grid at a glance.
 */
public class ColumnLayoutPanelController extends AbstractPropertyEditor implements Initializable {

  /** Common layout presets: two- and three-column distributions summing to ≤ 12. */
  private static final List<String> LAYOUT_PRESETS = List.of(
      // 2-column layouts
      "6-6",
      "4-8",
      "8-4",
      "3-9",
      "9-3",
      // 3-column layouts
      "4-4-4",
      "3-6-3",
      "3-3-6",
      "6-3-3",
      "2-8-2"
  );

  @FXML
  private ComboBox<String> layoutCombo;

  @FXML
  private Label layoutInfoIcon;

  private Consumer<String> writer;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    super.initialize(location, resources);
    WidgetFactory.createHelpIcon(layoutInfoIcon, StudioBundle.get("control_grid_layout_tooltip"));

    layoutCombo.setEditable(true);
    layoutCombo.getItems().setAll(LAYOUT_PRESETS);
    layoutCombo.setCellFactory(lv -> new LayoutPreviewCell());

    // Bind the ComboBox's internal editor TextField through AbstractPropertyEditor's bindTextField so
    // the existing debounce/updatingFromModel guard is shared without duplicating the logic.
    bindTextField(layoutCombo.getEditor(), (el, value) -> writer.accept(value.isEmpty() ? null : value));
  }

  public void setCustom(@NonNull Supplier<String> reader, @NonNull Consumer<String> writer) {
    this.writer = writer;
    // Use setFieldValue(TextField, ...) on the editor – same guard used by the base class.
    setFieldValue(layoutCombo.getEditor(), reader.get());
  }

  // ── Preview cell ─────────────────────────────────────────────────────────────

  /**
   * A {@link ListCell} that renders a small proportional column-preview canvas to the left of the
   * layout string. Falls back to plain text for values that cannot be parsed as a dash-separated
   * list of integers.
   */
  private static final class LayoutPreviewCell extends ListCell<String> {

    private static final double CANVAS_WIDTH  = 54;
    private static final double CANVAS_HEIGHT = 14;
    private static final double GAP           = 1.5;
    private static final double CORNER        = 2;

    private static final Color[] PALETTE = {
        Color.web("#5B9BD5"),   // blue
        Color.web("#70AD47"),   // green
        Color.web("#ED7D31")    // orange
    };

    private final Canvas canvas = new Canvas(CANVAS_WIDTH, CANVAS_HEIGHT);
    private final Label  label  = new Label();
    private final HBox   box    = new HBox(6, canvas, label);

    LayoutPreviewCell() {
      box.setPadding(new Insets(1, 0, 1, 0));
      label.setStyle("-fx-font-size: 11px;");
      setGraphic(box);
    }

    @Override
    protected void updateItem(@Nullable String item, boolean empty) {
      super.updateItem(item, empty);
      if (empty || item == null) {
        setText(null);
        setGraphic(null);
        return;
      }
      setText(null);
      label.setText(item);
      drawPreview(item);
      setGraphic(box);
    }

    private void drawPreview(String layout) {
      GraphicsContext gc = canvas.getGraphicsContext2D();
      gc.clearRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);

      int[] cols = parseLayout(layout);
      if (cols == null || cols.length == 0) {
        return;
      }
      int total = 0;
      for (int c : cols) total += c;
      if (total <= 0) {
        return;
      }

      double x = 0;
      double availWidth = CANVAS_WIDTH - GAP * (cols.length - 1);
      for (int i = 0; i < cols.length; i++) {
        double w = availWidth * cols[i] / total;
        gc.setFill(PALETTE[i % PALETTE.length]);
        gc.fillRoundRect(x, 0, w, CANVAS_HEIGHT, CORNER, CORNER);
        x += w + GAP;
      }
    }

    /** Parses {@code "4-4-4"} → {@code [4, 4, 4]}, returns {@code null} on any parse error. */
    private static int @Nullable [] parseLayout(@NonNull String layout) {
      String[] parts = layout.split("-");
      int[] result = new int[parts.length];
      for (int i = 0; i < parts.length; i++) {
        try {
          result[i] = Integer.parseInt(parts[i].trim());
        } catch (NumberFormatException e) {
          return null;
        }
      }
      return result;
    }
  }
}
