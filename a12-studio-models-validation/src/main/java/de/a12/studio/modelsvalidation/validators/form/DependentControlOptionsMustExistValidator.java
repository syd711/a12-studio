package de.a12.studio.modelsvalidation.validators.form;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.formmodel.Control;
import de.a12.studio.models.formmodel.ControlGrid;
import de.a12.studio.models.formmodel.CustomScreenElement;
import de.a12.studio.models.formmodel.DependentCase;
import de.a12.studio.models.formmodel.FieldConfigEntry;
import de.a12.studio.models.formmodel.FormModel;
import de.a12.studio.models.formmodel.FormModelContent;
import de.a12.studio.models.formmodel.FormModelWalker;
import de.a12.studio.models.formmodel.MultiColumnSection;
import de.a12.studio.models.formmodel.Screen;
import de.a12.studio.models.formmodel.ScreenElement;
import de.a12.studio.models.formmodel.Section;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.Severity;
import de.a12.studio.modelsvalidation.ValidationContext;
import de.a12.studio.modelsvalidation.ValidationMessages;
import de.a12.studio.modelsvalidation.validators.ModelValidator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The screen elements a Control's dependency hides must still exist (SME's
 * {@code DependentControlOptionsMustExistInFormModel}, "either invalid or missing in the form model"). Two
 * representations are checked:
 * <ul>
 *   <li>{@code Control.dependentControls} (SME's wire shape): each {@code idref} must name a screen element that
 *       exists, is on the <em>same top-level screen</em> as the Control and is a Section, Multi Column Section,
 *       Control Grid or Custom Screen Element (SME's {@code isAllowedDependentControlType}); the message says
 *       which of the three is wrong.</li>
 *   <li>{@code DependentCase.notRelevantNodes} (a12-studio's Confirm control Dependencies tab): each id must
 *       still name a screen element - deleting a node otherwise leaves a dangling id behind.</li>
 * </ul>
 */
public final class DependentControlOptionsMustExistValidator implements ModelValidator {

  private enum Problem {
    NO_ELEMENT_SELECTED("validation.dependentControls.optionBlank"),
    MISSING("validation.dependentControls.optionMissing"),
    OTHER_SCREEN("validation.dependentControls.optionOtherScreen"),
    INVALID_TYPE("validation.dependentControls.optionInvalidType");

    private final String messageKey;

    Problem(String messageKey) {
      this.messageKey = messageKey;
    }
  }

  @Override
  public List<ModelValidationError> validate(A12Model<?> model, ValidationContext context) {
    if (!(model instanceof FormModel formModel) || formModel.getContent() == null) {
      return List.of();
    }
    FormModelContent content = formModel.getContent();
    List<ModelValidationError> errors = new ArrayList<>();
    checkDependentControls(formModel, content, errors);
    checkNotRelevantNodes(formModel, content, errors);
    return errors;
  }

  private static void checkDependentControls(FormModel model, FormModelContent content, List<ModelValidationError> errors) {
    Set<String> knownIds = elementIds(content);
    for (Screen screen : content.getScreens()) {
      List<ScreenElement> elementsOnScreen = FormModelWalker.find(screen, ScreenElement.class, node -> true);
      Set<String> idsOnScreen = new HashSet<>();
      elementsOnScreen.forEach(element -> idsOnScreen.add(element.getId()));
      for (Control control : FormModelWalker.find(screen, Control.class, node -> true)) {
        if (control.getDependentControls() == null) {
          continue;
        }
        for (Control.DependentControls.Entry entry : control.getDependentControls().getScreenElement()) {
          Problem problem = problemOf(entry.getIdref(), knownIds, idsOnScreen, elementsOnScreen);
          if (problem != null) {
            errors.add(new ModelValidationError(model, control.getId(),
                ValidationMessages.get(problem.messageKey, controlLabel(control), entry.getIdref()), Severity.ERROR.name()));
          }
        }
      }
    }
  }

  private static Problem problemOf(String idref, Set<String> knownIds, Set<String> idsOnScreen,
      List<ScreenElement> elementsOnScreen) {
    if (idref == null || idref.isBlank()) {
      return Problem.NO_ELEMENT_SELECTED;
    }
    if (!knownIds.contains(idref)) {
      return Problem.MISSING;
    }
    if (!idsOnScreen.contains(idref)) {
      return Problem.OTHER_SCREEN;
    }
    boolean allowedType = elementsOnScreen.stream().anyMatch(element -> idref.equals(element.getId()) && isAllowedType(element));
    return allowedType ? null : Problem.INVALID_TYPE;
  }

  private static boolean isAllowedType(ScreenElement element) {
    return element instanceof Section || element instanceof MultiColumnSection || element instanceof ControlGrid
        || element instanceof CustomScreenElement;
  }

  private static void checkNotRelevantNodes(FormModel model, FormModelContent content, List<ModelValidationError> errors) {
    if (content.getFieldConfiguration() == null) {
      return;
    }
    Set<String> knownIds = elementIds(content);
    for (FieldConfigEntry entry : content.getFieldConfiguration().getField()) {
      if (entry.getDependentField() == null) {
        continue;
      }
      for (DependentCase dependentCase : entry.getDependentField().getCases()) {
        for (String nodeId : dependentCase.getNotRelevantNodes()) {
          if (!knownIds.contains(nodeId)) {
            errors.add(new ModelValidationError(model, FormFieldReferenceValidator.ELEMENT_ID,
                ValidationMessages.get("validation.dependentControls.notRelevantNodeMissing", entry.getElementRef(), nodeId),
                Severity.ERROR.name()));
          }
        }
      }
    }
  }

  private static Set<String> elementIds(FormModelContent content) {
    Set<String> ids = new HashSet<>();
    FormModelWalker.find(content, ScreenElement.class).forEach(element -> ids.add(element.getId()));
    return ids;
  }

  /** How a Control is named in a message: its element reference (what the tree shows), else its name, else its id. */
  static String controlLabel(Control control) {
    if (control.getElementRef() != null && !control.getElementRef().isBlank()) {
      return control.getElementRef();
    }
    return control.getName() != null && !control.getName().isBlank() ? control.getName() : control.getId();
  }
}
