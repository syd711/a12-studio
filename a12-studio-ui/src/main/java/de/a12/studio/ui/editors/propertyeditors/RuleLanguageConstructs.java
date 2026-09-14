package de.a12.studio.ui.editors.propertyeditors;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The a12 kernel's Rule/Computation condition language's built-in "language constructs" (functions), e.g.
 * {@code GroupFilled(Group)} or {@code RangeAsString(Field, Start, End)} - used in a {@link
 * de.a12.studio.models.documentmodel.RuleElement}'s error condition and a {@link
 * de.a12.studio.models.documentmodel.ComputationAlternative}'s precondition/operation (see {@link
 * PlainPathSuggestionProvider}, the only {@link SuggestionProvider} that suggests these). Sourced from {@code
 * documentation/2606-06-doc/kernel-kernel-documentation-ba-en.md}'s "Language constructs in alphabetical order"
 * list and its Tables 15-20, 22-23, 26, 29-30.
 */
public final class RuleLanguageConstructs {

  public record LanguageConstruct(String name, String signature, String description) {
  }

  public static final List<LanguageConstruct> ALL = List.of(
      new LanguageConstruct("Abs", "Abs(Value)", "Returns the absolute value of Value."),
      new LanguageConstruct("AbsValue", "AbsValue(Field)", "Returns the absolute value of Field."),
      new LanguageConstruct("AddDays", "AddDays(X, Integer)", "Returns X (a Date or DateTime) plus Integer days, where Integer may be negative."),
      new LanguageConstruct("AddHours", "AddHours(DateTime, Integer)", "Returns DateTime plus Integer hours, where Integer may be negative."),
      new LanguageConstruct("AddMinutes", "AddMinutes(DateTime, Integer)", "Returns DateTime plus Integer minutes, where Integer may be negative."),
      new LanguageConstruct("AddMonths", "AddMonths(Date, Integer)", "Returns Date plus Integer months, where Integer may be negative."),
      new LanguageConstruct("AddSeconds", "AddSeconds(DateTime, Integer)", "Returns DateTime plus Integer seconds, where Integer may be negative."),
      new LanguageConstruct("AddYears", "AddYears(Date, Integer)", "Returns Date plus Integer years, where Integer may be negative."),
      new LanguageConstruct("AllFieldsFilled", "AllFieldsFilled(FieldList)", "Satisfied if a value is specified for all Fields in FieldList."),
      new LanguageConstruct("AllGroupsFilled", "AllGroupsFilled(GroupList)", "Satisfied if each Group in GroupList is filled."),
      new LanguageConstruct("AtLeastOneDateRangeOverlaps", "AtLeastOneDateRangeOverlaps(Field IN FieldList)", "Satisfied if the date range of Field overlaps with the date range of at least one Field in FieldList."),
      new LanguageConstruct("AtLeastOneFieldFilled", "AtLeastOneFieldFilled(FieldList)", "Satisfied if for at least one Field in FieldList a value is given."),
      new LanguageConstruct("AtLeastOneFieldValueIncludedInValueList", "AtLeastOneFieldValueIncludedInValueList(FieldList IN ValueList)", "Satisfied if one or more Field values in FieldList are included in ValueList."),
      new LanguageConstruct("AtLeastOneGroupFilled", "AtLeastOneGroupFilled(GroupList)", "Satisfied if there is a Group in GroupList that contains a Field which is filled."),
      new LanguageConstruct("BaseYear", "BaseYear", "Returns the Base Year specified in the Model's settings."),
      new LanguageConstruct("Comments", ";; comment text", "Text following ;; to the end of the line; a comment that may be placed before or after a partial condition or language construct and is ignored during evaluation."),
      new LanguageConstruct("CurrentRepetition", "CurrentRepetition(Group)", "Returns the repetition index of Group when the Rule iterates over Group, or the repetition matching the semantic index when Group is used with one."),
      new LanguageConstruct("CustomCondition", "CustomCondition Name", "Delegates evaluation to custom code implemented by the calling system, for conditions the validation language cannot express."),
      new LanguageConstruct("Date", "Date(Day, Month, Year)", "Combines Day, Month and Year (or Day, Month, Century, YearInCentury; or just Day, Month when a Base Year is set) into a Date constant."),
      new LanguageConstruct("DateFromDateTime", "DateFromDateTime(DateTime)", "Returns the date of DateTime."),
      new LanguageConstruct("DateRange", "DateRange(Date1, Date2)", "Returns the Date Range which starts with Date1 and ends with Date2."),
      new LanguageConstruct("DateRangesOverlap", "DateRangesOverlap(FieldList)", "Satisfied if at least two DateRange Fields in FieldList overlap."),
      new LanguageConstruct("DateTime", "DateTime(Date, Time)", "Combines Date and Time and returns the corresponding DateTime constant."),
      new LanguageConstruct("DayFromDate", "DayFromDate(X)", "Returns the day of X (a Date or DateTime) as a number."),
      new LanguageConstruct("DifferenceInDays", "DifferenceInDays(Date1, Date2)", "Returns the difference of Date1 and Date2 in days."),
      new LanguageConstruct("DifferenceInHours", "DifferenceInHours(DateTime1, DateTime2)", "Returns the difference of DateTime1 and DateTime2 in hours."),
      new LanguageConstruct("DifferenceInMinutes", "DifferenceInMinutes(DateTime1, DateTime2)", "Returns the difference of DateTime1 and DateTime2 in minutes."),
      new LanguageConstruct("DifferenceInMonths", "DifferenceInMonths(Date1, Date2)", "Returns the difference of Date1 and Date2 in months."),
      new LanguageConstruct("DifferenceInSeconds", "DifferenceInSeconds(DateTime1, DateTime2)", "Returns the difference of DateTime1 and DateTime2 in seconds."),
      new LanguageConstruct("DifferenceInYears", "DifferenceInYears(Date1, Date2)", "Returns the difference of Date1 and Date2 in years."),
      new LanguageConstruct("DiffersWithToleranceRange1", "Field1 DiffersWithToleranceRange1 Field2", "Satisfied if Field1 is unequal to Field2 with a tolerance of 1, i.e. the absolute value of Field1 minus Field2 is greater than 1."),
      new LanguageConstruct("DiffersWithToleranceRange10", "Field1 DiffersWithToleranceRange10 Field2", "Satisfied if Field1 is unequal to Field2 with a tolerance of 10, i.e. the absolute value of Field1 minus Field2 is greater than 10."),
      new LanguageConstruct("DiffersWithToleranceRange2", "Field1 DiffersWithToleranceRange2 Field2", "Satisfied if Field1 is unequal to Field2 with a tolerance of 2, i.e. the absolute value of Field1 minus Field2 is greater than 2."),
      new LanguageConstruct("DiffersWithToleranceRange5", "Field1 DiffersWithToleranceRange5 Field2", "Satisfied if Field1 is unequal to Field2 with a tolerance of 5, i.e. the absolute value of Field1 minus Field2 is greater than 5."),
      new LanguageConstruct("EndOfDateRange", "EndOfDateRange(DateRange)", "Returns the end date of DateRange (or, for the BaseYear constant, of the Model's Base Year)."),
      new LanguageConstruct("False", "False", "Boolean constant; only comparable for equality/inequality with Fields of type boolean, not confirm."),
      new LanguageConstruct("FieldFilled", "FieldFilled(Field)", "Satisfied if a value is given for Field."),
      new LanguageConstruct("FieldNotFilled", "FieldNotFilled(Field)", "Satisfied if a value is not given for Field."),
      new LanguageConstruct("FieldsNotCollectivelyFilled", "FieldsNotCollectivelyFilled(FieldList)", "Satisfied if a value is given for at least one, but not all, Fields in FieldList."),
      new LanguageConstruct("FieldValueAsNumber", "FieldValueAsNumber(Field)", "Returns the value of Field as a number."),
      new LanguageConstruct("FieldValueAsString", "FieldValueAsString(Field)", "Returns the value of Field as a string."),
      new LanguageConstruct("FieldValueIncludedInValueList", "FieldValueIncludedInValueList(Field, ValueList)", "Satisfied if a value is given for Field that is included in ValueList."),
      new LanguageConstruct("FieldValueNotIncludedInValueList", "FieldValueNotIncludedInValueList(Field, ValueList)", "Satisfied if a value is given for Field that is not included in ValueList."),
      new LanguageConstruct("FieldValuesNotUnique", "FieldValuesNotUnique(FieldList)", "Satisfied if at least two Fields in FieldList share the same value."),
      new LanguageConstruct("FirstDay", "FirstDay", "Date specification for ValueAsDate that replaces a partially known date with the earliest possible date."),
      new LanguageConstruct("FirstFilledValue", "FirstFilledValue(FieldList)", "Returns the value of the first filled Field in FieldList."),
      new LanguageConstruct("GroupFilled", "GroupFilled(Group)", "Satisfied if for at least one Field of Group a value is given."),
      new LanguageConstruct("GroupNotFilled", "GroupNotFilled(Group)", "Satisfied if no value is given for any Field of Group."),
      new LanguageConstruct("GroupsNotCollectivelyFilled", "GroupsNotCollectivelyFilled(GroupList)", "Satisfied if at least one, but not all, Groups in GroupList are filled."),
      new LanguageConstruct("HoursFromTime", "HoursFromTime(X)", "Returns the hours of X (a Time or DateTime) as a number between 00 and 23."),
      new LanguageConstruct("Invalid", "Invalid(Field, CustomType)", "Satisfied if Field has an invalid value relative to CustomType, or, without CustomType, if Field's constructed date is invalid."),
      new LanguageConstruct("LastDay", "LastDay", "Date specification for ValueAsDate that replaces a partially known date with the latest possible date."),
      new LanguageConstruct("Length", "Length(Field)", "Returns the number of characters of the value of Field."),
      new LanguageConstruct("Max", "Max(ValueList)", "Returns the maximum of the values in ValueList."),
      new LanguageConstruct("MaxValue", "MaxValue(FieldList)", "Returns the maximum value of the Fields in FieldList."),
      new LanguageConstruct("Min", "Min(ValueList)", "Returns the minimum of the values in ValueList."),
      new LanguageConstruct("MinutesFromTime", "MinutesFromTime(X)", "Returns the minutes of X (a Time or DateTime) as a number."),
      new LanguageConstruct("MinValue", "MinValue(FieldList)", "Returns the minimum value of the Fields in FieldList."),
      new LanguageConstruct("MonthFromDate", "MonthFromDate(X)", "Returns the month of X (a Date or DateTime) as a number."),
      new LanguageConstruct("MoreThanOneFieldFilled", "MoreThanOneFieldFilled(FieldList)", "Satisfied if for more than one Field in FieldList a value is given."),
      new LanguageConstruct("NoFieldFilled", "NoFieldFilled(FieldList)", "Satisfied if no value is given for any of the Fields in FieldList."),
      new LanguageConstruct("NoFieldValueIncludedInValueList", "NoFieldValueIncludedInValueList(FieldList IN ValueList)", "Satisfied if no Field value in FieldList is contained in ValueList."),
      new LanguageConstruct("NoGroupFilled", "NoGroupFilled(GroupList)", "Satisfied if no Group in GroupList contains a Field that is filled."),
      new LanguageConstruct("NotAllFieldsFilled", "NotAllFieldsFilled(FieldList)", "Satisfied if a value is not given for all Fields in FieldList."),
      new LanguageConstruct("NotAllFieldValuesIncludedInValueList", "NotAllFieldValuesIncludedInValueList(FieldList IN ValueList)", "Satisfied if for at least one Field in FieldList a value is given but this value is not included in ValueList."),
      new LanguageConstruct("NotAllGroupsFilled", "NotAllGroupsFilled(GroupList)", "Satisfied if there is a Group in GroupList that is not filled."),
      new LanguageConstruct("NotExactlyOneFieldFilled", "NotExactlyOneFieldFilled(FieldList)", "Satisfied if either none or more than one of the Fields in FieldList are filled."),
      new LanguageConstruct("Now", "Now", "Returns the current date and time of evaluation."),
      new LanguageConstruct("NumberOfDifferentValues", "NumberOfDifferentValues(FieldList)", "Returns the number of different values of the Fields in FieldList."),
      new LanguageConstruct("NumberOfFilledFields", "NumberOfFilledFields(FieldList)", "Returns the number of filled Fields in FieldList."),
      new LanguageConstruct("NumberOfFilledGroups", "NumberOfFilledGroups(GroupList)", "Returns the number of filled Groups in GroupList."),
      new LanguageConstruct("NumberOfValueInFields", "NumberOfValueInFields(Constant IN FieldList)", "Returns the number of Fields in FieldList with value Constant."),
      new LanguageConstruct("PatternMatched", "StringField PatternMatched RegExp", "Satisfied if the value of StringField matches the regular expression RegExp."),
      new LanguageConstruct("PatternViolated", "StringField PatternViolated RegExp", "Satisfied if the value of StringField violates (does not match) the regular expression RegExp."),
      new LanguageConstruct("QuarterFromDate", "QuarterFromDate(X)", "Returns the quarter of X (a Date or DateTime) as a number between 1 and 4."),
      new LanguageConstruct("RangeAsNumber", "RangeAsNumber(Field, Start, End)", "Returns the substring of Field given by Start and End as a number."),
      new LanguageConstruct("RangeAsString", "RangeAsString(Field, Start, End)", "Returns the substring of Field given by Start and End as a string."),
      new LanguageConstruct("RepetitionNotUnique", "RepetitionNotUnique(FieldList @From Group)", "Satisfied, per iteration, if there exists another instance with the same values in FieldList; the '@From Group' reference is optional."),
      new LanguageConstruct("RoundAccounting", "RoundAccounting(Value, DecimalPlaces)", "Returns Value, rounded to the nearest number with DecimalPlaces decimal places; DecimalPlaces is optional."),
      new LanguageConstruct("RoundAccountingValue", "RoundAccountingValue(Field, DecimalPlaces)", "Returns the value of Field, rounded to the nearest number with DecimalPlaces decimal places; DecimalPlaces is optional."),
      new LanguageConstruct("RoundDown", "RoundDown(Value, DecimalPlaces)", "Returns Value, rounded down to DecimalPlaces decimal places; DecimalPlaces is optional."),
      new LanguageConstruct("RoundDownValue", "RoundDownValue(Field, DecimalPlaces)", "Returns the value of Field, rounded down to DecimalPlaces decimal places; DecimalPlaces is optional."),
      new LanguageConstruct("RoundUp", "RoundUp(Value, DecimalPlaces)", "Returns Value, rounded up to DecimalPlaces decimal places; DecimalPlaces is optional."),
      new LanguageConstruct("RoundUpValue", "RoundUpValue(Field, DecimalPlaces)", "Returns the value of Field, rounded up to DecimalPlaces decimal places; DecimalPlaces is optional."),
      new LanguageConstruct("RuleGroup", "RuleGroup", "Refers to the nearest enclosing Rule Group; usable as a path segment (e.g. '@From RuleGroup') but never with an asterisk."),
      new LanguageConstruct("SecondsFromTime", "SecondsFromTime(X)", "Returns the seconds of X (a Time or DateTime) as a number."),
      new LanguageConstruct("StartOfDateRange", "StartOfDateRange(DateRange)", "Returns the start date of DateRange (or, for the BaseYear constant, of the Model's Base Year)."),
      new LanguageConstruct("Sum", "Sum(FieldList)", "Returns the sum of the values of the filled Fields in FieldList."),
      new LanguageConstruct("SumOfProducts", "SumOfProducts(Field1, Field2)", "For each repetition, calculates the product of the two repeatable Fields Field1 and Field2 and returns the sum of those products."),
      new LanguageConstruct("SumOfTerms", "SumOfTerms(Path, Term)", "For each repetition along Path, calculates the value of the numerical Term and returns the sum of those values."),
      new LanguageConstruct("Time", "Time(Hours, Minutes, Seconds)", "Combines Hours, Minutes and Seconds (each optional, defaulting to 00) into a Time constant."),
      new LanguageConstruct("TimeFromDateTime", "TimeFromDateTime(DateTime)", "Returns the time of DateTime."),
      new LanguageConstruct("Today", "Today", "Returns the current date."),
      new LanguageConstruct("True", "True", "Boolean constant, used to compare equality/inequality with Fields of type confirm or boolean."),
      new LanguageConstruct("Valid", "Valid(Field, CustomType)", "Satisfied if Field has a valid value relative to CustomType, or, without CustomType, if Field's constructed date is valid."),
      new LanguageConstruct("ValueAsDate", "ValueAsDate(Field, DateSpecification)", "Returns the value of the partially known date Field as a concrete date, using the FirstDay or LastDay DateSpecification."),
      new LanguageConstruct("YearFromDate", "YearFromDate(X)", "Returns the year of X (a Date or DateTime) as a number.")
  );

  /** Bare names only, for {@link RuleEditorController#setHighlightedFunctionNames}. */
  public static final Set<String> NAMES = ALL.stream()
      .map(LanguageConstruct::name)
      .collect(Collectors.toUnmodifiableSet());

  private RuleLanguageConstructs() {
  }

  /** {@code prefix}-filtered (case-insensitive substring match, matching {@link PlainPathSuggestionProvider}'s
   * own field-path filtering convention) suggestions, ready to merge with field-path suggestions. */
  public static List<Suggestion> suggestions(String prefix) {
    String lower = prefix.toLowerCase();
    return ALL.stream()
        .filter(construct -> construct.name().toLowerCase().contains(lower))
        .map(construct -> new Suggestion(construct.name(), construct.name(), construct.signature() + " – " + construct.description()))
        .toList();
  }
}
