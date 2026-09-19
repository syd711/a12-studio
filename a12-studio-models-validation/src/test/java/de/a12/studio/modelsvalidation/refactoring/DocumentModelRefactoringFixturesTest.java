package de.a12.studio.modelsvalidation.refactoring;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.documentmodel.Element;
import de.a12.studio.models.documentmodel.GroupElement;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.modelsvalidation.refactoring.DocumentModelRefactoring.Edit;
import de.a12.studio.modelsvalidation.refactoring.DocumentModelRefactoring.Plan;
import de.a12.studio.modelsvalidation.validators.ElementIndex;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Renames and moves every element of every real Document Model in {@code testing/workspaces} and checks the one
 * property that matters: after the rewrite, each Rule/Computation reference still points at the same element (and
 * the same unresolved tail) as before, and undoing the rewrite gives that back too. Catches whatever spelling of a
 * path the hand-built cases in {@link DocumentModelRefactoringTest} didn't think of.
 */
class DocumentModelRefactoringFixturesTest {

  private static final int MOVE_TARGETS_PER_ELEMENT = 3;

  @Test
  void everyReferenceOfEveryFixtureModelSurvivesEveryRenameAndMove() throws IOException {
    List<String> failures = new ArrayList<>();
    int models = 0;
    int operations = 0;
    int rewritingOperations = 0;

    for (Path file : documentModelFiles()) {
      DocumentModel model = loadDocumentModel(file);
      if (model == null) {
        continue;
      }
      models++;
      Map<String, List<DocumentModelRefactoring.Entry>> baseline = DocumentModelRefactoring.prepare(model).resolvedTargets();
      if (baseline.isEmpty()) {
        continue;
      }
      List<Element> elements = List.copyOf(new ElementIndex(model).allElements());
      ElementIndex index = new ElementIndex(model);
      // A reference that doesn't resolve (typically one into an Additive base model, which isn't loaded here) keeps
      // its unresolved tail. Moving an element with that very name into place would make it start resolving to the
      // wrong thing - a name coincidence no rewrite can prevent - so such moves are left out.
      Set<String> danglingNames = baseline.values().stream().flatMap(List::stream).flatMap(entry -> entry.tail().stream())
          .map(segment -> segment.replace("*", "").replace("'", "")).collect(Collectors.toSet());

      for (Element element : elements) {
        operations++;
        rewritingOperations += check(failures, file, "rename " + index.getPath(element), model, baseline, () -> {
          String oldName = element.getName();
          element.setName(oldName + "Renamed");
          return () -> element.setName(oldName);
        });

        GroupElement parent = index.parentOf(element);
        if (parent == null || danglingNames.contains(element.getName())) {
          continue;
        }
        for (GroupElement target : moveTargets(elements, element, parent)) {
          operations++;
          rewritingOperations += check(failures, file, "move " + index.getPath(element) + " into " + index.getPath(target), model,
              baseline, () -> {
                int position = parent.getGroup().getElements().indexOf(element);
                parent.getGroup().getElements().remove(element);
                target.getGroup().getElements().add(element);
                return () -> {
                  target.getGroup().getElements().remove(element);
                  parent.getGroup().getElements().add(position, element);
                };
              });
        }
      }
    }

    assertTrue(models > 0, "no Document Model fixture found");
    assertTrue(rewritingOperations > 0, "no operation rewrote anything - the test isn't exercising the engine");
    System.out.printf("Refactoring fixtures: %d models, %d operations, %d of them rewrote a reference%n",
        models, operations, rewritingOperations);
    assertTrue(failures.isEmpty(), failures.size() + " operation(s) broke a reference, first ones:\n  "
        + String.join("\n  ", failures.subList(0, Math.min(failures.size(), 15))));
  }

  /** Performs {@code change} under a {@link Plan}, applies the edits, checks the baseline, then undoes everything. */
  private static int check(List<String> failures, Path file, String what, DocumentModel model, Map<String, List<DocumentModelRefactoring.Entry>> baseline,
      StructuralChange change) {
    Plan plan = DocumentModelRefactoring.prepare(model);
    Runnable undoStructure = change.perform();
    List<Edit> edits = plan.computeEdits();
    edits.forEach(Edit::apply);
    boolean holds = DocumentModelRefactoring.sameReferences(baseline, DocumentModelRefactoring.prepare(model).resolvedTargets());

    edits.forEach(Edit::revert);
    undoStructure.run();
    boolean restored = DocumentModelRefactoring.sameReferences(baseline, DocumentModelRefactoring.prepare(model).resolvedTargets());

    if (!holds) {
      failures.add(file.getFileName() + ": " + what + " leaves a reference pointing elsewhere");
    }
    if (!restored) {
      failures.add(file.getFileName() + ": " + what + " isn't fully undone");
    }
    return edits.isEmpty() ? 0 : 1;
  }

  private interface StructuralChange {
    /** Applies the change and returns what undoes it. */
    Runnable perform();
  }

  /** A few groups {@code element} can move into: not its own parent, itself or a descendant, and not an Include. */
  private static List<GroupElement> moveTargets(List<Element> all, Element element, GroupElement currentParent) {
    List<GroupElement> targets = new ArrayList<>();
    for (Element candidate : all) {
      if (candidate instanceof GroupElement group && group != currentParent && group.getGroup() != null
          && group.getGroup().getIncludeConfig() == null && group.getGroup().getElements() != null
          && !isSelfOrDescendant(element, group)
          && group.getGroup().getElements().stream().noneMatch(child -> child.getName().equals(element.getName()))) {
        targets.add(group);
      }
    }
    if (targets.size() <= MOVE_TARGETS_PER_ELEMENT) {
      return targets;
    }
    return List.of(targets.get(0), targets.get(targets.size() / 2), targets.get(targets.size() - 1));
  }

  private static boolean isSelfOrDescendant(Element ancestor, Element candidate) {
    if (ancestor == candidate) {
      return true;
    }
    if (ancestor instanceof GroupElement group && group.getGroup() != null && group.getGroup().getElements() != null) {
      return group.getGroup().getElements().stream().anyMatch(child -> isSelfOrDescendant(child, candidate));
    }
    return false;
  }

  private static DocumentModel loadDocumentModel(Path file) {
    try {
      A12Model<?> model = new ProjectItem(file.toFile()).getModel();
      return model instanceof DocumentModel documentModel && documentModel.getContent() != null
          && documentModel.getContent().getModelRoot() != null ? documentModel : null;
    }
    catch (RuntimeException e) {
      return null;
    }
  }

  private static List<Path> documentModelFiles() throws IOException {
    Path workspaces = null;
    for (Path dir = Path.of("").toAbsolutePath(); dir != null && workspaces == null; dir = dir.getParent()) {
      Path candidate = dir.resolve("testing").resolve("workspaces");
      if (Files.isDirectory(candidate)) {
        workspaces = candidate;
      }
    }
    assertFalse(workspaces == null, "Could not locate 'testing/workspaces' above " + Path.of("").toAbsolutePath());
    try (Stream<Path> walk = Files.walk(workspaces)) {
      return walk.filter(Files::isRegularFile)
          .filter(path -> path.getFileName().toString().endsWith(".json"))
          .sorted()
          .toList();
    }
  }
}
