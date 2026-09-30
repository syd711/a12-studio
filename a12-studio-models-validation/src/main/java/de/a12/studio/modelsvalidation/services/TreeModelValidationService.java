package de.a12.studio.modelsvalidation.services;

import de.a12.studio.models.treemodel.TreeModel;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.ValidationContext;
import de.a12.studio.modelsvalidation.ValidatorRunner;
import de.a12.studio.modelsvalidation.validators.AnnotationDuplicateValidator;
import de.a12.studio.modelsvalidation.validators.HeaderModelReferenceValidator;
import de.a12.studio.modelsvalidation.validators.HeaderRolesValidator;
import de.a12.studio.modelsvalidation.validators.LocaleCodeValidator;
import de.a12.studio.modelsvalidation.validators.MissingLocaleValidator;
import de.a12.studio.modelsvalidation.validators.ModelIdFilenameValidator;
import de.a12.studio.modelsvalidation.validators.ModelSuffixValidator;
import de.a12.studio.modelsvalidation.validators.ModelValidator;
import de.a12.studio.modelsvalidation.validators.NameConventionValidator;
import de.a12.studio.modelsvalidation.validators.UniqueModelIdValidator;
import de.a12.studio.modelsvalidation.validators.tree.TreeActionsValidator;
import de.a12.studio.modelsvalidation.validators.tree.TreeChildRelationshipValidator;
import de.a12.studio.modelsvalidation.validators.tree.TreeColumnFieldValidator;
import de.a12.studio.modelsvalidation.validators.tree.TreeColumnValidator;
import de.a12.studio.modelsvalidation.validators.tree.TreeColumnsNotEmptyValidator;
import de.a12.studio.modelsvalidation.validators.tree.TreeDocumentModelReferenceValidator;
import de.a12.studio.modelsvalidation.validators.tree.TreeExpansionStrategyValidator;
import de.a12.studio.modelsvalidation.validators.tree.TreeHierarchicalColumnRefValidator;
import de.a12.studio.modelsvalidation.validators.tree.TreeMultiSelectionElementValidator;
import de.a12.studio.modelsvalidation.validators.tree.TreeNodeStructureValidator;
import de.a12.studio.modelsvalidation.validators.tree.TreeNodesNotEmptyValidator;
import de.a12.studio.modelsvalidation.validators.tree.TreeRootRefValidator;
import de.a12.studio.modelsvalidation.validators.tree.TreeStylesValidator;
import de.a12.studio.modelsvalidation.validators.tree.TreeUniqueNodeValidator;
import de.a12.studio.modelsvalidation.validators.tree.TreeVirtualRootValidator;
import de.a12.studio.modelsvalidation.validators.tree.TreeVirtualScrollingValidator;
import de.a12.studio.modelsvalidation.validators.tree.TreeWholeTreeExpansionValidator;

import java.util.ArrayList;
import java.util.List;

/**
 * Validates a {@link TreeModel}: generic header checks plus the tree-specific rules ported from SME (its tree meta model and
 * {@code customConditions}; see "Tree Model: full gap review" in {@code docs/sme-reference-comparison.md}).
 */
public final class TreeModelValidationService {

  private final List<ModelValidator> validators = new ArrayList<>(List.of(
      new MissingLocaleValidator(),
      new LocaleCodeValidator(),
      new ModelIdFilenameValidator(),
      new ModelSuffixValidator(),
      new UniqueModelIdValidator(),
      new NameConventionValidator(),
      new HeaderModelReferenceValidator(),
      new TreeNodesNotEmptyValidator(),
      new TreeColumnsNotEmptyValidator(),
      new TreeUniqueNodeValidator(),
      new TreeDocumentModelReferenceValidator(),
      new TreeColumnFieldValidator(),
      new TreeHierarchicalColumnRefValidator(),
      new TreeExpansionStrategyValidator(),
      new TreeWholeTreeExpansionValidator(),
      new TreeRootRefValidator(),
      new TreeVirtualScrollingValidator(),
      new TreeMultiSelectionElementValidator(),
      new TreeStylesValidator(),
      new TreeColumnValidator(),
      new TreeNodeStructureValidator(),
      new TreeChildRelationshipValidator(),
      new TreeActionsValidator(),
      new TreeVirtualRootValidator(),
      new HeaderRolesValidator(),
      new AnnotationDuplicateValidator()));

  public void addValidator(ModelValidator validator) {
    validators.add(validator);
  }

  public void removeValidator(ModelValidator validator) {
    validators.remove(validator);
  }

  public List<ModelValidationError> validate(TreeModel model, ValidationContext context) {
    return ValidatorRunner.runAll(validators, model, context);
  }
}
