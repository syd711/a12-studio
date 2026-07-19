package de.a12.studio.ui.editors.documentmodel;

import de.a12.studio.dataservices.models.documentmodel.Element;
import de.a12.studio.dataservices.models.documentmodel.GroupConfig;
import de.a12.studio.dataservices.models.documentmodel.GroupElement;
import org.jspecify.annotations.NonNull;

import java.util.List;

public interface ElementEditorController {

  void setElement(@NonNull Element element, @NonNull List<Element> ancestors);

  /**
   * Whether the selected element is nested inside an attachment group, whose fixed children (filename,
   * content, ...) are managed by the kernel and shouldn't be hand-edited in the property editors.
   */
  default boolean isWithinAttachment(@NonNull List<Element> ancestors) {
    return ancestors.stream().anyMatch(ancestor -> ancestor instanceof GroupElement groupElement
        && groupElement.getGroup() != null
        && GroupConfig.USAGE_TYPE_ATTACHMENT.equals(groupElement.getGroup().getUsageType()));
  }
}
