package de.a12.studio.ui.editors.formmodel.dialogs;

import de.a12.studio.models.formmodel.RowAction;
import de.a12.studio.ui.editors.formmodel.FxTestSupport;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

// The Edit Row Action dialog against real FXML: what ends up on the RowAction it edits.
class RowActionDialogControllerTest {

  private static boolean toolkitAvailable;

  @BeforeAll
  static void startToolkit() throws Exception {
    toolkitAvailable = FxTestSupport.startToolkit();
  }

  private static RowActionDialogController open(RowAction rowAction) throws Exception {
    RowActionDialogController controller = FxTestSupport.<RowActionDialogController>load(
        "/de/a12/studio/ui/editors/formmodel/dialogs/row-action-dialog.fxml").controller();
    FxTestSupport.onFx(() -> controller.init(new Stage(), null, rowAction));
    return controller;
  }

  private static void submit(RowActionDialogController controller) throws Exception {
    Button ok = FxTestSupport.field(controller, "okButton");
    FxTestSupport.onFx(ok::fire);
  }

  @Test
  void untouchedActionIsNotGivenEmptyContainers() throws Exception {
    assumeTrue(toolkitAvailable, "No JavaFX toolkit available");
    RowAction rowAction = new RowAction();
    rowAction.setEvent("event_remove");
    rowAction.setScope("ALWAYS");

    RowActionDialogController controller = open(rowAction);
    submit(controller);

    assertTrue(controller.isConfirmed());
    assertNull(controller.getRowAction().getButtonStyling(), "opening and confirming must not create an empty buttonStyling");
    assertNull(controller.getRowAction().getConfirmation());
    assertNull(controller.getRowAction().getConfirmationDialogTitle());
    assertEquals("event_remove", controller.getRowAction().getEvent());
    assertEquals("ALWAYS", controller.getRowAction().getScope());
  }

  @Test
  void visualSettingsAreWrittenToTheActionsButtonStyling() throws Exception {
    assumeTrue(toolkitAvailable, "No JavaFX toolkit available");
    RowAction rowAction = new RowAction();
    rowAction.setEvent("event_remove");
    RowActionDialogController controller = open(rowAction);

    Object visual = FxTestSupport.field(controller, "visualController");
    ComboBox<String> priority = FxTestSupport.field(visual, "priorityCombo");
    CheckBox destructive = FxTestSupport.field(visual, "destructiveField");
    FxTestSupport.onFx(() -> {
      priority.setValue("PRIMARY");
      destructive.setSelected(true);
    });
    submit(controller);

    assertNotNull(controller.getRowAction().getButtonStyling());
    assertEquals("PRIMARY", controller.getRowAction().getButtonStyling().getPriority());
    assertEquals(Boolean.TRUE, controller.getRowAction().getButtonStyling().getDestructive());
  }

  @Test
  void stylingThatWasEditedAndClearedAgainIsDroppedOnConfirm() throws Exception {
    assumeTrue(toolkitAvailable, "No JavaFX toolkit available");
    RowAction rowAction = new RowAction();
    rowAction.setEvent("event_remove");
    RowActionDialogController controller = open(rowAction);

    Object visual = FxTestSupport.field(controller, "visualController");
    CheckBox destructive = FxTestSupport.field(visual, "destructiveField");
    FxTestSupport.onFx(() -> destructive.setSelected(true));
    FxTestSupport.onFx(() -> destructive.setSelected(false));
    assertNotNull(rowAction.getButtonStyling(), "the edit created it");
    submit(controller);

    assertNull(controller.getRowAction().getButtonStyling(), "a styling with nothing set must not be saved as {}");
  }

  @Test
  void okIsDisabledWhileTheEventIsBlank() throws Exception {
    assumeTrue(toolkitAvailable, "No JavaFX toolkit available");
    RowAction rowAction = new RowAction();
    RowActionDialogController controller = open(rowAction);
    Button ok = FxTestSupport.field(controller, "okButton");
    assertTrue(ok.isDisabled());

    Object functions = FxTestSupport.field(controller, "functionsController");
    TextField eventField = FxTestSupport.field(functions, "eventField");
    FxTestSupport.onFx(() -> eventField.setText("event_remove"));
    assertFalse(ok.isDisabled());
    assertEquals("event_remove", rowAction.getEvent());

    FxTestSupport.onFx(() -> eventField.setText(""));
    assertTrue(ok.isDisabled());
  }
}
