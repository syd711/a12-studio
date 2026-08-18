package de.a12.studio.ui.editors.formmodel.formtree.nodeeditors;

import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.formmodel.AbstractRepeat;
import de.a12.studio.models.formmodel.FormModelContent;
import de.a12.studio.ui.editors.formmodel.StylesPanelController;
import de.a12.studio.ui.editors.formmodel.formtree.FormModelTreeController;
import de.a12.studio.ui.editors.propertyeditors.AnnotationsPanelController;
import javafx.fxml.FXML;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * The Form Model tree's right-hand editor pane for a selected {@link AbstractRepeat} node (Inline,
 * Embedded or Detached Repeat) in the Form Model tree ({@link FormModelTreeController}). Aggregates:
 * <ol>
 *   <li><b>Field Information</b> ({@link GroupInformationPanelController}) — read-only: group ID,
 *       repeatability, Document Model path, internal description.</li>
 *   <li><b>Label</b> ({@link RepeatLabelPanelController}) — Group Configuration + Repeat sub-editors,
 *       both with expression chooser.</li>
 *   <li><b>Hint</b> ({@link RepeatHintPanelController}) — Group Configuration + Repeat, plain localized text.</li>
 *   <li><b>Placeholder</b> ({@link RepeatPlaceholderPanelController}) — Group Configuration + Repeat, plain.</li>
 *   <li><b>Column Settings</b> ({@link RepeatColumnSettingsPanelController}) — row height / table height
 *       from {@link de.a12.studio.models.formmodel.TableStyle}.</li>
 *   <li><b>Alignment</b> ({@link RepeatAlignmentPanelController}) — default horizontal alignment combo.</li>
 *   <li><b>Additional Settings</b> ({@link RepeatAdditionalSettingsPanelController}) — behavioural flags
 *       (enableAdd/Remove/Reorder/Copy/ColumnsResize, infiniteScrolling, readonlyPresentation, readonly).</li>
 *   <li><b>Hide Condition</b> ({@link HideConditionPanelController}) — inherited from
 *       {@link de.a12.studio.models.formmodel.ScreenElement}.</li>
 *   <li><b>Styles</b> ({@link StylesPanelController}) — body CSS style classes from
 *       {@link de.a12.studio.models.formmodel.ScreenElement#getStyle()}.</li>
 *   <li><b>Header Styles</b> ({@link RepeatHeaderStylesPanelController}) — header-row CSS style classes
 *       from {@link AbstractRepeat#getHeaderStyle()}.</li>
 *   <li><b>Annotations</b> ({@link AnnotationsPanelController}) — from
 *       {@link de.a12.studio.models.formmodel.ScreenElement#getAnnotations()}.</li>
 * </ol>
 */
public class FormNodeEditorRepeatPanelController {

  @FXML private GroupInformationPanelController fieldInformationController;
  @FXML private RepeatLabelPanelController labelController;
  @FXML private RepeatHintPanelController hintController;
  @FXML private RepeatPlaceholderPanelController placeholderController;
  @FXML private RepeatColumnSettingsPanelController columnSettingsController;
  @FXML private RepeatAlignmentPanelController alignmentController;
  @FXML private RepeatAdditionalSettingsPanelController additionalSettingsController;
  @FXML private HideConditionPanelController hideConditionController;
  @FXML private StylesPanelController stylesController;
  @FXML private RepeatHeaderStylesPanelController headerStylesController;
  @FXML private AnnotationsPanelController annotationsController;

  public void setRepeat(@NonNull AbstractRepeat repeat,
      @Nullable DocumentModel documentModel,
      @Nullable FormModelContent content) {
    fieldInformationController.setRepeat(repeat, documentModel);
    labelController.setRepeat(repeat, content);
    hintController.setRepeat(repeat, content);
    placeholderController.setRepeat(repeat, content);
    columnSettingsController.setRepeat(repeat);
    alignmentController.setRepeat(repeat);
    additionalSettingsController.setRepeat(repeat);
    hideConditionController.configure(
        repeat::getHideConditionField, repeat::setHideConditionField,
        repeat::getHideConditionValue, repeat::setHideConditionValue,
        documentModel);
    stylesController.setCustom(repeat::getStyle, repeat::getStyle);
    headerStylesController.setRepeat(repeat);
    annotationsController.setCustom(repeat::getAnnotations);
  }
}
