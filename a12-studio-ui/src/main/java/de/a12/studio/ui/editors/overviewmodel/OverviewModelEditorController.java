package de.a12.studio.ui.editors.overviewmodel;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.ModelReference;
import de.a12.studio.models.ModelType;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.overviewmodel.BoxElement;
import de.a12.studio.models.overviewmodel.Button;
import de.a12.studio.models.overviewmodel.ButtonElement;
import de.a12.studio.models.overviewmodel.ElementBox;
import de.a12.studio.models.overviewmodel.FilterConfiguration;
import de.a12.studio.models.overviewmodel.OverviewConfiguration;
import de.a12.studio.models.overviewmodel.OverviewModel;
import de.a12.studio.models.overviewmodel.RowActionGroup;
import de.a12.studio.models.querymodel.QueryModel;
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
import java.util.List;
import java.util.ResourceBundle;

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
 * rowTitle}, delegated to {@link de.a12.studio.ui.editors.propertyeditors.LocalizedTextPanelController}), Subheader ({@code
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
  private ElementIndex documentModelIndex;

  @Override
  public void initialize(URL url, ResourceBundle resources) {
    initializeGeneralSettings();
    rowTitleController.configureCustom("rowTitle", StudioBundle.get("title_for_interactive_rows"));
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
      List<QueryModel> otherQueryModels = ProjectDocumentModels.getOtherModelsOfType(projectItem, ModelType.QUERY).stream()
          .filter(QueryModel.class::isInstance)
          .map(QueryModel.class::cast)
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
    customSelectionOfFieldsController.setDocumentModelIndex(documentModelIndex);
    overviewSectionDataController.setDocumentModelIndex(documentModelIndex);
    customFilterConfigurationController.setDocumentModelIndex(documentModelIndex);
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
