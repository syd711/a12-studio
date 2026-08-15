package de.a12.studio.ui.editors.formmodel.dialogs;

import de.a12.studio.models.formmodel.Button;
import de.a12.studio.models.formmodel.EventButton;
import de.a12.studio.models.util.JsonSettings;
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
    return showButton(owner, StudioBundle.get("add_button_title"), screenIds, button);
  }

  /**
   * Opens the Edit Button dialog for a working copy of {@code button}, so a Cancel leaves the real, attached
   * button untouched - simpler than a field-by-field snapshot/restore (see {@code OverviewColumnDialogController}
   * for that pattern) given {@link FormButtonDialogController#onTypeChanged} can swap the edited instance to a
   * different {@link Button} subtype mid-edit, which a field-level restore can't undo. The caller only replaces
   * the original row with the returned button once present.
   */
  public static Optional<Button> showButtonForEdit(Stage owner, List<String> screenIds, Button button) {
    return showButton(owner, StudioBundle.get("edit_button_title"), screenIds, cloneButton(button));
  }

  private static Optional<Button> showButton(Stage owner, String title, List<String> screenIds, Button button) {
    FXMLLoader fxmlLoader = new FXMLLoader(FormButtonDialogController.class.getResource("form-button-dialog.fxml"));
    fxmlLoader.setResources(StudioBundle.getBundle());
    Stage stage = WidgetFactory.createDialogStage("button-dialog", fxmlLoader, owner, title);
    FormButtonDialogController controller = (FormButtonDialogController) stage.getUserData();
    controller.init(stage, screenIds, button);
    stage.setOnHidden(event -> controller.destroy());

    FXResizeHelper.install(stage, 30, 6, WidgetFactory.DIALOG_SHADOW_MARGIN);
    stage.setMinWidth(800);
    stage.setMinHeight(600);

    stage.showAndWait();
    // Reads back controller.getButton() rather than the local `button` above, since onTypeChanged may have
    // replaced it with a different Button subtype instance since init() was called.
    return controller.isConfirmed() ? Optional.of(controller.getButton()) : Optional.empty();
  }

  private static Button cloneButton(Button button) {
    String json = JsonSettings.objectMapper.writeValueAsString(button);
    return JsonSettings.objectMapper.readValue(json, Button.class);
  }

  /** Also used by {@link de.a12.studio.ui.editors.propertyeditors.ToolbarButtonsPanelController}'s Copy button
   * to id the duplicated row, so both id generation paths stay in sync. */
  public static String generateButtonId() {
    return "button-" + String.format("%05x", ID_RANDOM.nextInt(0x100000));
  }
}
