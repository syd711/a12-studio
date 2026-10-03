package de.a12.studio.ui.editors.structuralmappingmodel.dialogs;

import de.a12.studio.models.mappingmodel.MappingModel;
import de.a12.studio.models.projects.Project;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.models.structuralmappingmodel.ResolutionStrategy;
import de.a12.studio.models.structuralmappingmodel.ResolutionStrategyType;
import de.a12.studio.models.structuralmappingmodel.SmmNode;
import de.a12.studio.models.structuralmappingmodel.SmmOperations;
import de.a12.studio.models.structuralmappingmodel.StructuralMappingModel;
import de.a12.studio.modelsvalidation.kernel.StructuralMappingContext;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.editors.formmodel.FxTestSupport;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * The resolution strategy dialog: it offers what the kernel says is valid for the strategy, shows the slice
 * fields only for a Slice, and writes the strategy only on OK.
 */
class ResolutionStrategyDialogTest {

  private static final String DIALOG_FXML = "/de/a12/studio/ui/editors/structuralmappingmodel/dialogs/resolution-strategy-dialog.fxml";

  private static String documentModel(String id, String rootGroup) {
    return """
        {"header":{"id":"%s","modelType":"document","modelVersion":"29.4.0","locales":[{"code":"en"}],"modelReferences":[]},
         "content":{"modelInfo":{"name":"%s","immutable":false},
           "modelConfig":{"timeZone":"UTC","decimalSeparator":".","conditionLanguage":{"code":"en_US"}},
           "modelRoot":{"rootGroups":[
             {"type":"Group","id":"G1","name":"%s","Group":{"repeatability":1,"elements":[
               {"type":"Group","id":"G2","name":"Addresses","Group":{"repeatability":5,"elements":[
                 {"type":"Field","id":"F2","name":"City","Field":{"fieldType":{"type":"StringType"}}},
                 {"type":"Field","id":"F3","name":"Street","Field":{"fieldType":{"type":"StringType"}}}]}}]}}]}}}"""
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

  private static boolean toolkitAvailable;

  @TempDir
  Path workspace;

  private StructuralMappingContext context;
  private StructuralMappingModel model;
  private SmmNode strategyNode;

  @BeforeAll
  static void startToolkit() throws Exception {
    toolkitAvailable = FxTestSupport.startToolkit();
  }

  @AfterAll
  static void restoreStudio() throws Exception {
    if (toolkitAvailable) {
      Field field = Studio.class.getDeclaredField("currentProject");
      field.setAccessible(true);
      field.set(null, new Project());
    }
  }

  @BeforeEach
  void openProject() throws Exception {
    assumeTrue(toolkitAvailable, "No JavaFX toolkit available");
    Files.writeString(workspace.resolve("Source_DM.json"), documentModel("Source_DM", "Person"));
    Files.writeString(workspace.resolve("Target_DM.json"), documentModel("Target_DM", "Employee"));
    Files.writeString(workspace.resolve("Test_Ma.json"), MAPPING_MODEL);
    Files.writeString(workspace.resolve("Test_SMM.json"), SMM);
    ProjectItem root = new ProjectItem(workspace.toFile());
    model = (StructuralMappingModel) root.findByModelId("Test_SMM").getModel();
    MappingModel mapping = (MappingModel) root.findByModelId("Test_Ma").getModel();
    context = StructuralMappingContext.resolve(root, mapping);
    strategyNode = SmmOperations.trees(model.getContent()).get(0).roots().get(0);
  }

  private ResolutionStrategyDialogController open() throws Exception {
    FxTestSupport.Loaded<ResolutionStrategyDialogController> loaded = FxTestSupport.load(DIALOG_FXML);
    Method init = ResolutionStrategyDialogController.class.getDeclaredMethod("initDialog", Stage.class,
        StructuralMappingContext.class, StructuralMappingModel.class, SmmNode.class);
    init.setAccessible(true);
    FxTestSupport.onFx(() -> init.invoke(loaded.controller(), new Stage(), context, model, strategyNode));
    return loaded.controller();
  }

  @Test
  void offersTheValidTypesAndSourceGroupsOfTheStrategy() throws Exception {
    ResolutionStrategyDialogController dialog = open();

    ComboBox<ResolutionStrategyType> type = FxTestSupport.field(dialog, "typeCombo");
    ComboBox<String> sourceGroup = FxTestSupport.field(dialog, "sourceGroupCombo");
    VBox sliceBox = FxTestSupport.field(dialog, "sliceBox");
    Button ok = FxTestSupport.field(dialog, "okButton");

    assertEquals(ResolutionStrategyType.FOLD, FxTestSupport.onFx(type::getValue));
    assertTrue(FxTestSupport.onFx(() -> type.getItems().containsAll(List.of(ResolutionStrategyType.FOLD, ResolutionStrategyType.SLICE))));
    assertEquals("/Src/Person/Addresses", FxTestSupport.onFx(sourceGroup::getValue));
    assertFalse(FxTestSupport.onFx(sliceBox::isVisible), "a Fold has no slice fields");
    assertFalse(FxTestSupport.onFx(ok::isDisable));
  }

  @Test
  void aSliceNeedsItsTwoFieldsAndIsWrittenOnlyOnOk() throws Exception {
    ResolutionStrategyDialogController dialog = open();
    ComboBox<ResolutionStrategyType> type = FxTestSupport.field(dialog, "typeCombo");
    ComboBox<String> sliceSource = FxTestSupport.field(dialog, "sliceSourceFieldCombo");
    ComboBox<String> sliceTarget = FxTestSupport.field(dialog, "sliceTargetFieldCombo");
    VBox sliceBox = FxTestSupport.field(dialog, "sliceBox");
    Button ok = FxTestSupport.field(dialog, "okButton");

    FxTestSupport.onFx(() -> type.setValue(ResolutionStrategyType.SLICE));

    assertTrue(FxTestSupport.onFx(sliceBox::isVisible));
    assertTrue(FxTestSupport.onFx(ok::isDisable), "without the slice fields the strategy is incomplete");
    assertTrue(FxTestSupport.onFx(() -> sliceSource.getItems().contains("/Src/Person/Addresses/City")),
        () -> "slice source fields: " + sliceSource.getItems());
    // City is the target of a field mapping, which the kernel does not allow as the lookup field of the slice as well.
    assertEquals(List.of("/Employee/Addresses/Street"), FxTestSupport.onFx(() -> List.copyOf(sliceTarget.getItems())));

    FxTestSupport.onFx(() -> {
      sliceSource.setValue("/Src/Person/Addresses/Street");
      sliceTarget.setValue("/Employee/Addresses/Street");
    });
    assertFalse(FxTestSupport.onFx(ok::isDisable));
    ResolutionStrategy strategy = strategyNode.resolutionStrategy();
    assertEquals(ResolutionStrategyType.FOLD, strategy.getType(), "nothing is written before OK");

    Method submit = ResolutionStrategyDialogController.class.getDeclaredMethod("onDialogSubmit");
    submit.setAccessible(true);
    FxTestSupport.onFx(() -> submit.invoke(dialog));

    assertEquals(ResolutionStrategyType.SLICE, strategy.getType());
    assertEquals("/Src/Person/Addresses/Street", strategy.getSlice().getSourceFieldFullName());
    assertEquals("/Employee/Addresses/Street", strategy.getSlice().getTargetFieldFullName());
    assertTrue(dialog.isConfirmed());
  }
}
