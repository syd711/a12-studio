package de.a12.studio.modelsvalidation.validators.query;

import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.documentmodel.Element;
import de.a12.studio.models.querymodel.QueryModel;
import de.a12.studio.modelsvalidation.ValidationContext;
import de.a12.studio.modelsvalidation.validators.ElementIndex;

/**
 * Shared target-Document-Model/field-path resolution for every Query Model validator. Unlike Overview Model's
 * {@code elementRef} (a kernel element id, resolved via {@link ElementIndex#resolveElement}), a Query Model's
 * {@code content.fields[]}/{@code content.sort[].sortBy.field} are "/"-separated name paths (e.g.
 * {@code "/Person/FirstName"}, matching {@link ElementIndex#getPath}), so resolution here is a plain linear scan
 * over the target model's elements rather than an id lookup - correct for typical model sizes, not worth
 * indexing by path up front.
 */
public final class QueryElementResolution {

  private QueryElementResolution() {
  }

  /** The Document Model {@code content.targetDocumentModel} refers to, or null if unset/unresolved. */
  public static DocumentModel targetDocumentModel(QueryModel model, ValidationContext context) {
    if (model.getContent() == null) {
      return null;
    }
    return context.findOtherDocumentModel(model.getContent().getTargetDocumentModel());
  }

  /** Resolves an absolute "/"-separated field path (as stored in {@code fields[]}/{@code sortBy.field}) against
   * {@code index}, or null if no element has that exact path. */
  public static Element resolveByPath(ElementIndex index, String path) {
    if (path == null || path.isBlank()) {
      return null;
    }
    for (Element element : index.allElements()) {
      if (path.equals(index.getPath(element))) {
        return element;
      }
    }
    return null;
  }
}
