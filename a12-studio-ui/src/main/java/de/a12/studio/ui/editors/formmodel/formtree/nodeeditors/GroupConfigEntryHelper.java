package de.a12.studio.ui.editors.formmodel.formtree.nodeeditors;

import de.a12.studio.models.formmodel.AbstractRepeat;
import de.a12.studio.models.formmodel.FormModelContent;
import de.a12.studio.models.formmodel.GroupConfigEntry;
import de.a12.studio.models.formmodel.GroupConfiguration;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Shared helper for Repeat node editor sub-panels that need to locate or create the
 * {@link GroupConfigEntry} for an {@link AbstractRepeat}'s {@link AbstractRepeat#getGroupRef()} in
 * {@link FormModelContent#getGroupConfiguration()}.
 * <p>
 * Mirrors {@link FieldConfigEntryHelper} for fields: returns a detached entry when content is absent
 * or the repeat has no group reference, so callers never need null-checks.
 */
final class GroupConfigEntryHelper {

  private GroupConfigEntryHelper() {}

  @NonNull
  static GroupConfigEntry findOrCreate(@NonNull AbstractRepeat repeat,
      @Nullable FormModelContent content) {
    if (content == null || repeat.getGroupRef() == null) {
      return new GroupConfigEntry();
    }
    if (content.getGroupConfiguration() == null) {
      content.setGroupConfiguration(new GroupConfiguration());
    }
    for (GroupConfigEntry entry : content.getGroupConfiguration().getGroup()) {
      if (repeat.getGroupRef().equals(entry.getGroupRef())) {
        return entry;
      }
    }
    GroupConfigEntry newEntry = new GroupConfigEntry();
    newEntry.setGroupRef(repeat.getGroupRef());
    content.getGroupConfiguration().getGroup().add(newEntry);
    return newEntry;
  }
}
