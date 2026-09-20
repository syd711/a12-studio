package de.a12.studio.modelsvalidation.validators.form;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.formmodel.Control;
import de.a12.studio.models.formmodel.FormModel;
import de.a12.studio.models.formmodel.FormModelContent;
import de.a12.studio.models.formmodel.FormModelWalker;
import de.a12.studio.models.formmodel.Screen;
import de.a12.studio.models.formmodel.ScreenElement;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.Severity;
import de.a12.studio.modelsvalidation.ValidationContext;
import de.a12.studio.modelsvalidation.ValidationMessages;
import de.a12.studio.modelsvalidation.validators.ElementIndex;
import de.a12.studio.modelsvalidation.validators.ModelValidator;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * A control dependency must be controlled by exactly one instance of its trigger field (SME's
 * {@code areControlAndScreenElementCompatible}, and the BA docs' "not allowed to select screen elements placed in a
 * data context outside of the context of the trigger field"). Whether that holds depends on the Document Model - a
 * group that became repeatable, or a field that moved into one, makes a formerly fine dependency ambiguous - so it
 * is checked here against the current model rather than being a property of the form alone. Reported per hidden
 * element: it is in a data context the trigger cannot uniquely control, or it contains the trigger itself.
 *
 * <p>Entries {@link DependentControlOptionsMustExistValidator} already reports (missing, other screen, wrong type)
 * are not repeated, and a trigger whose field cannot be resolved is skipped.</p>
 */
public final class FormDependentControlContextValidator implements ModelValidator {

  @Override
  public List<ModelValidationError> validate(A12Model<?> model, ValidationContext context) {
    if (!(model instanceof FormModel formModel) || formModel.getContent() == null) {
      return List.of();
    }
    List<ElementIndex> indexes = FormDocumentModelIndexes.referencedDocumentModelIndexes(model, context);
    if (indexes.isEmpty()) {
      return List.of();
    }
    FormModelContent content = formModel.getContent();
    List<ModelValidationError> errors = new ArrayList<>();
    for (Control control : FormModelWalker.find(content, Control.class)) {
      if (control.getDependentControls() == null) {
        continue;
      }
      Optional<ElementIndex> index = indexes.stream().filter(candidate -> candidate.isResolvable(control.getElementRef())).findFirst();
      Optional<Screen> screen = DependentControlSupport.topLevelScreen(control, content);
      if (index.isEmpty() || screen.isEmpty()) {
        continue;
      }
      List<ScreenElement> elementsOnScreen = FormModelWalker.find(screen.get(), ScreenElement.class, node -> true);
      for (Control.DependentControls.Entry entry : control.getDependentControls().getScreenElement()) {
        Optional<ScreenElement> target = elementsOnScreen.stream()
            .filter(element -> element.getId() != null && element.getId().equals(entry.getIdref()))
            .filter(DependentControlSupport::isAllowedType)
            .findFirst();
        if (target.isEmpty()) {
          continue;
        }
        String messageKey = problemOf(target.get(), control, content, index.get());
        if (messageKey != null) {
          errors.add(new ModelValidationError(formModel, control.getId(),
              ValidationMessages.get(messageKey, DependentControlOptionsMustExistValidator.controlLabel(control), entry.getIdref()),
              Severity.ERROR.name()));
        }
      }
    }
    return errors;
  }

  private static String problemOf(ScreenElement target, Control master, FormModelContent content, ElementIndex index) {
    if (!DependentControlSupport.isCandidate(target, master, null, null)) {
      return "validation.dependentControls.optionContainsControl";
    }
    return DependentControlSupport.isCompatible(target, master, content, index) ? null : "validation.dependentControls.optionOtherContext";
  }
}
