package de.a12.studio.ui.editors.formmodel.formtree.nodeeditors;

import de.a12.studio.models.formmodel.ColumnLayout;
import de.a12.studio.models.formmodel.ControlGrid;
import de.a12.studio.ui.editors.formmodel.NamePanelController;
import de.a12.studio.ui.editors.formmodel.ResponsiveLayoutPanelController;
import de.a12.studio.ui.editors.formmodel.StylesPanelController;
import de.a12.studio.ui.editors.formmodel.formtree.FormModelTreeController;
import de.a12.studio.ui.editors.propertyeditors.AnnotationsPanelController;
import de.a12.studio.ui.editors.propertyeditors.ColumnLayoutPanelController;
import de.a12.studio.ui.editors.propertyeditors.LocalizedTextTypePanelController;
import javafx.fxml.FXML;
import org.jspecify.annotations.NonNull;

/**
 * The Form Model tree's right-hand editor pane for a selected {@link ControlGrid} node ({@link
 * FormModelTreeController}): Name, Layout ({@code layout.lg}, via the shared {@link ColumnLayoutPanelController}),
 * Responsive Layout ({@code layout.md}/{@code layout.sm}, via {@link ResponsiveLayoutPanelController}), Label
 * (per-locale text or expression, bound to {@link ControlGrid#getTitle()}), Styles and Annotations.
 */
public class FormNodeEditorControlGridPanelController {

  @FXML
  private NamePanelController nameController;
  @FXML
  private ColumnLayoutPanelController layoutController;
  @FXML
  private ResponsiveLayoutPanelController responsiveLayoutController;
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

  public void setControlGrid(@NonNull ControlGrid grid) {
    nameController.setCustom(grid::getName, grid::setName);
    layoutController.setCustom(() -> grid.getLayout() != null ? grid.getLayout().getLg() : null,
        value -> getOrCreateLayout(grid).setLg(value));
    responsiveLayoutController.setControlGrid(grid);
    labelController.setCustom(grid::getTitle, grid::setTitle);
    stylesController.setCustom(grid::getStyle, grid::getStyle);
    annotationsController.setCustom(grid::getAnnotations);
  }

  private static ColumnLayout getOrCreateLayout(@NonNull ControlGrid grid) {
    ColumnLayout layout = grid.getLayout();
    if (layout == null) {
      layout = new ColumnLayout();
      grid.setLayout(layout);
    }
    return layout;
  }
}
