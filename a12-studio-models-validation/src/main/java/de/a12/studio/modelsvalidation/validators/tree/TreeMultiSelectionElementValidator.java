package de.a12.studio.modelsvalidation.validators.tree;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.overviewmodel.BoxElement;
import de.a12.studio.models.overviewmodel.BoxElementType;
import de.a12.studio.models.overviewmodel.ElementBox;
import de.a12.studio.models.treemodel.TreeModel;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.Severity;
import de.a12.studio.modelsvalidation.ValidationContext;
import de.a12.studio.modelsvalidation.ValidationMessages;
import de.a12.studio.modelsvalidation.validators.ModelValidator;

import java.util.List;

/**
 * Multi-Selection and its Subheader element belong together (SME's four rules on the Tree meta model): a Multi-Selection
 * element is not allowed while multi-selection is off, and while it is on the Subheader needs exactly one.
 */
public final class TreeMultiSelectionElementValidator implements ModelValidator {

  public static final String SUBHEADER_ELEMENT_ID = "content/subHeaderBox";
  public static final String MULTI_SELECTION_ELEMENT_ID = "content/configuration/multiSelection";

  @Override
  public List<ModelValidationError> validate(A12Model<?> model, ValidationContext context) {
    if (!(model instanceof TreeModel treeModel)) {
      return List.of();
    }
    boolean enabled = treeModel.getContent().getConfiguration() != null
        && treeModel.getContent().getConfiguration().getMultiSelection() != null;
    long count = count(treeModel.getContent().getSubHeaderBox());

    if (!enabled) {
      return java.util.stream.LongStream.range(0, count)
          .mapToObj(index -> new ModelValidationError(model, SUBHEADER_ELEMENT_ID,
              ValidationMessages.get("validation.treeMultiSelectionElement.notEnabled"), Severity.ERROR.name()))
          .toList();
    }
    if (count == 0) {
      return List.of(new ModelValidationError(model, MULTI_SELECTION_ELEMENT_ID,
          ValidationMessages.get("validation.treeMultiSelectionElement.missing"), Severity.ERROR.name()));
    }
    if (count > 1) {
      return List.of(new ModelValidationError(model, MULTI_SELECTION_ELEMENT_ID,
          ValidationMessages.get("validation.treeMultiSelectionElement.multiple"), Severity.ERROR.name()));
    }
    return List.of();
  }

  private static long count(ElementBox subHeaderBox) {
    if (subHeaderBox == null) {
      return 0;
    }
    long count = 0;
    for (List<BoxElement> slot : List.of(subHeaderBox.getLeftSlot(), subHeaderBox.getRightSlot())) {
      count += slot.stream().filter(element -> element.getType() == BoxElementType.MULTI_SELECTION).count();
    }
    return count;
  }
}
