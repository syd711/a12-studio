package de.a12.studio.modelsvalidation.validators.content;

import de.a12.studio.models.contentmodel.ContentElement;
import de.a12.studio.models.contentmodel.ContentElementLibrary;
import de.a12.studio.models.contentmodel.ContentModel;
import de.a12.studio.models.contentmodel.ContentNodes;
import org.jspecify.annotations.Nullable;

/**
 * Visits the elements of a Content Model with the <em>data context</em> each one has: the base group, or the group of
 * the closest enclosing Repeatable Group, or nothing at the top. Elements below a Repeatable Group that cannot be
 * resolved are not visited (their context is unknown); if the base group cannot be resolved nothing is, like in SME,
 * whose validator stops there. The event nodes of an element's click settings are visited right after the element, with
 * the same context.
 */
final class ContentNodeWalker {

  /**
   * One visited element.
   *
   * @param element the element, or the event node of {@code owner}'s click setting
   * @param owner   the element the event node belongs to, {@code null} for an element of the tree
   * @param context the data context of the element, {@code null} at the top
   */
  record NodeInfo(ContentModel model, ContentElement element, @Nullable ContentElement owner, ContentDocument document,
                  DocumentStructure.@Nullable Node context) {

    /** The id of the element of the tree the finding belongs to. */
    String elementId() {
      return owner != null ? owner.getId() : element.getId();
    }

    /** What the message names: the element, or the event node's type on its element. */
    String label() {
      return ContentLabels.of(element);
    }
  }

  interface Visitor {
    void visit(NodeInfo info);
  }

  private ContentNodeWalker() {
  }

  static void walk(ContentModel model, ContentDocument document, Visitor visitor) {
    if (model.getContent() == null || model.getContent().getRoot() == null || document.baseGroupInvalid()) {
      return;
    }
    walk(model, model.getContent().getRoot(), document.baseGroup(), document, visitor);
  }

  private static void walk(ContentModel model, ContentElement element, DocumentStructure.Node context, ContentDocument document,
      Visitor visitor) {
    visitor.visit(new NodeInfo(model, element, null, document, context));
    for (ContentNodes.EventNode event : ContentNodes.eventNodes(element)) {
      visitor.visit(new NodeInfo(model, event.node(), element, document, context));
    }
    if (element.getChildren() == null) {
      return;
    }
    DocumentStructure.Node childContext = context;
    if (isRepeatableGroup(element)) {
      DocumentStructure.Node group = document.structure() != null && element.getProps() != null
          && element.getProps().get("groupId") instanceof String groupId ? document.structure().find(groupId) : null;
      if (group == null || !group.isGroup()) {
        return;
      }
      childContext = group;
    }
    for (ContentElement child : element.getChildren()) {
      walk(model, child, childContext, document, visitor);
    }
  }

  static boolean isRepeatableGroup(ContentElement element) {
    return "Group".equals(element.getType()) && ContentElementLibrary.NAMESPACE.equals(namespace(element));
  }

  static String namespace(ContentElement element) {
    return element.getNamespace() != null ? element.getNamespace() : ContentElementLibrary.NAMESPACE;
  }
}
