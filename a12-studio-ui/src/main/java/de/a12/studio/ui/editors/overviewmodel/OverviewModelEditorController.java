package de.a12.studio.ui.editors.overviewmodel;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.ModelReference;
import de.a12.studio.models.ModelType;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.documentmodel.Element;
import de.a12.studio.models.overviewmodel.BoxElement;
import de.a12.studio.models.overviewmodel.Button;
import de.a12.studio.models.overviewmodel.ButtonElement;
import de.a12.studio.models.overviewmodel.ElementBox;
import de.a12.studio.models.overviewmodel.FilterConfiguration;
import de.a12.studio.models.overviewmodel.OverviewConfiguration;
import de.a12.studio.models.overviewmodel.OverviewModel;
import de.a12.studio.models.overviewmodel.RowActionGroup;
import de.a12.studio.models.querymodel.QueryModel;
import de.a12.studio.models.relationshipmodel.RelationshipModel;
import de.a12.studio.modelsvalidation.validators.ElementIndex;
import de.a12.studio.ui.editors.AbstractEditorController;
import de.a12.studio.ui.editors.propertyeditors.EventButtonsPanelController;
import de.a12.studio.ui.editors.propertyeditors.LocalizedTextPanelController;
import de.a12.studio.ui.events.StudioEventManager;
import de.a12.studio.ui.util.ProjectDocumentModels;
import de.a12.studio.ui.util.StudioBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import org.jspecify.annotations.NonNull;

import java.net.URL;
import java.util.HashMap;
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
  private ElementIndex documentModelIndex;
  private final Map<String, ElementIndex> linkDocumentModelIndexByRelationshipId = new HashMap<>();
  private final Function<String, ElementIndex> linkDocumentModelIndexResolver = linkDocumentModelIndexByRelationshipId::get;

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
    // The Sorting panel's column picker and its own dangling-reference validation, as well as the
    // Accessibility panel's screen-reader column picker, both derive from the Columns list, so keep them in
    // sync with every structural change made there.
    overviewColumnsController.setOnChange(() -> {
      overviewSortingController.refresh();
      overviewAccessibilityController.refresh();
    });
    overviewSearchAndFiltersController.setOnRelevanceChange(this::updateFilterModeDependentVisibility);
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
      otherQueryModels = ProjectDocumentModels.getOtherModelsOfType(projectItem, ModelType.QUERY).stream()
          .filter(QueryModel.class::isInstance)
          .map(QueryModel.class::cast)
          .toList();
      otherRelationshipModels = ProjectDocumentModels.getOtherModelsOfType(projectItem, ModelType.RELATIONSHIP).stream()
          .filter(RelationshipModel.class::isInstance)
          .map(RelationshipModel.class::cast)
          .toList();
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
      model.getContent().setSubHeaderBox(new ElementBox());
    }
    return model.getContent().getSubHeaderBox();
  }

  private ElementBox ensureFooterBox() {
    if (model.getContent().getFooterBox() == null) {
      model.getContent().setFooterBox(new ElementBox());
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
    refreshLinkDocumentModelIndexes();
    overviewColumnsController.setDocumentModelIndex(documentModelIndex, documentModelId, linkDocumentModelIndexResolver);
    overviewSortingController.setDocumentModelIndex(documentModelIndex, linkDocumentModelIndexResolver);
    overviewAccessibilityController.setDocumentModelIndex(documentModelIndex, linkDocumentModelIndexResolver);
    customSelectionOfFieldsController.setDocumentModelIndex(documentModelIndex);
    overviewSectionDataController.setDocumentModelIndex(documentModelIndex);
    customFilterConfigurationController.setDocumentModelIndex(documentModelIndex);
  }

  /**
   * Rebuilds {@link #linkDocumentModelIndexByRelationshipId}: for every Relationship Model in the project
   * that declares a {@code linkDocumentModel} (the fields attached to the relationship's own link, e.g. a
   * Relationship UI Model's "Proficiency"/"Acknowledged" columns - see {@code PersonSkills_Re.json}), an
   * {@link ElementIndex} over that model, keyed by the relationship's id. A Column whose {@code
   * linkReferences} names that relationship resolves its {@code elementRef} against this index instead of
   * {@link #documentModelIndex}, since the field lives on the link document, not the primary one (see {@link
   * OverviewColumnOptions#indexFor}). Rebuilt on every {@link #refreshDocumentModelIndex()} call so it stays
   * in sync with {@link #otherDocumentModels}/{@link #otherRelationshipModels}.
   */
  private void refreshLinkDocumentModelIndexes() {
    linkDocumentModelIndexByRelationshipId.clear();
    for (RelationshipModel relationshipModel : otherRelationshipModels) {
      if (relationshipModel.getContent() == null || relationshipModel.getId() == null) {
        continue;
      }
      String linkDocumentModelId = relationshipModel.getContent().getLinkDocumentModelValue();
      if (linkDocumentModelId == null || linkDocumentModelId.isBlank()) {
        continue;
      }
      DocumentModel linkDocumentModel = otherDocumentModels.stream()
          .filter(candidate -> linkDocumentModelId.equals(candidate.getId()))
          .findFirst()
          .orElseGet(() -> ProjectDocumentModels.resolveDocumentModelForFieldReferences(linkDocumentModelId));
      ElementIndex linkIndex = OverviewElementOptions.indexOf(linkDocumentModel, otherDocumentModels);
      if (linkIndex != null) {
        linkDocumentModelIndexByRelationshipId.put(relationshipModel.getId(), linkIndex);
      }
    }
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
    otherDocumentModels = ProjectDocumentModels.getOtherDocumentModels(projectItem);
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
