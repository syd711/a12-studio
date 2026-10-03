package de.a12.studio.ui.editors.structuralmappingmodel;

import de.a12.studio.models.ModelType;
import de.a12.studio.models.projects.Project;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.models.structuralmappingmodel.SmmElement;
import de.a12.studio.models.structuralmappingmodel.StructuralMappingModel;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.editors.formmodel.FxTestSupport;
import de.a12.studio.ui.events.StudioEventManager;
import de.a12.studio.ui.util.StudioBundle;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableView;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * The Structural Mapping Model editor: its FXML wiring, the source and target tree it fills from the kernel's
 * source and target model of the Mapping Model that uses the model, one tag column per mapping block, and what
 * happens when a field mapping is added (what dropping a source field on a target field does).
 */
class StructuralMappingModelEditorTest {

  private static final String EDITOR_FXML = "/de/a12/studio/ui/editors/structuralmappingmodel/structural-mapping-model-editor.fxml";

  private static boolean toolkitAvailable;

  @TempDir
  Path workspace;

  private ProjectItem item;
  private StructuralMappingModel model;

  private static String documentModel(String id, String rootGroup) {
    return """
        {"header":{"id":"%s","modelType":"document","modelVersion":"29.4.0","locales":[{"code":"en"}],"modelReferences":[]},
         "content":{"modelInfo":{"name":"%s","immutable":false},
           "modelConfig":{"timeZone":"UTC","decimalSeparator":".","conditionLanguage":{"code":"en_US"}},
           "modelRoot":{"rootGroups":[
             {"type":"Group","id":"G1","name":"%s","Group":{"repeatability":1,"elements":[
               {"type":"Field","id":"F1","name":"Name","Field":{"fieldType":{"type":"StringType"}}},
               {"type":"Group","id":"G2","name":"Addresses","Group":{"repeatability":5,"elements":[
                 {"type":"Field","id":"F2","name":"City","Field":{"fieldType":{"type":"StringType"}}}]}}]}}]}}}"""
        .formatted(id, id, rootGroup);
  }

  private static final String MAPPING_MODEL = """
      {"header":{"id":"Test_Ma","modelType":"mapping","modelVersion":"1.0.0","locales":[{"code":"en"}],"modelReferences":[
          {"modelType":"document","reference":"Source_DM"},{"modelType":"document","reference":"Target_DM"},
          {"modelType":"structuralmapping","reference":"Test_SMM"}]},
       "content":{"Source":[{"dmId":"Source_DM","name":"Src","maxRepeat":1,"includeLevel":"MODEL_ROOT"}],
         "Target":{"dmId":"Target_DM"},"StructuralMappingModel":{"id":"Test_SMM"}}}""";

  private static final String SMM = """
      {"header":{"id":"Test_SMM","modelType":"structuralmapping","modelVersion":"1.0.0","locales":[{"code":"en"}],"modelReferences":[]},
       "content":{"GroupsToClearOnFirstFill":[],"MappingBlocks":[{"ResolutionStrategies":[
           {"type":"Fold","sourceGroupFullName":"/Src/Person/Addresses","targetGroupFullName":"/Employee/Addresses"}],
         "FieldMappings":[
           {"sourceFieldFullName":"/Src/Person/Addresses/City","targetFieldFullName":"/Employee/Addresses/City"}]}]}}""";

  @BeforeAll
  static void startToolkit() throws Exception {
    toolkitAvailable = FxTestSupport.startToolkit();
  }

  @AfterAll
  static void restoreStudio() throws Exception {
    if (toolkitAvailable) {
      setStatic("currentProject", new Project());
      setStatic("validationService", null);
    }
  }

  @BeforeEach
  void openProject() throws Exception {
    assumeTrue(toolkitAvailable, "No JavaFX toolkit available");
    Files.writeString(workspace.resolve("Source_DM.json"), documentModel("Source_DM", "Person"));
    Files.writeString(workspace.resolve("Target_DM.json"), documentModel("Target_DM", "Employee"));
    Files.writeString(workspace.resolve("Test_Ma.json"), MAPPING_MODEL);
    Files.writeString(workspace.resolve("Test_SMM.json"), SMM);
    openItem();
  }

  private void openItem() throws Exception {
    Project project = new Project();
    project.load(workspace.toFile());
    setStatic("currentProject", project);
    FxTestSupport.setValidationServiceForProject(project);
    item = new ProjectItem(workspace.resolve("Test_SMM.json").toFile());
    model = (StructuralMappingModel) item.getModel();
    assertNotNull(model, "the fixture structural mapping model must load");
    FxTestSupport.selectProjectItem(item);
  }

  @AfterEach
  void tearDown() throws Exception {
    if (!toolkitAvailable) {
      return;
    }
    if (item != null) {
      StudioEventManager.getInstance().fireModelClosedEvent(item);
    }
    FxTestSupport.onFx(() -> {
    });
    FxTestSupport.clearValidationService();
    FxTestSupport.selectProjectItem(null);
  }

  private StructuralMappingModelEditorController openEditor() throws Exception {
    FxTestSupport.Loaded<StructuralMappingModelEditorController> loaded = FxTestSupport.load(EDITOR_FXML);
    FxTestSupport.onFx(() -> loaded.controller().load(item));
    return loaded.controller();
  }

  private static TreeTableView<SmmElement> tree(StructuralMappingModelEditorController editor, String pane) {
    try {
      SmmTreePaneController controller = FxTestSupport.field(editor, pane);
      return controller.getTree();
    }
    catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }

  private static void awaitCondition(BooleanSupplier condition) throws Exception {
    long deadline = System.currentTimeMillis() + 20_000;
    while (System.currentTimeMillis() < deadline) {
      if (FxTestSupport.onFx(() -> condition.getAsBoolean())) {
        return;
      }
      Thread.sleep(50);
    }
    throw new AssertionError("condition not met in time");
  }

  private static List<String> paths(TreeItem<SmmElement> root) {
    List<String> paths = new ArrayList<>();
    if (root != null) {
      for (TreeItem<SmmElement> child : root.getChildren()) {
        paths.add(child.getValue().fullName());
        paths.addAll(paths(child));
      }
    }
    return paths;
  }

  @Test
  void showsTheSourceAndTargetModelOfTheMappingModelThatUsesTheModel() throws Exception {
    StructuralMappingModelEditorController editor = openEditor();
    assertEquals(ModelType.STRUCTURALMAPPING, editor.getModelType());

    awaitCondition(() -> !paths(tree(editor, "sourcePaneController").getRoot()).isEmpty());

    List<String> source = FxTestSupport.onFx(() -> paths(tree(editor, "sourcePaneController").getRoot()));
    List<String> target = FxTestSupport.onFx(() -> paths(tree(editor, "targetPaneController").getRoot()));
    assertTrue(source.contains("/Src/Person/Name"), source::toString);
    assertTrue(source.contains("/Src/Person/Addresses/City"), source::toString);
    assertTrue(target.contains("/Employee/Addresses"), target::toString);
    Label context = FxTestSupport.field(editor, "contextLabel");
    assertTrue(FxTestSupport.onFx(context::getText).contains("Test_Ma"), () -> context.getText());
  }

  @Test
  void theTargetTreeHasAClearColumnAndOneTagColumnPerMappingBlock() throws Exception {
    StructuralMappingModelEditorController editor = openEditor();
    awaitCondition(() -> tree(editor, "targetPaneController").getColumns().size() == 3);

    List<String> titles = FxTestSupport.onFx(() -> tree(editor, "targetPaneController").getColumns().stream().map(c -> c.getText()).toList());
    assertEquals(List.of(StudioBundle.get("structural_mapping.name"), StudioBundle.get("structural_mapping.clear"),
        StudioBundle.get("structural_mapping.mapping_block", 1)), titles);
  }

  @Test
  void droppingASourceFieldOnATargetFieldAddsTheFieldMappingAndSavesIt() throws Exception {
    StructuralMappingModelEditorController editor = openEditor();
    awaitCondition(() -> !paths(tree(editor, "targetPaneController").getRoot()).isEmpty());

    Method addFieldMapping = StructuralMappingModelEditorController.class.getDeclaredMethod("addFieldMapping", String.class, String.class);
    addFieldMapping.setAccessible(true);
    FxTestSupport.onFx(() -> addFieldMapping.invoke(editor, "/Src/Person/Name", "/Employee/Name"));

    boolean mapped = model.getContent().getMappingBlocks().stream().flatMap(block -> block.getFieldMappings().stream())
        .anyMatch(fieldMapping -> "/Employee/Name".equals(fieldMapping.getTargetFieldFullName())
            && "/Src/Person/Name".equals(fieldMapping.getSourceFieldFullName()));
    assertTrue(mapped, "the dropped mapping is in the model");
    // The change is on disk, too.
    assertTrue(Files.readString(workspace.resolve("Test_SMM.json")).contains("/Employee/Name"));
  }

  @Test
  void movingAFieldMappingToANewBlockTakesItAndItsStrategyOutOfTheOldOne() throws Exception {
    Files.writeString(workspace.resolve("Test_SMM.json"), """
        {"header":{"id":"Test_SMM","modelType":"structuralmapping","modelVersion":"1.0.0","locales":[{"code":"en"}],"modelReferences":[]},
         "content":{"GroupsToClearOnFirstFill":[],"MappingBlocks":[{"ResolutionStrategies":[
             {"type":"Fold","sourceGroupFullName":"/Src/Person/Addresses","targetGroupFullName":"/Employee/Addresses"}],
           "FieldMappings":[
             {"sourceFieldFullName":"/Src/Person/Name","targetFieldFullName":"/Employee/Name"},
             {"sourceFieldFullName":"/Src/Person/Addresses/City","targetFieldFullName":"/Employee/Addresses/City"}]}]}}""");
    openItem();
    StructuralMappingModelEditorController editor = openEditor();
    awaitCondition(() -> !paths(tree(editor, "targetPaneController").getRoot()).isEmpty());

    var cityNode = de.a12.studio.models.structuralmappingmodel.SmmOperations.trees(model.getContent()).get(0).nodes().stream()
        .filter(node -> node.isFieldMapping() && node.targetPath().contains("City")).findFirst().orElseThrow();
    Field contextField = StructuralMappingModelEditorController.class.getDeclaredField("context");
    contextField.setAccessible(true);
    var context = (de.a12.studio.modelsvalidation.kernel.StructuralMappingContext) FxTestSupport.onFx(() -> contextField.get(editor));
    var options = context.moveOptions(model, "/Src/Person/Addresses/City", "/Employee/Addresses/City");
    var newBlock = options.stream().filter(option -> option.mappingBlock() == 1).findFirst().orElseThrow();

    Method moveTo = StructuralMappingModelEditorController.class.getDeclaredMethod("moveTo",
        de.a12.studio.models.structuralmappingmodel.SmmNode.class, de.a12.studio.models.structuralmappingmodel.StructuralMappingModelContent.class);
    moveTo.setAccessible(true);
    FxTestSupport.onFx(() -> moveTo.invoke(editor, cityNode, newBlock.modified()));

    var blocks = model.getContent().getMappingBlocks();
    assertEquals(2, blocks.size(), "the old block stays for the name, the moved mapping got a block of its own");
    assertEquals(List.of("/Employee/Name"), blocks.get(0).getFieldMappings().stream().map(f -> f.getTargetFieldFullName()).toList());
    assertTrue(blocks.get(0).getResolutionStrategies().isEmpty(), "the strategy of the moved mapping left with it");
    assertEquals(List.of("/Employee/Addresses/City"), blocks.get(1).getFieldMappings().stream().map(f -> f.getTargetFieldFullName()).toList());
    assertEquals(1, blocks.get(1).getResolutionStrategies().size());
  }

  @Test
  void aMappingOntoAMissingTargetFieldIsOfferedForRemoval() throws Exception {
    Files.writeString(workspace.resolve("Test_SMM.json"), """
        {"header":{"id":"Test_SMM","modelType":"structuralmapping","modelVersion":"1.0.0","locales":[{"code":"en"}],"modelReferences":[]},
         "content":{"GroupsToClearOnFirstFill":[{"fullName":"/Employee/Gone"}],"MappingBlocks":[{"FieldMappings":[
           {"sourceFieldFullName":"/Src/Person/Name","targetFieldFullName":"/Employee/Name"},
           {"sourceFieldFullName":"/Src/Person/Name","targetFieldFullName":"/Employee/Nope"}]}]}}""");
    openItem();
    StructuralMappingModelEditorController editor = openEditor();
    awaitCondition(() -> !paths(tree(editor, "targetPaneController").getRoot()).isEmpty());

    Method invalidElements = StructuralMappingModelEditorController.class.getDeclaredMethod("invalidElements");
    invalidElements.setAccessible(true);
    @SuppressWarnings("unchecked")
    List<String> invalid = FxTestSupport.onFx(() -> (List<String>) invalidElements.invoke(editor));

    assertEquals(2, invalid.size(), invalid::toString);
    assertTrue(invalid.stream().anyMatch(line -> line.contains("/Employee/Nope")), invalid::toString);
    assertTrue(invalid.contains("/Employee/Gone"), invalid::toString);
    javafx.scene.control.Button button = FxTestSupport.field(editor, "removeInvalidButton");
    assertTrue(FxTestSupport.onFx(button::isVisible));
  }

  @Test
  void aModelNoMappingModelUsesSaysSoAndShowsNoTrees() throws Exception {
    Files.delete(workspace.resolve("Test_Ma.json"));
    openItem();

    StructuralMappingModelEditorController editor = openEditor();

    Label context = FxTestSupport.field(editor, "contextLabel");
    assertEquals(StudioBundle.get("structural_mapping.no_owner"), FxTestSupport.onFx(context::getText));
    assertTrue(FxTestSupport.onFx(() -> paths(tree(editor, "sourcePaneController").getRoot()).isEmpty()));
  }

  private static void setStatic(String name, Object value) throws Exception {
    Field field = Studio.class.getDeclaredField(name);
    field.setAccessible(true);
    field.set(null, value);
  }
}
