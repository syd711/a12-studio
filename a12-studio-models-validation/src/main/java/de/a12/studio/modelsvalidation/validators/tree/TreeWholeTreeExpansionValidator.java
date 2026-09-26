package de.a12.studio.modelsvalidation.validators.tree;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.overviewmodel.BoxElement;
import de.a12.studio.models.overviewmodel.ElementBox;
import de.a12.studio.models.overviewmodel.ExpandAllPopupElement;
import de.a12.studio.models.treemodel.TreeModel;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.Severity;
import de.a12.studio.modelsvalidation.ValidationContext;
import de.a12.studio.modelsvalidation.ValidationMessages;
import de.a12.studio.modelsvalidation.validators.ModelValidator;

import java.util.ArrayList;
import java.util.List;

/**
 * "Enable Expand/Collapse The Whole Tree" and the Subheader's Expand All PopUp element belong together (SME's four
 * rules on the Tree meta model): the element is not allowed while the flag is off, and while it is on there must be
 * exactly one (in either slot).
 */
public final class TreeWholeTreeExpansionValidator implements ModelValidator {

  public static final String FLAG_ELEMENT_ID = "content/configuration/wholeTreeExpansion";
  public static final String POPUP_ELEMENT_ID = "content/subHeaderBox";

  @Override
  public List<ModelValidationError> validate(A12Model<?> model, ValidationContext context) {
    if (!(model instanceof TreeModel treeModel)) {
      return List.of();
    }
    boolean enabled = treeModel.getContent().getConfiguration() != null
        && Boolean.TRUE.equals(treeModel.getContent().getConfiguration().getWholeTreeExpansion());
    int popups = countPopups(treeModel.getContent().getSubHeaderBox());

    List<ModelValidationError> errors = new ArrayList<>();
    if (!enabled) {
      for (int index = 0; index < popups; index++) {
        errors.add(new ModelValidationError(model, POPUP_ELEMENT_ID,
            ValidationMessages.get("validation.treeWholeTreeExpansion.popupNotEnabled"), Severity.ERROR.name()));
      }
    }
    else if (popups == 0) {
      errors.add(new ModelValidationError(model, FLAG_ELEMENT_ID,
          ValidationMessages.get("validation.treeWholeTreeExpansion.popupMissing"), Severity.ERROR.name()));
    }
    else if (popups > 1) {
      errors.add(new ModelValidationError(model, FLAG_ELEMENT_ID,
          ValidationMessages.get("validation.treeWholeTreeExpansion.popupMultiple"), Severity.ERROR.name()));
    }
    return errors;
  }

  private static int countPopups(ElementBox subHeaderBox) {
    if (subHeaderBox == null) {
      return 0;
    }
    int count = 0;
    for (List<BoxElement> slot : List.of(subHeaderBox.getLeftSlot(), subHeaderBox.getRightSlot())) {
      count += (int) slot.stream().filter(ExpandAllPopupElement.class::isInstance).count();
    }
    return count;
  }
}
