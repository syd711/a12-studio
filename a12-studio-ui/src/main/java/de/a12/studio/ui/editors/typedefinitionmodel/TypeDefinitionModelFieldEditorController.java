package de.a12.studio.ui.editors.typedefinitionmodel;

import de.a12.studio.models.documentmodel.Element;
import de.a12.studio.ui.editors.AbstractPropertyEditor;
import de.a12.studio.ui.editors.documentmodel.ElementEditorController;
import de.a12.studio.ui.editors.propertyeditors.*;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import org.jspecify.annotations.NonNull;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class TypeDefinitionModelFieldEditorController implements ElementEditorController, Initializable {

  @FXML
  private GeneralInformationPanelController generalInformationController;

  @FXML
  private TypeDefinitionPanelController typeDefinitionController;

  @FXML
  private DataTypeConfigurationPanelController dataTypeConfigurationController;

  private List<AbstractPropertyEditor> propertyEditors;

  private Element element;

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    typeDefinitionController.setCustomTypeDisabled();
    typeDefinitionController.hideCheckboxesGrid();

    typeDefinitionController.fieldTypeProperty().addListener((observable, oldValue, newValue) -> {
      if (element != null) {
        dataTypeConfigurationController.setElement(element);
      }
    });

    propertyEditors = List.of(generalInformationController, typeDefinitionController, dataTypeConfigurationController);
  }

  @Override
  public void setElement(@NonNull Element element, @NonNull List<Element> ancestors) {
    this.element = element;
    generalInformationController.setAncestors(ancestors);
    propertyEditors.forEach(propertyEditor -> propertyEditor.setElement(element));
  }

  public void focusNameField() {
    generalInformationController.focusNameField();
  }
}
