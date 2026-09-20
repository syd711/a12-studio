package de.a12.studio.modelsvalidation.validators.form;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.documentmodel.Element;
import de.a12.studio.models.documentmodel.FieldElement;
import de.a12.studio.models.documentmodel.GroupConfig;
import de.a12.studio.models.documentmodel.GroupElement;
import de.a12.studio.models.formmodel.AbstractRepeat;
import de.a12.studio.models.formmodel.Control;
import de.a12.studio.models.formmodel.FieldBasedRepeatOverviewColumn;
import de.a12.studio.models.formmodel.FormModel;
import de.a12.studio.models.formmodel.FormModelWalker;
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
 * A reference from the Form Model that still <em>resolves</em> in the Document Model but to the wrong kind of
 * element, i.e. the Document Model changed underneath it (the missing-reference case is
 * {@link FormFieldReferenceValidator}'s and {@link FormGroupReferenceValidator}'s):
 * <ul>
 *   <li>a Control or a field-based overview column ({@code elementRef}) that now points at a group - other than an
 *       attachment or multi-select group, which a Control binds as a whole (upload / multi-select control);</li>
 *   <li>a Repeat ({@code groupRef}) that now points at a field, or at a group that is no longer repeatable -
 *       a repeat needs a group with more than one repetition to have rows.</li>
 * </ul>
 */
public final class FormReferenceTypeDriftValidator implements ModelValidator {

  @Override
  public List<ModelValidationError> validate(A12Model<?> model, ValidationContext context) {
    if (!(model instanceof FormModel formModel) || formModel.getContent() == null) {
      return List.of();
    }
    List<ElementIndex> indexes = FormDocumentModelIndexes.referencedDocumentModelIndexes(model, context);
    if (indexes.isEmpty()) {
      return List.of();
    }
    List<ModelValidationError> errors = new ArrayList<>();
    for (Control control : FormModelWalker.find(formModel.getContent(), Control.class)) {
      checkFieldReference(formModel, indexes, control.getId(), control.getElementRef(), errors);
    }
    for (FieldBasedRepeatOverviewColumn column : FormModelWalker.find(formModel.getContent(), FieldBasedRepeatOverviewColumn.class)) {
      checkFieldReference(formModel, indexes, column.getId(), column.getElementRef(), errors);
    }
    for (AbstractRepeat repeat : FormModelWalker.find(formModel.getContent(), AbstractRepeat.class)) {
      checkGroupReference(formModel, indexes, repeat, errors);
    }
    return errors;
  }

  private static void checkFieldReference(FormModel model, List<ElementIndex> indexes, String nodeId, String elementRef,
      List<ModelValidationError> errors) {
    Optional<Element> element = resolve(indexes, elementRef);
    if (element.isPresent() && !isBindableAsField(element.get())) {
      errors.add(new ModelValidationError(model, nodeId,
          ValidationMessages.get("validation.formReferenceType.fieldExpected", nodeId, elementRef), Severity.ERROR.name()));
    }
  }

  /** A field, or a group a Control binds as one: an attachment (file upload) or a multi-select group. */
  private static boolean isBindableAsField(Element element) {
    if (element instanceof FieldElement) {
      return true;
    }
    if (element instanceof GroupElement group && group.getGroup() != null) {
      String usageType = group.getGroup().getUsageType();
      return GroupConfig.USAGE_TYPE_ATTACHMENT.equals(usageType) || GroupConfig.USAGE_TYPE_MULTI_SELECT.equals(usageType);
    }
    return false;
  }

  private static void checkGroupReference(FormModel model, List<ElementIndex> indexes, AbstractRepeat repeat,
      List<ModelValidationError> errors) {
    Optional<Element> element = resolve(indexes, repeat.getGroupRef());
    if (element.isEmpty()) {
      return;
    }
    if (!(element.get() instanceof GroupElement group)) {
      errors.add(new ModelValidationError(model, repeat.getId(),
          ValidationMessages.get("validation.formReferenceType.groupExpected", repeat.getId(), repeat.getGroupRef()),
          Severity.ERROR.name()));
    }
    else if (group.getGroup() == null || group.getGroup().getRepeatability() == null || group.getGroup().getRepeatability() <= 1) {
      errors.add(new ModelValidationError(model, repeat.getId(),
          ValidationMessages.get("validation.formReferenceType.groupNotRepeatable", repeat.getId(), repeat.getGroupRef()),
          Severity.ERROR.name()));
    }
  }

  private static Optional<Element> resolve(List<ElementIndex> indexes, String elementId) {
    if (elementId == null || elementId.isBlank()) {
      return Optional.empty();
    }
    for (ElementIndex index : indexes) {
      Optional<Element> element = index.resolveElement(elementId);
      if (element.isPresent()) {
        return element;
      }
    }
    return Optional.empty();
  }
}
