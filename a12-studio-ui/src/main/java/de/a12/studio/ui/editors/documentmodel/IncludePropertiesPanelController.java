package de.a12.studio.ui.editors.documentmodel;

import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.documentmodel.Element;
import de.a12.studio.models.documentmodel.GroupConfig;
import de.a12.studio.models.documentmodel.GroupElement;
import de.a12.studio.models.documentmodel.IncludeConfig;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.models.typedefinitionmodel.TypeDefinitionModel;
import de.a12.studio.modelsvalidation.ElementProperty;
import de.a12.studio.modelsvalidation.validators.TransitiveTypeDefinitions;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.editors.AbstractPropertyEditor;
import de.a12.studio.ui.util.ProjectDocumentModels;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import org.jspecify.annotations.NonNull;

import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class IncludePropertiesPanelController extends AbstractPropertyEditor implements Initializable {

  @FXML
  private ComboBox<String> referenceComboBox;

  @FXML
  private Button editReferenceButton;

  @FXML
  private CheckBox excludeValidationRulesCheckBox;

  @FXML
  private CheckBox excludeComputationRulesCheckBox;

  @Override
  public void initialize(URL url, ResourceBundle resources) {
    super.initialize(url, resources);

    bindComboBox(referenceComboBox, (element, value) ->
        getGroupConfig(element).ifPresent(config -> includeConfig(config).setReference(value)));
    bindCheckBox(excludeValidationRulesCheckBox, (element, value) ->
        getGroupConfig(element).ifPresent(config -> includeConfig(config).setExcludeRules(value ? true : null)));
    bindCheckBox(excludeComputationRulesCheckBox, (element, value) ->
        getGroupConfig(element).ifPresent(config -> includeConfig(config).setExcludeComputations(value ? true : null)));

    editReferenceButton.disableProperty().bind(referenceComboBox.valueProperty().isNull());
  }

  /**
   * Opens the Document Model referenced by the combo box in an editor tab, selecting its tab instead if it's
   * already open (see {@code TabPaneController#modelOpened}).
   */
  @FXML
  private void onEditReference(ActionEvent event) {
    String reference = referenceComboBox.getValue();
    if (reference == null) {
      return;
    }

    ProjectDocumentModels.openModelInEditor(reference);
  }

  @Override
  public void setElement(@NonNull Element element) {
    super.setElement(element);

    GroupConfig config = getGroupConfig(element).orElse(null);
    IncludeConfig includeConfig = config != null ? config.getIncludeConfig() : null;

    setComboBoxItems(referenceComboBox, includableModelIds());
    setFieldValue(referenceComboBox, includeConfig != null ? includeConfig.getReference() : null);
    setFieldValue(excludeValidationRulesCheckBox, includeConfig != null && Boolean.TRUE.equals(includeConfig.getExcludeRules()));
    setFieldValue(excludeComputationRulesCheckBox, includeConfig != null && Boolean.TRUE.equals(includeConfig.getExcludeComputations()));
  }

  @Override
  protected String validationProperty() {
    return ElementProperty.INCLUDE_REFERENCE;
  }

  private static IncludeConfig includeConfig(@NonNull GroupConfig config) {
    if (config.getIncludeConfig() == null) {
      config.setIncludeConfig(new IncludeConfig());
    }
    return config.getIncludeConfig();
  }

  private static Optional<GroupConfig> getGroupConfig(Element element) {
    if (element instanceof GroupElement groupElement && groupElement.getGroup() != null) {
      return Optional.of(groupElement.getGroup());
    }
    return Optional.empty();
  }

  /**
   * Every other Document Model in the project this Include could reference, mirroring {@link
   * de.a12.studio.ui.editors.documentmodel.dialogs.IncludeDialogController}'s selection: Type Definition
   * models aren't includable business objects, a model can't include itself, and - mirroring SME's {@code
   * createsIncludeLoop} check - a candidate that already includes (directly or transitively) this model is
   * excluded too, since picking it would close an Include cycle.
   */
  private static List<String> includableModelIds() {
    ProjectItem projectItem = Studio.getSelectedProjectItem();
    if (projectItem == null || !(projectItem.getModel() instanceof DocumentModel currentModel)) {
      return List.of();
    }
    List<DocumentModel> otherModels = ProjectDocumentModels.getOtherDocumentModels(projectItem);
    List<DocumentModel> allModels = new ArrayList<>(otherModels);
    allModels.add(currentModel);
    return otherModels.stream()
        .filter(model -> !(model instanceof TypeDefinitionModel))
        .filter(candidate -> !TransitiveTypeDefinitions.includedModelIds(candidate, allModels).contains(currentModel.getId()))
        .map(DocumentModel::getId)
        .sorted(Comparator.naturalOrder())
        .toList();
  }
}
