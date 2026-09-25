package de.a12.studio.ui.editors.typesettingmodel;

import de.a12.studio.models.typesettingmodel.PreventLineBreakRule;
import de.a12.studio.models.typesettingmodel.PreventLineBreakRuleType;
import de.a12.studio.models.typesettingmodel.PreventLineBreakRules;
import de.a12.studio.models.typesettingmodel.SpecialPattern;
import de.a12.studio.ui.util.StudioBundle;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.util.StringConverter;
import org.jspecify.annotations.NonNull;

/**
 * "Special Pattern Rules": curated patterns for text the print engine must keep together, chosen from the fixed
 * list the print engine team supports (see {@link PreventLineBreakRules#SPECIAL_PATTERNS}), e.g. a
 * {@code 219(1)(a)} section reference. There is no free input: a pattern outside the list cannot be represented.
 */
public class SpecialPatternRulesPanelController extends AbstractRulesPanelController {

  @Override
  protected PreventLineBreakRuleType ruleType() {
    return PreventLineBreakRuleType.SPECIAL_PATTERN;
  }

  @Override
  protected Node createValueControl(@NonNull PreventLineBreakRule rule) {
    ComboBox<SpecialPattern> patternCombo = new ComboBox<>();
    patternCombo.getItems().setAll(PreventLineBreakRules.SPECIAL_PATTERNS);
    patternCombo.setPromptText(StudioBundle.get("typesetting_model.select_pattern"));
    patternCombo.setMaxWidth(Double.MAX_VALUE);
    patternCombo.setConverter(new StringConverter<>() {
      @Override
      public String toString(SpecialPattern pattern) {
        return pattern == null ? "" : pattern.label();
      }

      @Override
      public SpecialPattern fromString(String label) {
        return null;
      }
    });
    // Each entry also shows an example of the text it matches.
    patternCombo.setCellFactory(list -> new ListCell<>() {
      @Override
      protected void updateItem(SpecialPattern pattern, boolean empty) {
        super.updateItem(pattern, empty);
        setText(empty || pattern == null ? null : pattern.label() + "   (" + pattern.example() + ")");
      }
    });

    // Set the initial selection before attaching the listener, so showing it is never mistaken for an edit.
    patternCombo.setValue(PreventLineBreakRules.findSpecialByRegex(rule.getPattern()));
    patternCombo.valueProperty().addListener((observable, oldValue, newValue) -> {
      if (newValue != null) {
        onValueEdited(rule, newValue.label());
      }
    });
    return patternCombo;
  }
}
