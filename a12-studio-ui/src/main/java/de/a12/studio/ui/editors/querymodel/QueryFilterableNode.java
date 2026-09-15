package de.a12.studio.ui.editors.querymodel;

import de.a12.studio.models.querymodel.QueryLink;
import de.a12.studio.models.querymodel.QueryModelContent;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * Adapts whichever backing type a selected "document node" tree row ({@link QueryTreeRow#isDocumentNode()})
 * actually wraps - {@link QueryModelContent} for the query's own root, or a {@link QueryLink} for a
 * relationship-traversal hop - to one shape, so {@link QueryDocumentNodePanelController}/{@link
 * QueryFieldsProjectionPanelController} don't need to know which case they're bound to. {@link #exclude()}/
 * {@link #setExclude} are root-only (see {@link QueryModelContent#getExclude()} - "retrieve only the links,
 * not the target document itself"; {@link QueryLink} has no equivalent) - a link-backed instance answers
 * {@code null} to {@link #exclude()} and ignores {@link #setExclude}.
 */
interface QueryFilterableNode {

  @Nullable
  String getFilterDefinition();

  void setFilterDefinition(String value);

  @NonNull
  List<String> getFields();

  @Nullable
  Boolean getUseAllFields();

  void setUseAllFields(Boolean value);

  /** {@code null} for a {@link QueryLink}-backed instance - see class doc. */
  @Nullable
  Boolean getExclude();

  void setExclude(Boolean value);

  static QueryFilterableNode of(@NonNull QueryModelContent content) {
    return new QueryFilterableNode() {
      @Override
      public String getFilterDefinition() {
        return content.getFilterDefinition();
      }

      @Override
      public void setFilterDefinition(String value) {
        content.setFilterDefinition(value);
      }

      @Override
      public List<String> getFields() {
        return content.getFields();
      }

      @Override
      public Boolean getUseAllFields() {
        return content.getUseAllFields();
      }

      @Override
      public void setUseAllFields(Boolean value) {
        content.setUseAllFields(value);
      }

      @Override
      public Boolean getExclude() {
        return content.getExclude();
      }

      @Override
      public void setExclude(Boolean value) {
        content.setExclude(value);
      }
    };
  }

  static QueryFilterableNode of(@NonNull QueryLink link) {
    return new QueryFilterableNode() {
      @Override
      public String getFilterDefinition() {
        return link.getFilterDefinition();
      }

      @Override
      public void setFilterDefinition(String value) {
        link.setFilterDefinition(value);
      }

      @Override
      public List<String> getFields() {
        return link.getFields();
      }

      @Override
      public Boolean getUseAllFields() {
        return link.getUseAllFields();
      }

      @Override
      public void setUseAllFields(Boolean value) {
        link.setUseAllFields(value);
      }

      @Override
      public Boolean getExclude() {
        return null;
      }

      @Override
      public void setExclude(Boolean value) {
        // No "exclude" concept on a relationship-link node - see class doc.
      }
    };
  }
}
