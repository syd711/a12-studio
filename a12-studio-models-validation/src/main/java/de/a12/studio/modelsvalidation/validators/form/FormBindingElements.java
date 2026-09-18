package de.a12.studio.modelsvalidation.validators.form;

import de.a12.studio.models.formmodel.Binding;
import de.a12.studio.models.formmodel.DetachedRepeat;
import de.a12.studio.models.formmodel.FormModel;
import de.a12.studio.models.formmodel.MultiColumnSection;
import de.a12.studio.models.formmodel.Screen;
import de.a12.studio.models.formmodel.ScreenElement;
import de.a12.studio.models.formmodel.Section;

import java.util.ArrayList;
import java.util.List;

/** Every {@link Binding} node in a Form Model's Screens tree - shared by {@link FormBindingRelationshipReferenceValidator}/{@link FormBindingTargetRoleValidator}. */
final class FormBindingElements {

  private FormBindingElements() {
  }

  static List<Binding> findBindings(FormModel model) {
    List<Binding> bindings = new ArrayList<>();
    if (model.getContent() != null && model.getContent().getScreens() != null) {
      for (Screen screen : model.getContent().getScreens()) {
        visit(screen.getScreenElements(), bindings);
      }
    }
    return bindings;
  }

  private static void visit(List<ScreenElement> elements, List<Binding> bindings) {
    if (elements == null) {
      return;
    }
    for (ScreenElement element : elements) {
      if (element instanceof Binding binding) {
        bindings.add(binding);
      }
      else if (element instanceof Section section) {
        visit(section.getScreenElements(), bindings);
      }
      else if (element instanceof MultiColumnSection section) {
        visit(section.getScreenElements(), bindings);
      }
      else if (element instanceof DetachedRepeat repeat && repeat.getDetailScreen() != null) {
        visit(repeat.getDetailScreen().getScreenElements(), bindings);
      }
      // ControlGrid's Rows/Cells and EmbeddedRepeat's own ControlGrid slot can't hold a Binding (it's always a
      // sibling ScreenElement in a screenElements list), so no further recursion is needed for those types.
    }
  }
}
