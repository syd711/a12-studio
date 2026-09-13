package de.a12.studio.modelsvalidation.validators.selection;

import de.a12.studio.models.selectionmodel.SelectionCategory;
import de.a12.studio.models.selectionmodel.SelectionModel;

import java.util.List;
import java.util.function.Function;

/**
 * The three identically-validated sections of a {@link SelectionModel}, each ported from SME's
 * {@code DomainSelectionSpecification.json} - Data/Validation/Computation share one meta-model group
 * definition there (same fields, same rule set, different labels), so every validator in this package
 * loops over this list once instead of duplicating the same check three times.
 */
final class SelectionCategories {

  record Entry(String jsonName, Function<SelectionModel, SelectionCategory> getter) {

    SelectionCategory get(SelectionModel model) {
      return getter.apply(model);
    }

    /** e.g. {@code "content/Data"} - the common prefix every path/Default element id for this category starts with. */
    String elementIdPrefix() {
      return "content/" + jsonName;
    }
  }

  static final List<Entry> ALL = List.of(
      new Entry("Data", model -> model.getContent().getData()),
      new Entry("Computation", model -> model.getContent().getComputation()),
      new Entry("Validation", model -> model.getContent().getValidation()));

  private SelectionCategories() {
  }
}
