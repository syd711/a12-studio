package de.a12.studio.modelsvalidation.validators.overview;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.overviewmodel.BoxElementType;
import de.a12.studio.models.overviewmodel.ElementBox;
import de.a12.studio.models.overviewmodel.OverviewModel;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.Severity;
import de.a12.studio.modelsvalidation.ValidationContext;
import de.a12.studio.modelsvalidation.ValidationMessages;
import de.a12.studio.modelsvalidation.validators.ModelValidator;

import java.util.List;
import java.util.stream.Stream;

/**
 * When Show Full Text Search is enabled, the Sub header must contain exactly one Search element (SME rules
 * "noSearchIsAdd" and "onlyOneSearchIsAllowed"), mirroring {@link OverviewMultiSelectionElementValidator}.
 */
public final class OverviewSearchElementValidator implements ModelValidator {

  public static final String ELEMENT_ID = "content/configuration/showFullTextSearch";

  @Override
  public List<ModelValidationError> validate(A12Model<?> model, ValidationContext context) {
    if (!(model instanceof OverviewModel overviewModel) || overviewModel.getContent().getConfiguration() == null) {
      return List.of();
    }
    if (!Boolean.TRUE.equals(overviewModel.getContent().getConfiguration().getShowFullTextSearch())) {
      return List.of();
    }

    long count = countSearchElements(overviewModel.getContent().getSubHeaderBox());
    if (count == 0) {
      return List.of(new ModelValidationError(model, ELEMENT_ID,
          ValidationMessages.get("validation.overviewSearchElement.missing"), Severity.ERROR.name()));
    }
    if (count > 1) {
      return List.of(new ModelValidationError(model, ELEMENT_ID,
          ValidationMessages.get("validation.overviewSearchElement.duplicate"), Severity.ERROR.name()));
    }
    return List.of();
  }

  private static long countSearchElements(ElementBox subHeaderBox) {
    if (subHeaderBox == null) {
      return 0;
    }
    return Stream.concat(subHeaderBox.getLeftSlot().stream(), subHeaderBox.getRightSlot().stream())
        .filter(element -> element.getType() == BoxElementType.SEARCH)
        .count();
  }
}
