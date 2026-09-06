package de.a12.studio.ui.editors.formmodel.formtree.nodeeditors;

import de.a12.studio.models.documentmodel.BooleanFieldType;
import de.a12.studio.models.documentmodel.ConfirmFieldType;
import de.a12.studio.models.documentmodel.EnumerationFieldType;
import de.a12.studio.models.formmodel.DependentCase;
import de.a12.studio.models.formmodel.DependentConfig;
import de.a12.studio.models.formmodel.GroupConfigEntry;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.modelsvalidation.validators.ElementIndex;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.editors.formmodel.formtree.nodeeditors.HideConditionPanelController.MasterFieldScope;
import de.a12.studio.ui.events.StudioEventManager;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.TextFieldTableCell;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * "Dependent Group" property editor for a {@link GroupConfigEntry}: makes this group readonly/not-relevant
 * based on a master field's value, mirroring SME's {@code dependentGroup} group configuration section.
 * Previously had no editor at all. Reuses {@link DependentCase}/{@link DependentConfig} (the same shape
 * {@code dependentField} uses), but never sets {@code value}/{@code fieldRef} - those are field-only.
 */
public class DependentGroupPanelController implements Initializable {

  private static final String NO_VALUE_DISPLAY = "(no value)";

  @FXML
  private ComboBox<String> masterFieldCombo;
  @FXML
  private TableView<DependentCase> casesTable;
  @FXML
  private TableColumn<DependentCase, String> masterValueColumn;
  @FXML
  private TableColumn<DependentCase, Boolean> notRelevantColumn;
  @FXML
  private TableColumn<DependentCase, Boolean> readonlyColumn;
  @FXML
  private Button addButton;
  @FXML
  private Button removeButton;

  private boolean updatingFromModel;
  private GroupConfigEntry entry;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    casesTable.setEditable(true);
    casesTable.setItems(FXCollections.observableArrayList());

    masterValueColumn.setCellValueFactory(data -> new SimpleStringProperty(displayOf(data.getValue().getMasterValue())));
    masterValueColumn.setCellFactory(TextFieldTableCell.forTableColumn());
    masterValueColumn.setOnEditCommit(event -> {
      event.getRowValue().setMasterValue(storedOf(event.getNewValue()));
      commitChange();
    });

    notRelevantColumn.setCellValueFactory(data ->
        new SimpleBooleanProperty(Boolean.TRUE.equals(data.getValue().getNotRelevant())));
    notRelevantColumn.setCellFactory(CheckBoxTableCell.forTableColumn(notRelevantColumn));
    notRelevantColumn.setOnEditCommit(event -> {
      event.getRowValue().setNotRelevant(event.getNewValue() ? Boolean.TRUE : null);
      commitChange();
    });

    readonlyColumn.setCellValueFactory(data ->
        new SimpleBooleanProperty(Boolean.TRUE.equals(data.getValue().getReadonly())));
    readonlyColumn.setCellFactory(CheckBoxTableCell.forTableColumn(readonlyColumn));
    readonlyColumn.setOnEditCommit(event -> {
      event.getRowValue().setReadonly(event.getNewValue() ? Boolean.TRUE : null);
      commitChange();
    });

    masterFieldCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
      if (updatingFromModel) {
        return;
      }
      String fieldValue = (newVal == null || newVal.isBlank()) ? null : newVal;
      if (fieldValue == null) {
        entry.setDependentGroup(null);
        casesTable.getItems().clear();
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
      DependentCase newCase = new DependentCase();
      getOrCreate().getCases().add(newCase);
      casesTable.getItems().add(newCase);
      commitChange();
    });

    removeButton.setOnAction(e -> {
      DependentCase selected = casesTable.getSelectionModel().getSelectedItem();
      if (selected == null) {
        return;
      }
      DependentConfig config = entry.getDependentGroup();
      if (config != null) {
        config.getCases().remove(selected);
      }
      casesTable.getItems().remove(selected);
      commitChange();
    });
  }

  /** Master field candidates are collected with root scope: a group config entry isn't anchored to one tree position. */
  public void setEntry(@NonNull GroupConfigEntry entry, @Nullable ElementIndex elementIndex) {
    this.entry = entry;

    List<String> masterFieldIds = HideConditionPanelController.collectMasterFieldIds(elementIndex, MasterFieldScope.root(),
        (index, field) -> field.getField() != null && isCompatibleMasterType(index.effectiveFieldType(field.getField().getFieldType())));

    updatingFromModel = true;
    try {
      masterFieldCombo.getItems().setAll(masterFieldIds);
      DependentConfig config = entry.getDependentGroup();
      masterFieldCombo.setValue(config == null ? null : config.getMasterField());
      ObservableList<DependentCase> items = FXCollections.observableArrayList();
      if (config != null) {
        items.addAll(config.getCases());
      }
      casesTable.setItems(items);
    } finally {
      updatingFromModel = false;
    }
  }

  private static boolean isCompatibleMasterType(Object effectiveType) {
    return effectiveType instanceof BooleanFieldType || effectiveType instanceof ConfirmFieldType
        || effectiveType instanceof EnumerationFieldType;
  }

  private DependentConfig getOrCreate() {
    if (entry.getDependentGroup() == null) {
      entry.setDependentGroup(new DependentConfig());
    }
    return entry.getDependentGroup();
  }

  private static String displayOf(@Nullable String stored) {
    return stored == null ? NO_VALUE_DISPLAY : stored;
  }

  private static @Nullable String storedOf(@Nullable String display) {
    return (display == null || NO_VALUE_DISPLAY.equals(display)) ? null : display;
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
