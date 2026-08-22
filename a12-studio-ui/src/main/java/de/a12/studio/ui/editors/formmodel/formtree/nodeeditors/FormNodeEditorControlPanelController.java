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
 * The Form Model tree's right-hand editor pane for a selected {@link Control} (document form field) node
 * ({@link FormModelTreeController}). Aggregates the following property editors in order:
 * <ol>
 *   <li><b>Field Information</b> ({@link FieldInformationPanelController}) — read-only: field ID, data type,
 *       Document Model path, and internal description of the bound Document Model element.</li>
 *   <li><b>Label</b> ({@link ControlLabelPanelController}) — two {@code LocalizedTextType} sub-editors:
 *       "Field Configuration" (model-wide) and "Control" (per-control override), both with expression chooser.</li>
 *   <li><b>Hint</b> ({@link ControlHintPanelController}) — two plain localized text sub-editors:
 *       "Field Configuration" and "Control".</li>
 *   <li><b>Placeholder</b> ({@link ControlPlaceholderPanelController}) — two plain localized text sub-editors:
 *       "Field Configuration" and "Control".</li>
 *   <li><b>Hide Condition</b> ({@link HideConditionPanelController}) — boolean field selector and condition
 *       value combo, populated from the linked Document Model's boolean fields. Note: {@link Control} does not
 *       extend {@link de.a12.studio.models.formmodel.ScreenElement}, so it carries its own
 *       {@code hideConditionField}/{@code hideConditionValue} pair (see {@code Control.java}).</li>
 *   <li><b>Accessibility</b> ({@link ControlAccessibilityPanelController}) — checkbox keeping the label mandatory
 *       for screen readers while hiding it visually on screen, stored in
 *       {@link Control#getLabelHiddenButRead()}.</li>
 *   <li><b>Styles</b> ({@link StylesPanelController}) — CSS style classes on {@link Control#getStyle()}.</li>
 *   <li><b>Annotations</b> ({@link AnnotationsPanelController}) — model annotations on
 *       {@link Control#getAnnotations()}.</li>
 * </ol>
 * The "Layout" section (offset / span) is intentionally omitted for now — it will be added in a separate step
 * once the layout editor components are in place.
 */
public class FormNodeEditorControlPanelController {

  @FXML
  private FieldInformationPanelController fieldInformationController;
  @FXML
  private ControlLabelPanelController labelController;
  @FXML
  private ControlHintPanelController hintController;
  @FXML
  private ControlPlaceholderPanelController placeholderController;
  @FXML
  private HideConditionPanelController hideConditionController;
  @FXML
  private ControlAccessibilityPanelController accessibilityController;
  @FXML
  private StylesPanelController stylesController;
  @FXML
  private AnnotationsPanelController annotationsController;

  public void setControl(@NonNull Control control,
      @Nullable DocumentModel documentModel,
      @Nullable FormModelContent content) {
    fieldInformationController.setControl(control, documentModel);
    labelController.setControl(control, content);
    hintController.setControl(control, content);
    placeholderController.setControl(control, content);
    hideConditionController.configure(
        control::getHideConditionField, control::setHideConditionField,
        control::getHideConditionValue, control::setHideConditionValue,
        documentModel);
    accessibilityController.setControl(control);
    stylesController.setCustom(control::getStyle, control::getStyle);
    annotationsController.setCustom(control::getAnnotations);
  }
}
