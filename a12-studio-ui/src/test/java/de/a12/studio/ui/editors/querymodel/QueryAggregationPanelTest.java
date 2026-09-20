package de.a12.studio.ui.editors.querymodel;

import de.a12.studio.models.projects.Project;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.models.querymodel.QueryAggregation;
import de.a12.studio.models.querymodel.QueryAggregationEntry;
import de.a12.studio.models.querymodel.QueryAggregationGroup;
import de.a12.studio.models.querymodel.QueryModel;
import de.a12.studio.models.util.JsonSettings;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.editors.formmodel.FxTestSupport;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import tools.jackson.databind.JsonNode;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Callable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * The Query Model editor's aggregation panel on the JavaFX thread (skips itself without a display): the switch that
 * is the presence of {@code content.aggregation}, the rows, the choices they offer, and what reaches the file. Runs
 * against the {@code advanced_new} workspace's query and its target Document Model {@code Person_Dc} (number field
 * {@code /Person/WeeklyWorkhours}, string fields {@code /Person/Name}, {@code /Person/FirstName}, an enumeration
 * {@code /Person/Type}).
 */
class QueryAggregationPanelTest {

  private static final String QUERY = "PersonsWithFulltimeContract_UnassignedToTeam_Qe";
  private static final String NUMBER = "/Person/WeeklyWorkhours";
  private static final String STRING = "/Person/Name";
  private static final String ENUMERATION = "/Person/Type";

  private static boolean toolkitAvailable;

  @TempDir
  Path workspace;

  private QueryAggregationPanelController openPanel;

  @BeforeAll
  static void startToolkit() throws Exception {
    toolkitAvailable = FxTestSupport.startToolkit();
  }

  @AfterAll
  static void restoreEmptyProject() throws Exception {
    if (toolkitAvailable) {
      setCurrentProject(new Project());
      FxTestSupport.selectProjectItem(null);
    }
  }

  /** Closes the panel like the editor does when its tab closes, which cancels a still-debounced alias save - one
   * firing after the test would race the deletion of the temporary workspace. */
  @AfterEach
  void closePanel() throws Exception {
    if (openPanel != null) {
      FxTestSupport.onFx(openPanel::destroy);
      FxTestSupport.onFx(() -> { });
      openPanel = null;
    }
  }

  @Test
  void aQueryWithoutAggregationHasTheSwitchOffAndNoEditor() throws Exception {
    Fixture fixture = fixture();

    assertFalse(fixture.switchField().isSelected());
    assertFalse(fixture.aggregationBox().isVisible());
    assertFalse(fixture.aggregationBox().isManaged());
    assertNull(fixture.query.getContent().getAggregation());
  }

  @Test
  void theSwitchIsThePresenceOfTheAggregationBlockInTheFile() throws Exception {
    Fixture fixture = fixture();

    fixture.onFx(() -> fixture.switchField().setSelected(true));
    assertNotNull(fixture.query.getContent().getAggregation());
    assertTrue(fixture.aggregationBox().isVisible());
    assertTrue(fixture.savedContent().has("aggregation"));

    fixture.onFx(() -> fixture.switchField().setSelected(false));
    assertNull(fixture.query.getContent().getAggregation());
    assertFalse(fixture.aggregationBox().isVisible());
    assertFalse(fixture.savedContent().has("aggregation"));
  }

  @Test
  void switchingOffAndOnAgainRestoresTheConfigurationOfTheSession() throws Exception {
    Fixture fixture = fixture();
    fixture.onFx(() -> fixture.switchField().setSelected(true));
    fixture.addAggregation();
    fixture.onFx(() -> fixture.entryRow(0).function().setValue("max"));

    fixture.onFx(() -> fixture.switchField().setSelected(false));
    fixture.onFx(() -> fixture.switchField().setSelected(true));

    assertEquals(1, fixture.query.getContent().getAggregation().getAggregations().size());
    assertEquals("max", fixture.query.getContent().getAggregation().getAggregations().get(0).getFunction());
    assertEquals("max", fixture.savedContent().get("aggregation").get("aggregations").get(0).get("function").asString());
  }

  @Test
  void groupAndAggregationRowsEditTheModelAndTheFile() throws Exception {
    Fixture fixture = fixture();
    fixture.onFx(() -> fixture.switchField().setSelected(true));

    fixture.addGroup();
    fixture.onFx(() -> fixture.groupRow(0).field().setValue(ENUMERATION));
    fixture.addAggregation();
    fixture.onFx(() -> {
      Fixture.EntryRow row = fixture.entryRow(0);
      row.function().setValue(QueryAggregationEntry.FUNCTION_SUM);
      row.field().setValue(NUMBER);
      row.alias().setText("total");
    });

    QueryAggregation aggregation = fixture.query.getContent().getAggregation();
    assertEquals(ENUMERATION, aggregation.getGroup().get(0).getField());
    QueryAggregationEntry entry = aggregation.getAggregations().get(0);
    assertEquals("sum", entry.getFunction());
    assertEquals(NUMBER, entry.getField());
    assertEquals("total", entry.getAlias());

    Thread.sleep(600); // the alias is saved debounced
    fixture.onFx(() -> { });
    JsonNode saved = fixture.savedContent().get("aggregation");
    assertEquals(ENUMERATION, saved.get("group").get(0).get("field").asString());
    assertEquals("sum", saved.get("aggregations").get(0).get("function").asString());
    assertEquals(NUMBER, saved.get("aggregations").get(0).get("field").asString());
    assertEquals("total", saved.get("aggregations").get(0).get("alias").asString());
    assertFalse(fixture.savedContent().has("aggregateResults"));
  }

  @Test
  void aBlankAliasIsNotWritten() throws Exception {
    Fixture fixture = fixture();
    fixture.onFx(() -> fixture.switchField().setSelected(true));
    fixture.addAggregation();
    fixture.onFx(() -> {
      fixture.entryRow(0).alias().setText("x");
      fixture.entryRow(0).alias().setText("");
    });

    assertNull(fixture.query.getContent().getAggregation().getAggregations().get(0).getAlias());
  }

  @Test
  void removingARowDropsItsEntry() throws Exception {
    Fixture fixture = fixture();
    fixture.onFx(() -> fixture.switchField().setSelected(true));
    fixture.addGroup();
    fixture.addAggregation();

    fixture.onFx(() -> fixture.groupRow(0).remove().fire());
    fixture.onFx(() -> fixture.entryRow(0).remove().fire());

    assertTrue(fixture.query.getContent().getAggregation().getGroup().isEmpty());
    assertTrue(fixture.query.getContent().getAggregation().getAggregations().isEmpty());
    assertEquals(0, fixture.groupRows().getChildren().size());
    assertEquals(0, fixture.aggregationRows().getChildren().size());
  }

  @Test
  void groupChoicesAreTheEligibleFieldsAndAggregationChoicesFollowTheFunction() throws Exception {
    Fixture fixture = fixture();
    fixture.onFx(() -> fixture.switchField().setSelected(true));
    fixture.addGroup();
    fixture.addAggregation();

    List<String> groupChoices = fixture.groupRow(0).field().getItems();
    assertTrue(groupChoices.containsAll(List.of(NUMBER, STRING, ENUMERATION)), groupChoices.toString());

    // count (the default of a new entry) fits every type ...
    assertTrue(fixture.entryRow(0).field().getItems().containsAll(List.of(NUMBER, STRING, ENUMERATION)));
    // ... sum only numbers
    fixture.onFx(() -> fixture.entryRow(0).function().setValue("sum"));
    List<String> sumChoices = fixture.entryRow(0).field().getItems();
    assertTrue(sumChoices.contains(NUMBER), sumChoices.toString());
    assertFalse(sumChoices.contains(STRING), sumChoices.toString());
    assertFalse(sumChoices.contains(ENUMERATION), sumChoices.toString());
  }

  @Test
  void aFieldThatDoesNotFitAnyMoreStaysSelectedAndIsFlagged() throws Exception {
    Fixture fixture = fixture();
    fixture.onFx(() -> fixture.switchField().setSelected(true));
    fixture.addAggregation();
    fixture.onFx(() -> fixture.entryRow(0).field().setValue(STRING));
    assertFalse(fixture.entryRow(0).field().getStyleClass().contains("validation-error"));

    fixture.onFx(() -> fixture.entryRow(0).function().setValue("sum"));

    // not undone: the value is kept (and marked), the file keeps what the user chose
    assertEquals(STRING, fixture.query.getContent().getAggregation().getAggregations().get(0).getField());
    assertEquals(STRING, fixture.entryRow(0).field().getValue());
    assertTrue(fixture.entryRow(0).field().getStyleClass().contains("validation-error"));

    fixture.onFx(() -> fixture.entryRow(0).function().setValue("count"));
    assertFalse(fixture.entryRow(0).field().getStyleClass().contains("validation-error"));
  }

  @Test
  void aStoredFieldThatNoLongerResolvesIsShownAndFlaggedNotDropped() throws Exception {
    Fixture fixture = fixture();
    QueryAggregation aggregation = new QueryAggregation();
    aggregation.getGroup().add(new QueryAggregationGroup("/Person/Gone"));
    fixture.query.getContent().setAggregation(aggregation);

    fixture.onFx(() -> fixture.controller.load(fixture.item, fixture.query));

    assertTrue(fixture.switchField().isSelected());
    assertEquals("/Person/Gone", fixture.groupRow(0).field().getValue());
    assertTrue(fixture.groupRow(0).field().getStyleClass().contains("validation-error"));
    assertEquals("/Person/Gone", fixture.query.getContent().getAggregation().getGroup().get(0).getField());
  }

  @Test
  void thePostProcessingTabEmbedsAndLoadsThePanel() throws Exception {
    Fixture fixture = fixture();
    QueryAggregation aggregation = new QueryAggregation();
    aggregation.getGroup().add(new QueryAggregationGroup(ENUMERATION));
    fixture.query.getContent().setAggregation(aggregation);

    FxTestSupport.Loaded<PostProcessingPanelController> tab =
        FxTestSupport.load("/de/a12/studio/ui/editors/querymodel/post-processing-panel.fxml");
    FxTestSupport.onFx(() -> tab.controller().load(fixture.item, fixture.query));

    QueryAggregationPanelController embedded = FxTestSupport.field(tab.controller(), "queryAggregationPanelController");
    assertNotNull(embedded);
    CheckBox embeddedSwitch = FxTestSupport.field(embedded, "aggregateResultsField");
    assertTrue(embeddedSwitch.isSelected());
    VBox rows = FxTestSupport.field(embedded, "groupRows");
    assertEquals(1, rows.getChildren().size());
  }

  /** Read from the property files (StudioBundle follows the JVM's locale, so it cannot show both). */
  @Test
  void everyKeyThePanelUsesIsDefinedInBothLanguages() throws Exception {
    List<String> keys = List.of("aggregation", "aggregate_results", "field", "alias", "query_aggregation.aggregate_results_hint",
        "query_aggregation.sort_paging_ignored", "query_aggregation.group_title", "query_aggregation.no_group",
        "query_aggregation.add_group", "query_aggregation.remove_group", "query_aggregation.aggregations_title",
        "query_aggregation.no_aggregations", "query_aggregation.add_aggregation", "query_aggregation.remove_aggregation",
        "query_aggregation.function", "query_aggregation.select_field");
    List<String> functions = QueryAggregationEntry.FUNCTIONS.stream().map(function -> "query_aggregation.function." + function).toList();

    for (String bundle : List.of("/messages.properties", "/messages_de.properties")) {
      Properties properties = new Properties();
      try (InputStream in = getClass().getResourceAsStream(bundle)) {
        assertNotNull(in, bundle);
        properties.load(in);
      }
      for (String key : keys) {
        assertTrue(properties.getProperty(key, "").length() > 0, bundle + " is missing " + key);
      }
      for (String key : functions) {
        assertTrue(properties.getProperty(key, "").length() > 0, bundle + " is missing " + key);
      }
    }
  }

  // ---- fixture ----------------------------------------------------------------------------------------------

  @FunctionalInterface
  private interface Action {
    void run() throws Exception;
  }

  private final class Fixture {
    final QueryAggregationPanelController controller;
    final ProjectItem item;
    final QueryModel query;
    final Object root;

    Fixture(QueryAggregationPanelController controller, Object root, ProjectItem item, QueryModel query) {
      this.controller = controller;
      this.root = root;
      this.item = item;
      this.query = query;
    }

    /** Runs {@code action} on the JavaFX thread; unlike a Runnable it may throw (the accessors below do). */
    void onFx(Action action) throws Exception {
      FxTestSupport.onFx((Callable<Object>) () -> {
        action.run();
        return null;
      });
    }

    CheckBox switchField() throws Exception {
      return FxTestSupport.field(controller, "aggregateResultsField");
    }

    VBox aggregationBox() throws Exception {
      return FxTestSupport.field(controller, "aggregationBox");
    }

    VBox groupRows() throws Exception {
      return FxTestSupport.field(controller, "groupRows");
    }

    VBox aggregationRows() throws Exception {
      return FxTestSupport.field(controller, "aggregationRows");
    }

    /** The "Add ..." buttons are only reachable through the TitledPane's skin (there is no scene here), so their
     * {@code onAction} handlers are invoked directly. */
    void addGroup() throws Exception {
      invoke("onAddGroup");
    }

    void addAggregation() throws Exception {
      invoke("onAddAggregation");
    }

    private void invoke(String handler) throws Exception {
      FxTestSupport.onFx((Callable<Object>) () -> {
        Method method = QueryAggregationPanelController.class.getDeclaredMethod(handler);
        method.setAccessible(true);
        method.invoke(controller);
        return null;
      });
    }

    record GroupRow(ComboBox<String> field, Button remove) {
    }

    record EntryRow(ComboBox<String> function, ComboBox<String> field, TextField alias, Button remove) {
    }

    @SuppressWarnings("unchecked")
    GroupRow groupRow(int index) throws Exception {
      HBox row = (HBox) groupRows().getChildren().get(index);
      return new GroupRow((ComboBox<String>) row.getChildren().get(0), (Button) row.getChildren().get(1));
    }

    @SuppressWarnings("unchecked")
    EntryRow entryRow(int index) throws Exception {
      HBox row = (HBox) aggregationRows().getChildren().get(index);
      return new EntryRow((ComboBox<String>) row.getChildren().get(0), (ComboBox<String>) row.getChildren().get(1),
          (TextField) row.getChildren().get(2), (Button) row.getChildren().get(3));
    }

    /** The {@code content} of the file as it is on disk right now. */
    JsonNode savedContent() throws IOException {
      return JsonSettings.objectMapper.readTree(Files.readString(Path.of(item.getPath()))).get("content");
    }
  }

  private Fixture fixture() throws Exception {
    assumeTrue(toolkitAvailable, "No JavaFX toolkit available");
    Path source = locateWorkspace().resolve("models").resolve("10_People");
    Path models = Files.createDirectories(workspace.resolve("models"));
    Files.copy(source.resolve("Person_Dc.json"), models.resolve("Person_Dc.json"));
    Files.copy(source.resolve(QUERY + ".json"), models.resolve(QUERY + ".json"));

    Project project = new Project();
    project.load(workspace.toFile());
    setCurrentProject(project);
    ProjectItem item = project.getRoot().findByPath(models.resolve(QUERY + ".json").toString());
    if (item == null) {
      item = new ProjectItem(models.resolve(QUERY + ".json").toFile());
    }
    FxTestSupport.selectProjectItem(item);
    QueryModel query = (QueryModel) item.getModel();

    FxTestSupport.Loaded<QueryAggregationPanelController> loaded =
        FxTestSupport.load("/de/a12/studio/ui/editors/querymodel/aggregation-panel.fxml");
    ProjectItem queryItem = item;
    FxTestSupport.onFx(() -> loaded.controller().load(queryItem, query));
    openPanel = loaded.controller();
    return new Fixture(loaded.controller(), loaded.root(), item, query);
  }

  private static void setCurrentProject(Project project) throws Exception {
    Field currentProject = Studio.class.getDeclaredField("currentProject");
    currentProject.setAccessible(true);
    currentProject.set(null, project);
  }

  private static Path locateWorkspace() throws IOException {
    for (Path dir = Path.of("").toAbsolutePath(); dir != null; dir = dir.getParent()) {
      Path candidate = dir.resolve("testing").resolve("workspaces").resolve("advanced_new");
      if (Files.isDirectory(candidate)) {
        return candidate;
      }
    }
    throw new IllegalStateException("Could not locate 'testing/workspaces/advanced_new' above " + Path.of("").toAbsolutePath());
  }
}
