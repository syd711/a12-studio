package de.a12.studio.ui.editors.formmodel.formtree;

import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.formmodel.ControlGrid;
import de.a12.studio.models.formmodel.EmbeddedRepeat;
import de.a12.studio.models.formmodel.FormModel;
import de.a12.studio.models.formmodel.Screen;
import de.a12.studio.models.formmodel.ScreenElement;
import de.a12.studio.models.formmodel.Section;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.models.util.JsonSettings;
import de.a12.studio.ui.editors.formmodel.FxTestSupport;
import de.a12.studio.ui.util.StudioBundle;
import de.a12.studio.ui.util.commandstack.CommandStack;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.InputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

// Where the include actions show up in the tree's menus: SME's include example (see FormIncludeExpanderTest) as host.
class FormModelActionsIncludeMenuTest {

  private static boolean toolkitAvailable;

  @BeforeAll
  static void startToolkit() throws Exception {
    toolkitAvailable = FxTestSupport.startToolkit();
  }

  private final ProjectItem item = new ProjectItem(fixture("HostModel_expanded"));
  private final FormModel host = (FormModel) item.getModel();
  private final Screen screen = host.getContent().getScreens().get(0);
  private final Section addressSection = (Section) screen.getScreenElements().get(1);
  private final ScreenElement includedGrid = addressSection.getScreenElements().get(0);

  private static File fixture(String name) {
    try {
      return new File(FormModelActionsIncludeMenuTest.class.getResource("/formincludes/" + name + ".json").toURI());
    }
    catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }

  private static DocumentModel hostDm() throws Exception {
    try (InputStream in = FormModelActionsIncludeMenuTest.class.getResourceAsStream("/formincludes/A-for-host.json")) {
      return JsonSettings.objectMapper.readValue(in, DocumentModel.class);
    }
  }

  private FormModelActions actions(DocumentModel documentModel) {
    return new FormModelActions(host.getContent(), new CommandStack(), node -> {
    }, item, documentModel);
  }

  private static List<String> texts(List<MenuItem> items) {
    return items.stream().map(MenuItem::getText).toList();
  }

  @Test
  void aScreenAndASectionOfAFormWithADocumentModelOfferToIncludeAFormModel() throws Exception {
    assumeTrue(toolkitAvailable, "No JavaFX toolkit available");
    FormModelActions actions = actions(hostDm());

    List<String> onScreen = FxTestSupport.onFx(() -> texts(actions.createAddMenuItems(new FormElementViewModel(screen, null, null))));
    List<String> onSection = FxTestSupport.onFx(() -> texts(actions.createAddMenuItems(new FormElementViewModel(addressSection, screen, null))));

    String label = StudioBundle.get("form_model_tree.include_form_model");
    assertTrue(onScreen.contains(label), onScreen.toString());
    assertTrue(onSection.contains(label), onSection.toString());
  }

  @Test
  void aControlGridCannotHoldAnInclude() throws Exception {
    assumeTrue(toolkitAvailable, "No JavaFX toolkit available");
    FormModelActions actions = actions(hostDm());

    List<String> onGrid = FxTestSupport.onFx(
        () -> texts(actions.createAddMenuItems(new FormElementViewModel(includedGrid, addressSection, null))));

    assertFalse(onGrid.contains(StudioBundle.get("form_model_tree.include_form_model")), onGrid.toString());
  }

  @Test
  void withoutADocumentModelThereIsNothingToBindTheIncludeTo() throws Exception {
    assumeTrue(toolkitAvailable, "No JavaFX toolkit available");
    FormModelActions actions = actions(null);

    List<String> onScreen = FxTestSupport.onFx(() -> texts(actions.createAddMenuItems(new FormElementViewModel(screen, null, null))));

    assertFalse(onScreen.contains(StudioBundle.get("form_model_tree.include_form_model")), onScreen.toString());
  }

  @Test
  void anEmbeddedRepeatOffersToIncludeIntoItsGridAndItsIncludedGridCanBeRefreshed() throws Exception {
    assumeTrue(toolkitAvailable, "No JavaFX toolkit available");
    FormModelActions actions = actions(hostDm());
    EmbeddedRepeat repeat = new EmbeddedRepeat();
    repeat.setControlGrid((ControlGrid) includedGrid);

    List<String> onRepeat = FxTestSupport.onFx(() -> texts(actions.createAddMenuItems(new FormElementViewModel(repeat, screen, null))));
    ContextMenu onGrid = FxTestSupport.onFx(() -> actions.createContextMenu(new FormElementViewModel(includedGrid, repeat, null)));

    assertTrue(onRepeat.contains(StudioBundle.get("form_model_tree.include_form_model")), onRepeat.toString());
    String label = StudioBundle.get("form_model_tree.refresh_include");
    assertEquals(1, onGrid.getItems().stream().filter(entry -> label.equals(entry.getText())).count(),
        "a grid in the slot has no siblings but is refreshable");
  }

  @Test
  void anIncludedElementCanBeRefreshedAndAPlainOneCannot() throws Exception {
    assumeTrue(toolkitAvailable, "No JavaFX toolkit available");
    FormModelActions actions = actions(hostDm());

    ContextMenu onIncluded = FxTestSupport.onFx(() -> actions.createContextMenu(new FormElementViewModel(includedGrid, addressSection, null)));
    ContextMenu onPlain = FxTestSupport.onFx(() -> actions.createContextMenu(new FormElementViewModel(addressSection, screen, null)));

    String label = StudioBundle.get("form_model_tree.refresh_include");
    assertEquals(1, onIncluded.getItems().stream().filter(entry -> label.equals(entry.getText())).count());
    assertEquals(0, onPlain.getItems().stream().filter(entry -> label.equals(entry.getText())).count());
  }
}
