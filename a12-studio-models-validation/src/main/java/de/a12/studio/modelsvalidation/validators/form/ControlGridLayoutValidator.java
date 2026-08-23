package de.a12.studio.modelsvalidation.validators.form;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.formmodel.Cell;
import de.a12.studio.models.formmodel.ColumnLayout;
import de.a12.studio.models.formmodel.Control;
import de.a12.studio.models.formmodel.ControlGrid;
import de.a12.studio.models.formmodel.DetachedRepeat;
import de.a12.studio.models.formmodel.EmbeddedRepeat;
import de.a12.studio.models.formmodel.FormModel;
import de.a12.studio.models.formmodel.GridSpan;
import de.a12.studio.models.formmodel.MultiColumnSection;
import de.a12.studio.models.formmodel.Row;
import de.a12.studio.models.formmodel.Screen;
import de.a12.studio.models.formmodel.ScreenElement;
import de.a12.studio.models.formmodel.Section;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.Severity;
import de.a12.studio.modelsvalidation.ValidationContext;
import de.a12.studio.modelsvalidation.ValidationMessages;
import de.a12.studio.modelsvalidation.validators.ModelValidator;

import java.util.ArrayList;
import java.util.List;

/**
 * Every {@link ControlGrid} row's cells must fit the column count its {@code layout} declares (SME:
 * "Form model field [x] contains a wrong number of columns for layout lg. The expected number of columns
 * is N but there are M defined columns.", "The element [x] exceeds for layout lg with offset [o] the
 * defined maximum index [N] for the control grid [x]."). Ported from SME's closed-source kernel
 * (com.mgmtp.a12.formengine:formengine-model, not available as a local dependency here) by
 * reverse-engineering against a real fixture (Invoice_FM.json's "BillingAddressControls" grid) rather than
 * reading the original source.
 */
public final class ControlGridLayoutValidator implements ModelValidator {

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
      if (element instanceof ControlGrid grid) {
        checkGrid(model, grid, errors);
      }
      else if (element instanceof Section section) {
        visit(model, section.getScreenElements(), errors);
      }
      else if (element instanceof MultiColumnSection section) {
        visit(model, section.getScreenElements(), errors);
      }
      else if (element instanceof EmbeddedRepeat repeat && repeat.getControlGrid() != null) {
        checkGrid(model, repeat.getControlGrid(), errors);
      }
      else if (element instanceof DetachedRepeat repeat && repeat.getDetailScreen() != null) {
        visit(model, repeat.getDetailScreen().getScreenElements(), errors);
      }
    }
  }

  private void checkGrid(A12Model<?> model, ControlGrid grid, List<ModelValidationError> errors) {
    ColumnLayout layout = grid.getLayout();
    if (layout == null || layout.getLg() == null) {
      return;
    }
    int[] lg = parse(layout.getLg());
    if (lg.length == 0) {
      return;
    }
    int[] md = layout.getMd() != null ? parse(layout.getMd()) : lg;
    int[] sm = layout.getSm() != null ? parse(layout.getSm()) : md;

    // A row's cells aren't authored per breakpoint - only their offset/span/the grid's column widths vary
    // per breakpoint - so the "wrong number of columns" check only ever runs once, against "lg" (matches
    // the real SME output: exactly one such message even when md/sm both disagree with lg too).
    for (Row row : grid.getRow()) {
      checkColumnCount(model, grid, row, lg.length, errors);
    }
    checkExceedsIndex(model, grid, "lg", lg.length, errors);
    checkExceedsIndex(model, grid, "md", md.length, errors);
    checkExceedsIndex(model, grid, "sm", sm.length, errors);
  }

  private void checkColumnCount(A12Model<?> model, ControlGrid grid, Row row, int columnCount,
                                 List<ModelValidationError> errors) {
    int total = 0;
    for (Cell cell : row.getCell()) {
      total += cellOffset(cell, "lg") + valueOrDefault(cell.getSpan(), "lg", 1);
    }
    if (total > columnCount) {
      errors.add(new ModelValidationError(model, grid.getId(),
          ValidationMessages.get("validation.controlGridLayout.wrongColumnCount", grid.getName(), columnCount, total),
          Severity.ERROR.name()));
    }
  }

  private void checkExceedsIndex(A12Model<?> model, ControlGrid grid, String breakpoint, int columnCount,
                                  List<ModelValidationError> errors) {
    for (Row row : grid.getRow()) {
      int cumulative = 1;
      for (Cell cell : row.getCell()) {
        int offset = cellOffset(cell, breakpoint);
        int position = cumulative + offset;
        if (position > columnCount) {
          errors.add(new ModelValidationError(model, cell.getId(),
              ValidationMessages.get("validation.controlGridLayout.exceedsMaxIndex", cell.getId(), breakpoint, offset, columnCount,
                  grid.getName()),
              Severity.ERROR.name()));
        }
        cumulative = position + valueOrDefault(cell.getSpan(), breakpoint, 1);
      }
    }
  }

  // Only Control cells carry an offset; TextCell/ExpressionCell always start flush at the cumulative position.
  private static int cellOffset(Cell cell, String breakpoint) {
    if (!(cell instanceof Control control)) {
      return 0;
    }
    return valueOrDefault(control.getOffset(), breakpoint, 0);
  }

  // Deliberately does NOT cascade lg->md->sm the way the column layout itself does: cascading a Control's
  // offset/span produced a false positive on a real fixture (Invoice_FM.json's "TermsAndConditions" grid),
  // so each breakpoint uses only its own explicit value here.
  private static int valueOrDefault(GridSpan gridSpan, String breakpoint, int defaultValue) {
    if (gridSpan == null) {
      return defaultValue;
    }
    Integer value = switch (breakpoint) {
      case "lg" -> gridSpan.getLg();
      case "md" -> gridSpan.getMd();
      case "sm" -> gridSpan.getSm();
      default -> null;
    };
    return value != null ? value : defaultValue;
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
