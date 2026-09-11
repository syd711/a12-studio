package de.a12.studio.ui.editors.relationshipuimodel;

import de.a12.studio.ui.util.StudioBundle;
import javafx.scene.control.ComboBox;
import javafx.util.StringConverter;

import java.util.ArrayList;
import java.util.List;

/**
 * Shared setup for a {@code ComboBox<String>} that picks a sibling model's id and (for optional references
 * such as {@code linkFormModel}) can be cleared back to "no selection" via a leading empty-string entry
 * displayed as {@link de.a12.studio.ui.util.StudioBundle}'s {@code "none"}.
 */
final class ComboOptions {

  private ComboOptions() {
  }

  static List<String> withNoneOption(List<String> ids) {
    List<String> options = new ArrayList<>();
    options.add("");
    options.addAll(ids);
    return options;
  }

  static void installNoneConverter(ComboBox<String> comboBox) {
    comboBox.setConverter(new StringConverter<>() {
      @Override
      public String toString(String value) {
        return value == null || value.isEmpty() ? StudioBundle.get("none") : value;
      }

      @Override
      public String fromString(String string) {
        return string;
      }
    });
  }

  /** Normalizes the combo's current value: the synthetic empty-string "none" entry becomes {@code null}. */
  static String valueOrNull(ComboBox<String> comboBox) {
    String value = comboBox.getValue();
    return value == null || value.isEmpty() ? null : value;
  }
}
