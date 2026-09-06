package de.a12.studio.ui.editors.formmodel.formtree.nodeeditors;

import de.a12.studio.models.formmodel.Button;
import de.a12.studio.models.formmodel.ButtonPanel;
import de.a12.studio.modelsvalidation.validators.ElementIndex;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.editors.formmodel.NamePanelController;
import de.a12.studio.ui.editors.formmodel.StylesPanelController;
import de.a12.studio.ui.editors.formmodel.dialogs.Dialogs;
import de.a12.studio.ui.editors.formmodel.formtree.FormModelTreeController;
import de.a12.studio.ui.editors.propertyeditors.AnnotationsPanelController;
import de.a12.studio.ui.editors.propertyeditors.LocalizedTextTypePanelController;
import de.a12.studio.ui.editors.propertyeditors.ToolbarButtonsPanelController;
import de.a12.studio.ui.util.StudioBundle;
import javafx.fxml.FXML;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Optional;

/**
 * The Form Model tree's right-hand editor pane for a selected {@link ButtonPanel} node ({@link
 * FormModelTreeController}): Name, Label, its own button list (reusing {@link ToolbarButtonsPanelController},
 * the same widget used for the model-level/per-screen header+footer boxes), Hide Condition, Styles and
 * Annotations. Previously {@code ButtonPanel} didn't exist at all - a12-studio only supported buttons in
 * those header/footer boxes, never as an inline, addable screen-tree node.
 */
public class FormNodeEditorButtonPanelPanelController {

  @FXML
  private NamePanelController nameController;
  @FXML
  private LocalizedTextTypePanelController labelController;
  @FXML
  private ToolbarButtonsPanelController buttonsController;
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

  public void setButtonPanel(@NonNull ButtonPanel buttonPanel, @Nullable ElementIndex elementIndex,
      @NonNull List<String> screenIds, HideConditionPanelController.@NonNull MasterFieldScope hideConditionScope) {
    nameController.setCustom(buttonPanel::getName, buttonPanel::setName);
    labelController.setCustom(buttonPanel::getTitle, buttonPanel::setTitle);
    buttonsController.configure(StudioBundle.get("buttons"), ".buttonPanel-" + buttonPanel.getId(), buttonPanel.getButton(),
        () -> Dialogs.showButtonForAdd(Studio.stage, screenIds), button -> editButtonViaDialog(screenIds, button),
        Dialogs::generateButtonId);
    hideConditionController.configure(
        buttonPanel::getHideCondition, buttonPanel::setHideCondition,
        elementIndex, hideConditionScope);
    stylesController.setCustom(buttonPanel::getStyle, buttonPanel::getStyle);
    annotationsController.setCustom(buttonPanel::getAnnotations);
  }

  private Optional<Button> editButtonViaDialog(@NonNull List<String> screenIds, @NonNull Button button) {
    return Dialogs.showButtonForEdit(Studio.stage, screenIds, button);
  }
}
