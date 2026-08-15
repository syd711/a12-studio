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
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.editors.AbstractEditorController;
import de.a12.studio.ui.editors.formmodel.dialogs.Dialogs;
import de.a12.studio.ui.editors.propertyeditors.LocalizedTextPanelController;
import de.a12.studio.ui.editors.propertyeditors.ToolbarButtonsPanelController;
import de.a12.studio.ui.util.ProjectDocumentModels;
import de.a12.studio.ui.util.StudioBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.net.URL;
import java.util.List;
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
  private FormModelTreeController formModelTreeController;

  public void loadModel(@NonNull A12Model<?> model) {
    load((FormModel) model);
    updateSettingsErrorBadge();
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
    documentSourceTreeController.load(documentModel);
    formModelTreeController.setModel(formModel, documentModel, projectItem);
  }

  private @Nullable DocumentModel resolveDataBindingDocumentModel(@NonNull FormModel formModel) {
    String documentModelId = currentDocumentModelId(formModel);
    if (documentModelId == null) {
      return null;
    }
    List<DocumentModel> documentModels = ProjectDocumentModels.getOtherDocumentModels(projectItem);
    return documentModels.stream().filter(candidate -> documentModelId.equals(candidate.getId())).findFirst().orElse(null);
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

    addLabelController.configureButtonLabel("ADD", "ADD");
    addLabelController.setDefaults(defaults);
    commitAddLabelController.configureButtonLabel("COMMIT_ADD", "COMMIT ADD");
    commitAddLabelController.setDefaults(defaults);
    applyLabelController.configureButtonLabel("APPLY", "APPLY");
    applyLabelController.setDefaults(defaults);
    editLabelController.configureButtonLabel("EDIT", "EDIT");
    editLabelController.setDefaults(defaults);
    removeLabelController.configureButtonLabel("REMOVE", "REMOVE");
    removeLabelController.setDefaults(defaults);
    viewLabelController.configureButtonLabel("VIEW", "VIEW");
    viewLabelController.setDefaults(defaults);
    cancelLabelController.configureButtonLabel("CANCEL", "CANCEL");
    cancelLabelController.setDefaults(defaults);
    confirmLabelController.configureButtonLabel("CONFIRM", "CONFIRM");
    confirmLabelController.setDefaults(defaults);
    returnLabelController.configureButtonLabel("RETURN", "RETURN");
    returnLabelController.setDefaults(defaults);
    upLabelController.configureButtonLabel("UP", "UP");
    upLabelController.setDefaults(defaults);
    downLabelController.configureButtonLabel("DOWN", "DOWN");
    downLabelController.setDefaults(defaults);
    copyLabelController.configureButtonLabel("COPY", "COPY");
    copyLabelController.setDefaults(defaults);
    closeLabelController.configureButtonLabel("CLOSE", "CLOSE");
    closeLabelController.setDefaults(defaults);
    downloadLabelController.configureButtonLabel("DOWNLOAD", "DOWNLOAD");
    downloadLabelController.setDefaults(defaults);
    skipLabelController.configureButtonLabel("SKIP", "SKIP");
    skipLabelController.setDefaults(defaults);
    replaceLabelController.configureButtonLabel("REPLACE", "REPLACE");
    replaceLabelController.setDefaults(defaults);
    uploadAsCopyLabelController.configureButtonLabel("UPLOAD_AS_COPY", "UPLOAD AS COPY");
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
    return Dialogs.showButtonForAdd(Studio.stage, screenIds);
  }

  private Optional<Button> editButtonViaDialog(List<String> screenIds, Button button) {
    return Dialogs.showButtonForEdit(Studio.stage, screenIds, button);
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
  }

  @Override
  public @NonNull ModelType getModelType() {
    return ModelType.FORM;
  }
}
