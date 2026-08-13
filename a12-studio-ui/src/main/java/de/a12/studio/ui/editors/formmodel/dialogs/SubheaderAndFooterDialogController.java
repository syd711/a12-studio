package de.a12.studio.ui.editors.formmodel.dialogs;

import de.a12.studio.models.formmodel.Button;
import de.a12.studio.models.formmodel.ButtonGroup;
import de.a12.studio.models.formmodel.EventButton;
import de.a12.studio.models.formmodel.FormModel;
import de.a12.studio.models.formmodel.FormModelContent;
import de.a12.studio.models.formmodel.HeaderFooterBox;
import de.a12.studio.ui.components.DialogController;
import de.a12.studio.ui.editors.propertyeditors.EventButtonsPanelController;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.stage.Stage;
import org.jspecify.annotations.NonNull;

import java.net.URL;
import java.security.SecureRandom;
import java.util.Random;
import java.util.ResourceBundle;
import java.util.function.Consumer;

/**
 * Edits {@code content.subHeaderBox}/{@code content.footerBox}'s Major/Minor button lists, one {@link
 * EventButtonsPanelController} per list, matching the SME reference's "Major Buttons"/"Minor Buttons" tables.
 * Unlike Overview Model's subheader (a mixed list of button/search/filter/multi-selection markers, handled by
 * {@link de.a12.studio.ui.editors.overviewmodel.SubheaderSlotPanelController}), Form Model's subHeaderBox and
 * footerBox are both button-only ({@link HeaderFooterBox}), so all four lists here reuse the same simpler panel.
 * Rows can be either {@link EventButton} or {@link de.a12.studio.models.formmodel.NavigationButton} (see
 * Company_FM.json, where subHeaderBox holds navigation buttons and footerBox holds event buttons) - both satisfy
 * {@link de.a12.studio.models.EventButtonLike} via the abstract {@link Button} base, so either kind displays
 * correctly here; newly added rows default to {@link EventButton}, the more common case.
 */
public class SubheaderAndFooterDialogController implements Initializable, DialogController {

  private static final Random ID_RANDOM = new SecureRandom();

  @FXML
  private EventButtonsPanelController subheaderMajorButtonsController;
  @FXML
  private EventButtonsPanelController subheaderMinorButtonsController;
  @FXML
  private EventButtonsPanelController footerMajorButtonsController;
  @FXML
  private EventButtonsPanelController footerMinorButtonsController;

  private Stage stage;

  public void setStage(Stage stage) {
    this.stage = stage;
  }

  @Override
  public void initialize(URL location, ResourceBundle resources) {
  }

  public void setModel(@NonNull FormModel model) {
    FormModelContent content = model.getContent();
    HeaderFooterBox subHeaderBox = ensureBox(content.getSubHeaderBox(), "subHeaderBox1", content::setSubHeaderBox);
    HeaderFooterBox footerBox = ensureBox(content.getFooterBox(), "footerBox1", content::setFooterBox);

    subheaderMajorButtonsController.configure("SUBHEADER MAJOR BUTTONS", ".subheaderMajor",
        ensureMajorButtons(subHeaderBox).getButton(), this::newButton);
    subheaderMinorButtonsController.configure("SUBHEADER MINOR BUTTONS", ".subheaderMinor",
        ensureMinorButtons(subHeaderBox).getButton(), this::newButton);
    footerMajorButtonsController.configure("FOOTER MAJOR BUTTONS", ".footerMajor",
        ensureMajorButtons(footerBox).getButton(), this::newButton);
    footerMinorButtonsController.configure("FOOTER MINOR BUTTONS", ".footerMinor",
        ensureMinorButtons(footerBox).getButton(), this::newButton);
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

  private Button newButton() {
    EventButton button = new EventButton();
    button.setId(generateButtonId());
    button.setName(button.getId());
    return button;
  }

  private static String generateButtonId() {
    return "button-" + String.format("%05x", ID_RANDOM.nextInt(0x100000));
  }

  @FXML
  private void onClose() {
    stage.close();
  }

  @Override
  public void onDialogCancel() {
    stage.close();
  }
}
