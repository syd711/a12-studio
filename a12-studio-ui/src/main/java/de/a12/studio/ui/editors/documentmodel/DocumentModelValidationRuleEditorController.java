package de.a12.studio.ui.editors.documentmodel;

import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.documentmodel.Element;
import de.a12.studio.models.documentmodel.RuleElement;
import de.a12.studio.models.documentmodel.rulelang.RuleLanguageSyntaxChecker;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.modelsvalidation.kernel.RuleConditionKernelCheck;
import de.a12.studio.modelsvalidation.validators.ElementIndex;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.editors.AbstractPropertyEditor;
import de.a12.studio.ui.editors.propertyeditors.AnnotationsPanelController;
import de.a12.studio.ui.editors.propertyeditors.GeneralInformationPanelController;
import de.a12.studio.ui.editors.propertyeditors.LocalizedTextPanelController;
import de.a12.studio.ui.editors.propertyeditors.PlainPathSuggestionProvider;
import de.a12.studio.ui.editors.propertyeditors.RuleEditorController;
import de.a12.studio.ui.editors.propertyeditors.RuleLanguageConstructs;
import de.a12.studio.ui.util.StudioBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import org.jspecify.annotations.NonNull;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.BiConsumer;

public class DocumentModelValidationRuleEditorController implements ElementEditorController, Initializable {

  @FXML
  private GeneralInformationPanelController generalInformationController;

  @FXML
  private RulePropertiesPanelController rulePropertiesController;

  @FXML
  private TargetFieldPanelController errorEntityController;

  @FXML
  private RuleEditorController errorConditionController;

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
    errorConditionController.setValidator(RuleLanguageSyntaxChecker::validate);
    // Loads the kernel in the background so the first rule the user selects is not slowed down by it.
    RuleConditionKernelCheck.warmUpAsync();
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
    boolean readOnly = isWithinAttachment(ancestors) || isWithinInclude(ancestors);

    ProjectItem projectItem = Studio.getSelectedProjectItem();
    // Grammar first (instant, no kernel needed), then the kernel for what only the model can tell - unknown fields,
    // wrong arguments. Read-only rules (attachment/include) are not the user's to fix, so no kernel opinion there.
    RuleConditionKernelCheck kernelCheck = !readOnly && projectItem != null && projectItem.getModel() instanceof DocumentModel model
        ? new RuleConditionKernelCheck(projectItem, model.getId(), element.getId()) : null;
    // Before setCustom, which validates the initial text.
    errorConditionController.setValidator(text -> {
      String syntaxError = RuleLanguageSyntaxChecker.validate(text);
      return syntaxError != null || kernelCheck == null ? syntaxError : kernelCheck.check(text);
    });
    errorConditionController.setCustom(() -> rule.getRule().getErrorCondition(), value -> rule.getRule().setErrorCondition(value));

    if (projectItem != null && projectItem.getModel() instanceof DocumentModel documentModel) {
      errorConditionController.setSuggestionProvider(new PlainPathSuggestionProvider(new ElementIndex(documentModel), element));
      errorConditionController.setHighlightedFunctionNames(RuleLanguageConstructs.NAMES);
    }

    propertyEditors.forEach(propertyEditor -> {
      propertyEditor.setElement(element);
      propertyEditor.setEditorDisabled(readOnly);
    });
  }

  @Override
  public void setRenameHandler(@NonNull BiConsumer<Element, String> renameHandler) {
    generalInformationController.setRenameHandler(renameHandler);
  }

  @Override
  public void destroy() {
    propertyEditors.forEach(AbstractPropertyEditor::destroy);
  }
}
