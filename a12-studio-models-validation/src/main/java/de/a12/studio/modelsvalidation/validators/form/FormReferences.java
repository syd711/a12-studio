package de.a12.studio.modelsvalidation.validators.form;

import de.a12.studio.models.formmodel.AbstractRepeat;
import de.a12.studio.models.formmodel.Cell;
import de.a12.studio.models.formmodel.Control;
import de.a12.studio.models.formmodel.ControlGrid;
import de.a12.studio.models.formmodel.DetachedRepeat;
import de.a12.studio.models.formmodel.EmbeddedRepeat;
import de.a12.studio.models.formmodel.FieldBasedRepeatOverviewColumn;
import de.a12.studio.models.formmodel.FormModel;
import de.a12.studio.models.formmodel.MultiColumnSection;
import de.a12.studio.models.formmodel.RepeatOverviewColumn;
import de.a12.studio.models.formmodel.Row;
import de.a12.studio.models.formmodel.Screen;
import de.a12.studio.models.formmodel.ScreenElement;
import de.a12.studio.models.formmodel.Section;

import java.util.List;

/**
 * Whether a Document Model field/group is still referenced by anything in a Form Model's Screens tree - used
 * by the Cleanup tab (SME's "orphaned field/group config entry" detection) to distinguish a config entry
 * that's merely unused (safe to remove) from one that's actively bound to a Control/Repeat.
 */
public final class FormReferences {

  private FormReferences() {}

  public static boolean isFieldReferenced(FormModel model, String elementRef) {
    if (model.getContent() == null || model.getContent().getScreens() == null) {
      return false;
    }
    for (Screen screen : model.getContent().getScreens()) {
      if (fieldReferenced(screen.getScreenElements(), elementRef)) {
        return true;
      }
    }
    return false;
  }

  public static boolean isGroupReferenced(FormModel model, String groupRef) {
    if (model.getContent() == null || model.getContent().getScreens() == null) {
      return false;
    }
    for (Screen screen : model.getContent().getScreens()) {
      if (groupReferenced(screen.getScreenElements(), groupRef)) {
        return true;
      }
    }
    return false;
  }

  private static boolean fieldReferenced(List<ScreenElement> elements, String elementRef) {
    if (elements == null) {
      return false;
    }
    for (ScreenElement element : elements) {
      if (element instanceof Section section && fieldReferenced(section.getScreenElements(), elementRef)) {
        return true;
      }
      else if (element instanceof MultiColumnSection section && fieldReferenced(section.getScreenElements(), elementRef)) {
        return true;
      }
      else if (element instanceof ControlGrid grid && gridReferencesField(grid, elementRef)) {
        return true;
      }
      else if (element instanceof AbstractRepeat repeat) {
        if (columnsReferenceField(repeat.getRepeatOverviewColumn(), elementRef)) {
          return true;
        }
        if (element instanceof EmbeddedRepeat er && er.getControlGrid() != null && gridReferencesField(er.getControlGrid(), elementRef)) {
          return true;
        }
        if (element instanceof DetachedRepeat dr && dr.getDetailScreen() != null
            && fieldReferenced(dr.getDetailScreen().getScreenElements(), elementRef)) {
          return true;
        }
      }
    }
    return false;
  }

  private static boolean gridReferencesField(ControlGrid grid, String elementRef) {
    for (Row row : grid.getRow()) {
      for (Cell cell : row.getCell()) {
        if (cell instanceof Control control && elementRef.equals(control.getElementRef())) {
          return true;
        }
      }
    }
    return false;
  }

  private static boolean columnsReferenceField(List<RepeatOverviewColumn> columns, String elementRef) {
    for (RepeatOverviewColumn column : columns) {
      if (column instanceof FieldBasedRepeatOverviewColumn fieldColumn && elementRef.equals(fieldColumn.getElementRef())) {
        return true;
      }
    }
    return false;
  }

  private static boolean groupReferenced(List<ScreenElement> elements, String groupRef) {
    if (elements == null) {
      return false;
    }
    for (ScreenElement element : elements) {
      if (element instanceof Section section && groupReferenced(section.getScreenElements(), groupRef)) {
        return true;
      }
      else if (element instanceof MultiColumnSection section && groupReferenced(section.getScreenElements(), groupRef)) {
        return true;
      }
      else if (element instanceof AbstractRepeat repeat) {
        if (groupRef.equals(repeat.getGroupRef())) {
          return true;
        }
        if (element instanceof DetachedRepeat dr && dr.getDetailScreen() != null
            && groupReferenced(dr.getDetailScreen().getScreenElements(), groupRef)) {
          return true;
        }
      }
    }
    return false;
  }
}
