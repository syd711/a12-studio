package de.a12.studio.ui.editors;

import de.a12.studio.models.Annotation;
import de.a12.studio.models.ModelType;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.documentmodel.Element;
import de.a12.studio.models.documentmodel.FieldElement;
import de.a12.studio.models.documentmodel.GroupElement;
import de.a12.studio.models.projects.Project;
import de.a12.studio.models.projects.ProjectItem;
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
 * Collects the annotation names (and their most recently used value) used across the whole project, grouped
 * by model type and field type, so property editors can suggest previously used annotation names instead of
 * requiring users to retype them, and prefill the value that went with a suggested name.
 * "Field type" is the field's data type (e.g. "StringType") for {@link FieldElement}s, or the element's own
 * type (e.g. "Group", "Rule") for every other element, since annotations can be attached to any element.
 * Names are reference-counted per key so callers can incrementally {@link #addName} / {@link #removeName} as
 * annotations are edited, without requiring a full {@link #rebuild} of the project.
 */
public final class AnnotationFieldRegistry implements StudioEventListener {

  private static final AnnotationFieldRegistry INSTANCE = new AnnotationFieldRegistry();

  private Map<Key, TreeMap<String, NameUsage>> countsByKey = new HashMap<>();

  private AnnotationFieldRegistry() {
    StudioEventManager.getInstance().addListener(this);
  }

  public static AnnotationFieldRegistry getInstance() {
    return INSTANCE;
  }

  @Override
  public void projectOpened(@NonNull ProjectOpenedEvent event) {
    rebuild(event.getProject());
  }

  public void rebuild(@NonNull Project project) {
    List<ProjectItem> documentItems = new ArrayList<>();
    collectDocumentItems(project.getRoot(), documentItems);

    Map<Key, TreeMap<String, NameUsage>> collected = new HashMap<>();
    for (ProjectItem item : documentItems) {
      if (item.getModel() instanceof DocumentModel documentModel
          && documentModel.getContent() != null && documentModel.getContent().getModelRoot() != null) {
        for (GroupElement rootGroup : documentModel.getContent().getModelRoot().getRootGroups()) {
          collectFromElement(rootGroup, documentModel.getModelType(), collected);
        }
      }
    }
    this.countsByKey = collected;
  }

  /**
   * Returns the annotation names previously used for the given model type / field type combination, sorted
   * alphabetically. Never {@code null}.
   */
  public @NonNull List<String> getNames(@Nullable ModelType modelType, @Nullable String fieldType) {
    Map<String, NameUsage> counts = countsByKey.get(new Key(modelType, fieldType));
    return counts == null ? List.of() : new ArrayList<>(counts.keySet());
  }

  /**
   * Returns the value most recently used alongside {@code name} for the given key, so a property editor can
   * prefill the value field when the user picks {@code name} from the suggestions. {@code null} if the name
   * isn't known for this key.
   */
  public @Nullable String getValue(@Nullable ModelType modelType, @Nullable String fieldType, @Nullable String name) {
    if (name == null || name.isBlank()) {
      return null;
    }
    Map<String, NameUsage> counts = countsByKey.get(new Key(modelType, fieldType));
    NameUsage usage = counts == null ? null : counts.get(name);
    return usage == null ? null : usage.value;
  }

  /**
   * Registers a new use of {@code name} (with {@code value}) for the given key, so it's offered as a
   * suggestion even before the next full {@link #rebuild}. Blank names are ignored. Callers should pair this
   * with {@link #removeName} when an annotation's name changes or the annotation is deleted, so the registry
   * stays in sync with what's actually used in the project.
   */
  public void addName(@Nullable ModelType modelType, @Nullable String fieldType, @Nullable String name, @Nullable String value) {
    if (name == null || name.isBlank()) {
      return;
    }
    mergeUsage(countsByKey.computeIfAbsent(new Key(modelType, fieldType), k -> new TreeMap<>()), name, value);
  }

  /**
   * Reverses a previous {@link #addName} call, dropping {@code name} from the suggestions for this key once
   * its last use is gone. Blank names are ignored.
   */
  public void removeName(@Nullable ModelType modelType, @Nullable String fieldType, @Nullable String name) {
    if (name == null || name.isBlank()) {
      return;
    }
    Key key = new Key(modelType, fieldType);
    TreeMap<String, NameUsage> counts = countsByKey.get(key);
    if (counts == null) {
      return;
    }
    NameUsage usage = counts.get(name);
    if (usage != null && --usage.count <= 0) {
      counts.remove(name);
    }
    if (counts.isEmpty()) {
      countsByKey.remove(key);
    }
  }

  /**
   * Updates the value on record for an already-registered {@code name}, without affecting its reference
   * count. Used when an annotation's value is edited without its name changing. A no-op if the name isn't
   * registered for this key.
   */
  public void setValue(@Nullable ModelType modelType, @Nullable String fieldType, @Nullable String name, @Nullable String value) {
    if (name == null || name.isBlank()) {
      return;
    }
    TreeMap<String, NameUsage> counts = countsByKey.get(new Key(modelType, fieldType));
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

  /**
   * The "field type" grouping key for an element: the field's data type for a {@link FieldElement}, or the
   * element's own type (e.g. "Group", "Rule", "Computation") otherwise.
   */
  public static String resolveFieldType(@NonNull Element element) {
    if (element instanceof FieldElement fieldElement && fieldElement.getField() != null
        && fieldElement.getField().getFieldType() != null) {
      return fieldElement.getField().getFieldType().getType();
    }
    return element.getType() == null ? null : element.getType().getValue();
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

  private static void collectFromElement(@NonNull Element element, ModelType modelType,
      @NonNull Map<Key, TreeMap<String, NameUsage>> collected) {
    Key key = new Key(modelType, resolveFieldType(element));
    for (Annotation annotation : element.getAnnotations()) {
      if (annotation.getName() != null && !annotation.getName().isBlank()) {
        mergeUsage(collected.computeIfAbsent(key, k -> new TreeMap<>()), annotation.getName(), annotation.getValue());
      }
    }

    if (element instanceof GroupElement groupElement && groupElement.getGroup() != null) {
      for (Element child : groupElement.getGroup().getElements()) {
        collectFromElement(child, modelType, collected);
      }
    }
  }

  private record Key(ModelType modelType, String fieldType) {
  }

  /**
   * How many elements currently use a given annotation name for a key, and the value most recently seen
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
