package de.a12.studio.ui.editors.formmodel.formtree.nodeeditors;

import de.a12.studio.models.documentmodel.FieldElement;
import de.a12.studio.models.formmodel.DependentCase;
import de.a12.studio.models.formmodel.DependentConfig;
import de.a12.studio.models.formmodel.GroupConfigEntry;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.modelsvalidation.validators.ElementIndex;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.editors.AbstractPropertyEditor;
import de.a12.studio.ui.editors.formmodel.formtree.nodeeditors.HideConditionPanelController.MasterFieldScope;
import de.a12.studio.ui.events.StudioEventManager;
import de.a12.studio.ui.util.StudioBundle;
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
import javafx.scene.control.TitledPane;
import javafx.scene.layout.HBox;
import javafx.util.StringConverter;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

/**
 * "Dependent Group" property editor for a {@link GroupConfigEntry}: makes this group readonly/not-relevant
 * based on a master field's value, mirroring SME's {@code dependentGroup} group configuration section and its
 * grid editor ({@code DependentFieldGroupTable} in {@code dependencyTable.tsx}, group branch). Reuses {@link
 * DependentCase}/{@link DependentConfig} (the same shape {@code dependentField} uses), but never sets {@code
 * value}/{@code fieldRef} - those are field-only - and, unlike {@link DependentFieldPanelController}, has no
 * "Default" display option: a group row is either "Not Relevant" or "Read Only".
 * <p>
 * As with the field panel, the table shows one row per value the selected master field can actually take (see
 * {@link DependentCaseSupport#masterValueOptions}), not just whatever {@code case} entries already happen to
 * be in the file; {@link #syncCasesFromRows} rebuilds the whole {@code case} list from the table after every
 * edit.
 */
public class DependentGroupPanelController implements Initializable {

  static final String DISPLAY_NOT_RELEVANT = "NOT_RELEVANT";
  static final String DISPLAY_READONLY = "READONLY";

  private static final class CaseRow {
    final String display;
    final @Nullable String storedValue;
    final BooleanProperty enabled = new SimpleBooleanProperty(false);
    final StringProperty displayOption = new SimpleStringProperty(DISPLAY_NOT_RELEVANT);

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

  private boolean updatingFromModel;
  private GroupConfigEntry entry;
  private @Nullable ElementIndex elementIndex;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    AbstractPropertyEditor.persistExpandedState(root, getClass());

    masterFieldCombo.setConverter(elementRefConverter());
    DependentCaseSupport.wireMasterFieldCopyButton(copyMasterFieldButton, masterFieldCombo);
    casesTable.setItems(FXCollections.observableArrayList());

    triggerColumn.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue()));
    triggerColumn.setCellFactory(col -> new TriggerCell());

    displayColumn.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue()));
    displayColumn.setCellFactory(col -> new DisplayCell());

    masterFieldCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
      if (updatingFromModel) {
        return;
      }
      String fieldValue = (newVal == null || newVal.isBlank()) ? null : newVal;
      if (fieldValue == null) {
        entry.setDependentGroup(null);
      }
      else {
        getOrCreate().setMasterField(fieldValue);
      }
      rebuildRows();
      syncCasesFromRows();
    });
  }

  /** Master field candidates are anchored to this entry's own group, mirroring SME's {@code getMasterField}
   * (called with the group configuration entry's {@code groupRef}) - so fields reachable through the same repeat
   * context as the group being configured are offered, not just fields reachable from the document model root. */
  public void setEntry(@NonNull GroupConfigEntry entry, @Nullable ElementIndex elementIndex) {
    this.entry = entry;
    this.elementIndex = elementIndex;

    MasterFieldScope scope = MasterFieldScope.anchoredOrUnbound(entry.getGroupRef(), elementIndex);
    List<String> masterFieldIds = HideConditionPanelController.collectMasterFieldIds(elementIndex, scope,
        (index, field) -> field.getField() != null
            && DependentCaseSupport.isEnumerableMasterType(index.effectiveFieldType(field.getField().getFieldType())));

    updatingFromModel = true;
    try {
      masterFieldCombo.getItems().setAll(masterFieldIds);
      masterFieldCombo.setValue(entry.getDependentGroup() == null ? null : entry.getDependentGroup().getMasterField());
      rebuildRows();
    }
    finally {
      updatingFromModel = false;
    }
  }

  private void rebuildRows() {
    List<CaseRow> rows = new ArrayList<>();
    String masterFieldId = masterFieldCombo.getValue();
    FieldElement masterField = masterFieldId == null || elementIndex == null ? null
        : elementIndex.resolveElement(masterFieldId).filter(FieldElement.class::isInstance).map(FieldElement.class::cast).orElse(null);

    if (masterField != null && elementIndex != null) {
      List<DependentCase> existingCases = entry.getDependentGroup() == null ? List.of() : entry.getDependentGroup().getCases();
      for (DependentCaseSupport.MasterValueOption option
          : DependentCaseSupport.masterValueOptions(masterField, StudioBundle.get("dependent_field_no_selection"), elementIndex)) {
        CaseRow row = new CaseRow(option.display(), option.storedValue());
        existingCases.stream().filter(c -> valuesEqual(c.getMasterValue(), option.storedValue())).findFirst()
            .ifPresent(existing -> {
              row.enabled.set(true);
              row.displayOption.set(Boolean.TRUE.equals(existing.getReadonly()) ? DISPLAY_READONLY : DISPLAY_NOT_RELEVANT);
            });
        wireRow(row);
        rows.add(row);
      }
    }
    casesTable.setItems(FXCollections.observableArrayList(rows));
  }

  private void wireRow(CaseRow row) {
    row.enabled.addListener((obs, oldVal, newVal) -> syncCasesFromRows());
    row.displayOption.addListener((obs, oldVal, newVal) -> syncCasesFromRows());
  }

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
      boolean readonly = DISPLAY_READONLY.equals(row.displayOption.get());
      newCase.setNotRelevant(readonly ? null : Boolean.TRUE);
      newCase.setReadonly(readonly ? Boolean.TRUE : null);
      cases.add(newCase);
    }
    config.getCases().clear();
    config.getCases().addAll(cases);
    commitChange();
    casesTable.refresh();
  }

  private static boolean valuesEqual(@Nullable String a, @Nullable String b) {
    return (a == null && b == null) || (a != null && a.equals(b));
  }

  private DependentConfig getOrCreate() {
    if (entry.getDependentGroup() == null) {
      entry.setDependentGroup(new DependentConfig());
    }
    return entry.getDependentGroup();
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
        return DISPLAY_READONLY.equals(token) ? StudioBundle.get("readonly") : StudioBundle.get("dependent_field_not_relevant_column");
      }

      @Override
      public String fromString(String string) {
        return string;
      }
    };
  }

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

  private final class DisplayCell extends TableCell<CaseRow, CaseRow> {
    private final ComboBox<String> combo = new ComboBox<>();
    private boolean guarding;

    DisplayCell() {
      combo.setMaxWidth(Double.MAX_VALUE);
      combo.setConverter(displayConverter());
      combo.getItems().setAll(DISPLAY_NOT_RELEVANT, DISPLAY_READONLY);
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
      guarding = true;
      combo.setValue(row.displayOption.get());
      guarding = false;
      combo.setDisable(!row.enabled.get());
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
