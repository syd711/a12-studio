package de.a12.studio.ui.editors.propertyeditors;

import de.a12.studio.dataservices.models.documentmodel.BooleanFieldType;
import de.a12.studio.dataservices.models.documentmodel.ConfirmFieldType;
import de.a12.studio.dataservices.models.documentmodel.CustomFieldFieldType;
import de.a12.studio.dataservices.models.documentmodel.DateFieldType;
import de.a12.studio.dataservices.models.documentmodel.DateFragmentFieldType;
import de.a12.studio.dataservices.models.documentmodel.DateRangeFieldType;
import de.a12.studio.dataservices.models.documentmodel.DateTimeFieldType;
import de.a12.studio.dataservices.models.documentmodel.DocumentModel;
import de.a12.studio.dataservices.models.documentmodel.Element;
import de.a12.studio.dataservices.models.documentmodel.EnumerationFieldType;
import de.a12.studio.dataservices.models.documentmodel.FieldConfig;
import de.a12.studio.dataservices.models.documentmodel.FieldElement;
import de.a12.studio.dataservices.models.documentmodel.FieldType;
import de.a12.studio.dataservices.models.documentmodel.NumberFieldType;
import de.a12.studio.dataservices.models.documentmodel.RequirednessConfig;
import de.a12.studio.dataservices.models.documentmodel.StringFieldType;
import de.a12.studio.dataservices.models.documentmodel.TimeFieldType;
import de.a12.studio.dataservices.models.documentmodel.TypeDefFieldType;
import de.a12.studio.dataservices.models.documentmodel.TypeDefinition;
import de.a12.studio.dataservices.models.documentmodel.UnspecifiedFieldType;
import de.a12.studio.dataservices.projects.ProjectItem;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.editors.AbstractPropertyEditor;
import de.a12.studio.ui.util.ProjectDocumentModels;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
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
  private static final String TYPE_CUSTOM_FIELD = "CustomField";
  private static final String TYPE_ENUMERATION = "Enumeration";
  private static final String TYPE_UNSPECIFIED = "Unspecified";

  // Order mirrors the field-type dropdown in the SME reference implementation (DomainField.json).
  private static final List<String> DATA_TYPES = List.of(
      TYPE_STRING, TYPE_NUMBER, TYPE_DATE, TYPE_DATE_TIME, TYPE_TIME, TYPE_DATE_FRAGMENT, TYPE_DATE_RANGE,
      TYPE_CONFIRM, TYPE_BOOLEAN, TYPE_CUSTOM_FIELD, TYPE_ENUMERATION, TYPE_UNSPECIFIED);

  @FXML
  private ComboBox<String> dataTypeCombo;

  @FXML
  private CheckBox typeDefinitionCheckBox;

  @FXML
  private CheckBox requiredParentCheckBox;

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

  private List<TypeDefinition> availableTypeDefinitions = List.of();

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    super.initialize(url, resourceBundle);

    dataTypeComboBox.getItems().addAll(DATA_TYPES);

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
    dataTypeComboBox.disableProperty().bind(typeDefinitionCheckBox.selectedProperty());

    requiredParentBox.managedProperty().bind(requiredParentBox.visibleProperty());
    requiredParentBox.visibleProperty().bind(requiredCheckBox.selectedProperty());

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

    bindCheckBox(typeDefinitionCheckBox, (element, value) -> withFieldConfig(element, field ->
        field.setFieldType(value ? new TypeDefFieldType() : createFieldType(dataTypeComboBox.getValue()))));
    bindComboBox(dataTypeCombo, (element, value) -> withFieldConfig(element, field -> {
      if (field.getFieldType() instanceof TypeDefFieldType typeDefFieldType) {
        typeDefFieldType.getTypeDefType().setTypeDefinitionId(value);
      }
    }));
    bindComboBox(dataTypeComboBox, (element, value) -> withFieldConfig(element, field -> {
      if (!(field.getFieldType() instanceof TypeDefFieldType)) {
        field.setFieldType(createFieldType(value));
      }
    }));
  }

  @Override
  public void setElement(@NonNull Element element) {
    super.setElement(element);

    availableTypeDefinitions = collectAvailableTypeDefinitions(element.getId());
    dataTypeCombo.getItems().setAll(availableTypeDefinitions.stream().map(TypeDefinition::getId).toList());

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
      case TYPE_CUSTOM_FIELD -> new CustomFieldFieldType();
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
      return TYPE_CUSTOM_FIELD;
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
