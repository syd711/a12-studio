package de.a12.studio.ui.editors.formmodel;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.ModelReference;
import de.a12.studio.models.ModelType;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.formmodel.Button;
import de.a12.studio.models.formmodel.ButtonGroup;
import de.a12.studio.models.formmodel.Defaults;
import de.a12.studio.models.formmodel.FormModel;
import de.a12.studio.models.formmodel.FormModelContent;
import de.a12.studio.models.formmodel.HeaderFooterBox;
import de.a12.studio.models.formmodel.Screen;
import de.a12.studio.modelsvalidation.validators.ElementIndex;
import de.a12.studio.modelsvalidation.validators.form.FormConfigEntryCleanup;
import de.a12.studio.modelsvalidation.validators.form.FormFieldReferenceValidator;
import de.a12.studio.modelsvalidation.validators.form.FormGroupReferenceValidator;
import de.a12.studio.modelsvalidation.validators.form.FormUnusedConfigEntryValidator;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.editors.AbstractEditorController;
import de.a12.studio.ui.editors.formmodel.dialogs.Dialogs;
import de.a12.studio.ui.editors.formmodel.documenttree.DocumentSourceTreeController;
import de.a12.studio.ui.editors.formmodel.formtree.FormModelTreeController;
import de.a12.studio.ui.editors.propertyeditors.LocalizedTextPanelController;
import de.a12.studio.ui.editors.propertyeditors.ToolbarButtonsPanelController;
import de.a12.studio.ui.events.ModelSaveEvent;
import de.a12.studio.ui.events.StudioEventManager;
import de.a12.studio.ui.events.TabSelectionChangedEvent;
import de.a12.studio.ui.preview.PreviewLauncher;
import de.a12.studio.ui.projecttree.ProjectItemViewModel;
import de.a12.studio.ui.util.Icons;
import de.a12.studio.ui.util.ProjectDocumentModels;
import de.a12.studio.ui.util.StudioBundle;
import de.a12.studio.ui.util.WidgetFactory;
import de.a12.studio.ui.util.localsettings.BaseTableSettings;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TitledPane;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.kordamp.ikonli.javafx.FontIcon;

import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Edits an {@link FormModel}'s "Overview", "Repeat Default Button Labels" and "Subheader and Footer" tabs.
 * <p>
 * "Repeat Default Button Labels": {@code content.defaults.buttonLabels} - the model-wide overrides for the
 * built-in repeat-widget button labels (ADD/CANCEL/COMMIT_ADD/...), one {@link LocalizedTextPanelController}
 * per action, matching the SME reference's {@code I_SectionDefaultRepeatButtonLabels-form.json} order.
 * <p>
 * "Subheader and Footer": {@code content.subHeaderBox}/{@code content.footerBox}'s Major/Minor button lists,
 * one {@link ToolbarButtonsPanelController} per list, matching the SME reference's "Major Buttons"/"Minor
 * Buttons" tables. Form Model's subHeaderBox and footerBox are both button-only ({@link HeaderFooterBox}), so
 * all four lists reuse the same simple panel; rows can be dragged between them to move a button to a different
 * section. Rows can be either {@link de.a12.studio.models.formmodel.EventButton} or {@link
 * de.a12.studio.models.formmodel.NavigationButton} (see Company_FM.json, where subHeaderBox holds navigation
 * buttons and footerBox holds event buttons). Each panel's Add/Edit actions open {@link Dialogs#showButtonForAdd}/
 * {@link Dialogs#showButtonForEdit}; Add defaults to a new Event button (the more common case) and lets the
 * user switch it to Navigation.
 */
public class FormModelEditorController extends AbstractEditorController implements Initializable {

  @FXML
  private LocalizedTextPanelController addLabelController;
  @FXML
  private LocalizedTextPanelController commitAddLabelController;
  @FXML
  private LocalizedTextPanelController applyLabelController;
  @FXML
  private LocalizedTextPanelController editLabelController;
  @FXML
  private LocalizedTextPanelController removeLabelController;
  @FXML
  private LocalizedTextPanelController viewLabelController;
  @FXML
  private LocalizedTextPanelController cancelLabelController;
  @FXML
  private LocalizedTextPanelController confirmLabelController;
  @FXML
  private LocalizedTextPanelController returnLabelController;
  @FXML
  private LocalizedTextPanelController upLabelController;
  @FXML
  private LocalizedTextPanelController downLabelController;
  @FXML
  private LocalizedTextPanelController copyLabelController;
  @FXML
  private LocalizedTextPanelController closeLabelController;
  @FXML
  private LocalizedTextPanelController downloadLabelController;
  @FXML
  private LocalizedTextPanelController skipLabelController;
  @FXML
  private LocalizedTextPanelController replaceLabelController;
  @FXML
  private LocalizedTextPanelController uploadAsCopyLabelController;

  @FXML
  private ToolbarButtonsPanelController subheaderMajorButtonsController;
  @FXML
  private ToolbarButtonsPanelController subheaderMinorButtonsController;
  @FXML
  private ToolbarButtonsPanelController footerMajorButtonsController;
  @FXML
  private ToolbarButtonsPanelController footerMinorButtonsController;

  @FXML
  private DocumentSourceTreeController documentSourceTreeController;
  @FXML
  private RelationshipModelPanelController relationshipModelPanelController;
  @FXML
  private FormModelTreeController formModelTreeController;
  @FXML
  private DataConfigurationPanelController dataConfigurationController;

  @FXML
  private TitledPane documentModelPane;
  @FXML
  private SplitPane overviewSplitPane;
  @FXML
  private StackPane documentRelationshipModelPane;
  @FXML
  private Node documentRelationshipModelExpandedPane;
  @FXML
  private Node documentRelationshipModelCollapsedStrip;
  @FXML
  private FontIcon pinDocumentRelationshipModelIcon;
  @FXML
  private Tooltip pinDocumentRelationshipModelTooltip;

  private static final double DOCUMENT_RELATIONSHIP_MODEL_DIVIDER_POSITION = 0.32;
  private static final Duration DOCUMENT_RELATIONSHIP_MODEL_TOGGLE_DURATION = Duration.millis(200);

  private static final String OVERVIEW_DIVIDER_ID = "overviewDivider";
  private static final String DOCUMENT_RELATIONSHIP_MODEL_PINNED_FLAG = "documentRelationshipModelPinned";
  private static final String DOCUMENT_RELATIONSHIP_MODEL_COLLAPSED_FLAG = "documentRelationshipModelCollapsed";

  private boolean documentRelationshipModelPinned = false;
  private boolean documentRelationshipModelCollapsed = false;
  private @Nullable Timeline documentRelationshipModelToggleTimeline;

  // Resolved once in loadOverview() (the data-binding Document Model's field tree), reused by the subheader/
  // footer button dialogs' label field for field-name autocomplete - see newButtonViaDialog/editButtonViaDialog.
  private @Nullable ElementIndex dataBindingElementIndex;

  public void loadModel(@NonNull A12Model<?> model) {
    load((FormModel) model);
    updateSettingsErrorBadge();
  }

  @FXML
  public void onPreview(ActionEvent e) {
    PreviewLauncher.openFormPreview(projectItem);
  }

  private void load(@NonNull FormModel formModel) {
    loadOverview(formModel);
    loadRepeatDefaultButtonLabels(formModel);
    loadSubheaderAndFooter(formModel);
  }

  // ---- Overview ----

  /**
   * Wires the Overview tab's split view: the left Document Model tree shows whatever Document Model is linked
   * via the header's {@link ModelReference#PURPOSE_DATA_BINDING} reference (see {@code
   * GeneralSettingsPanelController#currentDocumentModelId}, the same lookup this mirrors), and the right Form
   * Model tree edits {@code formModel}'s own {@code screens} structure, using that same Document Model to
   * resolve fields/groups dropped in from the left.
   */
  private void loadOverview(@NonNull FormModel formModel) {
    DocumentModel documentModel = resolveDataBindingDocumentModel(formModel);
    // Title the pane after the linked model's id (the referenced one, not documentModel.getId(), which is
    // synthetic for a Combination Model); the bundled "Document Model" text stays as the fallback.
    String linkedDocumentModelId = currentDocumentModelId(formModel);
    documentModelPane.setText(linkedDocumentModelId != null && !linkedDocumentModelId.isBlank()
        ? linkedDocumentModelId
        : StudioBundle.get("document_model"));
    documentModelPane.setGraphic(WidgetFactory.createModelIcon(documentModelIconPath(linkedDocumentModelId)));
    documentSourceTreeController.load(documentModel, projectItem);
    relationshipModelPanelController.load(documentModel, projectItem);
    formModelTreeController.setModel(formModel, documentModel, projectItem);
    formModelTreeController.setOnNodeSelected(this::onFormModelTreeNodeSelected);
    ElementIndex elementIndex = resolveElementIndex(documentModel);
    dataBindingElementIndex = elementIndex;
    dataConfigurationController.setModel(formModel.getContent(), elementIndex);
    cleanupDanglingAndUnusedConfigEntries(formModel, elementIndex);
  }

  /**
   * Auto-removes any dangling ({@link FormFieldReferenceValidator}/{@link FormGroupReferenceValidator}) or
   * unused ({@link FormUnusedConfigEntryValidator}) field/group configuration entries - replacing the old
   * manual "Cleanup" tab: those two validators keep flagging the problem on the project tree node the moment it
   * appears (e.g. right after a Document Model field is deleted elsewhere), but by the time this editor is
   * opened or its tab is reselected (see {@link #tabSelectionChanged}) there's nothing left for the user to fix
   * by hand - this just does it and tells them via a one-time notification.
   */
  private void cleanupDanglingAndUnusedConfigEntries(@NonNull FormModel formModel, @Nullable ElementIndex elementIndex) {
    List<ElementIndex> indexes = elementIndex == null ? List.of() : List.of(elementIndex);
    int removed = FormConfigEntryCleanup.removeDanglingAndUnusedEntries(formModel, indexes);
    if (removed == 0) {
      return;
    }
    projectItem.save();
    StudioEventManager.getInstance().fireModelSavedEvent(projectItem);
    WidgetFactory.showAlert(Studio.stage, StudioBundle.get("form_model_cleanup_notification"));
  }

  /** Re-runs the cleanup above whenever this editor's tab is (re)selected, not just when it's first opened. */
  @Override
  public void tabSelectionChanged(@NonNull TabSelectionChangedEvent event) {
    if (projectItem != null && projectItem.equals(event.getItem()) && projectItem.getModel() instanceof FormModel formModel) {
      cleanupDanglingAndUnusedConfigEntries(formModel, dataBindingElementIndex);
    }
  }

  /** Mirrors {@link FormModelTreeController}'s own element index construction. */
  private @Nullable ElementIndex resolveElementIndex(@Nullable DocumentModel documentModel) {
    if (documentModel == null || documentModel.getContent() == null || documentModel.getContent().getModelRoot() == null) {
      return null;
    }
    return new ElementIndex(documentModel, ProjectDocumentModels.getOtherDocumentModelsWithCombinations(projectItem));
  }

  /**
   * Auto-collapses the "Document/Relationship Model" side panel whenever a node is selected in the Form Model
   * tree, unless the panel is currently pinned via {@link #onTogglePinDocumentRelationshipModel}. No-op while
   * already collapsed.
   */
  private void onFormModelTreeNodeSelected() {
    if (!documentRelationshipModelPinned && !documentRelationshipModelCollapsed) {
      setDocumentRelationshipModelCollapsed(true);
    }
  }

  private @Nullable DocumentModel resolveDataBindingDocumentModel(@NonNull FormModel formModel) {
    String documentModelId = currentDocumentModelId(formModel);
    if (documentModelId == null) {
      return null;
    }
    // The reference may name a Combination Model (e.g. PersonEmployee_Cm), which a plain Document Model lookup
    // skips - resolve it to the synthetic merged Document Model whose ids match the form's elementRefs.
    return ProjectDocumentModels.resolveDocumentModelForFieldReferences(documentModelId);
  }

  /**
   * The project tree's icon for the model behind {@code documentModelId} (plain Document Model, Additive
   * Document Model, or Combination Model - the same choice {@link ProjectItemViewModel#getIconPath} makes), so
   * the pane title matches how the linked model looks in the tree. Falls back to the plain Document Model icon
   * while nothing is linked or the reference doesn't resolve.
   */
  private static @NonNull String documentModelIconPath(@Nullable String documentModelId) {
    if (documentModelId == null || documentModelId.isBlank()) {
      return Icons.PNG_MODEL_DOCUMENT;
    }
    String iconPath = ProjectDocumentModels.findProjectItemByModelId(documentModelId)
        .map(item -> new ProjectItemViewModel(item, Map.of()).getIconPath())
        .orElse(null);
    return iconPath != null ? iconPath : Icons.PNG_MODEL_DOCUMENT;
  }

  private @Nullable String currentDocumentModelId(@NonNull FormModel formModel) {
    if (formModel.getModelReferences() == null) {
      return null;
    }
    return formModel.getModelReferences().stream()
        .filter(FormModelEditorController::isDataBindingReference)
        .map(ModelReference::getReference)
        .findFirst()
        .orElse(null);
  }

  private static boolean isDataBindingReference(@NonNull ModelReference reference) {
    return reference.getModelType() == ModelType.DOCUMENT && ModelReference.PURPOSE_DATA_BINDING.equals(reference.getPurpose());
  }

  // ---- Repeat Default Button Labels ----

  private void loadRepeatDefaultButtonLabels(@NonNull FormModel model) {
    Defaults defaults = ensureDefaults(model);

    addLabelController.configureButtonLabel("ADD", StudioBundle.get("add"));
    addLabelController.setDefaults(defaults);
    commitAddLabelController.configureButtonLabel("COMMIT_ADD", StudioBundle.get("commit_add"));
    commitAddLabelController.setDefaults(defaults);
    applyLabelController.configureButtonLabel("APPLY", StudioBundle.get("apply"));
    applyLabelController.setDefaults(defaults);
    editLabelController.configureButtonLabel("EDIT", StudioBundle.get("edit_label"));
    editLabelController.setDefaults(defaults);
    removeLabelController.configureButtonLabel("REMOVE", StudioBundle.get("remove"));
    removeLabelController.setDefaults(defaults);
    viewLabelController.configureButtonLabel("VIEW", StudioBundle.get("view"));
    viewLabelController.setDefaults(defaults);
    cancelLabelController.configureButtonLabel("CANCEL", StudioBundle.get("cancel"));
    cancelLabelController.setDefaults(defaults);
    confirmLabelController.configureButtonLabel("CONFIRM", StudioBundle.get("confirm"));
    confirmLabelController.setDefaults(defaults);
    returnLabelController.configureButtonLabel("RETURN", StudioBundle.get("return_label"));
    returnLabelController.setDefaults(defaults);
    upLabelController.configureButtonLabel("UP", StudioBundle.get("up"));
    upLabelController.setDefaults(defaults);
    downLabelController.configureButtonLabel("DOWN", StudioBundle.get("down"));
    downLabelController.setDefaults(defaults);
    copyLabelController.configureButtonLabel("COPY", StudioBundle.get("copy"));
    copyLabelController.setDefaults(defaults);
    closeLabelController.configureButtonLabel("CLOSE", StudioBundle.get("close"));
    closeLabelController.setDefaults(defaults);
    downloadLabelController.configureButtonLabel("DOWNLOAD", StudioBundle.get("download"));
    downloadLabelController.setDefaults(defaults);
    skipLabelController.configureButtonLabel("SKIP", StudioBundle.get("skip"));
    skipLabelController.setDefaults(defaults);
    replaceLabelController.configureButtonLabel("REPLACE", StudioBundle.get("replace"));
    replaceLabelController.setDefaults(defaults);
    uploadAsCopyLabelController.configureButtonLabel("UPLOAD_AS_COPY", StudioBundle.get("upload_as_copy"));
    uploadAsCopyLabelController.setDefaults(defaults);
  }

  private static Defaults ensureDefaults(FormModel model) {
    Defaults defaults = model.getContent().getDefaults();
    if (defaults == null) {
      defaults = new Defaults();
      model.getContent().setDefaults(defaults);
    }
    return defaults;
  }

  // ---- Subheader and Footer ----

  private void loadSubheaderAndFooter(@NonNull FormModel model) {
    FormModelContent content = model.getContent();
    HeaderFooterBox subHeaderBox = ensureBox(content.getSubHeaderBox(), "subHeaderBox1", content::setSubHeaderBox);
    HeaderFooterBox footerBox = ensureBox(content.getFooterBox(), "footerBox1", content::setFooterBox);
    List<String> screenIds = content.getScreens().stream().map(Screen::getId).collect(Collectors.toList());

    subheaderMajorButtonsController.configure(StudioBundle.get("subheader_major_buttons"), ".subheaderMajor",
        ensureMajorButtons(subHeaderBox).getButton(), () -> newButtonViaDialog(screenIds), button -> editButtonViaDialog(screenIds, button),
        Dialogs::generateButtonId);
    subheaderMinorButtonsController.configure(StudioBundle.get("subheader_minor_buttons"), ".subheaderMinor",
        ensureMinorButtons(subHeaderBox).getButton(), () -> newButtonViaDialog(screenIds), button -> editButtonViaDialog(screenIds, button),
        Dialogs::generateButtonId);
    footerMajorButtonsController.configure(StudioBundle.get("footer_major_buttons"), ".footerMajor",
        ensureMajorButtons(footerBox).getButton(), () -> newButtonViaDialog(screenIds), button -> editButtonViaDialog(screenIds, button),
        Dialogs::generateButtonId);
    footerMinorButtonsController.configure(StudioBundle.get("footer_minor_buttons"), ".footerMinor",
        ensureMinorButtons(footerBox).getButton(), () -> newButtonViaDialog(screenIds), button -> editButtonViaDialog(screenIds, button),
        Dialogs::generateButtonId);
  }

  private static HeaderFooterBox ensureBox(HeaderFooterBox box, String id, Consumer<HeaderFooterBox> setter) {
    if (box != null) {
      return box;
    }
    HeaderFooterBox newBox = new HeaderFooterBox();
    newBox.setId(id);
    setter.accept(newBox);
    return newBox;
  }

  private static ButtonGroup ensureMajorButtons(HeaderFooterBox box) {
    if (box.getMajorButtons() == null) {
      box.setMajorButtons(new ButtonGroup());
    }
    return box.getMajorButtons();
  }

  private static ButtonGroup ensureMinorButtons(HeaderFooterBox box) {
    if (box.getMinorButtons() == null) {
      box.setMinorButtons(new ButtonGroup());
    }
    return box.getMinorButtons();
  }

  private Optional<Button> newButtonViaDialog(List<String> screenIds) {
    return Dialogs.showButtonForAdd(Studio.stage, dataBindingElementIndex, screenIds);
  }

  private Optional<Button> editButtonViaDialog(List<String> screenIds, Button button) {
    return Dialogs.showButtonForEdit(Studio.stage, dataBindingElementIndex, screenIds, button);
  }

  /**
   * Toggles the "Document/Relationship Model" side panel between its full-width expanded view and
   * a narrow vertical strip carrying a rotated title (a mobile-style burger-menu sidebar), showing
   * only whichever of the two is current and clamping {@code documentRelationshipModelPane}'s
   * maxWidth to that node's own preferred width so the SplitPane hands the freed-up width to the
   * sibling form model tree, moving the divider along with it. Re-expanding restores maxWidth and
   * the divider's original position. The transition is animated (200ms width + cross-fade); {@link
   * #restoreDocumentRelationshipModelState()} calls the non-animated overload so restoring saved
   * state on editor load doesn't flash.
   */
  @FXML
  private void onToggleDocumentRelationshipModelCollapsed() {
    setDocumentRelationshipModelCollapsed(documentRelationshipModelExpandedPane.isVisible());
  }

  private void setDocumentRelationshipModelCollapsed(boolean collapsing) {
    setDocumentRelationshipModelCollapsed(collapsing, true);
  }

  private void setDocumentRelationshipModelCollapsed(boolean collapsing, boolean animate) {
    documentRelationshipModelCollapsed = collapsing;
    if (documentRelationshipModelToggleTimeline != null) {
      documentRelationshipModelToggleTimeline.stop();
      documentRelationshipModelToggleTimeline = null;
    }

    double totalWidth = overviewSplitPane.getWidth();
    double targetDividerPosition = collapsing
        ? (totalWidth > 0 ? 44 / totalWidth : 0)
        : (getSavedOverviewDividerPosition() >= 0 ? getSavedOverviewDividerPosition() : DOCUMENT_RELATIONSHIP_MODEL_DIVIDER_POSITION);
    Node showingNode = collapsing ? documentRelationshipModelCollapsedStrip : documentRelationshipModelExpandedPane;
    Node hidingNode = collapsing ? documentRelationshipModelExpandedPane : documentRelationshipModelCollapsedStrip;

    if (!animate || totalWidth <= 0) {
      hidingNode.setVisible(false);
      hidingNode.setManaged(false);
      showingNode.setVisible(true);
      showingNode.setManaged(true);
      documentRelationshipModelPane.setMaxWidth(collapsing ? 44 : Double.MAX_VALUE);
      if (totalWidth > 0 || !collapsing) {
        overviewSplitPane.setDividerPosition(0, targetDividerPosition);
      }
      saveFlag(DOCUMENT_RELATIONSHIP_MODEL_COLLAPSED_FLAG, collapsing);
      return;
    }

    double startWidth = documentRelationshipModelPane.getWidth();
    double targetWidth = collapsing ? 44 : targetDividerPosition * totalWidth;

    documentRelationshipModelPane.setMaxWidth(startWidth);
    showingNode.setOpacity(0);
    showingNode.setVisible(true);
    showingNode.setManaged(true);
    hidingNode.setManaged(true);
    overviewSplitPane.setDividerPosition(0, targetDividerPosition);

    Timeline timeline = new Timeline(
        new KeyFrame(Duration.ZERO,
            new KeyValue(documentRelationshipModelPane.maxWidthProperty(), startWidth, Interpolator.EASE_BOTH),
            new KeyValue(hidingNode.opacityProperty(), 1, Interpolator.EASE_BOTH),
            new KeyValue(showingNode.opacityProperty(), 0, Interpolator.EASE_BOTH)),
        new KeyFrame(DOCUMENT_RELATIONSHIP_MODEL_TOGGLE_DURATION,
            new KeyValue(documentRelationshipModelPane.maxWidthProperty(), targetWidth, Interpolator.EASE_BOTH),
            new KeyValue(hidingNode.opacityProperty(), 0, Interpolator.EASE_BOTH),
            new KeyValue(showingNode.opacityProperty(), 1, Interpolator.EASE_BOTH)));
    timeline.setOnFinished(event -> {
      hidingNode.setVisible(false);
      hidingNode.setManaged(false);
      hidingNode.setOpacity(1);
      showingNode.setOpacity(1);
      documentRelationshipModelPane.setMaxWidth(collapsing ? 44 : Double.MAX_VALUE);
      overviewSplitPane.setDividerPosition(0, targetDividerPosition);
      documentRelationshipModelToggleTimeline = null;
    });

    documentRelationshipModelToggleTimeline = timeline;
    timeline.play();

    saveFlag(DOCUMENT_RELATIONSHIP_MODEL_COLLAPSED_FLAG, collapsing);
  }

  /**
   * Toggles whether the "Document/Relationship Model" side panel is pinned: while pinned, selecting a node in
   * the Form Model tree ({@link #onFormModelTreeNodeSelected}) leaves the panel alone; while unpinned, such a
   * selection auto-collapses it.
   */
  @FXML
  private void onTogglePinDocumentRelationshipModel() {
    documentRelationshipModelPinned = !documentRelationshipModelPinned;
    applyPinVisuals();
    saveFlag(DOCUMENT_RELATIONSHIP_MODEL_PINNED_FLAG, documentRelationshipModelPinned);
  }

  private void applyPinVisuals() {
    pinDocumentRelationshipModelIcon.setIconLiteral(documentRelationshipModelPinned ? "mdi2p-pin" : "mdi2p-pin-outline");
    pinDocumentRelationshipModelTooltip.setText(StudioBundle.get(documentRelationshipModelPinned ? "unpin" : "pin"));
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    overviewSplitPane.getDividers().get(0).positionProperty().addListener((observable, oldValue, newValue) -> {
      if (!documentRelationshipModelCollapsed) {
        saveDividerPosition(newValue.doubleValue());
      }
    });
    restoreDocumentRelationshipModelState();
  }

  private void restoreDocumentRelationshipModelState() {
    BaseTableSettings tableSettings = getBaseTableSettings();
    if (tableSettings == null) {
      return;
    }
    documentRelationshipModelPinned = tableSettings.getFlag(DOCUMENT_RELATIONSHIP_MODEL_PINNED_FLAG);
    applyPinVisuals();
    boolean collapsed = tableSettings.getFlag(DOCUMENT_RELATIONSHIP_MODEL_COLLAPSED_FLAG);
    if (collapsed) {
      Platform.runLater(() -> setDocumentRelationshipModelCollapsed(true, false));
    }
    else {
      double position = getSavedOverviewDividerPosition();
      if (position >= 0) {
        Platform.runLater(() -> overviewSplitPane.setDividerPosition(0, position));
      }
    }
  }

  private double getSavedOverviewDividerPosition() {
    BaseTableSettings tableSettings = getBaseTableSettings();
    return tableSettings == null ? -1 : tableSettings.getDividerPosition(OVERVIEW_DIVIDER_ID);
  }

  private void saveDividerPosition(double position) {
    BaseTableSettings tableSettings = getBaseTableSettings();
    if (tableSettings == null) {
      return;
    }
    tableSettings.getDividerPositions().put(OVERVIEW_DIVIDER_ID, position);
    tableSettings.save();
  }

  private void saveFlag(@NonNull String key, boolean value) {
    BaseTableSettings tableSettings = getBaseTableSettings();
    if (tableSettings == null) {
      return;
    }
    tableSettings.getFlags().put(key, value);
    tableSettings.save();
  }

  /**
   * Refreshes the Form Model tree's cell labels, and the Data Configuration tab's tree's bold-when-configured
   * cell styling, whenever anything belonging to this model is saved - both editors commit directly from their
   * own sub-panels rather than through a shared tree-rebuilding path, so neither would otherwise reflect the
   * other's (or their own right-hand editor pane's) edits immediately. Delegates every other save (in
   * particular a Document Model save elsewhere - see {@link #onDocumentModelChangedElsewhere}) to the base
   * class.
   */
  @Override
  public void modelSaved(@NonNull ModelSaveEvent event) {
    if (event.getItem().equals(projectItem)) {
      formModelTreeController.refreshTreeLabels();
      dataConfigurationController.refreshTreeAppearance();
      return;
    }
    super.modelSaved(event);
  }

  /**
   * Reloads the Overview tab's Document Model tree and Form Model tree (including their validation state,
   * see {@link FormModelTreeController#applyFilter}) whenever the linked Document Model - or one it
   * transitively includes - is saved in a different tab, e.g. a Field deleted there must immediately stop
   * showing up as draggable/resolvable here, and any now-dangling {@code elementRef} must show its
   * validation error right away instead of only after this tab is closed and reopened.
   */
  @Override
  protected void onDocumentModelChangedElsewhere() {
    if (projectItem.getModel() instanceof FormModel formModel) {
      loadOverview(formModel);
    }
  }

  @Override
  public @NonNull ModelType getModelType() {
    return ModelType.FORM;
  }
}
