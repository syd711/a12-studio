package de.a12.studio.modelsvalidation.validators.tree;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.treemodel.TreeColumn;
import de.a12.studio.models.treemodel.TreeModel;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.Severity;
import de.a12.studio.modelsvalidation.ValidationContext;
import de.a12.studio.modelsvalidation.ValidationMessages;
import de.a12.studio.modelsvalidation.validators.ModelValidator;

import java.util.List;

/** The hierarchical column reference must point to one of the tree's columns. */
public final class TreeHierarchicalColumnRefValidator implements ModelValidator {

  public static final String ELEMENT_ID = "content/configuration/hierarchicalColumnRef";

  @Override
  public List<ModelValidationError> validate(A12Model<?> model, ValidationContext context) {
    if (!(model instanceof TreeModel treeModel) || treeModel.getContent().getConfiguration() == null) {
      return List.of();
    }
    String hierarchicalColumnRef = treeModel.getContent().getConfiguration().getHierarchicalColumnRef();
    if (hierarchicalColumnRef == null || hierarchicalColumnRef.isBlank()) {
      return List.of();
    }
    boolean exists = treeModel.getContent().getColumns().stream()
        .map(TreeColumn::getId)
        .anyMatch(hierarchicalColumnRef::equals);
    if (exists) {
      return List.of();
    }
    return List.of(new ModelValidationError(model, ELEMENT_ID,
        ValidationMessages.get("validation.treeHierarchicalColumnRef.missing", hierarchicalColumnRef), Severity.ERROR.name()));
  }
}
