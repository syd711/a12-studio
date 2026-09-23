package de.a12.studio.modelsvalidation.validators.form;

import de.a12.studio.models.formmodel.Binding;
import de.a12.studio.models.formmodel.BindingContent;
import de.a12.studio.models.formmodel.BindingRepeat;
import de.a12.studio.models.formmodel.DetachedRepeat;
import de.a12.studio.models.formmodel.FormModel;
import de.a12.studio.models.formmodel.MultiColumnSection;
import de.a12.studio.models.formmodel.Screen;
import de.a12.studio.models.formmodel.ScreenElement;
import de.a12.studio.models.formmodel.Section;

import java.util.ArrayList;
import java.util.List;

/**
 * Every {@link Binding} and {@link BindingRepeat} node's {@code binding} content in a Form Model's Screens tree,
 * paired with the owning element's id - shared by the {@code FormBinding*}/{@code FormBindingComponent*}
 * validators, which validate the same {@link BindingContent} shape whether it comes from a plain {@link Binding}
 * or is wrapped inside a to-many {@link BindingRepeat}.
 */
final class FormBindingElements {

  private FormBindingElements() {
  }

  /** One {@link Binding}/{@link BindingRepeat} node's id, paired with its (possibly null) {@code binding} content. */
  record BindingHolder(String elementId, BindingContent content) {
  }

  static List<BindingHolder> findBindingContents(FormModel model) {
    List<BindingHolder> holders = new ArrayList<>();
    if (model.getContent() != null && model.getContent().getScreens() != null) {
      for (Screen screen : model.getContent().getScreens()) {
        visit(screen.getScreenElements(), holders);
      }
    }
    return holders;
  }

  private static void visit(List<ScreenElement> elements, List<BindingHolder> holders) {
    if (elements == null) {
      return;
    }
    for (ScreenElement element : elements) {
      if (element instanceof Binding binding) {
        holders.add(new BindingHolder(binding.getId(), binding.getBinding()));
      }
      else if (element instanceof BindingRepeat bindingRepeat) {
        holders.add(new BindingHolder(bindingRepeat.getId(), bindingRepeat.getBinding()));
      }
      else if (element instanceof Section section) {
        visit(section.getScreenElements(), holders);
      }
      else if (element instanceof MultiColumnSection section) {
        visit(section.getScreenElements(), holders);
      }
      else if (element instanceof DetachedRepeat repeat && repeat.getDetailScreen() != null) {
        visit(repeat.getDetailScreen().getScreenElements(), holders);
      }
      // ControlGrid's Rows/Cells and EmbeddedRepeat's own ControlGrid slot can't hold a Binding/BindingRepeat
      // (they're always a sibling ScreenElement in a screenElements list), so no further recursion is needed
      // for those types.
    }
  }
}
