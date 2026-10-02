package de.a12.studio.modelsvalidation.kernel;

import java.io.FileNotFoundException;
import java.io.UncheckedIOException;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.a12.studio.kernel.KernelDocumentModelExpander;
import de.a12.studio.kernel.KernelException;
import de.a12.studio.kernel.KernelExpansion;
import de.a12.studio.kernel.KernelModelSource;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.models.util.JsonSettings;

/**
 * Connects the kernel facade to a project: the kernel gets every model from the in-memory {@link ProjectItem}
 * tree (so unsaved edits of open editor tabs count), and its flattened result comes back as a studio
 * {@link DocumentModel}. Callers keep their own fallback for {@link Optional#empty()}: the kernel refuses
 * models it cannot read, and that must never break an editor.
 */
public final class ProjectKernelModels {

  private static final Logger log = LoggerFactory.getLogger(ProjectKernelModels.class);

  private ProjectKernelModels() {
  }

  /** The kernel's view of the project: model json by id, taken from the loaded model objects. */
  public static KernelModelSource sourceOf(ProjectItem contextItem) {
    return id -> {
      ProjectItem item = contextItem.findByModelId(id);
      if (item == null || item.getModel() == null) {
        throw new UncheckedIOException(new FileNotFoundException("No model with id " + id + " in the project"));
      }
      try {
        return JsonSettings.objectMapper.writeValueAsString(item.getModel());
      }
      catch (Exception e) {
        throw new UncheckedIOException(new java.io.IOException(e));
      }
    };
  }

  /**
   * Expands a Document Model (includes, type definitions) or a Combination Model (base, additions, selections,
   * decorations) with the kernel and returns the resulting Document Model.
   *
   * @return the flattened model, or empty if the kernel could not expand it or reported errors
   */
  public static Optional<DocumentModel> expand(ProjectItem contextItem, String modelId) {
    if (modelId == null) {
      return Optional.empty();
    }
    try {
      KernelExpansion expansion = new KernelDocumentModelExpander().expand(modelId, sourceOf(contextItem));
      if (expansion.hasErrors()) {
        log.debug("Kernel expansion of {} reported {}", modelId, expansion.findings());
        return Optional.empty();
      }
      return Optional.of(JsonSettings.objectMapper.readValue(expansion.expandedJson(), DocumentModel.class));
    }
    catch (KernelException e) {
      log.debug("Kernel could not expand {}: {}", modelId, e.getMessage());
      return Optional.empty();
    }
    catch (Exception e) {
      log.warn("Kernel expansion of {} could not be read back: {}", modelId, e.getMessage());
      return Optional.empty();
    }
  }
}
