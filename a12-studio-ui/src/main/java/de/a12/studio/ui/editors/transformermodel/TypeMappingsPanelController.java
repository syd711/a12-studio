package de.a12.studio.ui.editors.transformermodel;

import de.a12.studio.models.transformermodel.A12DataType;
import de.a12.studio.models.transformermodel.EnumerationTypeConfig;
import de.a12.studio.models.transformermodel.NumberTypeConfig;
import de.a12.studio.models.transformermodel.StringTypeConfig;
import de.a12.studio.models.transformermodel.TypeMappingEntry;
import de.a12.studio.modelsvalidation.validators.transformer.TransformerElementIds;
import de.a12.studio.ui.editors.propertyeditors.RowFactory;
import de.a12.studio.ui.util.Icons;
import de.a12.studio.ui.util.StudioBundle;
import de.a12.studio.ui.util.WidgetFactory;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

/**
 * The "Type Mappings" section of the Transformation tab ({@code content.TypeMapping}): which A12 data type an XSD
 * simple type becomes, with the configuration of that data type. One card per mapping: the XSD type name (suggested
 * from the XSD's simple types, SME's {@code xsd:simpleTypes}, any text accepted), the A12 data type, and only the
 * configuration fields that apply to it - SME's dependent fields: String fields for {@code StringType}, Number fields
 * for {@code NumberType}, the sorting option for {@code EnumerationType}/{@code EnumForStringType}. Choosing another
 * data type clears the fields that no longer apply (what SME's form engine does with a field that becomes not
 * relevant), so no stale configuration is left to trip the rules. A configuration field is a plain checkbox in SME;
 * here it has a third, cleared state, because a file can say {@code false} explicitly as well as say nothing.
 */
public class TypeMappingsPanelController extends TransformerModelPanelController {

  @FXML
  private VBox rows;

  @FXML
  private Label emptyLabel;

  private List<String> xsdTypes = List.of();

  /** The names of the XSD's simple types (SME's {@code xsd:simpleTypes}). */
  public void setXsdTypes(@NonNull List<String> xsdTypes) {
    this.xsdTypes = xsdTypes;
    refreshSuggestions();
  }

  @Override
  protected String errorIdPrefix() {
    return TransformerElementIds.TYPE_MAPPING;
  }

  @FXML
  private void onAdd() {
    content().getOrCreateTypeMapping().add(new TypeMappingEntry());
    structuralChange();
  }

  @Override
  protected void rebuild() {
    rows.getChildren().clear();
    List<TypeMappingEntry> entries = content().getTypeMappingOrEmpty();
    emptyLabel.setVisible(entries.isEmpty());
    emptyLabel.setManaged(entries.isEmpty());

    for (int index = 0; index < entries.size(); index++) {
      rows.getChildren().add(card(index, entries.get(index), entries.size()));
    }
  }

  private VBox card(int index, TypeMappingEntry entry, int count) {
    var xsdType = suggestionCombo(entry.getXsdType(), () -> xsdTypes, entry::setXsdType, "xsd-type-" + index,
        StudioBundle.get("transformer_model.type_mappings.xsd_type_prompt"));
    registerErrorTarget(TransformerElementIds.typeMapping(index, "xsdType"), xsdType);

    ComboBox<String> a12Type = a12TypeCombo(entry);
    a12Type.valueProperty().addListener((observable, oldValue, newValue) -> {
      if (rebuilding) {
        return;
      }
      entry.setA12Type(newValue);
      clearConfigurationsThatNoLongerApply(entry);
      structuralChange();
    });
    registerErrorTarget(TransformerElementIds.typeMapping(index, "a12Type"), a12Type);

    var moveButtons = RowFactory.createMoveButtonsBox(index, count, (from, to) -> {
      Collections.swap(content().getOrCreateTypeMapping(), from, to);
      structuralChange();
    });
    var delete = actionButton(Icons.TRASH, "row_action.delete", () -> {
      content().getOrCreateTypeMapping().remove(index);
      structuralChange();
    });

    VBox card = new VBox(8);
    card.getStyleClass().add("transformer-card");
    card.getChildren().add(row(grow(labelled("transformer_model.type_mappings.xsd_type", xsdType)),
        labelled("transformer_model.type_mappings.a12_type", a12Type), moveButtons, delete));

    A12DataType dataType = entry.getDataType();
    if (dataType != null && dataType.showsStringConfig()) {
      card.getChildren().add(stringConfiguration(index, entry));
    }
    if (dataType != null && dataType.showsNumberConfig()) {
      card.getChildren().add(numberConfiguration(index, entry));
    }
    if (dataType != null && dataType.showsEnumerationConfig()) {
      card.getChildren().add(enumerationConfiguration(index, entry));
    }
    return card;
  }

  private static void clearConfigurationsThatNoLongerApply(TypeMappingEntry entry) {
    A12DataType dataType = entry.getDataType();
    if (dataType == null || !dataType.showsStringConfig()) {
      entry.setStringType(null);
    }
    if (dataType == null || !dataType.showsNumberConfig()) {
      entry.setNumberType(null);
    }
    if (dataType == null || !dataType.showsEnumerationConfig()) {
      entry.setEnumerationType(null);
    }
  }

  private ComboBox<String> a12TypeCombo(TypeMappingEntry entry) {
    ComboBox<String> combo = new ComboBox<>();
    for (A12DataType type : A12DataType.values()) {
      combo.getItems().add(type.getValue());
    }
    combo.setPromptText(StudioBundle.get("transformer_model.type_mappings.a12_type_prompt"));
    combo.setConverter(new StringConverter<>() {
      @Override
      public String toString(@Nullable String value) {
        return value == null ? "" : StudioBundle.get("transformer_model.a12_type." + value);
      }

      @Override
      public String fromString(String text) {
        return text;
      }
    });
    combo.setPrefWidth(190);
    combo.setValue(entry.getA12Type());
    return combo;
  }

  // ---- the configuration of a data type ---------------------------------------------------------------------------

  private FlowPane stringConfiguration(int index, TypeMappingEntry entry) {
    StringTypeConfig config = entry.getStringType();
    String id = "StringType/";
    FlowPane fields = fields();
    fields.getChildren().addAll(
        labelled("transformer_model.field.min_length", integerField(config == null ? null : config.getMinLength(),
            value -> edit(entry, () -> entry.getOrCreateStringType().setMinLength(value)), TransformerElementIds.typeMapping(index, id + "minLength"), "min-" + index)),
        labelled("transformer_model.field.max_length", integerField(config == null ? null : config.getMaxLength(),
            value -> edit(entry, () -> entry.getOrCreateStringType().setMaxLength(value)), TransformerElementIds.typeMapping(index, id + "maxLength"), "max-" + index)),
        labelled("transformer_model.field.pattern", patternField(config == null ? null : config.getPattern(),
            value -> edit(entry, () -> entry.getOrCreateStringType().setPattern(value)), TransformerElementIds.typeMapping(index, id + "pattern"), "pattern-" + index)),
        checkBox("transformer_model.field.line_breaks_permitted", config == null ? null : config.getLineBreaksPermitted(),
            value -> edit(entry, () -> entry.getOrCreateStringType().setLineBreaksPermitted(value)), TransformerElementIds.typeMapping(index, id + "lineBreaksPermitted")),
        checkBox("transformer_model.field.alphabetical_sorting", config == null ? null : config.getAlphabeticalSorting(),
            value -> edit(entry, () -> entry.getOrCreateStringType().setAlphabeticalSorting(value)), TransformerElementIds.typeMapping(index, id + "alphabeticalSorting")));
    return fields;
  }

  private FlowPane numberConfiguration(int index, TypeMappingEntry entry) {
    NumberTypeConfig config = entry.getNumberType();
    String id = "NumberType/";
    FlowPane fields = fields();
    fields.getChildren().addAll(
        labelled("transformer_model.field.min_fractional_digits", integerField(config == null ? null : config.getMinFractionalDigits(),
            value -> edit(entry, () -> entry.getOrCreateNumberType().setMinFractionalDigits(value)), TransformerElementIds.typeMapping(index, id + "minFractionalDigits"), "min-fraction-" + index)),
        labelled("transformer_model.field.max_fractional_digits", integerField(config == null ? null : config.getMaxFractionalDigits(),
            value -> edit(entry, () -> entry.getOrCreateNumberType().setMaxFractionalDigits(value)), TransformerElementIds.typeMapping(index, id + "maxFractionalDigits"), "max-fraction-" + index)),
        labelled("transformer_model.field.max_integer_digits", integerField(config == null ? null : config.getMaxIntegerDigits(),
            value -> edit(entry, () -> entry.getOrCreateNumberType().setMaxIntegerDigits(value)), TransformerElementIds.typeMapping(index, id + "maxIntegerDigits"), "max-integer-" + index)),
        labelled("transformer_model.field.min_value", decimalField(config == null ? null : config.getMinValue(),
            value -> edit(entry, () -> entry.getOrCreateNumberType().setMinValue(value)), TransformerElementIds.typeMapping(index, id + "minValue"), "min-value-" + index)),
        labelled("transformer_model.field.max_value", decimalField(config == null ? null : config.getMaxValue(),
            value -> edit(entry, () -> entry.getOrCreateNumberType().setMaxValue(value)), TransformerElementIds.typeMapping(index, id + "maxValue"), "max-value-" + index)),
        labelled("transformer_model.field.min_length", integerField(config == null ? null : config.getMinLength(),
            value -> edit(entry, () -> entry.getOrCreateNumberType().setMinLength(value)), TransformerElementIds.typeMapping(index, id + "minLength"), "number-min-" + index)),
        labelled("transformer_model.field.max_length", integerField(config == null ? null : config.getMaxLength(),
            value -> edit(entry, () -> entry.getOrCreateNumberType().setMaxLength(value)), TransformerElementIds.typeMapping(index, id + "maxLength"), "number-max-" + index)),
        labelled("transformer_model.field.trait", traitCombo(config == null ? null : config.getTrait(),
            value -> edit(entry, () -> entry.getOrCreateNumberType().setTrait(value)), TransformerElementIds.typeMapping(index, id + "trait"))),
        checkBox("transformer_model.field.leading_zeros_allowed", config == null ? null : config.getLeadingZerosAllowed(),
            value -> edit(entry, () -> entry.getOrCreateNumberType().setLeadingZerosAllowed(value)), TransformerElementIds.typeMapping(index, id + "leadingZerosAllowed")),
        checkBox("transformer_model.field.zero_not_allowed", config == null ? null : config.getZeroNotAllowed(),
            value -> edit(entry, () -> entry.getOrCreateNumberType().setZeroNotAllowed(value)), TransformerElementIds.typeMapping(index, id + "zeroNotAllowed")),
        checkBox("transformer_model.field.positives_only", config == null ? null : config.getPositivesOnly(),
            value -> edit(entry, () -> entry.getOrCreateNumberType().setPositivesOnly(value)), TransformerElementIds.typeMapping(index, id + "positivesOnly")));
    return fields;
  }

  private FlowPane enumerationConfiguration(int index, TypeMappingEntry entry) {
    EnumerationTypeConfig config = entry.getEnumerationType();
    FlowPane fields = fields();
    fields.getChildren().add(checkBox("transformer_model.field.alphabetical_sorting", config == null ? null : config.getAlphabeticalSorting(),
        value -> edit(entry, () -> entry.getOrCreateEnumerationType().setAlphabeticalSorting(value)),
        TransformerElementIds.typeMapping(index, "EnumerationType/alphabeticalSorting")));
    return fields;
  }

  /** Applies one field edit, then drops the configuration groups that have no value left (an empty group is not written). */
  private void edit(TypeMappingEntry entry, Runnable change) {
    change.run();
    entry.pruneEmptyConfigs();
  }

  private static FlowPane fields() {
    FlowPane fields = new FlowPane(14, 8);
    fields.setPrefWrapLength(560);
    return fields;
  }

  // ---- the controls of the configuration fields -------------------------------------------------------------------

  private TextField integerField(@Nullable Integer value, Consumer<Integer> setter, String errorId, String key) {
    TextField field = new TextField(value == null ? "" : value.toString());
    field.setPrefWidth(110);
    WidgetFactory.restrictToNumericInput(field);
    field.textProperty().addListener((observable, oldValue, newValue) -> {
      if (rebuilding) {
        return;
      }
      setter.accept(newValue == null || newValue.isEmpty() ? null : (int) Math.min(Integer.MAX_VALUE, Long.parseLong(newValue)));
      textEdited(key);
    });
    registerErrorTarget(errorId, field);
    return field;
  }

  private TextField decimalField(@Nullable BigDecimal value, Consumer<BigDecimal> setter, String errorId, String key) {
    TextField field = new TextField(value == null ? "" : value.toPlainString());
    field.setPrefWidth(130);
    WidgetFactory.restrictToDecimalInput(field);
    field.textProperty().addListener((observable, oldValue, newValue) -> {
      if (rebuilding) {
        return;
      }
      if (newValue == null || newValue.isEmpty()) {
        setter.accept(null);
      }
      else {
        try {
          setter.accept(new BigDecimal(newValue));
        }
        catch (NumberFormatException e) {
          return; // "-" or "." while the number is still being typed: nothing to store yet
        }
      }
      textEdited(key);
    });
    registerErrorTarget(errorId, field);
    return field;
  }

  private TextField patternField(@Nullable String value, Consumer<String> setter, String errorId, String key) {
    TextField field = textField(value, setter, key, null);
    field.setPrefWidth(260);
    registerErrorTarget(errorId, field);
    return field;
  }

  /** A checkbox with a third, cleared (indeterminate) state for "not specified". */
  private CheckBox checkBox(String labelKey, @Nullable Boolean value, Consumer<Boolean> setter, String errorId) {
    CheckBox box = new CheckBox(StudioBundle.get(labelKey));
    box.setAllowIndeterminate(true);
    box.setIndeterminate(value == null);
    box.setSelected(Boolean.TRUE.equals(value));
    Runnable commit = () -> {
      if (!rebuilding) {
        setter.accept(box.isIndeterminate() ? null : box.isSelected());
        textEdited(errorId);
      }
    };
    box.selectedProperty().addListener((observable, oldValue, newValue) -> commit.run());
    box.indeterminateProperty().addListener((observable, oldValue, newValue) -> commit.run());
    registerErrorTarget(errorId, box);
    return box;
  }

  private ComboBox<String> traitCombo(@Nullable String value, Consumer<String> setter, String errorId) {
    ComboBox<String> combo = new ComboBox<>();
    combo.getItems().add(null);
    combo.getItems().addAll(NumberTypeConfig.TRAIT_AMOUNT, NumberTypeConfig.TRAIT_PERCENT, NumberTypeConfig.TRAIT_PERMILLE);
    combo.setConverter(new StringConverter<>() {
      @Override
      public String toString(@Nullable String trait) {
        return trait == null ? "" : StudioBundle.get("transformer_model.trait." + trait);
      }

      @Override
      public String fromString(String text) {
        return text;
      }
    });
    combo.setPrefWidth(150);
    combo.setValue(value);
    combo.valueProperty().addListener((observable, oldValue, newValue) -> {
      if (!rebuilding) {
        setter.accept(newValue);
        textEdited(errorId);
      }
    });
    registerErrorTarget(errorId, combo);
    return combo;
  }
}
