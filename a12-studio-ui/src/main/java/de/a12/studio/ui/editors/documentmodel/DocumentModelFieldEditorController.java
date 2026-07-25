package de.a12.studio.ui.editors.documentmodel;

import de.a12.studio.models.documentmodel.Element;
import de.a12.studio.ui.editors.AbstractPropertyEditor;
import de.a12.studio.ui.editors.propertyeditors.AnnotationsPanelController;
import de.a12.studio.ui.editors.propertyeditors.DataTypeConfigurationPanelController;
import de.a12.studio.ui.editors.propertyeditors.GeneralInformationPanelController;
import de.a12.studio.ui.editors.propertyeditors.LocalizedTextPanelController;
import de.a12.studio.ui.editors.propertyeditors.TypeDefinitionPanelController;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TitledPane;
import org.jspecify.annotations.NonNull;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class DocumentModelFieldEditorController implements ElementEditorController, Initializable {

  @FXML
  private GeneralInformationPanelController generalInformationController;

  @FXML
  private TypeDefinitionPanelController typeDefinitionController;

  @FXML
  private DataTypeConfigurationPanelController dataTypeConfigurationController;

  @FXML
  private TitledPane errorMessages;

  @FXML
  private LocalizedTextPanelController errorMessagesController;

  @FXML
  private LocalizedTextPanelController labelController;

  @FXML
  private LocalizedTextPanelController descriptionInternalController;

  @FXML
  private LocalizedTextPanelController descriptionExternalController;

  @FXML
  private LocalizedTextPanelController helperTextController;

  @FXML
  private AnnotationsPanelController annotationsController;

  private List<AbstractPropertyEditor> propertyEditors;

  private Element element;

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    labelController.configureLabel();
    descriptionInternalController.configureInternal();
    descriptionExternalController.configureExternal();
    errorMessagesController.configureErrorMessages();
    helperTextController.configureHelperText();

    errorMessages.managedProperty().bind(errorMessages.visibleProperty());
    errorMessages.visibleProperty().bind(dataTypeConfigurationController.patternProperty().isNotEmpty());

    typeDefinitionController.fieldTypeProperty().addListener((observable, oldValue, newValue) -> {
      if (element != null) {
        dataTypeConfigurationController.setElement(element);
      }
    });

    propertyEditors = List.of(generalInformationController, typeDefinitionController, dataTypeConfigurationController, errorMessagesController,
        labelController, descriptionInternalController, descriptionExternalController,
        helperTextController, annotationsController);
  }

  @Override
  public void setElement(@NonNull Element element, @NonNull List<Element> ancestors) {
    this.element = element;
    generalInformationController.setAncestors(ancestors);
    typeDefinitionController.setAncestors(ancestors);
    boolean readOnly = isWithinAttachment(ancestors);
    propertyEditors.forEach(propertyEditor -> {
      propertyEditor.setElement(element);
      propertyEditor.setEditorDisabled(readOnly);
    });
  }
}
