package de.a12.studio.models.treemodel;

import java.util.ArrayList;
import java.util.List;

/**
 * SME's pin direction rules for a tree's columns ({@code pinDirectionHelper.ts}): the columns stay sorted with the
 * left-pinned ones first and the right-pinned ones last, and a column is never moved past one with another pin
 * direction.
 */
public final class TreeColumns {

  private TreeColumns() {
  }

  /** {@code column}'s place in the order: 0 = pinned left, 1 = not pinned, 2 = pinned right. */
  public static int pinRank(TreeColumn column) {
    if (TreeColumn.PIN_DIRECTION_LEFT.equalsIgnoreCase(column.getPinDirection())) {
      return 0;
    }
    return TreeColumn.PIN_DIRECTION_RIGHT.equalsIgnoreCase(column.getPinDirection()) ? 2 : 1;
  }

  /** Re-sorts {@code columns} in place: left-pinned, unpinned, right-pinned, each group in its current order. */
  public static void sortByPinDirection(List<TreeColumn> columns) {
    List<TreeColumn> sorted = new ArrayList<>(columns);
    sorted.sort((first, second) -> Integer.compare(pinRank(first), pinRank(second)));
    columns.clear();
    columns.addAll(sorted);
  }

  /** Whether the columns are already in the order {@link #sortByPinDirection} produces. */
  public static boolean isSortedByPinDirection(List<TreeColumn> columns) {
    for (int index = 1; index < columns.size(); index++) {
      if (pinRank(columns.get(index - 1)) > pinRank(columns.get(index))) {
        return false;
      }
    }
    return true;
  }

  /** Whether swapping the column at {@code index} with the one at {@code index + delta} would mix pin directions. */
  public static boolean isIllegalMove(List<TreeColumn> columns, int index, int delta) {
    int target = index + delta;
    if (index < 0 || index >= columns.size() || target < 0 || target >= columns.size()) {
      return false;
    }
    return pinRank(columns.get(index)) != pinRank(columns.get(target));
  }
}
