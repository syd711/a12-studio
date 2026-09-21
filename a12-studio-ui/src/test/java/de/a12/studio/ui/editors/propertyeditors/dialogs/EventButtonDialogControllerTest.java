package de.a12.studio.ui.editors.propertyeditors.dialogs;

import de.a12.studio.models.overviewmodel.BoxElement;
import de.a12.studio.models.overviewmodel.ButtonElement;
import de.a12.studio.models.overviewmodel.SearchElement;
import de.a12.studio.models.util.JsonSettings;
import de.a12.studio.ui.editors.formmodel.FxTestSupport;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

// The Event Button dialog against real FXML, as it opens for a Subheader element: a button needs its Event, the
// Search/Filter/Multi-Selection elements share the dialog without the button-only block.
class EventButtonDialogControllerTest {

  private static boolean toolkitAvailable;

  @BeforeAll
  static void startToolkit() throws Exception {
    toolkitAvailable = FxTestSupport.startToolkit();
  }

  private static EventButtonDialogController open(BoxElement element) throws Exception {
    EventButtonDialogController controller = FxTestSupport.<EventButtonDialogController>load(
        "/de/a12/studio/ui/editors/propertyeditors/dialogs/event-button-dialog.fxml").controller();
    FxTestSupport.onFx(() -> controller.init(new Stage(), (de.a12.studio.models.overviewmodel.OverviewButtonLike) element));
    return controller;
  }

  @Test
  void aSearchElementHidesTheButtonOnlyFieldsAndConfirmsWithoutAnEvent() throws Exception {
    assumeTrue(toolkitAvailable, "No JavaFX toolkit available");
    SearchElement search = new SearchElement();

    EventButtonDialogController controller = open(search);
    VBox buttonOnly = FxTestSupport.field(controller, "buttonOnlyBox");
    Button ok = FxTestSupport.field(controller, "okButton");
    CheckBox hideLabel = FxTestSupport.field(controller, "hideLabelField");

    assertFalse(buttonOnly.isVisible());
    assertFalse(buttonOnly.isManaged());
    assertFalse(ok.isDisabled(), "a Search element has no Event to require");

    FxTestSupport.onFx(() -> hideLabel.setSelected(true));
    FxTestSupport.onFx(ok::fire);

    assertTrue(controller.isConfirmed());
    JsonNode written = JsonSettings.objectMapper.readTree(JsonSettings.objectMapper.writeValueAsString(controller.getButton()));
    assertEquals(2, written.size(), "only type and labelHidden may be written, not the button-only defaults: " + written);
    assertEquals("search", written.get("type").asText());
    assertTrue(written.get("labelHidden").asBoolean());
  }

  @Test
  void aButtonKeepsItsButtonOnlyFieldsAndStillNeedsAnEvent() throws Exception {
    assumeTrue(toolkitAvailable, "No JavaFX toolkit available");
    ButtonElement button = new ButtonElement();

    EventButtonDialogController controller = open(button);
    VBox buttonOnly = FxTestSupport.field(controller, "buttonOnlyBox");
    Button ok = FxTestSupport.field(controller, "okButton");

    assertTrue(buttonOnly.isVisible());
    assertTrue(ok.isDisabled(), "a button without an Event cannot be confirmed");
  }
}
