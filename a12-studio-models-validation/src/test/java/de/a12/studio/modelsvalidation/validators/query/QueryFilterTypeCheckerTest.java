package de.a12.studio.modelsvalidation.validators.query;

import de.a12.studio.models.documentmodel.DateFieldType;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.documentmodel.DocumentModelContent;
import de.a12.studio.models.documentmodel.EnumerationFieldType;
import de.a12.studio.models.documentmodel.EnumerationValue;
import de.a12.studio.models.documentmodel.FieldConfig;
import de.a12.studio.models.documentmodel.FieldElement;
import de.a12.studio.models.documentmodel.FieldType;
import de.a12.studio.models.documentmodel.GroupConfig;
import de.a12.studio.models.documentmodel.GroupElement;
import de.a12.studio.models.documentmodel.ModelRoot;
import de.a12.studio.models.documentmodel.NumberFieldType;
import de.a12.studio.models.documentmodel.StringFieldType;
import de.a12.studio.modelsvalidation.validators.query.QueryFilterReferenceChecker.Models;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link QueryFilterTypeChecker} on a small model of its own; the message texts and ranges are pinned against SME
 * itself by {@code QueryFilterTypeCheckerGoldenTest} (which needs the SME checkout), so this keeps one example of
 * each kind of finding where the build can always see it. Messages are the English ones.
 */
class QueryFilterTypeCheckerTest {

  private static final ResourceBundle ENGLISH = ResourceBundle.getBundle("validation-messages", Locale.ENGLISH,
      ResourceBundle.Control.getNoFallbackControl(ResourceBundle.Control.FORMAT_DEFAULT));

  private static final DocumentModel MODEL = model();
  private final QueryFilterTypeChecker checker = new QueryFilterTypeChecker(new Models(List.of(MODEL), List.of()));

  private static FieldElement field(String name, FieldType type) {
    FieldElement field = new FieldElement();
    field.setId("field_" + name);
    field.setName(name);
    FieldConfig config = new FieldConfig();
    config.setFieldType(type);
    field.setField(config);
    return field;
  }

  private static DocumentModel model() {
    EnumerationFieldType state = new EnumerationFieldType();
    state.setEnumerationType(new de.a12.studio.models.documentmodel.EnumerationTypeOptions());
    for (String value : List.of("Open", "Closed")) {
      EnumerationValue enumerationValue = new EnumerationValue();
      enumerationValue.setValue(value);
      state.getEnumerationType().getValues().add(enumerationValue);
    }
    GroupElement root = new GroupElement();
    root.setId("group_root");
    root.setName("Root");
    GroupConfig config = new GroupConfig();
    config.setRepeatability(1);
    config.setElements(new ArrayList<>(List.of(field("Name", new StringFieldType()), field("Count", new NumberFieldType()),
        field("Due", new DateFieldType()), field("State", state))));
    root.setGroup(config);
    ModelRoot modelRoot = new ModelRoot();
    modelRoot.setRootGroups(new ArrayList<>(List.of(root)));
    DocumentModelContent content = new DocumentModelContent();
    content.setModelRoot(modelRoot);
    DocumentModel model = new DocumentModel();
    model.setId("Task_DM");
    model.setContent(content);
    return model;
  }

  private List<String> check(String filter) {
    return checker.check(filter, MODEL).stream()
        .map(diagnostic -> filter.substring(diagnostic.start(), diagnostic.stop() + 1) + " -> " + diagnostic.message(ENGLISH))
        .toList();
  }

  @Test
  void aWellTypedFilterHasNoFindings() {
    assertEquals(List.of(), check("[/Root/Name] == \"a\" and [/Root/Count] >= 3"));
    assertEquals(List.of(), check("[/Root/Due] == Date(29, 2, 2024) or [/Root/State] == \"Open\" or [/Root/Name] == Null"));
    assertEquals(List.of(), check("InRange([/Root/Count], 1, 5) and Match([/Root/Name], \"x\", \"y\")"));
  }

  @Test
  void aValueMustFitTheFieldsType() {
    assertEquals(List.of("\"3\" -> Expected a value of type Number | Null, but got String."), check("[/Root/Count] == \"3\""));
    assertEquals(List.of("[/Root/Name] -> Expected a field of one of the types: NumberType, TimeType, DateType, DateTimeType, "
        + "DateFragmentType, DateRangeType, but got a field of type StringType."), check("[/Root/Name] >= 3"));
  }

  @Test
  void anEnumerationValueMustBeDeclaredAndSuggestsTheClosest() {
    assertEquals(List.of("\"Opne\" -> Invalid enumeration value for field [/Root/State]. Do you mean \"Open\", or \"Closed\"?"),
        check("[/Root/State] == \"Opne\""));
  }

  @Test
  void numbersDatesAndRangesAreValidated() {
    assertEquals(List.of("Date(30, 2, 2024) -> Invalid date: 2024-2-30 (YYYY-MM-DD). The date does not exist."),
        check("[/Root/Due] == Date(30, 2, 2024)"));
    assertEquals(List.of("13 -> Expected a number less than or equal to 12, but got 13."), check("[/Root/Due] == Date(1, 13, 2024)"));
    assertEquals(List.of("1.5 -> Expected an integer, but got 1.5."), check("[/Root/Due] == Date(1.5, 1, 2024)"));
    assertEquals(List.of("1 -> The 'to' value must be greater than the 'from' value in a range."), check("InRange([/Root/Count], 5, 1)"));
  }

  @Test
  void functionsAndArgumentCountsAreChecked() {
    assertEquals(List.of("Foo([/Root/Name]) -> Unknown function name: Foo.", "Foo([/Root/Name]) -> Unknown function name: Foo."),
        check("Foo([/Root/Name])"));
    assertEquals(List.of("InRange([/Root/Count], 1) -> Function InRange expects exactly 3 arguments, but got 2."),
        check("InRange([/Root/Count], 1)"));
    assertEquals(List.of("Date(1, 1, 2024) -> Expected a value of type BooleanExpression, but got Date."), check("Date(1, 1, 2024)"));
  }

  @Test
  void anEmptyStringIsRejected() {
    assertEquals(List.of("\"\" -> String literal can not be empty. In comparison operators, please compare to Null instead"),
        check("[/Root/Name] == \"\""));
  }

  @Test
  void aBindingProblemIsReportedAloneAndSyntaxProblemsAreLeftToTheSyntaxValidator() {
    List<String> findings = check("[/Root/Missing] == 3 and [/Root/Name] >= 1");

    assertEquals(1, findings.size());
    assertTrue(findings.get(0).startsWith("[/Root/Missing] -> Field with path [/Root/Missing] not found in document model Task_DM."),
        findings.get(0));
    assertEquals(List.of(), check("[/Root/Name] ==="));
    assertEquals(List.of(), check(" "));
    assertEquals(List.of(), check(null));
  }

  @Test
  void checkAllShowsTypeFindingsOnlyWhenTheReferencesAreFine() {
    QueryFilterReferenceChecker references = new QueryFilterReferenceChecker(new Models(List.of(MODEL), List.of()));

    assertEquals(List.of(), references.check("[/Root/Name] >= 1", MODEL));
    assertEquals(1, references.checkAll("[/Root/Name] >= 1", MODEL).size());
    assertEquals(references.check("[/Root/Missing] == 1", MODEL), references.checkAll("[/Root/Missing] == 1", MODEL));
  }
}
