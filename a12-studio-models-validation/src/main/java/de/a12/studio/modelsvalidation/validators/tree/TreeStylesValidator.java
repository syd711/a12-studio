package de.a12.studio.modelsvalidation.validators.tree;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.overviewmodel.ColumnStyles;
import de.a12.studio.models.treemodel.TreeColumn;
import de.a12.studio.models.treemodel.TreeModel;
import de.a12.studio.models.treemodel.TreeNode;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.Severity;
import de.a12.studio.modelsvalidation.ValidationContext;
import de.a12.studio.modelsvalidation.ValidationMessages;
import de.a12.studio.modelsvalidation.validators.ModelValidator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The logical style names of a tree ({@code content.styles}): a name is required and only listed once (SME:
 * {@code styleNamesNotUnique}), and every style a node type, column, action or button uses must be one of them (SME:
 * "Invalid Reference") and is used only once per list ({@code stylesNotUnique}, {@code headerStyleNamesNotUnique}, ...).
 */
public final class TreeStylesValidator implements ModelValidator {

  public static final String ELEMENT_ID = "content/styles";

  @Override
  public List<ModelValidationError> validate(A12Model<?> model, ValidationContext context) {
    if (!(model instanceof TreeModel treeModel)) {
      return List.of();
    }
    List<ModelValidationError> errors = new ArrayList<>();
    Set<String> defined = new HashSet<>();
    Set<String> seen = new HashSet<>();
    for (String style : treeModel.getContent().getStyles()) {
      if (style == null || style.isBlank()) {
        errors.add(error(model, "validation.treeStyles.emptyValue"));
        continue;
      }
      defined.add(style);
      if (!seen.add(style)) {
        errors.add(error(model, "validation.treeStyles.duplicate", style));
      }
    }

    for (TreeNode node : treeModel.getContent().getNodes()) {
      check(model, errors, defined, node.getStyles(), ValidationMessages.get("validation.treeOwner.node", TreeValidationSupport.name(node)));
    }
    for (TreeColumn column : treeModel.getContent().getColumns()) {
      ColumnStyles styles = column.getStyles();
      if (styles != null) {
        String name = column.getName() != null ? column.getName() : column.getId();
        check(model, errors, defined, styles.getHeader(), ValidationMessages.get("validation.treeOwner.columnHeader", name));
        check(model, errors, defined, styles.getContent(), ValidationMessages.get("validation.treeOwner.columnContent", name));
      }
    }
    for (TreeValidationSupport.Site site : TreeValidationSupport.sites(treeModel)) {
      check(model, errors, defined, site.button().getStyles(), site.owner());
    }
    return errors;
  }

  private static void check(A12Model<?> model, List<ModelValidationError> errors, Set<String> defined, List<String> styles, String owner) {
    Set<String> seen = new HashSet<>();
    for (String style : styles) {
      if (style == null || style.isBlank()) {
        continue;
      }
      if (!defined.contains(style)) {
        errors.add(error(model, "validation.treeStyles.notDefined", style, owner));
      }
      if (!seen.add(style)) {
        errors.add(error(model, "validation.treeStyles.usedTwice", style, owner));
      }
    }
  }

  private static ModelValidationError error(A12Model<?> model, String key, Object... arguments) {
    return new ModelValidationError(model, ELEMENT_ID, ValidationMessages.get(key, arguments), Severity.ERROR.name());
  }
}
