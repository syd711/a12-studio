package de.a12.studio.ui.editors.formmodel.formtree.nodeeditors;

import de.a12.studio.models.formmodel.CustomScreenElement;
import de.a12.studio.modelsvalidation.validators.ElementIndex;
import de.a12.studio.ui.editors.formmodel.NamePanelController;
import de.a12.studio.ui.editors.formmodel.StylesPanelController;
import de.a12.studio.ui.editors.formmodel.formtree.FormModelTreeController;
import de.a12.studio.ui.editors.propertyeditors.AnnotationsPanelController;
import de.a12.studio.ui.editors.propertyeditors.LocalizedTextTypePanelController;
import de.a12.studio.ui.util.StudioBundle;
import javafx.fxml.FXML;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * The Form Model tree's right-hand editor pane for a selected {@link CustomScreenElement} node ({@link
 * FormModelTreeController}): Name, Label, Hide Condition, Styles and Annotations. Previously this node type
 * had no editor at all - it could be added via the tree/drag-drop but none of its (inherited)
 * {@link de.a12.studio.models.formmodel.ScreenElement} fields could be changed afterward.
 */
public class FormNodeEditorCustomScreenElementPanelController {

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
    labelController.configureCustom("label", StudioBundle.get("label"));
  }

  public void setCustomScreenElement(@NonNull CustomScreenElement element, @Nullable ElementIndex elementIndex,
      HideConditionPanelController.@NonNull MasterFieldScope hideConditionScope) {
    nameController.setCustom(element::getName, element::setName);
    labelController.setCustom(element::getTitle, element::setTitle);
    hideConditionController.configure(
        element::getHideCondition, element::setHideCondition,
        elementIndex, hideConditionScope);
    stylesController.setCustom(element::getStyle, element::getStyle);
    annotationsController.setCustom(element::getAnnotations);
  }
}
