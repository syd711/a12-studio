package de.a12.studio.modelsvalidation.validators.typesetting;

import de.a12.studio.models.typesettingmodel.PreventLineBreakRule;
import de.a12.studio.models.typesettingmodel.PreventLineBreakRuleType;
import de.a12.studio.models.typesettingmodel.PreventLineBreakRules;
import de.a12.studio.models.typesettingmodel.TypesettingModel;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * One {@link PreventLineBreakRule} as the editor shows it: {@code index} is its position in the file (what the
 * error's element id carries), {@code row} its 1-based row within the table of its {@code type} (what a message
 * shows the user, since the editor lists the three kinds in separate tables).
 */
record TypesettingRuleRow(int index, PreventLineBreakRuleType type, int row, String pattern) {

  /** The value the editor shows for this rule, see {@link PreventLineBreakRules#toValue}. */
  String value() {
    return PreventLineBreakRules.toValue(type, pattern);
  }

  static List<TypesettingRuleRow> of(TypesettingModel model) {
    List<TypesettingRuleRow> rows = new ArrayList<>();
    if (model.getContent() == null || model.getContent().getPreventLineBreakRules() == null) {
      return rows;
    }
    Map<PreventLineBreakRuleType, Integer> rowsPerType = new EnumMap<>(PreventLineBreakRuleType.class);
    List<PreventLineBreakRule> rules = model.getContent().getPreventLineBreakRules();
    for (int index = 0; index < rules.size(); index++) {
      String pattern = rules.get(index).getPattern();
      PreventLineBreakRuleType type = PreventLineBreakRules.classify(pattern);
      int row = rowsPerType.merge(type, 1, Integer::sum);
      rows.add(new TypesettingRuleRow(index, type, row, pattern));
    }
    return rows;
  }
}
