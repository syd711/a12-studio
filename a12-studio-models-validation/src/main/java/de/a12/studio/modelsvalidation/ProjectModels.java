package de.a12.studio.modelsvalidation;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.projects.Project;
import de.a12.studio.models.projects.ProjectItem;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * Looks up sibling models in a project, needed for cross-model checks (e.g. resolving an Include or a
 * tree model's document model reference, or comparing time zones across every document model).
 */
final class ProjectModels {

  private ProjectModels() {
  }

  /** Every {@link DocumentModel} in {@code project}, excluding {@code excludedModel} itself. */
  static List<DocumentModel> getOtherDocumentModels(Project project, A12Model<?> excludedModel) {
    List<DocumentModel> result = new ArrayList<>();
    collect(project.getRoot(), item -> item.getModel() != excludedModel && item.getModel() instanceof DocumentModel,
        item -> result.add((DocumentModel) item.getModel()));
    return result;
  }

  /** Every model of any type in {@code project}, excluding {@code excludedModel} itself. */
  static List<A12Model<?>> getOtherModels(Project project, A12Model<?> excludedModel) {
    List<A12Model<?>> result = new ArrayList<>();
    collect(project.getRoot(), item -> item.getModel() != null && item.getModel() != excludedModel,
        item -> result.add(item.getModel()));
    return result;
  }

  /** The {@link ProjectItem} whose loaded model is {@code model} (by identity), or null. */
  static ProjectItem findItem(Project project, A12Model<?> model) {
    List<ProjectItem> result = new ArrayList<>(1);
    collect(project.getRoot(), item -> item.getModel() == model, result::add);
    return result.isEmpty() ? null : result.get(0);
  }

  private interface ItemConsumer {
    void accept(ProjectItem item);
  }

  private static void collect(ProjectItem item, Predicate<ProjectItem> filter, ItemConsumer consumer) {
    if (item.isFolder()) {
      for (ProjectItem child : item.getChildren()) {
        collect(child, filter, consumer);
      }
    }
    else if (filter.test(item)) {
      consumer.accept(item);
    }
  }
}
