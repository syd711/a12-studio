package de.a12.studio.modelsvalidation.refactoring;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.documentmodel.ComputationElement;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.documentmodel.Element;
import de.a12.studio.models.documentmodel.GroupElement;
import de.a12.studio.models.documentmodel.RuleElement;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.models.util.JsonSettings;
import de.a12.studio.modelsvalidation.refactoring.DocumentModelRefactoring.Edit;
import de.a12.studio.modelsvalidation.refactoring.ProjectReferenceRefactoring.ModelEdits;
import de.a12.studio.modelsvalidation.validators.ElementIndex;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link ProjectReferenceRefactoring} against the real workspaces in {@code testing/workspaces}: every element of
 * every Document Model is renamed and moved with every other model of its workspace loaded, and two things must
 * hold - undoing the edits gives every model back byte for byte, and a relative path in a Document Model that
 * includes the changed one still reaches the same element afterwards.
 */
class ProjectReferenceRefactoringFixturesTest {

  @Test
  void aSelectionOfTheAdvancedWorkspaceFollowsARenameInItsCombinationsBaseModel() throws IOException {
    List<A12Model<?>> project = loadWorkspace("advanced_new");
    DocumentModel base = documentModel(project, "PersonSkills_LinkFields_Base_Dc");
    A12Model<?> selection = model(project, "PersonSkills_NumberConversion_Se");
    Element acquiredAt = new ElementIndex(base).allElements().stream()
        .filter(element -> new ElementIndex(base).getPath(element).equals("/Status/AcquiredAt"))
        .findFirst().orElseThrow();
    String before = JsonSettings.objectMapper.writeValueAsString(selection);

    DocumentModelRefactoring.Plan plan = DocumentModelRefactoring.prepare(base);
    acquiredAt.setName("AcquiredOn");
    List<ModelEdits> edits = ProjectReferenceRefactoring.computeEdits(base, plan, project);
    edits.forEach(modelEdits -> modelEdits.edits().forEach(Edit::apply));

    String after = JsonSettings.objectMapper.writeValueAsString(selection);
    assertTrue(after.contains("\"/Status/AcquiredOn\""), after);
    assertFalse(after.contains("\"/Status/AcquiredAt\""), after);
    assertTrue(after.contains("\"/HR/Acknowledged\""), "an unrelated path stays as it was");
    assertEquals(before.replace("/Status/AcquiredAt", "/Status/AcquiredOn"), after);
  }

  @Test
  void aQueryOfTheAdvancedWorkspaceFollowsARenameInItsTargetModelButNotInLinkedOnes() throws IOException {
    List<A12Model<?>> project = loadWorkspace("advanced_new");
    DocumentModel person = documentModel(project, "Person_Dc");
    A12Model<?> query = model(project, "PersonsWithFulltimeContract_UnassignedToTeam_Qe");
    Element lastName = new ElementIndex(person).allElements().stream()
        .filter(element -> new ElementIndex(person).getPath(element).equals("/Person/LastName"))
        .findFirst().orElseThrow();

    DocumentModelRefactoring.Plan plan = DocumentModelRefactoring.prepare(person);
    lastName.setName("Surname");
    List<ModelEdits> edits = ProjectReferenceRefactoring.computeEdits(person, plan, project);
    edits.forEach(modelEdits -> modelEdits.edits().forEach(Edit::apply));

    String after = JsonSettings.objectMapper.writeValueAsString(query);
    // once in "fields", once in "sort"
    assertEquals(2, after.split("\"/Person/Surname\"", -1).length - 1, after);
    assertFalse(after.contains("/Person/LastName"), after);
    assertTrue(after.contains("/Contract/WeeklyWorkhours"), "a path below a relationship belongs to another model");
  }

  @Test
  void renamesAndMovesInEveryFixtureModelKeepThePathsOfIncludingModelsValidAndUndoRestoresEverything()
      throws IOException {
    List<String> failures = new ArrayList<>();
    int operations = 0;
    int operationsWithEdits = 0;
    int followedIncludedPaths = 0;

    for (Path workspace : workspaces()) {
      List<A12Model<?>> project = loadWorkspace(workspace);
      List<DocumentModel> documentModels = project.stream()
          .filter(DocumentModel.class::isInstance).map(DocumentModel.class::cast)
          .filter(model -> model.getContent() != null && model.getContent().getModelRoot() != null
              && model.getContent().getModelRoot().getRootGroups() != null
              && !model.getContent().getModelRoot().getRootGroups().isEmpty())
          .toList();

      for (DocumentModel changed : documentModels) {
        List<DocumentModel> includers = documentModels.stream()
            .filter(candidate -> candidate != changed && new ElementIndex(candidate).allElements().stream()
                .anyMatch(element -> element instanceof GroupElement group && group.getGroup() != null
                    && group.getGroup().getIncludeConfig() != null
                    && changed.getId().equals(group.getGroup().getIncludeConfig().getReference())))
            .toList();
        ElementIndex index = new ElementIndex(changed);
        GroupElement firstRoot = changed.getContent().getModelRoot().getRootGroups().get(0);

        for (Element element : List.copyOf(index.allElements())) {
          String label = workspace.getFileName() + ":" + changed.getId() + ":" + index.getPath(element);

          operations++;
          String oldName = element.getName();
          int result = check(failures, label + " rename", changed, project, includers, () -> {
            element.setName(oldName + "Renamed");
            return () -> element.setName(oldName);
          });
          operationsWithEdits += result > 0 ? 1 : 0;
          followedIncludedPaths += result;

          GroupElement parent = index.parentOf(element);
          if (parent != null && parent != firstRoot && element != firstRoot && firstRoot.getGroup().getElements() != null) {
            operations++;
            int moved = check(failures, label + " move", changed, project, includers, () -> {
              int position = parent.getGroup().getElements().indexOf(element);
              parent.getGroup().getElements().remove(element);
              firstRoot.getGroup().getElements().add(element);
              return () -> {
                firstRoot.getGroup().getElements().remove(element);
                parent.getGroup().getElements().add(position, element);
              };
            });
            operationsWithEdits += moved > 0 ? 1 : 0;
            followedIncludedPaths += moved;
          }
        }
      }
    }

    assertTrue(failures.isEmpty(), failures.size() + " of " + operations + " operations failed, e.g.:\n"
        + String.join("\n", failures.subList(0, Math.min(15, failures.size()))));
    assertTrue(operationsWithEdits > 0, "the fixtures were expected to hold cross-model references");
    assertTrue(followedIncludedPaths > 0, "the fixtures were expected to hold paths through an Include");
  }

  /**
   * Runs one structural change against the whole workspace and checks it. Returns the number of relative paths of
   * including models that resolved into the changed model before the change (and so were verified afterwards).
   */
  private static int check(List<String> failures, String label, DocumentModel changed, List<A12Model<?>> project,
      List<DocumentModel> includers, StructuralChange change) throws IOException {
    List<DocumentModel> documentModels = project.stream().filter(DocumentModel.class::isInstance)
        .map(DocumentModel.class::cast).toList();
    Map<Element, Optional<Element>> resolvedBefore = resolveRelativePaths(includers, documentModels);
    Map<A12Model<?>, String> serializedBefore = new IdentityHashMap<>();
    for (A12Model<?> model : project) {
      serializedBefore.put(model, JsonSettings.objectMapper.writeValueAsString(model));
    }

    DocumentModelRefactoring.Plan plan = DocumentModelRefactoring.prepare(changed);
    Runnable undoChange = change.perform();
    List<ModelEdits> edits;
    try {
      edits = ProjectReferenceRefactoring.computeEdits(changed, plan, project);
      edits.forEach(modelEdits -> modelEdits.edits().forEach(Edit::apply));

      Map<Element, Optional<Element>> resolvedAfter = resolveRelativePaths(includers, documentModels);
      for (Map.Entry<Element, Optional<Element>> entry : resolvedBefore.entrySet()) {
        Optional<Element> before = entry.getValue();
        if (before.isPresent() && !before.equals(resolvedAfter.get(entry.getKey()))) {
          failures.add(label + ": a path of " + entry.getKey().getName() + " no longer reaches "
              + before.get().getName());
        }
      }

      for (int i = edits.size() - 1; i >= 0; i--) {
        List<Edit> modelEdits = edits.get(i).edits();
        for (int j = modelEdits.size() - 1; j >= 0; j--) {
          modelEdits.get(j).revert();
        }
      }
    }
    finally {
      undoChange.run();
    }

    for (A12Model<?> model : project) {
      if (!serializedBefore.get(model).equals(JsonSettings.objectMapper.writeValueAsString(model))) {
        failures.add(label + ": undo did not restore " + model.getId());
      }
    }
    return (int) resolvedBefore.values().stream().filter(resolved -> resolved.isPresent()
        && isInto(resolved.get(), changed)).count();
  }

  private static boolean isInto(Element element, DocumentModel model) {
    return new ElementIndex(model).allElements().contains(element);
  }

  /**
   * For every Rule/Computation of {@code models} that names its target by relative path: the element that path
   * reaches, resolved through Includes (which is what makes the paths of an including model depend on the model it
   * includes).
   */
  private static Map<Element, Optional<Element>> resolveRelativePaths(List<DocumentModel> models,
      List<DocumentModel> allDocumentModels) {
    Map<Element, Optional<Element>> result = new IdentityHashMap<>();
    for (DocumentModel model : models) {
      ElementIndex index = new ElementIndex(model, allDocumentModels);
      for (Element element : index.allElements()) {
        if (element instanceof RuleElement rule && rule.getRule() != null
            && rule.getRule().getErrorEntityRelPath() != null) {
          result.put(element, index.resolveRelativePath(element, rule.getRule().getErrorEntityRelPath()));
        }
        else if (element instanceof ComputationElement computation && computation.getComputation() != null
            && computation.getComputation().getComputedFieldRelPath() != null) {
          result.put(element, index.resolveRelativePath(element, computation.getComputation().getComputedFieldRelPath()));
        }
      }
    }
    return result;
  }

  @FunctionalInterface
  private interface StructuralChange {

    /** Makes the change; the returned action takes it back. */
    Runnable perform();
  }

  private static DocumentModel documentModel(List<A12Model<?>> project, String id) {
    return (DocumentModel) model(project, id);
  }

  private static A12Model<?> model(List<A12Model<?>> project, String id) {
    A12Model<?> found = project.stream().filter(candidate -> id.equals(candidate.getId())).findFirst().orElse(null);
    assertNotNull(found, "fixture model " + id + " not found");
    return found;
  }

  private static List<A12Model<?>> loadWorkspace(String name) throws IOException {
    return loadWorkspace(workspacesRoot().resolve(name));
  }

  private static List<A12Model<?>> loadWorkspace(Path workspace) throws IOException {
    List<A12Model<?>> models = new ArrayList<>();
    try (Stream<Path> walk = Files.walk(workspace)) {
      for (Path file : walk.filter(Files::isRegularFile).filter(path -> path.getFileName().toString().endsWith(".json"))
          .sorted().toList()) {
        try {
          A12Model<?> model = new ProjectItem(file.toFile()).getModel();
          if (model != null) {
            models.add(model);
          }
        }
        catch (RuntimeException e) {
          // not a model file
        }
      }
    }
    return models;
  }

  private static List<Path> workspaces() throws IOException {
    try (Stream<Path> children = Files.list(workspacesRoot())) {
      return children.filter(Files::isDirectory).sorted().toList();
    }
  }

  private static Path workspacesRoot() {
    for (Path dir = Path.of("").toAbsolutePath(); dir != null; dir = dir.getParent()) {
      Path candidate = dir.resolve("testing").resolve("workspaces");
      if (Files.isDirectory(candidate)) {
        return candidate;
      }
    }
    throw new AssertionError("Could not locate 'testing/workspaces' above " + Path.of("").toAbsolutePath());
  }
}
