package de.a12.studio.modelsvalidation.validators.content;

import de.a12.studio.models.contentmodel.ContentElement;
import de.a12.studio.models.contentmodel.ContentModel;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * The <em>data context</em> of an element of a Content Model: the group of the Document Model it is relative to, which is
 * the closest enclosing Repeatable Group (whose group could be resolved) or else the base group, or nothing at the top.
 * What an element may reference in the Document Model depends on it, see {@link DocumentStructure}.
 */
public final class ContentDataContext {

  private ContentDataContext() {
  }

  /**
   * The data context of {@code element}, which is not affected by the element itself (a Repeatable Group's own reference is
   * relative to what encloses it). {@code null} if there is none, if {@code element} is not part of the tree or the base
   * group does not resolve.
   */
  public static DocumentStructure.@Nullable Node of(ContentModel model, DocumentStructure structure, ContentElement element) {
    DocumentStructure.Node context = null;
    String baseGroupId = model.getContent() != null && model.getContent().getConfiguration() != null
        ? model.getContent().getConfiguration().getBaseGroupId()
        : null;
    DocumentStructure.Node baseGroup = structure.find(baseGroupId);
    if (baseGroup != null && baseGroup.isGroup()) {
      context = baseGroup;
    }
    if (model.getContent() == null || model.getContent().getRoot() == null) {
      return context;
    }
    List<ContentElement> path = new ArrayList<>();
    if (!collect(model.getContent().getRoot(), element, path)) {
      return context;
    }
    // The strict ancestors, outermost first: each Repeatable Group that resolves narrows the context.
    for (ContentElement ancestor : path.subList(0, path.size() - 1)) {
      if (ContentNodeWalker.isRepeatableGroup(ancestor) && ancestor.getProps() != null
          && ancestor.getProps().get("groupId") instanceof String groupId) {
        DocumentStructure.Node group = structure.find(groupId);
        if (group != null && group.isGroup()) {
          context = group;
        }
      }
    }
    return context;
  }

  private static boolean collect(ContentElement node, ContentElement target, List<ContentElement> path) {
    path.add(node);
    if (node == target) {
      return true;
    }
    if (node.getChildren() != null) {
      for (ContentElement child : node.getChildren()) {
        if (collect(child, target, path)) {
          return true;
        }
      }
    }
    path.remove(path.size() - 1);
    return false;
  }
}
