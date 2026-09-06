package de.a12.studio.ui.editors.formmodel.formtree.nodeeditors;

import de.a12.studio.models.documentmodel.EnumerationFieldType;
import de.a12.studio.models.formmodel.DependentEnumeration;
import de.a12.studio.models.formmodel.DependentEnumerationConstraint;
import de.a12.studio.models.formmodel.EnumerationConstraintValue;
import de.a12.studio.models.formmodel.FieldConfigEntry;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.modelsvalidation.validators.ElementIndex;
import de.a12.studio.ui.editors.formmodel.formtree.nodeeditors.HideConditionPanelController.MasterFieldScope;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.events.StudioEventManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.TextFieldTableCell;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * "Dependent Enumeration" property editor for a {@link FieldConfigEntry}: constrains which of the field's
 * own Enumeration values are offered, based on a master field's value - distinct from
 * {@code dependentField}, which only affects visibility/readonly. Reused both from the Control node editor
 * ({@link FormNodeEditorControlPanelController}) and from the model-wide Data Configuration tab
 * ({@code DataConfigurationPanelController}).
 * <p>
 * Every column is plain text (master value, comma-separated constraint values, value-on-master-change)
 * rather than pickers backed by the master/dependent fields' own declared enum values - a deliberate scope
 * simplification (this still round-trips the full data shape correctly; it just doesn't offer autocomplete
 * or reject a typo'd value the way SME's editor does).
 */
public class DependentEnumerationPanelController implements Initializable {

  private static final String NO_VALUE_DISPLAY = "(no value)";

  @FXML
  private ComboBox<String> masterFieldCombo;
  @FXML
  private TableView<DependentEnumerationConstraint> constraintsTable;
  @FXML
  private TableColumn<DependentEnumerationConstraint, String> masterValueColumn;
  @FXML
  private TableColumn<DependentEnumerationConstraint, String> constraintValuesColumn;
  @FXML
  private TableColumn<DependentEnumerationConstraint, String> valueForMasterChangeColumn;
  @FXML
  private Button addButton;
  @FXML
  private Button removeButton;

  private boolean updatingFromModel;

  private FieldConfigEntry entry;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    constraintsTable.setEditable(true);
    constraintsTable.setItems(FXCollections.observableArrayList());

    masterValueColumn.setCellValueFactory(data ->
        new javafx.beans.property.SimpleStringProperty(displayOf(data.getValue().getMasterValue())));
    masterValueColumn.setCellFactory(TextFieldTableCell.forTableColumn());
    masterValueColumn.setOnEditCommit(event -> {
      event.getRowValue().setMasterValue(storedOf(event.getNewValue()));
      commitChange();
    });

    constraintValuesColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
        joinValues(data.getValue().getConstraintValues())));
    constraintValuesColumn.setCellFactory(TextFieldTableCell.forTableColumn());
    constraintValuesColumn.setOnEditCommit(event -> {
      event.getRowValue().setConstraintValues(parseValues(event.getNewValue()));
      commitChange();
    });

    valueForMasterChangeColumn.setCellValueFactory(data ->
        new javafx.beans.property.SimpleStringProperty(data.getValue().getValueForMasterChange()));
    valueForMasterChangeColumn.setCellFactory(TextFieldTableCell.forTableColumn());
    valueForMasterChangeColumn.setOnEditCommit(event -> {
      event.getRowValue().setValueForMasterChange(
          event.getNewValue() == null || event.getNewValue().isBlank() ? null : event.getNewValue());
      commitChange();
    });

    masterFieldCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
      if (updatingFromModel) {
        return;
      }
      String fieldValue = (newVal == null || newVal.isBlank()) ? null : newVal;
      if (fieldValue == null) {
        entry.setDependentEnumeration(null);
        constraintsTable.getItems().clear();
      }
      else {
        getOrCreate().setMasterField(fieldValue);
      }
      commitChange();
    });

    addButton.setOnAction(e -> {
      if (masterFieldCombo.getValue() == null) {
        return;
      }
      DependentEnumerationConstraint constraint = new DependentEnumerationConstraint();
      getOrCreate().getConstraints().add(constraint);
      constraintsTable.getItems().add(constraint);
      commitChange();
    });

    removeButton.setOnAction(e -> {
      DependentEnumerationConstraint selected = constraintsTable.getSelectionModel().getSelectedItem();
      if (selected == null) {
        return;
      }
      DependentEnumeration dependentEnumeration = entry.getDependentEnumeration();
      if (dependentEnumeration != null) {
        dependentEnumeration.getConstraints().remove(selected);
      }
      constraintsTable.getItems().remove(selected);
      commitChange();
    });
  }

  public void setEntry(@NonNull FieldConfigEntry entry, @Nullable ElementIndex elementIndex, @NonNull MasterFieldScope scope) {
    this.entry = entry;

    List<String> masterFieldIds = HideConditionPanelController.collectMasterFieldIds(elementIndex, scope,
        (index, field) -> field.getField() != null && index.effectiveFieldType(field.getField().getFieldType()) instanceof EnumerationFieldType);

    updatingFromModel = true;
    try {
      masterFieldCombo.getItems().setAll(masterFieldIds);
      DependentEnumeration dependentEnumeration = entry.getDependentEnumeration();
      masterFieldCombo.setValue(dependentEnumeration == null ? null : dependentEnumeration.getMasterField());
      ObservableList<DependentEnumerationConstraint> items = FXCollections.observableArrayList();
      if (dependentEnumeration != null) {
        items.addAll(dependentEnumeration.getConstraints());
      }
      constraintsTable.setItems(items);
    } finally {
      updatingFromModel = false;
    }
  }

  private DependentEnumeration getOrCreate() {
    if (entry.getDependentEnumeration() == null) {
      entry.setDependentEnumeration(new DependentEnumeration());
    }
    return entry.getDependentEnumeration();
  }

  private static String displayOf(@Nullable String stored) {
    return stored == null ? NO_VALUE_DISPLAY : stored;
  }

  private static @Nullable String storedOf(@Nullable String display) {
    return (display == null || NO_VALUE_DISPLAY.equals(display)) ? null : display;
  }

  private static String joinValues(@Nullable List<EnumerationConstraintValue> values) {
    if (values == null) {
      return "";
    }
    return values.stream().map(EnumerationConstraintValue::getValue).collect(Collectors.joining(", "));
  }

  private static List<EnumerationConstraintValue> parseValues(@Nullable String text) {
    List<EnumerationConstraintValue> values = new ArrayList<>();
    if (text == null || text.isBlank()) {
      return values;
    }
    for (String token : text.split(",")) {
      String trimmed = token.strip();
      if (!trimmed.isEmpty()) {
        EnumerationConstraintValue value = new EnumerationConstraintValue();
        value.setValue(trimmed);
        values.add(value);
      }
    }
    return values;
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
