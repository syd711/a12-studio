package de.a12.studio.models.projects.settings.annotations;

import de.a12.studio.models.Annotation;
import de.a12.studio.models.ModelType;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.documentmodel.Element;
import de.a12.studio.models.documentmodel.GroupElement;
import de.a12.studio.models.projects.Project;
import de.a12.studio.models.projects.ProjectItem;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Collects the annotation names (and their most recently used value) used on document content (as opposed to
 * document headers, see {@link AnnotationHeaderRegistry}) across the whole project, grouped by model type, so
 * property editors can suggest previously used annotation names instead of requiring users to retype them, and
 * prefill the value that went with a suggested name.
 * Names are reference-counted per model type so callers can incrementally {@link #addName} / {@link #removeName}
 * as annotations are edited, without requiring a full {@link #rebuild} of the project.
 */
public final class AnnotationFieldRegistry {

  private AnnotationFieldSet fieldSet = new AnnotationFieldSet();

  public AnnotationFieldSet getFieldSet() {
    return fieldSet;
  }

  public void rebuild(@NonNull Project project) {
    List<ProjectItem> documentItems = new ArrayList<>();
    collectDocumentItems(project.getRoot(), documentItems);

    AnnotationFieldSet collected = new AnnotationFieldSet();
    for (ProjectItem item : documentItems) {
      if (item.getModel() instanceof DocumentModel documentModel
          && documentModel.getContent() != null && documentModel.getContent().getModelRoot() != null) {
        for (GroupElement rootGroup : documentModel.getContent().getModelRoot().getRootGroups()) {
          collectFromElement(rootGroup, documentModel.getModelType(), collected.getModelTypes());
        }
      }
    }
    this.fieldSet = collected;
  }

  /**
   * Returns the annotation names previously used for the given model type, sorted alphabetically.
   * Never {@code null}.
   */
  public @NonNull List<String> getNames(@Nullable ModelType modelType) {
    Map<String, NameUsage> values = valuesFor(modelType);
    return values == null ? List.of() : new ArrayList<>(values.keySet());
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
    Map<String, NameUsage> values = valuesFor(modelType);
    NameUsage usage = values == null ? null : values.get(name);
    return usage == null ? null : usage.getValue();
  }

  /**
   * Registers a new use of {@code name} (with {@code value}) for the given model type, so it's offered as a
   * suggestion even before the next full {@link #rebuild}. Blank names are ignored. Callers should pair this
   * with {@link #removeName} when an annotation's name changes or the annotation is deleted, so the registry
   * stays in sync with what's actually used in the project.
   */
  public void addName(@Nullable ModelType modelType, @Nullable String name, @Nullable String value) {
    if (name == null || name.isBlank()) {
      return;
    }
    AnnotationModelSet modelSet = getOrCreateModelSet(fieldSet.getModelTypes(), modelType);
    mergeUsage(modelSet.getValues(), name, value);
  }

  /**
   * Reverses a previous {@link #addName} call, dropping {@code name} from the suggestions for this model type
   * once its last use is gone. Blank names are ignored.
   */
  public void removeName(@Nullable ModelType modelType, @Nullable String name) {
    if (name == null || name.isBlank()) {
      return;
    }
    String modelTypeKey = modelTypeKey(modelType);
    AnnotationModelSet modelSet = fieldSet.getModelTypes().get(modelTypeKey);
    if (modelSet == null) {
      return;
    }
    Map<String, NameUsage> values = modelSet.getValues();
    NameUsage usage = values.get(name);
    if (usage != null && usage.decrementCount() <= 0) {
      values.remove(name);
    }
    if (values.isEmpty()) {
      fieldSet.getModelTypes().remove(modelTypeKey);
    }
  }

  /**
   * Updates the value on record for an already-registered {@code name}, without affecting its reference
   * count. Used when an annotation's value is edited without its name changing. A no-op if the name isn't
   * registered for this model type.
   */
  public void setValue(@Nullable ModelType modelType, @Nullable String name, @Nullable String value) {
    if (name == null || name.isBlank()) {
      return;
    }
    Map<String, NameUsage> values = valuesFor(modelType);
    NameUsage usage = values == null ? null : values.get(name);
    if (usage != null) {
      usage.setValue(value);
    }
  }

  private @Nullable Map<String, NameUsage> valuesFor(@Nullable ModelType modelType) {
    AnnotationModelSet modelSet = fieldSet.getModelTypes().get(modelTypeKey(modelType));
    return modelSet == null ? null : modelSet.getValues();
  }

  private static AnnotationModelSet getOrCreateModelSet(Map<String, AnnotationModelSet> setsByModelType,
      ModelType modelType) {
    return setsByModelType.computeIfAbsent(modelTypeKey(modelType), AnnotationFieldRegistry::newAnnotationModelSet);
  }

  private static AnnotationModelSet newAnnotationModelSet(String modelTypeKey) {
    AnnotationModelSet modelSet = new AnnotationModelSet();
    modelSet.setModelType(modelTypeKey);
    return modelSet;
  }

  private static @Nullable String modelTypeKey(@Nullable ModelType modelType) {
    return modelType == null ? null : modelType.name();
  }

  private static void mergeUsage(Map<String, NameUsage> values, String name, String value) {
    NameUsage usage = values.get(name);
    if (usage == null) {
      values.put(name, new NameUsage(value));
    } else {
      usage.incrementCount();
      usage.setValue(value);
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

  private static void collectFromElement(@NonNull Element element, ModelType modelType,
      @NonNull Map<String, AnnotationModelSet> collected) {
    for (Annotation annotation : element.getAnnotations()) {
      if (annotation.getName() != null && !annotation.getName().isBlank()) {
        AnnotationModelSet modelSet = getOrCreateModelSet(collected, modelType);
        mergeUsage(modelSet.getValues(), annotation.getName(), annotation.getValue());
      }
    }

    if (element instanceof GroupElement groupElement && groupElement.getGroup() != null) {
      for (Element child : groupElement.getGroup().getElements()) {
        collectFromElement(child, modelType, collected);
      }
    }
  }
}
