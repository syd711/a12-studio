package de.a12.studio.modelsvalidation.validators.form;

import de.a12.studio.models.formmodel.AbstractRepeat;
import de.a12.studio.models.formmodel.DetachedRepeat;
import de.a12.studio.models.formmodel.FormModelContent;
import de.a12.studio.models.formmodel.MultiColumnSection;
import de.a12.studio.models.formmodel.Screen;
import de.a12.studio.models.formmodel.ScreenElement;
import de.a12.studio.models.formmodel.Section;

import java.util.ArrayList;
import java.util.List;

/** Shared traversal collecting every Repeat in a Form Model's screen tree, including those in a detail screen. */
final class FormRepeats {

  private FormRepeats() {
  }

  static List<AbstractRepeat> collect(FormModelContent content) {
    List<AbstractRepeat> repeats = new ArrayList<>();
    if (content != null && content.getScreens() != null) {
      for (Screen screen : content.getScreens()) {
        visit(screen.getScreenElements(), repeats);
      }
    }
    return repeats;
  }

  private static void visit(List<ScreenElement> elements, List<AbstractRepeat> repeats) {
    if (elements == null) {
      return;
    }
    for (ScreenElement element : elements) {
      if (element instanceof AbstractRepeat repeat) {
        repeats.add(repeat);
        if (repeat instanceof DetachedRepeat detached && detached.getDetailScreen() != null) {
          visit(detached.getDetailScreen().getScreenElements(), repeats);
        }
      }
      else if (element instanceof Section section) {
        visit(section.getScreenElements(), repeats);
      }
      else if (element instanceof MultiColumnSection section) {
        visit(section.getScreenElements(), repeats);
      }
    }
  }
}
