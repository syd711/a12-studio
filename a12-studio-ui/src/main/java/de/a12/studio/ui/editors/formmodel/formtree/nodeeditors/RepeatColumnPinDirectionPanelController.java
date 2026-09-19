package de.a12.studio.ui.editors.formmodel.formtree.nodeeditors;

import de.a12.studio.models.formmodel.RepeatOverviewColumn;
import de.a12.studio.ui.editors.AbstractPropertyEditor;
import de.a12.studio.ui.util.StudioBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.util.StringConverter;
import org.jspecify.annotations.NonNull;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * "Pin Direction" property editor for a selected {@link RepeatOverviewColumn}: whether the column stays fixed
 * at the left or right edge of the overview table while the rest scrolls ({@link
 * RepeatOverviewColumn#getPinDirection()}, {@code "LEFT"}/{@code "RIGHT"} on the wire; absent = not pinned).
 * Not tied to a document-model {@code Element}, so it follows the model-header pattern: a plain {@link
 * #setColumn} entry point and {@code bindComboBox}'s built-in commit.
 */
public class RepeatColumnPinDirectionPanelController extends AbstractPropertyEditor implements Initializable {

  @FXML
  private ComboBox<String> pinDirectionCombo;

  private RepeatOverviewColumn column;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    super.initialize(location, resources);

    pinDirectionCombo.getItems().setAll(null, "LEFT", "RIGHT");
    pinDirectionCombo.setConverter(new StringConverter<>() {
      @Override
      public String toString(String value) {
        return switch (value == null ? "" : value) {
          case "LEFT" -> StudioBundle.get("repeat_column_pin_left");
          case "RIGHT" -> StudioBundle.get("repeat_column_pin_right");
          default -> StudioBundle.get("repeat_column_pin_none");
        };
      }

      @Override
      public String fromString(String displayName) {
        if (StudioBundle.get("repeat_column_pin_left").equals(displayName)) {
          return "LEFT";
        }
        return StudioBundle.get("repeat_column_pin_right").equals(displayName) ? "RIGHT" : null;
      }
    });
    bindComboBox(pinDirectionCombo, (el, value) -> column.setPinDirection(value));
  }

  public void setColumn(@NonNull RepeatOverviewColumn column) {
    this.column = column;
    setFieldValue(pinDirectionCombo, column.getPinDirection());
  }
}
