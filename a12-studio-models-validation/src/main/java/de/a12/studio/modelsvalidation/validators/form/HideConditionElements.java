package de.a12.studio.modelsvalidation.validators.form;

import de.a12.studio.models.formmodel.AbstractRepeat;
import de.a12.studio.models.formmodel.Cell;
import de.a12.studio.models.formmodel.Control;
import de.a12.studio.models.formmodel.ControlGrid;
import de.a12.studio.models.formmodel.DetachedRepeat;
import de.a12.studio.models.formmodel.EmbeddedRepeat;
import de.a12.studio.models.formmodel.FormModelContent;
import de.a12.studio.models.formmodel.HideCondition;
import de.a12.studio.models.formmodel.MultiColumnSection;
import de.a12.studio.models.formmodel.Row;
import de.a12.studio.models.formmodel.Screen;
import de.a12.studio.models.formmodel.ScreenElement;
import de.a12.studio.models.formmodel.Section;

import java.util.ArrayList;
import java.util.List;

/** Shared traversal collecting every node in a Form Model's screen tree that carries a {@link HideCondition}. */
final class HideConditionElements {

  private HideConditionElements() {}

  record Entry(String nodeId, HideCondition hideCondition) {}

  static List<Entry> collect(FormModelContent content) {
    List<Entry> entries = new ArrayList<>();
    if (content == null || content.getScreens() == null) {
      return entries;
    }
    for (Screen screen : content.getScreens()) {
      visit(screen.getScreenElements(), entries);
    }
    return entries;
  }

  private static void visit(List<ScreenElement> elements, List<Entry> entries) {
    if (elements == null) {
      return;
    }
    for (ScreenElement element : elements) {
      if (element.getHideCondition() != null) {
        entries.add(new Entry(element.getId(), element.getHideCondition()));
      }
      if (element instanceof Section section) {
        visit(section.getScreenElements(), entries);
      }
      else if (element instanceof MultiColumnSection section) {
        visit(section.getScreenElements(), entries);
      }
      else if (element instanceof ControlGrid grid) {
        visitRows(grid, entries);
      }
      else if (element instanceof EmbeddedRepeat repeat && repeat.getControlGrid() != null) {
        visitRows(repeat.getControlGrid(), entries);
      }
      else if (element instanceof DetachedRepeat repeat && repeat.getDetailScreen() != null) {
        visit(repeat.getDetailScreen().getScreenElements(), entries);
      }
      else if (element instanceof AbstractRepeat) {
        // InlineRepeat has no nested screen elements to descend into.
      }
    }
  }

  private static void visitRows(ControlGrid grid, List<Entry> entries) {
    for (Row row : grid.getRow()) {
      if (row.getHideCondition() != null) {
        entries.add(new Entry(row.getId(), row.getHideCondition()));
      }
      for (Cell cell : row.getCell()) {
        if (cell instanceof Control control && control.getHideCondition() != null) {
          entries.add(new Entry(control.getId(), control.getHideCondition()));
        }
      }
    }
  }
}
