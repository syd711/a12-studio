package de.a12.studio.ui.editors.documentmodel;

import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.documentmodel.Element;
import de.a12.studio.models.documentmodel.RuleElement;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.modelsvalidation.validators.ElementIndex;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.editors.AbstractPropertyEditor;
import de.a12.studio.ui.editors.propertyeditors.AnnotationsPanelController;
import de.a12.studio.ui.editors.propertyeditors.GeneralInformationPanelController;
import de.a12.studio.ui.editors.propertyeditors.LocalizedTextPanelController;
import de.a12.studio.ui.editors.propertyeditors.PlainPathSuggestionProvider;
import de.a12.studio.ui.editors.propertyeditors.RichtextEditorController;
import de.a12.studio.ui.util.StudioBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import org.jspecify.annotations.NonNull;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class DocumentModelValidationRuleEditorController implements ElementEditorController, Initializable {

  @FXML
  private GeneralInformationPanelController generalInformationController;

  @FXML
  private RulePropertiesPanelController rulePropertiesController;

  @FXML
  private TargetFieldPanelController errorEntityController;

  @FXML
  private RichtextEditorController errorConditionController;

  @FXML
  private LocalizedTextPanelController errorMessageController;

  @FXML
  private LocalizedTextPanelController descriptionInternalController;

  @FXML
  private LocalizedTextPanelController descriptionExternalController;

  @FXML
  private AnnotationsPanelController annotationsController;

  private List<AbstractPropertyEditor> propertyEditors;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    errorEntityController.configureRuleErrorEntity();
    errorConditionController.configureCustom("errorCondition", StudioBundle.get("error_condition"));
    errorMessageController.configureRuleErrorMessage();
    descriptionInternalController.configureInternal();
    descriptionExternalController.configureExternal();

    propertyEditors = List.of(generalInformationController, rulePropertiesController, errorEntityController,
        errorConditionController, errorMessageController, descriptionInternalController,
        descriptionExternalController, annotationsController);
  }

  @Override
  public void setElement(@NonNull Element element, @NonNull List<Element> ancestors) {
    generalInformationController.setAncestors(ancestors);
    RuleElement rule = (RuleElement) element;
    errorConditionController.setCustom(() -> rule.getRule().getErrorCondition(), value -> rule.getRule().setErrorCondition(value));

    ProjectItem projectItem = Studio.getSelectedProjectItem();
    if (projectItem != null && projectItem.getModel() instanceof DocumentModel documentModel) {
      errorConditionController.setSuggestionProvider(new PlainPathSuggestionProvider(new ElementIndex(documentModel), element));
    }

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
