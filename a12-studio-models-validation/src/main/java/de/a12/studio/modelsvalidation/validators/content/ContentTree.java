package de.a12.studio.modelsvalidation.validators.content;

import de.a12.studio.models.contentmodel.ContentElement;
import de.a12.studio.models.contentmodel.ContentModel;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.Severity;
import de.a12.studio.modelsvalidation.ValidationMessages;

import java.util.ArrayList;
import java.util.List;

/** Walks every element of a Content Model's tree, for the checks that do not depend on the Document Model. */
final class ContentTree {

  interface Visitor {
    /** @param ancestors the elements from the root down to (excluding) {@code element} */
    void visit(ContentElement element, List<ContentElement> ancestors);
  }

  private ContentTree() {
  }

  static void forEach(ContentModel model, Visitor visitor) {
    if (model.getContent() != null && model.getContent().getRoot() != null) {
      visit(model.getContent().getRoot(), new ArrayList<>(), visitor);
    }
  }

  private static void visit(ContentElement element, List<ContentElement> ancestors, Visitor visitor) {
    visitor.visit(element, ancestors);
    if (element.getChildren() != null) {
      ancestors.add(element);
      element.getChildren().forEach(child -> visit(child, ancestors, visitor));
      ancestors.remove(ancestors.size() - 1);
    }
  }

  /**
   * A finding on {@code element}; its message gets the element's label and id as {@code {0}} and {@code {1}}, then {@code
   * arguments}.
   */
  static ModelValidationError finding(ContentModel model, ContentElement element, Severity severity, String messageKey, Object... arguments) {
    Object[] all = new Object[arguments.length + 2];
    all[0] = ContentLabels.of(element);
    all[1] = element.getId();
    System.arraycopy(arguments, 0, all, 2, arguments.length);
    return new ModelValidationError(model, element.getId(), ValidationMessages.get(messageKey, all), severity.name());
  }
}
