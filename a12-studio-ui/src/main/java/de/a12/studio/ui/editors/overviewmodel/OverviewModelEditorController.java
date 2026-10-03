package de.a12.studio.ui.editors.overviewmodel;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.ModelReference;
import de.a12.studio.models.ModelType;
import de.a12.studio.models.NewModelFactory;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.documentmodel.Element;
import de.a12.studio.models.overviewmodel.BoxElement;
import de.a12.studio.models.overviewmodel.Button;
import de.a12.studio.models.overviewmodel.ButtonElement;
import de.a12.studio.models.overviewmodel.Column;
import de.a12.studio.models.overviewmodel.ColumnLinkReference;
import de.a12.studio.models.overviewmodel.ColumnRef;
import de.a12.studio.models.overviewmodel.ElementBox;
import de.a12.studio.models.overviewmodel.FilterConfiguration;
import de.a12.studio.models.overviewmodel.OverviewConfiguration;
import de.a12.studio.models.overviewmodel.OverviewModel;
import de.a12.studio.models.overviewmodel.RowActionGroup;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.models.querymodel.QueryModel;
import de.a12.studio.models.querymodel.QueryPaging;
import de.a12.studio.models.querymodel.QuerySort;
import de.a12.studio.models.relationshipmodel.RelationshipModel;
import de.a12.studio.modelsvalidation.validators.ElementIndex;
import de.a12.studio.modelsvalidation.validators.overview.OverviewBindingPurpose;
import de.a12.studio.modelsvalidation.validators.overview.OverviewSubHeaderPruning;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.editors.AbstractEditorController;
import de.a12.studio.ui.editors.overviewmodel.dialogs.CreateQueryModelDialogController;
import de.a12.studio.ui.editors.overviewmodel.dialogs.Dialogs;
import de.a12.studio.ui.editors.propertyeditors.EventButtonsPanelController;
import de.a12.studio.ui.editors.propertyeditors.LocalizedTextPanelController;
import de.a12.studio.ui.editors.propertyeditors.RolesEditorPanelController;
import de.a12.studio.ui.events.StudioEventManager;
import de.a12.studio.ui.util.ProjectDocumentModels;
import de.a12.studio.ui.util.StudioBundle;
import de.a12.studio.ui.util.WidgetFactory;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import org.jspecify.annotations.NonNull;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Edits an {@link OverviewModel}'s "Overview" and "Custom Actions" tabs.
 * <p>
 * "Overview": General Settings (the Overview Reference, delegated to {@link OverviewReferencePanelController}),
 * Columns (delegated to {@link OverviewColumnsPanelController}), Search and Filters (search/filter/row-count,
 * delegated to {@link OverviewSearchAndFiltersPanelController}), Multi-Selection (delegated to {@link
 * OverviewMultiSelectionPanelController}), Custom Selection Of Fields (delegated to {@link
 * CustomSelectionOfFieldsPanelController}), Section Data (delegated to {@link
 * OverviewSectionDataPanelController}), Custom Filter Configuration ({@code content.configuration.
 * newFilterConfiguration}, the "Custom Filter" filter mode's full filter structure, delegated to {@link
 * CustomFilterConfigurationPanelController}), Filter String Fields with Multi-Select (delegated to {@link
 * FilterStringFieldsMultiSelectPanelController}), Row Height And Action Column Width (delegated to {@link
 * RowHeightActionColumnWidthPanelController}), Paging Behaviour (delegated to {@link
 * PagingBehaviourPanelController}), Accessibility (delegated to {@link OverviewAccessibilityPanelController})
 * and Styles (delegated to {@link StylesPanelController}).
 * <p>
 * "Custom Actions": Row Action Group ({@code content.rowActionGroup.actions}, delegated to {@link
 * de.a12.studio.ui.editors.propertyeditors.EventButtonsPanelController}, and {@code content.contextMenu},
 * delegated to {@link ContextMenuPanelController}), Row Activation ({@code content.defaultRowAction}, delegated
 * to {@link RowActivationPanelController}) and Title For Interactive Rows ({@code content.configuration.
 * rowTitle}, delegated to {@link de.a12.studio.ui.editors.propertyeditors.LocalizedTextPanelController}) and Subtitle
 * ({@code content.configuration.subtitle}, delegated to another instance of the same {@link
 * de.a12.studio.ui.editors.propertyeditors.LocalizedTextPanelController}), Subheader ({@code
 * content.subHeaderBox}, delegated to {@link SubheaderSlotPanelController} - a mixed list of button/search/
 * filter/multi-selection position markers) and Footer ({@code content.footerBox}, Button-only, delegated to
 * {@link de.a12.studio.ui.editors.propertyeditors.EventButtonsPanelController}). Per the {@code
 * testing/basic/models} fixtures (e.g. {@code RelationshipOMs/*_OM.json}, {@code Invoice_OM.json}), both boxes
 * persist as {@code {leftSlot: [...], rightSlot: [...]}}; "Major" (Subheader)/"Major Buttons" (Footer) map to
 * {@code rightSlot}, "Minor"/"Minor Buttons" to {@code leftSlot}.
 */
public class OverviewModelEditorController extends AbstractEditorController implements Initializable {

  // General Settings
  @FXML
  private OverviewReferencePanelController overviewReferenceController;

  // Search and Filters
  @FXML
  private OverviewSearchAndFiltersPanelController overviewSearchAndFiltersController;

  // Multi-Selection
  @FXML
  private OverviewMultiSelectionPanelController overviewMultiSelectionController;

  // Custom Selection Of Fields
  @FXML
  private CustomSelectionOfFieldsPanelController customSelectionOfFieldsController;

  // Section Data
  @FXML
  private OverviewSectionDataPanelController overviewSectionDataController;

  // Custom Filter Configuration
  @FXML
  private CustomFilterConfigurationPanelController customFilterConfigurationController;

  // Filter String Fields with Multi-Select
  @FXML
  private FilterStringFieldsMultiSelectPanelController filterStringFieldsMultiSelectController;

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

  // Custom Actions: Row Action Group
  @FXML
  private EventButtonsPanelController rowActionButtonsController;
  @FXML
  private ContextMenuPanelController contextMenuController;

  // Custom Actions: Row Activation
  @FXML
  private RowActivationPanelController rowActivationController;
  @FXML
  private LocalizedTextPanelController rowTitleController;
  @FXML
  private LocalizedTextPanelController subtitleController;

  // Custom Actions: Subheader
  @FXML
  private SubheaderSlotPanelController subheaderMajorController;
  @FXML
  private SubheaderSlotPanelController subheaderMinorController;

  // Custom Actions: Footer
  @FXML
  private EventButtonsPanelController footerMinorButtonsController;
  @FXML
  private EventButtonsPanelController footerMajorButtonsController;

  private OverviewModel model;
  private List<DocumentModel> otherDocumentModels = List.of();
  private List<QueryModel> otherQueryModels = List.of();
  private List<RelationshipModel> otherRelationshipModels = List.of();
  // Form Model Bindings and Relationship UI Model DualPaneSelection/TableList components, the only two sources
  // OverviewBindingPurpose.resolve scans - cached for pruneSubHeader (gap 17 of "Overview Model: gap review"),
  // which needs the current purpose to know whether Search/Multi-Selection are prunable at all (never, for
  // either Binding purpose - see OverviewSubHeaderElementValidator's own javadoc).
  private List<A12Model<?>> otherModelsForBindingPurpose = List.of();
  private ElementIndex documentModelIndex;
  private final Map<String, ElementIndex> linkedDocumentModelIndexByModelId = new HashMap<>();
  private final Function<ColumnLinkReference, ElementIndex> linkDocumentModelIndexResolver = this::linkedDocumentModelIndex;

  @Override
  public void initialize(URL url, ResourceBundle resources) {
    initializeGeneralSettings();
    rowTitleController.configureCustom("rowTitle", StudioBundle.get("title_for_interactive_rows"));
    subtitleController.configureCustom("subtitle", StudioBundle.get("overview_subtitle"));
  }

  private void initializeGeneralSettings() {
    overviewReferenceController.setOnChange(() -> {
      refreshDocumentModelIndex();
      commitChange();
    });
    overviewReferenceController.setOnAddQueryModel(this::onAddQueryModel);
    // The Sorting panel's column picker and its own dangling-reference validation, as well as the
    // Accessibility panel's screen-reader column picker, both derive from the Columns list, so keep them in
    // sync with every structural change made there.
    overviewColumnsController.setOnChange(() -> {
      overviewSortingController.refresh();
      overviewAccessibilityController.refresh();
    });
    overviewSearchAndFiltersController.setOnRelevanceChange(this::updateFilterModeDependentVisibility);
    overviewSearchAndFiltersController.setOnFeatureSwitchChange(this::pruneSubHeader);
    overviewMultiSelectionController.setOnEnabledChange(this::pruneSubHeader);
    // Switching Pagination <-> Infinite Scrolling changes whether Row Height/Action Column Width are required,
    // and may itself seed a default Row Height (see PagingBehaviourPanelController) - re-read/re-validate them.
    overviewPagingBehaviourController.setOnBehaviourChange(overviewRowHeightActionColumnWidthController::refresh);
    // The Multi-Selection panel's "exactly one Multi-Selection element in Sub header" validation and the
    // Search and Filters panel's "exactly one Search element in Sub header" validation both depend on the
    // Subheader panels' content, so re-check them whenever either slot changes.
    subheaderMajorController.setOnChange(() -> {
      overviewMultiSelectionController.refresh();
      overviewSearchAndFiltersController.refresh();
    });
    subheaderMinorController.setOnChange(() -> {
      overviewMultiSelectionController.refresh();
      overviewSearchAndFiltersController.refresh();
    });
  }

  /**
   * filterMode and showFilterButton decide which of the Custom Selection Of Fields/Section Data/Custom Filter
   * Configuration panels are relevant: Custom Selection Of Fields only for {@link
   * FilterConfiguration#FILTER_MODE_CUSTOM_LIST}; Section Data for every mode except {@link
   * FilterConfiguration#FILTER_MODE_CUSTOM_FILTER} (which models the same grouping via its own Filter Groups
   * instead) AND only while the filter button is shown, since Section Data groups the fields shown in that
   * button's dropdown and is meaningless once the button itself is hidden; Custom Filter Configuration only for
   * that same {@code custom_filter} mode; Filter String Fields with Multi-Select for every mode except {@code
   * custom_filter}, which it does not apply to. Re-run on every {@link
   * OverviewSearchAndFiltersPanelController#setOnRelevanceChange} notification, including the initial one fired
   * from its own {@code setModel}.
   */
  private void updateFilterModeDependentVisibility() {
    String filterMode = overviewSearchAndFiltersController.getFilterMode();
    boolean customFilter = FilterConfiguration.FILTER_MODE_CUSTOM_FILTER.equals(filterMode);
    boolean showFilterButton = overviewSearchAndFiltersController.isShowFilterButtonSelected();

    customSelectionOfFieldsController.setVisible(FilterConfiguration.FILTER_MODE_CUSTOM_LIST.equals(filterMode));
    overviewSectionDataController.setVisible(!customFilter && showFilterButton);
    customFilterConfigurationController.setVisible(customFilter);
    filterStringFieldsMultiSelectController.setVisible(!customFilter);
  }

  /**
   * Gap 17 of "Overview Model: gap review" (structural refactoring): removes a Filter/Search/Multi-Selection
   * Subheader element that just became disallowed because the user switched its feature off - Enable Filter/Show
   * Filter Button ({@link OverviewSearchAndFiltersPanelController#setOnFeatureSwitchChange}), Show Full Text
   * Search (same callback) or Multi-Selection's own enabled checkbox ({@link
   * OverviewMultiSelectionPanelController#setOnEnabledChange}) - mirroring what SME's own refactoring dialog
   * does. Never runs from the initial {@code load()} itself (neither callback fires from a panel's {@code
   * setModel}/{@code loadFromModel}, only from a user toggle - see {@link OverviewSortingPanelController#refresh}
   * for why an initial load must not silently prune-and-save a pre-existing file). A no-op (via {@link
   * OverviewSubHeaderPruning#pruneDisallowedElements}) if nothing is actually disallowed.
   */
  private void pruneSubHeader() {
    if (model == null) {
      return;
    }
    String purpose = OverviewBindingPurpose.resolve(model.getId(), otherModelsForBindingPurpose);
    if (!OverviewSubHeaderPruning.pruneDisallowedElements(model, purpose)) {
      return;
    }
    subheaderMajorController.refresh();
    subheaderMinorController.refresh();
    overviewMultiSelectionController.refresh();
    overviewSearchAndFiltersController.refresh();
    commitChange();
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
      otherDocumentModels = ProjectDocumentModels.getOtherDocumentModelsWithGenerated(projectItem);
      otherQueryModels = ProjectDocumentModels.getOtherModelsOfType(projectItem, ModelType.QUERY).stream()
          .filter(QueryModel.class::isInstance)
          .map(QueryModel.class::cast)
          .toList();
      otherRelationshipModels = ProjectDocumentModels.getOtherModelsOfType(projectItem, ModelType.RELATIONSHIP).stream()
          .filter(RelationshipModel.class::isInstance)
          .map(RelationshipModel.class::cast)
          .toList();
      otherModelsForBindingPurpose = new ArrayList<>();
      otherModelsForBindingPurpose.addAll(ProjectDocumentModels.getOtherModelsOfType(projectItem, ModelType.FORM));
      otherModelsForBindingPurpose.addAll(ProjectDocumentModels.getOtherModelsOfType(projectItem, ModelType.RELATIONSHIPUI));
      overviewReferenceController.load(model, otherDocumentModels, otherQueryModels);
      refreshDocumentModelIndex();
      overviewColumnsController.setModel(model);
      overviewSortingController.setModel(model);

      overviewSearchAndFiltersController.setModel(model);

      overviewMultiSelectionController.setModel(model);

      customSelectionOfFieldsController.setModel(model);

      overviewSectionDataController.setModel(model);

      customFilterConfigurationController.setModel(model);

      filterStringFieldsMultiSelectController.setModel(model);

      overviewPagingBehaviourController.setModel(model);

      overviewRowHeightActionColumnWidthController.setModel(model);

      overviewAccessibilityController.setModel(model);

      overviewStylesController.setModel(model);

      loadCustomActions();
    }
    finally {
      updatingFromModel = false;
    }
  }

  // ---- Custom Actions ----

  private void loadCustomActions() {
    rowActionButtonsController.configure(StudioBundle.get("row_action"), ".rowAction", ensureRowActionGroup().getActions(), Button::new);
    contextMenuController.setModel(model);

    rowActivationController.setModel(model);
    rowTitleController.setCustom(() -> ensureConfiguration().getRowTitle());
    subtitleController.setCustom(() -> ensureConfiguration().getSubtitle());

    ElementBox subHeaderBox = ensureSubHeaderBox();
    subheaderMajorController.configure(StudioBundle.get("major_buttons"), ".subheaderMajor", subHeaderBox.getRightSlot());
    subheaderMinorController.configure(StudioBundle.get("minor_buttons"), ".subheaderMinor", subHeaderBox.getLeftSlot());

    ElementBox footerBox = ensureFooterBox();
    footerMinorButtonsController.configure(StudioBundle.get("minor_buttons"), ".footerMinor", footerBox.getLeftSlot(), ButtonElement::new);
    footerMajorButtonsController.configure(StudioBundle.get("major_buttons"), ".footerMajor", footerBox.getRightSlot(), ButtonElement::new);
  }

  private RowActionGroup ensureRowActionGroup() {
    if (model.getContent().getRowActionGroup() == null) {
      model.getContent().setRowActionGroup(new RowActionGroup());
    }
    return model.getContent().getRowActionGroup();
  }

  private ElementBox ensureSubHeaderBox() {
    if (model.getContent().getSubHeaderBox() == null) {
      model.getContent().setSubHeaderBox(ElementBox.createEmpty());
    }
    return model.getContent().getSubHeaderBox();
  }

  private ElementBox ensureFooterBox() {
    if (model.getContent().getFooterBox() == null) {
      model.getContent().setFooterBox(ElementBox.createEmpty());
    }
    return model.getContent().getFooterBox();
  }

  private String currentDocumentModelId() {
    if (model.getModelReferences() == null) {
      return null;
    }
    String explicitDocumentModelId = model.getModelReferences().stream()
        .filter(reference -> ModelReference.PURPOSE_DOCUMENT_MODEL_FOR_OVERVIEW.equals(reference.getPurpose()))
        .map(ModelReference::getReference)
        .findFirst()
        .orElse(null);
    if (explicitDocumentModelId != null) {
      return explicitDocumentModelId;
    }
    // No direct Document Model reference: this Overview Model may instead be bound only through a Query
    // Model (query-model-for-overview), a pattern SME itself uses (see e.g. its own
    // IntegrationTestModelQmRef.json fixture) - it resolves the Document Model to use for every
    // field-reference picker from the Query Model's own target Document Model rather than requiring a
    // second, redundant header reference (see importTransformations.ts#transformModelReference).
    QueryModel queryModel = currentQueryModel();
    return queryModel != null && queryModel.getContent() != null ? queryModel.getContent().getTargetDocumentModel() : null;
  }

  private QueryModel currentQueryModel() {
    if (model.getModelReferences() == null) {
      return null;
    }
    String queryModelId = model.getModelReferences().stream()
        .filter(reference -> ModelReference.PURPOSE_QUERY_MODEL_FOR_OVERVIEW.equals(reference.getPurpose()))
        .map(ModelReference::getReference)
        .findFirst()
        .orElse(null);
    if (queryModelId == null || queryModelId.isBlank()) {
      return null;
    }
    return otherQueryModels.stream().filter(queryModel -> queryModelId.equals(queryModel.getId())).findFirst().orElse(null);
  }

  private void refreshDocumentModelIndex() {
    String documentModelId = currentDocumentModelId();
    DocumentModel documentModel = otherDocumentModels.stream()
        .filter(candidate -> documentModelId != null && documentModelId.equals(candidate.getId()))
        .findFirst()
        .orElseGet(() -> ProjectDocumentModels.resolveDocumentModelForFieldReferences(documentModelId));
    documentModelIndex = OverviewElementOptions.indexOf(documentModel, otherDocumentModels);
    OverviewElementOptions.restrictFieldIds(documentModelIndex, queryModelFieldRestriction());
    linkedDocumentModelIndexByModelId.clear();
    overviewColumnsController.setDocumentModelIndex(documentModelIndex, documentModelId, linkDocumentModelIndexResolver);
    overviewSortingController.setDocumentModelIndex(documentModelIndex, linkDocumentModelIndexResolver);
    overviewAccessibilityController.setDocumentModelIndex(documentModelIndex, linkDocumentModelIndexResolver);
    customSelectionOfFieldsController.setDocumentModelIndex(documentModelIndex, otherDocumentModels);
    overviewSectionDataController.setDocumentModelIndex(documentModelIndex);
    customFilterConfigurationController.setDocumentModelIndex(documentModelIndex);
    filterStringFieldsMultiSelectController.setDocumentModelIndex(documentModelIndex);
  }

  /**
   * Gap 14 of "Overview Model: gap review" - {@link OverviewReferencePanelController}'s "Add" button next to
   * the Query Model picker. Opens {@link Dialogs#showCreateQueryModel}, targeted at this Overview's own
   * (resolved) Document Model, creates the new Query Model there, optionally seeds its Fields/Paging/Sorting
   * from this Overview's own columns/configuration (see {@link #generateQueryFromOverview}), then selects it
   * as the new Overview Reference.
   */
  private void onAddQueryModel() {
    ProjectItem targetFolder = projectItem.getParent();
    String preselectedDocumentModelId = currentDocumentModelId();
    Optional<CreateQueryModelDialogController.Result> input = Dialogs.showCreateQueryModel(Studio.stage, targetFolder,
        otherDocumentModels, preselectedDocumentModelId, model.getLocales(), defaultQueryModelName(model.getId()));
    if (input.isEmpty()) {
      return;
    }

    try {
      ProjectItem selectedFolder = input.get().folder();
      ProjectItem newItem = NewModelFactory.createModel(selectedFolder, ModelType.QUERY, input.get().name(),
          input.get().targetDocumentModelId());
      QueryModel queryModel = (QueryModel) newItem.getModel();
      if (input.get().generateFromOverview()) {
        generateQueryFromOverview(queryModel, input.get().targetDocumentModelId());
      }
      if (!input.get().locales().isEmpty()) {
        queryModel.setLocales(input.get().locales());
      }
      if (!input.get().roles().isEmpty()) {
        RolesEditorPanelController.applyRoles(queryModel, input.get().roles());
      }
      newItem.save();
      StudioEventManager.getInstance().fireModelSavedEvent(newItem);

      otherQueryModels = ProjectDocumentModels.getOtherModelsOfType(projectItem, ModelType.QUERY).stream()
          .filter(QueryModel.class::isInstance)
          .map(QueryModel.class::cast)
          .toList();
      overviewReferenceController.selectQueryModel(queryModel, otherQueryModels);
    }
    catch (IOException e) {
      WidgetFactory.showAlert(Studio.stage, StudioBundle.get("could_not_create_item", input.get().name()), e.getMessage());
    }
  }

  /**
   * Seeds {@code queryModel}'s Fields, Paging and Sorting from this Overview Model's own columns/
   * configuration: one field per reference column's resolved absolute path (expression columns have no field
   * to project, so they're skipped), the Paging Size, and one Sort entry per Initial Sorting column (direction
   * from the column's own Preferred Sorting, defaulting to Ascending). Resolves paths against {@code
   * targetDocumentModelId} fresh (not {@link #documentModelIndex}, which may still be pointed at a different
   * Document Model if the user picked one other than this Overview's currently resolved one).
   */
  // Package-private (not private) so OverviewModelEditorControllerAddQueryModelTest can exercise it directly
  // without going through the blocking Dialogs#showCreateQueryModel it's normally reached from.
  void generateQueryFromOverview(@NonNull QueryModel queryModel, @NonNull String targetDocumentModelId) {
    DocumentModel targetDocumentModel = otherDocumentModels.stream()
        .filter(candidate -> targetDocumentModelId.equals(candidate.getId()))
        .findFirst()
        .orElseGet(() -> ProjectDocumentModels.resolveDocumentModelForFieldReferences(targetDocumentModelId));
    ElementIndex index = OverviewElementOptions.indexOf(targetDocumentModel, otherDocumentModels);
    if (index == null) {
      return;
    }

    List<Column> columns = model.getContent().getColumns();
    Set<String> fieldPaths = new LinkedHashSet<>();
    for (Column column : columns) {
      String elementRef = column.getElementRef();
      if (elementRef != null && !elementRef.isBlank() && index.isResolvable(elementRef)) {
        fieldPaths.add(index.resolveDisplayPath(elementRef));
      }
    }
    queryModel.getContent().setFields(new ArrayList<>(fieldPaths));

    Integer pagingSize = model.getContent().getConfiguration().getPagingSize();
    if (pagingSize != null) {
      QueryPaging paging = new QueryPaging();
      paging.setPageSize(pagingSize);
      queryModel.getContent().setPaging(paging);
    }

    List<QuerySort> sortEntries = new ArrayList<>();
    for (ColumnRef sortRef : model.getContent().getConfiguration().getInitialSorting()) {
      Column sortedColumn = columns.stream().filter(column -> column.getId() != null && column.getId().equals(sortRef.getIdref())).findFirst().orElse(null);
      if (sortedColumn == null || sortedColumn.getElementRef() == null || sortedColumn.getElementRef().isBlank()
          || !index.isResolvable(sortedColumn.getElementRef())) {
        continue;
      }
      QuerySort sort = new QuerySort();
      sort.getSortBy().setField(index.resolveDisplayPath(sortedColumn.getElementRef()));
      sort.getSortBy().setDirection(Column.PREFERRED_SORTING_DESC.equals(sortedColumn.getPreferredSorting())
          ? de.a12.studio.models.querymodel.QuerySortBy.DIRECTION_DESC : de.a12.studio.models.querymodel.QuerySortBy.DIRECTION_ASC);
      sortEntries.add(sort);
    }
    queryModel.getContent().setSort(sortEntries);
  }

  // Mirrors DocumentModelActions#defaultOverviewModelName's "<Base>_OM" convention, but for the Query Model
  // this Overview is about to be re-bound to ("_Qe", matching real fixtures like
  // PersonSkills_Person_Ru_SelectedItems_Qe.json for PersonSkills_Person_Ru_SelectedItems_Ov.json).
  private static String defaultQueryModelName(@NonNull String overviewModelId) {
    for (String suffix : List.of("_OM", "_Ov")) {
      if (overviewModelId.endsWith(suffix)) {
        return overviewModelId.substring(0, overviewModelId.length() - suffix.length()) + "_Qe";
      }
    }
    return overviewModelId + "_Qe";
  }

  /**
   * The {@link ElementIndex} a Column's {@code linkReferences} entry resolves its {@code elementRef}
   * against instead of {@link #documentModelIndex}, since the field lives on the related document, not the
   * primary one: the named relationship's link document (e.g. a Relationship UI Model's
   * "Proficiency"/"Acknowledged" columns - see {@code PersonSkills_Re.json}) for a {@code LINK} reference, the
   * target role's document (e.g. {@code Team_Dc} for {@code TeamPerson_Re}'s {@code Team} role) for a {@code
   * CHILD} one - see {@link ColumnLinkReference#resolveDocumentModelId}. Built lazily and cached per
   * Document Model id; the cache is dropped on every {@link #refreshDocumentModelIndex()} call so it stays in
   * sync with {@link #otherDocumentModels}/{@link #otherRelationshipModels}. {@code null} if the
   * relationship or its Document Model doesn't resolve.
   */
  private ElementIndex linkedDocumentModelIndex(ColumnLinkReference linkReference) {
    String relationshipId = linkReference == null ? null : linkReference.getRelationship();
    if (relationshipId == null) {
      return null;
    }
    RelationshipModel relationshipModel = otherRelationshipModels.stream()
        .filter(candidate -> relationshipId.equals(candidate.getId()) && candidate.getContent() != null)
        .findFirst()
        .orElse(null);
    String linkedDocumentModelId = relationshipModel == null ? null : linkReference.resolveDocumentModelId(relationshipModel.getContent());
    if (linkedDocumentModelId == null || linkedDocumentModelId.isBlank()) {
      return null;
    }
    ElementIndex cached = linkedDocumentModelIndexByModelId.get(linkedDocumentModelId);
    if (cached != null) {
      return cached;
    }
    DocumentModel linkedDocumentModel = otherDocumentModels.stream()
        .filter(candidate -> linkedDocumentModelId.equals(candidate.getId()))
        .findFirst()
        .orElseGet(() -> ProjectDocumentModels.resolveDocumentModelForFieldReferences(linkedDocumentModelId));
    ElementIndex linkedIndex = OverviewElementOptions.indexOf(linkedDocumentModel, otherDocumentModels);
    if (linkedIndex != null) {
      linkedDocumentModelIndexByModelId.put(linkedDocumentModelId, linkedIndex);
    }
    return linkedIndex;
  }

  /**
   * When this Overview Model is bound through a Query Model (a {@link
   * ModelReference#PURPOSE_QUERY_MODEL_FOR_OVERVIEW} header reference is present), every field-reference
   * picker should only offer fields the Query Model actually projects ({@code QueryModelContent.fields}),
   * mirroring SME's {@code getExtendedGetDmCandidates}. {@code null} - meaning "no restriction" - both in
   * Document-Model mode and when the referenced Query Model can't be resolved or hasn't projected any
   * fields yet, so a not-yet-configured Query Model doesn't lock every picker to zero options. {@code
   * QueryModelContent.fields} are absolute "/"-separated paths (e.g. {@code "/Person/FirstName"}), not
   * element ids, so each is resolved against {@link #documentModelIndex} via {@link
   * ElementIndex#resolveAbsolutePath} first - a path that doesn't resolve (yet) is dropped rather than
   * passed through verbatim, since {@link OverviewElementOptions#elementIds} compares against {@code
   * Element#getId()}.
   */
  private Set<String> queryModelFieldRestriction() {
    QueryModel queryModel = currentQueryModel();
    if (queryModel == null || queryModel.getContent() == null || documentModelIndex == null) {
      return null;
    }
    List<String> fieldPaths = queryModel.getContent().getFields();
    if (fieldPaths == null || fieldPaths.isEmpty()) {
      return null;
    }
    Set<String> ids = fieldPaths.stream()
        .map(documentModelIndex::resolveAbsolutePath)
        .filter(Optional::isPresent)
        .map(Optional::get)
        .map(Element::getId)
        .filter(Objects::nonNull)
        .collect(Collectors.toSet());
    return ids.isEmpty() ? null : ids;
  }

  /**
   * Re-derives {@link #otherDocumentModels} and {@link #documentModelIndex} whenever a Document Model is
   * saved in a different tab, so a Field this Overview Model's Columns/Sorting/Accessibility/Custom Selection
   * Of Fields/Section Data/Custom Filter Configuration panels reference immediately reflects being
   * added/renamed/removed, instead of only after this tab is closed and reopened.
   */
  @Override
  protected void onDocumentModelChangedElsewhere() {
    otherDocumentModels = ProjectDocumentModels.getOtherDocumentModelsWithGenerated(projectItem);
    refreshDocumentModelIndex();
  }

  // ---- Shared helpers ----

  private OverviewConfiguration ensureConfiguration() {
    if (model.getContent().getConfiguration() == null) {
      model.getContent().setConfiguration(new OverviewConfiguration());
    }
    return model.getContent().getConfiguration();
  }

  private void commitChange() {
    projectItem.save();
    StudioEventManager.getInstance().fireModelSavedEvent(projectItem);
  }

  @Override
  public @NonNull ModelType getModelType() {
    return ModelType.OVERVIEW;
  }
}
