package de.a12.studio.ui.editors.treemodel;

import de.a12.studio.models.projects.Project;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.ui.EditorFactory;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.editors.formmodel.FxTestSupport;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assumptions.assumeTrue;

class ScratchRestoreTest {
  @Test
  void buildRealTeamTr() throws Exception {
    assumeTrue(FxTestSupport.startToolkit(), "No JavaFX toolkit available");
    Project project = new Project();
    project.load(new File("C:/workspace/a12-studio/testing/workspaces/advanced_new"));
    Field f = Studio.class.getDeclaredField("currentProject");
    f.setAccessible(true);
    f.set(null, project);
    String path = new File("C:/workspace/a12-studio/testing/workspaces/advanced_new/models/20_Teams/Team_Tr.json").getPath();
    ProjectItem item = project.getRoot().findByPath(path);
    System.out.println("SCRATCH item=" + item + " supported=" + (item != null && item.isModelSupported()));
    FxTestSupport.selectProjectItem(item);
    Object content = FxTestSupport.onFx(() -> {
      try {
        return EditorFactory.create(item);
      }
      catch (Throwable t) {
        t.printStackTrace(System.out);
        throw t;
      }
    });
    System.out.println("SCRATCH content=" + content);
  }
}
