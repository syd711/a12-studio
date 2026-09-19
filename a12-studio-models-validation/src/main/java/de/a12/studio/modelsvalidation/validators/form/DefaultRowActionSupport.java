package de.a12.studio.modelsvalidation.validators.form;

import de.a12.studio.models.Label;
import de.a12.studio.models.formmodel.AbstractRepeat;
import de.a12.studio.models.formmodel.DefaultRowAction;
import de.a12.studio.models.formmodel.DetachedRepeat;
import de.a12.studio.models.formmodel.EmbeddedRepeat;
import de.a12.studio.models.formmodel.RowAction;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Rules for a Detached/Embedded Repeat's default row action - the row action executed when a row is clicked -
 * ported from SME's {@code references/customRowActions}, {@code updateRowAction}, {@code deleteRowAction} and
 * {@code disableMultiFileUpload} (SME docs, "Default Row Action" and the refactoring table).
 * <p>
 * The wire shape is {@code {"event": "edit"}} for a built-in action and {@code {"event": "x", "custom": true}}
 * for one of the repeat's own row actions. SME's editor works on a "technical event" instead, where the built-in
 * ones carry the prefix {@value #BUILT_IN_PREFIX}; {@link #technicalEvent} and {@link #fromTechnicalEvent}
 * convert. A custom row action can only be the default while it has no confirmation, since a click on a row
 * must not open a dialog first, and the default has to follow that action through renames and deletions -
 * {@link #reconcile} does that.
 */
public final class DefaultRowActionSupport {

  public static final String BUILT_IN_PREFIX = "a12-built-in-";
  public static final String EDIT = BUILT_IN_PREFIX + "edit";
  public static final String DOWNLOAD = BUILT_IN_PREFIX + "download";

  private DefaultRowActionSupport() {
  }

  /** Whether the repeat type has a default row action at all (Detached and Embedded, not Inline). */
  public static boolean isSupported(AbstractRepeat repeat) {
    return repeat instanceof DetachedRepeat || repeat instanceof EmbeddedRepeat;
  }

  /** The editor-side identifier of {@code action}: its event, with the built-in prefix for a non-custom one. */
  public static @Nullable String technicalEvent(@Nullable DefaultRowAction action) {
    if (action == null || action.getEvent() == null || action.getEvent().isBlank()) {
      return null;
    }
    return Boolean.TRUE.equals(action.getCustom()) ? action.getEvent() : BUILT_IN_PREFIX + action.getEvent();
  }

  /** The default row action for {@code technicalEvent}, keeping {@code hideButton}; {@code null} for none. */
  public static @Nullable DefaultRowAction fromTechnicalEvent(@Nullable String technicalEvent, @Nullable Boolean hideButton) {
    if (technicalEvent == null || technicalEvent.isBlank()) {
      return null;
    }
    DefaultRowAction action = new DefaultRowAction();
    if (technicalEvent.startsWith(BUILT_IN_PREFIX)) {
      action.setEvent(technicalEvent.substring(BUILT_IN_PREFIX.length()));
    }
    else {
      action.setEvent(technicalEvent);
      action.setCustom(Boolean.TRUE);
    }
    action.setHideButton(hideButton);
    return action;
  }

  /**
   * What the repeat's default row action may be, in display order: "edit", "download" if multi file upload is
   * enabled, then every custom row action's event that has no confirmation (each event once).
   */
  public static List<String> candidates(AbstractRepeat repeat) {
    List<String> candidates = new ArrayList<>();
    candidates.add(EDIT);
    if (isMultiFileUpload(repeat)) {
      candidates.add(DOWNLOAD);
    }
    Set<String> withConfirmation = eventsWithConfirmation(repeat);
    Set<String> custom = new LinkedHashSet<>();
    for (RowAction action : actions(repeat)) {
      if (action.getEvent() != null && !action.getEvent().isBlank() && !withConfirmation.contains(action.getEvent())) {
        custom.add(action.getEvent());
      }
    }
    candidates.addAll(custom);
    return candidates;
  }

  /** The events of the repeat's custom row actions, in order (one entry per action, blanks kept as {@code null}). */
  public static List<String> events(AbstractRepeat repeat) {
    List<String> events = new ArrayList<>();
    for (RowAction action : actions(repeat)) {
      events.add(action.getEvent());
    }
    return events;
  }

  /**
   * Keeps a custom default row action valid after the repeat's row actions changed. {@code previousEvents} is
   * {@link #events} from before the change. The default is cleared when its row action was deleted or now has
   * a confirmation, and follows it when it was renamed (same position in a list of unchanged length). A
   * built-in default is left alone. Clearing drops the whole {@code defaultRowAction}, {@code hideButton}
   * included, like SME.
   *
   * @return whether the repeat's default row action changed
   */
  public static boolean reconcile(AbstractRepeat repeat, List<String> previousEvents) {
    DefaultRowAction current = repeat.getDefaultRowAction();
    if (current == null || !Boolean.TRUE.equals(current.getCustom()) || current.getEvent() == null) {
      return false;
    }
    List<String> currentEvents = events(repeat);
    String target;
    if (currentEvents.contains(current.getEvent())) {
      target = current.getEvent();
    }
    else {
      int index = previousEvents.indexOf(current.getEvent());
      target = index >= 0 && previousEvents.size() == currentEvents.size() ? currentEvents.get(index) : null;
    }
    if (target != null && (target.isBlank() || eventsWithConfirmation(repeat).contains(target))) {
      target = null;
    }
    if (target == null) {
      repeat.setDefaultRowAction(null);
      return true;
    }
    if (!target.equals(current.getEvent())) {
      current.setEvent(target);
      return true;
    }
    return false;
  }

  /**
   * To be called when multi file upload was switched off: a "download" default has nothing to download from
   * any more and is cleared.
   *
   * @return whether the repeat's default row action changed
   */
  public static boolean onMultiFileUploadDisabled(AbstractRepeat repeat) {
    if (DOWNLOAD.equals(technicalEvent(repeat.getDefaultRowAction()))) {
      repeat.setDefaultRowAction(null);
      return true;
    }
    return false;
  }

  public static boolean hasConfirmation(RowAction action) {
    return hasTexts(action.getConfirmation() == null ? null : action.getConfirmation().getText())
        || hasTexts(action.getConfirmationDialogTitle() == null ? null : action.getConfirmationDialogTitle().getText());
  }

  // SME's isMultilingualTextEmpty: a text counts once at least one locale has actual (non-blank) text.
  private static boolean hasTexts(@Nullable List<Label> texts) {
    return texts != null && texts.stream().anyMatch(label -> label.getText() != null && !label.getText().isBlank());
  }

  static boolean isMultiFileUpload(AbstractRepeat repeat) {
    // Only an Embedded repeat can have both a default row action and multi file upload (SME).
    return repeat instanceof EmbeddedRepeat embedded && Boolean.TRUE.equals(embedded.getMultiFileUpload());
  }

  private static Set<String> eventsWithConfirmation(AbstractRepeat repeat) {
    Set<String> events = new LinkedHashSet<>();
    for (RowAction action : actions(repeat)) {
      if (action.getEvent() != null && hasConfirmation(action)) {
        events.add(action.getEvent());
      }
    }
    return events;
  }

  private static List<RowAction> actions(AbstractRepeat repeat) {
    return repeat.getRowActionGroup() == null ? List.of() : repeat.getRowActionGroup().getAction();
  }
}
