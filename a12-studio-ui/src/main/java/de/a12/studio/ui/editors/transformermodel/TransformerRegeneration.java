package de.a12.studio.ui.editors.transformermodel;

import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.models.projects.ProjectResources;
import de.a12.studio.models.transformermodel.GeneratedDocumentModels;
import de.a12.studio.models.transformermodel.TransformerCmd;
import de.a12.studio.models.transformermodel.TransformerModel;
import de.a12.studio.ui.events.StudioEventManager;
import javafx.application.Platform;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;

import java.io.File;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * Generates the Document Model of a Transformer Model that another editor asks for before it was ever transformed
 * (see {@link GeneratedDocumentModels#setMissHandler}): in the background, with the installed SME like the editor
 * itself, once per Transformer Model and session, and only for a configuration that names its main XSD and root element.
 * Whoever asked got "none yet"; when the model is there the Transformer Model's saved event is fired so validation and
 * the pickers look again.
 */
@Slf4j
public final class TransformerRegeneration {

  private static final Set<String> REQUESTED = ConcurrentHashMap.newKeySet();

  private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor(runnable -> {
    Thread thread = new Thread(runnable, "transformer-regeneration");
    thread.setDaemon(true);
    return thread;
  });

  private TransformerRegeneration() {
  }

  /** Makes cache misses of {@link GeneratedDocumentModels} regenerate. Call once at startup. */
  public static void install() {
    GeneratedDocumentModels.setMissHandler(TransformerRegeneration::request);
  }

  static void request(@NonNull ProjectItem item) {
    if (!(item.getModel() instanceof TransformerModel model) || !isComplete(model)
        || !REQUESTED.add(item.getProjectFolder().getAbsolutePath() + "|" + model.getId())) {
      return;
    }
    EXECUTOR.submit(() -> regenerate(item, model));
  }

  private static boolean isComplete(TransformerModel model) {
    TransformerCmd cmd = model.getContent() == null ? null : model.getContent().getCmd();
    return cmd != null && cmd.getMainXsd() != null && !cmd.getMainXsd().isBlank()
        && cmd.getRootElement() != null && !cmd.getRootElement().isBlank();
  }

  private static void regenerate(ProjectItem item, TransformerModel model) {
    try {
      List<File> xsdFiles = ProjectResources.findFiles(item.getProjectFolder(), "xsd");
      TransformationOutcome outcome = TransformerRun.withKernelFindings(TransformerRun.run(model, xsdFiles));
      if (outcome.success() && outcome.documentModel() != null) {
        GeneratedDocumentModels.store(item.getProjectFolder(), model.getId(), outcome.documentModel());
        Consumer<ProjectItem> refresh = changed -> StudioEventManager.getInstance().fireModelSavedEvent(changed);
        Platform.runLater(() -> refresh.accept(item));
      }
    }
    catch (RuntimeException e) {
      log.warn("The Document Model of '{}' could not be regenerated: {}", model.getId(), e.getMessage(), e);
    }
  }
}
