package de.a12.studio.modelsvalidation.validators.selection;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.selectionmodel.PathSpecification;
import de.a12.studio.models.selectionmodel.SelectionModel;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.Severity;
import de.a12.studio.modelsvalidation.ValidationContext;
import de.a12.studio.modelsvalidation.ValidationMessages;
import de.a12.studio.modelsvalidation.validators.ModelValidator;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Ports SME's {@code ONLY_WILDCARD} and {@code INVALID_PATTERN} rules ({@code
 * DomainSelectionSpecification.json}, one copy per {@code Selected}/{@code Unselected} list in each of
 * Data/Computation/Validation): a path specification must be a real path (not a bare {@code "/*​/"}) and
 * must match the kernel's path-specification grammar - a sequence of name segments (optionally ending in an
 * unqualified or prefix/suffix-wildcarded final segment), or a bare {@code "*​/"} segment.
 */
public final class SelectionPathPatternValidator implements ModelValidator {

  private static final String ONLY_WILDCARD = "/*/";

  // Ported verbatim from the kernel's INVALID_PATTERN rule condition (confirmed identical in both SME's
  // DomainSelectionSpecification.json and the kernel-shipped MM_SelectionModel_2 domain model bundled in
  // this repo's Output/A12-Studio/wcf-cli jars).
  private static final Pattern VALID_PATTERN = Pattern.compile(
      "/(([a-zA-Z0-9_üöäÜÖÄß]{1,60}/)+|\\*/)((([a-zA-Z0-9_])+[*]{0,1}|\\*([a-zA-Z0-9_])+))?");

  @Override
  public List<ModelValidationError> validate(A12Model<?> model, ValidationContext context) {
    if (!(model instanceof SelectionModel selectionModel)) {
      return List.of();
    }
    List<ModelValidationError> errors = new ArrayList<>();
    for (SelectionCategories.Entry category : SelectionCategories.ALL) {
      checkList(model, category, "Selected", category.get(selectionModel).getSelected(), errors);
      checkList(model, category, "Unselected", category.get(selectionModel).getUnselected(), errors);
    }
    return errors;
  }

  private static void checkList(A12Model<?> model, SelectionCategories.Entry category, String listName,
      List<PathSpecification> paths, List<ModelValidationError> errors) {
    if (paths == null) {
      return;
    }
    for (int index = 0; index < paths.size(); index++) {
      String path = paths.get(index).getPath();
      if (path == null || path.isBlank()) {
        continue;
      }
      String elementId = category.elementIdPrefix() + "/" + listName + "/" + index;
      if (path.equals(ONLY_WILDCARD)) {
        errors.add(new ModelValidationError(model, elementId,
            ValidationMessages.get("validation.selectionOnlyWildcard"), Severity.ERROR.name()));
      }
      else if (!VALID_PATTERN.matcher(path).matches()) {
        errors.add(new ModelValidationError(model, elementId,
            ValidationMessages.get("validation.selectionInvalidPattern"), Severity.ERROR.name()));
      }
    }
  }
}
