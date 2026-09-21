package de.a12.studio.modelsvalidation.validators.form;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.formmodel.FormModel;
import de.a12.studio.models.formmodel.FormModelWalker;
import de.a12.studio.models.formmodel.MultiColumnSection;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.Severity;
import de.a12.studio.modelsvalidation.ValidationContext;
import de.a12.studio.modelsvalidation.ValidationMessages;
import de.a12.studio.modelsvalidation.validators.ModelValidator;

import java.util.ArrayList;
import java.util.List;

/**
 * A Multi-Column Section must define its {@code lg} layout, the base breakpoint the {@code md}/{@code sm}
 * overrides derive from (SME's {@code mustHaveLayout} rule: {@code FieldNotFilled(layout/lg)}). What a filled
 * layout may contain is {@link FormLayoutColumnSumValidator}'s business.
 */
public final class FormMultiColumnSectionLayoutValidator implements ModelValidator {

  @Override
  public List<ModelValidationError> validate(A12Model<?> model, ValidationContext context) {
    if (!(model instanceof FormModel formModel) || formModel.getContent() == null) {
      return List.of();
    }
    List<ModelValidationError> errors = new ArrayList<>();
    for (MultiColumnSection section : FormModelWalker.find(formModel.getContent(), MultiColumnSection.class)) {
      if (section.getLayout() == null || section.getLayout().getLg() == null || section.getLayout().getLg().isBlank()) {
        errors.add(new ModelValidationError(model, section.getId(),
            ValidationMessages.get("validation.multiColumnSectionLayout.lgMissing",
                section.getName() != null && !section.getName().isBlank() ? section.getName() : section.getId()),
            Severity.ERROR.name()));
      }
    }
    return errors;
  }
}
