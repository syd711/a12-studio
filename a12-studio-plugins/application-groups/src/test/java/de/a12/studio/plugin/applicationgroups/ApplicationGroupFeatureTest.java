package de.a12.studio.plugin.applicationgroups;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.Annotation;
import de.a12.studio.models.ModelReference;
import de.a12.studio.models.applicationmodel.ApplicationModel;
import de.a12.studio.models.applicationmodel.ApplicationModelContent;
import de.a12.studio.models.applicationmodel.Directive;
import de.a12.studio.models.applicationmodel.Flow;
import de.a12.studio.models.applicationmodel.ModelDescriptor;
import de.a12.studio.models.applicationmodel.Module;
import de.a12.studio.models.applicationmodel.Scene;
import de.a12.studio.models.applicationmodel.SceneChange;
import de.a12.studio.models.applicationmodel.ViewAddDirective;
import de.a12.studio.models.formmodel.FormModel;
import de.a12.studio.models.querymodel.QueryModel;
import de.a12.studio.models.projects.Project;
import de.a12.studio.models.projects.ProjectItem;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Copies the real {@code testing/basic} sample project into a temp directory and applies
 * {@link ApplicationGroupFeature} against it, verifying the rename/annotation/re-run/reference-rewrite
 * behavior against real project fixtures (in particular {@code PreviewApp_AM.json}, an
 * {@code ApplicationModel} whose content directly embeds "Company_DM"/"Company_FM" style model-id
 * strings, and {@code Company_FM.json}, whose {@code header.modelReferences} already points at
 * {@code Company_DM}).
 */
class ApplicationGroupFeatureTest {

  @Test
  void prefixesEveryModelAndRewritesReferences(@TempDir Path tempDir) throws Exception {
    Path projectDir = copyBasicProject(tempDir);
    Project project = loadProject(projectDir);
    setGroupName(projectDir, "App");

    ApplicationGroupResult result = new ApplicationGroupFeature().apply(project);

    assertEquals("App", result.groupName());
    assertTrue(result.renamedCount() > 0);

    File modelsDir = new File(projectDir.toFile(), "models");
    File companyDm = new File(modelsDir, "App_Company_DM.json");
    assertTrue(companyDm.exists(), "Company_DM.json should have been renamed with the group prefix");

    A12Model<?> companyDmModel = new ProjectItem(companyDm).getModel();
    assertEquals("App_Company_DM", companyDmModel.getId());
    assertEquals("App", findAnnotation(companyDmModel, ApplicationGroupFeature.ANNOTATION_NAME));

    File companyFm = new File(modelsDir, "App_Company_FM.json");
    FormModel companyFmModel = (FormModel) new ProjectItem(companyFm).getModel();
    ModelReference dmReference = companyFmModel.getModelReferences().stream()
        .filter(reference -> "Company_DM".equals(reference.getAlias()))
        .findFirst()
        .orElseThrow(() -> new AssertionError("Expected Company_FM to still reference Company_DM by alias"));
    assertEquals("App_Company_DM", dmReference.getReference(), "header.modelReferences must be rewritten to the new id");

    // Company_FM's "bindingConfiguration" header annotation is an opaque JSON-encoded string (SME's
    // relationship-widget config), holding BindingModel entries like {"name": "PersonCompany_Person_..._OM",
    // "use": "link"}. SME matches that "name" byte-for-byte against the real target Overview Model's
    // header.id (no alias indirection), so it must be rewritten just like every other reference field, or
    // SME throws "Model info for X could not be determined" once the referenced Overview Model is renamed.
    String bindingConfiguration = findAnnotation(companyFmModel, "bindingConfiguration");
    assertTrue(bindingConfiguration.contains("\"name\":\"App_PersonCompany_Person_AvailableItems_OM\""),
        "bindingConfiguration's candidate BindingModel.name must be rewritten to the new id");
    assertTrue(bindingConfiguration.contains("\"name\":\"App_PersonCompany_Person_SelectedItems_OM\""),
        "bindingConfiguration's link BindingModel.name must be rewritten to the new id");
    assertFalse(bindingConfiguration.contains("\"name\":\"PersonCompany_Person_AvailableItems_OM\""),
        "No BindingModel.name should still carry the old bare id");
    assertFalse(bindingConfiguration.contains("\"name\":\"PersonCompany_Person_SelectedItems_OM\""),
        "No BindingModel.name should still carry the old bare id");
    assertTrue(bindingConfiguration.contains("\"name\":\"PersonCompanyDualPane\""),
        "The relationship widget's own component/detail \"name\" fields (not BindingModel entries) must stay untouched");

    File previewApp = new File(modelsDir, "App_PreviewApp_AM.json");
    assertTrue(previewApp.exists());
    ApplicationModel previewAppModel = (ApplicationModel) new ProjectItem(previewApp).getModel();
    List<ModelDescriptor> descriptors = collectModelDescriptors(previewAppModel.getContent());
    assertTrue(descriptors.stream().anyMatch(d -> "App_WelcomePage_CM".equals(d.getName())),
        "ModelDescriptor.name must be rewritten to the new id");
    assertTrue(descriptors.stream().anyMatch(d -> "App_Company_DM".equals(d.getDocumentModel())),
        "ModelDescriptor.documentModel must be rewritten to the new id");
    assertTrue(descriptors.stream().noneMatch(
        d -> "WelcomePage_CM".equals(d.getName()) || "Company_DM".equals(d.getDocumentModel())),
        "No ModelDescriptor should still carry an old bare id");

    // MatchCondition.mustEqual duplicates a referenced id as an incidental, unvalidated string (see
    // ApplicationGroupFeature's REFERENCE_FIELD_NAMES javadoc) and is intentionally left untouched.
    String previewAppJson = Files.readString(previewApp.toPath());
    assertTrue(previewAppJson.contains("\"mustEqual\": \"Company_DM\""),
        "MatchCondition.mustEqual is out of scope for the reference rewrite and must be left as-is");

    // QueryModel.json's content directly embeds both a bare targetDocumentModel id (Person_DM) and a
    // relationshipModel id nested inside its sort array (PersonCompany) - both real models in this fixture,
    // so both actually get renamed and both fields must follow.
    File queryModel = new File(modelsDir, "App_QueryModel.json");
    assertTrue(queryModel.exists());
    QueryModel queryModelModel = (QueryModel) new ProjectItem(queryModel).getModel();
    assertEquals("App_Person_DM", queryModelModel.getContent().getTargetDocumentModel(),
        "QueryModelContent.targetDocumentModel must be rewritten to the new id");
    assertEquals("App_PersonCompany", queryModelModel.getContent().getSort().get(0).getRelationshipModel(),
        "QuerySort.relationshipModel must be rewritten to the new id");
  }

  private static List<ModelDescriptor> collectModelDescriptors(ApplicationModelContent content) {
    List<ModelDescriptor> descriptors = new ArrayList<>();
    for (Module module : content.getModules()) {
      for (Flow flow : module.getFlows()) {
        for (Scene scene : flow.getScenes()) {
          SceneChange sceneChange = scene.getSceneChange();
          if (sceneChange != null) {
            collectDirectiveDescriptors(sceneChange.getOnEnter(), descriptors);
            collectDirectiveDescriptors(sceneChange.getOnExit(), descriptors);
          }
        }
      }
    }
    return descriptors;
  }

  private static void collectDirectiveDescriptors(List<Directive> directives, List<ModelDescriptor> descriptors) {
    for (Directive directive : directives) {
      if (directive instanceof ViewAddDirective viewAdd) {
        descriptors.addAll(viewAdd.getModels());
      }
    }
  }

  // Mapping Model, Master-Detail Model and Combined Document Model each hold model-id references that the
  // real testing/workspaces/basic fixtures don't otherwise exercise (Mapping Model isn't present there at
  // all), so this test writes minimal synthetic files directly into the copied temp project rather than
  // touching the shared checked-in fixtures.
  @Test
  void rewritesModelIdReferencesInMappingMasterDetailAndCombinedDocumentModels(@TempDir Path tempDir) throws Exception {
    Path projectDir = copyBasicProject(tempDir);
    File modelsDir = new File(projectDir.toFile(), "models");

    // TreeNode.id is a local element id, not a model reference - giving it the same value as a real model's
    // id ("Company_DM") checks that the generic field-name rewrite doesn't touch it by coincidence, while its
    // sibling documentModelRef (a genuine reference) does get rewritten.
    writeModel(modelsDir, "Extra_TrM.json", """
        {
          "header": {"id": "Extra_TrM", "modelType": "tree", "modelVersion": "1.0.0"},
          "content": {"nodes": [{"id": "Company_DM", "documentModelRef": "Company_DM"}]}
        }
        """);
    writeModel(modelsDir, "Extra_SmM.json", """
        {
          "header": {"id": "Extra_SmM", "modelType": "structuralmapping", "modelVersion": "1.0.0"},
          "content": {}
        }
        """);
    writeModel(modelsDir, "Extra_MdM.json", """
        {
          "header": {"id": "Extra_MdM", "modelType": "module-masterdetail", "modelVersion": "1.0.0"},
          "content": {"type": "tree", "treeModel": "Extra_TrM"}
        }
        """);
    writeModel(modelsDir, "Extra_MaM.json", """
        {
          "header": {"id": "Extra_MaM", "modelType": "mapping", "modelVersion": "1.0.0"},
          "content": {
            "Source": [{"name": "src", "dmId": "Company_DM"}],
            "Target": {"dmId": "Company_DM"},
            "PreComputationFragment": {"dmId": "Company_DM"},
            "OverallModel": {"dmId": "Company_DM"},
            "StructuralMappingModel": {"id": "Extra_SmM"}
          }
        }
        """);
    writeModel(modelsDir, "Extra_CmM.json", """
        {
          "header": {"id": "Extra_CmM", "modelType": "combination", "modelVersion": "1.0.0"},
          "content": {
            "baseModelId": "Company_DM",
            "CombinationSteps": [
              {"type": "Addition", "AdditiveModel": {"dmId": "Company_DM"}},
              {"type": "Selection", "SelectionModel": {"smId": "Company_DM"}}
            ]
          }
        }
        """);

    Project project = loadProject(projectDir);
    setGroupName(projectDir, "App");
    new ApplicationGroupFeature().apply(project);

    String treeModelJson = Files.readString(new File(modelsDir, "App_Extra_TrM.json").toPath());
    assertTrue(treeModelJson.contains("\"documentModelRef\": \"App_Company_DM\""),
        "TreeNode.documentModelRef must be rewritten to the new id");
    assertTrue(treeModelJson.contains("\"id\": \"Company_DM\""),
        "TreeNode.id is a local element id, not a model reference, and must be left untouched even though "
            + "it happens to equal an old model id");

    String masterDetailJson = Files.readString(new File(modelsDir, "App_Extra_MdM.json").toPath());
    assertTrue(masterDetailJson.contains("\"treeModel\": \"App_Extra_TrM\""),
        "MasterDetailModelContent.treeModel must be rewritten to the new id");

    String mappingJson = Files.readString(new File(modelsDir, "App_Extra_MaM.json").toPath());
    assertFalse(mappingJson.contains("\"dmId\": \"Company_DM\""),
        "No Mapping Model dmId reference (Target/Source/PreComputationFragment/OverallModel) should still carry the old bare id");
    assertTrue(mappingJson.contains("\"id\": \"App_Extra_SmM\""),
        "StructuralMappingModelRef.id must be rewritten to the new id");

    String combinedJson = Files.readString(new File(modelsDir, "App_Extra_CmM.json").toPath());
    assertTrue(combinedJson.contains("\"baseModelId\": \"App_Company_DM\""),
        "CombinedDocumentModelContent.baseModelId must be rewritten to the new id");
    assertTrue(combinedJson.contains("\"dmId\": \"App_Company_DM\""),
        "DocumentModelIdRef.dmId (AdditiveModel) must be rewritten to the new id");
    assertTrue(combinedJson.contains("\"smId\": \"App_Company_DM\""),
        "SelectionModelIdRef.smId must be rewritten to the new id");
  }

  private static void writeModel(File modelsDir, String fileName, String json) throws IOException {
    Files.writeString(new File(modelsDir, fileName).toPath(), json);
  }

  @Test
  void reapplyingWithSameGroupNameIsIdempotent(@TempDir Path tempDir) throws Exception {
    Path projectDir = copyBasicProject(tempDir);
    Project project = loadProject(projectDir);
    setGroupName(projectDir, "App");
    new ApplicationGroupFeature().apply(project);

    project.reload();
    ApplicationGroupResult second = new ApplicationGroupFeature().apply(project);

    assertEquals(0, second.renamedCount(), "Files already prefixed with the current group must not be renamed again");

    File modelsDir = new File(projectDir.toFile(), "models");
    assertTrue(new File(modelsDir, "App_Company_DM.json").exists());
    assertFalse(new File(modelsDir, "App_App_Company_DM.json").exists());

    A12Model<?> model = new ProjectItem(new File(modelsDir, "App_Company_DM.json")).getModel();
    assertEquals("App", findAnnotation(model, ApplicationGroupFeature.ANNOTATION_NAME));
  }

  @Test
  void reapplyingWithDifferentGroupNameStripsThePreviousPrefix(@TempDir Path tempDir) throws Exception {
    Path projectDir = copyBasicProject(tempDir);
    Project project = loadProject(projectDir);
    setGroupName(projectDir, "App");
    new ApplicationGroupFeature().apply(project);

    project.reload();
    setGroupName(projectDir, "Other");
    new ApplicationGroupFeature().apply(project);

    File modelsDir = new File(projectDir.toFile(), "models");
    assertTrue(new File(modelsDir, "Other_Company_DM.json").exists());
    assertFalse(new File(modelsDir, "App_Company_DM.json").exists());
    assertFalse(new File(modelsDir, "Other_App_Company_DM.json").exists());

    A12Model<?> model = new ProjectItem(new File(modelsDir, "Other_Company_DM.json")).getModel();
    assertEquals("Other_Company_DM", model.getId());
    assertEquals("Other", findAnnotation(model, ApplicationGroupFeature.ANNOTATION_NAME));
  }

  private static String findAnnotation(A12Model<?> model, String name) {
    return model.getAnnotations().stream()
        .filter(annotation -> name.equals(annotation.getName()))
        .map(Annotation::getValue)
        .findFirst()
        .orElse(null);
  }

  private static Project loadProject(Path projectDir) {
    Project project = new Project();
    project.load(projectDir.toFile());
    return project;
  }

  private static void setGroupName(Path projectDir, String groupName) {
    ApplicationGroupsSettings settings = ApplicationGroupsSettings.load(projectDir.toFile());
    settings.setApplicationGroupName(groupName);
    settings.save();
  }

  private static Path copyBasicProject(Path tempDir) throws IOException {
    Path source = resolveTestingBasicDir();
    Path projectDir = tempDir.resolve("basic");
    copyDirectory(source, projectDir);
    return projectDir;
  }

  private static Path resolveTestingBasicDir() {
    for (Path dir = Path.of("").toAbsolutePath(); dir != null; dir = dir.getParent()) {
      Path candidate = dir.resolve("testing").resolve("workspaces").resolve("basic");
      if (Files.isDirectory(candidate)) {
        return candidate;
      }
    }
    throw new IllegalStateException("Could not locate 'testing/workspaces/basic' above " + Path.of("").toAbsolutePath());
  }

  private static void copyDirectory(Path source, Path target) throws IOException {
    try (Stream<Path> walk = Files.walk(source)) {
      for (Path path : (Iterable<Path>) walk::iterator) {
        Path destination = target.resolve(source.relativize(path));
        if (Files.isDirectory(path)) {
          Files.createDirectories(destination);
        }
        else {
          Files.createDirectories(destination.getParent());
          Files.copy(path, destination, StandardCopyOption.COPY_ATTRIBUTES);
        }
      }
    }
  }
}
