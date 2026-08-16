package de.a12.studio.ui.editors.formmodel.formtree.nodeeditors;

import de.a12.studio.models.formmodel.Section;
import de.a12.studio.ui.editors.formmodel.StylesPanelController;
import de.a12.studio.ui.editors.formmodel.formtree.FormModelTreeController;
import de.a12.studio.ui.editors.propertyeditors.AnnotationsPanelController;
import de.a12.studio.ui.editors.propertyeditors.LocalizedTextTypePanelController;
import javafx.fxml.FXML;
import org.jspecify.annotations.NonNull;

/**
 * The Form Model tree's right-hand editor pane for a selected {@link Section} node ({@link
 * FormModelTreeController}): Name/Collapsible ({@link SectionNamePanelController}), Label (per-locale text or
 * expression, bound to {@link Section#getTitle()}), Styles and Annotations.
 */
public class FormNodeEditorSectionPanelController {

  @FXML
  private SectionNamePanelController nameController;
  @FXML
  private LocalizedTextTypePanelController labelController;
  @FXML
  private StylesPanelController stylesController;
  @FXML
  private AnnotationsPanelController annotationsController;

  @FXML
  private void initialize() {
    labelController.configureCustom("label", "Label");
  }

  public void setSection(@NonNull Section section) {
    nameController.setSection(section);
    labelController.setCustom(section::getTitle, section::setTitle);
    stylesController.setCustom(section::getStyle, section::getStyle);
    annotationsController.setCustom(section::getAnnotations);
  }
}
