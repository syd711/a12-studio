package de.a12.studio.modelsvalidation.validators.tree;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.treemodel.TreeChildRelationshipConfiguration;
import de.a12.studio.models.treemodel.TreeColumn;
import de.a12.studio.models.treemodel.TreeConfiguration;
import de.a12.studio.models.treemodel.TreeModel;
import de.a12.studio.models.treemodel.TreeNode;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.Severity;
import de.a12.studio.modelsvalidation.ValidationContext;
import de.a12.studio.modelsvalidation.ValidationMessages;
import de.a12.studio.modelsvalidation.validators.ModelValidator;

import java.util.ArrayList;
import java.util.List;

/**
 * The Root must be one of the child relationship configurations of a node type (SME: {@code rootRefMustHaveValidReference},
 * "Invalid Reference"), and the Screen Reader Column one of the tree's columns (SME has no rule for that reference; a
 * dangling one would leave the engine without the column it announces rows by).
 */
public final class TreeRootRefValidator implements ModelValidator {

  public static final String ROOT_ELEMENT_ID = "content/configuration/rootRef";
  public static final String SCREEN_READER_ELEMENT_ID = "content/configuration/screenReaderColumnRef";

  @Override
  public List<ModelValidationError> validate(A12Model<?> model, ValidationContext context) {
    if (!(model instanceof TreeModel treeModel) || treeModel.getContent().getConfiguration() == null) {
      return List.of();
    }
    TreeConfiguration configuration = treeModel.getContent().getConfiguration();
    List<ModelValidationError> errors = new ArrayList<>();

    String rootRef = configuration.getRootRef();
    if (rootRef != null && !rootRef.isBlank() && !isChildRelationshipConfiguration(treeModel, rootRef)) {
      errors.add(new ModelValidationError(model, ROOT_ELEMENT_ID,
          ValidationMessages.get("validation.treeRootRef.notFound", rootRef), Severity.ERROR.name()));
    }

    String screenReaderColumnRef = configuration.getScreenReaderColumnRef();
    if (screenReaderColumnRef != null && !screenReaderColumnRef.isBlank()
        && treeModel.getContent().getColumns().stream().map(TreeColumn::getId).noneMatch(screenReaderColumnRef::equals)) {
      errors.add(new ModelValidationError(model, SCREEN_READER_ELEMENT_ID,
          ValidationMessages.get("validation.treeRootRef.screenReaderColumnNotFound", screenReaderColumnRef), Severity.ERROR.name()));
    }
    return errors;
  }

  private static boolean isChildRelationshipConfiguration(TreeModel model, String id) {
    for (TreeNode node : model.getContent().getNodes()) {
      for (TreeChildRelationshipConfiguration configuration : node.getChildRelationshipConfigurations()) {
        if (id.equals(configuration.getId())) {
          return true;
        }
      }
    }
    return false;
  }
}
