package de.a12.studio.ui.editors.formmodel;

import de.a12.studio.models.formmodel.Style;
import de.a12.studio.ui.util.StudioBundle;
import javafx.scene.control.Button;
import javafx.scene.control.TitledPane;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
}
