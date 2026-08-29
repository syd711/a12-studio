package de.a12.studio.ui.editors.documentmodel;

import de.a12.studio.models.documentmodel.Element;
import de.a12.studio.ui.editors.AbstractPropertyEditor;
import de.a12.studio.ui.editors.propertyeditors.AnnotationsPanelController;
import de.a12.studio.ui.editors.propertyeditors.GeneralInformationPanelController;
import de.a12.studio.ui.editors.propertyeditors.LocalizedTextPanelController;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import org.jspecify.annotations.NonNull;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class DocumentModelIncludeEditorController implements ElementEditorController, Initializable {

  @FXML
  private GeneralInformationPanelController generalInformationController;

  @FXML
  private GroupPropertiesPanelController propertiesController;

  @FXML
  private IncludePropertiesPanelController includePropertiesController;

  @FXML
  private LocalizedTextPanelController labelController;

  @FXML
  private LocalizedTextPanelController descriptionInternalController;

  @FXML
  private LocalizedTextPanelController descriptionExternalController;

  @FXML
  private AnnotationsPanelController annotationsController;

  private List<AbstractPropertyEditor> propertyEditors;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    labelController.configureLabel();
    descriptionInternalController.configureInternal();
    descriptionExternalController.configureExternal();

    propertyEditors = List.of(generalInformationController, propertiesController, includePropertiesController, labelController,
        descriptionInternalController, descriptionExternalController, annotationsController);
  }

  @Override
  public void setElement(@NonNull Element element, @NonNull List<Element> ancestors) {
    generalInformationController.setAncestors(ancestors);
    boolean readOnly = isWithinAttachment(ancestors) || isWithinInclude(ancestors);
    propertyEditors.forEach(propertyEditor -> {
      propertyEditor.setElement(element);
      propertyEditor.setEditorDisabled(readOnly);
    });
  }

  @Override
  public void destroy() {
    propertyEditors.forEach(AbstractPropertyEditor::destroy);
  }
}
