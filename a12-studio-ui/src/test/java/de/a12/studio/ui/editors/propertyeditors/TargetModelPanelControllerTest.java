package de.a12.studio.ui.editors.propertyeditors;

import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.ui.components.ErrorContainerController;
import de.a12.studio.ui.editors.formmodel.FxTestSupport;
import de.a12.studio.ui.util.StudioBundle;
import javafx.scene.control.Label;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * The combo box of the shared "pick one Document Model" panel displays whatever id the model stores, including one
 * it has no item for (the model was deleted or renamed outside the app); that has to be reported, not look like a
 * valid selection.
 */
class TargetModelPanelControllerTest {

  private static boolean toolkitAvailable;

  @BeforeAll
  static void startToolkit() throws Exception {
    toolkitAvailable = FxTestSupport.startToolkit();
  }

  @Test
  void aStoredIdThatIsNotAmongTheProjectsModelsIsReportedByName() throws Exception {
    TargetModelPanelController panel = load(List.of(model("Person_DM")), "Gone_DM");

    assertTrue(errorShown(panel));
    assertEquals(StudioBundle.get("target_model_panel.not_found", "Gone_DM"), errorText(panel));
    assertTrue(errorText(panel).contains("Gone_DM"));
  }

  @Test
  void anExistingTargetShowsNoError() throws Exception {
    TargetModelPanelController panel = load(List.of(model("Person_DM")), "Person_DM");

    assertFalse(errorShown(panel));
  }

  @Test
  void aMissingRequiredTargetStillReportsThatOneMustBeSelected() throws Exception {
    TargetModelPanelController panel = load(List.of(model("Person_DM")), null);

    assertTrue(errorShown(panel));
    assertEquals("A Target Model must be selected.", errorText(panel));
  }

  @Test
  void anOptionalTargetThatIsUnsetShowsNoError() throws Exception {
    assumeTrue(toolkitAvailable, "No JavaFX toolkit available");
    FxTestSupport.Loaded<TargetModelPanelController> loaded =
        FxTestSupport.load("/de/a12/studio/ui/editors/propertyeditors/target-model-panel.fxml");
    FxTestSupport.onFx(() -> {
      loaded.controller().setRequired(false);
      loaded.controller().load(List.of(model("Person_DM")), null);
    });

    assertFalse(errorShown(loaded.controller()));
  }

  private static TargetModelPanelController load(List<DocumentModel> models, String selectedId) throws Exception {
    assumeTrue(toolkitAvailable, "No JavaFX toolkit available");
    FxTestSupport.Loaded<TargetModelPanelController> loaded =
        FxTestSupport.load("/de/a12/studio/ui/editors/propertyeditors/target-model-panel.fxml");
    FxTestSupport.onFx(() -> loaded.controller().load(models, selectedId));
    return loaded.controller();
  }

  private static DocumentModel model(String id) {
    DocumentModel model = new DocumentModel();
    model.setId(id);
    return model;
  }

  private static boolean errorShown(TargetModelPanelController panel) throws Exception {
    ErrorContainerController container = FxTestSupport.field(panel, "errorContainerController");
    return container.errorProperty().get();
  }

  private static String errorText(TargetModelPanelController panel) throws Exception {
    ErrorContainerController container = FxTestSupport.field(panel, "errorContainerController");
    Label label = FxTestSupport.field(container, "errorMessage");
    return label.getText();
  }
}
