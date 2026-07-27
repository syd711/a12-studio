package de.a12.studio.modelsvalidation.services;

import de.a12.studio.models.treemodel.TreeModel;
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
import de.a12.studio.modelsvalidation.validators.tree.TreeColumnFieldValidator;
import de.a12.studio.modelsvalidation.validators.tree.TreeColumnsNotEmptyValidator;
import de.a12.studio.modelsvalidation.validators.tree.TreeDocumentModelReferenceValidator;
import de.a12.studio.modelsvalidation.validators.tree.TreeHierarchicalColumnRefValidator;
import de.a12.studio.modelsvalidation.validators.tree.TreeNodesNotEmptyValidator;
import de.a12.studio.modelsvalidation.validators.tree.TreeUniqueNodeValidator;

import java.util.List;

/** Validates a {@link TreeModel}: generic header checks plus the tree-specific rules ported from SME. */
public final class TreeModelValidationService {

  private static final List<ModelValidator> VALIDATORS = List.of(
      new MissingLocaleValidator(),
      new LocaleCodeValidator(),
      new ModelIdFilenameValidator(),
      new UniqueModelIdValidator(),
      new NameConventionValidator(),
      new HeaderModelReferenceValidator(),
      new TreeNodesNotEmptyValidator(),
      new TreeColumnsNotEmptyValidator(),
      new TreeUniqueNodeValidator(),
      new TreeDocumentModelReferenceValidator(),
      new TreeColumnFieldValidator(),
      new TreeHierarchicalColumnRefValidator());

  public List<ModelValidationError> validate(TreeModel model, ValidationContext context) {
    return ValidatorRunner.runAll(VALIDATORS, model, context);
  }
}
