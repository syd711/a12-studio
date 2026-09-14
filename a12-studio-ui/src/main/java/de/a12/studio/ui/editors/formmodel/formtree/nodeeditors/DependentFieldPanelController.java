package de.a12.studio.ui.editors.formmodel.formtree.nodeeditors;

import de.a12.studio.models.documentmodel.FieldElement;
import de.a12.studio.models.documentmodel.FieldType;
import de.a12.studio.models.formmodel.DependentCase;
import de.a12.studio.models.formmodel.DependentConfig;
import de.a12.studio.models.formmodel.FieldConfigEntry;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.modelsvalidation.validators.ElementIndex;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.editors.AbstractPropertyEditor;
import de.a12.studio.ui.editors.formmodel.formtree.nodeeditors.HideConditionPanelController.MasterFieldScope;
import de.a12.studio.ui.events.StudioEventManager;
import de.a12.studio.ui.util.StudioBundle;
import de.a12.studio.ui.util.WidgetFactory;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.HBox;
import javafx.util.StringConverter;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;

/**
 * "Dependent Field" property editor for a {@link FieldConfigEntry}: makes this field readonly/not-relevant
 * (and optionally forces a value or copies another field's value) based on a master field's value, mirroring
 * SME's {@code dependentField} field configuration section and its grid editor ({@code
 * DependentFieldGroupTable} in {@code dependencyTable.tsx}).
 * <p>
 * The table shows one row per value the selected master field can actually take (via {@link
 * DependentCaseSupport#masterValueOptions}), including a synthetic "no selection" row - not just whatever
 * {@code case} entries already happen to be in the file. Checking a row's trigger-value checkbox is what adds
 * (or, unchecked, removes) its {@link DependentCase}; {@link #syncCasesFromRows} rebuilds the whole {@code
 * case} list from the table's current state after every edit, mirroring SME's {@code mergeCaseMap}.
 */
public class DependentFieldPanelController implements Initializable {

  static final String DISPLAY_DEFAULT = "DEFAULT";
  static final String DISPLAY_NOT_RELEVANT = "NOT_RELEVANT";
  static final String DISPLAY_READONLY = "READONLY";

  static final String VALUE_TYPE_UNCHANGED = "UNCHANGED";
  static final String VALUE_TYPE_VALUE = "VALUE";
  static final String VALUE_TYPE_FIELD_VALUE = "FIELD_VALUE";

  /** One row of {@link #casesTable}: a possible master-field value, whether it's currently an active case,
   * and (if active) that case's own Display/Value settings - kept as live properties so unchecking then
   * rechecking a row within the same field selection restores what was configured before, the same way SME's
   * in-memory {@code caseMap} does, even though only checked rows are ever written to the file. */
  private static final class CaseRow {
    final String display;
    final @Nullable String storedValue;
    final BooleanProperty enabled = new SimpleBooleanProperty(false);
    final StringProperty displayOption = new SimpleStringProperty(DISPLAY_DEFAULT);
    final StringProperty valueType = new SimpleStringProperty(VALUE_TYPE_UNCHANGED);
    final StringProperty value = new SimpleStringProperty();
    final StringProperty fieldRef = new SimpleStringProperty();

    CaseRow(String display, @Nullable String storedValue) {
      this.display = display;
      this.storedValue = storedValue;
    }
  }

  @FXML
  private TitledPane root;
  @FXML
  private ComboBox<String> masterFieldCombo;
  @FXML
  private Button copyMasterFieldButton;
  @FXML
  private TableView<CaseRow> casesTable;
  @FXML
  private TableColumn<CaseRow, CaseRow> triggerColumn;
  @FXML
  private TableColumn<CaseRow, CaseRow> displayColumn;
  @FXML
  private TableColumn<CaseRow, CaseRow> valueTypeColumn;
  @FXML
  private TableColumn<CaseRow, CaseRow> valueColumn;

  private boolean updatingFromModel;
  private FieldConfigEntry entry;
  private @Nullable ElementIndex elementIndex;

  // Recomputed by setEntry()/the master-field listener; read by the value/value-type column cells.
  private @Nullable List<String> dependentValueOptions;
  private List<String> compatibleFieldIds = List.of();
  private boolean computedField;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    AbstractPropertyEditor.persistExpandedState(root, getClass());

    masterFieldCombo.setConverter(elementRefConverter());
    DependentCaseSupport.wireMasterFieldCopyButton(copyMasterFieldButton, masterFieldCombo);
    casesTable.setItems(FXCollections.observableArrayList());
    casesTable.setPlaceholder(WidgetFactory.createDefaultLabel(StudioBundle.get("dependent_field_no_trigger_field_selected")));

    triggerColumn.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue()));
    triggerColumn.setCellFactory(col -> new TriggerCell());

    displayColumn.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue()));
    displayColumn.setCellFactory(col -> new DisplayCell());

    valueTypeColumn.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue()));
    valueTypeColumn.setCellFactory(col -> new ValueTypeCell());

    valueColumn.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue()));
    valueColumn.setCellFactory(col -> new ValueCell());

    masterFieldCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
      if (updatingFromModel) {
        return;
      }
      String fieldValue = blankToNull(newVal);
      if (fieldValue == null) {
        entry.setDependentField(null);
      }
      else {
        getOrCreate().setMasterField(fieldValue);
      }
      rebuildRows();
      syncCasesFromRows();
    });
  }

  /** Master field candidates are anchored to this entry's own field, mirroring SME's {@code getMasterField}
   * (called with the field configuration entry's {@code elementRef}) - so sibling fields reachable through the
   * same repeat context as the field being configured (e.g. another field in the same repeatable group) are
   * offered, not just fields reachable from the document model root. */
  public void setEntry(@NonNull FieldConfigEntry entry, @Nullable ElementIndex elementIndex) {
    this.entry = entry;
    this.elementIndex = elementIndex;

    MasterFieldScope scope = MasterFieldScope.anchoredOrUnbound(entry.getElementRef(), elementIndex);
    List<String> masterFieldIds = HideConditionPanelController.collectMasterFieldIds(elementIndex, scope,
        (index, field) -> field.getField() != null
            && DependentCaseSupport.isEnumerableMasterType(index.effectiveFieldType(field.getField().getFieldType())));

    FieldType dependentEffectiveType = elementIndex == null ? null
        : DependentCaseSupport.resolveEffectiveFieldType(entry.getElementRef(), elementIndex);
    dependentValueOptions = DependentCaseSupport.dependentValueOptions(dependentEffectiveType);
    computedField = elementIndex != null && DependentCaseSupport.isComputedField(entry.getElementRef(), elementIndex);
    compatibleFieldIds = elementIndex == null ? List.of() : HideConditionPanelController.collectMasterFieldIds(elementIndex, scope,
        (index, field) -> field.getField() != null
            && sameFieldTypeCategory(index.effectiveFieldType(field.getField().getFieldType()), dependentEffectiveType));

    updatingFromModel = true;
    try {
      List<String> comboItems = new ArrayList<>();
      comboItems.add(null);
      comboItems.addAll(masterFieldIds);
      masterFieldCombo.getItems().setAll(comboItems);
      masterFieldCombo.setValue(entry.getDependentField() == null ? null : entry.getDependentField().getMasterField());
      rebuildRows();
    }
    finally {
      updatingFromModel = false;
    }
  }

  /** Repopulates {@link #casesTable} with one row per value the currently selected master field can take,
   * pre-filling each row from any existing {@link DependentCase} for that value - see {@link
   * DependentCaseSupport#masterValueOptions}. */
  private void rebuildRows() {
    List<CaseRow> rows = new ArrayList<>();
    String masterFieldId = masterFieldCombo.getValue();
    FieldElement masterField = masterFieldId == null || elementIndex == null ? null
        : elementIndex.resolveElement(masterFieldId).filter(FieldElement.class::isInstance).map(FieldElement.class::cast).orElse(null);

    if (masterField != null && elementIndex != null) {
      List<DependentCase> existingCases = entry.getDependentField() == null ? List.of() : entry.getDependentField().getCases();
      for (DependentCaseSupport.MasterValueOption option
          : DependentCaseSupport.masterValueOptions(masterField, StudioBundle.get("dependent_field_no_selection"), elementIndex)) {
        CaseRow row = new CaseRow(option.display(), option.storedValue());
        existingCases.stream().filter(c -> valuesEqual(c.getMasterValue(), option.storedValue())).findFirst()
            .ifPresent(existing -> populateFromExisting(row, existing));
        wireRow(row);
        rows.add(row);
      }
    }
    casesTable.setItems(FXCollections.observableArrayList(rows));
  }

  private void populateFromExisting(CaseRow row, DependentCase existing) {
    row.enabled.set(true);
    row.displayOption.set(Boolean.TRUE.equals(existing.getNotRelevant()) ? DISPLAY_NOT_RELEVANT
        : Boolean.TRUE.equals(existing.getReadonly()) ? DISPLAY_READONLY : DISPLAY_DEFAULT);
    if (existing.getFieldRef() != null) {
      row.valueType.set(VALUE_TYPE_FIELD_VALUE);
      row.fieldRef.set(existing.getFieldRef());
    }
    else if (existing.getValue() != null) {
      row.valueType.set(VALUE_TYPE_VALUE);
      row.value.set(existing.getValue());
    }
  }

  private void wireRow(CaseRow row) {
    row.enabled.addListener((obs, oldVal, newVal) -> syncCasesFromRows());
    row.displayOption.addListener((obs, oldVal, newVal) -> syncCasesFromRows());
    row.valueType.addListener((obs, oldVal, newVal) -> syncCasesFromRows());
    row.value.addListener((obs, oldVal, newVal) -> syncCasesFromRows());
    row.fieldRef.addListener((obs, oldVal, newVal) -> syncCasesFromRows());
  }

  /** Rebuilds {@code dependentField.case} from every currently-checked row, mirroring SME's {@code
   * mergeCaseMap}: a "Not Relevant" row drops any Value/Field Value it was carrying (matching the reference's
   * {@code display === "Not Relevant" ? undefined : ...}), and unchecked rows simply aren't written. */
  private void syncCasesFromRows() {
    if (updatingFromModel) {
      return;
    }
    if (masterFieldCombo.getValue() == null) {
      commitChange();
      return;
    }
    DependentConfig config = getOrCreate();
    List<DependentCase> cases = new ArrayList<>();
    for (CaseRow row : casesTable.getItems()) {
      if (!row.enabled.get()) {
        continue;
      }
      DependentCase newCase = new DependentCase();
      newCase.setMasterValue(row.storedValue);
      boolean notRelevant = DISPLAY_NOT_RELEVANT.equals(row.displayOption.get());
      boolean readonly = DISPLAY_READONLY.equals(row.displayOption.get());
      newCase.setNotRelevant(notRelevant ? Boolean.TRUE : null);
      newCase.setReadonly(readonly ? Boolean.TRUE : null);
      if (!notRelevant) {
        if (VALUE_TYPE_VALUE.equals(row.valueType.get())) {
          newCase.setValue(blankToNull(row.value.get()));
        }
        else if (VALUE_TYPE_FIELD_VALUE.equals(row.valueType.get())) {
          newCase.setFieldRef(blankToNull(row.fieldRef.get()));
        }
      }
      cases.add(newCase);
    }
    config.getCases().clear();
    config.getCases().addAll(cases);
    commitChange();
    casesTable.refresh();
  }

  private static boolean sameFieldTypeCategory(@Nullable FieldType a, @Nullable FieldType b) {
    return a != null && b != null && a.getClass() == b.getClass();
  }

  private static boolean valuesEqual(@Nullable String a, @Nullable String b) {
    return (a == null && b == null) || (a != null && a.equals(b));
  }

  private DependentConfig getOrCreate() {
    if (entry.getDependentField() == null) {
      entry.setDependentField(new DependentConfig());
    }
    return entry.getDependentField();
  }

  private static @Nullable String blankToNull(@Nullable String value) {
    return (value == null || value.isBlank()) ? null : value;
  }

  private StringConverter<String> elementRefConverter() {
    return new StringConverter<>() {
      @Override
      public String toString(String elementId) {
        return elementId == null ? "" : (elementIndex != null ? elementIndex.resolveDisplayPath(elementId) : elementId);
      }

      @Override
      public String fromString(String string) {
        return string;
      }
    };
  }

  private static StringConverter<String> displayConverter() {
    return new StringConverter<>() {
      @Override
      public String toString(String token) {
        if (DISPLAY_NOT_RELEVANT.equals(token)) {
          return StudioBundle.get("dependent_field_not_relevant_column");
        }
        if (DISPLAY_READONLY.equals(token)) {
          return StudioBundle.get("readonly");
        }
        return StudioBundle.get("dependent_field_display_default");
      }

      @Override
      public String fromString(String string) {
        return string;
      }
    };
  }

  private static StringConverter<String> valueTypeConverter() {
    return new StringConverter<>() {
      @Override
      public String toString(String token) {
        if (VALUE_TYPE_VALUE.equals(token)) {
          return StudioBundle.get("dependent_field_value_column");
        }
        if (VALUE_TYPE_FIELD_VALUE.equals(token)) {
          return StudioBundle.get("dependent_field_value_type_field_value");
        }
        return StudioBundle.get("dependent_field_value_type_unchanged");
      }

      @Override
      public String fromString(String string) {
        return string;
      }
    };
  }

  /** Checkbox + label cell: checking/unchecking is what adds/removes this row's {@link DependentCase}. */
  private final class TriggerCell extends TableCell<CaseRow, CaseRow> {
    private final CheckBox checkBox = new CheckBox();
    private final Label label = new Label();
    private final HBox box = new HBox(6, checkBox, label);

    TriggerCell() {
      box.setAlignment(Pos.CENTER_LEFT);
      checkBox.setOnAction(e -> {
        CaseRow row = getItem();
        if (row != null) {
          row.enabled.set(checkBox.isSelected());
        }
      });
    }

    @Override
    protected void updateItem(CaseRow row, boolean empty) {
      super.updateItem(row, empty);
      if (empty || row == null) {
        setGraphic(null);
        return;
      }
      label.setText(row.display);
      checkBox.setSelected(row.enabled.get());
      setGraphic(box);
    }
  }

  /** "Default" / "Not Relevant" / "Read Only" (hidden when {@link #computedField}) picker. */
  private final class DisplayCell extends TableCell<CaseRow, CaseRow> {
    private final ComboBox<String> combo = new ComboBox<>();
    private boolean guarding;

    DisplayCell() {
      combo.setMaxWidth(Double.MAX_VALUE);
      combo.setConverter(displayConverter());
      combo.valueProperty().addListener((obs, oldVal, newVal) -> {
        if (guarding || newVal == null) {
          return;
        }
        CaseRow row = getItem();
        if (row != null) {
          row.displayOption.set(newVal);
        }
      });
    }

    @Override
    protected void updateItem(CaseRow row, boolean empty) {
      super.updateItem(row, empty);
      if (empty || row == null) {
        setGraphic(null);
        return;
      }
      List<String> items = new ArrayList<>(List.of(DISPLAY_DEFAULT, DISPLAY_NOT_RELEVANT));
      if (!computedField) {
        items.add(DISPLAY_READONLY);
      }
      guarding = true;
      combo.getItems().setAll(items);
      combo.setValue(row.displayOption.get());
      guarding = false;
      combo.setDisable(!row.enabled.get());
      setGraphic(combo);
    }
  }

  /** "Unchanged" / "Value" / "Field Value" picker - disabled while computed, unchecked, or Not Relevant. */
  private final class ValueTypeCell extends TableCell<CaseRow, CaseRow> {
    private final ComboBox<String> combo = new ComboBox<>();
    private boolean guarding;

    ValueTypeCell() {
      combo.setMaxWidth(Double.MAX_VALUE);
      combo.setConverter(valueTypeConverter());
      combo.getItems().setAll(VALUE_TYPE_UNCHANGED, VALUE_TYPE_VALUE, VALUE_TYPE_FIELD_VALUE);
      combo.valueProperty().addListener((obs, oldVal, newVal) -> {
        if (guarding || newVal == null) {
          return;
        }
        CaseRow row = getItem();
        if (row == null) {
          return;
        }
        row.valueType.set(newVal);
        if (VALUE_TYPE_VALUE.equals(newVal) && dependentValueOptions != null && !dependentValueOptions.isEmpty()
            && blankToNull(row.value.get()) == null) {
          row.value.set(dependentValueOptions.get(0));
        }
      });
    }

    @Override
    protected void updateItem(CaseRow row, boolean empty) {
      super.updateItem(row, empty);
      if (empty || row == null) {
        setGraphic(null);
        return;
      }
      guarding = true;
      combo.setValue(row.valueType.get());
      guarding = false;
      combo.setDisable(!row.enabled.get() || computedField || DISPLAY_NOT_RELEVANT.equals(row.displayOption.get()));
      setGraphic(combo);
    }
  }

  /** Renders as a value picker (enum/boolean/confirm dependent field), free-text field, or a compatible-field
   * picker, depending on the row's {@code valueType} - mirrors SME's {@code DependentFieldValuePicker}/{@code
   * DependentFieldValueInput}/field-ref {@code Select} split in {@code dependencyTable.tsx}. */
  private final class ValueCell extends TableCell<CaseRow, CaseRow> {
    private final TextField textField = new TextField();
    private final ComboBox<String> valueCombo = new ComboBox<>();
    private final ComboBox<String> fieldRefCombo = new ComboBox<>();
    private boolean guarding;

    ValueCell() {
      valueCombo.setMaxWidth(Double.MAX_VALUE);
      fieldRefCombo.setMaxWidth(Double.MAX_VALUE);
      fieldRefCombo.setConverter(elementRefConverter());

      textField.textProperty().addListener((obs, oldVal, newVal) -> {
        if (guarding) {
          return;
        }
        CaseRow row = getItem();
        if (row != null) {
          row.value.set(blankToNull(newVal));
        }
      });
      valueCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
        if (guarding) {
          return;
        }
        CaseRow row = getItem();
        if (row != null) {
          row.value.set(newVal);
        }
      });
      fieldRefCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
        if (guarding) {
          return;
        }
        CaseRow row = getItem();
        if (row != null) {
          row.fieldRef.set(newVal);
        }
      });
    }

    @Override
    protected void updateItem(CaseRow row, boolean empty) {
      super.updateItem(row, empty);
      if (empty || row == null) {
        setGraphic(null);
        return;
      }
      boolean disabled = !row.enabled.get() || DISPLAY_NOT_RELEVANT.equals(row.displayOption.get());
      guarding = true;
      try {
        if (VALUE_TYPE_FIELD_VALUE.equals(row.valueType.get())) {
          fieldRefCombo.getItems().setAll(compatibleFieldIds);
          fieldRefCombo.setValue(row.fieldRef.get());
          fieldRefCombo.setDisable(disabled);
          setGraphic(fieldRefCombo);
        }
        else if (VALUE_TYPE_VALUE.equals(row.valueType.get())) {
          if (dependentValueOptions != null) {
            valueCombo.getItems().setAll(dependentValueOptions);
            valueCombo.setValue(row.value.get());
            valueCombo.setDisable(disabled);
            setGraphic(valueCombo);
          }
          else {
            textField.setText(Objects.requireNonNullElse(row.value.get(), ""));
            textField.setDisable(disabled);
            setGraphic(textField);
          }
        }
        else {
          setGraphic(null);
        }
      }
      finally {
        guarding = false;
      }
    }
  }

  private void commitChange() {
    ProjectItem projectItem = Studio.getSelectedProjectItem();
    if (projectItem == null) {
      return;
    }
    projectItem.save();
    StudioEventManager.getInstance().fireModelSavedEvent(projectItem);
  }
}
