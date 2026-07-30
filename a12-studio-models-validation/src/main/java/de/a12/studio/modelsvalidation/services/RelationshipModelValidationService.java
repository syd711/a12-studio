package de.a12.studio.modelsvalidation.services;

import de.a12.studio.models.relationshipmodel.RelationshipModel;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.ValidationContext;
import de.a12.studio.modelsvalidation.ValidatorRunner;
import de.a12.studio.modelsvalidation.validators.HeaderModelReferenceValidator;
import de.a12.studio.modelsvalidation.validators.LocaleCodeValidator;
import de.a12.studio.modelsvalidation.validators.MissingLocaleValidator;
import de.a12.studio.modelsvalidation.validators.ModelIdFilenameValidator;
import de.a12.studio.modelsvalidation.validators.ModelValidator;
import de.a12.studio.modelsvalidation.validators.NameConventionValidator;
import de.a12.studio.modelsvalidation.validators.UniqueModelIdValidator;
import de.a12.studio.modelsvalidation.validators.relationship.RelationshipDocumentModelReferenceValidator;
import de.a12.studio.modelsvalidation.validators.relationship.RelationshipEntityCountValidator;
import de.a12.studio.modelsvalidation.validators.relationship.RelationshipGeneratedDmNameLengthValidator;
import de.a12.studio.modelsvalidation.validators.relationship.RelationshipLinkDocumentModelValidator;
import de.a12.studio.modelsvalidation.validators.relationship.RelationshipUniqueRolesValidator;
import de.a12.studio.modelsvalidation.validators.relationship.RelationshipUpperLimitValidator;

import java.util.ArrayList;
import java.util.List;

/** Validates a {@link RelationshipModel}: generic header checks plus the relationship-specific rules ported from SME. */
public final class RelationshipModelValidationService {

  private final List<ModelValidator> validators = new ArrayList<>(List.of(
      new MissingLocaleValidator(),
      new LocaleCodeValidator(),
      new ModelIdFilenameValidator(),
      new UniqueModelIdValidator(),
      new NameConventionValidator(),
      new HeaderModelReferenceValidator(),
      new RelationshipEntityCountValidator(),
      new RelationshipUniqueRolesValidator(),
      new RelationshipUpperLimitValidator(),
      new RelationshipDocumentModelReferenceValidator(),
      new RelationshipLinkDocumentModelValidator(),
      new RelationshipGeneratedDmNameLengthValidator()));

  public void addValidator(ModelValidator validator) {
    validators.add(validator);
  }

  public void removeValidator(ModelValidator validator) {
    validators.remove(validator);
  }

  public List<ModelValidationError> validate(RelationshipModel model, ValidationContext context) {
    return ValidatorRunner.runAll(validators, model, context);
  }
}
