package de.a12.studio.ui.editors.formmodel.formtree;

import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.formmodel.FormModel;
import de.a12.studio.models.formmodel.Screen;
import de.a12.studio.models.projects.Project;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.editors.formmodel.FxTestSupport;
import de.a12.studio.ui.util.ProjectDocumentModels;
import de.a12.studio.ui.util.StudioBundle;
import de.a12.studio.ui.util.commandstack.CommandStack;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * A Form Model bound to a Combination Model (the "advanced_new" workspace's {@code PersonEmployee_Fm}, bound to
 * {@code PersonEmployee_Cm}) should still offer "Include Form Model..." on its screen - see TODO.md. Already fixed
 * as a side effect of {@code ProjectDocumentModels#resolveDocumentModelForFieldReferences}/{@code
 * getOtherDocumentModelsWithCombinations} (2026-09-20); this pins it so it stays fixed.
 */
class FormModelActionsCombinationIncludeMenuTest {

  private static boolean toolkitAvailable;

  @TempDir
  Path workspace;

  @BeforeAll
  static void startToolkit() throws Exception {
    toolkitAvailable = FxTestSupport.startToolkit();
  }

  @AfterAll
  static void restoreEmptyProject() throws Exception {
    if (toolkitAvailable) {
      setCurrentProject(new Project());
    }
  }

  @Test
  void aFormBoundToACombinationModelOffersToIncludeAFormModel() throws Exception {
    assumeTrue(toolkitAvailable, "No JavaFX toolkit available");
    Path source = locateWorkspace().resolve("models").resolve("10_People");
    Path models = Files.createDirectories(workspace.resolve("models").resolve("10_People"));
    for (String name : List.of("PersonEmployee_Fm.json", "PersonEmployee_Cm.json", "PersonEmployee_Ad.json", "Person_Dc.json")) {
      Files.copy(source.resolve(name), models.resolve(name));
    }

    Project project = new Project();
    project.load(workspace.toFile());
    setCurrentProject(project);

    ProjectItem item = project.getRoot().findByPath(models.resolve("PersonEmployee_Fm.json").toString());
    FormModel host = (FormModel) item.getModel();
    DocumentModel documentModel = ProjectDocumentModels.resolveDocumentModelForFieldReferences("PersonEmployee_Cm");

    FormModelActions actions = new FormModelActions(host.getContent(), new CommandStack(), node -> {
    }, item, documentModel);
    Screen screen = host.getContent().getScreens().get(0);

    List<String> onScreen = FxTestSupport.onFx(
        () -> actions.createAddMenuItems(new FormElementViewModel(screen, null, null)).stream().map(m -> m.getText()).toList());

    String label = StudioBundle.get("form_model_tree.include_form_model");
    assertTrue(onScreen.contains(label), onScreen.toString());

    // Also the source-filtering FormModelIncludeActions.includeInto relies on: the combination must resolve to
    // a stand-in DocumentModel carrying its own id, so a form bound to it is recognized as a valid include source.
    List<DocumentModel> withCombinations = ProjectDocumentModels.getOtherDocumentModelsWithCombinations(item);
    assertTrue(withCombinations.stream().anyMatch(model -> "PersonEmployee_Cm".equals(model.getId())), withCombinations.toString());
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
