package de.a12.studio.ui.preferences;

import de.a12.studio.models.projects.Project;
import de.a12.studio.models.projects.settings.annotations.AnnotationDataSet;
import de.a12.studio.models.projects.settings.annotations.AnnotationModelSet;
import de.a12.studio.models.projects.settings.annotations.NameUsage;
import de.a12.studio.models.util.JsonSettings;
import org.jspecify.annotations.NonNull;

/**
 * Helpers around {@link AnnotationDataSet} used by {@link PreferenceAnnotationSetsController} and the annotation set
 * dialogs: building the live "default" set from a project's annotation registries, counting table column
 * values, and cloning a set so tree pruning in a dialog never mutates the persisted/live source.
 */
public final class AnnotationDataSetSupport {

  public static final String DEFAULT_NAME = "default";

  private AnnotationDataSetSupport() {
  }

  /**
   * Builds the "default" row: a live view of every annotation currently used across the project, sourced
   * directly from {@link Project#getAnnotationHeaderRegistry()} / {@link Project#getAnnotationFieldRegistry()}.
   * The returned set wraps the registries' sets by reference and must never be mutated in place - only
   * {@link #deepCopy} of it may be edited (e.g. for Export).
   */
  public static AnnotationDataSet buildDefault(@NonNull Project project) {
    AnnotationDataSet dataSet = new AnnotationDataSet();
    dataSet.setName(DEFAULT_NAME);
    dataSet.setHeaderSet(project.getAnnotationHeaderRegistry().getHeaderSet());
    dataSet.setFieldSet(project.getAnnotationFieldRegistry().getFieldSet());
    return dataSet;
  }

  /**
   * Total usage count (sum of {@link NameUsage#getCount()}) of every header annotation in the set.
   */
  public static int countHeaderEntries(@NonNull AnnotationDataSet dataSet) {
    int count = 0;
    for (AnnotationModelSet modelSet : dataSet.getHeaderSet().getModelTypes().values()) {
      for (NameUsage usage : modelSet.getValues().values()) {
        count += usage.getCount();
      }
    }
    return count;
  }

  /**
   * Total usage count (sum of {@link NameUsage#getCount()}) of every content (field) annotation in the set.
   */
  public static int countFieldEntries(@NonNull AnnotationDataSet dataSet) {
    int count = 0;
    for (AnnotationModelSet modelSet : dataSet.getFieldSet().getModelTypes().values()) {
      for (NameUsage usage : modelSet.getValues().values()) {
        count += usage.getCount();
      }
    }
    return count;
  }

  /**
   * Deep-clones {@code source} via a Jackson round-trip, so callers (Export/Edit dialogs) can prune the
   * copy's tree without affecting the persisted or live-registry-backed original.
   */
  public static AnnotationDataSet deepCopy(@NonNull AnnotationDataSet source) {
    return JsonSettings.objectMapper.convertValue(source, AnnotationDataSet.class);
  }
}
