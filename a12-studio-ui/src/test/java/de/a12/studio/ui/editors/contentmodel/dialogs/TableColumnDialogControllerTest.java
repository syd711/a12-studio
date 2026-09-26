package de.a12.studio.ui.editors.contentmodel.dialogs;

import de.a12.studio.ui.editors.formmodel.FxTestSupport;
import de.a12.studio.ui.util.StudioBundle;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class TableColumnDialogControllerTest {

  private static final String FXML = "/de/a12/studio/ui/editors/contentmodel/dialogs/table-column-dialog.fxml";

  @Test
  void widthsFollowSmesRule() {
    assertNull(TableColumnDialogController.parseWidth(null));
    assertNull(TableColumnDialogController.parseWidth("  "));
    assertEquals(1, TableColumnDialogController.parseWidth("1"), "a whole number stays an integer");
    assertEquals(1, TableColumnDialogController.parseWidth("1.0"));
    assertEquals(2.5, TableColumnDialogController.parseWidth(" 2.55 "), "rounded down to one decimal");
    assertEquals(0, TableColumnDialogController.parseWidth("0"));
    for (String invalid : new String[]{"-1", "abc", "NaN", "Infinity", "1,5"}) {
      assertThrows(NumberFormatException.class, () -> TableColumnDialogController.parseWidth(invalid), invalid);
      assertFalse(TableColumnDialogController.isValidWidth(invalid), invalid);
    }
    assertTrue(TableColumnDialogController.isValidWidth(""));
  }

  @Test
  void theDialogShowsAllSettingsOfAColumnAndReturnsTheEditedCopy() throws Exception {
    assumeTrue(FxTestSupport.startToolkit(), "No JavaFX toolkit available");
    FxTestSupport.Loaded<TableColumnDialogController> loaded = FxTestSupport.load(FXML);
    TableColumnDialogController controller = loaded.controller();
    Stage stage = FxTestSupport.onFx(() -> new Stage());

    Map<String, Object> column = new LinkedHashMap<>();
    column.put("id", "c1");
    column.put("width", 1.5);
    column.put("pinning", "left");
    column.put("unknownKey", "kept");
    column.put("horizontalAlignment", Map.of("head", "center"));
    FxTestSupport.onFx(() -> controller.init(stage, column, true));

    ComboBox<String> pinning = FxTestSupport.field(controller, "pinningCombo");
    TextField width = FxTestSupport.field(controller, "widthField");
    TextField minWidth = FxTestSupport.field(controller, "minWidthField");
    VBox minWidthBox = FxTestSupport.field(controller, "minWidthBox");
    CheckBox fixed = FxTestSupport.field(controller, "fixedWidthCheck");
    CheckBox action = FxTestSupport.field(controller, "actionColumnCheck");
    GridPane horizontal = FxTestSupport.field(controller, "horizontalGrid");
    GridPane vertical = FxTestSupport.field(controller, "verticalGrid");
    Label error = FxTestSupport.field(controller, "errorLabel");
    Button ok = FxTestSupport.field(controller, "okButton");

    assertEquals("left", FxTestSupport.onFx(() -> pinning.getValue()));
    assertEquals("1.5", FxTestSupport.onFx(() -> width.getText()));
    assertTrue(FxTestSupport.onFx(() -> minWidthBox.isManaged()), "resizing is on, so the minimum width is offered");
    assertEquals(4, FxTestSupport.onFx(() -> horizontal.getChildren().size()), "general, head, body, foot");
    assertEquals(4, FxTestSupport.onFx(() -> vertical.getChildren().size()));
    assertEquals("center", alignment(horizontal, 1));

    // An invalid width names the field and blocks OK
    FxTestSupport.onFx(() -> width.setText("wide"));
    assertTrue(FxTestSupport.onFx(() -> error.isVisible()));
    assertEquals(StudioBundle.get("content_settings.column_error_width"), FxTestSupport.onFx(() -> error.getText()));
    assertTrue(FxTestSupport.onFx(() -> ok.isDisabled()));
    FxTestSupport.onFx(() -> minWidth.setText("-2"));
    FxTestSupport.onFx(() -> width.setText("3.79"));
    assertEquals(StudioBundle.get("content_settings.column_error_min_width"), FxTestSupport.onFx(() -> error.getText()));

    FxTestSupport.onFx(() -> {
      minWidth.setText("0.5");
      fixed.setSelected(true);
      action.setSelected(true);
      pinning.setValue("none");
      alignmentCombo(vertical, 2).setValue("bottom");
      alignmentCombo(horizontal, 1).setValue("auto");
    });
    assertFalse(FxTestSupport.onFx(() -> ok.isDisabled()));
    submit(controller);

    Map<String, Object> result = controller.getResult();
    assertEquals("c1", result.get("id"));
    assertEquals("kept", result.get("unknownKey"), "keys the dialog does not know stay");
    assertEquals(3.7, result.get("width"));
    assertEquals(0.5, result.get("minResizeWidth"));
    assertEquals(Boolean.TRUE, result.get("fixedWidth"));
    assertEquals(Boolean.TRUE, result.get("actionColumn"));
    assertFalse(result.containsKey("pinning"), "none removes the key");
    assertFalse(result.containsKey("horizontalAlignment"), "all areas automatic removes the key");
    assertEquals(Map.of("body", "bottom"), result.get("verticalAlignment"));
    assertEquals(1.5, column.get("width"), "the edited column itself is not touched");
    assertEquals("left", column.get("pinning"));
  }

  @Test
  void theMinimumWidthIsOnlyOfferedWhenResizingIsEnabledAndThenLeftAlone() throws Exception {
    assumeTrue(FxTestSupport.startToolkit(), "No JavaFX toolkit available");
    FxTestSupport.Loaded<TableColumnDialogController> loaded = FxTestSupport.load(FXML);
    TableColumnDialogController controller = loaded.controller();
    Stage stage = FxTestSupport.onFx(() -> new Stage());
    Map<String, Object> column = new LinkedHashMap<>(Map.of("id", "c1", "minResizeWidth", 2));
    FxTestSupport.onFx(() -> controller.init(stage, column, false));

    VBox minWidthBox = FxTestSupport.field(controller, "minWidthBox");
    assertFalse(FxTestSupport.onFx(() -> minWidthBox.isManaged()));
    submit(controller);
    assertEquals(2, controller.getResult().get("minResizeWidth"), "a hidden field must not delete the stored value");
  }

  @SuppressWarnings("unchecked")
  private static ComboBox<String> alignmentCombo(GridPane grid, int areaIndex) {
    VBox cell = (VBox) grid.getChildren().get(areaIndex);
    return (ComboBox<String>) cell.getChildren().get(1);
  }

  private static String alignment(GridPane grid, int areaIndex) throws Exception {
    return FxTestSupport.onFx(() -> alignmentCombo(grid, areaIndex).getValue());
  }

  private static void submit(TableColumnDialogController controller) throws Exception {
    Method method = TableColumnDialogController.class.getDeclaredMethod("onDialogSubmit");
    method.setAccessible(true);
    FxTestSupport.onFx(() -> {
      method.invoke(controller);
      return null;
    });
  }
}
