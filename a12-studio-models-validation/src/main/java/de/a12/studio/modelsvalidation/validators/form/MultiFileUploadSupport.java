package de.a12.studio.modelsvalidation.validators.form;

import de.a12.studio.models.documentmodel.Element;
import de.a12.studio.models.documentmodel.GroupConfig;
import de.a12.studio.models.documentmodel.GroupElement;
import de.a12.studio.modelsvalidation.validators.ElementIndex;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Which attachment group an Inline/Embedded Repeat's {@code multiFileUploadOptions.elementRef} may point at.
 * The SME docs ("Multi File Upload", {@code sme-sme-fm-ba-docs.md}) allow the feature only "if the underlying
 * repeatable group contains exactly one attachment group, which has to be non-repeatable and is not nested in
 * an additional repeatable group", and set the attachment group path automatically in the editor - so the
 * editor needs to find that one group, and to tell the user why there isn't one.
 */
public final class MultiFileUploadSupport {

  private MultiFileUploadSupport() {
  }

  /**
   * The attachment-usage groups below the group {@code repeatGroupId} (the repeat's {@code groupRef}) that
   * qualify as a multi-file-upload target: non-repeatable, and with no repeatable group between them and the
   * repeat's own group. Empty when {@code repeatGroupId} doesn't resolve to a group of {@code elementIndex}'s
   * model (e.g. it is reached through an Include).
   */
  public static List<GroupElement> attachmentGroupCandidates(@Nullable ElementIndex elementIndex, @Nullable String repeatGroupId) {
    List<GroupElement> candidates = new ArrayList<>();
    if (elementIndex == null || repeatGroupId == null) {
      return candidates;
    }
    Element repeatGroup = elementIndex.resolveElement(repeatGroupId).orElse(null);
    if (!(repeatGroup instanceof GroupElement)) {
      return candidates;
    }
    for (Element element : elementIndex.allElements()) {
      if (element instanceof GroupElement group && isAttachmentGroup(group) && !isRepeatable(group)
          && isDirectlyBelowWithoutRepeatableGroup(elementIndex, group, repeatGroup)) {
        candidates.add(group);
      }
    }
    return candidates;
  }

  /** The single qualifying attachment group of {@code repeatGroupId}, or {@code null} if there are none or several. */
  public static @Nullable GroupElement singleAttachmentGroup(@Nullable ElementIndex elementIndex, @Nullable String repeatGroupId) {
    List<GroupElement> candidates = attachmentGroupCandidates(elementIndex, repeatGroupId);
    return candidates.size() == 1 ? candidates.get(0) : null;
  }

  private static boolean isAttachmentGroup(GroupElement group) {
    return group.getGroup() != null && GroupConfig.USAGE_TYPE_ATTACHMENT.equals(group.getGroup().getUsageType());
  }

  private static boolean isRepeatable(GroupElement group) {
    return group.getGroup() != null && group.getGroup().getRepeatability() != null && group.getGroup().getRepeatability() > 1;
  }

  private static boolean isDirectlyBelowWithoutRepeatableGroup(ElementIndex elementIndex, GroupElement group, Element repeatGroup) {
    GroupElement parent = elementIndex.parentOf(group);
    while (parent != null) {
      if (parent == repeatGroup) {
        return true;
      }
      if (isRepeatable(parent)) {
        return false;
      }
      parent = elementIndex.parentOf(parent);
    }
    return false;
  }
}
