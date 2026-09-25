package de.a12.studio.ui.editors.contentmodel;

import de.a12.studio.models.contentmodel.ContentElement;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * A section of the Content Model editor's property column, like one of SME's setting-panel sections. The editor
 * hands every panel the selected element; a panel shows itself only if it has settings for that element's type and
 * reports edits (already applied to the element's props) through the change callback so the editor can save.
 */
public interface ContentSettingsPanel {

  /** Key under which a panel registers itself in its root pane's {@code getProperties()}, for the editor to find. */
  String PANEL_KEY = "contentSettingsPanel";

  /** Shows the settings of {@code element}, or hides the panel when it has none for its type / for {@code null}. */
  void showElement(@Nullable ContentElement element);

  /** Called after every user edit that changed the shown element's props. */
  void setOnChange(@NonNull Runnable onChange);

  /** Gives the panel access to the surrounding model; panels that only need the shown element ignore it. */
  default void setContext(@NonNull Context context) {
  }

  /** What a panel may ask of the editor beyond the selected element. */
  interface Context {

    /** The element containing {@code element}, or {@code null} for the root. */
    @Nullable ContentElement parentOf(@NonNull ContentElement element);

    /** The panel added or removed child elements of the selected element: the tree must be rebuilt below it. */
    void structureChanged();
  }

  /** Releases listeners and pending work once the editor closes. */
  void destroy();
}
