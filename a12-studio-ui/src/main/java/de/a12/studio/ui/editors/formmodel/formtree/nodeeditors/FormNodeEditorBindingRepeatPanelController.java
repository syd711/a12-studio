package de.a12.studio.ui.editors.formmodel.formtree.nodeeditors;

import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.formmodel.BindingRepeat;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.modelsvalidation.validators.ElementIndex;
import de.a12.studio.ui.editors.formmodel.StylesPanelController;
import de.a12.studio.ui.editors.formmodel.formtree.FormModelTreeController;
import de.a12.studio.ui.editors.propertyeditors.AnnotationsPanelController;
import javafx.fxml.FXML;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * The Form Model tree's right-hand editor pane for a selected {@link BindingRepeat} node ({@link
 * FormModelTreeController}) - a repeat whose rows come from a CDM relationship traversal rather than a plain
 * repeatable Document Model group. Aggregates the same {@link de.a12.studio.models.formmodel.AbstractRepeat}
 * sub-panels {@code FormNodeEditorRepeatPanelController} reuses for Inline/Embedded/Detached repeats (Column
 * Settings, Alignment, Additional Settings, Row Actions, Hide Condition, Styles, Header Styles, Annotations -
 * none of which depend on a Document Model {@code groupRef}), plus {@link BindingRepeatRelationshipPanelController}
 * for the wrapped {@code binding} (relationship/role, UI-component configuration). Deliberately omits Field
 * Information/Label/Hint/Placeholder/Default Row Action/Multi File Upload, which are all specific to a
 * groupRef-bound repeat's Document Model group or Inline/Embedded's own row-click behavior.
 */
public class FormNodeEditorBindingRepeatPanelController {

  @FXML private BindingRepeatRelationshipPanelController bindingController;
  @FXML private RepeatColumnSettingsPanelController columnSettingsController;
  @FXML private RepeatAlignmentPanelController alignmentController;
  @FXML private RepeatAdditionalSettingsPanelController additionalSettingsController;
  @FXML private RepeatRowActionsPanelController rowActionsController;
  @FXML private HideConditionPanelController hideConditionController;
  @FXML private StylesPanelController stylesController;
  @FXML private RepeatHeaderStylesPanelController headerStylesController;
  @FXML private AnnotationsPanelController annotationsController;

  public void setBindingRepeat(@NonNull BindingRepeat bindingRepeat,
      @Nullable DocumentModel documentModel,
      @Nullable ElementIndex elementIndex,
      HideConditionPanelController.@NonNull MasterFieldScope hideConditionScope,
      @NonNull ProjectItem projectItem) {
    bindingController.setBindingRepeat(bindingRepeat, documentModel, projectItem);
    columnSettingsController.setRepeat(bindingRepeat);
    alignmentController.setRepeat(bindingRepeat);
    additionalSettingsController.setRepeat(bindingRepeat);
    rowActionsController.setRepeat(bindingRepeat, elementIndex);
    hideConditionController.configure(
        bindingRepeat.getId(), bindingRepeat::getHideCondition, bindingRepeat::setHideCondition,
        elementIndex, hideConditionScope);
    stylesController.setCustom(bindingRepeat::getStyle, bindingRepeat::getStyle);
    headerStylesController.setRepeat(bindingRepeat);
    annotationsController.setCustom(bindingRepeat::getAnnotations);
  }
}
