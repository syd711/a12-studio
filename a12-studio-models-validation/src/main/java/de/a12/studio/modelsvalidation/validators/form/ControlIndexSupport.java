package de.a12.studio.modelsvalidation.validators.form;

import de.a12.studio.models.formmodel.AbstractRepeat;
import de.a12.studio.models.formmodel.Control;
import de.a12.studio.models.formmodel.DetachedRepeat;
import de.a12.studio.models.formmodel.EmbeddedRepeat;
import de.a12.studio.models.formmodel.FormModelContent;
import de.a12.studio.models.formmodel.FormModelWalker;
import de.a12.studio.modelsvalidation.validators.ElementIndex;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * When a Control needs an index ({@link Control#getIndex()}, SME's "Control Index"): when it is placed outside
 * the repeat of the repeatable group its field lives in, so it has to say which repetition it shows. SME's
 * {@code isIndexableControl}: the granularity distance from the Control's <em>data context</em> to its field is
 * positive, i.e. the field sits below more repeatable groups than the enclosing Embedded/Detached Repeat (or
 * the model root, outside any repeat) already provides. A Control inside its own repeat, or one that only shows
 * ancestor data, has no use for an index.
 */
public final class ControlIndexSupport {

  private ControlIndexSupport() {
  }

  /** Whether {@code control} sits outside the repeat of (some of) the repeatable groups its field is in. */
  public static boolean isIndexable(@NonNull Control control, @Nullable FormModelContent content, @Nullable ElementIndex elementIndex) {
    if (content == null || elementIndex == null || elementIndex.resolveElement(control.getElementRef()).isEmpty()) {
      return false;
    }
    List<String> controlGranularity = elementIndex.granularity(control.getElementRef());
    List<String> contextGranularity = dataContextGroupRef(control, content).map(elementIndex::granularity).orElse(List.of());
    for (int i = 0; i < Math.min(controlGranularity.size(), contextGranularity.size()); i++) {
      if (!controlGranularity.get(i).equals(contextGranularity.get(i))) {
        // Not below the data context at all: SME's distance is "infinite", which counts as needing an index.
        return true;
      }
    }
    return controlGranularity.size() > contextGranularity.size();
  }

  /**
   * The {@code groupRef} of the nearest Embedded/Detached Repeat {@code control} is placed in, i.e. the
   * repeatable group whose current row the Control's data comes from; empty outside any repeat (the model root).
   */
  public static Optional<String> dataContextGroupRef(@NonNull Control control, @NonNull FormModelContent content) {
    // A nested repeat is itself found inside the outer one, so the nearest enclosing repeat is the one holding
    // the fewest Controls.
    return FormModelWalker.find(content, AbstractRepeat.class).stream()
        .filter(repeat -> repeat instanceof EmbeddedRepeat || repeat instanceof DetachedRepeat)
        .filter(repeat -> FormModelWalker.find(repeat, Control.class, node -> true).contains(control))
        .min(Comparator.comparingInt(repeat -> FormModelWalker.find(repeat, Control.class, node -> true).size()))
        .map(AbstractRepeat::getGroupRef);
  }
}
