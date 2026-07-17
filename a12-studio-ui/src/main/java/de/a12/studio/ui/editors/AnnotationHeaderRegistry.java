package de.a12.studio.ui.editors;

import de.a12.studio.dataservices.models.Annotation;
import de.a12.studio.dataservices.models.ModelType;
import de.a12.studio.dataservices.models.documentmodel.DocumentModel;
import de.a12.studio.dataservices.projects.Project;
import de.a12.studio.dataservices.projects.ProjectItem;
import de.a12.studio.ui.events.ProjectOpenedEvent;
import de.a12.studio.ui.events.StudioEventListener;
import de.a12.studio.ui.events.StudioEventManager;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Collects the annotation names (and their most recently used value) stored at the json path
 * {@code /header/annotations} across every document model in the project, grouped by model type, so property
 * editors can suggest previously used header annotation names instead of requiring users to retype them, and
 * prefill the value that went with a suggested name.
 * Names are reference-counted per model type so callers can incrementally {@link #addName} / {@link #removeName}
 * as header annotations are edited, without requiring a full {@link #rebuild} of the project.
 */
public final class AnnotationHeaderRegistry implements StudioEventListener {

  private static final AnnotationHeaderRegistry INSTANCE = new AnnotationHeaderRegistry();

  private Map<ModelType, TreeMap<String, NameUsage>> countsByModelType = new HashMap<>();

  private AnnotationHeaderRegistry() {
    StudioEventManager.getInstance().addListener(this);
  }

  public static AnnotationHeaderRegistry getInstance() {
    return INSTANCE;
  }

  @Override
  public void projectOpened(@NonNull ProjectOpenedEvent event) {
    rebuild(event.getProject());
  }

  public void rebuild(@NonNull Project project) {
    List<ProjectItem> documentItems = new ArrayList<>();
    collectDocumentItems(project.getRoot(), documentItems);

    Map<ModelType, TreeMap<String, NameUsage>> collected = new HashMap<>();
    for (ProjectItem item : documentItems) {
      if (item.getModel() instanceof DocumentModel documentModel) {
        for (Annotation annotation : documentModel.getAnnotations()) {
          if (annotation.getName() != null && !annotation.getName().isBlank()) {
            mergeUsage(collected.computeIfAbsent(documentModel.getModelType(), k -> new TreeMap<>()),
                annotation.getName(), annotation.getValue());
          }
        }
      }
    }
    this.countsByModelType = collected;
  }

  /**
   * Returns the header annotation names previously used for the given model type, sorted alphabetically.
   * Never {@code null}.
   */
  public @NonNull List<String> getNames(@Nullable ModelType modelType) {
    Map<String, NameUsage> counts = countsByModelType.get(modelType);
    return counts == null ? List.of() : new ArrayList<>(counts.keySet());
  }

  /**
   * Returns the value most recently used alongside {@code name} for the given model type, so a property editor
   * can prefill the value field when the user picks {@code name} from the suggestions. {@code null} if the name
   * isn't known for this model type.
   */
  public @Nullable String getValue(@Nullable ModelType modelType, @Nullable String name) {
    if (name == null || name.isBlank()) {
      return null;
    }
    Map<String, NameUsage> counts = countsByModelType.get(modelType);
    NameUsage usage = counts == null ? null : counts.get(name);
    return usage == null ? null : usage.value;
  }

  /**
   * Registers a new use of {@code name} (with {@code value}) for the given model type, so it's offered as a
   * suggestion even before the next full {@link #rebuild}. Blank names are ignored. Callers should pair this
   * with {@link #removeName} when a header annotation's name changes or the annotation is deleted, so the
   * registry stays in sync with what's actually used in the project.
   */
  public void addName(@Nullable ModelType modelType, @Nullable String name, @Nullable String value) {
    if (name == null || name.isBlank()) {
      return;
    }
    mergeUsage(countsByModelType.computeIfAbsent(modelType, k -> new TreeMap<>()), name, value);
  }

  /**
   * Reverses a previous {@link #addName} call, dropping {@code name} from the suggestions for this model type
   * once its last use is gone. Blank names are ignored.
   */
  public void removeName(@Nullable ModelType modelType, @Nullable String name) {
    if (name == null || name.isBlank()) {
      return;
    }
    TreeMap<String, NameUsage> counts = countsByModelType.get(modelType);
    if (counts == null) {
      return;
    }
    NameUsage usage = counts.get(name);
    if (usage != null && --usage.count <= 0) {
      counts.remove(name);
    }
    if (counts.isEmpty()) {
      countsByModelType.remove(modelType);
    }
  }

  /**
   * Updates the value on record for an already-registered {@code name}, without affecting its reference count.
   * Used when a header annotation's value is edited without its name changing. A no-op if the name isn't
   * registered for this model type.
   */
  public void setValue(@Nullable ModelType modelType, @Nullable String name, @Nullable String value) {
    if (name == null || name.isBlank()) {
      return;
    }
    TreeMap<String, NameUsage> counts = countsByModelType.get(modelType);
    NameUsage usage = counts == null ? null : counts.get(name);
    if (usage != null) {
      usage.value = value;
    }
  }

  private static void mergeUsage(TreeMap<String, NameUsage> counts, String name, String value) {
    NameUsage usage = counts.get(name);
    if (usage == null) {
      counts.put(name, new NameUsage(value));
    } else {
      usage.count++;
      usage.value = value;
    }
  }

  private static void collectDocumentItems(@NonNull ProjectItem item, @NonNull List<ProjectItem> result) {
    if (item.isFolder()) {
      for (ProjectItem child : item.getChildren()) {
        collectDocumentItems(child, result);
      }
    } else if (item.getModel() instanceof DocumentModel) {
      result.add(item);
    }
  }

  /**
   * How many header annotations currently use a given name for a model type, and the value most recently seen
   * alongside it (used to prefill the value field when the name is picked from suggestions).
   */
  private static final class NameUsage {
    private int count = 1;
    private String value;

    private NameUsage(String value) {
      this.value = value;
    }
  }
}
