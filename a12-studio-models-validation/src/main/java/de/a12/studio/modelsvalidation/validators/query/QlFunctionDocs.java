package de.a12.studio.modelsvalidation.validators.query;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The hover documentation of the Query Language functions - the {@code documentation} blocks of SME's
 * {@code moduleSupport/qmm} {@code functions.ts}, keyed by callee ({@code Equal}, {@code Has}, {@code And}, ...).
 * {@code signature} is only set where SME spells one out by hand (the logical operators and {@code Match}).
 */
final class QlFunctionDocs {

  record Doc(String description, String signature, List<String> notes, List<String> examples) {
  }

  private static final Map<String, Doc> DOCS = new HashMap<>();

  private QlFunctionDocs() {
  }

  static Doc find(String callee) {
    return DOCS.get(callee);
  }

  private static void put(String callee, String description, List<String> notes, List<String> examples) {
    DOCS.put(callee, new Doc(description, null, notes, examples));
  }

  private static void put(String callee, String description, String signature, List<String> examples) {
    DOCS.put(callee, new Doc(description, signature, List.of(), examples));
  }

  static {
    put("GreaterThanOrEqual", "Checks if a field value is greater than or equal to a specified value.", List.of(),
        List.of("[/price] >= 100", "[/expiresAt] >= Date(15, 8, 2024)"));
    put("LessThanOrEqual", "Checks if a field value is less than or equal to a specified value.", List.of(),
        List.of("[/price] <= 100", "[/expiresAt] <= Date(15, 8, 2024)"));
    put("Equal", "Checks whether a field value is equal to a specified value", List.of(),
        List.of("[/status] == \"active\"", "[/customer/address/city] == \"Berlin\""));
    put("NotEqual", "Checks whether a field value is not equal to a specified value", List.of(),
        List.of("[/updatedAt] != Date(1,1, 2024)", "[/status] != \"archived\""));
    put("SingleMatch", "Returns true if the field value contains the specified string", List.of(),
        List.of("[/name] ~ \"John\"", "[/description] ~ \"premium\"", "[/title] ~ \"Important\""));
    put("NotSingleMatch", "Returns true if the field value does not contain the specified string", List.of(),
        List.of("[/name] !~ \"test\"", "[/description] !~ \"deprecated\"", "[/status] !~ \"inactive\""));
    put("InRange", "Checks if a field value falls within a specified range (inclusive).",
        List.of("All three arguments must be of compatible types (Number, Time, Date, DateTime, or DateFragment)",
            "The 'from' value must be less than or equal to the 'to' value",
            "For DateFragment arguments, both range bounds must use the same format",
            "Range comparison is inclusive of both bounds"),
        List.of("InRange([/age], 18, 65) // Age between 18 and 65",
            "InRange([/appointmentTime], Time(9, 0, 0), Time(17, 0, 0)) // Business hours",
            "InRange([/birthDate], Date(1, 1, 1980), Date(31, 12, 1999)) // Born in 80s or 90s",
            "InRange([/eventMonth], DateFragment(3), DateFragment(5)) // Spring months"));
    put("DateFragment",
        "Creates a partial date for range comparisons. Supports multiple formats based on argument count and values.",
        List.of("Single argument: Month (1-12) or year (>=1000) based on value",
            "Two arguments with month first (1-12): Month and day format",
            "Two arguments with year first (>=1000): Year and month format",
            "Values 13-999 are invalid and will cause validation errors"),
        List.of("DateFragment(3) // March", "DateFragment(2024) // Year 2024", "DateFragment(3, 15) // March 15th",
            "DateFragment(2024, 3) // March 2024"));
    put("Date", "Creates a date value from day, month, and year components.",
        List.of("Day must be a positive integer representing the day of the month (1-31)",
            "Month must be a positive integer representing the month (1=January, 12=December)",
            "Year must be a positive integer representing the year (1000-9999)",
            "The function validates that the resulting date exists (handles leap years and month-specific day limits)"),
        List.of("Date(15, 3, 2024) // March 15th, 2024", "Date(31, 12, 2023) // December 31st, 2023"));
    put("DateRange", "Creates a date range between two dates or date fragments.",
        List.of("Both arguments must be the same type (Date or DateFragment)",
            "The 'from' date/fragment must be earlier than or equal to the 'to' date/fragment",
            "For DateFragment arguments, both must use the same format (e.g., both MM-DD or both YYYY-MM)"),
        List.of("DateRange(Date(1, 1, 2024), Date(31, 12, 2024)) // Full year 2024",
            "DateRange(DateFragment(2024, 1), DateFragment(2024, 3)) // January to March 2024",
            "DateRange(DateFragment(1), DateFragment(6)) // January to June (any year)"));
    put("DateTime", "Combines a date and time into a datetime value.",
        List.of("Date argument must be a valid Date value created by the Date function",
            "Time argument must be a valid Time value created by the Time function",
            "Both Date and Time function results are validated before being passed to DateTime"),
        List.of("DateTime(Date(15, 3, 2024), Time(14, 30, 0)) // March 15th, 2024 at 2:30 PM",
            "DateTime(Date(1, 1, 2024), Time(0, 0, 0)) // New Year's Day 2024 at midnight",
            "DateTime(Date(31, 12, 2023), Time(23, 59, 59)) // Last moment of 2023"));
    put("Time", "Creates a time value from hour, minute, and second components.",
        List.of("Hour must be a non-negative integer representing the hour in 24-hour format (0-23)",
            "Minute must be a non-negative integer representing the minute (0-59)",
            "Second must be a non-negative integer representing the second (0-59)"),
        List.of("Time(14, 30, 0) // 2:30 PM", "Time(0, 0, 0) // Midnight", "Time(23, 59, 59) // End of day"));
    put("Has", "Checks for the existence of related documents through a relationship.",
        List.of("Relationship model must exist and include current document model",
            "Target role must exist and differ from current document's role",
            "Constraint expressions are validated against respective document models"),
        List.of("Has(\"CustomerOrders\", \"Order\") // checks if there are any orders for the current customer",
            "Has(\"CustomerOrders\", \"order\", [/status] == \"completed\", Null) // checks if there are any completed orders for the current customer",
            "Has(\"TeamMember\", \"Member\", [/active] == True, [/joinDate] >= Date(1, 1, 2024)) // checks if there are any active team members who joined in 2024"));
    put("Not", "Returns the logical opposite of the provided expression", "!(Expression)",
        List.of("!([/price] >= 300) // equivalent to [/price] < 300", "!([/age] <= 18 or [/student] == True)"));
    put("And", "Returns true only when all provided expressions evaluate to true",
        "Expression1 and Expression2 and ... and ExpressionN",
        List.of("[/updatedAt] >= Date(1, 1, 2025) and [/category] == \"premium\"",
            "[/price] >= 100 and [/inStock] == True"));
    put("Or", "Returns true when any of the provided expressions evaluates to true",
        "Expression1 or Expression2 or ... or ExpressionN",
        List.of("[/category] == \"premium\" or [/category] == \"gold\" or [/category] == \"platinum\"",
            "[/age] < 18 or [/student] == True", "[/status] == \"active\" or [/trial] == True"));
    DOCS.put("Match", new Doc(
        "Performs text matching against multiple fields and values. Field references must come before string literals.",
        "Match([field1: FieldReference, ...], value1: String, [value2: String, ...])",
        List.of("All field references must come before string value arguments",
            "At least one string argument is required for the function to be valid",
            "Field references are validated against the document model",
            "Each field reference is validated to ensure it exists and refers to a text-compatible field"),
        List.of("Match([/name], [/description], \"premium\", \"gold\")",
            "Match([/title], \"important\")  // Single field, single value",
            "Match(\"urgent\", \"review\") // String values only")));
  }
}
