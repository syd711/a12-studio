package de.a12.studio.ui.editors.transformermodel;

import de.a12.studio.models.transformermodel.EnumLabel;
import de.a12.studio.modelsvalidation.validators.transformer.TransformerElementIds;
import de.a12.studio.ui.editors.propertyeditors.RowFactory;
import de.a12.studio.ui.editors.transformermodel.TransformationOutcome.EnumValue;
import de.a12.studio.ui.util.Icons;
import de.a12.studio.ui.util.StudioBundle;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.jspecify.annotations.NonNull;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 * The "Enumeration Display Texts" section of the Custom Texts tab ({@code content.EnumLabels}): the display text of an
 * enumeration value of the generated Document Model, one text per locale of the model. The value is suggested from the
 * XSD's enumeration values (SME's {@code enumValue}), the field path from the fields that value occurs in
 * ({@code enumFieldPathByValue}) and the type definition id from the type definitions it belongs to; any text is
 * accepted. A text applies to every enumeration with that value unless it is restricted to one type definition or one
 * field (never both).
 */
public class EnumLabelsPanelController extends TransformerModelPanelController {

  @FXML
  private VBox rows;

  @FXML
  private Label emptyLabel;

  private Map<String, EnumValue> enumValues = Map.of();

  /** The enumeration values of the XSD with where they occur (SME's discovered {@code enumValues}). */
  public void setEnumValues(@NonNull Map<String, EnumValue> enumValues) {
    this.enumValues = enumValues;
    refreshSuggestions();
  }

  @Override
  protected String errorIdPrefix() {
    return TransformerElementIds.ENUM_LABELS;
  }

  @FXML
  private void onAdd() {
    content().getOrCreateEnumLabels().add(new EnumLabel());
    structuralChange();
  }

  @Override
  protected void rebuild() {
    rows.getChildren().clear();
    List<EnumLabel> entries = content().getEnumLabelsOrEmpty();
    emptyLabel.setVisible(entries.isEmpty());
    emptyLabel.setManaged(entries.isEmpty());

    for (int index = 0; index < entries.size(); index++) {
      rows.getChildren().add(card(index, entries.get(index), entries.size()));
    }
  }

  private VBox card(int index, EnumLabel entry, int count) {
    ComboBox<String> fieldPath = suggestionCombo(entry.getEnumFieldPath(), () -> collect(entry, EnumValue::fieldPaths),
        entry::setEnumFieldPath, "field-path-" + index, StudioBundle.get("transformer_model.enum_labels.field_path_prompt"));
    ComboBox<String> typeDefinition = suggestionCombo(entry.getTypeDefinitionId(), () -> collect(entry, EnumValue::typeDefinitionIds),
        entry::setTypeDefinitionId, "type-definition-" + index, StudioBundle.get("transformer_model.enum_labels.type_definition_prompt"));
    // The value decides which paths and type definitions are proposed; only those two are refreshed, not the one typed in.
    ComboBox<String> value = suggestionCombo(entry.getValue(), () -> List.copyOf(enumValues.keySet()), text -> {
      entry.setValue(text);
      reapplySuggestions(fieldPath, typeDefinition);
    }, "value-" + index, StudioBundle.get("transformer_model.enum_labels.value_prompt"));

    registerErrorTarget(TransformerElementIds.enumLabel(index, "value"), value);
    registerErrorTarget(TransformerElementIds.enumLabel(index, "enumFieldPath"), fieldPath);
    registerErrorTarget(TransformerElementIds.enumLabel(index, "typeDefinitionId"), typeDefinition);

    var moveButtons = RowFactory.createMoveButtonsBox(index, count, (from, to) -> {
      Collections.swap(content().getOrCreateEnumLabels(), from, to);
      structuralChange();
    });
    var delete = actionButton(Icons.TRASH, "row_action.delete", () -> {
      content().getOrCreateEnumLabels().remove(index);
      structuralChange();
    });

    VBox card = new VBox(8);
    card.getStyleClass().add("transformer-card");
    card.getChildren().add(row(grow(labelled("transformer_model.enum_labels.value", value)),
        grow(labelled("transformer_model.enum_labels.field_path", fieldPath)),
        grow(labelled("transformer_model.enum_labels.type_definition", typeDefinition)), moveButtons, delete));
    card.getChildren().add(fieldLabel("transformer_model.enum_labels.texts"));
    card.getChildren().add(localeTextRows(entry::getReplacementsOrEmpty, entry::getOrCreateReplacements,
        () -> {
          if (entry.getReplacements() != null && entry.getReplacements().isEmpty()) {
            entry.setReplacements(null);
          }
        },
        replacementIndex -> TransformerElementIds.enumLabelLocale(index, replacementIndex), "text-" + index + "-"));
    return card;
  }

  /** What the XSD knows for the entry's value, or - while that is not a known value - for all of them. */
  private List<String> collect(EnumLabel entry, Function<EnumValue, List<String>> property) {
    Set<String> result = new LinkedHashSet<>();
    EnumValue known = entry.getValue() == null ? null : enumValues.get(entry.getValue());
    if (known != null) {
      result.addAll(property.apply(known));
    }
    else {
      enumValues.values().forEach(enumValue -> result.addAll(property.apply(enumValue)));
    }
    return List.copyOf(result);
  }
}
