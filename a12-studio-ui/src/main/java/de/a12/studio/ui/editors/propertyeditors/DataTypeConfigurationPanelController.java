package de.a12.studio.ui.editors.propertyeditors;

import de.a12.studio.models.documentmodel.CustomFieldFieldType;
import de.a12.studio.models.documentmodel.DateFragmentFieldType;
import de.a12.studio.models.documentmodel.DateRangeFieldType;
import de.a12.studio.models.documentmodel.Element;
import de.a12.studio.models.documentmodel.EnumerationFieldType;
import de.a12.studio.models.documentmodel.FieldElement;
import de.a12.studio.models.documentmodel.NumberFieldType;
import de.a12.studio.models.documentmodel.StringFieldType;
import de.a12.studio.ui.editors.AbstractPropertyEditor;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.layout.VBox;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Container panel that hosts whichever data-type-specific configuration editor applies to the currently
 * selected element's field type (e.g. {@link DataTypeStringConfigurationPanelController} for a
 * {@link StringFieldType}). Field types without a dedicated configuration editor leave the content area empty.
 */
@Slf4j
public class DataTypeConfigurationPanelController extends AbstractPropertyEditor implements Initializable {

  @FXML
  private VBox content;

  private Node stringConfigurationNode;

  private DataTypeStringConfigurationPanelController stringConfigurationController;

  private Node numberConfigurationNode;

  private DataTypeNumberConfigurationPanelController numberConfigurationController;

  private Node dateFragmentConfigurationNode;

  private DataTypeDateFragmentConfigurationPanelController dateFragmentConfigurationController;

  private Node dateRangeConfigurationNode;

  private DataTypeDateRangeConfigurationPanelController dateRangeConfigurationController;

  private Node customConfigurationNode;

  private DataTypeCustomConfigurationPanelController customConfigurationController;

  private Node enumerationConfigurationNode;

  private DataTypeEnumerationConfigurationPanelController enumerationConfigurationController;

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    super.initialize(url, resourceBundle);

    FXMLLoader loader = new FXMLLoader(DataTypeStringConfigurationPanelController.class.getResource("data-type-string-configuration-panel.fxml"));
    try {
      stringConfigurationNode = loader.load();
    } catch (IOException e) {
      log.error("Error loading data-type-string-configuration-panel.fxml: " + e.getMessage(), e);
      return;
    }
    stringConfigurationController = loader.getController();

    FXMLLoader numberLoader = new FXMLLoader(DataTypeNumberConfigurationPanelController.class.getResource("data-type-number-configuration-panel.fxml"));
    try {
      numberConfigurationNode = numberLoader.load();
    } catch (IOException e) {
      log.error("Error loading data-type-number-configuration-panel.fxml: " + e.getMessage(), e);
      return;
    }
    numberConfigurationController = numberLoader.getController();

    FXMLLoader dateFragmentLoader = new FXMLLoader(DataTypeDateFragmentConfigurationPanelController.class.getResource("data-type-date-fragment-configuration-panel.fxml"));
    try {
      dateFragmentConfigurationNode = dateFragmentLoader.load();
    } catch (IOException e) {
      log.error("Error loading data-type-date-fragment-configuration-panel.fxml: " + e.getMessage(), e);
      return;
    }
    dateFragmentConfigurationController = dateFragmentLoader.getController();

    FXMLLoader dateRangeLoader = new FXMLLoader(DataTypeDateRangeConfigurationPanelController.class.getResource("data-type-date-range-configuration-panel.fxml"));
    try {
      dateRangeConfigurationNode = dateRangeLoader.load();
    } catch (IOException e) {
      log.error("Error loading data-type-date-range-configuration-panel.fxml: " + e.getMessage(), e);
      return;
    }
    dateRangeConfigurationController = dateRangeLoader.getController();

    FXMLLoader customLoader = new FXMLLoader(DataTypeCustomConfigurationPanelController.class.getResource("data-type-custom-configuration-panel.fxml"));
    try {
      customConfigurationNode = customLoader.load();
    } catch (IOException e) {
      log.error("Error loading data-type-custom-configuration-panel.fxml: " + e.getMessage(), e);
      return;
    }
    customConfigurationController = customLoader.getController();

    FXMLLoader enumerationLoader = new FXMLLoader(DataTypeEnumerationConfigurationPanelController.class.getResource("data-type-enumeration-configuration-panel.fxml"));
    try {
      enumerationConfigurationNode = enumerationLoader.load();
    } catch (IOException e) {
      log.error("Error loading data-type-enumeration-configuration-panel.fxml: " + e.getMessage(), e);
      return;
    }
    enumerationConfigurationController = enumerationLoader.getController();
  }

  public ReadOnlyStringProperty patternProperty() {
    return stringConfigurationController.patternProperty();
  }

  @Override
  public void setElement(@NonNull Element element) {
    super.setElement(element);

    stringConfigurationController.setElement(element);
    numberConfigurationController.setElement(element);
    dateFragmentConfigurationController.setElement(element);
    dateRangeConfigurationController.setElement(element);
    customConfigurationController.setElement(element);
    enumerationConfigurationController.setElement(element);
    if (isStringFieldType(element)) {
      content.getChildren().setAll(List.of(stringConfigurationNode));
    } else if (isNumberFieldType(element)) {
      content.getChildren().setAll(List.of(numberConfigurationNode));
    } else if (isDateFragmentFieldType(element)) {
      content.getChildren().setAll(List.of(dateFragmentConfigurationNode));
    } else if (isDateRangeFieldType(element)) {
      content.getChildren().setAll(List.of(dateRangeConfigurationNode));
    } else if (isCustomFieldType(element)) {
      content.getChildren().setAll(List.of(customConfigurationNode));
    } else if (isEnumerationFieldType(element)) {
      content.getChildren().setAll(List.of(enumerationConfigurationNode));
    } else {
      content.getChildren().setAll(List.of());
    }
    setEditorVisible(!content.getChildren().isEmpty());
  }

  private static boolean isStringFieldType(Element element) {
    return element instanceof FieldElement fieldElement
        && fieldElement.getField() != null
        && fieldElement.getField().getFieldType() instanceof StringFieldType;
  }

  private static boolean isNumberFieldType(Element element) {
    return element instanceof FieldElement fieldElement
        && fieldElement.getField() != null
        && fieldElement.getField().getFieldType() instanceof NumberFieldType;
  }

  private static boolean isDateFragmentFieldType(Element element) {
    return element instanceof FieldElement fieldElement
        && fieldElement.getField() != null
        && fieldElement.getField().getFieldType() instanceof DateFragmentFieldType;
  }

  private static boolean isDateRangeFieldType(Element element) {
    return element instanceof FieldElement fieldElement
        && fieldElement.getField() != null
        && fieldElement.getField().getFieldType() instanceof DateRangeFieldType;
  }

  private static boolean isCustomFieldType(Element element) {
    return element instanceof FieldElement fieldElement
        && fieldElement.getField() != null
        && fieldElement.getField().getFieldType() instanceof CustomFieldFieldType;
  }

  private static boolean isEnumerationFieldType(Element element) {
    return element instanceof FieldElement fieldElement
        && fieldElement.getField() != null
        && fieldElement.getField().getFieldType() instanceof EnumerationFieldType;
  }
}
