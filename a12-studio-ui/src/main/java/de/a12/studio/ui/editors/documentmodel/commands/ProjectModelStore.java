package de.a12.studio.ui.editors.documentmodel.commands;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.ui.events.StudioEventManager;
import org.jspecify.annotations.NonNull;

import java.util.List;

/**
 * The other models of the project, as far as a {@link RefactoringCommand} is concerned: which ones there are, and how
 * to make a change to one of them stick. Editors save on every edit and share their model instance with the project
 * tree, so "stick" means saving the file and announcing it - open editors and validation then pick the change up like
 * any other save.
 */
public interface ProjectModelStore {

  /** Every model of the project, the changed Document Model itself included. */
  List<A12Model<?>> models();

  /**
   * {@code model} has just been edited in place (by a refactoring, or the undo/redo of one): persist and announce it.
   */
  void changed(@NonNull A12Model<?> model);

  /**
   * Whether {@code model} is still the instance the project holds for its file. False after the file has been
   * reloaded from disk (e.g. reverted), in which case an edit prepared against the old instance would go nowhere.
   */
  default boolean isCurrent(@NonNull A12Model<?> model) {
    return models().stream().anyMatch(candidate -> candidate == model);
  }

  /** The store of the project {@code item} belongs to. */
  static ProjectModelStore of(@NonNull ProjectItem item) {
    return new ProjectModelStore() {
      @Override
      public List<A12Model<?>> models() {
        return item.getProjectModelItems().stream().<A12Model<?>>map(ProjectItem::getModel).toList();
      }

      @Override
      public void changed(@NonNull A12Model<?> model) {
        for (ProjectItem candidate : item.getProjectModelItems()) {
          if (candidate.getModel() == model) {
            candidate.save();
            StudioEventManager.getInstance().fireModelSavedEvent(candidate);
            return;
          }
        }
      }
    };
  }
}
