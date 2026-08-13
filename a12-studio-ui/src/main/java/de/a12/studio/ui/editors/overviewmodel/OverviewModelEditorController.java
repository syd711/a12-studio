package de.a12.studio.ui.editors.overviewmodel;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.Label;
import de.a12.studio.models.Locale;
import de.a12.studio.models.ModelReference;
import de.a12.studio.models.ModelType;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.overviewmodel.BoxElement;
import de.a12.studio.models.overviewmodel.Button;
import de.a12.studio.models.overviewmodel.ButtonElement;
import de.a12.studio.models.overviewmodel.ElementBox;
import de.a12.studio.models.overviewmodel.OverviewConfiguration;
import de.a12.studio.models.overviewmodel.OverviewModel;
import de.a12.studio.models.overviewmodel.RowAction;
import de.a12.studio.models.overviewmodel.RowActionGroup;
import de.a12.studio.models.querymodel.QueryModel;
import de.a12.studio.modelsvalidation.validators.ElementIndex;
import de.a12.studio.ui.editors.AbstractEditorController;
import de.a12.studio.ui.editors.propertyeditors.EventButtonsPanelController;
import de.a12.studio.ui.events.StudioEventManager;
import de.a12.studio.ui.util.ProjectDocumentModels;
import de.a12.studio.ui.util.WidgetFactory;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.jspecify.annotations.NonNull;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.BiConsumer;

/**
 * Edits an {@link OverviewModel}'s "Overview" and "Custom Actions" tabs.
 * <p>
 * "Overview": General Settings (the Overview Reference, delegated to {@link OverviewReferencePanelController}),
 * Columns (delegated to {@link OverviewColumnsPanelController}), Search and Filters (search/filter/row-count,
 * delegated to {@link OverviewSearchAndFiltersPanelController}), Multi-Selection (delegated to {@link
 * OverviewMultiSelectionPanelController}), Custom Selection Of Fields (delegated to {@link
 * CustomSelectionOfFieldsPanelController}), Section Data (delegated to {@link
 * OverviewSectionDataPanelController}), Filter String Fields with Multi-Select (delegated to {@link
 * FilterStringFieldsMultiSelectPanelController}), Row Height And Action Column Width (delegated to {@link
 * RowHeightActionColumnWidthPanelController}), Paging Behaviour (delegated to {@link
 * PagingBehaviourPanelController}), Accessibility (delegated to {@link OverviewAccessibilityPanelController})
 * and Styles (delegated to {@link StylesPanelController}).
 * <p>
 * "Custom Actions": Row Action Group ({@code content.rowActionGroup.actions}, delegated to {@link
 * de.a12.studio.ui.editors.propertyeditors.EventButtonsPanelController}, and {@code content.contextMenu},
 * delegated to {@link ContextMenuPanelController}), Row Activation ({@code content.defaultRowAction} and Title
 * For Interactive Rows / {@code content.configuration.rowTitle}, both inline here), Subheader ({@code
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
  private ComboBox<String> rowActivationTypeField;
  @FXML
  private javafx.scene.control.Label rowActivationInfoIcon;
  @FXML
  private VBox rowActivationEventBox;
  @FXML
  private TextField rowActivationEventField;
  @FXML
  private GridPane interactiveRowTitleGrid;

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

  private static final String ROW_ACTIVATION_DEFAULT = "Default Engine Behavior";
  private static final String ROW_ACTIVATION_EVENT = "Event";
  private static final String ROW_ACTIVATION_NON_INTERACTIVE = "Non Interactive";

  private OverviewModel model;
  private List<DocumentModel> otherDocumentModels = List.of();
  private ElementIndex documentModelIndex;

  @Override
  public void initialize(URL url, ResourceBundle resources) {
    initializeGeneralSettings();
    initializeRowActivation();
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
    rowActionButtonsController.configure("ROW ACTION", ".rowAction", ensureRowActionGroup().getActions(), Button::new);
    contextMenuController.setModel(model);

    populateRowActivation();

    ElementBox subHeaderBox = ensureSubHeaderBox();
    subheaderMajorController.configure("MAJOR", ".subheaderMajor", subHeaderBox.getRightSlot());
    subheaderMinorController.configure("MINOR", ".subheaderMinor", subHeaderBox.getLeftSlot());

    ElementBox footerBox = ensureFooterBox();
    footerMinorButtonsController.configure("MINOR BUTTONS", ".footerMinor", footerBox.getLeftSlot(), ButtonElement::new);
    footerMajorButtonsController.configure("MAJOR BUTTONS", ".footerMajor", footerBox.getRightSlot(), ButtonElement::new);
  }

  private void initializeRowActivation() {
    WidgetFactory.createHelpIcon(rowActivationInfoIcon,
        "Default Engine Behavior: the Overview Engine's built-in row-click behavior applies. Event: clicking a row "
            + "triggers the given event. Non Interactive: rows are explicitly not clickable.");

    rowActivationTypeField.getItems().setAll(ROW_ACTIVATION_DEFAULT, ROW_ACTIVATION_EVENT, ROW_ACTIVATION_NON_INTERACTIVE);
    rowActivationTypeField.valueProperty().addListener((observable, oldValue, newValue) -> {
      if (updatingFromModel || model == null) {
        return;
      }
      applyRowActivationType(newValue);
      refreshRowActivationEventVisibility(newValue);
      commitChange();
    });

    rowActivationEventField.textProperty().addListener((observable, oldValue, newValue) -> {
      if (updatingFromModel || model == null) {
        return;
      }
      RowAction rowAction = model.getContent().getDefaultRowAction();
      if (rowAction == null) {
        return;
      }
      rowAction.setEvent(newValue == null || newValue.isBlank() ? null : newValue);
      commitChange();
    });
  }

  private void populateRowActivation() {
    RowAction rowAction = model.getContent().getDefaultRowAction();
    String type;
    if (rowAction == null || !Boolean.TRUE.equals(rowAction.getCustom())) {
      type = ROW_ACTIVATION_DEFAULT;
    }
    else if (rowAction.getEvent() != null && !rowAction.getEvent().isBlank()) {
      type = ROW_ACTIVATION_EVENT;
    }
    else {
      type = ROW_ACTIVATION_NON_INTERACTIVE;
    }
    rowActivationTypeField.setValue(type);
    rowActivationEventField.setText(rowAction != null && rowAction.getEvent() != null ? rowAction.getEvent() : "");
    refreshRowActivationEventVisibility(type);

    rebuildLocaleGrid(interactiveRowTitleGrid, ensureConfiguration().getRowTitle(),
        (code, text) -> setLabelText(ensureConfiguration().getRowTitle(), code, text));
  }

  private void applyRowActivationType(String type) {
    if (ROW_ACTIVATION_DEFAULT.equals(type)) {
      model.getContent().setDefaultRowAction(null);
      return;
    }
    RowAction rowAction = model.getContent().getDefaultRowAction();
    if (rowAction == null) {
      rowAction = new RowAction();
      model.getContent().setDefaultRowAction(rowAction);
    }
    rowAction.setCustom(true);
    if (ROW_ACTIVATION_EVENT.equals(type)) {
      rowAction.setEvent(rowActivationEventField.getText());
    }
    else {
      rowAction.setEvent(null);
    }
  }

  private void refreshRowActivationEventVisibility(String type) {
    boolean showEvent = ROW_ACTIVATION_EVENT.equals(type);
    rowActivationEventBox.setVisible(showEvent);
    rowActivationEventBox.setManaged(showEvent);
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
  }

  // ---- Shared helpers ----

  private OverviewConfiguration ensureConfiguration() {
    if (model.getContent().getConfiguration() == null) {
      model.getContent().setConfiguration(new OverviewConfiguration());
    }
    return model.getContent().getConfiguration();
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

  private void commitChange() {
    projectItem.save();
    StudioEventManager.getInstance().fireModelSavedEvent(projectItem);
  }

  @Override
  public @NonNull ModelType getModelType() {
    return ModelType.OVERVIEW;
  }
}
