package de.a12.studio.ui.editors.overviewmodel;

import de.a12.studio.models.Label;
import de.a12.studio.models.overviewmodel.BooleanUserAccessOption;
import de.a12.studio.models.overviewmodel.Column;
import de.a12.studio.models.overviewmodel.FilterConfiguration;
import de.a12.studio.models.overviewmodel.FilterGroup;
import de.a12.studio.models.overviewmodel.FilterItem;
import de.a12.studio.models.overviewmodel.FilterItemOptions;
import de.a12.studio.models.overviewmodel.FilterSelectorConfig;
import de.a12.studio.models.overviewmodel.FilterTriggerConfig;
import de.a12.studio.models.overviewmodel.FilterTriggerValue;
import de.a12.studio.models.overviewmodel.JoinOperatorConfig;
import de.a12.studio.models.overviewmodel.NewFilterConfiguration;
import de.a12.studio.models.overviewmodel.OverviewConfiguration;
import de.a12.studio.models.overviewmodel.OverviewModel;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.validators.ElementIndex;
import de.a12.studio.modelsvalidation.validators.overview.OverviewFilterGroupsValidator;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.editors.AbstractPropertyEditor;
import de.a12.studio.ui.editors.overviewmodel.dialogs.Dialogs;
import de.a12.studio.ui.editors.propertyeditors.IconPanelController;
import de.a12.studio.ui.editors.propertyeditors.LocalizedTextPanelController;
import de.a12.studio.ui.editors.propertyeditors.RowFactory;
import de.a12.studio.ui.util.Icons;
import de.a12.studio.ui.util.StudioBundle;
import de.a12.studio.ui.util.WidgetFactory;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioButton;
import javafx.scene.control.SplitMenuButton;
import javafx.scene.input.DataFormat;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.jspecify.annotations.NonNull;
import org.kordamp.ikonli.javafx.FontIcon;

import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.UUID;

public class CustomFilterConfigurationPanelController extends AbstractPropertyEditor implements Initializable {

  // 3 action buttons (.default-button/.move-button, each min-width 34px) + 2 * 4px inter-button spacing,
  // matching createFilterGroupActionsBox()'s actual content width - a narrower box lets the buttons overflow
  // it and eat into the row's right padding. Must stay in sync with the matching 110.0-wide header Region in
  // the FXML so the header and rows line up on the right edge.
  private static final double ACTIONS_BOX_WIDTH = 110.0;

  // Identifies a filter-group row-reorder drag; the dragboard content is the dragged row's current index into
  // getFilterGroups().
  private static final DataFormat FILTER_GROUP_INDEX = new DataFormat("application/x-a12-overview-filter-group-index");

  @FXML
  private HBox filterGroupColumnHeaders;
  @FXML
  private VBox filterGroupRows;
  @FXML
  private javafx.scene.control.Label filterGroupsEmptyLabel;
  @FXML
  private SplitMenuButton addFilterGroupButton;

  @FXML
  private RadioButton matchAllField;
  @FXML
  private RadioButton matchAnyField;
  @FXML
  private CheckBox matchUserAccessField;
  @FXML
  private CheckBox initiallyInvertedField;
  @FXML
  private CheckBox initiallyInvertedUserAccessField;

  @FXML
  private RadioButton displayModeOverlayField;
  @FXML
  private RadioButton displayModeDockedField;
  @FXML
  private RadioButton displayModeModalField;
  @FXML
  private RadioButton initialVisibilityShowField;
  @FXML
  private RadioButton initialVisibilityHideField;
  @FXML
  private CheckBox showOnlySetFiltersField;
  @FXML
  private CheckBox showOnlySetFiltersUserAccessField;

  @FXML
  private LocalizedTextPanelController headerSubtitleTextController;

  @FXML
  private CheckBox showSearchFieldInitiallyField;
  @FXML
  private CheckBox showSearchFieldInitiallyUserAccessField;

  @FXML
  private IconPanelController filterButtonIconController;
  @FXML
  private CheckBox filterButtonUserAccessField;
  @FXML
  private CheckBox hideFilterButtonLabelField;
  @FXML
  private LocalizedTextPanelController filterButtonLabelController;

  private OverviewModel model;

  private ElementIndex documentModelIndex;

  // Set while fields are being repopulated from the model, so those programmatic updates aren't mistaken for
  // user edits and don't trigger a save.
  private boolean updatingFromModel;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    super.initialize(location, resources);
    initFilterResult();
    initFilterSelector();
    initSearchBar();
    initFilterButton();
    buildAddFilterGroupMenu();
  }

  /** Only relevant for {@link FilterConfiguration#FILTER_MODE_CUSTOM_FILTER} - hidden for every other filter
   * mode, see {@link OverviewModelEditorController}. */
  public void setVisible(boolean visible) {
    setEditorVisible(visible);
  }

  public void setModel(@NonNull OverviewModel model) {
    this.model = model;
    updatingFromModel = true;
    try {
      populateFilterResult();
      populateFilterSelector();
      populateSearchBar();
      populateFilterButton();
      rebuildFilterGroupRows();
    }
    finally {
      updatingFromModel = false;
    }
  }

  /** Re-points the Field Reference pickers in the Filter Group/Item dialogs, and the "Generate from document
   * fields" menu actions, at the currently referenced Document Model. */
  public void setDocumentModelIndex(ElementIndex documentModelIndex) {
    this.documentModelIndex = documentModelIndex;
    boolean hasDocumentModel = documentModelIndex != null;
    for (MenuItem item : addFilterGroupButton.getItems()) {
      item.setDisable(!hasDocumentModel);
    }
    rebuildFilterGroupRows();
  }

  // ---- Filter Result ----

  private void initFilterResult() {
    matchAllField.selectedProperty().addListener((observable, oldValue, newValue) -> {
      if (newValue) {
        applyMatch(JoinOperatorConfig.MATCH_ALL);
      }
    });
    matchAnyField.selectedProperty().addListener((observable, oldValue, newValue) -> {
      if (newValue) {
        applyMatch(JoinOperatorConfig.MATCH_ANY);
      }
    });
    matchUserAccessField.selectedProperty().addListener((observable, oldValue, newValue) -> {
      if (updatingFromModel || model == null) {
        return;
      }
      ensureJoinOperator().setEnabled(newValue);
      commitHeaderChange();
    });
    initiallyInvertedField.selectedProperty().addListener((observable, oldValue, newValue) -> {
      if (updatingFromModel || model == null) {
        return;
      }
      ensureInvert().setValue(newValue);
      commitHeaderChange();
    });
    initiallyInvertedUserAccessField.selectedProperty().addListener((observable, oldValue, newValue) -> {
      if (updatingFromModel || model == null) {
        return;
      }
      ensureInvert().setEnabled(newValue);
      commitHeaderChange();
    });
  }

  private void applyMatch(String value) {
    if (updatingFromModel || model == null) {
      return;
    }
    ensureJoinOperator().setValue(value);
    commitHeaderChange();
  }

  private void populateFilterResult() {
    NewFilterConfiguration configuration = currentNewFilterConfiguration();
    JoinOperatorConfig joinOperator = configuration != null ? configuration.getJoinOperator() : null;
    String match = joinOperator != null && joinOperator.getValue() != null ? joinOperator.getValue() : JoinOperatorConfig.MATCH_ALL;
    matchAllField.setSelected(JoinOperatorConfig.MATCH_ALL.equals(match));
    matchAnyField.setSelected(JoinOperatorConfig.MATCH_ANY.equals(match));
    matchUserAccessField.setSelected(joinOperator != null && Boolean.TRUE.equals(joinOperator.getEnabled()));

    BooleanUserAccessOption invert = configuration != null ? configuration.getInvert() : null;
    initiallyInvertedField.setSelected(invert != null && Boolean.TRUE.equals(invert.getValue()));
    initiallyInvertedUserAccessField.setSelected(invert != null && Boolean.TRUE.equals(invert.getEnabled()));
  }

  // ---- Filter Selector ----

  private void initFilterSelector() {
    displayModeOverlayField.selectedProperty().addListener((observable, oldValue, newValue) -> {
      if (newValue) {
        applyDisplayMode(FilterSelectorConfig.VIEW_MODE_OVERLAY);
      }
    });
    displayModeDockedField.selectedProperty().addListener((observable, oldValue, newValue) -> {
      if (newValue) {
        applyDisplayMode(FilterSelectorConfig.VIEW_MODE_DOCKED);
      }
    });
    displayModeModalField.selectedProperty().addListener((observable, oldValue, newValue) -> {
      if (newValue) {
        applyDisplayMode(FilterSelectorConfig.VIEW_MODE_MODAL);
      }
    });
    initialVisibilityShowField.selectedProperty().addListener((observable, oldValue, newValue) -> {
      if (newValue) {
        applyInitialVisibility(FilterSelectorConfig.VISIBILITY_SHOW);
      }
    });
    initialVisibilityHideField.selectedProperty().addListener((observable, oldValue, newValue) -> {
      if (newValue) {
        applyInitialVisibility(FilterSelectorConfig.VISIBILITY_HIDE);
      }
    });
    showOnlySetFiltersField.selectedProperty().addListener((observable, oldValue, newValue) -> {
      if (updatingFromModel || model == null) {
        return;
      }
      ensureShowOnlySetFilters().setValue(newValue);
      commitHeaderChange();
    });
    showOnlySetFiltersUserAccessField.selectedProperty().addListener((observable, oldValue, newValue) -> {
      if (updatingFromModel || model == null) {
        return;
      }
      ensureShowOnlySetFilters().setEnabled(newValue);
      commitHeaderChange();
    });

    headerSubtitleTextController.configureCustom("headerSubtitle", StudioBundle.get("subtitle"));
  }

  private void applyDisplayMode(String value) {
    if (updatingFromModel || model == null) {
      return;
    }
    ensureFilterSelector().setViewMode(value);
    commitHeaderChange();
  }

  private void applyInitialVisibility(String value) {
    if (updatingFromModel || model == null) {
      return;
    }
    ensureFilterSelector().setInitialVisibility(value);
    commitHeaderChange();
  }

  private void populateFilterSelector() {
    FilterSelectorConfig selector = currentFilterSelector();

    String viewMode = selector != null ? selector.getViewMode() : null;
    displayModeOverlayField.setSelected(FilterSelectorConfig.VIEW_MODE_OVERLAY.equals(viewMode));
    displayModeDockedField.setSelected(FilterSelectorConfig.VIEW_MODE_DOCKED.equals(viewMode));
    displayModeModalField.setSelected(FilterSelectorConfig.VIEW_MODE_MODAL.equals(viewMode));

    String visibility = selector != null ? selector.getInitialVisibility() : null;
    initialVisibilityShowField.setSelected(FilterSelectorConfig.VISIBILITY_SHOW.equals(visibility));
    initialVisibilityHideField.setSelected(FilterSelectorConfig.VISIBILITY_HIDE.equals(visibility));

    BooleanUserAccessOption showOnlySetFilters = selector != null ? selector.getShowSetFiltersOnly() : null;
    showOnlySetFiltersField.setSelected(showOnlySetFilters != null && Boolean.TRUE.equals(showOnlySetFilters.getValue()));
    showOnlySetFiltersUserAccessField.setSelected(showOnlySetFilters != null && Boolean.TRUE.equals(showOnlySetFilters.getEnabled()));

    // Read via currentFilterSelector() (non-mutating) rather than ensureFilterSelector(): merely opening this
    // panel must not materialize newFilterConfiguration.filterSelector on a model that has none, since nothing
    // was actually edited yet. ensureFilterSelector() is still used for the write side, so a parent chain
    // missing on read is only created once the user actually types into a locale field.
    headerSubtitleTextController.setCustom(this::currentHeaderSubtitle, () -> ensureFilterSelector().getHeaderSubtitle());
  }

  private List<Label> currentHeaderSubtitle() {
    FilterSelectorConfig selector = currentFilterSelector();
    return selector != null ? selector.getHeaderSubtitle() : List.of();
  }

  // ---- Search Bar ----

  private void initSearchBar() {
    showSearchFieldInitiallyField.selectedProperty().addListener((observable, oldValue, newValue) -> {
      if (updatingFromModel || model == null) {
        return;
      }
      ensureSearchBar().setValue(newValue);
      commitHeaderChange();
    });
    showSearchFieldInitiallyUserAccessField.selectedProperty().addListener((observable, oldValue, newValue) -> {
      if (updatingFromModel || model == null) {
        return;
      }
      ensureSearchBar().setEnabled(newValue);
      commitHeaderChange();
    });
  }

  private void populateSearchBar() {
    FilterSelectorConfig selector = currentFilterSelector();
    BooleanUserAccessOption searchBar = selector != null ? selector.getSearchBar() : null;
    showSearchFieldInitiallyField.setSelected(searchBar != null && Boolean.TRUE.equals(searchBar.getValue()));
    showSearchFieldInitiallyUserAccessField.setSelected(searchBar != null && Boolean.TRUE.equals(searchBar.getEnabled()));
  }

  // ---- Filter Button ----

  private void initFilterButton() {
    filterButtonUserAccessField.selectedProperty().addListener((observable, oldValue, newValue) -> {
      if (updatingFromModel || model == null) {
        return;
      }
      ensureTrigger().setEnabled(newValue);
      commitHeaderChange();
    });
    hideFilterButtonLabelField.selectedProperty().addListener((observable, oldValue, newValue) -> {
      if (updatingFromModel || model == null) {
        return;
      }
      ensureTriggerValue().setHideLabel(newValue ? Boolean.TRUE : null);
      commitHeaderChange();
    });

    filterButtonLabelController.configureCustom("filterButtonLabel", StudioBundle.get("label"));
  }

  private void populateFilterButton() {
    FilterSelectorConfig selector = currentFilterSelector();
    FilterTriggerConfig trigger = selector != null ? selector.getTrigger() : null;
    filterButtonUserAccessField.setSelected(trigger != null && Boolean.TRUE.equals(trigger.getEnabled()));

    FilterTriggerValue triggerValue = trigger != null ? trigger.getValue() : null;
    hideFilterButtonLabelField.setSelected(triggerValue != null && Boolean.TRUE.equals(triggerValue.getHideLabel()));

    // Read via currentTriggerValue() (non-mutating) rather than ensureTriggerValue(): same reasoning as
    // currentHeaderSubtitle() above - opening this panel must not materialize newFilterConfiguration.
    // filterSelector.trigger.value on a model that has none. The setter/write side still goes through
    // ensureTriggerValue(), so the parent chain is only created once the user actually edits the icon/label.
    filterButtonIconController.setCustom(() -> {
      FilterTriggerValue value = currentTriggerValue();
      return value != null ? value.getIcon() : null;
    }, icon -> ensureTriggerValue().setIcon(icon));
    filterButtonLabelController.setCustom(this::currentFilterButtonLabel, () -> ensureTriggerValue().getLabel());
  }

  private FilterTriggerValue currentTriggerValue() {
    FilterSelectorConfig selector = currentFilterSelector();
    FilterTriggerConfig trigger = selector != null ? selector.getTrigger() : null;
    return trigger != null ? trigger.getValue() : null;
  }

  private List<Label> currentFilterButtonLabel() {
    FilterTriggerValue value = currentTriggerValue();
    return value != null ? value.getLabel() : List.of();
  }

  // ---- Filter Groups ----

  private void buildAddFilterGroupMenu() {
    MenuItem generateFromColumns = new MenuItem(StudioBundle.get("generate_from_overview_columns"));
    generateFromColumns.setOnAction(event -> generateFilterGroupsFromColumns());

    MenuItem generateFromFields = new MenuItem(StudioBundle.get("generate_from_document_fields"));
    generateFromFields.setOnAction(event -> generateFilterGroupsFromFields());

    MenuItem generateFromFieldsAndMetadata = new MenuItem(StudioBundle.get("generate_from_document_fields_and_metadata"));
    generateFromFieldsAndMetadata.setOnAction(event -> generateFilterGroupsFromFields());

    addFilterGroupButton.getItems().setAll(generateFromColumns, generateFromFields, generateFromFieldsAndMetadata);
  }

  @FXML
  private void onAddFilterGroup() {
    Dialogs.showFilterGroupForAdd(Studio.stage, documentModelIndex).ifPresent(group -> {
      getFilterGroups().add(group);
      rebuildFilterGroupRows();
      commitHeaderChange();
    });
  }

  /** "Generate from overview columns": one group (with one field-reference filter item) per {@link Column}.
   * Replaces any existing filter groups rather than appending to them, so re-running (or switching to) one of
   * the "Generate from ..." menu options doesn't pile up duplicates alongside what was there before. */
  private void generateFilterGroupsFromColumns() {
    if (model == null || model.getContent().getColumns() == null) {
      return;
    }
    List<FilterGroup> groups = getFilterGroups();
    groups.clear();
    for (Column column : model.getContent().getColumns()) {
      if (column.getElementRef() == null) {
        continue;
      }
      groups.add(newGeneratedFilterGroup(column.getElementRef(), column.getLabel()));
    }
    rebuildFilterGroupRows();
    commitHeaderChange();
  }

  /** "Generate from document fields" / "+ metadata": one group (with one field-reference filter item) per
   * field in the referenced Document Model. Both menu actions behave identically for now: this codebase has no
   * separate metadata-field enumeration anywhere ({@link ElementIndex#allElements()} only walks the Document
   * Model's own element tree), the same limitation {@code FilterConfiguration.FILTER_MODE_ALL_WITH_META}
   * already has elsewhere in this editor. */
  private void generateFilterGroupsFromFields() {
    if (model == null || documentModelIndex == null) {
      return;
    }
    List<FilterGroup> groups = getFilterGroups();
    groups.clear();
    for (String fieldId : OverviewElementOptions.elementIds(documentModelIndex)) {
      groups.add(newGeneratedFilterGroup(fieldId, OverviewElementOptions.fieldLabel(documentModelIndex, fieldId)));
    }
    rebuildFilterGroupRows();
    commitHeaderChange();
  }

  private FilterGroup newGeneratedFilterGroup(String fieldId, List<Label> label) {
    FilterGroup group = new FilterGroup();
    group.setId("filter-group-" + shortId());
    group.setName(fieldId);
    group.getLabel().addAll(copyLabels(label));

    FilterItem item = new FilterItem();
    item.setId("filter-item-" + shortId());
    FilterItemOptions options = new FilterItemOptions();
    options.setFieldId(fieldId);
    item.setOptions(options);
    item.setType(OverviewElementOptions.filterItemFieldType(documentModelIndex, fieldId));
    group.getFilterItems().add(item);

    return group;
  }

  private static List<Label> copyLabels(List<Label> source) {
    return source.stream().map(label -> {
      Label copy = new Label();
      copy.setLocale(label.getLocale());
      copy.setText(label.getText());
      return copy;
    }).toList();
  }

  private List<FilterGroup> getFilterGroups() {
    return ensureNewFilterConfiguration().getFilterGroups();
  }

  private List<FilterGroup> currentFilterGroups() {
    NewFilterConfiguration configuration = currentNewFilterConfiguration();
    return configuration != null ? configuration.getFilterGroups() : List.of();
  }

  private void rebuildFilterGroupRows() {
    if (model == null) {
      return;
    }
    refreshValidationError();
    filterGroupRows.getChildren().clear();

    List<FilterGroup> groups = currentFilterGroups();
    boolean empty = groups.isEmpty();
    filterGroupColumnHeaders.setVisible(!empty);
    filterGroupColumnHeaders.setManaged(!empty);
    filterGroupsEmptyLabel.setVisible(empty);
    filterGroupsEmptyLabel.setManaged(empty);

    for (int index = 0; index < groups.size(); index++) {
      filterGroupRows.getChildren().add(createFilterGroupRow(groups.get(index), index, groups.size()));
    }
  }

  /** {@link OverviewFilterGroupsValidator} keys its errors off a synthetic {@code ELEMENT_ID} for the whole
   * filter-groups list, not a single bound {@link de.a12.studio.models.documentmodel.Element}, so this panel
   * must query and display them itself - same pattern as {@link OverviewSortingPanelController}. */
  private void refreshValidationError() {
    if (currentFilterGroups().isEmpty()) {
      hideError();
      return;
    }
    List<ModelValidationError> errors =
        Studio.getValidationService().validateElement(model, OverviewFilterGroupsValidator.ELEMENT_ID);
    if (errors.isEmpty()) {
      hideError();
    } else {
      showError(errors.get(0).severity(), errors.get(0).message());
    }
  }

  private HBox createFilterGroupRow(FilterGroup group, int index, int rowCount) {
    FontIcon dragHandle = RowFactory.createDragHandle();

    javafx.scene.control.Label nameCell = new javafx.scene.control.Label(filterGroupSummary(group));
    nameCell.setId("filterGroupName-" + index);
    nameCell.setMaxWidth(Double.MAX_VALUE);
    nameCell.setAlignment(Pos.CENTER_LEFT);
    makeClickableToEdit(nameCell, group);

    // Wrapped in a VBox (rather than added to contentGrid directly) so the shorter name label reliably
    // centers vertically against the taller filter-items column via the wrapper's own alignment, regardless
    // of the GridPane row's fill/valignment behavior for the Label itself.
    VBox nameCellWrapper = new VBox(nameCell);
    nameCellWrapper.setAlignment(Pos.CENTER_LEFT);
    nameCellWrapper.setMaxHeight(Region.USE_PREF_SIZE);

    VBox itemsCell = createFilterItemsCell(group, index);

    GridPane contentGrid = new GridPane();
    contentGrid.setHgap(10.0);
    contentGrid.setMaxWidth(Double.MAX_VALUE);
    ColumnConstraints nameColumn = new ColumnConstraints();
    nameColumn.setPercentWidth(33.33);
    ColumnConstraints itemsColumn = new ColumnConstraints();
    itemsColumn.setPercentWidth(66.67);
    contentGrid.getColumnConstraints().addAll(nameColumn, itemsColumn);
    contentGrid.add(nameCellWrapper, 0, 0);
    contentGrid.add(itemsCell, 1, 0);
    GridPane.setValignment(nameCellWrapper, VPos.CENTER);
    GridPane.setValignment(itemsCell, VPos.CENTER);
    HBox.setHgrow(contentGrid, Priority.ALWAYS);

    HBox actionsBox = createFilterGroupActionsBox(group, index, rowCount);
    actionsBox.setPrefWidth(ACTIONS_BOX_WIDTH);
    actionsBox.setMinWidth(ACTIONS_BOX_WIDTH);
    actionsBox.setMaxWidth(ACTIONS_BOX_WIDTH);
    actionsBox.setMaxHeight(Region.USE_PREF_SIZE);
    HBox.setHgrow(actionsBox, Priority.NEVER);

    HBox row = new HBox(10.0, dragHandle, contentGrid, actionsBox);
    row.setAlignment(Pos.CENTER_LEFT);
    row.getStyleClass().add("module-row");
    RowFactory.setupRowDragAndDrop(row, dragHandle, FILTER_GROUP_INDEX, index, this::onFilterGroupRowDropped);
    return row;
  }

  private void onFilterGroupRowDropped(int fromIndex, int insertBeforeIndex) {
    if (RowFactory.reorder(getFilterGroups(), fromIndex, insertBeforeIndex)) {
      rebuildFilterGroupRows();
      commitHeaderChange();
    }
  }

  private VBox createFilterItemsCell(FilterGroup group, int index) {
    VBox cell = new VBox(4.0);
    cell.setId("filterGroupItems-" + index);
    cell.setAlignment(Pos.CENTER_LEFT);
    cell.setMaxWidth(Double.MAX_VALUE);
    HBox.setHgrow(cell, Priority.ALWAYS);
    for (FilterItem item : group.getFilterItems()) {
      javafx.scene.control.Label chip = new javafx.scene.control.Label(filterItemSummary(item));
      chip.getStyleClass().add("path-chip");
      chip.setWrapText(true);
      chip.setMaxWidth(Double.MAX_VALUE);
      cell.getChildren().add(chip);
    }
    makeClickableToEdit(cell, group);
    return cell;
  }

  private String filterGroupSummary(FilterGroup group) {
    if (group.getName() != null && !group.getName().isBlank()) {
      return group.getName();
    }
    return firstLabelText(group.getLabel());
  }

  private String filterItemSummary(FilterItem item) {
    String label = firstLabelText(item.getLabel());
    if (!label.isBlank()) {
      return label;
    }
    if (item.getOptions() != null && item.getOptions().getFieldId() != null) {
      return OverviewElementOptions.displayPath(documentModelIndex, item.getOptions().getFieldId());
    }
    return "";
  }

  private static String firstLabelText(List<Label> labels) {
    return labels.stream()
        .map(Label::getText)
        .filter(text -> text != null && !text.isBlank())
        .findFirst()
        .orElse("");
  }

  private void makeClickableToEdit(Node node, FilterGroup group) {
    node.setCursor(Cursor.HAND);
    node.setOnMouseClicked(event -> {
      if (event.getClickCount() == 1) {
        openFilterGroupEditDialog(group);
      }
    });
  }

  private void openFilterGroupEditDialog(FilterGroup group) {
    if (Dialogs.showFilterGroupForEdit(Studio.stage, group, documentModelIndex)) {
      rebuildFilterGroupRows();
      commitHeaderChange();
    }
  }

  private HBox createFilterGroupActionsBox(FilterGroup group, int index, int rowCount) {
    VBox moveButtonsBox = RowFactory.createMoveButtonsBox(index, rowCount, this::moveFilterGroup);

    Button editButton = RowFactory.createActionButton(Icons.PENCIL, "Edit", () -> openFilterGroupEditDialog(group));

    Button deleteButton = RowFactory.createActionButton(Icons.TRASH, StudioBundle.get("delete"), () -> {
      Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, StudioBundle.get("delete_this_filter_group"), null, null, StudioBundle.get("delete"));
      if (result.isPresent() && result.get() == ButtonType.OK) {
        getFilterGroups().remove(group);
        rebuildFilterGroupRows();
        commitHeaderChange();
      }
    });

    HBox actionsBox = new HBox(4.0, moveButtonsBox, editButton, deleteButton);
    actionsBox.setAlignment(Pos.CENTER_LEFT);
    return actionsBox;
  }

  private void moveFilterGroup(int fromIndex, int toIndex) {
    Collections.swap(getFilterGroups(), fromIndex, toIndex);
    rebuildFilterGroupRows();
    commitHeaderChange();
  }

  private static String shortId() {
    return UUID.randomUUID().toString().replace("-", "").substring(0, 5);
  }

  // ---- Shared helpers ----

  private OverviewConfiguration ensureConfiguration() {
    if (model.getContent().getConfiguration() == null) {
      model.getContent().setConfiguration(new OverviewConfiguration());
    }
    return model.getContent().getConfiguration();
  }

  private NewFilterConfiguration ensureNewFilterConfiguration() {
    OverviewConfiguration configuration = ensureConfiguration();
    if (configuration.getNewFilterConfiguration() == null) {
      configuration.setNewFilterConfiguration(new NewFilterConfiguration());
    }
    return configuration.getNewFilterConfiguration();
  }

  private JoinOperatorConfig ensureJoinOperator() {
    NewFilterConfiguration configuration = ensureNewFilterConfiguration();
    if (configuration.getJoinOperator() == null) {
      configuration.setJoinOperator(new JoinOperatorConfig());
    }
    return configuration.getJoinOperator();
  }

  private BooleanUserAccessOption ensureInvert() {
    NewFilterConfiguration configuration = ensureNewFilterConfiguration();
    if (configuration.getInvert() == null) {
      configuration.setInvert(new BooleanUserAccessOption());
    }
    return configuration.getInvert();
  }

  private FilterSelectorConfig ensureFilterSelector() {
    NewFilterConfiguration configuration = ensureNewFilterConfiguration();
    if (configuration.getFilterSelector() == null) {
      configuration.setFilterSelector(new FilterSelectorConfig());
    }
    return configuration.getFilterSelector();
  }

  private BooleanUserAccessOption ensureShowOnlySetFilters() {
    FilterSelectorConfig selector = ensureFilterSelector();
    if (selector.getShowSetFiltersOnly() == null) {
      selector.setShowSetFiltersOnly(new BooleanUserAccessOption());
    }
    return selector.getShowSetFiltersOnly();
  }

  private BooleanUserAccessOption ensureSearchBar() {
    FilterSelectorConfig selector = ensureFilterSelector();
    if (selector.getSearchBar() == null) {
      selector.setSearchBar(new BooleanUserAccessOption());
    }
    return selector.getSearchBar();
  }

  private FilterTriggerConfig ensureTrigger() {
    FilterSelectorConfig selector = ensureFilterSelector();
    if (selector.getTrigger() == null) {
      selector.setTrigger(new FilterTriggerConfig());
    }
    return selector.getTrigger();
  }

  private FilterTriggerValue ensureTriggerValue() {
    FilterTriggerConfig trigger = ensureTrigger();
    if (trigger.getValue() == null) {
      trigger.setValue(new FilterTriggerValue());
    }
    return trigger.getValue();
  }

  private NewFilterConfiguration currentNewFilterConfiguration() {
    OverviewConfiguration configuration = model.getContent().getConfiguration();
    return configuration != null ? configuration.getNewFilterConfiguration() : null;
  }

  private FilterSelectorConfig currentFilterSelector() {
    NewFilterConfiguration configuration = currentNewFilterConfiguration();
    return configuration != null ? configuration.getFilterSelector() : null;
  }
}
