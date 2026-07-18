package de.a12.studio.ui.editors;

import de.a12.studio.dataservices.projects.ProjectItem;
import org.jspecify.annotations.NonNull;

/**
 * Controls when a property editor's committed changes are actually persisted to disk. Property editors always
 * apply a change to the in-memory model right away (so validation/other panels see it immediately); this only
 * decides when {@link ProjectItem#save()} is called for that change.
 * <p>
 * {@link #IMMEDIATE} saves on every commit, which is correct for editors bound to the currently selected
 * project item outside of any dialog. {@link Deferred} instead is for editors embedded in a dialog with its
 * own Save button: it remembers the project item to save and only writes it when {@link Deferred#flush()} is
 * called, e.g. from that button's action handler.
 */
public interface PropertyEditorSaveMode {

  PropertyEditorSaveMode IMMEDIATE = ProjectItem::save;

  void commit(@NonNull ProjectItem projectItem);

  class Deferred implements PropertyEditorSaveMode {

    private ProjectItem pending;

    @Override
    public void commit(@NonNull ProjectItem projectItem) {
      this.pending = projectItem;
    }

    /**
     * Persists the project item if any editor sharing this save mode committed a change since the last flush.
     * Returns whether there was anything to save.
     */
    public boolean flush() {
      if (pending == null) {
        return false;
      }
      pending.save();
      pending = null;
      return true;
    }
  }
}
