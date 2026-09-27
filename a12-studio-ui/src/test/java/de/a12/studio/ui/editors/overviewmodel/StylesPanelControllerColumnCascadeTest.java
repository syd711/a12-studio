package de.a12.studio.ui.editors.overviewmodel;

import de.a12.studio.models.overviewmodel.Column;
import de.a12.studio.models.overviewmodel.ColumnStyles;
import de.a12.studio.models.overviewmodel.OverviewModel;
import de.a12.studio.models.overviewmodel.OverviewModelContent;
import de.a12.studio.models.projects.Project;
import de.a12.studio.modelsvalidation.ValidationService;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.editors.formmodel.FxTestSupport;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/** Gap 17 of the "Overview Model: gap review" - renaming or deleting a model-level style in {@link
 * StylesPanelController} (bound via {@link StylesPanelController#setModel}) cascades into every Column's
 * header/content style references, instead of leaving them dangling. */
class StylesPanelControllerColumnCascadeTest {

  private static final String FXML = "/de/a12/studio/ui/editors/overviewmodel/styles-panel.fxml";

  private static boolean toolkitAvailable;

  @BeforeAll
  static void startToolkit() throws Exception {
    toolkitAvailable = FxTestSupport.startToolkit();
  }

  @BeforeEach
  void setUp(@TempDir Path dir) throws Exception {
    if (toolkitAvailable) {
      Project project = new Project();
      project.load(dir.toFile());
      setStatic("currentProject", project);
      setStatic("validationService", new ValidationService(project));
    }
  }

  private static void setStatic(String name, Object value) throws Exception {
    Field field = Studio.class.getDeclaredField(name);
    field.setAccessible(true);
    field.set(null, value);
  }

  private static OverviewModel modelWithOneStyledColumn() {
    OverviewModel model = new OverviewModel();
    model.setId("Team_Ov");
    model.setContent(new OverviewModelContent());
    model.getContent().getStyles().add("highlight");
    Column column = new Column();
    column.setId("column_1");
    column.setWidth(1.0);
    column.setElementRef("field_1");
    ColumnStyles styles = new ColumnStyles();
    styles.getHeader().add("highlight");
    styles.getContent().add("highlight");
    column.setStyles(styles);
    model.getContent().getColumns().add(column);
    return model;
  }

  @Test
  void renamingAStyleUpdatesEveryColumnReference() throws Exception {
    assumeTrue(toolkitAvailable, "No JavaFX toolkit available");
    OverviewModel model = modelWithOneStyledColumn();

    FxTestSupport.Loaded<StylesPanelController> loaded = FxTestSupport.load(FXML);
    StylesPanelController controller = loaded.controller();
    FxTestSupport.onFx(() -> controller.setModel(model));

    TextField styleField = firstStyleField(controller);
    FxTestSupport.onFx(() -> styleField.setText("emphasis"));

    ColumnStyles styles = model.getContent().getColumns().get(0).getStyles();
    assertEquals(List.of("emphasis"), styles.getHeader());
    assertEquals(List.of("emphasis"), styles.getContent());
  }

  @Test
  void deletingAStyleRemovesEveryColumnReference() throws Exception {
    // The real delete button goes through WidgetFactory.showConfirmation, a blocking modal this test can't
    // drive - removeColumnStyleReferences is exercised directly instead (the button's own action, once
    // confirmed, is a one-line getStyles().remove(index) + this call, already covered by manual testing).
    assumeTrue(toolkitAvailable, "No JavaFX toolkit available");
    OverviewModel model = modelWithOneStyledColumn();

    FxTestSupport.Loaded<StylesPanelController> loaded = FxTestSupport.load(FXML);
    StylesPanelController controller = loaded.controller();
    FxTestSupport.onFx(() -> controller.setModel(model));

    Method removeReferences = StylesPanelController.class.getDeclaredMethod("removeColumnStyleReferences", String.class);
    removeReferences.setAccessible(true);
    FxTestSupport.onFx(() -> {
      removeReferences.invoke(controller, "highlight");
      return null;
    });

    ColumnStyles styles = model.getContent().getColumns().get(0).getStyles();
    assertTrue(styles.getHeader().isEmpty());
    assertTrue(styles.getContent().isEmpty());
  }

  private static TextField firstStyleField(StylesPanelController controller) throws Exception {
    VBox stylesList = FxTestSupport.field(controller, "stylesList");
    HBox row = (HBox) stylesList.getChildren().get(0);
    return (TextField) row.getChildren().get(1);
  }
}
