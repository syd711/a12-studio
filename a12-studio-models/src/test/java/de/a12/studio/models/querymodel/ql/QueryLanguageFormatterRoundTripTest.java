package de.a12.studio.models.querymodel.ql;

import de.a12.studio.models.querymodel.operator.Operator;
import de.a12.studio.models.util.JsonSettings;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import tools.jackson.databind.JsonNode;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Verifies emit(text) -> format(operator) -> emit(formattedText) reaches the same {@link Operator} JSON, i.e. an
 * editor round trip (edit -> save -> reload) is lossless even though the formatted text need not be byte-identical
 * to what a user originally typed (e.g. Date(...) argument spacing, or an InRange(...) rendering of a two-sided
 * comparison that could also have been written as two separate constraints).
 */
class QueryLanguageFormatterRoundTripTest {

  private final QueryLanguageEmitter emitter = new QueryLanguageEmitter();
  private final QueryLanguageFormatter formatter = new QueryLanguageFormatter();

  @ParameterizedTest
  @ValueSource(strings = {
      "[/Person/FirstName] == \"John Doe\"",
      "[/Fields/Price] == 100",
      "[/Fields/Price] == 100.5",
      "[/Fields/sport] == Null",
      "[/LinkFields/TimeShare] >= 20",
      "[/price] <= 100",
      "[/BusinessPartnerRoot/Country] != \"Italy\"",
      "[/title] ~ \"important\"",
      "[/title] !~ \"important\"",
      "[/Person/FirstName] == \"John Doe\" and [/LinkFields/TimeShare] >= 20",
      "[/Person/FirstName] == \"John Doe\" or [/Person/FirstName] == \"Jane Doe\"",
      "!([/Person/FirstName] == \"John Doe\")",
      "([/A] == \"x\" or [/B] == \"y\") and [/C] == \"z\"",
      "([/A] == \"x\" and [/B] == \"y\") or [/C] == \"z\"",
      "Has(\"TeamPerson\", \"Person\")",
      "Has(\"TeamPerson\", \"Person\", [/Person/FirstName] == \"John Doe\")",
      "Has(\"TeamPerson\", \"Person\", Null, [/LinkFields/TimeShare] >= 20)",
      "Has(\"TeamPerson\", \"Person\", [/A] == \"x\", [/B] == \"y\")",
      "[/expiresAt] >= Date(15, 8, 2024)",
      "[/expiresAt] <= Date(15, 8, 2024)",
      "[/Contract/SignedAt] == Date(1, 1, 2020)",
      "[/appointmentTime] >= Time(9, 0, 0)",
      "[/x] == DateTime(Date(15, 3, 2024), Time(14, 30, 0))",
      "[/eventMonth] >= DateFragment(3)",
      "[/eventYear] <= DateFragment(2024)",
      "[/x] >= DateFragment(3, 15)",
      "[/x] >= DateFragment(2024, 3)",
      "InRange([/age], 18, 65)",
      "InRange([/birthDate], Date(1, 1, 1980), Date(31, 12, 1999))",
      "InRange([/appointmentTime], Time(9, 0, 0), Time(17, 0, 0))",
      "InRange([/eventMonth], DateFragment(3), DateFragment(5))",
      "Match([/name], [/description], \"premium\", \"gold\")",
      "Match([/title], \"important\")",
      "Match(\"urgent\", \"review\")"
  })
  void formattingAndReEmittingProducesTheSameOperatorJson(String expression) throws Exception {
    Operator original = emitter.emit(expression);
    String formatted = formatter.format(original);
    Operator reEmitted = emitter.emit(formatted);

    JsonNode originalJson = JsonSettings.objectMapper.valueToTree(original);
    JsonNode reEmittedJson = JsonSettings.objectMapper.valueToTree(reEmitted);

    assertEquals(originalJson, reEmittedJson,
        "round-tripping \"" + expression + "\" through the formatter changed the operator (formatted as: " + formatted + ")");
  }
}
