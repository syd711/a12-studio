package de.a12.studio.ui.editors.formmodel.formtree.nodeeditors;

import de.a12.studio.models.formmodel.Section;
import de.a12.studio.modelsvalidation.validators.ElementIndex;
import de.a12.studio.ui.editors.formmodel.StylesPanelController;
import de.a12.studio.ui.editors.formmodel.formtree.FormModelTreeController;
import de.a12.studio.ui.editors.propertyeditors.AnnotationsPanelController;
import de.a12.studio.ui.editors.propertyeditors.LocalizedTextTypePanelController;
import de.a12.studio.ui.util.StudioBundle;
import javafx.fxml.FXML;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * The Form Model tree's right-hand editor pane for a selected {@link Section} node ({@link
 * FormModelTreeController}): Name/Collapsible ({@link SectionNamePanelController}), Label (per-locale text or
 * expression, bound to {@link Section#getTitle()}), Hide Condition (boolean field from the linked Document
 * Model and condition value), Styles and Annotations.
 */
public class FormNodeEditorSectionPanelController {

  @FXML
  private SectionNamePanelController nameController;
  @FXML
  private LocalizedTextTypePanelController labelController;
  @FXML
  private HideConditionPanelController hideConditionController;
  @FXML
  private StylesPanelController stylesController;
  @FXML
  private AnnotationsPanelController annotationsController;

  @FXML
  private void initialize() {
    labelController.configureCustom("label", StudioBundle.get("label"));
  }

  public void setSection(@NonNull Section section, @Nullable ElementIndex elementIndex,
      HideConditionPanelController.@NonNull MasterFieldScope hideConditionScope) {
    nameController.setSection(section);
    labelController.setCustom(section::getTitle, section::setTitle);
    hideConditionController.configure(
        section::getHideConditionField, section::setHideConditionField,
        section::getHideConditionValue, section::setHideConditionValue,
        elementIndex, hideConditionScope);
    stylesController.setCustom(section::getStyle, section::getStyle);
    annotationsController.setCustom(section::getAnnotations);
  }
}
