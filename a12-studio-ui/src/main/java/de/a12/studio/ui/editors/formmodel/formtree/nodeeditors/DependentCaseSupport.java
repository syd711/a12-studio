package de.a12.studio.ui.editors.formmodel.formtree.nodeeditors;

import de.a12.studio.models.documentmodel.BooleanFieldType;
import de.a12.studio.models.documentmodel.ConfirmFieldType;
import de.a12.studio.models.documentmodel.ComputationElement;
import de.a12.studio.models.documentmodel.Element;
import de.a12.studio.models.documentmodel.EnumerationFieldType;
import de.a12.studio.models.documentmodel.EnumerationValue;
import de.a12.studio.models.documentmodel.FieldElement;
import de.a12.studio.models.documentmodel.FieldType;
import de.a12.studio.modelsvalidation.validators.ElementIndex;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.util.StringConverter;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Shared logic between {@link DependentFieldPanelController}, {@link DependentGroupPanelController} and
 * {@link DependentEnumerationPanelController}: all three auto-populate their grid from the master (and, for
 * Dependent Enumeration, dependent) field's own declared value set - mirroring SME's {@code createCaseMap}/
 * {@code createConstraintsMap} - instead of only showing whatever entries already happen to be in the file.
 */
final class DependentCaseSupport {

  private DependentCaseSupport() {
  }

  /** One possible value of a master field: {@code storedValue} is what's written into {@code DependentCase
   * .masterValue} (null for the synthetic "no selection" row), {@code display} is what the trigger-values
   * column shows for it. */
  record MasterValueOption(String display, @Nullable String storedValue) {
  }

  // Boolean, Confirm and Enumeration fields - the only types whose full value set is enumerable, matching
  // SME's isCompatibleMasterField.
  static boolean isEnumerableMasterType(@Nullable Object effectiveType) {
    return effectiveType instanceof BooleanFieldType || effectiveType instanceof ConfirmFieldType
        || effectiveType instanceof EnumerationFieldType;
  }

  /** Every value {@code masterField} can take, in the same order SME's {@code getMasterTypeAndValues} uses:
   * a synthetic "no selection" row first, then {@code false}/{@code true} for Boolean, just {@code true} for
   * Confirm, or the field's own declared enum literals in file order. Empty (besides the synthetic row) for
   * any other/unresolved field type. */
  static List<MasterValueOption> masterValueOptions(@Nullable FieldElement masterField, @NonNull String noSelectionDisplay,
      @NonNull ElementIndex index) {
    List<MasterValueOption> options = new ArrayList<>();
    options.add(new MasterValueOption(noSelectionDisplay, null));
    if (masterField == null || masterField.getField() == null) {
      return options;
    }
    FieldType effectiveType = index.effectiveFieldType(masterField.getField().getFieldType());
    if (effectiveType instanceof BooleanFieldType) {
      options.add(new MasterValueOption("false", "false"));
      options.add(new MasterValueOption("true", "true"));
    }
    else if (effectiveType instanceof ConfirmFieldType) {
      options.add(new MasterValueOption("true", "true"));
    }
    else if (effectiveType instanceof EnumerationFieldType enumType && enumType.getEnumerationType() != null) {
      for (EnumerationValue value : enumType.getEnumerationType().getValues()) {
        options.add(new MasterValueOption(value.getValue(), value.getValue()));
      }
    }
    return options;
  }

  /** The declared values of a Boolean/Confirm/Enumeration field, for the "Value" column's dropdown when the
   * dependent field's own type has a known, enumerable value set - {@code null} for any other type, signalling
   * that column should fall back to free text instead. Mirrors SME's {@code getDependentValues}; the leading
   * blank entry (an explicitly unset value) is represented by callers offering {@code null} as a selectable
   * item, not included here. */
  static @Nullable List<String> dependentValueOptions(@Nullable FieldType effectiveType) {
    List<String> values = new ArrayList<>();
    if (effectiveType instanceof BooleanFieldType) {
      values.add("false");
      values.add("true");
    }
    else if (effectiveType instanceof ConfirmFieldType) {
      values.add("true");
    }
    else if (effectiveType instanceof EnumerationFieldType enumType && enumType.getEnumerationType() != null) {
      for (EnumerationValue value : enumType.getEnumerationType().getValues()) {
        values.add(value.getValue());
      }
    }
    else {
      return null;
    }
    return values;
  }

  /** The declared literal values of an Enumeration field, in file order - empty for any other/unresolved
   * type. Used by Dependent Enumeration for both its master-value rows and its dependent-value columns: unlike
   * Dependent Field/Group's master (which may also be Boolean/Confirm), Dependent Enumeration's master and
   * dependent fields are always Enumeration-typed, and - unlike {@link #masterValueOptions} - never get a
   * synthetic "no selection" entry, matching SME's {@code getValuesFromEnumerationLike}. */
  static List<String> enumerationLiterals(@Nullable FieldType effectiveType) {
    List<String> values = new ArrayList<>();
    if (effectiveType instanceof EnumerationFieldType enumType && enumType.getEnumerationType() != null) {
      for (EnumerationValue value : enumType.getEnumerationType().getValues()) {
        values.add(value.getValue());
      }
    }
    return values;
  }

  /** Resolves {@code elementRef} to the effective (type-definition-aware) {@link FieldType} of the field it
   * points to, or {@code null} if it doesn't resolve to a {@link FieldElement}. */
  static @Nullable FieldType resolveEffectiveFieldType(@Nullable String elementRef, @NonNull ElementIndex index) {
    return index.resolveElement(elementRef)
        .filter(FieldElement.class::isInstance).map(FieldElement.class::cast)
        .map(FieldElement::getField).filter(Objects::nonNull)
        .map(field -> index.effectiveFieldType(field.getFieldType()))
        .orElse(null);
  }

  /** Wires {@code copyButton} to copy {@code masterFieldCombo}'s currently selected value (rendered through the
   * combo's own converter, i.e. the same display path the user sees) to the system clipboard; disabled while no
   * master field is selected. Shared by {@link DependentFieldPanelController}, {@link DependentGroupPanelController}
   * and {@link DependentEnumerationPanelController}, which all offer the same master-field combo. */
  static void wireMasterFieldCopyButton(@NonNull Button copyButton, @NonNull ComboBox<String> masterFieldCombo) {
    copyButton.disableProperty().bind(masterFieldCombo.valueProperty().isNull());
    copyButton.setOnAction(event -> {
      String value = masterFieldCombo.getValue();
      if (value == null) {
        return;
      }
      StringConverter<String> converter = masterFieldCombo.getConverter();
      String text = converter != null ? converter.toString(value) : value;
      ClipboardContent content = new ClipboardContent();
      content.putString(text);
      Clipboard.getSystemClipboard().setContent(content);
    });
  }

  /** True when {@code fieldElementId} is the target of some {@link ComputationElement}'s {@code
   * computedFieldRelPath} - mirrors SME's {@code isComputedField} check, which hides the "Read Only" display
   * option and disables the value-type picker for a field the kernel already computes on every change. */
  static boolean isComputedField(@Nullable String fieldElementId, @NonNull ElementIndex index) {
    if (fieldElementId == null) {
      return false;
    }
    for (Element element : index.allElements()) {
      if (!(element instanceof ComputationElement computation) || computation.getComputation() == null
          || computation.getComputation().getComputedFieldRelPath() == null) {
        continue;
      }
      Element resolved = index.resolveRelativePath(computation, computation.getComputation().getComputedFieldRelPath())
          .orElse(null);
      if (resolved != null && Objects.equals(resolved.getId(), fieldElementId)) {
        return true;
      }
    }
    return false;
  }
}
