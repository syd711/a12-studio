package de.a12.studio.ui.editors.propertyeditors;

import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.documentmodel.Element;
import de.a12.studio.models.documentmodel.GroupConfig;
import de.a12.studio.models.documentmodel.GroupElement;
import de.a12.studio.models.documentmodel.IncludeConfig;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.models.typedefinitionmodel.TypeDefinitionModel;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.editors.AbstractPropertyEditor;
import de.a12.studio.ui.util.ProjectDocumentModels;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import org.jspecify.annotations.NonNull;

import java.net.URL;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class IncludePropertiesPanelController extends AbstractPropertyEditor implements Initializable {

  @FXML
  private ComboBox<String> referenceComboBox;

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
        getGroupConfig(element).ifPresent(config -> config.setExcludeRules(value ? true : null)));
    bindCheckBox(excludeComputationRulesCheckBox, (element, value) ->
        getGroupConfig(element).ifPresent(config -> config.setExcludeComputations(value ? true : null)));
  }

  @Override
  public void setElement(@NonNull Element element) {
    super.setElement(element);

    GroupConfig config = getGroupConfig(element).orElse(null);
    IncludeConfig includeConfig = config != null ? config.getIncludeConfig() : null;

    setComboBoxItems(referenceComboBox, includableModelIds());
    setFieldValue(referenceComboBox, includeConfig != null ? includeConfig.getReference() : null);
    setFieldValue(excludeValidationRulesCheckBox, config != null && Boolean.TRUE.equals(config.getExcludeRules()));
    setFieldValue(excludeComputationRulesCheckBox, config != null && Boolean.TRUE.equals(config.getExcludeComputations()));

    // Reflects a reference that became invalid elsewhere (e.g. the referenced model was deleted) as soon as
    // this Include is selected.
    refreshValidationState();
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
   * models aren't includable business objects, and a model can't include itself.
   */
  private static List<String> includableModelIds() {
    ProjectItem projectItem = Studio.getSelectedProjectItem();
    if (projectItem == null) {
      return List.of();
    }
    return ProjectDocumentModels.getOtherDocumentModels(projectItem).stream()
        .filter(model -> !(model instanceof TypeDefinitionModel))
        .map(DocumentModel::getId)
        .sorted(Comparator.naturalOrder())
        .toList();
  }
}
