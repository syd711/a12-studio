package de.a12.studio.ui.editors.formmodel.formtree.nodeeditors;

import de.a12.studio.models.documentmodel.EnumerationFieldType;
import de.a12.studio.models.documentmodel.FieldElement;
import de.a12.studio.models.documentmodel.FieldType;
import de.a12.studio.models.formmodel.DependentEnumeration;
import de.a12.studio.models.formmodel.DependentEnumerationConstraint;
import de.a12.studio.models.formmodel.EnumerationConstraintValue;
import de.a12.studio.models.formmodel.FieldConfigEntry;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.modelsvalidation.validators.ElementIndex;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.editors.AbstractPropertyEditor;
import de.a12.studio.ui.editors.formmodel.formtree.nodeeditors.HideConditionPanelController.MasterFieldScope;
import de.a12.studio.ui.events.StudioEventManager;
import de.a12.studio.ui.util.StudioBundle;
import de.a12.studio.ui.util.WidgetFactory;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TitledPane;
import javafx.util.StringConverter;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * "Dependent Enumeration" property editor for a {@link FieldConfigEntry}: constrains which of the field's own
 * Enumeration values are offered, based on a master field's value - distinct from {@code dependentField}, which
 * only affects visibility/readonly. Mirrors SME's grid editor ({@code DependentEnumerationTable} in {@code
 * dependencyTable.tsx}): one row per value the master field's own Enumeration declares, one column per value
 * the *dependent* field (this entry's own {@code elementRef}) declares, each cell a 3-state Hidden/Visible/
 * Default toggle - unlike {@link DependentFieldPanelController}/{@link DependentGroupPanelController}, no
 * synthetic "no selection" row (mirrors SME's {@code getValuesFromEnumerationLike}, which - unlike {@code
 * getMasterTypeAndValues} - never prepends one), and every master-value row always exists (there's no
 * per-row checkbox: a row absent from the file just means every column defaults to Visible for it).
 * <p>
 * "Default" (at most one per row) is what {@code valueForMasterChange} means: the value the dependent field is
 * switched to when the master field changes to that row's value. Reused both from the Control node editor
 * ({@link FormNodeEditorControlPanelController}) and from the model-wide Data Configuration tab
 * ({@code DataConfigurationPanelController}).
 */
public class DependentEnumerationPanelController implements Initializable {

  static final String STATE_HIDDEN = "HIDDEN";
  static final String STATE_VISIBLE = "VISIBLE";
  static final String STATE_DEFAULT = "DEFAULT";

  /** One row: a master-field literal value, and a Hidden/Visible/Default state per dependent-field literal
   * value (the grid's columns, fixed for the lifetime of one {@link #setEntry} call). */
  private static final class ConstraintRow {
    final String masterValue;
    final Map<String, StringProperty> cellState = new LinkedHashMap<>();

    ConstraintRow(String masterValue, List<String> dependentValues) {
      this.masterValue = masterValue;
      for (String dependentValue : dependentValues) {
        cellState.put(dependentValue, new SimpleStringProperty(STATE_VISIBLE));
      }
    }
  }

  @FXML
  private TitledPane root;
  @FXML
  private ComboBox<String> masterFieldCombo;
  @FXML
  private Button copyMasterFieldButton;
  @FXML
  private TableView<ConstraintRow> constraintsTable;
  @FXML
  private TableColumn<ConstraintRow, String> masterValueColumn;

  private boolean updatingFromModel;
  private FieldConfigEntry entry;
  private @Nullable ElementIndex elementIndex;

  // This entry's own field's declared enum values - the grid's columns. Fixed per setEntry() call (the field
  // being configured doesn't change while its detail panel is open).
  private List<String> dependentValues = List.of();

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    AbstractPropertyEditor.persistExpandedState(root, getClass());

    masterFieldCombo.setConverter(elementRefConverter());
    DependentCaseSupport.wireMasterFieldCopyButton(copyMasterFieldButton, masterFieldCombo);
    constraintsTable.setItems(FXCollections.observableArrayList());
    constraintsTable.setPlaceholder(WidgetFactory.createDefaultLabel(StudioBundle.get("dependent_enumeration_no_trigger_field_selected")));
    masterValueColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().masterValue));

    masterFieldCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
      if (updatingFromModel) {
        return;
      }
      String fieldValue = blankToNull(newVal);
      if (fieldValue == null) {
        entry.setDependentEnumeration(null);
      }
      else {
        getOrCreate().setMasterField(fieldValue);
      }
      rebuildRows();
      syncConstraintsFromRows();
    });
  }

  public void setEntry(@NonNull FieldConfigEntry entry, @Nullable ElementIndex elementIndex, @NonNull MasterFieldScope scope) {
    this.entry = entry;
    this.elementIndex = elementIndex;

    List<String> masterFieldIds = HideConditionPanelController.collectMasterFieldIds(elementIndex, scope,
        (index, field) -> field.getField() != null && index.effectiveFieldType(field.getField().getFieldType()) instanceof EnumerationFieldType);

    FieldType dependentEffectiveType = elementIndex == null ? null
        : DependentCaseSupport.resolveEffectiveFieldType(entry.getElementRef(), elementIndex);
    dependentValues = DependentCaseSupport.enumerationLiterals(dependentEffectiveType);
    rebuildColumns();

    updatingFromModel = true;
    try {
      List<String> comboItems = new ArrayList<>();
      comboItems.add(null);
      comboItems.addAll(masterFieldIds);
      masterFieldCombo.getItems().setAll(comboItems);
      DependentEnumeration dependentEnumeration = entry.getDependentEnumeration();
      masterFieldCombo.setValue(dependentEnumeration == null ? null : dependentEnumeration.getMasterField());
      rebuildRows();
    }
    finally {
      updatingFromModel = false;
    }
  }

  /** (Re)builds one dynamic Hidden/Visible/Default column per {@link #dependentValues} literal, after the
   * fixed {@link #masterValueColumn}. */
  private void rebuildColumns() {
    constraintsTable.getColumns().setAll(masterValueColumn);
    for (String dependentValue : dependentValues) {
      TableColumn<ConstraintRow, ConstraintRow> column = new TableColumn<>(dependentValue);
      column.setSortable(false);
      column.setPrefWidth(130.0);
      column.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue()));
      column.setCellFactory(col -> new StateCell(dependentValue));
      constraintsTable.getColumns().add(column);
    }
  }

  /** Repopulates {@link #constraintsTable} with one row per literal value the currently selected master field
   * declares, pre-filling each cell from any existing {@link DependentEnumerationConstraint} for that master
   * value - see {@link DependentCaseSupport#enumerationLiterals}. */
  private void rebuildRows() {
    List<ConstraintRow> rows = new ArrayList<>();
    String masterFieldId = masterFieldCombo.getValue();
    FieldElement masterField = masterFieldId == null || elementIndex == null ? null
        : elementIndex.resolveElement(masterFieldId).filter(FieldElement.class::isInstance).map(FieldElement.class::cast).orElse(null);

    if (masterField != null && elementIndex != null && !dependentValues.isEmpty()) {
      List<String> masterValues = DependentCaseSupport.enumerationLiterals(
          elementIndex.effectiveFieldType(masterField.getField() == null ? null : masterField.getField().getFieldType()));
      List<DependentEnumerationConstraint> existing = entry.getDependentEnumeration() == null
          ? List.of() : entry.getDependentEnumeration().getConstraints();
      for (String masterValue : masterValues) {
        ConstraintRow row = new ConstraintRow(masterValue, dependentValues);
        existing.stream().filter(c -> masterValue.equals(c.getMasterValue())).findFirst()
            .ifPresent(constraint -> populateFromExisting(row, constraint));
        wireRow(row);
        rows.add(row);
      }
    }
    constraintsTable.setItems(FXCollections.observableArrayList(rows));
  }

  private void populateFromExisting(ConstraintRow row, DependentEnumerationConstraint constraint) {
    Set<String> allowed = constraint.getConstraintValues().stream()
        .map(EnumerationConstraintValue::getValue).collect(Collectors.toSet());
    for (String dependentValue : dependentValues) {
      String state = dependentValue.equals(constraint.getValueForMasterChange()) ? STATE_DEFAULT
          : allowed.contains(dependentValue) ? STATE_VISIBLE : STATE_HIDDEN;
      row.cellState.get(dependentValue).set(state);
    }
  }

  private void wireRow(ConstraintRow row) {
    for (StringProperty state : row.cellState.values()) {
      state.addListener((obs, oldVal, newVal) -> syncConstraintsFromRows());
    }
  }

  /** Rebuilds {@code dependentEnumeration.constraint} from the whole grid's current state, mirroring SME's
   * {@code mergeConstraintsMap}: every master-value row gets a constraint entry (there's no per-row
   * enable/disable - a fully "Visible" row just carries every dependent value in {@code constraintValues}) -
   * unless the entire grid is neutral (every cell Visible, nothing Hidden or Default anywhere), in which case
   * the constraint list is cleared entirely, matching the reference's "useless to have the dependent enum"
   * check. */
  private void syncConstraintsFromRows() {
    if (updatingFromModel) {
      return;
    }
    if (masterFieldCombo.getValue() == null) {
      commitChange();
      return;
    }
    DependentEnumeration config = getOrCreate();
    List<ConstraintRow> rows = constraintsTable.getItems();
    boolean neutral = rows.stream()
        .allMatch(row -> row.cellState.values().stream().allMatch(state -> STATE_VISIBLE.equals(state.get())));

    List<DependentEnumerationConstraint> constraints = new ArrayList<>();
    if (!neutral) {
      for (ConstraintRow row : rows) {
        DependentEnumerationConstraint constraint = new DependentEnumerationConstraint();
        constraint.setMasterValue(row.masterValue);
        List<EnumerationConstraintValue> constraintValues = new ArrayList<>();
        String valueForMasterChange = null;
        for (String dependentValue : dependentValues) {
          String state = row.cellState.get(dependentValue).get();
          if (!STATE_HIDDEN.equals(state)) {
            EnumerationConstraintValue constraintValue = new EnumerationConstraintValue();
            constraintValue.setValue(dependentValue);
            constraintValues.add(constraintValue);
          }
          if (STATE_DEFAULT.equals(state)) {
            valueForMasterChange = dependentValue;
          }
        }
        constraint.setConstraintValues(constraintValues);
        constraint.setValueForMasterChange(valueForMasterChange);
        constraints.add(constraint);
      }
    }
    config.getConstraints().clear();
    config.getConstraints().addAll(constraints);
    commitChange();
    constraintsTable.refresh();
  }

  private DependentEnumeration getOrCreate() {
    if (entry.getDependentEnumeration() == null) {
      entry.setDependentEnumeration(new DependentEnumeration());
    }
    return entry.getDependentEnumeration();
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

  private static StringConverter<String> stateConverter() {
    return new StringConverter<>() {
      @Override
      public String toString(String token) {
        if (STATE_HIDDEN.equals(token)) {
          return StudioBundle.get("dependent_enumeration_state_hidden");
        }
        if (STATE_DEFAULT.equals(token)) {
          return StudioBundle.get("dependent_enumeration_state_default");
        }
        return StudioBundle.get("dependent_enumeration_state_visible");
      }

      @Override
      public String fromString(String string) {
        return string;
      }
    };
  }

  /** One grid cell: a Hidden/Visible/Default picker for one (master value, dependent value) pair. Selecting
   * "Default" clears any other cell in the same row that was Default - only one dependent value can be the
   * master-change default per master value, mirroring SME's single {@code valueForMasterChange}. */
  private final class StateCell extends TableCell<ConstraintRow, ConstraintRow> {
    private final ComboBox<String> combo = new ComboBox<>();
    private final String dependentValue;
    private boolean guarding;

    StateCell(String dependentValue) {
      this.dependentValue = dependentValue;
      combo.setMaxWidth(Double.MAX_VALUE);
      combo.getItems().setAll(STATE_HIDDEN, STATE_VISIBLE, STATE_DEFAULT);
      combo.setConverter(stateConverter());
      combo.valueProperty().addListener((obs, oldVal, newVal) -> {
        if (guarding || newVal == null) {
          return;
        }
        ConstraintRow row = getItem();
        if (row == null) {
          return;
        }
        row.cellState.get(dependentValue).set(newVal);
        if (STATE_DEFAULT.equals(newVal)) {
          row.cellState.forEach((otherValue, state) -> {
            if (!otherValue.equals(dependentValue) && STATE_DEFAULT.equals(state.get())) {
              state.set(STATE_VISIBLE);
            }
          });
        }
      });
    }

    @Override
    protected void updateItem(ConstraintRow row, boolean empty) {
      super.updateItem(row, empty);
      if (empty || row == null) {
        setGraphic(null);
        return;
      }
      guarding = true;
      combo.setValue(row.cellState.get(dependentValue).get());
      guarding = false;
      setGraphic(combo);
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
