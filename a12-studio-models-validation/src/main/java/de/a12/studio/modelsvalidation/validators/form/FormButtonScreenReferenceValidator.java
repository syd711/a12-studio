package de.a12.studio.modelsvalidation.validators.form;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.formmodel.Button;
import de.a12.studio.models.formmodel.ButtonGroup;
import de.a12.studio.models.formmodel.FormModel;
import de.a12.studio.models.formmodel.FormModelContent;
import de.a12.studio.models.formmodel.HeaderFooterBox;
import de.a12.studio.models.formmodel.NavigationButton;
import de.a12.studio.models.formmodel.Screen;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.Severity;
import de.a12.studio.modelsvalidation.ValidationContext;
import de.a12.studio.modelsvalidation.ValidationMessages;
import de.a12.studio.modelsvalidation.validators.ModelValidator;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Checks that every {@link NavigationButton} whose {@code target} looks like a screen id (i.e. it
 * is not a special navigation token such as {@code #previous} or {@code #next}) actually references
 * a screen that still exists in the Form Model.  Applies to the model-level {@code subHeaderBox} /
 * {@code footerBox} as well as to every per-screen {@code subHeaderBox} / {@code footerBox}.
 */
public final class FormButtonScreenReferenceValidator implements ModelValidator {

  // Special navigation tokens that do not reference a concrete screen id.
  private static final Set<String> SPECIAL_TARGETS = Set.of("#previous", "#next", "#first", "#last");

  @Override
  public List<ModelValidationError> validate(A12Model<?> model, ValidationContext context) {
    if (!(model instanceof FormModel formModel) || formModel.getContent() == null) {
      return List.of();
    }

    FormModelContent content = formModel.getContent();
    Set<String> knownScreenIds = content.getScreens().stream()
        .map(Screen::getId)
        .filter(id -> id != null && !id.isBlank())
        .collect(Collectors.toSet());

    List<ModelValidationError> errors = new ArrayList<>();

    // Model-level subHeaderBox and footerBox
    checkBox(content.getSubHeaderBox(), knownScreenIds, formModel, errors);
    checkBox(content.getFooterBox(), knownScreenIds, formModel, errors);

    // Per-screen subHeaderBox and footerBox
    for (Screen screen : content.getScreens()) {
      checkBox(screen.getSubHeaderBox(), knownScreenIds, formModel, errors);
      checkBox(screen.getFooterBox(), knownScreenIds, formModel, errors);
    }

    return errors;
  }

  private static void checkBox(HeaderFooterBox box, Set<String> knownScreenIds,
                                FormModel model, List<ModelValidationError> errors) {
    if (box == null) {
      return;
    }
    checkGroup(box.getMajorButtons(), knownScreenIds, model, errors);
    checkGroup(box.getMinorButtons(), knownScreenIds, model, errors);
  }

  private static void checkGroup(ButtonGroup group, Set<String> knownScreenIds,
                                  FormModel model, List<ModelValidationError> errors) {
    if (group == null) {
      return;
    }
    for (Button button : group.getButton()) {
      if (!(button instanceof NavigationButton navigationButton)) {
        continue;
      }
      String target = navigationButton.getTarget();
      if (target == null || target.isBlank() || SPECIAL_TARGETS.contains(target)) {
        continue;
      }
      if (!knownScreenIds.contains(target)) {
        String message = ValidationMessages.get(
            "validation.formButtonScreenReference.missing", target, navigationButton.getName());
        errors.add(new ModelValidationError(model, navigationButton.getId(), message, Severity.ERROR.name()));
      }
    }
  }
}
