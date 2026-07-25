package de.a12.studio.modelsvalidation;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.projects.Project;
import de.a12.studio.models.projects.ProjectItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Looks up sibling {@link DocumentModel}s in a project, needed for cross-model checks (e.g. resolving an
 * Include reference, or comparing time zones across every document model in a project).
 */
final class ProjectModels {

  private ProjectModels() {
  }

  /** Every {@link DocumentModel} in {@code project}, excluding {@code excludedModel} itself. */
  static List<DocumentModel> getOtherDocumentModels(Project project, A12Model<?> excludedModel) {
    List<DocumentModel> result = new ArrayList<>();
    collectDocumentModels(project.getRoot(), excludedModel, result);
    return result;
  }

  private static void collectDocumentModels(ProjectItem item, A12Model<?> excludedModel, List<DocumentModel> result) {
    if (item.isFolder()) {
      for (ProjectItem child : item.getChildren()) {
        collectDocumentModels(child, excludedModel, result);
      }
    }
    else if (item.getModel() != excludedModel && item.getModel() instanceof DocumentModel documentModel) {
      result.add(documentModel);
    }
  }
}
