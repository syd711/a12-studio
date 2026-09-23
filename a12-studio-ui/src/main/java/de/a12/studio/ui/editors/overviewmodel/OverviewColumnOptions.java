package de.a12.studio.ui.editors.overviewmodel;

import de.a12.studio.models.overviewmodel.Column;
import de.a12.studio.models.overviewmodel.ColumnLinkReference;
import de.a12.studio.modelsvalidation.validators.ElementIndex;
import de.a12.studio.modelsvalidation.validators.overview.OverviewColumnHeaderLabelOrIconValidator;
import javafx.scene.control.ComboBox;
import javafx.util.StringConverter;

import java.util.List;
import java.util.function.Function;

/**
 * Describes {@link Column}s for display in pickers outside the Columns panel itself (currently the Sorting
 * panel's per-row combo box, see {@link de.a12.studio.ui.editors.overviewmodel.OverviewSortingPanelController}),
 * so a column reads the same wherever it's picked from as it does in {@link
 * de.a12.studio.ui.editors.overviewmodel.OverviewColumnsPanelController}'s own rows.
 */
public final class OverviewColumnOptions {

  private OverviewColumnOptions() {
  }

  public static List<String> columnIds(List<Column> columns) {
    return columns.stream().map(Column::getId).toList();
  }

  /**
   * The {@link ElementIndex} {@code column}'s {@code elementRef} actually resolves against: {@code
   * documentModelIndex} for a plain column, or - for a column carrying {@code linkReferences} (a
   * Relationship UI Model's Available/Selected Items overview projecting a field that lives on the
   * relationship's own link document, e.g. {@code PersonSkills_Re.json}'s {@code linkDocumentModel} - not on
   * the primary Document/Query Model) - the index {@code linkDocumentModelIndexResolver} returns for that
   * relationship's id, falling back to {@code documentModelIndex} if the relationship or its link document
   * model doesn't resolve (yet). See {@code OverviewModelEditorController#refreshLinkDocumentModelIndexes}
   * for how that resolver is built.
   */
  public static ElementIndex indexFor(Column column, ElementIndex documentModelIndex, Function<String, ElementIndex> linkDocumentModelIndexResolver) {
    if (column == null || linkDocumentModelIndexResolver == null) {
      return documentModelIndex;
    }
    List<ColumnLinkReference> linkReferences = column.getLinkReferences();
    if (linkReferences == null || linkReferences.isEmpty()) {
      return documentModelIndex;
    }
    String relationshipId = linkReferences.get(0).getRelationship();
    ElementIndex linkIndex = relationshipId == null ? null : linkDocumentModelIndexResolver.apply(relationshipId);
    return linkIndex != null ? linkIndex : documentModelIndex;
  }

  public static String describe(Column column, ElementIndex documentModelIndex, Function<String, ElementIndex> linkDocumentModelIndexResolver) {
    if (column == null) {
      return null;
    }
    if (isExpressionColumn(column)) {
      return "Expression Column";
    }
    if (column.getElementRef() != null && !column.getElementRef().isBlank()) {
      return OverviewElementOptions.displayPath(indexFor(column, documentModelIndex, linkDocumentModelIndexResolver), column.getElementRef());
    }
    return "(unset)";
  }

  /** Whether {@code column}'s {@code elementRef} is set but doesn't resolve against {@link #indexFor} - a
   * dangling reference, flagged by {@link
   * de.a12.studio.ui.editors.overviewmodel.OverviewColumnsPanelController}'s own row rendering (bold red
   * field summary) rather than by a validator, since a column always has *some* row to render even when its
   * reference is broken. */
  public static boolean isUnresolvedElementRef(Column column, ElementIndex documentModelIndex, Function<String, ElementIndex> linkDocumentModelIndexResolver) {
    return column != null && column.getElementRef() != null && !column.getElementRef().isBlank()
        && !OverviewElementOptions.isResolved(indexFor(column, documentModelIndex, linkDocumentModelIndexResolver), column.getElementRef());
  }

  /** Whether {@code column} is a reference column with no icon and no visible label - an accessibility
   * problem flagged by {@link OverviewColumnHeaderLabelOrIconValidator} (WARNING), surfaced here the same way
   * as {@link #isUnresolvedElementRef} since that validator's shared {@code ELEMENT_ID} can't identify which
   * column it's about. */
  public static boolean isMissingLabelOrIcon(Column column, ElementIndex documentModelIndex) {
    return OverviewColumnHeaderLabelOrIconValidator.isMissingLabelOrIcon(column, documentModelIndex);
  }

  /** A column with no field reference is an expression column, shown as "Expression Column" wherever it's
   * picked from (see {@link de.a12.studio.ui.editors.overviewmodel.OverviewColumnsPanelController}'s
   * epsilon-icon row), never by its own {@link Column#getName()}. */
  public static boolean isExpressionColumn(Column column) {
    return column != null && (column.getElementRef() == null || column.getElementRef().isBlank()) && column.getExpression() != null;
  }

  /** {@code columnId} as-is if it no longer matches any column in {@code columns} (a dangling reference,
   * flagged separately by {@link de.a12.studio.modelsvalidation.validators.overview.OverviewInitialSortingReferenceValidator}). */
  public static String describeById(List<Column> columns, String columnId, ElementIndex documentModelIndex, Function<String, ElementIndex> linkDocumentModelIndexResolver) {
    if (columnId == null) {
      return null;
    }
    return columns.stream()
        .filter(column -> columnId.equals(column.getId()))
        .findFirst()
        .map(column -> describe(column, documentModelIndex, linkDocumentModelIndexResolver))
        .orElse(columnId);
  }

  /** Renders column ids as their {@link #describe} summary in a {@code ComboBox<String>} while keeping the id
   * as the stored value, in the same monospace "path" font as every other path picker (see {@link
   * OverviewElementOptions#applyMonospaceCells}). */
  public static void applyColumnConverter(ComboBox<String> comboBox, List<Column> columns, ElementIndex documentModelIndex, Function<String, ElementIndex> linkDocumentModelIndexResolver) {
    StringConverter<String> converter = new StringConverter<>() {
      @Override
      public String toString(String columnId) {
        return columnId == null ? "" : describeById(columns, columnId, documentModelIndex, linkDocumentModelIndexResolver);
      }

      @Override
      public String fromString(String string) {
        return string;
      }
    };
    comboBox.setConverter(converter);
    OverviewElementOptions.applyMonospaceCells(comboBox, converter);
  }
}
