package de.a12.studio.ui.editors.propertyeditors;

import de.a12.studio.models.documentmodel.BooleanFieldType;
import de.a12.studio.models.documentmodel.ConfirmFieldType;
import de.a12.studio.models.documentmodel.CustomFieldFieldType;
import de.a12.studio.models.documentmodel.DateFieldType;
import de.a12.studio.models.documentmodel.DateFragmentFieldType;
import de.a12.studio.models.documentmodel.DateRangeFieldType;
import de.a12.studio.models.documentmodel.DateTimeFieldType;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.documentmodel.Element;
import de.a12.studio.models.documentmodel.EnumerationFieldType;
import de.a12.studio.models.documentmodel.FieldConfig;
import de.a12.studio.models.documentmodel.FieldElement;
import de.a12.studio.models.documentmodel.FieldType;
import de.a12.studio.models.documentmodel.GroupConfig;
import de.a12.studio.models.documentmodel.GroupElement;
import de.a12.studio.models.documentmodel.NumberFieldType;
import de.a12.studio.models.documentmodel.RequirednessConfig;
import de.a12.studio.models.documentmodel.StringFieldType;
import de.a12.studio.models.documentmodel.TimeFieldType;
import de.a12.studio.models.documentmodel.TypeDefFieldType;
import de.a12.studio.models.documentmodel.TypeDefinition;
import de.a12.studio.models.documentmodel.UnspecifiedFieldType;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.modelsvalidation.ElementProperty;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.editors.AbstractPropertyEditor;
import de.a12.studio.ui.util.ProjectDocumentModels;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.util.StringConverter;
import org.jspecify.annotations.NonNull;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.function.Consumer;

public class TypeDefinitionPanelController extends AbstractPropertyEditor implements Initializable {

  private static final String TYPE_STRING = "String";
  private static final String TYPE_NUMBER = "Number";
  private static final String TYPE_DATE = "Date";
  private static final String TYPE_DATE_TIME = "DateTime";
  private static final String TYPE_TIME = "Time";
  private static final String TYPE_DATE_FRAGMENT = "DateFragment";
  private static final String TYPE_DATE_RANGE = "DateRange";
  private static final String TYPE_CONFIRM = "Confirm";
  private static final String TYPE_BOOLEAN = "Boolean";
  private static final String TYPE_CUSTOM = "Custom";
  private static final String TYPE_ENUMERATION = "Enumeration";
  private static final String TYPE_UNSPECIFIED = "Unspecified";

  // Order mirrors the field-type dropdown in the SME reference implementation (DomainField.json).
  private static final List<String> DATA_TYPES = List.of(
      TYPE_STRING, TYPE_NUMBER, TYPE_DATE, TYPE_DATE_TIME, TYPE_TIME, TYPE_DATE_FRAGMENT, TYPE_DATE_RANGE,
      TYPE_CONFIRM, TYPE_BOOLEAN, TYPE_CUSTOM, TYPE_ENUMERATION);

  // A multi-select group's options are field-choices, so only String and Enumeration make sense as its
  // children's data type.
  private static final List<String> MULTI_SELECT_DATA_TYPES = List.of(TYPE_STRING, TYPE_ENUMERATION);

  @FXML
  private ComboBox<String> dataTypeCombo;

  @FXML
  private CheckBox typeDefinitionCheckBox;

  @FXML
  private CheckBox requiredParentCheckBox;

  @FXML
  private CheckBox defaultErrorMessagesCheckbox;

  @FXML
  private HBox defaultErrorMessagesBox;

  @FXML
  private HBox requiredParentBox;

  @FXML
  private ComboBox<String> dataTypeComboBox;

  @FXML
  private CheckBox globalCheckBox;

  @FXML
  private CheckBox transientCheckBox;

  @FXML
  private CheckBox requiredCheckBox;

  @FXML
  private GridPane customTypeGrid;

  @FXML
  private GridPane checkboxesGrid;

  private List<TypeDefinition> availableTypeDefinitions = List.of();

  private List<Element> ancestors = List.of();

  private boolean checkboxesGridDisabled = false;

  // Reflects the element's current FieldType, re-set whenever this panel changes it (i.e. via
  // typeDefinitionCheckBox or dataTypeComboBox) so sibling panels (e.g. DataTypeConfigurationPanelController)
  // can react and swap their own content without waiting for the next setElement() call.
  private final ObjectProperty<FieldType> currentFieldType = new SimpleObjectProperty<>();

  public ReadOnlyObjectProperty<FieldType> fieldTypeProperty() {
    return currentFieldType;
  }

  public ReadOnlyBooleanProperty defaultErrorMessagesProperty() {
    return defaultErrorMessagesCheckbox.selectedProperty();
  }

  public void setAncestors(@NonNull List<Element> ancestors) {
    this.ancestors = ancestors;
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    super.initialize(url, resourceBundle);
    customTypeGrid.managedProperty().bindBidirectional(customTypeGrid.visibleProperty());
    checkboxesGrid.managedProperty().bindBidirectional(checkboxesGrid.visibleProperty());

    dataTypeCombo.setConverter(new StringConverter<>() {
      @Override
      public String toString(String id) {
        return availableTypeDefinitions.stream()
            .filter(typeDefinition -> typeDefinition.getId().equals(id))
            .findFirst()
            .map(TypeDefinition::getName)
            .orElse(id);
      }

      @Override
      public String fromString(String name) {
        return name;
      }
    });

    dataTypeCombo.managedProperty().bind(dataTypeCombo.visibleProperty());
    dataTypeCombo.visibleProperty().bind(typeDefinitionCheckBox.selectedProperty());
    dataTypeComboBox.managedProperty().bind(dataTypeComboBox.visibleProperty());
    dataTypeComboBox.visibleProperty().bind(typeDefinitionCheckBox.selectedProperty().not());

    requiredParentBox.managedProperty().bind(requiredParentBox.visibleProperty());
    requiredParentBox.visibleProperty().bind(requiredCheckBox.selectedProperty());

    defaultErrorMessagesBox.managedProperty().bind(defaultErrorMessagesBox.visibleProperty());
    defaultErrorMessagesBox.visibleProperty().bind(requiredCheckBox.selectedProperty()
        .and(Bindings.createBooleanBinding(() -> !isMultiSelectStringDataType(), dataTypeComboBox.valueProperty())));

    bindCheckBox(globalCheckBox, (element, value) ->
        withFieldConfig(element, field -> field.setGlobal(value ? true : null)));
    bindCheckBox(transientCheckBox, (element, value) ->
        withFieldConfig(element, field -> field.setTransientField(value ? true : null)));

    bindCheckBox(requiredCheckBox, (element, value) -> withFieldConfig(element, field ->
        field.setRequirednessConfig(value ? newRequirednessConfig(requiredParentCheckBox.isSelected()) : null)));
    bindCheckBox(requiredParentCheckBox, (element, value) -> withFieldConfig(element, field -> {
      if (field.getRequirednessConfig() != null) {
        field.getRequirednessConfig().setMode(value
            ? RequirednessConfig.MODE_REQUIRED_IF_PARENT_FILLED
            : RequirednessConfig.MODE_REQUIRED);
      }
    }));
    bindCheckBox(defaultErrorMessagesCheckbox, (element, value) -> withFieldConfig(element, field -> {
      if (field.getRequirednessConfig() != null && value) {
        field.getRequirednessConfig().getErrorMessage().clear();
      }
    }));

    bindCheckBox(typeDefinitionCheckBox, (element, value) -> {
      if (value && dataTypeComboBox.getValue() == null) {
        setFieldValue(dataTypeComboBox, DATA_TYPES.get(0));
      }
      FieldType newFieldType = value ? new TypeDefFieldType() : createFieldType(dataTypeComboBox.getValue());
      withFieldConfig(element, field -> field.setFieldType(newFieldType));
      currentFieldType.set(newFieldType);
    });
    bindComboBox(dataTypeCombo, (element, value) -> withFieldConfig(element, field -> {
      if (field.getFieldType() instanceof TypeDefFieldType typeDefFieldType) {
        typeDefFieldType.getTypeDefType().setTypeDefinitionId(value);
      }
    }));
    bindComboBox(dataTypeComboBox, (element, value) -> {
      if (value == null) {
        return;
      }
      withFieldConfig(element, field -> {
        if (!(field.getFieldType() instanceof TypeDefFieldType)) {
          FieldType newFieldType = createFieldType(value);
          field.setFieldType(newFieldType);
          currentFieldType.set(newFieldType);
        }
      });
    });
  }

  public void setCustomTypeDisabled() {
    customTypeGrid.setVisible(false);
  }

  /**
   * Global/transient/required have no equivalent on a {@link TypeDefinition} (see {@code
   * TypeDefinitionFieldElement}), so the type definition editor hides this section entirely.
   */
  public void hideCheckboxesGrid() {
    checkboxesGridDisabled = true;
    checkboxesGrid.setVisible(false);
  }

  @Override
  public void setElement(@NonNull Element element) {
    super.setElement(element);

    availableTypeDefinitions = collectAvailableTypeDefinitions(element.getId());
//    dataTypeCombo.getItems().setAll(availableTypeDefinitions.stream().map(TypeDefinition::getId).toList());

    dataTypeComboBox.getItems().setAll(availableDataTypes());
    checkboxesGrid.setVisible(!checkboxesGridDisabled && !isMultiSelectParent());

    Optional<FieldConfig> fieldConfig = getFieldConfig(element);
    FieldType fieldType = fieldConfig.map(FieldConfig::getFieldType).orElse(null);
    boolean isTypeDefinition = fieldType instanceof TypeDefFieldType;

    setFieldValue(typeDefinitionCheckBox, isTypeDefinition);
    setFieldValue(dataTypeCombo, isTypeDefinition ? ((TypeDefFieldType) fieldType).getTypeDefType().getTypeDefinitionId() : "");
    setFieldValue(dataTypeComboBox, getDataTypeName(fieldType));

    setFieldValue(globalCheckBox, Boolean.TRUE.equals(fieldConfig.map(FieldConfig::getGlobal).orElse(null)));
    setFieldValue(transientCheckBox, Boolean.TRUE.equals(fieldConfig.map(FieldConfig::getTransientField).orElse(null)));

    RequirednessConfig requirednessConfig = fieldConfig.map(FieldConfig::getRequirednessConfig).orElse(null);
    setFieldValue(requiredCheckBox, requirednessConfig != null);
    setFieldValue(requiredParentCheckBox,
        requirednessConfig != null && RequirednessConfig.MODE_REQUIRED_IF_PARENT_FILLED.equals(requirednessConfig.getMode()));
    setFieldValue(defaultErrorMessagesCheckbox,
        requirednessConfig == null || requirednessConfig.getErrorMessage().isEmpty());
  }

  @Override
  protected String validationProperty() {
    return ElementProperty.TYPE;
  }

  private static RequirednessConfig newRequirednessConfig(boolean onlyIfParentFilled) {
    RequirednessConfig requirednessConfig = new RequirednessConfig();
    requirednessConfig.setMode(onlyIfParentFilled ? RequirednessConfig.MODE_REQUIRED_IF_PARENT_FILLED : RequirednessConfig.MODE_REQUIRED);
    return requirednessConfig;
  }

  private static FieldType createFieldType(String dataType) {
    return switch (dataType == null ? TYPE_STRING : dataType) {
      case TYPE_NUMBER -> new NumberFieldType();
      case TYPE_DATE -> new DateFieldType();
      case TYPE_DATE_TIME -> new DateTimeFieldType();
      case TYPE_TIME -> new TimeFieldType();
      case TYPE_DATE_FRAGMENT -> new DateFragmentFieldType();
      case TYPE_DATE_RANGE -> new DateRangeFieldType();
      case TYPE_CONFIRM -> new ConfirmFieldType();
      case TYPE_BOOLEAN -> new BooleanFieldType();
      case TYPE_CUSTOM -> new CustomFieldFieldType();
      case TYPE_ENUMERATION -> new EnumerationFieldType();
      case TYPE_UNSPECIFIED -> new UnspecifiedFieldType();
      default -> new StringFieldType();
    };
  }

  private static String getDataTypeName(FieldType fieldType) {
    if (fieldType instanceof NumberFieldType) {
      return TYPE_NUMBER;
    } else if (fieldType instanceof DateTimeFieldType) {
      return TYPE_DATE_TIME;
    } else if (fieldType instanceof DateFragmentFieldType) {
      return TYPE_DATE_FRAGMENT;
    } else if (fieldType instanceof DateRangeFieldType) {
      return TYPE_DATE_RANGE;
    } else if (fieldType instanceof DateFieldType) {
      return TYPE_DATE;
    } else if (fieldType instanceof TimeFieldType) {
      return TYPE_TIME;
    } else if (fieldType instanceof ConfirmFieldType) {
      return TYPE_CONFIRM;
    } else if (fieldType instanceof BooleanFieldType) {
      return TYPE_BOOLEAN;
    } else if (fieldType instanceof CustomFieldFieldType) {
      return TYPE_CUSTOM;
    } else if (fieldType instanceof EnumerationFieldType) {
      return TYPE_ENUMERATION;
    } else if (fieldType instanceof UnspecifiedFieldType) {
      return TYPE_UNSPECIFIED;
    }
    return TYPE_STRING;
  }

  private static void withFieldConfig(Element element, Consumer<FieldConfig> mutator) {
    getFieldConfig(element).ifPresent(mutator);
  }

  private static Optional<FieldConfig> getFieldConfig(Element element) {
    if (element instanceof FieldElement fieldElement && fieldElement.getField() != null) {
      return Optional.of(fieldElement.getField());
    }
    return Optional.empty();
  }

  /**
   * A multi-select group's children are the field-choices it offers, so only String and Enumeration are
   * valid data types there; every other parent allows the full {@link #DATA_TYPES} list.
   */
  private List<String> availableDataTypes() {
    return isMultiSelectParent() ? MULTI_SELECT_DATA_TYPES : DATA_TYPES;
  }

  /**
   * Global/transient/required are meaningless for a multi-select group's field-choice children, so the
   * checkboxesGrid is hidden there too.
   */
  public boolean isMultiSelectParent() {
    Element parent = ancestors.isEmpty() ? null : ancestors.get(ancestors.size() - 1);
    return parent instanceof GroupElement groupElement
        && groupElement.getGroup() != null
        && GroupConfig.USAGE_TYPE_MULTI_SELECT.equals(groupElement.getGroup().getUsageType());
  }

  /**
   * A multi-select group's String choices have no error-message-relevant validation of their own, so the
   * "Use default error messages" checkbox is meaningless there and stays hidden.
   */
  private boolean isMultiSelectStringDataType() {
    return isMultiSelectParent() && TYPE_STRING.equals(dataTypeComboBox.getValue());
  }

  /**
   * Every type definition a field can point to via {@link TypeDefFieldType}: the current document model's
   * own type definitions plus every other document model's in the same project (a type definition model is
   * just a document model whose header carries the "tdonly" annotation, so it's included here too), minus
   * {@code excludedId} itself so a type definition can't reference itself.
   */
  private static List<TypeDefinition> collectAvailableTypeDefinitions(@NonNull String excludedId) {
    ProjectItem projectItem = Studio.getSelectedProjectItem();
    if (projectItem == null) {
      return List.of();
    }

    List<TypeDefinition> result = new ArrayList<>();
    if (projectItem.getModel() instanceof DocumentModel documentModel) {
      result.addAll(documentModel.getContent().getTypeDefinitions());
    }
    for (DocumentModel other : ProjectDocumentModels.getOtherDocumentModels(projectItem)) {
      result.addAll(other.getContent().getTypeDefinitions());
    }
    result.removeIf(typeDefinition -> excludedId.equals(typeDefinition.getId()));
    return result;
  }
}
