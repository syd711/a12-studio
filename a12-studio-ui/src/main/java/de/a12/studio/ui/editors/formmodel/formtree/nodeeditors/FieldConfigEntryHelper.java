package de.a12.studio.ui.editors.formmodel.formtree.nodeeditors;

import de.a12.studio.models.formmodel.Control;
import de.a12.studio.models.formmodel.FieldConfigEntry;
import de.a12.studio.models.formmodel.FieldConfiguration;
import de.a12.studio.models.formmodel.FormModelContent;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Shared helper for the Control node editor's sub-panels ({@link ControlLabelPanelController},
 * {@link ControlHintPanelController}, {@link ControlPlaceholderPanelController}) that need to locate or
 * create the {@link FieldConfigEntry} for a {@link Control}'s {@link Control#getElementRef()} in the
 * form model's {@link FormModelContent#getFieldConfiguration()} list.
 * <p>
 * When no content is available or the control has no element reference, returns a detached entry so the
 * caller can still read/write against it without null-checks; writes to a detached entry are silently
 * discarded (they never reach the saved model).
 */
final class FieldConfigEntryHelper {

  private FieldConfigEntryHelper() {}

  /**
   * Returns the existing {@link FieldConfigEntry} whose {@code elementRef} matches
   * {@code control.getElementRef()}, or a newly inserted one when none exists.
   * Returns a <em>detached</em> (not linked to the model) entry when {@code content} is {@code null}
   * or the control has no element reference, so callers never need to null-check.
   */
  @NonNull
  static FieldConfigEntry findOrCreate(@NonNull Control control, @Nullable FormModelContent content) {
    if (content == null || control.getElementRef() == null) {
      return new FieldConfigEntry(); // detached placeholder
    }
    if (content.getFieldConfiguration() == null) {
      content.setFieldConfiguration(new FieldConfiguration());
    }
    for (FieldConfigEntry entry : content.getFieldConfiguration().getField()) {
      if (control.getElementRef().equals(entry.getElementRef())) {
        return entry;
      }
    }
    FieldConfigEntry newEntry = new FieldConfigEntry();
    newEntry.setElementRef(control.getElementRef());
    content.getFieldConfiguration().getField().add(newEntry);
    return newEntry;
  }
}
