package de.a12.studio.ui.editors.overviewmodel;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.Label;
import de.a12.studio.models.Locale;
import de.a12.studio.models.ModelReference;
import de.a12.studio.models.ModelType;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.overviewmodel.FieldRef;
import de.a12.studio.models.overviewmodel.FilterConfiguration;
import de.a12.studio.models.overviewmodel.FilterSection;
import de.a12.studio.models.overviewmodel.OverviewConfiguration;
import de.a12.studio.models.overviewmodel.OverviewModel;
import de.a12.studio.models.querymodel.QueryModel;
import de.a12.studio.modelsvalidation.validators.ElementIndex;
import de.a12.studio.ui.editors.AbstractEditorController;
import de.a12.studio.ui.events.StudioEventManager;
import de.a12.studio.ui.util.Icons;
import de.a12.studio.ui.util.ProjectDocumentModels;
import de.a12.studio.ui.util.WidgetFactory;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.jspecify.annotations.NonNull;
import org.kordamp.ikonli.javafx.FontIcon;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.UUID;
import java.util.function.BiConsumer;

/**
 * Edits an {@link OverviewModel}'s "Overview" tab: General Settings (the Overview Reference, delegated to
 * {@link OverviewReferencePanelController}), Columns (delegated to
 * {@link OverviewColumnsPanelController}), Features
 * (search/filter/row-count, delegated to {@link OverviewFeaturesPanelController}), Filter,
 * Multi-Selection (delegated to {@link OverviewMultiSelectionPanelController}), Row Height And Action Column
 * Width (delegated to {@link RowHeightActionColumnWidthPanelController}), Paging Behaviour (delegated to
 * {@link PagingBehaviourPanelController}), Accessibility (delegated to
 * {@link OverviewAccessibilityPanelController}) and Styles (delegated to
 * {@link StylesPanelController}). "Custom Actions" ({@code
 * content.rowActionGroup}) is out of scope, mirroring the Java model's already-reduced feature set versus
 * SME. {@code subHeaderBox}/{@code footerBox} are left untouched: sample models ({@code Company_OM.json},
 * {@code Invoice_OM.json}) show them written empty even with search/filter/multi-selection enabled, so the
 * a12 runtime derives that UI from the {@code configuration} flags rather than from manually-placed box
 * elements.
 */
public class OverviewModelEditorController extends AbstractEditorController implements Initializable {

  private static final List<String> FILTER_MODE_OPTIONS = List.of("",
      FilterConfiguration.FILTER_MODE_ALL, FilterConfiguration.FILTER_MODE_ALL_WITH_META,
      FilterConfiguration.FILTER_MODE_ALL_COLUMNS, FilterConfiguration.FILTER_MODE_CUSTOM_LIST);

  // General Settings
  @FXML
  private OverviewReferencePanelController overviewReferenceController;

  // Features
  @FXML
  private OverviewFeaturesPanelController overviewFeaturesController;

  @FXML
  private CheckBox enableFilterField;
  @FXML
  private VBox filterDetailsBox;
  @FXML
  private CheckBox showFilterButtonField;
  @FXML
  private CheckBox showFilterBarField;
  @FXML
  private ComboBox<String> filterModeField;
  @FXML
  private VBox customFieldsBox;
  @FXML
  private GridPane customFieldsGrid;
  @FXML
  private ListView<FilterSection> filterSectionsList;
  @FXML
  private VBox filterSectionDetailBox;
  @FXML
  private TextField filterSectionIdField;
  @FXML
  private GridPane filterSectionLabelGrid;
  @FXML
  private GridPane filterSectionFieldsGrid;

  // Multi-Selection
  @FXML
  private OverviewMultiSelectionPanelController overviewMultiSelectionController;

  // Paging Behaviour
  @FXML
  private PagingBehaviourPanelController overviewPagingBehaviourController;

  // Row Height And Action Column Width
  @FXML
  private RowHeightActionColumnWidthPanelController overviewRowHeightActionColumnWidthController;

  // Accessibility
  @FXML
  private OverviewAccessibilityPanelController overviewAccessibilityController;

  // Styles
  @FXML
  private StylesPanelController overviewStylesController;

  // Columns
  @FXML
  private OverviewColumnsPanelController overviewColumnsController;

  // Sorting
  @FXML
  private OverviewSortingPanelController overviewSortingController;

  private OverviewModel model;
  private List<DocumentModel> otherDocumentModels = List.of();
  private ElementIndex documentModelIndex;

  @Override
  public void initialize(URL url, ResourceBundle resources) {
    initializeGeneralSettings();
    initializeFilter();
  }

  private void initializeGeneralSettings() {
    overviewReferenceController.setOnChange(() -> {
      refreshDocumentModelIndex();
      refreshElementRefPickersAfterDocumentModelChange();
      commitChange();
    });
    // The Sorting panel's column picker and its own dangling-reference validation, as well as the
    // Accessibility panel's screen-reader column picker, both derive from the Columns list, so keep them in
    // sync with every structural change made there.
    overviewColumnsController.setOnChange(() -> {
      overviewSortingController.refresh();
      overviewAccessibilityController.refresh();
    });
  }

  private void initializeFilter() {
    bindCheckBox(enableFilterField, value -> {
      ensureConfiguration().setEnableFilter(value);
      filterDetailsBox.setVisible(value);
      filterDetailsBox.setManaged(value);
    });
    bindCheckBox(showFilterButtonField, value -> ensureFilterConfiguration().setShowFilterButton(value));
    bindCheckBox(showFilterBarField, value -> ensureFilterConfiguration().setShowFilterBar(value));

    filterModeField.getItems().setAll(FILTER_MODE_OPTIONS);
    filterModeField.valueProperty().addListener((observable, oldValue, newValue) -> {
      if (updatingFromModel || model == null) {
        return;
      }
      ensureFilterConfiguration().setFilterMode(newValue == null || newValue.isBlank() ? null : newValue);
      refreshCustomFieldsVisibility(newValue);
      commitChange();
    });

    filterSectionsList.setCellFactory(list -> new javafx.scene.control.ListCell<>() {
      @Override
      protected void updateItem(FilterSection section, boolean empty) {
        super.updateItem(section, empty);
        setText(empty || section == null ? null : describeFilterSection(section));
      }
    });
    filterSectionsList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> showFilterSection(newValue));

    filterSectionIdField.textProperty().addListener((observable, oldValue, newValue) -> {
      if (updatingFromModel) {
        return;
      }
      FilterSection section = filterSectionsList.getSelectionModel().getSelectedItem();
      if (section == null) {
        return;
      }
      section.setId(newValue == null || newValue.isBlank() ? null : newValue);
      filterSectionsList.refresh();
      commitChange();
    });
  }

  @Override
  public void loadModel(@NonNull A12Model<?> model) {
    load((OverviewModel) model);
    updateSettingsErrorBadge();
  }

  private void load(@NonNull OverviewModel overviewModel) {
    this.model = overviewModel;

    updatingFromModel = true;
    try {
      otherDocumentModels = ProjectDocumentModels.getOtherDocumentModels(projectItem);
      List<QueryModel> otherQueryModels = ProjectDocumentModels.getOtherModelsOfType(projectItem, ModelType.QUERY).stream()
          .filter(QueryModel.class::isInstance)
          .map(QueryModel.class::cast)
          .toList();
      overviewReferenceController.load(model, otherDocumentModels, otherQueryModels);
      refreshDocumentModelIndex();
      overviewColumnsController.setModel(model);
      overviewSortingController.setModel(model);

      overviewFeaturesController.setModel(model);

      populateFilterFields();

      overviewMultiSelectionController.setModel(model);

      overviewPagingBehaviourController.setModel(model);

      overviewRowHeightActionColumnWidthController.setModel(model);

      overviewAccessibilityController.setModel(model);

      overviewStylesController.setModel(model);
    }
    finally {
      updatingFromModel = false;
    }
  }

  private String currentDocumentModelId() {
    if (model.getModelReferences() == null) {
      return null;
    }
    return model.getModelReferences().stream()
        .filter(reference -> ModelReference.PURPOSE_DOCUMENT_MODEL_FOR_OVERVIEW.equals(reference.getPurpose()))
        .map(ModelReference::getReference)
        .findFirst()
        .orElse(null);
  }

  private void refreshDocumentModelIndex() {
    String documentModelId = currentDocumentModelId();
    DocumentModel documentModel = otherDocumentModels.stream()
        .filter(candidate -> documentModelId != null && documentModelId.equals(candidate.getId()))
        .findFirst()
        .orElse(null);
    documentModelIndex = OverviewElementOptions.indexOf(documentModel, otherDocumentModels);
    overviewColumnsController.setDocumentModelIndex(documentModelIndex, documentModelId);
    overviewSortingController.setDocumentModelIndex(documentModelIndex);
    overviewAccessibilityController.setDocumentModelIndex(documentModelIndex);
  }

  /** Every "element reference" picker's options depend on the selected Document Model; re-point them all. */
  private void refreshElementRefPickersAfterDocumentModelChange() {
    FilterConfiguration filterConfig = currentFilterConfiguration();
    rebuildFieldRefRows(customFieldsGrid, filterConfig != null ? filterConfig.getFields() : List.of());
    FilterSection selectedSection = filterSectionsList.getSelectionModel().getSelectedItem();
    if (selectedSection != null) {
      rebuildFieldRefRows(filterSectionFieldsGrid, selectedSection.getFields());
    }
    filterSectionsList.refresh();
  }

  // ---- Filter ----

  private void populateFilterFields() {
    OverviewConfiguration configuration = model.getContent().getConfiguration();
    boolean enabled = configuration != null && Boolean.TRUE.equals(configuration.getEnableFilter());
    enableFilterField.setSelected(enabled);
    filterDetailsBox.setVisible(enabled);
    filterDetailsBox.setManaged(enabled);

    FilterConfiguration filterConfig = configuration != null ? configuration.getFilterConfiguration() : null;
    showFilterButtonField.setSelected(filterConfig != null && Boolean.TRUE.equals(filterConfig.getShowFilterButton()));
    showFilterBarField.setSelected(filterConfig != null && Boolean.TRUE.equals(filterConfig.getShowFilterBar()));
    String mode = filterConfig != null ? orEmpty(filterConfig.getFilterMode()) : "";
    filterModeField.setValue(mode);
    refreshCustomFieldsVisibility(mode);

    rebuildFieldRefRows(customFieldsGrid, filterConfig != null ? filterConfig.getFields() : List.of());
    refreshFilterSectionsList();
    showFilterSection(null);
  }

  private void refreshCustomFieldsVisibility(String mode) {
    boolean custom = FilterConfiguration.FILTER_MODE_CUSTOM_LIST.equals(mode);
    customFieldsBox.setVisible(custom);
    customFieldsBox.setManaged(custom);
  }

  private FilterConfiguration currentFilterConfiguration() {
    return model.getContent().getConfiguration() != null ? model.getContent().getConfiguration().getFilterConfiguration() : null;
  }

  private void refreshFilterSectionsList() {
    FilterSection selected = filterSectionsList.getSelectionModel().getSelectedItem();
    FilterConfiguration config = currentFilterConfiguration();
    List<FilterSection> sections = config != null ? config.getSectionData() : List.of();
    filterSectionsList.getItems().setAll(sections);
    if (selected != null && sections.contains(selected)) {
      filterSectionsList.getSelectionModel().select(selected);
    }
  }

  private void showFilterSection(FilterSection section) {
    boolean wasUpdating = updatingFromModel;
    updatingFromModel = true;
    try {
      boolean present = section != null;
      filterSectionDetailBox.setVisible(present);
      filterSectionDetailBox.setManaged(present);
      if (!present) {
        return;
      }
      filterSectionIdField.setText(section.getId() != null ? section.getId() : "");
      rebuildLocaleGrid(filterSectionLabelGrid, section.getLabel(), (code, text) -> setLabelText(section.getLabel(), code, text));
      rebuildFieldRefRows(filterSectionFieldsGrid, section.getFields());
    }
    finally {
      updatingFromModel = wasUpdating;
    }
  }

  @FXML
  public void onAddCustomField(ActionEvent e) {
    FilterConfiguration config = ensureFilterConfiguration();
    config.getFields().add(new FieldRef());
    rebuildFieldRefRows(customFieldsGrid, config.getFields());
    commitChange();
  }

  @FXML
  public void onAddFilterSection(ActionEvent e) {
    FilterConfiguration config = ensureFilterConfiguration();
    FilterSection section = new FilterSection();
    section.setId("section-" + shortId());
    config.getSectionData().add(section);
    refreshFilterSectionsList();
    filterSectionsList.getSelectionModel().select(section);
    commitChange();
  }

  @FXML
  public void onRemoveFilterSection(ActionEvent e) {
    FilterSection section = filterSectionsList.getSelectionModel().getSelectedItem();
    if (section == null) {
      return;
    }
    FilterConfiguration config = currentFilterConfiguration();
    if (config != null) {
      config.getSectionData().remove(section);
    }
    refreshFilterSectionsList();
    commitChange();
  }

  @FXML
  public void onAddFilterSectionField(ActionEvent e) {
    FilterSection section = filterSectionsList.getSelectionModel().getSelectedItem();
    if (section == null) {
      return;
    }
    section.getFields().add(new FieldRef());
    rebuildFieldRefRows(filterSectionFieldsGrid, section.getFields());
    commitChange();
  }

  // ---- Shared helpers ----

  private OverviewConfiguration ensureConfiguration() {
    if (model.getContent().getConfiguration() == null) {
      model.getContent().setConfiguration(new OverviewConfiguration());
    }
    return model.getContent().getConfiguration();
  }

  private FilterConfiguration ensureFilterConfiguration() {
    OverviewConfiguration configuration = ensureConfiguration();
    if (configuration.getFilterConfiguration() == null) {
      configuration.setFilterConfiguration(new FilterConfiguration());
    }
    return configuration.getFilterConfiguration();
  }

  /** Rebuilds a field-reference picker row per {@link FieldRef}, with an "Add"-driven, delete-per-row grid. */
  private void rebuildFieldRefRows(GridPane grid, List<FieldRef> fieldRefs) {
    grid.getChildren().clear();
    int row = 0;
    for (FieldRef ref : List.copyOf(fieldRefs)) {
      ComboBox<String> field = new ComboBox<>();
      field.setMaxWidth(Double.MAX_VALUE);
      GridPane.setHgrow(field, Priority.ALWAYS);
      field.getItems().setAll(OverviewElementOptions.elementIds(documentModelIndex));
      OverviewElementOptions.applyElementRefConverter(field, documentModelIndex);

      boolean wasUpdating = updatingFromModel;
      updatingFromModel = true;
      try {
        field.setValue(ref.getFieldId());
      }
      finally {
        updatingFromModel = wasUpdating;
      }

      field.valueProperty().addListener((observable, oldValue, newValue) -> {
        if (updatingFromModel) {
          return;
        }
        ref.setFieldId(newValue);
        commitChange();
      });

      Button deleteButton = createDeleteButton("Remove Field", () -> {
        fieldRefs.remove(ref);
        rebuildFieldRefRows(grid, fieldRefs);
        commitChange();
      });

      grid.addRow(row++, field, deleteButton);
    }
  }

  /** One text field per model locale, in {@code grid}, calling {@code onTextChange} with (locale, text) on edit. */
  private void rebuildLocaleGrid(GridPane grid, List<Label> labels, BiConsumer<String, String> onTextChange) {
    grid.getChildren().clear();
    int row = 0;
    for (Locale locale : model.getLocales()) {
      String code = locale.getCode();
      javafx.scene.control.Label localeLabel = new javafx.scene.control.Label(code);
      localeLabel.getStyleClass().add("field-label");

      TextField textField = new TextField(labelText(labels, code));
      textField.setMaxWidth(Double.MAX_VALUE);
      GridPane.setHgrow(textField, Priority.ALWAYS);
      textField.textProperty().addListener((observable, oldValue, newValue) -> {
        if (updatingFromModel) {
          return;
        }
        onTextChange.accept(code, newValue);
        commitChange();
      });

      grid.addRow(row++, localeLabel, textField);
    }
  }

  private static String labelText(List<Label> labels, String locale) {
    return labels.stream()
        .filter(label -> locale.equals(label.getLocale()))
        .map(Label::getText)
        .filter(text -> text != null)
        .findFirst()
        .orElse("");
  }

  private static void setLabelText(List<Label> labels, String locale, String text) {
    Label existing = labels.stream()
        .filter(label -> locale.equals(label.getLocale()))
        .findFirst()
        .orElse(null);
    if (existing == null) {
      existing = new Label();
      existing.setLocale(locale);
      labels.add(existing);
    }
    existing.setText(text == null || text.isBlank() ? null : text);
  }

  private static String firstNonBlankText(List<Label> labels) {
    return labels.stream()
        .map(Label::getText)
        .filter(text -> text != null && !text.isBlank())
        .findFirst()
        .orElse(null);
  }

  private String describeFilterSection(FilterSection section) {
    String label = firstNonBlankText(section.getLabel());
    return label != null ? label : (section.getId() != null ? section.getId() : "(new section)");
  }

  private static String orEmpty(String value) {
    return value != null ? value : "";
  }

  private static String shortId() {
    return UUID.randomUUID().toString().replace("-", "").substring(0, 5);
  }

  private void commitChange() {
    projectItem.save();
    StudioEventManager.getInstance().fireModelSavedEvent(projectItem);
  }

  private static Button createDeleteButton(String tooltip, Runnable action) {
    FontIcon icon = new FontIcon(Icons.TRASH);
    icon.setIconSize(16);
    icon.getStyleClass().add("toolbar-icon");

    Button button = new Button();
    button.getStyleClass().add("default-button");
    button.setGraphic(icon);
    button.setTooltip(WidgetFactory.createTooltip(tooltip));
    button.setOnAction(event -> action.run());
    return button;
  }

  @Override
  public @NonNull ModelType getModelType() {
    return ModelType.OVERVIEW;
  }
}
