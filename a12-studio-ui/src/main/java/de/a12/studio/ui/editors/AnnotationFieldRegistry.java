package de.a12.studio.ui.editors;

import de.a12.studio.dataservices.models.Annotation;
import de.a12.studio.dataservices.models.ModelType;
import de.a12.studio.dataservices.models.documentmodel.DocumentModel;
import de.a12.studio.dataservices.models.documentmodel.Element;
import de.a12.studio.dataservices.models.documentmodel.FieldElement;
import de.a12.studio.dataservices.models.documentmodel.GroupElement;
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
 * Collects the annotation names used across the whole project, grouped by model type and field type, so
 * property editors can suggest previously used annotation names instead of requiring users to retype them.
 * "Field type" is the field's data type (e.g. "StringType") for {@link FieldElement}s, or the element's own
 * type (e.g. "Group", "Rule") for every other element, since annotations can be attached to any element.
 * Names are reference-counted per key so callers can incrementally {@link #addName} / {@link #removeName} as
 * annotations are edited, without requiring a full {@link #rebuild} of the project.
 */
public final class AnnotationFieldRegistry implements StudioEventListener {

  private static final AnnotationFieldRegistry INSTANCE = new AnnotationFieldRegistry();

  private Map<Key, TreeMap<String, Integer>> countsByKey = new HashMap<>();

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

    Map<Key, TreeMap<String, Integer>> collected = new HashMap<>();
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
    Map<String, Integer> counts = countsByKey.get(new Key(modelType, fieldType));
    return counts == null ? List.of() : new ArrayList<>(counts.keySet());
  }

  /**
   * Registers a new use of {@code name} for the given key, so it's offered as a suggestion even before the
   * next full {@link #rebuild}. Blank names are ignored. Callers should pair this with {@link #removeName}
   * when an annotation's name changes or the annotation is deleted, so the registry stays in sync with what's
   * actually used in the project.
   */
  public void addName(@Nullable ModelType modelType, @Nullable String fieldType, @Nullable String name) {
    if (name == null || name.isBlank()) {
      return;
    }
    countsByKey.computeIfAbsent(new Key(modelType, fieldType), k -> new TreeMap<>()).merge(name, 1, Integer::sum);
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
    TreeMap<String, Integer> counts = countsByKey.get(key);
    if (counts == null) {
      return;
    }
    if (counts.merge(name, -1, Integer::sum) <= 0) {
      counts.remove(name);
    }
    if (counts.isEmpty()) {
      countsByKey.remove(key);
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
    return element.getType();
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
      @NonNull Map<Key, TreeMap<String, Integer>> collected) {
    Key key = new Key(modelType, resolveFieldType(element));
    for (Annotation annotation : element.getAnnotations()) {
      if (annotation.getName() != null && !annotation.getName().isBlank()) {
        collected.computeIfAbsent(key, k -> new TreeMap<>()).merge(annotation.getName(), 1, Integer::sum);
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
}
