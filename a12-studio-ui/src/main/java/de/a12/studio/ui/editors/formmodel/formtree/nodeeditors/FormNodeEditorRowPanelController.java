package de.a12.studio.ui.editors.formmodel.formtree.nodeeditors;

import de.a12.studio.models.formmodel.Row;
import de.a12.studio.modelsvalidation.validators.ElementIndex;
import de.a12.studio.ui.editors.formmodel.NamePanelController;
import de.a12.studio.ui.editors.formmodel.StylesPanelController;
import de.a12.studio.ui.editors.formmodel.formtree.FormModelTreeController;
import de.a12.studio.ui.editors.propertyeditors.AnnotationsPanelController;
import de.a12.studio.ui.editors.propertyeditors.LocalizedTextTypePanelController;
import javafx.fxml.FXML;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * The Form Model tree's right-hand editor pane for a selected {@link Row} node ({@link
 * FormModelTreeController}): Name, Label (per-locale text or expression, bound to {@link Row#getTitle()}),
 * Hide Condition (boolean field from the linked Document Model and condition value), Styles and Annotations.
 */
public class FormNodeEditorRowPanelController {

  @FXML
  private NamePanelController nameController;
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
    labelController.configureCustom("label", "Label");
  }

  public void setRow(@NonNull Row row, @Nullable ElementIndex elementIndex,
      HideConditionPanelController.@NonNull MasterFieldScope hideConditionScope) {
    nameController.setCustom(row::getName, row::setName);
    labelController.setCustom(row::getTitle, row::setTitle);
    hideConditionController.configure(
        row::getHideConditionField, row::setHideConditionField,
        row::getHideConditionValue, row::setHideConditionValue,
        elementIndex, hideConditionScope);
    stylesController.setCustom(row::getStyle, row::getStyle);
    annotationsController.setCustom(row::getAnnotations);
  }
}
