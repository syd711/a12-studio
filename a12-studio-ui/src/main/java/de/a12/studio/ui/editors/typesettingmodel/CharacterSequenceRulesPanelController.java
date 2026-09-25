package de.a12.studio.ui.editors.typesettingmodel;

import de.a12.studio.models.typesettingmodel.PreventLineBreakRule;
import de.a12.studio.models.typesettingmodel.PreventLineBreakRuleType;
import de.a12.studio.models.typesettingmodel.PreventLineBreakRules;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import org.jspecify.annotations.NonNull;

/**
 * "Character Sequence Rules": words or character sequences the print engine must not break across lines, e.g.
 * {@code T-shirt}. Each row is the plain sequence (letters and hyphens, validated by {@code
 * TypesettingRuleValueValidator}); it is stored regex-escaped.
 */
public class CharacterSequenceRulesPanelController extends AbstractRulesPanelController {

  @Override
  protected PreventLineBreakRuleType ruleType() {
    return PreventLineBreakRuleType.CHARACTER_SEQUENCE;
  }

  @Override
  protected Node createValueControl(@NonNull PreventLineBreakRule rule) {
    // Set the initial value before attaching the listener, so showing it is never mistaken for an edit.
    TextField valueField = new TextField(PreventLineBreakRules.toValue(ruleType(), rule.getPattern()));
    valueField.setPromptText("T-shirt, E-mail...");
    valueField.setMaxWidth(Double.MAX_VALUE);
    valueField.textProperty().addListener((observable, oldValue, newValue) -> onValueEdited(rule, newValue));
    return valueField;
  }
}
