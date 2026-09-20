package de.a12.studio.modelsvalidation.validators.form;

import de.a12.studio.models.documentmodel.BooleanFieldType;
import de.a12.studio.models.documentmodel.ConfirmFieldType;
import de.a12.studio.models.documentmodel.EnumerationFieldType;
import de.a12.studio.models.documentmodel.EnumerationValue;
import de.a12.studio.models.documentmodel.FieldElement;
import de.a12.studio.models.documentmodel.FieldType;
import de.a12.studio.models.formmodel.AbstractRepeat;
import de.a12.studio.models.formmodel.Control;
import de.a12.studio.models.formmodel.ControlGrid;
import de.a12.studio.models.formmodel.CustomScreenElement;
import de.a12.studio.models.formmodel.FormModelContent;
import de.a12.studio.models.formmodel.FormModelWalker;
import de.a12.studio.models.formmodel.MultiColumnSection;
import de.a12.studio.models.formmodel.Screen;
import de.a12.studio.models.formmodel.ScreenElement;
import de.a12.studio.models.formmodel.Section;
import de.a12.studio.modelsvalidation.validators.ElementIndex;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * The rules of SME's "Dependent Controls" (a Control hiding whole blocks of the screen by its value, stored on the
 * master Control as {@link Control#getDependentControls()}) that both the editor's Dependencies tab and the
 * validators need: which Controls may be a master and for which values, and which screen elements may be hidden.
 * Ports {@code isPossibleDependentControlMaster}, {@code isAllowedDependentControlType} and
 * {@code areControlAndScreenElementCompatible} from SME's {@code fmElements/types}.
 *
 * <p>A master value of {@code null} is the "(no value)" case, as everywhere else in the Form Model.</p>
 */
public final class DependentControlSupport {

  private DependentControlSupport() {
  }

  /**
   * The values a master field of {@code effectiveType} can trigger on, "(no value)" ({@code null}) first as in SME's
   * {@code createDefaultMap}: Boolean {@code false}/{@code true}, Confirm {@code true}, Enumeration its declared
   * values. Empty for every other type - such a field cannot be a dependent-controls master.
   */
  public static List<String> masterValues(@Nullable FieldType effectiveType) {
    List<String> values = new ArrayList<>();
    if (effectiveType instanceof BooleanFieldType) {
      values.add(null);
      values.add("false");
      values.add("true");
    }
    else if (effectiveType instanceof ConfirmFieldType) {
      values.add(null);
      values.add("true");
    }
    else if (effectiveType instanceof EnumerationFieldType enumType && enumType.getEnumerationType() != null) {
      values.add(null);
      for (EnumerationValue value : enumType.getEnumerationType().getValues()) {
        values.add(value.getValue());
      }
    }
    return values;
  }

  /** The type of the Document Model field {@code elementRef} resolves to, type definitions resolved; empty if it is no field. */
  public static Optional<FieldType> effectiveFieldType(@Nullable String elementRef, @Nullable ElementIndex index) {
    if (elementRef == null || elementRef.isBlank() || index == null) {
      return Optional.empty();
    }
    return index.resolveElement(elementRef)
        .filter(FieldElement.class::isInstance)
        .map(FieldElement.class::cast)
        .filter(field -> field.getField() != null)
        .map(field -> index.effectiveFieldType(field.getField().getFieldType()));
  }

  /**
   * The master values of {@code control}'s field: empty when the Control's field does not resolve or is not
   * Boolean/Confirm/Enumeration, i.e. when the Control cannot be a master (SME's
   * {@code isPossibleDependentControlMaster}).
   */
  public static List<String> masterValues(@NonNull Control control, @Nullable ElementIndex index) {
    return effectiveFieldType(control.getElementRef(), index).map(DependentControlSupport::masterValues).orElse(List.of());
  }

  /** SME's {@code isAllowedDependentControlType}: only whole blocks can be hidden, not single Controls. */
  public static boolean isAllowedType(@Nullable Object element) {
    return element instanceof Section || element instanceof MultiColumnSection || element instanceof ControlGrid
        || element instanceof CustomScreenElement;
  }

  /** The top-level screen {@code node} is somewhere below; the dependency of a Control must stay within it. */
  public static Optional<Screen> topLevelScreen(@NonNull Object node, @Nullable FormModelContent content) {
    if (content == null) {
      return Optional.empty();
    }
    return content.getScreens().stream()
        .filter(screen -> screen == node || containsNode(screen, node))
        .findFirst();
  }

  /**
   * Whether {@code candidate} may be hidden by {@code master}: an allowed type, not a container of the master itself
   * (it would hide its own trigger) and in a compatible data context (SME's
   * {@code areControlAndScreenElementCompatible}). The compatible-context rule exists because a dependent element
   * must be controlled by exactly one instance of the trigger field: a Control that needs an index (it sits outside
   * the repeat of its field) can only control elements of the repeat it sits in itself, any other Control only
   * elements that show at most as many repetitions as its field has.
   */
  public static boolean isCandidate(@NonNull ScreenElement candidate, @NonNull Control master,
      @Nullable FormModelContent content, @Nullable ElementIndex index) {
    return isAllowedType(candidate)
        && !containsNode(candidate, master)
        && isCompatible(candidate, master, content, index);
  }

  /** Every element of {@code screen} {@code master} could hide, in document order. */
  public static List<ScreenElement> candidates(@NonNull Control master, @NonNull Screen screen,
      @Nullable FormModelContent content, @Nullable ElementIndex index) {
    return FormModelWalker.find(screen, ScreenElement.class, node -> true).stream()
        .filter(element -> isCandidate(element, master, content, index))
        .toList();
  }

  /** SME's {@code areControlAndScreenElementCompatible}. */
  public static boolean isCompatible(@NonNull ScreenElement element, @NonNull Control master,
      @Nullable FormModelContent content, @Nullable ElementIndex index) {
    if (content == null || index == null) {
      return true;
    }
    if (ControlIndexSupport.isIndexable(master, content, index)) {
      return enclosingRepeat(master, content) == enclosingRepeat(element, content);
    }
    List<String> controlGranularity = index.granularity(master.getElementRef());
    List<String> elementGranularity = ControlIndexSupport.enclosingRepeat(element, content)
        .map(AbstractRepeat::getGroupRef)
        .map(index::granularity)
        .orElse(List.of());
    for (int i = 0; i < Math.min(controlGranularity.size(), elementGranularity.size()); i++) {
      if (!controlGranularity.get(i).equals(elementGranularity.get(i))) {
        return false;
      }
    }
    return controlGranularity.size() <= elementGranularity.size();
  }

  private static AbstractRepeat enclosingRepeat(Object node, FormModelContent content) {
    return ControlIndexSupport.enclosingRepeat(node, content).orElse(null);
  }

  /** Whether {@code node} is {@code container} itself or anywhere below it. */
  private static boolean containsNode(Object container, Object node) {
    return container == node || FormModelWalker.find(container, node.getClass(), n -> true).stream().anyMatch(n -> n == node);
  }
}
