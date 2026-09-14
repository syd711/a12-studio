package de.a12.studio.models.documentmodel.rulelang;

import de.a12.studio.models.TestHelper;
import de.a12.studio.models.util.JsonSettings;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import tools.jackson.databind.JsonNode;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies RuleLang.g4 against (1) a hand-picked sample of every syntax construct the kernel doc + this repo's
 * fixtures exercise, (2) the kernel doc's own canonical parser-message examples (section "8.1 Grammar"), and
 * (3) - most importantly - every {@code errorCondition}/{@code precondition}/{@code operation}/{@code
 * commonPrecondition} string actually present in {@code testing/workspaces/**}, since a false positive there
 * would mean {@link RuleLanguageSyntaxChecker} flags real, already-saved model content as invalid the moment
 * someone opens it in the Studio.
 */
class RuleLanguageSyntaxCheckerTest {

  @ParameterizedTest
  @ValueSource(strings = {
      "GroupFilled(RuleGroup) and FieldNotFilled(internal_filename)",
      "GroupFilled(CreditCard) AND [MethodOfPayment] != \"credit\"",
      "FieldFilled(DueDate) and [DueDate] < Today",
      "AllFieldsFilled(OrderingDate, DeliveryDate) and [OrderingDate] >= [DeliveryDate]",
      "NotExactlyOneFieldFilled(attachment_id, content)",
      "Sum(/Order/OrderInformation*/Quantity)",
      "NumberOfFilledGroups(OrderInformation*) > 5",
      "AtLeastOneFieldValueIncludedInValueList(SpecialOfferProduct In OrderInformation*/Product)",
      "NoFieldValueIncludedInValueList(Products*/ProductNumber IN \"007\", \"42\", \"4711\", \"0815\")",
      "FieldValueNotIncludedInValueList(ProductNumber, \"007\", \"42\", \"4711\")",
      "RoundAccounting([TotalPrice] * 0.9, 2) + [ShippingCosts]",
      "[TotalPriceWithShippingCosts]-[TotalPriceWithDiscount]",
      "RoundAccounting([PricingSumOfItemPrices] * {1 + [PricingPremium]/100},2)",
      "[Addresses/BillingAddress/FirstName] + \" \" + [Addresses/BillingAddress/LastName] +\n\", \" + [Addresses/BillingAddress/Street]",
      "[OrderInformation/Product For MaxNumber]",
      "FieldValueAsString(OrderInformation/Quantity For MaxNumber)",
      "NumberOfFilledFields(OrderInformation*/ProductType Having [OrderInformation/ProductType -> Cat] == \"Food\")",
      "FieldValueAsNumber(Field->AsNumber)",
      "FieldFilled(Start) and AtLeastOneFieldFilled(../Pricing*/Price Having (CurrentRepetition(RuleGroup) != CurrentRepetition($RuleGroup) and ([Start] <= [$Start] and [$Start] <= [End])))",
      "NumberOfFilledFields(../../Variants*/Attributes*/AttributeName Having ([AttributeName] ==[$AttributeName] and [AttributeValue] ==[$AttributeValue]))>1",
      "FieldNotFilled(LatestPriceWithDiscount) and\r\nFieldNotFilled(CurrentPrice) and GroupFilled(RuleGroup)",
      ";; a leading comment\nFieldFilled(x) ;; and a trailing one",
      "10",
      "[Price] * [Quantity]",
      "[Field1] DiffersWithToleranceRange10 [Field2]",
      "StringField PatternMatched \"^[A-Z]+$\"",
      "@SuppressWarning(MVK_INVALID_COMPARE_DEC_PLACES)\n[Product] == [Factor1] * [Factor2]",
      "3 ^ 2 + 1 * 2",
      "a ^ { 2 + 1 } * 2".replace("a", "3"),
  })
  void acceptsKnownValidConstructs(String expression) {
    assertNull(RuleLanguageSyntaxChecker.validate(expression), () -> "expected no syntax error for: " + expression);
  }

  /** The kernel doc's own worked examples for each parser-message category - see
   * documentation/2606-06-doc/kernel-kernel-documentation-ba-en.md lines ~4826-4869. */
  @ParameterizedTest
  @CsvSource(delimiter = '|', textBlock = """
      NumberOfFilledFields(                | MVK_INCOMPLETE_INPUT
      NumberOfFilledFields                 | MVK_EXPECTED_TOKEN_NOT_FOUND
      FieldFilled()                        | MVK_UNEXPECTED_TOKEN
      FieldFilled(F) ?                     | MVK_LEXER_STANDARD_ERROR
      """)
  void reproducesKernelParserMessageCategories(String expression, String expectedCode) {
    String message = RuleLanguageSyntaxChecker.validate(expression);
    assertTrue(message != null && message.contains("[" + expectedCode + "]"),
        () -> "expected [" + expectedCode + "] for '" + expression + "', got: " + message);
  }

  @ParameterizedTest
  @ValueSource(strings = {
      "and [FirstName]",
      "FieldFilled(x",
      "[FirstName] ===",
  })
  void rejectsObviouslyInvalidInput(String expression) {
    assertTrue(RuleLanguageSyntaxChecker.validate(expression) != null);
  }

  /** Regression guard: every Rule/Computation condition already saved in this repo's fixtures must still
   * parse cleanly, or opening that model in the Studio would show a false error the moment {@link
   * de.a12.studio.ui.editors.propertyeditors.RichtextEditorController#setCustom} validates it on load. */
  @TestFactory
  Stream<DynamicTest> acceptsEveryConditionInTestFixtures() throws IOException {
    List<Path> roots = List.of(
        TestHelper.resolveTestingBasicDir(),
        TestHelper.resolveTestingAdvancedNewDir(),
        TestHelper.resolveTestingCommerceDir());

    List<DynamicTest> tests = new ArrayList<>();
    for (Path root : roots) {
      List<Path> jsonFiles;
      try (Stream<Path> walk = Files.walk(root)) {
        jsonFiles = walk.filter(Files::isRegularFile)
            .filter(path -> path.getFileName().toString().endsWith(".json"))
            .sorted()
            .toList();
      }
      for (Path file : jsonFiles) {
        JsonNode tree;
        try {
          tree = JsonSettings.objectMapper.readTree(file.toFile());
        }
        catch (RuntimeException e) {
          continue;
        }
        for (FoundExpression found : collectConditionFields(tree, "")) {
          tests.add(DynamicTest.dynamicTest(root.relativize(file) + " :: " + found.path(), () ->
              assertEquals(null, RuleLanguageSyntaxChecker.validate(found.value()),
                  "unexpected syntax error for " + file + " :: " + found.path())));
        }
      }
    }
    return tests.stream();
  }

  private record FoundExpression(String path, String value) {
  }

  private static final Set<String> CONDITION_FIELD_NAMES =
      Set.of("errorCondition", "precondition", "operation", "commonPrecondition");

  private static List<FoundExpression> collectConditionFields(JsonNode node, String path) {
    List<FoundExpression> found = new ArrayList<>();
    if (node.isObject()) {
      node.properties().forEach(entry -> {
        String childPath = path.isEmpty() ? entry.getKey() : path + "." + entry.getKey();
        JsonNode value = entry.getValue();
        if (CONDITION_FIELD_NAMES.contains(entry.getKey()) && value.isString() && !value.asString().isBlank()) {
          found.add(new FoundExpression(childPath, value.asString()));
        }
        else {
          found.addAll(collectConditionFields(value, childPath));
        }
      });
    }
    else if (node.isArray()) {
      for (int i = 0; i < node.size(); i++) {
        found.addAll(collectConditionFields(node.get(i), path + "[" + i + "]"));
      }
    }
    return found;
  }
}
