package de.a12.studio.modelsvalidation.validators.form;

import de.a12.studio.models.documentmodel.DateFieldType;
import de.a12.studio.models.documentmodel.FieldElement;
import de.a12.studio.models.documentmodel.DateRangeFieldType;
import de.a12.studio.models.documentmodel.DateTimeFieldType;
import de.a12.studio.models.documentmodel.FieldType;
import de.a12.studio.models.formmodel.DatePickerConfig;
import de.a12.studio.modelsvalidation.validators.ElementIndex;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Rules for a Control's or column's {@link DatePickerConfig} (the year range of the date picker), ported from
 * SME: which fields have a date picker at all ({@code technicalField_showDatePickerConfig}), when a config is
 * redundant and dropped ({@code removeEmptyDatePickerConfig} in {@code documentHelpers.ts}) and the three
 * rules of {@code I_DatePickerConfig.json}. Without {@code absolute} the years are offsets from the current
 * year (the default range is -7/+7); with it they are calendar years.
 */
public final class DatePickerSupport {

  /** What can be wrong with a config; each maps to one message of {@code validation.datePicker.*}. */
  public enum Problem {
    MIN_YEAR_ABOVE_MAX_YEAR("validation.datePicker.minYearAboveMaxYear"),
    PRESELECTION_OUTSIDE_RANGE("validation.datePicker.preselectionOutsideRange"),
    NEGATIVE_YEAR_WHEN_ABSOLUTE("validation.datePicker.negativeYearWhenAbsolute");

    private final String messageKey;

    Problem(String messageKey) {
      this.messageKey = messageKey;
    }

    public String messageKey() {
      return messageKey;
    }
  }

  private DatePickerSupport() {
  }

  /** Date and date-time fields, and date ranges in the format {@code YYYY-MM-DD}, have a date picker. */
  public static boolean isSupported(@Nullable FieldType fieldType) {
    if (fieldType instanceof DateFieldType || fieldType instanceof DateTimeFieldType) {
      return true;
    }
    return fieldType instanceof DateRangeFieldType range && range.getDateRangeType() != null
        && "YYYY-MM-DD".equals(range.getDateRangeType().getFormat());
  }

  /** Whether the element {@code elementRef} resolves to is a field with a date picker. */
  public static boolean isSupportedElement(@Nullable ElementIndex elementIndex, @Nullable String elementRef) {
    if (elementIndex == null || elementRef == null) {
      return false;
    }
    return elementIndex.resolveElement(elementRef)
        .filter(element -> element instanceof FieldElement field && field.getField() != null
            && isSupported(elementIndex.effectiveFieldType(field.getField().getFieldType())))
        .isPresent();
  }

  /**
   * Whether the config carries no information: no year, {@code absolute} not true and no preselection (or the
   * preselection 0 - the same as none). SME drops such a config, e.g. one left with only {@code "absolute": false}.
   */
  public static boolean isRedundant(@Nullable DatePickerConfig config) {
    if (config == null) {
      return true;
    }
    boolean noPreselection = config.getPreselectionYear() == null || config.getPreselectionYear() == 0;
    return config.getMinYear() == null && config.getMaxYear() == null
        && !Boolean.TRUE.equals(config.getAbsolute()) && noPreselection;
  }

  public static List<Problem> problems(@Nullable DatePickerConfig config) {
    List<Problem> problems = new ArrayList<>();
    if (config == null) {
      return problems;
    }
    Integer min = config.getMinYear();
    Integer max = config.getMaxYear();
    Integer preselection = config.getPreselectionYear();
    if (min != null && max != null && min > max) {
      problems.add(Problem.MIN_YEAR_ABOVE_MAX_YEAR);
    }
    if (preselection != null && min != null && max != null && (preselection < min || preselection > max)) {
      problems.add(Problem.PRESELECTION_OUTSIDE_RANGE);
    }
    if (Boolean.TRUE.equals(config.getAbsolute())
        && (isNegative(min) || isNegative(max) || isNegative(preselection))) {
      problems.add(Problem.NEGATIVE_YEAR_WHEN_ABSOLUTE);
    }
    return problems;
  }

  private static boolean isNegative(@Nullable Integer year) {
    return year != null && year < 0;
  }
}
