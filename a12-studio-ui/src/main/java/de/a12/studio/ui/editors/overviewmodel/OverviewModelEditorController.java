package de.a12.studio.ui.editors.overviewmodel;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.Label;
import de.a12.studio.models.Locale;
import de.a12.studio.models.ModelReference;
import de.a12.studio.models.ModelType;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.overviewmodel.Alignment;
import de.a12.studio.models.overviewmodel.Column;
import de.a12.studio.models.overviewmodel.ColumnAlignment;
import de.a12.studio.models.overviewmodel.ClearConfirmation;
import de.a12.studio.models.overviewmodel.Confirmation;
import de.a12.studio.models.overviewmodel.FieldRef;
import de.a12.studio.models.overviewmodel.FilterConfiguration;
import de.a12.studio.models.overviewmodel.FilterSection;
import de.a12.studio.models.overviewmodel.Icon;
import de.a12.studio.models.overviewmodel.MultiSelectionConfig;
import de.a12.studio.models.overviewmodel.OverviewConfiguration;
import de.a12.studio.models.overviewmodel.OverviewModel;
import de.a12.studio.models.overviewmodel.SummaryConfig;
import de.a12.studio.models.querymodel.QueryModel;
import de.a12.studio.modelsvalidation.validators.ElementIndex;
import de.a12.studio.ui.editors.AbstractEditorController;
import de.a12.studio.ui.editors.propertyeditors.OverviewReferencePanelController;
import de.a12.studio.ui.events.StudioEventManager;
import de.a12.studio.ui.util.Icons;
import de.a12.studio.ui.util.ProjectDocumentModels;
import de.a12.studio.ui.util.WidgetFactory;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.jspecify.annotations.NonNull;
import org.kordamp.ikonli.javafx.FontIcon;

import java.net.URL;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.UUID;
import java.util.function.BiConsumer;

/**
 * Edits an {@link OverviewModel}'s "Overview" tab: General Settings (the Overview Reference, delegated to
 * {@link de.a12.studio.ui.editors.propertyeditors.OverviewReferencePanelController}), Features
 * (search/filter/paging/row-count/multi-selection), and Columns. "Custom Actions" ({@code
 * content.rowActionGroup}) is out of scope, mirroring the Java model's already-reduced feature set versus
 * SME (no content-level Styles). {@code subHeaderBox}/{@code footerBox} are left untouched: sample models
 * ({@code Company_OM.json}, {@code Invoice_OM.json}) show them written empty even with
 * search/filter/multi-selection enabled, so the a12 runtime derives that UI from the {@code configuration}
 * flags rather than from manually-placed box elements.
 */
public class OverviewModelEditorController extends AbstractEditorController implements Initializable {

  private static final String COLUMN_TYPE_REFERENCE = "Reference";
  private static final String COLUMN_TYPE_EXPRESSION = "Expression";

  private static final List<String> FILTER_MODE_OPTIONS = List.of("",
      FilterConfiguration.FILTER_MODE_ALL, FilterConfiguration.FILTER_MODE_ALL_WITH_META,
      FilterConfiguration.FILTER_MODE_ALL_COLUMNS, FilterConfiguration.FILTER_MODE_CUSTOM_LIST);
  private static final List<String> COLLAPSE_OPTIONS = List.of("",
      MultiSelectionConfig.COLLAPSE_OPTION_COLLAPSIBLE_COLLAPSED, MultiSelectionConfig.COLLAPSE_OPTION_COLLAPSIBLE_EXPANDED,
      MultiSelectionConfig.COLLAPSE_OPTION_NON_COLLAPSIBLE);
  private static final List<String> COUNTER_OPTIONS = List.of("",
      MultiSelectionConfig.COUNTER_OPTION_SIMPLE, MultiSelectionConfig.COUNTER_OPTION_NONE);
  private static final List<String> SELECTION_AREA_OPTIONS = List.of("",
      MultiSelectionConfig.SELECTION_AREA_CHECKBOX, MultiSelectionConfig.SELECTION_AREA_CHECKBOX_AND_ROW);
  private static final List<String> ICON_THEME_OPTIONS = List.of("",
      Icon.THEME_FILLED, Icon.THEME_OUTLINED, Icon.THEME_ROUNDED, Icon.THEME_CUSTOM);
  private static final List<String> PIN_DIRECTION_OPTIONS = List.of("", Column.PIN_DIRECTION_LEFT, Column.PIN_DIRECTION_RIGHT);
  private static final List<String> PREFERRED_SORTING_OPTIONS = List.of("", Column.PREFERRED_SORTING_ASC, Column.PREFERRED_SORTING_DESC);
  private static final List<String> ATTACHMENT_DISPLAY_MODE_OPTIONS = List.of("",
      Column.ATTACHMENT_DISPLAY_MODE_PREVIEW, Column.ATTACHMENT_DISPLAY_MODE_ICON,
      Column.ATTACHMENT_DISPLAY_MODE_FILE_NAME, Column.ATTACHMENT_DISPLAY_MODE_ICON_WITH_FILE_NAME);
  private static final List<String> ALIGNMENT_OPTIONS = List.of("", "left", "center", "right");

  // General Settings
  @FXML
  private OverviewReferencePanelController overviewReferenceController;

  // Features
  @FXML
  private CheckBox showFullTextSearchField;
  @FXML
  private CheckBox showRowCountField;
  @FXML
  private Spinner<Integer> pagingSizeField;

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

  @FXML
  private CheckBox enableMultiSelectionField;
  @FXML
  private VBox multiSelectionDetailsBox;
  @FXML
  private ComboBox<String> collapseOptionField;
  @FXML
  private ComboBox<String> counterOptionField;
  @FXML
  private ComboBox<String> selectionAreaField;
  @FXML
  private CheckBox clearConfirmationField;
  @FXML
  private ListView<de.a12.studio.models.overviewmodel.Button> multiSelectionButtonsList;
  @FXML
  private VBox multiSelectionButtonDetailBox;
  @FXML
  private TextField buttonEventField;
  @FXML
  private CheckBox buttonDestructiveField;
  @FXML
  private CheckBox buttonPrimaryField;
  @FXML
  private TextField buttonIconNameField;
  @FXML
  private ComboBox<String> buttonIconThemeField;
  @FXML
  private GridPane buttonLabelGrid;
  @FXML
  private GridPane buttonDescriptionGrid;
  @FXML
  private CheckBox buttonConfirmationField;
  @FXML
  private VBox buttonConfirmationDetailsBox;
  @FXML
  private GridPane buttonConfirmationTitleGrid;
  @FXML
  private GridPane buttonConfirmationMessageGrid;

  // Columns
  @FXML
  private TableView<Column> columnsTable;
  @FXML
  private TableColumn<Column, String> columnLabelColumn;
  @FXML
  private TableColumn<Column, String> columnTypeColumn;
  @FXML
  private TableColumn<Column, String> columnWidthColumn;
  @FXML
  private TableColumn<Column, String> columnSortableColumn;
  @FXML
  private TableColumn<Column, String> columnPinDirectionColumn;
  @FXML
  private VBox columnDetailBox;
  @FXML
  private ComboBox<String> columnTypeField;
  @FXML
  private VBox columnReferenceBox;
  @FXML
  private ComboBox<String> columnElementRefField;
  @FXML
  private CheckBox columnSortableField;
  @FXML
  private ComboBox<String> columnPreferredSortingField;
  @FXML
  private ComboBox<String> columnAttachmentDisplayModeField;
  @FXML
  private VBox columnExpressionBox;
  @FXML
  private TextField columnNameField;
  @FXML
  private TextArea columnExpressionField;
  @FXML
  private GridPane columnLabelGrid;
  @FXML
  private Spinner<Double> columnWidthField;
  @FXML
  private ComboBox<String> columnPinDirectionField;
  @FXML
  private ComboBox<String> columnAlignmentHeaderField;
  @FXML
  private ComboBox<String> columnAlignmentContentField;
  @FXML
  private CheckBox columnFixedWidthField;
  @FXML
  private GridPane columnSuffixGrid;
  @FXML
  private CheckBox columnShowSummaryField;

  private OverviewModel model;
  private List<DocumentModel> otherDocumentModels = List.of();
  private ElementIndex documentModelIndex;
  // Preserves multi-selection settings across an uncheck/recheck of "Enable Multi-Selection" within the
  // same session, since disabling it nulls configuration.multiSelection (matching SME's on-disk shape).
  private MultiSelectionConfig cachedMultiSelectionConfig;

  @Override
  public void initialize(URL url, ResourceBundle resources) {
    initializeGeneralSettings();
    initializeFeatures();
    initializeFilter();
    initializeMultiSelection();
    initializeColumns();
  }

  private void initializeGeneralSettings() {
    overviewReferenceController.setOnChange(() -> {
      refreshDocumentModelIndex();
      refreshElementRefPickersAfterDocumentModelChange();
      commitChange();
    });
  }

  private void initializeFeatures() {
    bindCheckBox(showFullTextSearchField, value -> ensureConfiguration().setShowFullTextSearch(value));
    bindCheckBox(showRowCountField, value -> ensureConfiguration().setShowRowCount(value ? Boolean.TRUE : null));

    pagingSizeField.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, Integer.MAX_VALUE, 10));
    WidgetFactory.restrictToNumericInput(pagingSizeField.getEditor());
    pagingSizeField.valueProperty().addListener((observable, oldValue, newValue) -> {
      if (updatingFromModel || model == null) {
        return;
      }
      ensureConfiguration().setPagingSize(newValue);
      commitChange();
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

  private void initializeMultiSelection() {
    bindCheckBox(enableMultiSelectionField, value -> {
      OverviewConfiguration configuration = ensureConfiguration();
      if (value) {
        configuration.setMultiSelection(configuration.getMultiSelection() != null ? configuration.getMultiSelection()
            : (cachedMultiSelectionConfig != null ? cachedMultiSelectionConfig : new MultiSelectionConfig()));
      }
      else {
        cachedMultiSelectionConfig = configuration.getMultiSelection();
        configuration.setMultiSelection(null);
      }
      multiSelectionDetailsBox.setVisible(value);
      multiSelectionDetailsBox.setManaged(value);
      boolean wasUpdating = updatingFromModel;
      updatingFromModel = true;
      try {
        populateMultiSelectionFields();
      }
      finally {
        updatingFromModel = wasUpdating;
      }
    });

    collapseOptionField.getItems().setAll(COLLAPSE_OPTIONS);
    collapseOptionField.valueProperty().addListener((observable, oldValue, newValue) -> {
      if (updatingFromModel || model == null) {
        return;
      }
      ensureMultiSelectionConfig().setCollapseOption(newValue == null || newValue.isBlank() ? null : newValue);
      commitChange();
    });

    counterOptionField.getItems().setAll(COUNTER_OPTIONS);
    counterOptionField.valueProperty().addListener((observable, oldValue, newValue) -> {
      if (updatingFromModel || model == null) {
        return;
      }
      ensureMultiSelectionConfig().setCounterOption(newValue == null || newValue.isBlank() ? null : newValue);
      commitChange();
    });

    selectionAreaField.getItems().setAll(SELECTION_AREA_OPTIONS);
    selectionAreaField.valueProperty().addListener((observable, oldValue, newValue) -> {
      if (updatingFromModel || model == null) {
        return;
      }
      ensureMultiSelectionConfig().setSelectionArea(newValue == null || newValue.isBlank() ? null : newValue);
      commitChange();
    });

    bindCheckBox(clearConfirmationField, value -> {
      MultiSelectionConfig config = ensureMultiSelectionConfig();
      if (value) {
        ClearConfirmation confirmation = config.getClearConfirmation();
        if (confirmation == null) {
          confirmation = new ClearConfirmation();
          config.setClearConfirmation(confirmation);
        }
        confirmation.setEnabled(true);
      }
      else {
        config.setClearConfirmation(null);
      }
    });

    multiSelectionButtonsList.setCellFactory(list -> new javafx.scene.control.ListCell<>() {
      @Override
      protected void updateItem(de.a12.studio.models.overviewmodel.Button button, boolean empty) {
        super.updateItem(button, empty);
        setText(empty || button == null ? null : describeMultiSelectionButton(button));
      }
    });
    multiSelectionButtonsList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> showMultiSelectionButton(newValue));

    buttonEventField.textProperty().addListener((observable, oldValue, newValue) -> {
      if (updatingFromModel) {
        return;
      }
      de.a12.studio.models.overviewmodel.Button button = selectedMultiSelectionButton();
      if (button == null) {
        return;
      }
      button.setEvent(newValue == null || newValue.isBlank() ? null : newValue);
      multiSelectionButtonsList.refresh();
      commitChange();
    });

    bindCheckBox(buttonDestructiveField, value -> {
      de.a12.studio.models.overviewmodel.Button button = selectedMultiSelectionButton();
      if (button != null) {
        button.setDestructive(value);
      }
    });
    bindCheckBox(buttonPrimaryField, value -> {
      de.a12.studio.models.overviewmodel.Button button = selectedMultiSelectionButton();
      if (button != null) {
        button.setPrimary(value);
      }
    });

    buttonIconNameField.textProperty().addListener((observable, oldValue, newValue) -> {
      if (updatingFromModel) {
        return;
      }
      de.a12.studio.models.overviewmodel.Button button = selectedMultiSelectionButton();
      if (button == null) {
        return;
      }
      setIconName(button, newValue);
      commitChange();
    });

    buttonIconThemeField.getItems().setAll(ICON_THEME_OPTIONS);
    buttonIconThemeField.valueProperty().addListener((observable, oldValue, newValue) -> {
      if (updatingFromModel) {
        return;
      }
      de.a12.studio.models.overviewmodel.Button button = selectedMultiSelectionButton();
      if (button == null || button.getIcon() == null) {
        return;
      }
      button.getIcon().setTheme(newValue == null || newValue.isBlank() ? null : newValue);
      commitChange();
    });

    bindCheckBox(buttonConfirmationField, value -> {
      de.a12.studio.models.overviewmodel.Button button = selectedMultiSelectionButton();
      if (button == null) {
        return;
      }
      if (value) {
        ensureConfirmation(button);
      }
      else {
        button.setConfirmation(null);
      }
      buttonConfirmationDetailsBox.setVisible(value);
      buttonConfirmationDetailsBox.setManaged(value);
      boolean wasUpdating = updatingFromModel;
      updatingFromModel = true;
      try {
        rebuildLocaleGrid(buttonConfirmationTitleGrid, value ? ensureConfirmation(button).getTitle() : List.of(),
            (code, text) -> setLabelText(ensureConfirmation(button).getTitle(), code, text));
        rebuildLocaleGrid(buttonConfirmationMessageGrid, value ? ensureConfirmation(button).getMessage() : List.of(),
            (code, text) -> setLabelText(ensureConfirmation(button).getMessage(), code, text));
      }
      finally {
        updatingFromModel = wasUpdating;
      }
      multiSelectionButtonsList.refresh();
    });
  }

  private void initializeColumns() {
    columnsTable.setEditable(true);
    columnLabelColumn.setCellValueFactory(data -> new SimpleStringProperty(previewColumnLabel(data.getValue())));
    columnTypeColumn.setCellValueFactory(data -> new SimpleStringProperty(columnTypeOf(data.getValue())));
    columnWidthColumn.setCellValueFactory(data -> new SimpleStringProperty(
        data.getValue().getWidth() != null ? String.valueOf(data.getValue().getWidth()) : ""));
    columnSortableColumn.setCellValueFactory(data -> new SimpleStringProperty(
        Boolean.TRUE.equals(data.getValue().getSortable()) ? "Yes" : "No"));
    columnPinDirectionColumn.setCellValueFactory(data -> new SimpleStringProperty(
        data.getValue().getPinDirection() != null ? data.getValue().getPinDirection() : ""));
    columnsTable.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> showColumn(newValue));

    columnTypeField.getItems().setAll(COLUMN_TYPE_REFERENCE, COLUMN_TYPE_EXPRESSION);
    columnTypeField.valueProperty().addListener((observable, oldValue, newValue) -> {
      if (updatingFromModel) {
        return;
      }
      Column column = selectedColumn();
      if (column == null || newValue == null) {
        return;
      }
      refreshColumnTypeVisibility(newValue);
      boolean wasUpdating = updatingFromModel;
      updatingFromModel = true;
      try {
        if (COLUMN_TYPE_EXPRESSION.equals(newValue)) {
          column.setElementRef(null);
          column.setSortable(null);
          column.setPreferredSorting(null);
          column.setAttachmentDisplayMode(null);
          if (column.getExpression() == null) {
            column.setExpression("");
          }
          columnElementRefField.setValue(null);
          columnSortableField.setSelected(false);
          columnPreferredSortingField.setValue("");
          columnAttachmentDisplayModeField.setValue("");
          columnExpressionField.setText(column.getExpression());
        }
        else {
          column.setExpression(null);
          column.setName(null);
          columnNameField.setText("");
          columnExpressionField.setText("");
        }
      }
      finally {
        updatingFromModel = wasUpdating;
      }
      columnsTable.refresh();
      commitChange();
    });

    columnElementRefField.valueProperty().addListener((observable, oldValue, newValue) -> {
      if (updatingFromModel) {
        return;
      }
      Column column = selectedColumn();
      if (column == null) {
        return;
      }
      column.setElementRef(newValue == null || newValue.isBlank() ? null : newValue);
      columnsTable.refresh();
      commitChange();
    });

    bindCheckBox(columnSortableField, value -> {
      Column column = selectedColumn();
      if (column == null) {
        return;
      }
      column.setSortable(value);
      columnPreferredSortingField.setDisable(!value);
      if (!value) {
        column.setPreferredSorting(null);
        boolean wasUpdating = updatingFromModel;
        updatingFromModel = true;
        try {
          columnPreferredSortingField.setValue("");
        }
        finally {
          updatingFromModel = wasUpdating;
        }
      }
      columnsTable.refresh();
    });

    columnPreferredSortingField.getItems().setAll(PREFERRED_SORTING_OPTIONS);
    columnPreferredSortingField.valueProperty().addListener((observable, oldValue, newValue) -> {
      if (updatingFromModel) {
        return;
      }
      Column column = selectedColumn();
      if (column == null) {
        return;
      }
      column.setPreferredSorting(newValue == null || newValue.isBlank() ? null : newValue);
      commitChange();
    });

    columnAttachmentDisplayModeField.getItems().setAll(ATTACHMENT_DISPLAY_MODE_OPTIONS);
    columnAttachmentDisplayModeField.valueProperty().addListener((observable, oldValue, newValue) -> {
      if (updatingFromModel) {
        return;
      }
      Column column = selectedColumn();
      if (column == null) {
        return;
      }
      column.setAttachmentDisplayMode(newValue == null || newValue.isBlank() ? null : newValue);
      commitChange();
    });

    columnNameField.textProperty().addListener((observable, oldValue, newValue) -> {
      if (updatingFromModel) {
        return;
      }
      Column column = selectedColumn();
      if (column == null) {
        return;
      }
      column.setName(newValue == null || newValue.isBlank() ? null : newValue);
      columnsTable.refresh();
      commitChange();
    });

    columnExpressionField.textProperty().addListener((observable, oldValue, newValue) -> {
      if (updatingFromModel) {
        return;
      }
      Column column = selectedColumn();
      if (column == null) {
        return;
      }
      column.setExpression(newValue);
      commitChange();
    });

    columnWidthField.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(0.3, 100.0, 1.0, 0.1));
    columnWidthField.valueProperty().addListener((observable, oldValue, newValue) -> {
      if (updatingFromModel) {
        return;
      }
      Column column = selectedColumn();
      if (column == null) {
        return;
      }
      column.setWidth(newValue);
      commitChange();
    });

    columnPinDirectionField.getItems().setAll(PIN_DIRECTION_OPTIONS);
    columnPinDirectionField.valueProperty().addListener((observable, oldValue, newValue) -> {
      if (updatingFromModel) {
        return;
      }
      Column column = selectedColumn();
      if (column == null) {
        return;
      }
      column.setPinDirection(newValue == null || newValue.isBlank() ? null : newValue);
      columnsTable.refresh();
      commitChange();
    });

    columnAlignmentHeaderField.getItems().setAll(ALIGNMENT_OPTIONS);
    columnAlignmentHeaderField.valueProperty().addListener((observable, oldValue, newValue) -> {
      if (updatingFromModel) {
        return;
      }
      Column column = selectedColumn();
      if (column == null) {
        return;
      }
      if (newValue == null || newValue.isBlank()) {
        if (column.getAlignment() != null) {
          column.getAlignment().setHeader(null);
        }
      }
      else {
        ensureHeaderAlignment(column).setHorizontal(newValue);
      }
      commitChange();
    });

    columnAlignmentContentField.getItems().setAll(ALIGNMENT_OPTIONS);
    columnAlignmentContentField.valueProperty().addListener((observable, oldValue, newValue) -> {
      if (updatingFromModel) {
        return;
      }
      Column column = selectedColumn();
      if (column == null) {
        return;
      }
      if (newValue == null || newValue.isBlank()) {
        if (column.getAlignment() != null) {
          column.getAlignment().setContent(null);
        }
      }
      else {
        ensureContentAlignment(column).setHorizontal(newValue);
      }
      commitChange();
    });

    bindCheckBox(columnFixedWidthField, value -> {
      Column column = selectedColumn();
      if (column != null) {
        column.setFixedWidth(value ? Boolean.TRUE : null);
      }
    });

    bindCheckBox(columnShowSummaryField, value -> {
      Column column = selectedColumn();
      if (column == null) {
        return;
      }
      if (value) {
        if (column.getSummary().isEmpty()) {
          SummaryConfig summary = new SummaryConfig();
          summary.setOperation(SummaryConfig.OPERATION_SUM);
          column.getSummary().add(summary);
        }
      }
      else {
        column.getSummary().clear();
      }
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

      OverviewConfiguration configuration = model.getContent().getConfiguration();
      showFullTextSearchField.setSelected(configuration != null && Boolean.TRUE.equals(configuration.getShowFullTextSearch()));
      showRowCountField.setSelected(configuration != null && Boolean.TRUE.equals(configuration.getShowRowCount()));
      pagingSizeField.getValueFactory().setValue(
          configuration != null && configuration.getPagingSize() != null ? configuration.getPagingSize() : 10);

      populateFilterFields();

      boolean multiSelectionEnabled = configuration != null && configuration.getMultiSelection() != null;
      enableMultiSelectionField.setSelected(multiSelectionEnabled);
      multiSelectionDetailsBox.setVisible(multiSelectionEnabled);
      multiSelectionDetailsBox.setManaged(multiSelectionEnabled);
      populateMultiSelectionFields();

      refreshColumnsTable();
      showColumn(null);
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
    documentModelIndex = OverviewElementOptions.indexOf(documentModel);
  }

  /** Every "element reference" picker's options depend on the selected Document Model; re-point them all. */
  private void refreshElementRefPickersAfterDocumentModelChange() {
    FilterConfiguration filterConfig = currentFilterConfiguration();
    rebuildFieldRefRows(customFieldsGrid, filterConfig != null ? filterConfig.getFields() : List.of());
    FilterSection selectedSection = filterSectionsList.getSelectionModel().getSelectedItem();
    if (selectedSection != null) {
      rebuildFieldRefRows(filterSectionFieldsGrid, selectedSection.getFields());
    }
    if (selectedColumn() != null) {
      columnElementRefField.getItems().setAll(OverviewElementOptions.elementIds(documentModelIndex));
      OverviewElementOptions.applyElementRefConverter(columnElementRefField, documentModelIndex);
    }
    columnsTable.refresh();
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

  // ---- Multi-Selection ----

  private void populateMultiSelectionFields() {
    MultiSelectionConfig config = currentMultiSelectionConfig();
    collapseOptionField.setValue(config != null ? orEmpty(config.getCollapseOption()) : "");
    counterOptionField.setValue(config != null ? orEmpty(config.getCounterOption()) : "");
    selectionAreaField.setValue(config != null ? orEmpty(config.getSelectionArea()) : "");
    clearConfirmationField.setSelected(config != null && config.getClearConfirmation() != null
        && Boolean.TRUE.equals(config.getClearConfirmation().getEnabled()));
    refreshMultiSelectionButtonsList();
    showMultiSelectionButton(null);
  }

  private MultiSelectionConfig currentMultiSelectionConfig() {
    return model.getContent().getConfiguration() != null ? model.getContent().getConfiguration().getMultiSelection() : null;
  }

  private void refreshMultiSelectionButtonsList() {
    de.a12.studio.models.overviewmodel.Button selected = multiSelectionButtonsList.getSelectionModel().getSelectedItem();
    MultiSelectionConfig config = currentMultiSelectionConfig();
    List<de.a12.studio.models.overviewmodel.Button> buttons = config != null ? config.getButtons() : List.of();
    multiSelectionButtonsList.getItems().setAll(buttons);
    if (selected != null && buttons.contains(selected)) {
      multiSelectionButtonsList.getSelectionModel().select(selected);
    }
  }

  private de.a12.studio.models.overviewmodel.Button selectedMultiSelectionButton() {
    return multiSelectionButtonsList.getSelectionModel().getSelectedItem();
  }

  private void showMultiSelectionButton(de.a12.studio.models.overviewmodel.Button button) {
    boolean wasUpdating = updatingFromModel;
    updatingFromModel = true;
    try {
      boolean present = button != null;
      multiSelectionButtonDetailBox.setVisible(present);
      multiSelectionButtonDetailBox.setManaged(present);
      if (!present) {
        return;
      }
      buttonEventField.setText(button.getEvent() != null ? button.getEvent() : "");
      buttonDestructiveField.setSelected(Boolean.TRUE.equals(button.getDestructive()));
      buttonPrimaryField.setSelected(Boolean.TRUE.equals(button.getPrimary()));
      buttonIconNameField.setText(button.getIcon() != null && button.getIcon().getName() != null ? button.getIcon().getName() : "");
      buttonIconThemeField.setValue(button.getIcon() != null ? orEmpty(button.getIcon().getTheme()) : "");
      rebuildLocaleGrid(buttonLabelGrid, button.getLabel(), (code, text) -> setLabelText(button.getLabel(), code, text));
      rebuildLocaleGrid(buttonDescriptionGrid, button.getDescription(), (code, text) -> setLabelText(button.getDescription(), code, text));

      boolean confirmationEnabled = button.getConfirmation() != null;
      buttonConfirmationField.setSelected(confirmationEnabled);
      buttonConfirmationDetailsBox.setVisible(confirmationEnabled);
      buttonConfirmationDetailsBox.setManaged(confirmationEnabled);
      Confirmation confirmation = button.getConfirmation();
      rebuildLocaleGrid(buttonConfirmationTitleGrid, confirmation != null ? confirmation.getTitle() : List.of(),
          (code, text) -> setLabelText(ensureConfirmation(button).getTitle(), code, text));
      rebuildLocaleGrid(buttonConfirmationMessageGrid, confirmation != null ? confirmation.getMessage() : List.of(),
          (code, text) -> setLabelText(ensureConfirmation(button).getMessage(), code, text));
    }
    finally {
      updatingFromModel = wasUpdating;
    }
  }

  @FXML
  public void onAddMultiSelectionButton(ActionEvent e) {
    MultiSelectionConfig config = ensureMultiSelectionConfig();
    de.a12.studio.models.overviewmodel.Button button = new de.a12.studio.models.overviewmodel.Button();
    button.setEvent("");
    config.getButtons().add(button);
    refreshMultiSelectionButtonsList();
    multiSelectionButtonsList.getSelectionModel().select(button);
    commitChange();
  }

  @FXML
  public void onRemoveMultiSelectionButton(ActionEvent e) {
    de.a12.studio.models.overviewmodel.Button button = selectedMultiSelectionButton();
    if (button == null) {
      return;
    }
    MultiSelectionConfig config = currentMultiSelectionConfig();
    if (config != null) {
      config.getButtons().remove(button);
    }
    refreshMultiSelectionButtonsList();
    commitChange();
  }

  private static void setIconName(de.a12.studio.models.overviewmodel.Button button, String value) {
    if (value == null || value.isBlank()) {
      if (button.getIcon() != null) {
        button.getIcon().setName(null);
      }
      return;
    }
    Icon icon = button.getIcon();
    if (icon == null) {
      icon = new Icon();
      button.setIcon(icon);
    }
    icon.setName(value);
  }

  private static Confirmation ensureConfirmation(de.a12.studio.models.overviewmodel.Button button) {
    if (button.getConfirmation() == null) {
      button.setConfirmation(new Confirmation());
    }
    return button.getConfirmation();
  }

  // ---- Columns ----

  private void refreshColumnsTable() {
    Column selected = columnsTable.getSelectionModel().getSelectedItem();
    columnsTable.getItems().setAll(model.getContent().getColumns());
    if (selected != null && model.getContent().getColumns().contains(selected)) {
      columnsTable.getSelectionModel().select(selected);
    }
  }

  private Column selectedColumn() {
    return columnsTable.getSelectionModel().getSelectedItem();
  }

  private void showColumn(Column column) {
    boolean wasUpdating = updatingFromModel;
    updatingFromModel = true;
    try {
      boolean present = column != null;
      columnDetailBox.setVisible(present);
      columnDetailBox.setManaged(present);
      if (!present) {
        return;
      }

      String type = columnTypeOf(column);
      columnTypeField.setValue(type);
      refreshColumnTypeVisibility(type);

      columnElementRefField.getItems().setAll(OverviewElementOptions.elementIds(documentModelIndex));
      OverviewElementOptions.applyElementRefConverter(columnElementRefField, documentModelIndex);
      columnElementRefField.setValue(column.getElementRef());
      columnSortableField.setSelected(Boolean.TRUE.equals(column.getSortable()));
      columnPreferredSortingField.setValue(orEmpty(column.getPreferredSorting()));
      columnPreferredSortingField.setDisable(!Boolean.TRUE.equals(column.getSortable()));
      columnAttachmentDisplayModeField.setValue(orEmpty(column.getAttachmentDisplayMode()));

      columnNameField.setText(column.getName() != null ? column.getName() : "");
      columnExpressionField.setText(column.getExpression() != null ? column.getExpression() : "");

      rebuildLocaleGrid(columnLabelGrid, column.getLabel(), (code, text) -> setLabelText(column.getLabel(), code, text));
      rebuildLocaleGrid(columnSuffixGrid, column.getSuffix(), (code, text) -> setLabelText(column.getSuffix(), code, text));

      columnWidthField.getValueFactory().setValue(column.getWidth() != null ? column.getWidth() : 1.0);
      columnPinDirectionField.setValue(orEmpty(column.getPinDirection()));
      columnFixedWidthField.setSelected(Boolean.TRUE.equals(column.getFixedWidth()));
      columnAlignmentHeaderField.setValue(column.getAlignment() != null && column.getAlignment().getHeader() != null
          ? orEmpty(column.getAlignment().getHeader().getHorizontal()) : "");
      columnAlignmentContentField.setValue(column.getAlignment() != null && column.getAlignment().getContent() != null
          ? orEmpty(column.getAlignment().getContent().getHorizontal()) : "");
      columnShowSummaryField.setSelected(!column.getSummary().isEmpty());
    }
    finally {
      updatingFromModel = wasUpdating;
    }
  }

  private void refreshColumnTypeVisibility(String type) {
    boolean reference = COLUMN_TYPE_REFERENCE.equals(type);
    columnReferenceBox.setVisible(reference);
    columnReferenceBox.setManaged(reference);
    columnExpressionBox.setVisible(!reference);
    columnExpressionBox.setManaged(!reference);
  }

  private static String columnTypeOf(Column column) {
    return column.getExpression() != null ? COLUMN_TYPE_EXPRESSION : COLUMN_TYPE_REFERENCE;
  }

  private String previewColumnLabel(Column column) {
    String label = firstNonBlankText(column.getLabel());
    if (label != null) {
      return label;
    }
    if (column.getName() != null && !column.getName().isBlank()) {
      return column.getName();
    }
    if (column.getElementRef() != null && !column.getElementRef().isBlank()) {
      return OverviewElementOptions.displayPath(documentModelIndex, column.getElementRef());
    }
    return column.getId() != null ? column.getId() : "(new column)";
  }

  private static ColumnAlignment ensureAlignment(Column column) {
    if (column.getAlignment() == null) {
      column.setAlignment(new ColumnAlignment());
    }
    return column.getAlignment();
  }

  private static Alignment ensureHeaderAlignment(Column column) {
    ColumnAlignment alignment = ensureAlignment(column);
    if (alignment.getHeader() == null) {
      alignment.setHeader(new Alignment());
    }
    return alignment.getHeader();
  }

  private static Alignment ensureContentAlignment(Column column) {
    ColumnAlignment alignment = ensureAlignment(column);
    if (alignment.getContent() == null) {
      alignment.setContent(new Alignment());
    }
    return alignment.getContent();
  }

  @FXML
  public void onAddColumn(ActionEvent e) {
    Column column = new Column();
    column.setId("column-" + shortId());
    column.setWidth(1.0);
    model.getContent().getColumns().add(column);
    refreshColumnsTable();
    columnsTable.getSelectionModel().select(column);
    commitChange();
  }

  @FXML
  public void onRemoveColumn(ActionEvent e) {
    Column column = selectedColumn();
    if (column == null) {
      return;
    }
    model.getContent().getColumns().remove(column);
    refreshColumnsTable();
    commitChange();
  }

  @FXML
  public void onMoveColumnUp(ActionEvent e) {
    moveColumn(-1);
  }

  @FXML
  public void onMoveColumnDown(ActionEvent e) {
    moveColumn(1);
  }

  private void moveColumn(int direction) {
    Column column = selectedColumn();
    if (column == null) {
      return;
    }
    List<Column> columns = model.getContent().getColumns();
    int index = columns.indexOf(column);
    int newIndex = index + direction;
    if (newIndex < 0 || newIndex >= columns.size()) {
      return;
    }
    Collections.swap(columns, index, newIndex);
    refreshColumnsTable();
    columnsTable.getSelectionModel().select(column);
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

  private MultiSelectionConfig ensureMultiSelectionConfig() {
    OverviewConfiguration configuration = ensureConfiguration();
    if (configuration.getMultiSelection() == null) {
      configuration.setMultiSelection(cachedMultiSelectionConfig != null ? cachedMultiSelectionConfig : new MultiSelectionConfig());
    }
    return configuration.getMultiSelection();
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

  private String describeMultiSelectionButton(de.a12.studio.models.overviewmodel.Button button) {
    String label = firstNonBlankText(button.getLabel());
    if (label != null) {
      return label;
    }
    return button.getEvent() != null && !button.getEvent().isBlank() ? button.getEvent() : "(new button)";
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
    button.setTooltip(new Tooltip(tooltip));
    button.setOnAction(event -> action.run());
    return button;
  }

  @Override
  public @NonNull ModelType getModelType() {
    return ModelType.OVERVIEW;
  }
}
