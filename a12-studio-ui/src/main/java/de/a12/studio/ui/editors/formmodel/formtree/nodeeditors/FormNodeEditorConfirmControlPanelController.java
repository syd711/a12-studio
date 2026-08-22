package de.a12.studio.ui.editors.formmodel.formtree.nodeeditors;

import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.formmodel.Control;
import de.a12.studio.models.formmodel.FormModelContent;
import de.a12.studio.ui.editors.formmodel.StylesPanelController;
import de.a12.studio.ui.editors.formmodel.formtree.FormModelTreeController;
import de.a12.studio.ui.editors.propertyeditors.AnnotationsPanelController;
import javafx.fxml.FXML;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * The Form Model tree's right-hand editor pane for a {@link Control} node whose Document Model field has
 * type {@code ConfirmType} ({@link de.a12.studio.models.documentmodel.ConfirmFieldType}).
 *
 * <p>This editor extends the generic {@link FormNodeEditorControlPanelController} layout with a second tab
 * ("Dependencies") that lets the user define which form nodes are hidden for each confirm value. The
 * "Control" tab contains all the same property panels as the standard control editor:</p>
 * <ol>
 *   <li>Field Information (read-only)</li>
 *   <li>Label (Document Model + Field Configuration, read-only; Control, with expression chooser)</li>
 *   <li>Hint (Field Configuration + Control, plain localized text)</li>
 *   <li>Placeholder (Field Configuration + Control, plain localized text)</li>
 *   <li>Hide Condition</li>
 *   <li>Accessibility</li>
 *   <li>Styles</li>
 *   <li>Annotations</li>
 * </ol>
 *
 * <p>The "Dependencies" tab ({@link ConfirmDependenciesPanelController}) shows two sections — "true" and
 * "(no value)" — each with a checkable tree of the form's structural nodes (Screens down to
 * Section / ControlGrid level). Checked nodes are stored as
 * {@link de.a12.studio.models.formmodel.DependentCase#getNotRelevantNodes()} inside the
 * {@link de.a12.studio.models.formmodel.FieldConfigEntry#getDependentField()} for this control's field.</p>
 *
 * <p>Routing: {@link FormModelTreeController} routes a {@link Control} node here instead of to
 * {@link FormNodeEditorControlPanelController} when its {@link Control#getElementRef()} resolves to a
 * {@link de.a12.studio.models.documentmodel.ConfirmFieldType} field in the linked Document Model.</p>
 */
public class FormNodeEditorConfirmControlPanelController {

  // ── "Control" tab sub-panels (identical to FormNodeEditorControlPanelController) ──────────────
  @FXML private FieldInformationPanelController fieldInformationController;
  @FXML private ControlLabelPanelController labelController;
  @FXML private ControlHintPanelController hintController;
  @FXML private ControlPlaceholderPanelController placeholderController;
  @FXML private HideConditionPanelController hideConditionController;
  @FXML private ControlAccessibilityPanelController accessibilityController;
  @FXML private StylesPanelController stylesController;
  @FXML private AnnotationsPanelController annotationsController;

  // ── "Dependencies" tab ───────────────────────────────────────────────────────────────────────
  @FXML private ConfirmDependenciesPanelController dependenciesController;

  public void setControl(@NonNull Control control,
      @Nullable DocumentModel documentModel,
      @Nullable FormModelContent content) {

    // Control tab
    fieldInformationController.setControl(control, documentModel);
    labelController.setControl(control, documentModel, content);
    hintController.setControl(control, content);
    placeholderController.setControl(control, content);
    hideConditionController.configure(
        control::getHideConditionField, control::setHideConditionField,
        control::getHideConditionValue, control::setHideConditionValue,
        documentModel);
    accessibilityController.setControl(control);
    stylesController.setCustom(control::getStyle, control::getStyle);
    annotationsController.setCustom(control::getAnnotations);

    // Dependencies tab
    dependenciesController.setControl(control, content);
  }
}
