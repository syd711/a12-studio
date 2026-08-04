package de.a12.studio.modelsvalidation.validators.form;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.formmodel.FormModel;
import de.a12.studio.models.formmodel.MultiColumnSection;
import de.a12.studio.models.formmodel.Screen;
import de.a12.studio.models.formmodel.ScreenElement;
import de.a12.studio.models.formmodel.Section;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.Severity;
import de.a12.studio.modelsvalidation.ValidationContext;
import de.a12.studio.modelsvalidation.validators.ModelValidator;

import java.util.ArrayList;
import java.util.List;

/**
 * Multi-column sections: the "lg" layout column widths (e.g. "3-3-6") must sum to at most 12, and the
 * md/sm layouts must define the same number of columns as lg (SME: "Layout lg sum must not be &gt; 12",
 * "Number of columns for md must be the same as for lg").
 */
public final class FormLayoutColumnSumValidator implements ModelValidator {

  public static final String ELEMENT_ID = "content/screens/layout";

  @Override
  public List<ModelValidationError> validate(A12Model<?> model, ValidationContext context) {
    if (!(model instanceof FormModel formModel) || formModel.getContent() == null) {
      return List.of();
    }
    List<ModelValidationError> errors = new ArrayList<>();
    for (Screen screen : formModel.getContent().getScreens()) {
      visit(model, screen.getScreenElements(), errors);
    }
    return errors;
  }

  private void visit(A12Model<?> model, List<ScreenElement> elements, List<ModelValidationError> errors) {
    if (elements == null) {
      return;
    }
    for (ScreenElement element : elements) {
      if (element instanceof MultiColumnSection section) {
        checkLayout(model, section, errors);
        visit(model, section.getScreenElements(), errors);
      }
      else if (element instanceof Section section) {
        visit(model, section.getScreenElements(), errors);
      }
    }
  }

  private void checkLayout(A12Model<?> model, MultiColumnSection section, List<ModelValidationError> errors) {
    if (section.getLayout() == null || section.getLayout().getLg() == null) {
      return;
    }
    int[] lg = parse(section.getLayout().getLg());
    if (lg.length == 0) {
      return;
    }
    int sum = 0;
    for (int width : lg) {
      sum += width;
    }
    if (sum > 12) {
      errors.add(new ModelValidationError(model, ELEMENT_ID,
          "Layout lg sum must not be > 12 (multi-column section \"" + section.getName() + "\" has " + sum + ").",
          Severity.ERROR.name()));
    }
    checkSameColumnCount(model, section, "md", section.getLayout().getMd(), lg.length, errors);
    checkSameColumnCount(model, section, "sm", section.getLayout().getSm(), lg.length, errors);
  }

  private void checkSameColumnCount(A12Model<?> model, MultiColumnSection section, String breakpoint,
                                    String layout, int lgColumns, List<ModelValidationError> errors) {
    if (layout == null || layout.isBlank()) {
      return;
    }
    if (parse(layout).length != lgColumns) {
      errors.add(new ModelValidationError(model, ELEMENT_ID,
          "Number of columns for " + breakpoint + " must be the same as for lg (multi-column section \""
              + section.getName() + "\").", Severity.ERROR.name()));
    }
  }

  private static int[] parse(String layout) {
    try {
      String[] parts = layout.strip().split("-");
      int[] widths = new int[parts.length];
      for (int i = 0; i < parts.length; i++) {
        widths[i] = Integer.parseInt(parts[i].strip());
      }
      return widths;
    }
    catch (NumberFormatException e) {
      return new int[0];
    }
  }
}
