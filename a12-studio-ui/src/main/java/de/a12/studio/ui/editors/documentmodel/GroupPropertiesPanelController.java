package de.a12.studio.ui.editors.documentmodel;

import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.documentmodel.Element;
import de.a12.studio.models.documentmodel.FieldElement;
import de.a12.studio.models.documentmodel.GroupConfig;
import de.a12.studio.models.documentmodel.GroupElement;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.modelsvalidation.ElementProperty;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.editors.AbstractPropertyEditor;
import de.a12.studio.ui.util.ProjectDocumentModels;
import de.a12.studio.ui.util.WidgetFactory;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import org.jspecify.annotations.NonNull;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class GroupPropertiesPanelController extends AbstractPropertyEditor implements Initializable {

  // Matches the "unbounded" repeatability used for multi-select groups elsewhere (see
  // DocumentModelElementsTreeController.newMultiSelectElement).
  private static final int MAX_REPEATABILITY = 999_999;

  @FXML
  private Spinner<Integer> repetitionsField;

  @FXML
  private ComboBox<String> indexFieldComboBox;

  @Override
  public void initialize(URL url, ResourceBundle resources) {
    super.initialize(url, resources);

    repetitionsField.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, MAX_REPEATABILITY, 1));
    WidgetFactory.restrictToNumericInput(repetitionsField.getEditor());

    bindSpinner(repetitionsField, (element, value) -> getGroupConfig(element).ifPresent(config -> config.setRepeatability(parseInteger(value))));
    bindComboBox(indexFieldComboBox, (element, value) -> getGroupConfig(element).ifPresent(config -> config.setIndexFieldName(value == null || value.isEmpty() ? null : value)));
  }

  @Override
  public void setElement(@NonNull Element element) {
    super.setElement(element);

    GroupConfig config = getGroupConfig(element).orElse(null);
    setComboBoxItems(indexFieldComboBox, fieldNamesInGroup(config));
    setFieldValue(repetitionsField, config != null && config.getRepeatability() != null ? String.valueOf(config.getRepeatability()) : "");
    setFieldValue(indexFieldComboBox, config != null ? config.getIndexFieldName() : null);
  }

  @Override
  protected String validationProperty() {
    return ElementProperty.GROUP_PROPERTIES;
  }

  private static List<String> fieldNamesInGroup(GroupConfig config) {
    List<String> names = new ArrayList<>();
    names.add("");
    if (config != null) {
      elementsInGroup(config).stream()
          .filter(FieldElement.class::isInstance)
          .map(Element::getName)
          .forEach(names::add);
    }
    return names;
  }

  /**
   * The elements directly under this group: its own {@code elements} for a plain group, or the referenced
   * Document Model's root group(s)' elements for an Include, whose own {@code elements} is empty (mirrors
   * {@link ElementViewModel#getChildren()}).
   */
  private static List<Element> elementsInGroup(@NonNull GroupConfig config) {
    if (config.getIncludeConfig() != null) {
      return includedElements(config.getIncludeConfig().getReference());
    }
    return config.getElements() != null ? config.getElements() : List.of();
  }

  private static List<Element> includedElements(String reference) {
    ProjectItem projectItem = Studio.getSelectedProjectItem();
    if (reference == null || projectItem == null) {
      return List.of();
    }
    return ProjectDocumentModels.getOtherDocumentModels(projectItem).stream()
        .filter(model -> reference.equals(model.getId()))
        .findFirst()
        .map(GroupPropertiesPanelController::rootGroupElements)
        .orElse(List.of());
  }

  private static List<Element> rootGroupElements(@NonNull DocumentModel model) {
    if (model.getContent() == null || model.getContent().getModelRoot() == null
        || model.getContent().getModelRoot().getRootGroups() == null) {
      return List.of();
    }
    List<Element> elements = new ArrayList<>();
    for (GroupElement rootGroup : model.getContent().getModelRoot().getRootGroups()) {
      if (rootGroup.getGroup() != null && rootGroup.getGroup().getElements() != null) {
        elements.addAll(rootGroup.getGroup().getElements());
      }
    }
    return elements;
  }

  private static Optional<GroupConfig> getGroupConfig(Element element) {
    if (element instanceof GroupElement groupElement && groupElement.getGroup() != null) {
      return Optional.of(groupElement.getGroup());
    }
    return Optional.empty();
  }

  private static Integer parseInteger(String value) {
    return value.isEmpty() ? null : Integer.valueOf(value);
  }
}
