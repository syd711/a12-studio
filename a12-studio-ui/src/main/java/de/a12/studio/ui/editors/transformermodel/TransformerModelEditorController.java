package de.a12.studio.ui.editors.transformermodel;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.ModelType;
import de.a12.studio.models.projects.Project;
import de.a12.studio.models.projects.ProjectResources;
import de.a12.studio.models.transformermodel.GeneratedDocumentModels;
import de.a12.studio.models.transformermodel.TransformerModel;
import de.a12.studio.models.transformermodel.TransformerModelContent;
import de.a12.studio.models.util.JsonSettings;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.editors.AbstractEditorController;
import de.a12.studio.ui.editors.combineddocumentmodel.CombinationPreviewPanelController;
import de.a12.studio.ui.editors.transformermodel.TransformationOutcome.Discovery;
import de.a12.studio.ui.editors.transformermodel.TransformationOutcome.SimpleType;
import de.a12.studio.ui.events.ModelClosedEvent;
import de.a12.studio.ui.events.ModelSaveEvent;
import de.a12.studio.ui.util.Debouncer;
import de.a12.studio.ui.util.StudioBundle;
import javafx.application.Platform;
import javafx.fxml.FXML;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;

import java.io.File;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;

/**
 * Edits a {@link TransformerModel}, SME's "Transformed Document Model": the configuration that turns an XSD into a
 * Document Model. Like SME's editor it has four tabs - <b>Transformation</b> (the main XSD and root element, the type
 * mappings, and what the last transformation reported), <b>Element Selection</b> (rename and delete paths),
 * <b>Custom Texts</b> (pattern error messages and enumeration display texts) and <b>Preview</b> (the Document Model
 * that comes out) - and the Settings (name, version, description, locales, roles, annotations) are the generic Model
 * Settings dialog behind the toolbar button.
 *
 * <p>What SME does when it applies a tab - run the transformer and refresh everything from its answer - happens here
 * after every change, in the background: the model, with all XSD files of the project, goes to {@link TransformerRun}
 * (the installed Simple Model Editor's backend), a run started while another is still going supersedes it, and
 * only the answer to the latest request is shown. The XSD discovery of that answer feeds the suggestions of the
 * panels; the editor works without it (any text is accepted), e.g. when no A12 installation is configured.
 * The Document Model is never saved: only the configuration is.
 */
@Slf4j
public class TransformerModelEditorController extends AbstractEditorController {

  /** How long after the last change the transformation is started again. */
  private static final int RUN_DEBOUNCE_MS = 700;

  private static final String RUN_KEY = "transformer-run";

  @FXML
  private TransformerSourcePanelController sourcePanelController;

  @FXML
  private TypeMappingsPanelController typeMappingsPanelController;

  @FXML
  private TransformationIssuesPanelController issuesPanelController;

  @FXML
  private RenamePathsPanelController renamePathsPanelController;

  @FXML
  private DeletePathsPanelController deletePathsPanelController;

  @FXML
  private PatternErrorsPanelController patternErrorsPanelController;

  @FXML
  private EnumLabelsPanelController enumLabelsPanelController;

  @FXML
  private CombinationPreviewPanelController previewPanelController;

  private final Debouncer debouncer = new Debouncer();

  private final ExecutorService executor = Executors.newSingleThreadExecutor(runnable -> {
    Thread thread = new Thread(runnable, "transformer-model-run");
    thread.setDaemon(true);
    return thread;
  });

  // The id of the latest run request; an answer for an older one is dropped.
  private final AtomicInteger requests = new AtomicInteger();

  private TransformerModel model;

  private Discovery discovery = Discovery.EMPTY;

  private TransformationOutcome lastOutcome;

  // Replaceable so a test can run without the Simple Model Editor backend.
  private BiFunction<TransformerModel, List<File>, TransformationOutcome> runner = TransformerRun::run;

  private boolean closed;

  @Override
  public void loadModel(@NonNull A12Model<?> model) {
    this.model = (TransformerModel) model;
    if (this.model.getContent() == null) {
      this.model.setContent(new TransformerModelContent());
    }

    sourcePanelController.setProjectFolder(projectFolder());
    sourcePanelController.setXsdFileNames(xsdFileNames());
    sourcePanelController.setOnXsdFilesChanged(() -> {
      sourcePanelController.setXsdFileNames(xsdFileNames());
      runNow();
    });
    issuesPanelController.setOnRunRequested(this::runNow);

    previewPanelController.showElementDetails();
    for (TransformerModelPanelController panel : panels()) {
      panel.setModel(this.model);
      panel.setOnChange(this::scheduleRun);
    }
    applyDiscovery();
    updateSettingsErrorBadge();
    runNow();
  }

  private List<TransformerModelPanelController> panels() {
    return List.of(sourcePanelController, typeMappingsPanelController, renamePathsPanelController, deletePathsPanelController,
        patternErrorsPanelController, enumLabelsPanelController);
  }

  @NonNull
  @Override
  public ModelType getModelType() {
    return ModelType.TRANSFORMER;
  }

  // ---- running the transformation ------------------------------------------------------------------------------

  private void scheduleRun() {
    if (!closed) {
      debouncer.debounce(RUN_KEY, this::runNow, RUN_DEBOUNCE_MS, true);
    }
  }

  /** Starts a transformation of the model as it is now; its answer replaces whatever the panels show of the last one. */
  void runNow() {
    if (closed || model == null) {
      return;
    }
    int request = requests.incrementAndGet();
    issuesPanelController.showRunning();

    // The run works on a copy made here, on the FX thread, so editing on goes on without disturbing it.
    TransformerModel snapshot = copyOf(model);
    List<File> xsdFiles = ProjectResources.findFiles(projectFolder(), "xsd");
    executor.submit(() -> {
      TransformationOutcome outcome;
      try {
        outcome = runner.apply(snapshot, xsdFiles);
      }
      catch (RuntimeException e) {
        log.warn("The transformation of '{}' failed: {}", snapshot.getId(), e.getMessage(), e);
        outcome = TransformationOutcome.unavailable(String.valueOf(e.getMessage()));
      }
      TransformationOutcome result = outcome;
      Platform.runLater(() -> {
        if (!closed && request == requests.get()) {
          apply(result);
        }
      });
    });
  }

  private void apply(TransformationOutcome outcome) {
    lastOutcome = outcome;
    // A run that could not discover anything (the file is being retyped, the backend is down) keeps the last suggestions.
    if (outcome.discovery() != Discovery.EMPTY) {
      discovery = outcome.discovery();
      applyDiscovery();
    }
    issuesPanelController.show(outcome);
    if (outcome.success() && outcome.documentModel() != null && model != null) {
      // Offered to the other editors in place of a Document Model; a failed run keeps the last good one.
      GeneratedDocumentModels.store(projectItem.getProjectFolder(), model.getId(), outcome.documentModel());
    }
    previewPanelController.showDocumentModel(outcome.documentModel(), projectItem, previewPlaceholder(outcome));
  }

  private static String previewPlaceholder(TransformationOutcome outcome) {
    return switch (outcome.state()) {
      case INCOMPLETE -> outcome.message();
      case UNAVAILABLE -> StudioBundle.get("transformer_model.preview.unavailable", outcome.message());
      case DONE -> StudioBundle.get("transformer_model.preview.none_generated");
    };
  }

  private void applyDiscovery() {
    sourcePanelController.setRootElements(discovery.rootElements());
    typeMappingsPanelController.setXsdTypes(discovery.simpleTypes().stream().map(SimpleType::name).toList());
    renamePathsPanelController.setElementPaths(discovery.elementPaths());
    deletePathsPanelController.setElementPaths(discovery.elementPaths());
    patternErrorsPanelController.setPatterns(discovery.patterns());
    enumLabelsPanelController.setEnumValues(discovery.enumValues());
  }

  private List<String> xsdFileNames() {
    return ProjectResources.findFiles(projectFolder(), "xsd").stream().map(File::getName).distinct().toList();
  }

  /**
   * The folder of the project the model belongs to - the one the validators look the XSD files up in as well - or, when
   * no project is open, the root of the item's own tree.
   */
  private File projectFolder() {
    Project project = Studio.getCurrentProject();
    return project != null && project.getFolder() != null ? project.getFolder() : projectItem.getProjectFolder();
  }

  private static TransformerModel copyOf(TransformerModel model) {
    return JsonSettings.objectMapper.readValue(JsonSettings.objectMapper.writeValueAsString(model), TransformerModel.class);
  }

  /** The model's settings (name, locales) are edited in the Model Settings dialog; whatever it saved is transformed again. */
  @Override
  public void modelSaved(@NonNull ModelSaveEvent event) {
    super.modelSaved(event);
    if (projectItem != null && event.getItem().equals(projectItem)) {
      scheduleRun();
    }
  }

  @Override
  public void modelClosed(@NonNull ModelClosedEvent event) {
    super.modelClosed(event);
    if (projectItem != null && event.getItem().equals(projectItem)) {
      closed = true;
      debouncer.shutdown();
      executor.shutdownNow();
      panels().forEach(TransformerModelPanelController::destroy);
    }
  }

  // ---- for tests -------------------------------------------------------------------------------------------------

  void setRunner(@NonNull BiFunction<TransformerModel, List<File>, TransformationOutcome> runner) {
    this.runner = runner;
  }

  TransformationOutcome lastOutcome() {
    return lastOutcome;
  }
}
