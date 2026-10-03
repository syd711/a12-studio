package de.a12.studio.models.transformermodel;

import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.models.util.JsonSettings;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.io.File;
import java.util.Map;
import java.util.function.Consumer;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The last Document Model a {@link TransformerModel} generated, so that other models can refer to it the way they refer
 * to a Document Model (SME offers a Transformer Model wherever a Document Model is chosen). The generated model is
 * volatile in SME and only exists after a transformation, which needs the installed SME backend; so the Transformer
 * Model editor {@link #store stores} every successful answer here, and everyone else only reads it - nothing that lists
 * fields has to run the transformer.
 *
 * <p>Kept in memory and mirrored to {@code <project>/.a12-studio/generated/<id>.json}, so the references still resolve
 * after a restart. The file is a cache, never a model: it is not part of the project tree. A Transformer Model that was
 * never transformed (or whose cache was deleted) simply has no Document Model yet.
 */
@Slf4j
public final class GeneratedDocumentModels {

  static final String CACHE_FOLDER = ".a12-studio/generated";

  private static final Map<String, DocumentModel> MEMORY = new ConcurrentHashMap<>();

  // Asked to generate the model of a Transformer Model that has none yet; set by whoever can run the transformer.
  private static volatile Consumer<ProjectItem> missHandler;

  private GeneratedDocumentModels() {
  }

  /** Remembers {@code generated} as the Document Model of the Transformer Model {@code transformerId}. */
  public static void store(@NonNull File projectFolder, @NonNull String transformerId, @NonNull DocumentModel result) {
    // A copy, named after the Transformer Model (2026.06), which is also what a reference to it says.
    DocumentModel generated;
    try {
      generated = JsonSettings.objectMapper.readValue(JsonSettings.objectMapper.writeValueAsString(result), DocumentModel.class);
    }
    catch (RuntimeException e) {
      log.warn("The generated Document Model of '{}' could not be copied: {}", transformerId, e.getMessage(), e);
      return;
    }
    generated.setId(transformerId);
    MEMORY.put(key(projectFolder, transformerId), generated);
    try {
      File file = cacheFile(projectFolder, transformerId);
      File folder = file.getParentFile();
      if (folder.isDirectory() || folder.mkdirs()) {
        JsonSettings.objectMapper.writeValue(file, generated);
      }
    }
    catch (RuntimeException e) {
      log.warn("The generated Document Model of '{}' could not be cached: {}", transformerId, e.getMessage(), e);
    }
  }

  /** The last Document Model generated for {@code transformerId}, or {@code null} if there is none. */
  @Nullable
  public static DocumentModel find(@NonNull File projectFolder, @NonNull String transformerId) {
    String key = key(projectFolder, transformerId);
    DocumentModel cached = MEMORY.get(key);
    if (cached != null) {
      return cached;
    }
    File file = cacheFile(projectFolder, transformerId);
    if (!file.isFile()) {
      return null;
    }
    try {
      DocumentModel loaded = JsonSettings.objectMapper.readValue(file, DocumentModel.class);
      MEMORY.put(key, loaded);
      return loaded;
    }
    catch (RuntimeException e) {
      log.warn("The cached Document Model of '{}' could not be read: {}", transformerId, e.getMessage(), e);
      return null;
    }
  }

  /**
   * The Document Model of the Transformer Model with the given id in {@code context}'s project, or {@code null} if
   * {@code modelId} names no Transformer Model or none was generated yet.
   */
  @Nullable
  public static DocumentModel resolve(@NonNull ProjectItem context, @Nullable String modelId) {
    if (modelId == null) {
      return null;
    }
    ProjectItem item = context.findByModelId(modelId);
    if (item == null || !(item.getModel() instanceof TransformerModel)) {
      return null;
    }
    DocumentModel found = find(context.getProjectFolder(), modelId);
    Consumer<ProjectItem> handler = missHandler;
    if (found == null && handler != null) {
      // Generating takes seconds and needs the transformer, so this answers "none yet" now and the handler fills
      // the cache in the background.
      handler.accept(item);
    }
    return found;
  }

  /**
   * Registers who generates the Document Model of a Transformer Model that is asked for before it was ever transformed
   * (called with that Transformer Model's item, possibly many times for the same one, from any thread). {@code null}
   * removes it.
   */
  public static void setMissHandler(@Nullable Consumer<ProjectItem> handler) {
    missHandler = handler;
  }

  /** Forgets everything in memory (the files stay); for tests. */
  public static void clearMemory() {
    MEMORY.clear();
  }

  private static File cacheFile(File projectFolder, String transformerId) {
    return new File(new File(projectFolder, CACHE_FOLDER), transformerId + ".json");
  }

  private static String key(File projectFolder, String transformerId) {
    return projectFolder.getAbsolutePath() + "|" + transformerId;
  }
}
