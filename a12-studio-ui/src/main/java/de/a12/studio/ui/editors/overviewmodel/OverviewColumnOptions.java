package de.a12.studio.ui.editors.overviewmodel;

import de.a12.studio.models.overviewmodel.Column;
import de.a12.studio.modelsvalidation.validators.ElementIndex;
import javafx.scene.control.ComboBox;
import javafx.util.StringConverter;

import java.util.List;

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

  public static String describe(Column column, ElementIndex documentModelIndex) {
    if (column == null) {
      return null;
    }
    if (isExpressionColumn(column)) {
      return "Expression Column";
    }
    if (column.getElementRef() != null && !column.getElementRef().isBlank()) {
      return OverviewElementOptions.displayPath(documentModelIndex, column.getElementRef());
    }
    return "(unset)";
  }

  /** Whether {@code column}'s {@code elementRef} is set but doesn't resolve against {@code
   * documentModelIndex} - a dangling reference, flagged by {@link
   * de.a12.studio.ui.editors.overviewmodel.OverviewColumnsPanelController}'s own row rendering (bold red
   * field summary) rather than by a validator, since a column always has *some* row to render even when its
   * reference is broken. */
  public static boolean isUnresolvedElementRef(Column column, ElementIndex documentModelIndex) {
    return column != null && column.getElementRef() != null && !column.getElementRef().isBlank()
        && !OverviewElementOptions.isResolved(documentModelIndex, column.getElementRef());
  }

  /** A column with no field reference is an expression column, shown as "Expression Column" wherever it's
   * picked from (see {@link de.a12.studio.ui.editors.overviewmodel.OverviewColumnsPanelController}'s
   * epsilon-icon row), never by its own {@link Column#getName()}. */
  public static boolean isExpressionColumn(Column column) {
    return column != null && (column.getElementRef() == null || column.getElementRef().isBlank()) && column.getExpression() != null;
  }

  /** {@code columnId} as-is if it no longer matches any column in {@code columns} (a dangling reference,
   * flagged separately by {@link de.a12.studio.modelsvalidation.validators.overview.OverviewInitialSortingReferenceValidator}). */
  public static String describeById(List<Column> columns, String columnId, ElementIndex documentModelIndex) {
    if (columnId == null) {
      return null;
    }
    return columns.stream()
        .filter(column -> columnId.equals(column.getId()))
        .findFirst()
        .map(column -> describe(column, documentModelIndex))
        .orElse(columnId);
  }

  /** Renders column ids as their {@link #describe} summary in a {@code ComboBox<String>} while keeping the id
   * as the stored value, in the same monospace "path" font as every other path picker (see {@link
   * OverviewElementOptions#applyMonospaceCells}). */
  public static void applyColumnConverter(ComboBox<String> comboBox, List<Column> columns, ElementIndex documentModelIndex) {
    StringConverter<String> converter = new StringConverter<>() {
      @Override
      public String toString(String columnId) {
        return columnId == null ? "" : describeById(columns, columnId, documentModelIndex);
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
