package de.a12.studio.modelsvalidation.validators.form;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.Annotation;
import de.a12.studio.models.composeddocumentmodel.ComposedDocumentModel;
import de.a12.studio.models.composeddocumentmodel.ComposedDocumentModelResolver;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.documentmodel.Element;
import de.a12.studio.models.documentmodel.GroupElement;
import de.a12.studio.models.formmodel.FormModel;
import de.a12.studio.models.relationshipmodel.EntityCharacteristic;
import de.a12.studio.models.relationshipmodel.Multiplicity;
import de.a12.studio.models.relationshipmodel.RelationshipModel;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.Severity;
import de.a12.studio.modelsvalidation.ValidationContext;
import de.a12.studio.modelsvalidation.ValidationMessages;
import de.a12.studio.modelsvalidation.validators.ModelValidator;
import de.a12.studio.modelsvalidation.validators.form.FormBindingElements.BindingHolder;

import java.util.ArrayList;
import java.util.List;

/**
 * Port of SME's {@code InvalidBindingRepeatRepetitionAndMultiplicity} custom condition: a binding whose
 * relationship has a relationship group in the bound {@link ComposedDocumentModel} with a repeatability of 1,
 * while the relationship's target role allows more than one linked document, cannot hold the links. The CDM's
 * relationship groups are the {@code Group}s carrying a {@code cdm.relationship} annotation; like SME, the group
 * is picked by relationship name only (the first one wins), since a binding does not name its group.
 */
public final class FormBindingRepeatMultiplicityValidator implements ModelValidator {

  // SME's MAX_GROUP_REPEATABILITY stands in for an unbounded upper limit; anything above 1 behaves the same here.
  private static final int UNBOUNDED = Integer.MAX_VALUE;

  @Override
  public List<ModelValidationError> validate(A12Model<?> model, ValidationContext context) {
    if (!(model instanceof FormModel formModel)
        || !(context.findOtherDocumentModel(FormBindingRepeatCdmRequiredValidator.dataBindingDocumentModelId(formModel)) instanceof ComposedDocumentModel cdm)
        || cdm.getContent() == null || cdm.getContent().getModelRoot() == null) {
      return List.of();
    }
    List<GroupElement> relationshipGroups = new ArrayList<>();
    for (GroupElement root : cdm.getContent().getModelRoot().getRootGroups()) {
      collectRelationshipGroups(root, relationshipGroups);
    }
    List<ModelValidationError> errors = new ArrayList<>();
    for (BindingHolder holder : FormBindingElements.findBindingContents(formModel)) {
      String relationshipName = holder.content() == null || holder.content().getDetails() == null
          ? null : holder.content().getDetails().getRelationshipName();
      if (relationshipName == null || relationshipName.isBlank()) {
        continue;
      }
      GroupElement group = relationshipGroups.stream()
          .filter(candidate -> relationshipName.equals(annotation(candidate, ComposedDocumentModelResolver.RELATIONSHIP_ANNOTATION)))
          .findFirst().orElse(null);
      if (group == null || !(context.findOtherModel(relationshipName) instanceof RelationshipModel relationshipModel)) {
        continue;
      }
      String targetRole = annotation(group, ComposedDocumentModelResolver.TARGET_ROLE_ANNOTATION);
      Integer upperLimit = upperLimit(relationshipModel, targetRole);
      Integer repeatability = group.getGroup() == null ? null : group.getGroup().getRepeatability();
      if (upperLimit != null && repeatability != null && upperLimit > 1 && repeatability == 1) {
        errors.add(new ModelValidationError(model, holder.elementId(),
            ValidationMessages.get("validation.formBindingRepeatMultiplicity.invalid", relationshipName, targetRole), Severity.ERROR.name()));
      }
    }
    return errors;
  }

  private static void collectRelationshipGroups(GroupElement group, List<GroupElement> into) {
    if (annotation(group, ComposedDocumentModelResolver.RELATIONSHIP_ANNOTATION) != null) {
      into.add(group);
    }
    if (group.getGroup() == null) {
      return;
    }
    for (Element child : group.getGroup().getElements()) {
      if (child instanceof GroupElement childGroup) {
        collectRelationshipGroups(childGroup, into);
      }
    }
  }

  private static String annotation(Element element, String name) {
    return element.getAnnotations().stream().filter(a -> name.equals(a.getName())).map(Annotation::getValue).findFirst().orElse(null);
  }

  private static Integer upperLimit(RelationshipModel relationshipModel, String targetRole) {
    if (relationshipModel.getContent() == null || targetRole == null) {
      return null;
    }
    for (EntityCharacteristic characteristic : relationshipModel.getContent().getEntityCharacteristics()) {
      if (targetRole.equals(characteristic.getRole()) && characteristic.getLinkConstraints() != null
          && characteristic.getLinkConstraints().getMultiplicity() != null) {
        Multiplicity multiplicity = characteristic.getLinkConstraints().getMultiplicity();
        return Boolean.TRUE.equals(multiplicity.getUnbounded()) ? UNBOUNDED : multiplicity.getUpperLimit();
      }
    }
    return null;
  }
}
