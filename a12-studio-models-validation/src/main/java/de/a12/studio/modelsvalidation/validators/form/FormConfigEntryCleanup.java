package de.a12.studio.modelsvalidation.validators.form;

import de.a12.studio.models.formmodel.FieldConfigEntry;
import de.a12.studio.models.formmodel.FormModel;
import de.a12.studio.models.formmodel.GroupConfigEntry;
import de.a12.studio.modelsvalidation.validators.ElementIndex;

import java.util.List;
import java.util.function.Predicate;

/**
 * Removes {@link FieldConfigEntry}/{@link GroupConfigEntry} rows that {@link FormFieldReferenceValidator}/{@link
 * FormGroupReferenceValidator} would report as dangling (no longer resolvable against the linked Document Model)
 * or {@link FormUnusedConfigEntryValidator} would report as unused (resolvable, but no longer referenced by any
 * Control/Repeat in the Screens tree) - the automatic replacement for SME's manual "Clean All" action, invoked by
 * the Form Model editor whenever it's opened or its tab is reselected rather than through a dedicated UI.
 */
public final class FormConfigEntryCleanup {

  private FormConfigEntryCleanup() {}

  /** @return how many entries were removed, so the caller can decide whether a save/notification is warranted. */
  public static int removeDanglingAndUnusedEntries(FormModel model, List<ElementIndex> indexes) {
    if (model.getContent() == null) {
      return 0;
    }
    int removed = 0;
    if (model.getContent().getFieldConfiguration() != null) {
      List<FieldConfigEntry> fields = model.getContent().getFieldConfiguration().getField();
      removed += removeMatching(fields, entry -> shouldRemove(entry.getElementRef(), indexes,
          ref -> FormReferences.isFieldReferenced(model, ref)));
    }
    if (model.getContent().getGroupConfiguration() != null) {
      List<GroupConfigEntry> groups = model.getContent().getGroupConfiguration().getGroup();
      removed += removeMatching(groups, entry -> shouldRemove(entry.getGroupRef(), indexes,
          ref -> FormReferences.isGroupReferenced(model, ref)));
    }
    return removed;
  }

  private static <T> int removeMatching(List<T> entries, Predicate<T> shouldRemove) {
    int before = entries.size();
    entries.removeIf(shouldRemove);
    return before - entries.size();
  }

  private static boolean shouldRemove(String reference, List<ElementIndex> indexes, Predicate<String> referenced) {
    if (reference == null || reference.isBlank()) {
      return false;
    }
    boolean resolvable = indexes.stream().anyMatch(index -> index.isResolvable(reference));
    return !resolvable || !referenced.test(reference);
  }
}
