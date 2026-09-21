package de.a12.studio.ui.events;

import de.a12.studio.models.projects.ProjectItem;
import org.jspecify.annotations.NonNull;

/**
 * Fired after a refactoring in <em>another</em> model (renaming or moving a Document Model element) has rewritten
 * references inside {@code item}'s model in place - and again when that is undone or redone. The model object is
 * already right and has been saved, but an editor of it that is open on the screen still shows the old text, so
 * listeners that render the model (see {@link de.a12.studio.ui.tabs.TabPaneController#modelRefactored}) have to redraw.
 * Unlike {@link ModelSaveEvent} it is never the echo of an edit made in that model's own editor.
 */
public class ModelRefactoredEvent {
  @NonNull
  private final ProjectItem item;

  public ModelRefactoredEvent(@NonNull ProjectItem item) {
    this.item = item;
  }

  public @NonNull ProjectItem getItem() {
    return item;
  }
}
