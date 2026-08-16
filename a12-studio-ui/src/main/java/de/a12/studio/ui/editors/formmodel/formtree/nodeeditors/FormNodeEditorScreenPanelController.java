package de.a12.studio.ui.editors.formmodel.formtree.nodeeditors;

import de.a12.studio.models.formmodel.Button;
import de.a12.studio.models.formmodel.ButtonGroup;
import de.a12.studio.models.formmodel.HeaderFooterBox;
import de.a12.studio.models.formmodel.Screen;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.editors.formmodel.FormModelEditorController;
import de.a12.studio.ui.editors.formmodel.NamePanelController;
import de.a12.studio.ui.editors.formmodel.dialogs.Dialogs;
import de.a12.studio.ui.editors.formmodel.formtree.FormModelTreeController;
import de.a12.studio.ui.editors.propertyeditors.AnnotationsPanelController;
import de.a12.studio.ui.editors.propertyeditors.LocalizedTextTypePanelController;
import de.a12.studio.ui.editors.propertyeditors.ToolbarButtonsPanelController;
import de.a12.studio.ui.util.StudioBundle;
import javafx.fxml.FXML;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * The Form Model tree's right-hand editor pane for a selected {@link Screen} node ({@link
 * FormModelTreeController}): a "Screen" tab (Name, Label, Annotations) and a "Subheader And Footer" tab -
 * Major/Minor button lists for this screen's own {@link Screen#getSubHeaderBox()}/{@link Screen#getFooterBox()},
 * matching {@link FormModelEditorController}'s model-wide "Subheader and Footer" tab but scoped to this one
 * screen instead of the whole model's content.
 */
public class FormNodeEditorScreenPanelController {

  @FXML
  private NamePanelController nameController;
  @FXML
  private LocalizedTextTypePanelController labelController;
  @FXML
  private AnnotationsPanelController annotationsController;

  @FXML
  private ToolbarButtonsPanelController subheaderMajorButtonsController;
  @FXML
  private ToolbarButtonsPanelController subheaderMinorButtonsController;
  @FXML
  private ToolbarButtonsPanelController footerMajorButtonsController;
  @FXML
  private ToolbarButtonsPanelController footerMinorButtonsController;

  private Screen screen;

  @FXML
  private void initialize() {
    labelController.configureCustom("label", "Label");
  }

  public void setScreen(@NonNull Screen screen, @NonNull List<String> screenIds) {
    this.screen = screen;
    nameController.setCustom(screen::getName, screen::setName);
    labelController.setCustom(screen::getTitle, screen::setTitle);
    annotationsController.setCustom(screen::getAnnotations);

    HeaderFooterBox subHeaderBox = ensureBox(screen.getSubHeaderBox(), screen.getId() + "-subHeaderBox", screen::setSubHeaderBox);
    HeaderFooterBox footerBox = ensureBox(screen.getFooterBox(), screen.getId() + "-footerBox", screen::setFooterBox);

    subheaderMajorButtonsController.configure(StudioBundle.get("subheader_major_buttons"), ".screenSubheaderMajor",
        ensureMajorButtons(subHeaderBox).getButton(), () -> newButtonViaDialog(screenIds), button -> editButtonViaDialog(screenIds, button),
        Dialogs::generateButtonId);
    subheaderMinorButtonsController.configure(StudioBundle.get("subheader_minor_buttons"), ".screenSubheaderMinor",
        ensureMinorButtons(subHeaderBox).getButton(), () -> newButtonViaDialog(screenIds), button -> editButtonViaDialog(screenIds, button),
        Dialogs::generateButtonId);
    footerMajorButtonsController.configure(StudioBundle.get("footer_major_buttons"), ".screenFooterMajor",
        ensureMajorButtons(footerBox).getButton(), () -> newButtonViaDialog(screenIds), button -> editButtonViaDialog(screenIds, button),
        Dialogs::generateButtonId);
    footerMinorButtonsController.configure(StudioBundle.get("footer_minor_buttons"), ".screenFooterMinor",
        ensureMinorButtons(footerBox).getButton(), () -> newButtonViaDialog(screenIds), button -> editButtonViaDialog(screenIds, button),
        Dialogs::generateButtonId);
  }

  private static HeaderFooterBox ensureBox(HeaderFooterBox box, String id, Consumer<HeaderFooterBox> setter) {
    if (box != null) {
      return box;
    }
    HeaderFooterBox newBox = new HeaderFooterBox();
    newBox.setId(id);
    setter.accept(newBox);
    return newBox;
  }

  private static ButtonGroup ensureMajorButtons(HeaderFooterBox box) {
    if (box.getMajorButtons() == null) {
      box.setMajorButtons(new ButtonGroup());
    }
    return box.getMajorButtons();
  }

  private static ButtonGroup ensureMinorButtons(HeaderFooterBox box) {
    if (box.getMinorButtons() == null) {
      box.setMinorButtons(new ButtonGroup());
    }
    return box.getMinorButtons();
  }

  private Optional<Button> newButtonViaDialog(List<String> screenIds) {
    return Dialogs.showButtonForAdd(Studio.stage, screenIds);
  }

  private Optional<Button> editButtonViaDialog(List<String> screenIds, Button button) {
    return Dialogs.showButtonForEdit(Studio.stage, screenIds, button);
  }
}
