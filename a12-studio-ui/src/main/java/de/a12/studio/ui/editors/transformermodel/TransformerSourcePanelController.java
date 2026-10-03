package de.a12.studio.ui.editors.transformermodel;

import de.a12.studio.models.projects.ProjectResources;
import de.a12.studio.models.transformermodel.TransformerCmd;
import de.a12.studio.modelsvalidation.validators.transformer.TransformerElementIds;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.components.StudioFileChooser;
import de.a12.studio.ui.util.Icons;
import de.a12.studio.ui.util.StudioBundle;
import de.a12.studio.ui.util.WidgetFactory;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * The "General" section of the Transformation tab: the XML Structure Definition File ({@code Cmd.mainXsd}) and the Root
 * Element ({@code Cmd.rootElement}) of the XSD that become the generated Document Model's root group. The file is
 * chosen from the project's XSD files (SME's {@code availableXsdFiles}, any name accepted), the root element from the
 * ones the XSD declares (SME's {@code xsd:rootElements}, any text accepted). XSD files never show in the project
 * tree, so a button copies new ones into the project - SME has them as workspace resources.
 */
@Slf4j
public class TransformerSourcePanelController extends TransformerModelPanelController {

  private static final String XSD = "xsd";

  @FXML
  private VBox fields;

  private List<String> xsdFileNames = List.of();
  private List<String> rootElements = List.of();
  private File projectFolder;
  private Runnable onXsdFilesChanged = () -> {
  };

  /** The names of the XSD files of the project. */
  public void setXsdFileNames(@NonNull List<String> xsdFileNames) {
    this.xsdFileNames = xsdFileNames;
    refreshSuggestions();
  }

  /** The root elements the XSD declares. */
  public void setRootElements(@NonNull List<String> rootElements) {
    this.rootElements = rootElements;
    refreshSuggestions();
  }

  /** The project XSD files are added to; null disables adding. */
  public void setProjectFolder(@Nullable File projectFolder) {
    this.projectFolder = projectFolder;
  }

  /** Called after XSD files were added to the project, so the editor can list them and transform again. */
  public void setOnXsdFilesChanged(@NonNull Runnable onXsdFilesChanged) {
    this.onXsdFilesChanged = onXsdFilesChanged;
  }

  @Override
  protected String errorIdPrefix() {
    return "content/Cmd";
  }

  @Override
  protected void rebuild() {
    fields.getChildren().clear();
    TransformerCmd cmd = content().getCmd();

    var mainXsd = suggestionCombo(cmd == null ? null : cmd.getMainXsd(), () -> xsdFileNames, value -> content().getOrCreateCmd().setMainXsd(value),
        "main-xsd", StudioBundle.get("transformer_model.source.main_xsd_prompt"));
    registerErrorTarget(TransformerElementIds.MAIN_XSD, mainXsd);

    Button addXsd = actionButton(Icons.FILE_IMPORT, "transformer_model.source.add_xsd", this::onAddXsd);
    addXsd.setDisable(projectFolder == null);
    fields.getChildren().add(labelled("transformer_model.source.main_xsd", row(grow(mainXsd), addXsd)));

    var rootElement = suggestionCombo(cmd == null ? null : cmd.getRootElement(), () -> rootElements,
        value -> content().getOrCreateCmd().setRootElement(value), "root-element", StudioBundle.get("transformer_model.source.root_element_prompt"));
    registerErrorTarget(TransformerElementIds.ROOT_ELEMENT, rootElement);
    fields.getChildren().add(labelled("transformer_model.source.root_element", rootElement));
  }

  private void onAddXsd() {
    if (projectFolder == null) {
      return;
    }
    StudioFileChooser chooser = new StudioFileChooser();
    chooser.setTitle(StudioBundle.get("transformer_model.source.add_xsd_title"));
    chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(StudioBundle.get("transformer_model.source.xsd_filter"), "*.xsd"));
    List<File> chosen = chooser.showOpenMultipleDialog(Studio.stage);
    if (chosen == null || chosen.isEmpty()) {
      return;
    }
    String first = null;
    for (File file : chosen) {
      try {
        File resource = ProjectResources.addResource(projectFolder, file, XSD);
        first = first == null ? resource.getName() : first;
      }
      catch (IOException e) {
        log.warn("Failed to add '{}' to the project: {}", file, e.getMessage(), e);
        WidgetFactory.showAlert(Studio.stage, StudioBundle.get("transformer_model.source.add_xsd_failed", file.getName(), e.getMessage()));
      }
    }
    if (first == null) {
      return;
    }
    TransformerCmd cmd = content().getCmd();
    if (cmd == null || cmd.getMainXsd() == null || cmd.getMainXsd().isBlank()) {
      content().getOrCreateCmd().setMainXsd(first);
      structuralChange();
    }
    onXsdFilesChanged.run();
  }
}
