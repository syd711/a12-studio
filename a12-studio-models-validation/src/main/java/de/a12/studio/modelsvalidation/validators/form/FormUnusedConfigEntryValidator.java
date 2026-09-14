package de.a12.studio.modelsvalidation.validators.form;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.formmodel.FieldConfigEntry;
import de.a12.studio.models.formmodel.FormModel;
import de.a12.studio.models.formmodel.GroupConfigEntry;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.Severity;
import de.a12.studio.modelsvalidation.ValidationContext;
import de.a12.studio.modelsvalidation.ValidationMessages;
import de.a12.studio.modelsvalidation.validators.ElementIndex;
import de.a12.studio.modelsvalidation.validators.ModelValidator;

import java.util.ArrayList;
import java.util.List;

/**
 * Flags a {@link FieldConfigEntry}/{@link GroupConfigEntry} whose reference still resolves against the linked
 * Document Model (a dangling one is instead reported by {@link FormFieldReferenceValidator}/{@link
 * FormGroupReferenceValidator}) but is no longer referenced by any Control/Repeat in the Screens tree - dead
 * configuration data left behind once the node that used to bind it was removed from the tree. Reported as a
 * {@link Severity#WARNING} (nothing is actually broken) so it still surfaces on the project tree node; the Form
 * Model editor's controller auto-removes these entries (together with any dangling ones) whenever the editor is
 * opened or its tab is reselected.
 */
public final class FormUnusedConfigEntryValidator implements ModelValidator {

  public static final String FIELD_ELEMENT_ID = "content/fieldConfiguration";
  public static final String GROUP_ELEMENT_ID = "content/groupConfiguration";

  @Override
  public List<ModelValidationError> validate(A12Model<?> model, ValidationContext context) {
    if (!(model instanceof FormModel formModel) || formModel.getContent() == null) {
      return List.of();
    }

    List<ElementIndex> indexes = FormDocumentModelIndexes.referencedDocumentModelIndexes(model, context);
    if (indexes.isEmpty()) {
      // Without a resolvable document model there is nothing to check against.
      return List.of();
    }

    List<ModelValidationError> errors = new ArrayList<>();
    if (formModel.getContent().getFieldConfiguration() != null) {
      for (FieldConfigEntry entry : formModel.getContent().getFieldConfiguration().getField()) {
        String elementRef = entry.getElementRef();
        if (elementRef == null || elementRef.isBlank()
            || indexes.stream().noneMatch(index -> index.isResolvable(elementRef))
            || FormReferences.isFieldReferenced(formModel, elementRef)) {
          continue;
        }
        errors.add(new ModelValidationError(model, FIELD_ELEMENT_ID,
            ValidationMessages.get("validation.formUnusedConfigEntry.field", elementRef), Severity.WARNING.name()));
      }
    }
    if (formModel.getContent().getGroupConfiguration() != null) {
      for (GroupConfigEntry entry : formModel.getContent().getGroupConfiguration().getGroup()) {
        String groupRef = entry.getGroupRef();
        if (groupRef == null || groupRef.isBlank()
            || indexes.stream().noneMatch(index -> index.isResolvable(groupRef))
            || FormReferences.isGroupReferenced(formModel, groupRef)) {
          continue;
        }
        errors.add(new ModelValidationError(model, GROUP_ELEMENT_ID,
            ValidationMessages.get("validation.formUnusedConfigEntry.group", groupRef), Severity.WARNING.name()));
      }
    }
    return errors;
  }
}
