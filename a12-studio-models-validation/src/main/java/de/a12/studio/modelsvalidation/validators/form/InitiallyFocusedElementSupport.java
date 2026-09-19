package de.a12.studio.modelsvalidation.validators.form;

import de.a12.studio.models.formmodel.AbstractRepeat;
import de.a12.studio.models.formmodel.Control;
import de.a12.studio.models.formmodel.ControlGrid;
import de.a12.studio.models.formmodel.FormModelContent;
import de.a12.studio.models.formmodel.FormModelWalker;
import de.a12.studio.models.formmodel.Screen;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * Which Controls can take the initial keyboard focus of a Form Model ({@link Screen#getInitiallyFocusedElementId()}),
 * shared by the Screen editor's picker and {@link FormInitiallyFocusedElementValidator}. Mirrors SME's
 * {@code focusableElementsEnum}: only the <em>first</em> screen can name one, and its candidates are the
 * editable Controls of that screen outside of any repeat - a readonly Control, and every Control in a readonly
 * Control Grid, cannot take focus, and a Control in a repeat exists once per row so it is not addressable.
 */
public final class InitiallyFocusedElementSupport {

  private InitiallyFocusedElementSupport() {
  }

  /** Whether {@code screen} is the first screen of {@code content}, the only one that may set an initial focus. */
  public static boolean isFirstScreen(@Nullable FormModelContent content, @NonNull Screen screen) {
    return content != null && !content.getScreens().isEmpty() && content.getScreens().get(0) == screen;
  }

  /** The Controls of {@code screen} that can take the initial focus, in document order. */
  public static List<Control> focusableControls(@NonNull Screen screen) {
    return FormModelWalker.find(screen, Control.class, InitiallyFocusedElementSupport::descend).stream()
        .filter(control -> !Boolean.TRUE.equals(control.getReadonly()))
        .toList();
  }

  // Repeats hold their Controls once per row, a readonly grid makes its Controls readonly.
  private static boolean descend(Object node) {
    return !(node instanceof AbstractRepeat) && !(node instanceof ControlGrid grid && Boolean.TRUE.equals(grid.getReadonly()));
  }
}
