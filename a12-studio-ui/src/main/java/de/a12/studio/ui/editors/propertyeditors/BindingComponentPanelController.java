package de.a12.studio.ui.editors.propertyeditors;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.Label;
import de.a12.studio.models.ModelType;
import de.a12.studio.models.formmodel.BindingComponent;
import de.a12.studio.models.formmodel.BindingComponentModelsSme;
import de.a12.studio.models.formmodel.BindingComponentPropsExtensions;
import de.a12.studio.models.formmodel.BindingComponentTableLabel;
import de.a12.studio.models.formmodel.BindingComponentType;
import de.a12.studio.models.formmodel.DualPaneProps;
import de.a12.studio.models.formmodel.TableListProps;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.ui.editors.AbstractPropertyEditor;
import de.a12.studio.ui.util.ProjectDocumentModels;
import de.a12.studio.ui.util.StudioBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Which widget (drop-down, dual-pane selection, or an editable table-list) renders a {@link
 * de.a12.studio.models.formmodel.Binding}'s candidates/links, and that widget's own configuration - a {@link
 * BindingComponent}. Reused for both a Binding's {@code mainComponent} and {@code editModalComponent} (see
 * {@code FormNodeEditorBindingPanelController}), so it lives in the shared {@code propertyeditors} package per
 * CLAUDE.md's package-placement rule; the two embeds use {@link #configure} to set distinct titles.
 * <p>
 * Field visibility mirrors SME's own {@code I_BindingComponent.json} rules: the "available items" reference and
 * page size don't apply to {@link BindingComponentType#TABLE_LIST} (its rows come from the bound relationship
 * directly, not a separate candidate list); the dual-pane table titles only apply to {@link
 * BindingComponentType#DUAL_PANE_SELECTION}; the edit-dialog fields only apply to {@link
 * BindingComponentType#TABLE_LIST}.
 */
public class BindingComponentPanelController extends AbstractPropertyEditor implements Initializable {

  @FXML
  private ComboBox<BindingComponentType> typeCombo;
  @FXML
  private ComboBox<String> availableItemsOverviewCombo;
  @FXML
  private ComboBox<String> selectedItemsOverviewCombo;
  @FXML
  private ComboBox<String> additionalFieldsFormCombo;
  @FXML
  private TextField candidatePageSizeField;
  @FXML
  private TextField linkPageSizeField;
  @FXML
  private VBox dualPaneSection;
  @FXML
  private TextField heightField;
  @FXML
  private LocalizedTextPanelController availableItemsTableLabelController;
  @FXML
  private LocalizedTextPanelController selectedItemsTableLabelController;
  @FXML
  private VBox tableListSection;
  @FXML
  private ComboBox<String> editComponentCombo;
  @FXML
  private TextField editDialogWidthField;
  @FXML
  private LocalizedTextPanelController editDialogTitleController;
  @FXML
  private LocalizedTextPanelController editDialogCancelButtonLabelController;
  @FXML
  private LocalizedTextPanelController editDialogCloseButtonLabelController;

  private Supplier<BindingComponent> reader;
  private Consumer<BindingComponent> writer;
  private boolean updatingFromModel;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    super.initialize(location, resources);
    availableItemsTableLabelController.configureCustom("availableItemsTable", StudioBundle.get("binding_component_panel.available_items_table"));
    selectedItemsTableLabelController.configureCustom("selectedItemsTable", StudioBundle.get("binding_component_panel.selected_items_table"));
    editDialogTitleController.configureCustom("editDialogTitle", StudioBundle.get("binding_component_panel.edit_dialog_title"));
    editDialogCancelButtonLabelController.configureCustom("editDialogCancelButtonLabel", StudioBundle.get("binding_component_panel.cancel_button_label"));
    editDialogCloseButtonLabelController.configureCustom("editDialogCloseButtonLabel", StudioBundle.get("binding_component_panel.close_button_label"));

    typeCombo.getItems().setAll(BindingComponentType.values());
    typeCombo.valueProperty().addListener((observable, oldValue, newValue) -> {
      if (updatingFromModel) {
        return;
      }
      component().setName(newValue);
      updateSectionVisibility(newValue);
      commitChange();
    });

    bindPlainComboBox(availableItemsOverviewCombo, value -> modelsSme().setAvailableItemsOverview(value));
    bindPlainComboBox(selectedItemsOverviewCombo, value -> modelsSme().setSelectedItemsOverview(value));
    bindPlainComboBox(additionalFieldsFormCombo, value -> modelsSme().setAdditionalFieldsForm(value));
    bindPlainComboBox(editComponentCombo, value -> tableListProps().setEditComponent(value));

    bindIntegerField(candidatePageSizeField, value -> component().setCandidatePageSize(value));
    bindIntegerField(linkPageSizeField, value -> component().setLinkPageSize(value));
    bindIntegerField(heightField, value -> dualPaneProps().setHeight(value));
    bindIntegerField(editDialogWidthField, value -> tableListProps().setEditDialogWidth(value));
  }

  /** Sets this panel's title/settings-key, e.g. "Main Component" vs. "Edit Modal Component". */
  public void configure(@NonNull String title, @NonNull String settingsKeySuffix) {
    setTitle(title);
    setSettingsKeySuffix(settingsKeySuffix);
  }

  /**
   * Binds this panel to one of a Binding's component slots. {@code reader} may return {@code null} (no
   * component configured yet); {@code writer} installs a newly created {@link BindingComponent} into the
   * owning slot the first time the user actually picks a type.
   */
  public void setComponent(@NonNull Supplier<BindingComponent> reader, @NonNull Consumer<BindingComponent> writer,
      @NonNull ProjectItem projectItem) {
    this.reader = reader;
    this.writer = writer;

    List<String> overviewModelIds = modelIds(projectItem, ModelType.OVERVIEW);
    List<String> formModelIds = modelIds(projectItem, ModelType.FORM);

    updatingFromModel = true;
    try {
      BindingComponent current = reader.get();
      typeCombo.setValue(current == null ? null : current.getName());
      availableItemsOverviewCombo.getItems().setAll(overviewModelIds);
      selectedItemsOverviewCombo.getItems().setAll(overviewModelIds);
      additionalFieldsFormCombo.getItems().setAll(formModelIds);
      editComponentCombo.getItems().setAll(formModelIds);

      BindingComponentModelsSme modelsSme = current == null ? null : current.getModelsSME();
      availableItemsOverviewCombo.setValue(modelsSme == null ? null : modelsSme.getAvailableItemsOverview());
      selectedItemsOverviewCombo.setValue(modelsSme == null ? null : modelsSme.getSelectedItemsOverview());
      additionalFieldsFormCombo.setValue(modelsSme == null ? null : modelsSme.getAdditionalFieldsForm());
      setFieldValue(candidatePageSizeField, current == null || current.getCandidatePageSize() == null ? "" : String.valueOf(current.getCandidatePageSize()));
      setFieldValue(linkPageSizeField, current == null || current.getLinkPageSize() == null ? "" : String.valueOf(current.getLinkPageSize()));

      DualPaneProps dualPaneProps = current == null || current.getPropsExtensions() == null ? null : current.getPropsExtensions().getDualPaneProps();
      setFieldValue(heightField, dualPaneProps == null || dualPaneProps.getHeight() == null ? "" : String.valueOf(dualPaneProps.getHeight()));
      availableItemsTableLabelController.setCustom(
          () -> dualPaneProps == null || dualPaneProps.getAvailableItemsTable() == null ? List.of() : dualPaneProps.getAvailableItemsTable().getLabel(),
          () -> dualPaneProps().getAvailableItemsTable() != null ? dualPaneProps().getAvailableItemsTable().getLabel()
              : createTableLabel(dualPaneProps()::setAvailableItemsTable));
      selectedItemsTableLabelController.setCustom(
          () -> dualPaneProps == null || dualPaneProps.getSelectedItemsTable() == null ? List.of() : dualPaneProps.getSelectedItemsTable().getLabel(),
          () -> dualPaneProps().getSelectedItemsTable() != null ? dualPaneProps().getSelectedItemsTable().getLabel()
              : createTableLabel(dualPaneProps()::setSelectedItemsTable));

      TableListProps tableListProps = current == null || current.getPropsExtensions() == null ? null : current.getPropsExtensions().getTableListProps();
      editComponentCombo.setValue(tableListProps == null ? null : tableListProps.getEditComponent());
      setFieldValue(editDialogWidthField, tableListProps == null || tableListProps.getEditDialogWidth() == null ? "" : String.valueOf(tableListProps.getEditDialogWidth()));
      editDialogTitleController.setCustom(
          () -> tableListProps == null || tableListProps.getEditDialogTitle() == null ? List.of() : tableListProps.getEditDialogTitle().getLabel(),
          () -> tableListProps().getEditDialogTitle() != null ? tableListProps().getEditDialogTitle().getLabel()
              : createTableLabel(tableListProps()::setEditDialogTitle));
      editDialogCancelButtonLabelController.setCustom(
          () -> tableListProps == null || tableListProps.getEditDialogCancelButtonLabel() == null ? List.of() : tableListProps.getEditDialogCancelButtonLabel().getLabel(),
          () -> tableListProps().getEditDialogCancelButtonLabel() != null ? tableListProps().getEditDialogCancelButtonLabel().getLabel()
              : createTableLabel(tableListProps()::setEditDialogCancelButtonLabel));
      editDialogCloseButtonLabelController.setCustom(
          () -> tableListProps == null || tableListProps.getEditDialogCloseButtonLabel() == null ? List.of() : tableListProps.getEditDialogCloseButtonLabel().getLabel(),
          () -> tableListProps().getEditDialogCloseButtonLabel() != null ? tableListProps().getEditDialogCloseButtonLabel().getLabel()
              : createTableLabel(tableListProps()::setEditDialogCloseButtonLabel));

      updateSectionVisibility(typeCombo.getValue());
    }
    finally {
      updatingFromModel = false;
    }
  }

  private void updateSectionVisibility(@Nullable BindingComponentType type) {
    boolean showAvailableItems = type != BindingComponentType.TABLE_LIST;
    availableItemsOverviewCombo.setVisible(showAvailableItems);
    availableItemsOverviewCombo.setManaged(showAvailableItems);
    candidatePageSizeField.setVisible(showAvailableItems);
    candidatePageSizeField.setManaged(showAvailableItems);

    boolean dualPane = type == BindingComponentType.DUAL_PANE_SELECTION;
    dualPaneSection.setVisible(dualPane);
    dualPaneSection.setManaged(dualPane);

    boolean tableList = type == BindingComponentType.TABLE_LIST;
    tableListSection.setVisible(tableList);
    tableListSection.setManaged(tableList);
  }

  private List<String> modelIds(ProjectItem projectItem, ModelType modelType) {
    List<String> ids = new ArrayList<>(ProjectDocumentModels.getOtherModelsOfType(projectItem, modelType).stream()
        .map(A12Model::getId).toList());
    ids.sort(Comparator.naturalOrder());
    return ids;
  }

  private List<Label> createTableLabel(Consumer<BindingComponentTableLabel> setter) {
    BindingComponentTableLabel tableLabel = new BindingComponentTableLabel();
    setter.accept(tableLabel);
    return tableLabel.getLabel();
  }

  private BindingComponent component() {
    BindingComponent current = reader.get();
    if (current == null) {
      current = new BindingComponent();
      writer.accept(current);
    }
    return current;
  }

  private BindingComponentModelsSme modelsSme() {
    BindingComponent component = component();
    if (component.getModelsSME() == null) {
      component.setModelsSME(new BindingComponentModelsSme());
    }
    return component.getModelsSME();
  }

  private BindingComponentPropsExtensions propsExtensions() {
    BindingComponent component = component();
    if (component.getPropsExtensions() == null) {
      component.setPropsExtensions(new BindingComponentPropsExtensions());
    }
    return component.getPropsExtensions();
  }

  private DualPaneProps dualPaneProps() {
    BindingComponentPropsExtensions propsExtensions = propsExtensions();
    if (propsExtensions.getDualPaneProps() == null) {
      propsExtensions.setDualPaneProps(new DualPaneProps());
    }
    return propsExtensions.getDualPaneProps();
  }

  private TableListProps tableListProps() {
    BindingComponentPropsExtensions propsExtensions = propsExtensions();
    if (propsExtensions.getTableListProps() == null) {
      propsExtensions.setTableListProps(new TableListProps());
    }
    return propsExtensions.getTableListProps();
  }

  private void bindPlainComboBox(ComboBox<String> comboBox, Consumer<String> setter) {
    comboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
      if (updatingFromModel) {
        return;
      }
      setter.accept(newValue);
      commitChange();
    });
  }

  private void bindIntegerField(TextField textField, Consumer<Integer> setter) {
    textField.textProperty().addListener((observable, oldValue, newValue) -> {
      if (updatingFromModel) {
        return;
      }
      Integer value = parseInteger(newValue);
      setter.accept(value);
      commitChange();
    });
  }

  @Nullable
  private static Integer parseInteger(@Nullable String value) {
    if (value == null || value.isBlank()) {
      return null;
    }
    try {
      return Integer.valueOf(value.trim());
    }
    catch (NumberFormatException e) {
      return null;
    }
  }
}
