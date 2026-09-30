package de.a12.studio.ui.editors.maindetailmodel;

import de.a12.studio.models.ModelReference;
import de.a12.studio.models.ModelType;
import de.a12.studio.models.masterdetailmodel.FormMapping;
import de.a12.studio.models.masterdetailmodel.MasterDetailModel;
import de.a12.studio.models.overviewmodel.OverviewModel;
import de.a12.studio.models.projects.Project;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.modelsvalidation.ValidationService;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.editors.formmodel.FxTestSupport;
import de.a12.studio.ui.events.StudioEventManager;
import javafx.scene.control.ComboBox;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Gap 5 of "Master Detail Model: gap review" (docs/sme-reference-comparison.md): the Form Mapping panel must
 * not go stale while this editor's tab stays open. {@link MainDetailModelEditorController#modelSaved} extends
 * {@link de.a12.studio.ui.editors.AbstractEditorController}'s Document-Model-only dispatch to also react to the
 * currently selected Overview/Tree Model being saved in a different tab.
 */
class MainDetailModelEditorControllerTest {

  private static boolean toolkitAvailable;

  @BeforeAll
  static void startToolkit() throws Exception {
    toolkitAvailable = FxTestSupport.startToolkit();
  }

  @AfterEach
  void tearDown() throws Exception {
    if (toolkitAvailable) {
      setStatic("currentProject", new Project());
      setStatic("validationService", null);
    }
  }

  @Test
  void savingTheSelectedOverviewModelElsewhereRefreshesTheFormMappingPanelImmediately(@TempDir Path dir) throws Exception {
    assumeTrue(toolkitAvailable, "No JavaFX toolkit available");
    Files.writeString(dir.resolve("Order_DM.json"), documentModel("Order_DM"));
    Files.writeString(dir.resolve("Other_DM.json"), documentModel("Other_DM"));
    Files.writeString(dir.resolve("Team_Ov.json"), overviewModel("Team_Ov", "Order_DM"));
    Files.writeString(dir.resolve("Team_MDM.json"), masterDetailModel("Team_MDM", "Team_Ov"));

    Project project = new Project();
    project.load(dir.toFile());
    setStatic("currentProject", project);
    setStatic("validationService", new ValidationService(project));

    ProjectItem masterItem = project.getRoot().findByPath(dir.resolve("Team_MDM.json").toString());
    ProjectItem overviewItem = project.getRoot().findByPath(dir.resolve("Team_Ov.json").toString());

    FxTestSupport.Loaded<MainDetailModelEditorController> loaded =
        FxTestSupport.load("/de/a12/studio/ui/editors/maindetailmodel/main-detail-model-editor.fxml");
    MainDetailModelEditorController controller = loaded.controller();
    FxTestSupport.onFx(() -> controller.load(masterItem));

    MasterDetailModel masterModel = (MasterDetailModel) masterItem.getModel();
    assertEquals(List.of("Order_DM"), documentModelIds(masterModel));

    // In the real app this would be a second open tab: another Document Model reference is added to the
    // Overview Model currently selected as this module's master list, then saved - firing the same
    // ModelSaveEvent AbstractEditorController#modelSaved already listens for.
    OverviewModel overviewModel = (OverviewModel) overviewItem.getModel();
    ModelReference reference = new ModelReference();
    reference.setModelType(ModelType.DOCUMENT);
    reference.setPurpose(ModelReference.PURPOSE_DOCUMENT_MODEL_FOR_OVERVIEW);
    reference.setReference("Other_DM");
    overviewModel.getModelReferences().add(reference);
    FxTestSupport.onFx(() -> StudioEventManager.getInstance().fireModelSavedEvent(overviewItem));

    assertEquals(List.of("Order_DM", "Other_DM"), documentModelIds(masterModel));
  }

  // Gap 1 of "Master Detail Model: gap review": an abstract Document Model referenced by the master Overview
  // Model must not get its own Form Mapping row (nothing can be edited against an abstract type directly) -
  // it must be replaced by one row per concrete subtype, recursively, matching SME's
  // resolveAndFilterAbstractDocuments.
  @Test
  void anAbstractDocumentModelIsExpandedIntoItsConcreteSubtypesInTheFormMappingPanel(@TempDir Path dir) throws Exception {
    assumeTrue(toolkitAvailable, "No JavaFX toolkit available");
    Files.writeString(dir.resolve("Animal_DM.json"), documentModelWithAnnotations("Animal_DM", "{\"name\": \"abstract\", \"value\": \"true\"}"));
    Files.writeString(dir.resolve("Cat_DM.json"), documentModelWithAnnotations("Cat_DM", "{\"name\": \"superTypes\", \"value\": \"Animal_DM\"}"));
    Files.writeString(dir.resolve("Dog_DM.json"), documentModelWithAnnotations("Dog_DM", "{\"name\": \"superTypes\", \"value\": \"Animal_DM\"}"));
    Files.writeString(dir.resolve("Team_Ov.json"), overviewModel("Team_Ov", "Animal_DM"));
    Files.writeString(dir.resolve("Team_MDM.json"), masterDetailModel("Team_MDM", "Team_Ov"));

    Project project = new Project();
    project.load(dir.toFile());
    setStatic("currentProject", project);
    setStatic("validationService", new ValidationService(project));
    ProjectItem masterItem = project.getRoot().findByPath(dir.resolve("Team_MDM.json").toString());

    FxTestSupport.Loaded<MainDetailModelEditorController> loaded =
        FxTestSupport.load("/de/a12/studio/ui/editors/maindetailmodel/main-detail-model-editor.fxml");
    MainDetailModelEditorController controller = loaded.controller();
    FxTestSupport.onFx(() -> controller.load(masterItem));

    MasterDetailModel masterModel = (MasterDetailModel) masterItem.getModel();
    assertEquals(List.of("Cat_DM", "Dog_DM"), documentModelIds(masterModel));
  }

  // Same gap: a Composed Document Model reference must be replaced by its query root, matching SME's
  // resolveAndFilterAbstractDocuments.
  @Test
  void aComposedDocumentModelIsReplacedByItsQueryRootInTheFormMappingPanel(@TempDir Path dir) throws Exception {
    assumeTrue(toolkitAvailable, "No JavaFX toolkit available");
    Files.writeString(dir.resolve("Order_DM.json"), documentModel("Order_DM"));
    Files.writeString(dir.resolve("Composed_DM.json"),
        documentModelWithAnnotations("Composed_DM", "{\"name\": \"cdm.queryRoot\", \"value\": \"Order_DM\"}"));
    Files.writeString(dir.resolve("Team_Ov.json"), overviewModel("Team_Ov", "Composed_DM"));
    Files.writeString(dir.resolve("Team_MDM.json"), masterDetailModel("Team_MDM", "Team_Ov"));

    Project project = new Project();
    project.load(dir.toFile());
    setStatic("currentProject", project);
    setStatic("validationService", new ValidationService(project));
    ProjectItem masterItem = project.getRoot().findByPath(dir.resolve("Team_MDM.json").toString());

    FxTestSupport.Loaded<MainDetailModelEditorController> loaded =
        FxTestSupport.load("/de/a12/studio/ui/editors/maindetailmodel/main-detail-model-editor.fxml");
    MainDetailModelEditorController controller = loaded.controller();
    FxTestSupport.onFx(() -> controller.load(masterItem));

    MasterDetailModel masterModel = (MasterDetailModel) masterItem.getModel();
    assertEquals(List.of("Order_DM"), documentModelIds(masterModel));
  }

  // Gap 2 of "Master Detail Model: gap review": the BA doc is explicit that Binding Overview Models (here,
  // "Binding_Ov" - the Selected Items overview of a Relationship UI Model's TableList component) are excluded
  // from the master model combo, matching MainDetailModelEditorController#overviewModelOptions.
  @Test
  void theOverviewModelComboExcludesABindingOverviewModel(@TempDir Path dir) throws Exception {
    assumeTrue(toolkitAvailable, "No JavaFX toolkit available");
    Files.writeString(dir.resolve("Order_DM.json"), documentModel("Order_DM"));
    Files.writeString(dir.resolve("Team_Ov.json"), overviewModel("Team_Ov", "Order_DM"));
    Files.writeString(dir.resolve("Binding_Ov.json"), overviewModel("Binding_Ov", "Order_DM"));
    Files.writeString(dir.resolve("Person_Ru.json"), relationshipUiModel("Person_Ru", "Binding_Ov"));
    Files.writeString(dir.resolve("Team_MDM.json"), masterDetailModel("Team_MDM", "Team_Ov"));

    Project project = new Project();
    project.load(dir.toFile());
    setStatic("currentProject", project);
    setStatic("validationService", new ValidationService(project));
    ProjectItem masterItem = project.getRoot().findByPath(dir.resolve("Team_MDM.json").toString());

    FxTestSupport.Loaded<MainDetailModelEditorController> loaded =
        FxTestSupport.load("/de/a12/studio/ui/editors/maindetailmodel/main-detail-model-editor.fxml");
    MainDetailModelEditorController controller = loaded.controller();
    FxTestSupport.onFx(() -> controller.load(masterItem));

    MainModelReferencePanelController masterModelReferenceController = FxTestSupport.field(controller, "masterModelReferenceController");
    ComboBox<String> mainModelField = FxTestSupport.field(masterModelReferenceController, "mainModelField");
    assertEquals(List.of("Team_Ov"), mainModelField.getItems());
  }

  private static List<String> documentModelIds(MasterDetailModel model) {
    return model.getContent().getFormMapping().stream().map(FormMapping::getDocumentModel).toList();
  }

  private static void setStatic(String name, Object value) throws Exception {
    Field field = Studio.class.getDeclaredField(name);
    field.setAccessible(true);
    field.set(null, value);
  }

  private static String documentModel(String id) {
    return "{\"header\": {\"id\": \"" + id + "\", \"modelType\": \"document\", \"modelVersion\": \"29.4.0\","
        + " \"locales\": [{\"code\": \"en\"}], \"annotations\": [], \"modelReferences\": []},"
        + " \"content\": {\"modelInfo\": {\"name\": \"" + id + "\", \"immutable\": false},"
        + " \"modelConfig\": {\"timeZone\": \"UTC\", \"decimalSeparator\": \".\", \"conditionLanguage\": {\"code\": \"en_US\"}},"
        + " \"modelRoot\": {\"rootGroups\": [{\"type\": \"Group\", \"id\": \"g_root\", \"name\": \"Root\","
        + " \"Group\": {\"repeatability\": 1, \"elements\": []}}]}}}";
  }

  private static String documentModelWithAnnotations(String id, String annotationJson) {
    return "{\"header\": {\"id\": \"" + id + "\", \"modelType\": \"document\", \"modelVersion\": \"29.4.0\","
        + " \"locales\": [{\"code\": \"en\"}], \"annotations\": [" + annotationJson + "], \"modelReferences\": []},"
        + " \"content\": {\"modelInfo\": {\"name\": \"" + id + "\", \"immutable\": false},"
        + " \"modelConfig\": {\"timeZone\": \"UTC\", \"decimalSeparator\": \".\", \"conditionLanguage\": {\"code\": \"en_US\"}},"
        + " \"modelRoot\": {\"rootGroups\": [{\"type\": \"Group\", \"id\": \"g_root\", \"name\": \"Root\","
        + " \"Group\": {\"repeatability\": 1, \"elements\": []}}]}}}";
  }

  private static String overviewModel(String id, String documentModelId) {
    return "{\"header\": {\"id\": \"" + id + "\", \"modelType\": \"overview\", \"modelVersion\": \"39.0.0\","
        + " \"modelReferences\": [{\"modelType\": \"document\", \"purpose\": \"document-model-for-overview\","
        + " \"reference\": \"" + documentModelId + "\"}]},"
        + " \"content\": {\"configuration\": {}, \"columns\": []}}";
  }

  private static String relationshipUiModel(String id, String selectedItemsOverviewModel) {
    return "{\"header\": {\"id\": \"" + id + "\", \"modelType\": \"relationship-ui\", \"modelVersion\": \"1.0.0\"},"
        + " \"content\": {\"component\": {\"componentType\": \"TableList\", \"selectedItemsOverviewModel\": \""
        + selectedItemsOverviewModel + "\"}, \"relationshipName\": \"r\", \"targetRole\": \"role\"}}";
  }

  private static String masterDetailModel(String id, String overviewModelId) {
    return "{\"header\": {\"id\": \"" + id + "\", \"modelType\": \"module-masterdetail\", \"modelVersion\": \"1.0.0\"},"
        + " \"content\": {\"type\": \"overview\", \"overviewModel\": \"" + overviewModelId + "\", \"formMapping\": []}}";
  }
}
