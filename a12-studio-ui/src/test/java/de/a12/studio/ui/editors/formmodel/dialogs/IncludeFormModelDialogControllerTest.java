package de.a12.studio.ui.editors.formmodel.dialogs;

import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.formmodel.FormModel;
import de.a12.studio.models.formmodel.ScreenElement;
import de.a12.studio.models.formmodel.Section;
import de.a12.studio.models.util.JsonSettings;
import de.a12.studio.modelsvalidation.formincludes.FormIncludeExpander;
import de.a12.studio.ui.editors.formmodel.FxTestSupport;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

// The Include Form Model dialog against real FXML and SME's include example (see FormIncludeExpanderTest).
class IncludeFormModelDialogControllerTest {

  private static boolean toolkitAvailable;

  @BeforeAll
  static void startToolkit() throws Exception {
    toolkitAvailable = FxTestSupport.startToolkit();
  }

  private static <T> T load(String name, Class<T> type) throws Exception {
    try (InputStream in = IncludeFormModelDialogControllerTest.class.getResourceAsStream("/formincludes/" + name + ".json")) {
      return JsonSettings.objectMapper.readValue(in, type);
    }
  }

  private static IncludeFormModelDialogController open() throws Exception {
    return open(load("IncludedModel", FormModel.class), false);
  }

  private static IncludeFormModelDialogController open(FormModel source, boolean gridSlot) throws Exception {
    DocumentModel hostDm = load("A-for-host", DocumentModel.class);
    DocumentModel includedDm = load("B-for-include", DocumentModel.class);
    FormModel host = load("HostModel_expanded", FormModel.class);
    IncludeFormModelDialogController controller = FxTestSupport.<IncludeFormModelDialogController>load(
        "/de/a12/studio/ui/editors/formmodel/dialogs/include-form-model-dialog.fxml").controller();
    FxTestSupport.onFx(() -> controller.init(new Stage(), host, hostDm, List.of(source), List.of(hostDm, includedDm), "include-1", gridSlot));
    return controller;
  }

  @Test
  void theIncludeGroupOfTheSourcesDocumentModelIsPreselectedAndOkIsEnabled() throws Exception {
    assumeTrue(toolkitAvailable, "No JavaFX toolkit available");

    IncludeFormModelDialogController controller = open();

    ComboBox<String> path = FxTestSupport.field(controller, "pathComboBox");
    Button ok = FxTestSupport.field(controller, "okButton");
    Label summary = FxTestSupport.field(controller, "summaryLabel");
    assertEquals(List.of("/Person/address", "/Person/billing"), path.getItems());
    assertEquals("/Person/address", path.getEditor().getText());
    assertFalse(ok.isDisabled());
    // "1 element(s) and 0 configuration entries ..." - in whatever language the machine runs
    assertTrue(summary.getText().startsWith("1 ") && summary.getText().contains("0 "), summary.getText());
  }

  @Test
  void confirmingHandsOverTheExpansionWithThePrefixedIds() throws Exception {
    assumeTrue(toolkitAvailable, "No JavaFX toolkit available");
    IncludeFormModelDialogController controller = open();

    Button ok = FxTestSupport.field(controller, "okButton");
    FxTestSupport.onFx(ok::fire);

    Optional<FormIncludeExpander.Expansion> expansion = controller.getExpansion();
    assertTrue(expansion.isPresent());
    assertEquals("include-1_controlgrid_248a7", expansion.get().elements().get(0).getId());
    assertEquals("/Person/address", expansion.get().elements().get(0).getHostDocumentModelPath());
  }

  @Test
  void aPathThatDoesNotExistDisablesOkAndSaysWhy() throws Exception {
    assumeTrue(toolkitAvailable, "No JavaFX toolkit available");
    IncludeFormModelDialogController controller = open();

    ComboBox<String> path = FxTestSupport.field(controller, "pathComboBox");
    FxTestSupport.onFx(() -> path.getEditor().setText("/Person/nowhere"));

    Button ok = FxTestSupport.field(controller, "okButton");
    Label error = FxTestSupport.field(controller, "errorLabel");
    assertTrue(ok.isDisabled());
    assertTrue(error.getText().contains("/Person/nowhere"), error.getText());
    FxTestSupport.onFx(ok::fire);
    assertTrue(controller.getExpansion().isEmpty(), "a disabled OK cannot confirm anything");
  }

  @Test
  void anEmbeddedRepeatTakesAFormWhoseFirstScreenIsASingleControlGrid() throws Exception {
    assumeTrue(toolkitAvailable, "No JavaFX toolkit available");

    IncludeFormModelDialogController controller = open(load("IncludedModel", FormModel.class), true);

    Button ok = FxTestSupport.field(controller, "okButton");
    assertFalse(ok.isDisabled());
  }

  @Test
  void anEmbeddedRepeatRefusesAnythingButASingleControlGradAndNamesTheForm() throws Exception {
    assumeTrue(toolkitAvailable, "No JavaFX toolkit available");
    FormModel source = load("IncludedModel", FormModel.class);
    List<ScreenElement> elements = source.getContent().getScreens().get(0).getScreenElements();
    Section wrapper = new Section();
    wrapper.setId("wrapper");
    wrapper.getScreenElements().addAll(elements);
    elements.clear();
    elements.add(wrapper);

    IncludeFormModelDialogController controller = open(source, true);

    Button ok = FxTestSupport.field(controller, "okButton");
    Label error = FxTestSupport.field(controller, "errorLabel");
    assertTrue(ok.isDisabled());
    assertTrue(error.getText().contains("IncludedModel"), error.getText());
    // ... while a list of siblings takes it
    assertFalse(((Button) FxTestSupport.field(open(source, false), "okButton")).isDisabled());
  }

  @Test
  void cancellingHandsOverNothing() throws Exception {
    assumeTrue(toolkitAvailable, "No JavaFX toolkit available");
    IncludeFormModelDialogController controller = open();

    FxTestSupport.onFx(controller::onDialogCancel);

    assertTrue(controller.getExpansion().isEmpty());
  }
}
