package de.a12.studio.modelsvalidation.validators.query;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.querymodel.QueryLink;
import de.a12.studio.models.querymodel.QueryModel;
import de.a12.studio.models.relationshipmodel.EntityCharacteristic;
import de.a12.studio.models.relationshipmodel.RelationshipModel;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.Severity;
import de.a12.studio.modelsvalidation.ValidationContext;
import de.a12.studio.modelsvalidation.ValidationMessages;
import de.a12.studio.modelsvalidation.validators.ElementIndex;
import de.a12.studio.modelsvalidation.validators.ModelValidator;

import java.util.ArrayList;
import java.util.List;

/**
 * Every relationship-traversal hop in {@code content.links} (recursively, including nested hops - see {@link
 * QueryLink#getLinks()}) must reference a Relationship Model that exists and actually declares {@code
 * targetRole} (same check as {@link QueryRelationshipTraversalValidator} does for a sort entry's traversal), and
 * every path in that hop's own {@code fields} must resolve against the Document Model the role resolves to. A
 * hop whose relationship/role doesn't resolve has no Document Model to check its own {@code fields} against, but
 * its nested hops are still validated independently (a broken hop doesn't hide errors further down the graph).
 */
public final class QueryLinkValidator implements ModelValidator {

  public static final String ELEMENT_ID = "content/links";

  @Override
  public List<ModelValidationError> validate(A12Model<?> model, ValidationContext context) {
    if (!(model instanceof QueryModel queryModel) || queryModel.getContent() == null) {
      return List.of();
    }
    List<ModelValidationError> errors = new ArrayList<>();
    for (QueryLink link : queryModel.getContent().getLinks()) {
      validateLink(model, link, context, errors);
    }
    return errors;
  }

  private void validateLink(A12Model<?> model, QueryLink link, ValidationContext context, List<ModelValidationError> errors) {
    A12Model<?> referenced = context.findOtherModel(link.getRelationshipModel());
    if (!(referenced instanceof RelationshipModel relationshipModel) || relationshipModel.getContent() == null) {
      errors.add(new ModelValidationError(model, ELEMENT_ID,
          ValidationMessages.get("validation.queryRelationshipTraversal.unknownRelationship", link.getRelationshipModel()),
          Severity.ERROR.name()));
      validateNestedLinks(model, link, context, errors);
      return;
    }

    String documentModelId = relationshipModel.getContent().getEntityCharacteristics().stream()
        .filter(characteristic -> link.getTargetRole() != null && link.getTargetRole().equals(characteristic.getRole()))
        .map(EntityCharacteristic::getDocumentModel)
        .findFirst()
        .orElse(null);
    if (documentModelId == null) {
      errors.add(new ModelValidationError(model, ELEMENT_ID,
          ValidationMessages.get("validation.queryRelationshipTraversal.unknownRole", link.getTargetRole(), link.getRelationshipModel()),
          Severity.ERROR.name()));
      validateNestedLinks(model, link, context, errors);
      return;
    }

    DocumentModel linkedDocumentModel = context.findOtherDocumentModel(documentModelId);
    if (linkedDocumentModel != null && linkedDocumentModel.getContent() != null
        && linkedDocumentModel.getContent().getModelRoot() != null) {
      ElementIndex index = new ElementIndex(linkedDocumentModel, context.otherDocumentModels());
      for (String path : link.getFields()) {
        if (QueryElementResolution.resolveByPath(index, path) == null) {
          errors.add(new ModelValidationError(model, ELEMENT_ID,
              ValidationMessages.get("validation.common.fieldReferenceMissing", path), Severity.ERROR.name()));
        }
      }
    }
    validateNestedLinks(model, link, context, errors);
  }

  private void validateNestedLinks(A12Model<?> model, QueryLink link, ValidationContext context, List<ModelValidationError> errors) {
    for (QueryLink nested : link.getLinks()) {
      validateLink(model, nested, context, errors);
    }
  }
}
