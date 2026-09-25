package de.a12.studio.ui.editors.typesettingmodel;

import de.a12.studio.models.typesettingmodel.PreventLineBreakRule;
import de.a12.studio.models.typesettingmodel.PreventLineBreakRuleType;
import de.a12.studio.models.typesettingmodel.PreventLineBreakRules;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import org.jspecify.annotations.NonNull;

/**
 * "Number Unit Rules": units of measurement that stay on one line with the number in front of them (a "12 Km"
 * is never broken between "12" and "Km"). Each row is the plain unit (letters and unit symbols such as % $,
 * validated by {@code TypesettingRuleValueValidator}); it is stored as the number-unit regex plus the escaped unit.
 */
public class NumberUnitRulesPanelController extends AbstractRulesPanelController {

  @Override
  protected PreventLineBreakRuleType ruleType() {
    return PreventLineBreakRuleType.NUMBER_UNIT;
  }

  @Override
  protected Node createValueControl(@NonNull PreventLineBreakRule rule) {
    TextField valueField = new TextField(PreventLineBreakRules.toValue(ruleType(), rule.getPattern()));
    valueField.setPromptText("Km, %, $, €...");
    valueField.setMaxWidth(Double.MAX_VALUE);
    valueField.textProperty().addListener((observable, oldValue, newValue) -> onValueEdited(rule, newValue));
    return valueField;
  }
}
