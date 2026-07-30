package de.a12.studio.models.features;

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
    project.getSettings().getAdvancedSettings().setApplicationGroupName("App");

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

  @Test
  void reapplyingWithSameGroupNameIsIdempotent(@TempDir Path tempDir) throws Exception {
    Path projectDir = copyBasicProject(tempDir);
    Project project = loadProject(projectDir);
    project.getSettings().getAdvancedSettings().setApplicationGroupName("App");
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
    project.getSettings().getAdvancedSettings().setApplicationGroupName("App");
    new ApplicationGroupFeature().apply(project);

    project.reload();
    project.getSettings().getAdvancedSettings().setApplicationGroupName("Other");
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

  private static Path copyBasicProject(Path tempDir) throws IOException {
    Path source = resolveTestingBasicDir();
    Path projectDir = tempDir.resolve("basic");
    copyDirectory(source, projectDir);
    return projectDir;
  }

  private static Path resolveTestingBasicDir() {
    for (Path dir = Path.of("").toAbsolutePath(); dir != null; dir = dir.getParent()) {
      Path candidate = dir.resolve("testing").resolve("basic");
      if (Files.isDirectory(candidate)) {
        return candidate;
      }
    }
    throw new IllegalStateException("Could not locate 'testing/basic' above " + Path.of("").toAbsolutePath());
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
