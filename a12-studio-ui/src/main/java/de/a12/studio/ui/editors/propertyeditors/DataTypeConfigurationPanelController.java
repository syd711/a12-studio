package de.a12.studio.ui.editors.propertyeditors;

import de.a12.studio.models.documentmodel.Element;
import de.a12.studio.models.documentmodel.FieldElement;
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
  }

  public ReadOnlyStringProperty patternProperty() {
    return stringConfigurationController.patternProperty();
  }

  @Override
  public void setElement(@NonNull Element element) {
    super.setElement(element);

    stringConfigurationController.setElement(element);
    content.getChildren().setAll(isStringFieldType(element) ? List.of(stringConfigurationNode) : List.of());
  }

  private static boolean isStringFieldType(Element element) {
    return element instanceof FieldElement fieldElement
        && fieldElement.getField() != null
        && fieldElement.getField().getFieldType() instanceof StringFieldType;
  }
}
