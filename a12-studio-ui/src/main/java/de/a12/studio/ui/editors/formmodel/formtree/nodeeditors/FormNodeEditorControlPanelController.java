package de.a12.studio.ui.editors.formmodel.formtree.nodeeditors;

import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.formmodel.Control;
import de.a12.studio.models.formmodel.FieldConfigEntry;
import de.a12.studio.models.formmodel.FormModelContent;
import de.a12.studio.modelsvalidation.validators.ElementIndex;
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
 *   <li><b>Label</b> ({@link ControlLabelPanelController}) — read-only "Document Model" (the bound Document
 *       Model field's label) and "Field Configuration" (model-wide) sub-editors, plus an editable "Control"
 *       (per-control override) {@code LocalizedTextType} sub-editor with expression chooser.</li>
 *   <li><b>Hint</b> ({@link ControlHintPanelController}) — two plain localized text sub-editors:
 *       "Field Configuration" and "Control".</li>
 *   <li><b>Placeholder</b> ({@link ControlPlaceholderPanelController}) — two plain localized text sub-editors:
 *       "Field Configuration" and "Control".</li>
 *   <li><b>Layout</b> ({@link ControlLayoutPanelController}) — offset/span grid values per responsive
 *       breakpoint (lg/md/sm), on {@link Control#getOffset()} and {@link Control#getSpan()}.</li>
 *   <li><b>Additional Settings</b> ({@link AdditionalSettingsPanelController}) — initial value/exposition
 *       (model-wide {@code FieldConfigEntry}), message position, readonly, readonly presentation and
 *       required-field asterisk marking.</li>
 *   <li><b>External Enumeration</b> ({@link ExternalEnumerationPanelController}) — sources the bound field's
 *       enum options from an external URL (model-wide {@code FieldConfigEntry}).</li>
 *   <li><b>Dependent Enumeration</b> ({@link DependentEnumerationPanelController}) — constrains which of the
 *       bound field's own enum values are offered based on a master field's value (model-wide
 *       {@code FieldConfigEntry}).</li>
 *   <li><b>Hide Condition</b> ({@link HideConditionPanelController}) — master field selector and a checklist
 *       of trigger values, populated from the linked Document Model's Boolean/Confirm/Enumeration fields. Note:
 *       {@link Control} does not extend {@link de.a12.studio.models.formmodel.ScreenElement}, so it carries its
 *       own {@code hideCondition} property (see {@code Control.java}).</li>
 *   <li><b>Accessibility</b> ({@link ControlAccessibilityPanelController}) — checkbox keeping the label mandatory
 *       for screen readers while hiding it visually on screen, stored in
 *       {@link Control#getLabelHiddenButRead()}.</li>
 *   <li><b>Styles</b> ({@link StylesPanelController}) — CSS style classes on {@link Control#getStyle()}.</li>
 *   <li><b>Annotations</b> ({@link AnnotationsPanelController}) — model annotations on
 *       {@link Control#getAnnotations()}.</li>
 * </ol>
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
  private ControlLayoutPanelController layoutController;
  @FXML
  private AdditionalSettingsPanelController additionalSettingsController;
  @FXML
  private ExternalEnumerationPanelController externalEnumerationController;
  @FXML
  private DependentEnumerationPanelController dependentEnumerationController;
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
      @Nullable ElementIndex elementIndex,
      @Nullable FormModelContent content) {
    fieldInformationController.setControl(control, elementIndex);
    labelController.setControl(control, documentModel, content);
    hintController.setControl(control, content);
    placeholderController.setControl(control, content);
    layoutController.setControl(control);
    additionalSettingsController.setControl(control, elementIndex, content);
    FieldConfigEntry fieldConfigEntry = FieldConfigEntryHelper.findOrCreate(control, content);
    externalEnumerationController.setEntry(fieldConfigEntry);
    dependentEnumerationController.setEntry(fieldConfigEntry, elementIndex,
        HideConditionPanelController.MasterFieldScope.anchoredOrUnbound(control.getElementRef(), elementIndex));
    hideConditionController.configure(
        control::getHideCondition, control::setHideCondition,
        elementIndex, HideConditionPanelController.MasterFieldScope.anchoredOrUnbound(control.getElementRef(), elementIndex));
    accessibilityController.setControl(control);
    stylesController.setCustom(control::getStyle, control::getStyle);
    annotationsController.setCustom(control::getAnnotations);
  }
}
