package de.a12.studio.ui.editors.formmodel.dialogs;

import de.a12.studio.models.formmodel.Button;
import de.a12.studio.models.formmodel.EventButton;
import de.a12.studio.ui.util.FXResizeHelper;
import de.a12.studio.ui.util.StudioBundle;
import de.a12.studio.ui.util.WidgetFactory;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;

import java.security.SecureRandom;
import java.util.List;
import java.util.Optional;
import java.util.Random;

public class Dialogs {

  private static final Random ID_RANDOM = new SecureRandom();

  // Matches Company_FM.json's existing button ids (e.g. "button-55e7e") and
  // FormModelEditorController's previous newButton() id generation.
  private static final String DEFAULT_SCOPE = "HIDDEN_IN_READONLY_MODE";

  private Dialogs() {
  }

  /**
   * Opens the Add Button dialog for a brand-new, unattached {@link EventButton} (the more common case, see
   * {@link de.a12.studio.ui.editors.propertyeditors.ToolbarButtonsPanelController}'s Javadoc). The caller only
   * adds the returned button to its owning list once present - it's never attached to the model tree by this
   * method itself.
   */
  public static Optional<Button> showButtonForAdd(Stage owner, List<String> screenIds) {
    EventButton button = new EventButton();
    button.setId(generateButtonId());
    button.setScope(DEFAULT_SCOPE);
    return showButton(owner, StudioBundle.get("add_button_title"), screenIds, button) ? Optional.of(button) : Optional.empty();
  }

  private static boolean showButton(Stage owner, String title, List<String> screenIds, Button button) {
    FXMLLoader fxmlLoader = new FXMLLoader(FormButtonDialogController.class.getResource("form-button-dialog.fxml"));
    fxmlLoader.setResources(StudioBundle.getBundle());
    Stage stage = WidgetFactory.createDialogStage("button-dialog", fxmlLoader, owner, title);
    FormButtonDialogController controller = (FormButtonDialogController) stage.getUserData();
    controller.init(stage, screenIds, button);
    stage.setOnHidden(event -> controller.destroy());

    FXResizeHelper.install(stage, 30, 6, WidgetFactory.DIALOG_SHADOW_MARGIN);
    stage.setMinWidth(800);
    stage.setMinHeight(600);
    stage.setOnHidden(event -> controller.destroy());

    stage.showAndWait();
    return controller.isConfirmed();
  }

  private static String generateButtonId() {
    return "button-" + String.format("%05x", ID_RANDOM.nextInt(0x100000));
  }
}
