package de.a12.studio.ui.editors.propertyeditors;

import de.a12.studio.models.documentmodel.Element;
import de.a12.studio.models.documentmodel.FieldElement;
import de.a12.studio.models.documentmodel.GroupConfig;
import de.a12.studio.models.documentmodel.GroupElement;
import de.a12.studio.ui.editors.AbstractPropertyEditor;
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

    // Reflects an index field that was deleted elsewhere in the tree (so this panel never ran its own
    // commit/validate cycle for that change) as soon as this group is selected.
    refreshValidationState();
  }

  private static List<String> fieldNamesInGroup(GroupConfig config) {
    List<String> names = new ArrayList<>();
    names.add("");
    if (config != null && config.getElements() != null) {
      config.getElements().stream()
          .filter(FieldElement.class::isInstance)
          .map(Element::getName)
          .forEach(names::add);
    }
    return names;
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
