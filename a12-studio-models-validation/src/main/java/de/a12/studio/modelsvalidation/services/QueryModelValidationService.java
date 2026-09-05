package de.a12.studio.modelsvalidation.services;

import de.a12.studio.models.querymodel.QueryModel;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.ValidationContext;
import de.a12.studio.modelsvalidation.ValidatorRunner;
import de.a12.studio.modelsvalidation.validators.HeaderModelReferenceValidator;
import de.a12.studio.modelsvalidation.validators.LocaleCodeValidator;
import de.a12.studio.modelsvalidation.validators.MissingLocaleValidator;
import de.a12.studio.modelsvalidation.validators.ModelIdFilenameValidator;
import de.a12.studio.modelsvalidation.validators.ModelSuffixValidator;
import de.a12.studio.modelsvalidation.validators.ModelValidator;
import de.a12.studio.modelsvalidation.validators.NameConventionValidator;
import de.a12.studio.modelsvalidation.validators.UniqueModelIdValidator;
import de.a12.studio.modelsvalidation.validators.query.QueryFieldReferenceValidator;
import de.a12.studio.modelsvalidation.validators.query.QueryFilterDefinitionSyntaxValidator;
import de.a12.studio.modelsvalidation.validators.query.QueryPagingBoundsValidator;
import de.a12.studio.modelsvalidation.validators.query.QueryRelationshipTraversalValidator;
import de.a12.studio.modelsvalidation.validators.query.QuerySortFieldReferenceValidator;
import de.a12.studio.modelsvalidation.validators.query.QueryTargetDocumentModelRequiredValidator;

import java.util.ArrayList;
import java.util.List;

/** Validates a {@link QueryModel}: generic header checks plus the query-specific rules described in
 * docs/sme-reference-comparison.md "Query Model" section - previously there were none at all. */
public final class QueryModelValidationService {

  private final List<ModelValidator> validators = new ArrayList<>(List.of(
      new MissingLocaleValidator(),
      new LocaleCodeValidator(),
      new ModelIdFilenameValidator(),
      new ModelSuffixValidator(),
      new UniqueModelIdValidator(),
      new NameConventionValidator(),
      new HeaderModelReferenceValidator(),
      new QueryTargetDocumentModelRequiredValidator(),
      new QueryFieldReferenceValidator(),
      new QuerySortFieldReferenceValidator(),
      new QueryRelationshipTraversalValidator(),
      new QueryPagingBoundsValidator(),
      new QueryFilterDefinitionSyntaxValidator()));

  public void addValidator(ModelValidator validator) {
    validators.add(validator);
  }

  public void removeValidator(ModelValidator validator) {
    validators.remove(validator);
  }

  public List<ModelValidationError> validate(QueryModel model, ValidationContext context) {
    return ValidatorRunner.runAll(validators, model, context);
  }
}
