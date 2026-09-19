package de.a12.studio.ui.editors.formmodel;

import de.a12.studio.models.formmodel.Style;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.ui.util.StudioBundle;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.VBox;
import javafx.scene.control.TitledPane;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

// The Styles panel as reused for the Form Model's model-level style list (Model Settings dialog).
class StylesPanelControllerTest {

  private static boolean toolkitAvailable;

  @BeforeAll
  static void startToolkit() throws Exception {
    toolkitAvailable = FxTestSupport.startToolkit();
  }

  @Test
  void modelStylesPanelHasItsOwnTitleAndEditsTheGivenList() throws Exception {
    assumeTrue(toolkitAvailable, "No JavaFX toolkit available");
    FxTestSupport.Loaded<StylesPanelController> loaded =
        FxTestSupport.load("/de/a12/studio/ui/editors/formmodel/styles-panel.fxml");
    StylesPanelController panel = loaded.controller();
    TitledPane root = (TitledPane) loaded.root();
    List<Style> styles = new ArrayList<>();

    FxTestSupport.onFx(() -> {
      panel.configureModelStyles();
      panel.setCustom(() -> styles, () -> styles);
    });
    Button add = (Button) root.getContent().lookup("#addButton");
    assertNotNull(add);
    FxTestSupport.onFx(add::fire);

    assertEquals(StudioBundle.get("model_styles"), root.getText());
    assertEquals(1, styles.size());
  }

  @Test
  void modelStylesPanelCanBeHidden() throws Exception {
    assumeTrue(toolkitAvailable, "No JavaFX toolkit available");
    FxTestSupport.Loaded<StylesPanelController> loaded =
        FxTestSupport.load("/de/a12/studio/ui/editors/formmodel/styles-panel.fxml");
    TitledPane root = (TitledPane) loaded.root();

    FxTestSupport.onFx(() -> loaded.controller().setVisible(false));

    assertEquals(false, root.isVisible());
    assertEquals(false, root.isManaged());
  }

  // ---- preset mode: inside a Form Model an element's style is picked from the model-level styles ----

  @AfterEach
  void deselect() {
    FxTestSupport.selectProjectItem(null);
  }

  private static void selectFormModel(Path dir, String stylesJson) throws Exception {
    Path file = dir.resolve("Presets_FM.json");
    Files.writeString(file, "{\"header\": {\"id\": \"Presets_FM\", \"modelType\": \"form\", \"modelVersion\": \"39.0.0\"},"
        + " \"content\": {\"styles\": " + stylesJson + ", \"screens\": []}}");
    ProjectItem item = new ProjectItem(file.toFile());
    assertNotNull(item.getModel(), "the fixture form model must load");
    FxTestSupport.selectProjectItem(item);
  }

  @Test
  void insideAFormModelEachEntryIsPickedFromTheDefinedStyles(@TempDir Path dir) throws Exception {
    assumeTrue(toolkitAvailable, "No JavaFX toolkit available");
    selectFormModel(dir, "[{\"name\": \"highlight\"}, {\"name\": \"muted\"}]");
    FxTestSupport.Loaded<StylesPanelController> loaded =
        FxTestSupport.load("/de/a12/studio/ui/editors/formmodel/styles-panel.fxml");
    TitledPane root = (TitledPane) loaded.root();
    List<Style> elementStyles = new ArrayList<>();
    Style used = new Style();
    used.setName("legacy");
    elementStyles.add(used);

    FxTestSupport.onFx(() -> loaded.controller().setCustom(() -> elementStyles, () -> elementStyles));

    VBox rows = FxTestSupport.field(loaded.controller(), "stylesList");
    @SuppressWarnings("unchecked")
    ComboBox<String> combo = (ComboBox<String>) ((javafx.scene.layout.HBox) rows.getChildren().get(0)).getChildren().stream()
        .filter(node -> node instanceof ComboBox).findFirst().orElseThrow();
    assertEquals(List.of("highlight", "muted", "legacy"), combo.getItems(),
        "the defined styles, plus the entry's own undefined name so it can still be seen");
    assertEquals("legacy", combo.getValue());

    FxTestSupport.onFx(() -> combo.setValue("muted"));
    assertEquals("muted", used.getName());
    Button add = (Button) root.getContent().lookup("#addButton");
    assertFalse(add.isDisabled());
  }

  @Test
  void withoutDefinedStylesAddIsDisabledAndTheHintPointsToTheModelSettings(@TempDir Path dir) throws Exception {
    assumeTrue(toolkitAvailable, "No JavaFX toolkit available");
    selectFormModel(dir, "[]");
    FxTestSupport.Loaded<StylesPanelController> loaded =
        FxTestSupport.load("/de/a12/studio/ui/editors/formmodel/styles-panel.fxml");
    TitledPane root = (TitledPane) loaded.root();

    FxTestSupport.onFx(() -> loaded.controller().setCustom(ArrayList::new, ArrayList::new));

    assertTrue(((Button) root.getContent().lookup("#addButton")).isDisabled());
    javafx.scene.control.Label hint = FxTestSupport.field(loaded.controller(), "stylesEmptyLabel");
    assertEquals(StudioBundle.get("no_styles_defined_in_model_settings"), hint.getText());
  }

  @Test
  void theModelLevelListItselfKeepsTypedNames(@TempDir Path dir) throws Exception {
    assumeTrue(toolkitAvailable, "No JavaFX toolkit available");
    selectFormModel(dir, "[{\"name\": \"highlight\"}]");
    FxTestSupport.Loaded<StylesPanelController> loaded =
        FxTestSupport.load("/de/a12/studio/ui/editors/formmodel/styles-panel.fxml");
    List<Style> presets = new ArrayList<>();
    presets.add(new Style());

    FxTestSupport.onFx(() -> {
      loaded.controller().configureModelStyles();
      loaded.controller().setCustom(() -> presets, () -> presets);
    });

    VBox rows = FxTestSupport.field(loaded.controller(), "stylesList");
    assertTrue(((javafx.scene.layout.HBox) rows.getChildren().get(0)).getChildren().stream().anyMatch(node -> node instanceof javafx.scene.control.TextField),
        "the list that defines the styles can't pick from itself");
  }
}
