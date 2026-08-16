package de.a12.studio.ui.editors.querymodel.dialogs;

import de.a12.studio.models.querymodel.QuerySort;
import org.jspecify.annotations.NonNull;

/**
 * Captures a {@link QuerySort}'s fields before {@link QuerySortDialogController} lets them mutate live, so
 * {@link #restore()} can undo those changes on Cancel. See {@code ColumnSnapshot} for the same pattern - a plain
 * field-by-field copy suffices here since {@link QuerySort} has no nested collections.
 */
class QuerySortSnapshot {

  private final QuerySort sort;

  private final String relationshipModel;
  private final String targetRole;
  private final String field;
  private final String direction;
  private final String nullHandling;
  private final Boolean ignoreCase;

  QuerySortSnapshot(@NonNull QuerySort sort) {
    this.sort = sort;
    this.relationshipModel = sort.getRelationshipModel();
    this.targetRole = sort.getTargetRole();
    this.field = sort.getSortBy().getField();
    this.direction = sort.getSortBy().getDirection();
    this.nullHandling = sort.getSortBy().getNullHandling();
    this.ignoreCase = sort.getSortBy().getIgnoreCase();
  }

  void restore() {
    sort.setRelationshipModel(relationshipModel);
    sort.setTargetRole(targetRole);
    sort.getSortBy().setField(field);
    sort.getSortBy().setDirection(direction);
    sort.getSortBy().setNullHandling(nullHandling);
    sort.getSortBy().setIgnoreCase(ignoreCase);
  }
}
