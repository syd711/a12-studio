package de.a12.studio.ui.editors.documentmodel;

import de.a12.studio.dataservices.models.documentmodel.Element;
import de.a12.studio.ui.editors.AbstractPropertyEditor;
import de.a12.studio.ui.editors.propertyeditors.AnnotationsPanelController;
import de.a12.studio.ui.editors.propertyeditors.DataTypeConfigurationPanelController;
import de.a12.studio.ui.editors.propertyeditors.DescriptionExternalPanelController;
import de.a12.studio.ui.editors.propertyeditors.DescriptionInternalPanelController;
import de.a12.studio.ui.editors.propertyeditors.FieldInformationPanelController;
import de.a12.studio.ui.editors.propertyeditors.HelperTextPanelController;
import de.a12.studio.ui.editors.propertyeditors.LabelPanelController;
import de.a12.studio.ui.editors.propertyeditors.SuggestionsPanelController;
import de.a12.studio.ui.editors.propertyeditors.TypeDefinitionPanelController;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import org.jspecify.annotations.NonNull;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class DocumentModelFieldEditorController implements ElementEditorController, Initializable {

  @FXML
  private FieldInformationPanelController generalInformationController;

  @FXML
  private TypeDefinitionPanelController typeDefinitionController;

  @FXML
  private DataTypeConfigurationPanelController dataTypeConfigurationController;

  @FXML
  private SuggestionsPanelController suggestionsController;

  @FXML
  private LabelPanelController labelController;

  @FXML
  private DescriptionInternalPanelController descriptionInternalController;

  @FXML
  private DescriptionExternalPanelController descriptionExternalController;

  @FXML
  private HelperTextPanelController helperTextController;

  @FXML
  private AnnotationsPanelController annotationsController;

  private List<AbstractPropertyEditor> propertyEditors;

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    propertyEditors = List.of(generalInformationController, typeDefinitionController, dataTypeConfigurationController,
        suggestionsController, labelController, descriptionInternalController, descriptionExternalController,
        helperTextController, annotationsController);
  }

  @Override
  public void setElement(@NonNull Element element, @NonNull List<Element> ancestors) {
    generalInformationController.setAncestors(ancestors);
    propertyEditors.forEach(propertyEditor -> propertyEditor.setElement(element));
  }
}
