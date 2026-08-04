package de.a12.studio.modelsvalidation.validators.relationship;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.relationshipmodel.EntityCharacteristic;
import de.a12.studio.models.relationshipmodel.Multiplicity;
import de.a12.studio.models.relationshipmodel.RelationshipModel;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.Severity;
import de.a12.studio.modelsvalidation.ValidationContext;
import de.a12.studio.modelsvalidation.validators.ModelValidator;

import java.util.ArrayList;
import java.util.List;

/**
 * A Link Document Model and "Duplicates Allowed" are only valid for n:n relationships — both sides
 * unbounded (or with an upper limit above 1). SME shows warnings for these cases.
 */
public final class RelationshipLinkDocumentModelValidator implements ModelValidator {

  public static final String ELEMENT_ID = "content/linkDocumentModel";

  @Override
  public List<ModelValidationError> validate(A12Model<?> model, ValidationContext context) {
    if (!(model instanceof RelationshipModel relationshipModel)) {
      return List.of();
    }

    boolean manyToMany = relationshipModel.getContent().getEntityCharacteristics().stream()
        .allMatch(RelationshipLinkDocumentModelValidator::isToMany);
    if (manyToMany) {
      return List.of();
    }

    List<ModelValidationError> errors = new ArrayList<>();
    String linkDocumentModel = relationshipModel.getContent().getLinkDocumentModelValue();
    if (linkDocumentModel != null && !linkDocumentModel.isBlank()) {
      errors.add(new ModelValidationError(model, ELEMENT_ID,
          "validation.due_to_the_link_constraints_of_the_related_entitie",
          Severity.WARNING.name()));
    }
    if (Boolean.TRUE.equals(relationshipModel.getContent().getDuplicatesAllowed())) {
      errors.add(new ModelValidationError(model, ELEMENT_ID,
          "validation.due_to_the_link_constraints_of_the_related_entitie",
          Severity.WARNING.name()));
    }
    return errors;
  }

  private static boolean isToMany(EntityCharacteristic entity) {
    Multiplicity multiplicity = entity.getLinkConstraints() != null ? entity.getLinkConstraints().getMultiplicity() : null;
    if (multiplicity == null) {
      return false;
    }
    return Boolean.TRUE.equals(multiplicity.getUnbounded())
        || (multiplicity.getUpperLimit() != null && multiplicity.getUpperLimit() > 1);
  }
}
