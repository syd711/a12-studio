package de.a12.studio.modelsvalidation.validators.query;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.querymodel.QueryModel;
import de.a12.studio.models.querymodel.QuerySort;
import de.a12.studio.models.relationshipmodel.EntityCharacteristic;
import de.a12.studio.models.relationshipmodel.RelationshipModel;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.Severity;
import de.a12.studio.modelsvalidation.ValidationContext;
import de.a12.studio.modelsvalidation.ValidationMessages;
import de.a12.studio.modelsvalidation.validators.ModelValidator;

import java.util.ArrayList;
import java.util.List;

/**
 * A sort entry's {@code relationshipModel}/{@code targetRole} (the hop before the sorted field, see {@link
 * QuerySort}) must reference a Relationship Model that exists and actually declares that role. Previously only
 * flagged as a UI styling hint in the sorting panel ("relationship could not be resolved"), not a real validation
 * error.
 */
public final class QueryRelationshipTraversalValidator implements ModelValidator {

  public static final String ELEMENT_ID = "content/sort/relationshipModel";

  @Override
  public List<ModelValidationError> validate(A12Model<?> model, ValidationContext context) {
    if (!(model instanceof QueryModel queryModel) || queryModel.getContent() == null
        || queryModel.getContent().getSort() == null) {
      return List.of();
    }

    List<ModelValidationError> errors = new ArrayList<>();
    for (QuerySort sort : queryModel.getContent().getSort()) {
      String relationshipModelId = sort.getRelationshipModel();
      if (relationshipModelId == null) {
        continue;
      }
      A12Model<?> referenced = context.findOtherModel(relationshipModelId);
      if (!(referenced instanceof RelationshipModel relationshipModel) || relationshipModel.getContent() == null) {
        errors.add(new ModelValidationError(model, ELEMENT_ID,
            ValidationMessages.get("validation.queryRelationshipTraversal.unknownRelationship", relationshipModelId),
            Severity.ERROR.name()));
        continue;
      }
      boolean roleExists = relationshipModel.getContent().getEntityCharacteristics().stream()
          .map(EntityCharacteristic::getRole)
          .anyMatch(role -> role != null && role.equals(sort.getTargetRole()));
      if (!roleExists) {
        errors.add(new ModelValidationError(model, ELEMENT_ID,
            ValidationMessages.get("validation.queryRelationshipTraversal.unknownRole", sort.getTargetRole(), relationshipModelId),
            Severity.ERROR.name()));
      }
    }
    return errors;
  }
}
