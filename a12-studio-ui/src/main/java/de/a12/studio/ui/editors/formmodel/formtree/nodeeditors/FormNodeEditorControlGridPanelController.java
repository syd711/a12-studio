package de.a12.studio.ui.editors.formmodel.formtree.nodeeditors;

import de.a12.studio.models.formmodel.ColumnLayout;
import de.a12.studio.models.formmodel.ControlGrid;
import de.a12.studio.modelsvalidation.validators.ElementIndex;
import de.a12.studio.ui.editors.formmodel.NamePanelController;
import de.a12.studio.ui.editors.formmodel.ResponsiveLayoutPanelController;
import de.a12.studio.ui.editors.formmodel.StylesPanelController;
import de.a12.studio.ui.editors.formmodel.formtree.FormModelTreeController;
import de.a12.studio.ui.editors.propertyeditors.AnnotationsPanelController;
import de.a12.studio.ui.editors.propertyeditors.ColumnLayoutPanelController;
import de.a12.studio.ui.editors.propertyeditors.LocalizedTextTypePanelController;
import de.a12.studio.ui.util.StudioBundle;
import javafx.fxml.FXML;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * The Form Model tree's right-hand editor pane for a selected {@link ControlGrid} node ({@link
 * FormModelTreeController}): Name, Layout ({@code layout.lg}, via the shared {@link ColumnLayoutPanelController}),
 * Responsive Layout ({@code layout.md}/{@code layout.sm}, via {@link ResponsiveLayoutPanelController}), Label
 * (per-locale text or expression, bound to {@link ControlGrid#getTitle()}), Hide Condition (boolean field from
 * the linked Document Model and condition value), Styles and Annotations.
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
  private HideConditionPanelController hideConditionController;
  @FXML
  private StylesPanelController stylesController;
  @FXML
  private AnnotationsPanelController annotationsController;

  @FXML
  private void initialize() {
    labelController.configureCustom("label", StudioBundle.get("label"));
  }

  public void setControlGrid(@NonNull ControlGrid grid, @Nullable ElementIndex elementIndex,
      HideConditionPanelController.@NonNull MasterFieldScope hideConditionScope) {
    nameController.setCustom(grid::getName, grid::setName);
    layoutController.setCustom(() -> grid.getLayout() != null ? grid.getLayout().getLg() : null,
        value -> getOrCreateLayout(grid).setLg(value));
    responsiveLayoutController.setControlGrid(grid);
    labelController.setCustom(grid::getTitle, grid::setTitle);
    hideConditionController.configure(
        grid::getHideCondition, grid::setHideCondition,
        elementIndex, hideConditionScope);
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
