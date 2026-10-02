package de.a12.studio.ui.editors.documentmodel;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.documentmodel.Element;
import de.a12.studio.models.documentmodel.GroupElement;
import de.a12.studio.models.documentmodel.RuleElement;
import de.a12.studio.models.projects.Project;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.components.ErrorContainerController;
import de.a12.studio.ui.editors.formmodel.FxTestSupport;
import de.a12.studio.ui.editors.propertyeditors.RuleEditorController;
import javafx.scene.control.Label;
import org.fxmisc.richtext.CodeArea;

/**
 * The validation rule editor's condition field with a real JavaFX toolkit: grammar errors come from the local syntax
 * check, semantic ones (unknown field, ...) from the A12 kernel, shown with line and column. Works on a copy of the
 * basic workspace's {@code Order_DM} ({@code rule_f2562}, {@code FieldFilled(OrderingDate) and [OrderingDate] > Today}),
 * since selecting a rule may save the model.
 */
class DocumentModelValidationRuleEditorKernelFxTest {

  private static final String RULE_ID = "rule_f2562";

  private static boolean toolkitAvailable;

  @TempDir
  Path workspace;

  @BeforeAll
  static void startToolkit() throws Exception {
    toolkitAvailable = FxTestSupport.startToolkit();
  }

  @AfterAll
  static void restoreEmptyProject() throws Exception {
    FxTestSupport.selectProjectItem(null);
    FxTestSupport.clearValidationService();
    if (toolkitAvailable) {
      setCurrentProject(new Project());
    }
  }

  // Panels ask Studio for the current project (annotations, ...); the test's stand-in has no folder.
  private static void setCurrentProject(Project project) throws Exception {
    Field currentProject = Studio.class.getDeclaredField("currentProject");
    currentProject.setAccessible(true);
    currentProject.set(null, project);
  }

  @Test
  void aValidConditionShowsNoError() throws Exception {
    Editor editor = open();
    // Start from a shown error, so "no error" below means the valid text was checked and cleared it.
    editor.type("FieldFilled(NoSuchField)");
    assertTrue(editor.hasError());

    editor.type("FieldFilled(OrderingDate) and [OrderingDate] > Today");

    assertFalse(editor.hasError());
  }

  @Test
  void anUnknownFieldIsReportedByTheKernelWithLineAndColumn() throws Exception {
    Editor editor = open();

    editor.type("FieldFilled(NoSuchField) and [OrderingDate] > Today");

    assertTrue(editor.hasError());
    String message = editor.message();
    assertTrue(message.contains("MVK_INVALID_ENTITY"), message);
    assertTrue(message.contains("(line 1, column 13)"), message);
  }

  @Test
  void aSyntaxErrorIsStillReportedByTheLocalGrammarCheck() throws Exception {
    Editor editor = open();

    editor.type("FieldFilled(OrderingDate)\nand nonsense");

    assertTrue(editor.hasError());
    assertTrue(editor.message().contains("[MVK_"), editor.message());
  }

  @Test
  void theErrorGoesAwayAgainOnceTheConditionIsFixed() throws Exception {
    Editor editor = open();
    editor.type("FieldFilled(NoSuchField)");
    assertTrue(editor.hasError());

    editor.type("FieldFilled(OrderingDate)");

    assertFalse(editor.hasError());
  }

  private Editor open() throws Exception {
    assumeTrue(toolkitAvailable, "No JavaFX toolkit available");
    Path source = locateBasicModels();
    Path models = Files.createDirectories(workspace.resolve("models"));
    Files.copy(source.resolve("Invoice-Includes").resolve("Order_DM.json"), models.resolve("Order_DM.json"),
        StandardCopyOption.REPLACE_EXISTING);

    Project project = new Project();
    project.load(workspace.toFile());
    setCurrentProject(project);
    ProjectItem item = project.getRoot().findByPath(models.resolve("Order_DM.json").toString());
    assertNotNull(item, "Order_DM not found in the copied workspace");
    FxTestSupport.installRootController();
    FxTestSupport.selectProjectItem(item);
    // Like the running application: committing an edit fires model-saved events whose listeners validate the project.
    FxTestSupport.setValidationServiceForProject(project);

    DocumentModel model = (DocumentModel) item.getModel();
    List<Element> ancestors = new ArrayList<>();
    RuleElement rule = findRule(model.getContent().getModelRoot().getRootGroups(), RULE_ID, ancestors);
    assertNotNull(rule, "fixture changed, rule " + RULE_ID + " not found");

    FxTestSupport.Loaded<DocumentModelValidationRuleEditorController> loaded =
        FxTestSupport.load("/de/a12/studio/ui/editors/documentmodel/document-model-validation-rule-editor.fxml");
    FxTestSupport.onFx(() -> loaded.controller().setElement(rule, ancestors));

    RuleEditorController conditionEditor = FxTestSupport.field(loaded.controller(), "errorConditionController");
    return new Editor(conditionEditor);
  }

  private static RuleElement findRule(List<? extends Element> elements, String id, List<Element> ancestors) {
    for (Element element : elements) {
      if (element instanceof RuleElement rule && id.equals(rule.getId())) {
        return rule;
      }
      if (element instanceof GroupElement group && group.getGroup() != null && group.getGroup().getElements() != null) {
        ancestors.add(element);
        RuleElement found = findRule(group.getGroup().getElements(), id, ancestors);
        if (found != null) {
          return found;
        }
        ancestors.remove(ancestors.size() - 1);
      }
    }
    return null;
  }

  private static Path locateBasicModels() {
    Path dir = Path.of("").toAbsolutePath();
    while (dir != null && !Files.isDirectory(dir.resolve("testing/workspaces/basic/models"))) {
      dir = dir.getParent();
    }
    assertNotNull(dir, "testing/workspaces/basic/models not found");
    return dir.resolve("testing/workspaces/basic/models");
  }

  /** Reads what the condition field's own error container shows. */
  private record Editor(RuleEditorController condition) {

    static final long SETTLE_MILLIS = 1800;

    /**
     * Types like a user: replaces the text, then waits until the editor has settled. The editor validates and commits
     * after a 100 ms debounce, and the first kernel check of a cold JVM takes close to a second, so a fixed sleep is
     * either flaky or slow; instead this waits until the error state has not changed for {@link #SETTLE_MILLIS}.
     */
    void type(String text) throws Exception {
      CodeArea codeArea = FxTestSupport.field(condition, "codeArea");
      FxTestSupport.onFx(() -> codeArea.replaceText(text));
      boolean state = hasError();
      long lastChange = System.currentTimeMillis();
      long deadline = lastChange + 20_000;
      while (System.currentTimeMillis() - lastChange < SETTLE_MILLIS && System.currentTimeMillis() < deadline) {
        Thread.sleep(100);
        boolean now = FxTestSupport.onFx(this::hasError);
        if (now != state) {
          state = now;
          lastChange = System.currentTimeMillis();
        }
      }
    }

    boolean hasError() {
      return condition.errorProperty().get();
    }

    String message() throws Exception {
      ErrorContainerController container = FxTestSupport.field(condition, "errorContainerController");
      Label label = FxTestSupport.field(container, "errorMessage");
      return FxTestSupport.onFx(label::getText);
    }
  }
}
