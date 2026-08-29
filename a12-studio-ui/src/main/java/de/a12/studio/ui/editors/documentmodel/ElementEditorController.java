package de.a12.studio.ui.editors.documentmodel;

import de.a12.studio.models.documentmodel.Element;
import de.a12.studio.models.documentmodel.GroupConfig;
import de.a12.studio.models.documentmodel.GroupElement;
import org.jspecify.annotations.NonNull;

import java.util.List;

public interface ElementEditorController {

  void setElement(@NonNull Element element, @NonNull List<Element> ancestors);

  /**
   * Releases resources held by this controller and its embedded property editor panels once it's replaced by
   * another element's editor or the owning tab is closed. Implementations that embed panels registered with
   * {@link de.a12.studio.ui.events.StudioEventManager} (e.g. via {@link
   * de.a12.studio.ui.editors.AbstractPropertyEditor#destroy}) must override this to unregister them, since
   * nothing else does so on their behalf once this controller's {@link javafx.scene.Node} is discarded.
   */
  default void destroy() {
  }

  /**
   * Whether the selected element is nested inside an attachment group, whose fixed children (filename,
   * content, ...) are managed by the kernel and shouldn't be hand-edited in the property editors.
   */
  default boolean isWithinAttachment(@NonNull List<Element> ancestors) {
    return ancestors.stream().anyMatch(ancestor -> ancestor instanceof GroupElement groupElement
        && groupElement.getGroup() != null
        && GroupConfig.USAGE_TYPE_ATTACHMENT.equals(groupElement.getGroup().getUsageType()));
  }

  /**
   * Whether the selected element is nested inside an Include group (see {@link
   * de.a12.studio.ui.editors.documentmodel.ElementViewModel#getChildren}). Such elements are resolved
   * from - and belong to - the referenced Document Model rather than this one, so they shouldn't be
   * hand-edited from this model's property editors.
   */
  default boolean isWithinInclude(@NonNull List<Element> ancestors) {
    return ancestors.stream().anyMatch(ancestor -> ancestor instanceof GroupElement groupElement
        && groupElement.getGroup() != null
        && groupElement.getGroup().getIncludeConfig() != null);
  }
}
