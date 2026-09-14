package de.a12.studio.ui.editors.formmodel;

import de.a12.studio.models.documentmodel.Element;
import de.a12.studio.models.documentmodel.EnumerationFieldType;
import de.a12.studio.models.documentmodel.FieldElement;
import de.a12.studio.models.documentmodel.FieldType;
import de.a12.studio.models.documentmodel.GroupElement;
import de.a12.studio.models.documentmodel.StringFieldType;
import de.a12.studio.models.formmodel.FieldConfigEntry;
import de.a12.studio.models.formmodel.FieldConfiguration;
import de.a12.studio.models.formmodel.FormModelContent;
import de.a12.studio.models.formmodel.GroupConfigEntry;
import de.a12.studio.models.formmodel.GroupConfiguration;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.modelsvalidation.validators.ElementIndex;
import de.a12.studio.ui.editors.documentmodel.ElementViewModel;
import de.a12.studio.ui.editors.formmodel.formtree.nodeeditors.DependentEnumerationPanelController;
import de.a12.studio.ui.editors.formmodel.formtree.nodeeditors.DependentFieldPanelController;
import de.a12.studio.ui.editors.formmodel.formtree.nodeeditors.DependentGroupPanelController;
import de.a12.studio.ui.editors.formmodel.formtree.nodeeditors.ExternalEnumerationPanelController;
import de.a12.studio.ui.editors.formmodel.formtree.nodeeditors.HideConditionPanelController.MasterFieldScope;
import de.a12.studio.ui.editors.propertyeditors.AnnotationsPanelController;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.events.StudioEventManager;
import de.a12.studio.ui.util.StudioBundle;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.util.StringConverter;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;

/**
 * "Data Configuration" tab for the Form Model editor: a single place to see and configure every field/group
 * configuration entry's dependency setup, mirroring SME's dedicated Data Configuration tab. Previously this
 * was only reachable per-Control from the Screens tree (and only for {@code dependentEnumeration}/{@code
 * externalEnumeration} - {@code dependentField}/{@code dependentGroup} had no editor at all anywhere), which
 * meant a field/group referenced by more than one Control, or not yet placed in any screen, had no way to be
 * configured.
 * <p>
 * Left: a flat table of every {@link FieldConfigEntry}/{@link GroupConfigEntry} the model already has (an
 * entry only exists once something - typically a Control/Repeat, or this tab itself via {@link #onAddField}/
 * {@link #onAddGroup} - has referenced that field/group; see {@link de.a12.studio.ui.editors.formmodel.formtree.nodeeditors.FieldConfigEntryHelper}
 * for the equivalent per-Control lookup). Right: the selected entry's dependency editors.
 */
public class DataConfigurationPanelController implements Initializable {

  private record Row(Object entry, String reference, boolean isField) {
  }

  @FXML
  private TableView<Row> table;
  @FXML
  private TableColumn<Row, String> elementColumn;
  @FXML
  private TableColumn<Row, String> typeColumn;
  @FXML
  private TableColumn<Row, String> dataTypeColumn;
  @FXML
  private ComboBox<String> addFieldCombo;
  @FXML
  private Button addFieldButton;
  @FXML
  private ComboBox<String> addGroupCombo;
  @FXML
  private Button addGroupButton;

  @FXML
  private Label noSelectionLabel;
  @FXML
  private Node fieldDetailPane;
  @FXML
  private Node groupDetailPane;

  @FXML
  private Node externalEnumeration;
  @FXML
  private Node dependentEnumeration;

  @FXML
  private ExternalEnumerationPanelController externalEnumerationController;
  @FXML
  private DependentEnumerationPanelController dependentEnumerationController;
  @FXML
  private DependentFieldPanelController dependentFieldController;
  @FXML
  private DependentGroupPanelController dependentGroupController;
  @FXML
  private AnnotationsPanelController annotationsController;
  @FXML
  private TextField numberOfInitialRowsField;

  private FormModelContent content;
  private @Nullable ElementIndex elementIndex;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    elementColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().reference()));
    typeColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(
        data.getValue().isField() ? StudioBundle.get("data_configuration_field_type") : StudioBundle.get("data_configuration_group_type")));
    dataTypeColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(
        Objects.requireNonNullElse(typeLabel(rawReference(data.getValue())), "")));

    table.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> showDetail(newVal));

    numberOfInitialRowsField.textProperty().addListener((obs, oldVal, newVal) -> {
      Row selected = table.getSelectionModel().getSelectedItem();
      if (selected == null || selected.isField()) {
        return;
      }
      GroupConfigEntry entry = (GroupConfigEntry) selected.entry();
      try {
        entry.setNumberOfInitialRows(newVal.isBlank() ? null : Integer.parseInt(newVal.strip()));
        commitChange();
      }
      catch (NumberFormatException ignored) {
        // Leave the stored value unchanged until the user types a valid integer.
      }
    });

    addFieldCombo.setConverter(elementRefConverter());
    addGroupCombo.setConverter(elementRefConverter());

    addFieldButton.disableProperty().bind(addFieldCombo.valueProperty().isNull());
    addGroupButton.disableProperty().bind(addGroupCombo.valueProperty().isNull());

    addFieldButton.setOnAction(e -> onAddField());
    addGroupButton.setOnAction(e -> onAddGroup());

    showDetail(null);
  }

  public void setModel(@NonNull FormModelContent content, @Nullable ElementIndex elementIndex) {
    this.content = content;
    this.elementIndex = elementIndex;
    refreshTable();
    refreshAddCombos();
  }

  /** Lets the user start configuring a field/group before any Control/Repeat in the tree references it. */
  private void onAddField() {
    String elementRef = addFieldCombo.getValue();
    if (elementRef == null) {
      return;
    }
    if (content.getFieldConfiguration() == null) {
      content.setFieldConfiguration(new FieldConfiguration());
    }
    FieldConfigEntry entry = new FieldConfigEntry();
    entry.setElementRef(elementRef);
    content.getFieldConfiguration().getField().add(entry);
    commitChange();
    refreshTable();
    refreshAddCombos();
    table.getItems().stream().filter(r -> r.entry() == entry).findFirst().ifPresent(r -> table.getSelectionModel().select(r));
  }

  private void onAddGroup() {
    String groupRef = addGroupCombo.getValue();
    if (groupRef == null) {
      return;
    }
    if (content.getGroupConfiguration() == null) {
      content.setGroupConfiguration(new GroupConfiguration());
    }
    GroupConfigEntry entry = new GroupConfigEntry();
    entry.setGroupRef(groupRef);
    content.getGroupConfiguration().getGroup().add(entry);
    commitChange();
    refreshTable();
    refreshAddCombos();
    table.getItems().stream().filter(r -> r.entry() == entry).findFirst().ifPresent(r -> table.getSelectionModel().select(r));
  }

  private void refreshAddCombos() {
    List<String> configuredFields = content.getFieldConfiguration() == null ? List.of()
        : content.getFieldConfiguration().getField().stream().map(FieldConfigEntry::getElementRef).toList();
    List<String> configuredGroups = content.getGroupConfiguration() == null ? List.of()
        : content.getGroupConfiguration().getGroup().stream().map(GroupConfigEntry::getGroupRef).toList();

    List<String> fieldIds = new ArrayList<>();
    List<String> groupIds = new ArrayList<>();
    if (elementIndex != null) {
      for (Element element : elementIndex.allElements()) {
        if (element instanceof FieldElement field && !configuredFields.contains(field.getId())) {
          fieldIds.add(field.getId());
        }
        else if (element instanceof GroupElement group && !configuredGroups.contains(group.getId())) {
          groupIds.add(group.getId());
        }
      }
    }
    fieldIds.sort(Comparator.comparing(this::displayName));
    groupIds.sort(Comparator.comparing(this::displayName));
    addFieldCombo.getItems().setAll(fieldIds);
    addFieldCombo.setValue(null);
    addGroupCombo.getItems().setAll(groupIds);
    addGroupCombo.setValue(null);
  }

  /** Renders a combo box's underlying element id (e.g. {@link #addFieldCombo}/{@link #addGroupCombo}'s items)
   * as its resolved display path, matching how existing entries are shown in {@link #table} via {@link
   * #displayName}, plus the element's type in brackets (e.g. "String", "Enumeration", "Group"). */
  private StringConverter<String> elementRefConverter() {
    return new StringConverter<>() {
      @Override
      public String toString(String elementRef) {
        if (elementRef == null) {
          return "";
        }
        String name = displayName(elementRef);
        String type = typeLabel(elementRef);
        return type == null || type.isBlank() ? name : name + " [" + type + "]";
      }

      @Override
      public String fromString(String string) {
        return string;
      }
    };
  }

  /** The element's type, formatted the same way as {@code DocumentModelElementsTreeController}'s Type column
   * (e.g. {@code StringType} -> {@code String}). */
  private @Nullable String typeLabel(@Nullable String elementRef) {
    if (elementIndex == null) {
      return null;
    }
    String type = elementIndex.resolveElement(elementRef).map(el -> new ElementViewModel(el).getType()).orElse(null);
    return type == null ? null : type.replaceAll("Type", "");
  }

  private String rawReference(Row row) {
    return row.isField() ? ((FieldConfigEntry) row.entry()).getElementRef() : ((GroupConfigEntry) row.entry()).getGroupRef();
  }

  private void refreshTable() {
    List<Row> rows = new ArrayList<>();
    if (content.getFieldConfiguration() != null) {
      for (FieldConfigEntry entry : content.getFieldConfiguration().getField()) {
        rows.add(new Row(entry, displayName(entry.getElementRef()), true));
      }
    }
    if (content.getGroupConfiguration() != null) {
      for (GroupConfigEntry entry : content.getGroupConfiguration().getGroup()) {
        rows.add(new Row(entry, displayName(entry.getGroupRef()), false));
      }
    }
    Row previouslySelected = table.getSelectionModel().getSelectedItem();
    table.getItems().setAll(rows);
    if (previouslySelected != null) {
      rows.stream().filter(r -> r.entry() == previouslySelected.entry()).findFirst()
          .ifPresent(r -> table.getSelectionModel().select(r));
    }
  }

  /** Resolves the effective (type-definition-aware) {@link FieldType} of a field element reference, or
   * {@code null} if it doesn't resolve to a {@link FieldElement}. */
  private @Nullable FieldType resolveFieldType(@Nullable String elementRef) {
    if (elementIndex == null) {
      return null;
    }
    return elementIndex.resolveElement(elementRef)
        .filter(FieldElement.class::isInstance)
        .map(el -> ((FieldElement) el).getField())
        .filter(Objects::nonNull)
        .map(field -> elementIndex.effectiveFieldType(field.getFieldType()))
        .orElse(null);
  }

  private String displayName(@Nullable String reference) {
    if (reference == null || reference.isBlank()) {
      return "";
    }
    if (elementIndex == null) {
      return reference;
    }
    String path = elementIndex.resolveDisplayPath(reference);
    return path != null ? path : reference;
  }

  private void showDetail(@Nullable Row row) {
    boolean isField = row != null && row.isField();
    boolean isGroup = row != null && !row.isField();

    noSelectionLabel.setVisible(row == null);
    noSelectionLabel.setManaged(row == null);
    fieldDetailPane.setVisible(isField);
    fieldDetailPane.setManaged(isField);
    groupDetailPane.setVisible(isGroup);
    groupDetailPane.setManaged(isGroup);

    if (isField) {
      FieldConfigEntry entry = (FieldConfigEntry) row.entry();
      FieldType fieldType = resolveFieldType(entry.getElementRef());
      boolean isStringField = fieldType instanceof StringFieldType;
      boolean isEnumerationField = fieldType instanceof EnumerationFieldType;

      // externalEnumeration only applies to string-typed fields (its values replace what would otherwise be
      // free text), dependentEnumeration only to enumeration-like fields (it filters that field's own enum
      // values) - see SME's "External Enumeration"/"Dependent Enumeration" docs.
      externalEnumeration.setVisible(isStringField);
      externalEnumeration.setManaged(isStringField);
      dependentEnumeration.setVisible(isEnumerationField);
      dependentEnumeration.setManaged(isEnumerationField);

      externalEnumerationController.setEntry(entry);
      dependentEnumerationController.setEntry(entry, elementIndex,
          MasterFieldScope.anchoredOrUnbound(entry.getElementRef(), elementIndex));
      dependentFieldController.setEntry(entry, elementIndex);
      annotationsController.setCustom(entry::getAnnotations);
    }
    else if (isGroup) {
      GroupConfigEntry entry = (GroupConfigEntry) row.entry();
      dependentGroupController.setEntry(entry, elementIndex);
      numberOfInitialRowsField.setText(entry.getNumberOfInitialRows() == null ? "" : entry.getNumberOfInitialRows().toString());
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
