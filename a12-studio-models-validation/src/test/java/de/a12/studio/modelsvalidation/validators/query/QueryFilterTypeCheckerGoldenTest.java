package de.a12.studio.modelsvalidation.validators.query;

import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.relationshipmodel.RelationshipModel;
import de.a12.studio.models.util.JsonSettings;
import de.a12.studio.modelsvalidation.validators.query.QueryFilterReferenceChecker.Models;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Replays SME's own checker snapshots ({@code moduleSupport/qmm/test/__snapshots__/core/checker/*.snap}: a filter
 * and the exact messages and source ranges SME reports for it, against SME's test workspace with {@code Team_DM}
 * as the current model) through {@link QueryFilterTypeChecker}. Syntax errors differ by parser and are not
 * compared; every other expectation must match message for message. Needs the SME checkout - set the system
 * property {@code sme.qmm.dir} or the environment variable {@code SME_QMM_DIR} (default {@code
 * C:\workspace\sme\moduleSupport\qmm}); skipped when it is not there.
 */
class QueryFilterTypeCheckerGoldenTest {

  private static final Pattern ENTRY = Pattern.compile("exports\\[`(.*?)`\\] = `(.*?)`;\\n", Pattern.DOTALL);
  private static final Pattern MODEL_TYPE = Pattern.compile("\"modelType\"\\s*:\\s*\"(\\w+)\"");
  private static final Pattern ERROR_INPUT = Pattern.compile("^\\s+\"errorInput\": \"(.*)\",$");
  private static final Pattern MESSAGE = Pattern.compile("^\\s+\"message\": \"(.*)\",$");

  private static Path smeDirectory() {
    String configured = System.getProperty("sme.qmm.dir", System.getenv("SME_QMM_DIR"));
    return Path.of(configured != null ? configured : "C:\\workspace\\sme\\moduleSupport\\qmm");
  }

  @Test
  void matchesSmesCheckerSnapshots() throws IOException {
    Path directory = smeDirectory();
    Path snapshots = directory.resolve("test/__snapshots__/core/checker");
    Path models = directory.resolve("resources/workspace/models");
    Assumptions.assumeTrue(Files.isDirectory(snapshots) && Files.isDirectory(models), "SME checkout not found: " + directory);

    World world = World.load(models);
    DocumentModel team = world.team();
    QueryFilterTypeChecker checker = world.checker(id -> true, id -> true);
    ResourceBundle english = english();

    int compared = 0;
    int withFindings = 0;
    List<String> failures = new ArrayList<>();
    try (Stream<Path> snapshotFiles = Files.list(snapshots)) {
      for (Path snapshot : snapshotFiles.filter(path -> path.toString().endsWith(".snap")).sorted().toList()) {
        Matcher entries = ENTRY.matcher(Files.readString(snapshot, StandardCharsets.UTF_8).replace("\r\n", "\n"));
        while (entries.find()) {
          String input = inputOf(entries.group(1));
          List<String[]> expected = expectedOf(entries.group(2));
          if (input == null || expected == null) {
            continue;
          }
          compared++;
          if (!expected.isEmpty()) {
            withFindings++;
          }
          List<String[]> actual = new ArrayList<>();
          for (QlDiagnostic diagnostic : checker.check(input, team)) {
            actual.add(new String[] {diagnostic.message(english), slice(input, diagnostic.start(), diagnostic.stop())});
          }
          if (!same(expected, actual)) {
            failures.add(snapshot.getFileName() + "\n  input:    " + input + "\n  expected: " + render(expected) + "\n  actual:   " + render(actual));
          }
        }
      }
    }

    assertTrue(compared > 1000, "Only " + compared + " snapshot entries were compared");
    assertTrue(withFindings > 1000, "Only " + withFindings + " snapshot entries with findings were compared");
    assertTrue(failures.isEmpty(), failures.size() + " of " + compared + " differ, first:\n"
        + String.join("\n", failures.subList(0, Math.min(15, failures.size()))));
  }

  private static ResourceBundle english() {
    return ResourceBundle.getBundle("validation-messages", Locale.ENGLISH,
        ResourceBundle.Control.getNoFallbackControl(ResourceBundle.Control.FORMAT_DEFAULT));
  }

  /** SME's test workspace; {@code checker} drops the models for which a predicate says so ({@code deleteModels}). */
  private record World(List<DocumentModel> documentModels, List<RelationshipModel> relationshipModels) {

    static World load(Path models) throws IOException {
      List<DocumentModel> documentModels = new ArrayList<>();
      List<RelationshipModel> relationshipModels = new ArrayList<>();
      try (Stream<Path> files = Files.walk(models)) {
        for (Path file : files.filter(path -> path.toString().endsWith(".json")).toList()) {
          String json = Files.readString(file, StandardCharsets.UTF_8);
          try {
            Matcher type = MODEL_TYPE.matcher(json);
            String modelType = type.find() ? type.group(1) : "";
            if ("document".equals(modelType)) {
              documentModels.add(JsonSettings.objectMapper.readValue(json, DocumentModel.class));
            }
            else if ("relationship".equals(modelType)) {
              relationshipModels.add(JsonSettings.objectMapper.readValue(json, RelationshipModel.class));
            }
          }
          catch (Exception e) {
            // not a model of ours
          }
        }
      }
      return new World(documentModels, relationshipModels);
    }

    DocumentModel team() {
      return documentModels.stream().filter(model -> "Team_DM".equals(model.getId())).findFirst().orElseThrow();
    }

    QueryFilterTypeChecker checker(Predicate<String> keepDocumentModel, Predicate<String> keepRelationshipModel) {
      return new QueryFilterTypeChecker(new Models(
          documentModels.stream().filter(model -> keepDocumentModel.test(model.getId())).toList(),
          relationshipModels.stream().filter(model -> keepRelationshipModel.test(model.getId())).toList()));
    }
  }

  private static final String[] NONE = {};

  /** Expected findings given flat as message, errorInput, message, errorInput, ... */
  private static List<String[]> pairs(String... flat) {
    List<String[]> pairs = new ArrayList<>();
    for (int i = 0; i < flat.length; i += 2) {
      pairs.add(new String[] {flat[i], flat[i + 1]});
    }
    return pairs;
  }

  private record Case(String input, Predicate<String> keepDocumentModel, Predicate<String> keepRelationshipModel,
                      List<String[]> expected) {
  }

  /** The inline snapshots of SME's has-function, checker, match-function and logical-functions tests. */
  @Test
  void matchesSmesInlineSnapshots() throws IOException {
    Path directory = smeDirectory();
    Path models = directory.resolve("resources/workspace/models");
    Assumptions.assumeTrue(Files.isDirectory(models), "SME checkout not found: " + directory);
    World world = World.load(models);
    DocumentModel team = world.team();
    ResourceBundle english = english();
    List<String> failures = new ArrayList<>();

    Predicate<String> all = id -> true;
    List<Case> cases = new ArrayList<>();
    BiConsumer<String, String[]> ok = (input, expected) -> cases.add(new Case(input, all, all, pairs(expected)));
    String has = "Has(\"TeamPerson\", \"Person\"";

    ok.accept(has + ")", NONE);
    ok.accept(has + ", [/Person/FirstName] == \"John Doe\")", NONE);
    ok.accept(has + ", Null, [/LinkFields/TimeShare] >= 20)", NONE);
    ok.accept("Has(3, \"Person\")", new String[] {"Expected a value of type String, but got Number.", "3"});
    ok.accept("Has(\"TeamPerson\", 123)", new String[] {"Expected a value of type String, but got Number.", "123"});
    ok.accept(has + ", \"John Doe\")", new String[] {"Expected a value of type (BooleanExpression | Null)?, but got String.", "\"John Doe\""});
    ok.accept(has + ", Null, 123)", new String[] {"Expected a value of type (BooleanExpression | Null)?, but got Number.", "123"});
    ok.accept("Has(\"TeamPerson\")", new String[] {"Function Has expects at least 2 arguments, but got 1.", "Has(\"TeamPerson\")"});
    ok.accept("Has(\"Person\")", new String[] {"Function Has expects at least 2 arguments, but got 1.", "Has(\"Person\")"});
    ok.accept("Has()", new String[] {"Function Has expects at least 2 arguments, but got 0.", "Has()"});
    ok.accept(has + ", Null, Null, Null)", new String[] {"Function Has expects at most 4 arguments, but got 5.", has + ", Null, Null, Null)"});
    ok.accept("Has(\"TeamPerson\", \"Team\")", new String[] {"Invalid target role \"Team\". Expected target roles are: \"Person\".", "\"Team\""});
    ok.accept("Has(\"TeamPerson\", \"People\")", new String[] {"Invalid target role \"People\". Expected target role is \"Person\".", "\"People\""});
    ok.accept("Has(\"TeamTeam\", \"People\")", new String[] {"Invalid target role \"People\". Expected target roles are: \"Parent\" or \"Child\".", "\"People\""});
    ok.accept("Has(\"TeamCompany\", \"Company\")", new String[] {"Relationship model with name \"TeamCompany\" not found. Did you mean \"TeamCity\" or \"TeamPerson\"?", "\"TeamCompany\""});
    cases.add(new Case("Has(\"TeamCompany\", \"Company\")", all, id -> false,
        pairs("Relationship model with name \"TeamCompany\" not found.", "\"TeamCompany\"")));
    ok.accept("Has(\"TeamTeam\", \"Child\", Null, [/Test] == Null)", new String[] {
        "Relationship model \"TeamTeam\" does not have the link document model.", "[/Test] == Null",
        "Element with path [/Test] is not a field in document model Team_DM.", "[/Test]"});
    cases.add(new Case(has + ")", id -> !"Person_DM".equals(id), all,
        pairs("Document model with name \"Person_DM\" not found.", "\"Person\"")));
    cases.add(new Case(has + ", Null, [/LinkFields/TimeShare] == \"20\")", id -> !"TeamPerson_LinkFields_DM".equals(id), all,
        pairs("Link document model with name \"TeamPerson_LinkFields_DM\" not found.", "[/LinkFields/TimeShare] == \"20\"",
            "Field with path [/LinkFields/TimeShare] not found in document model Team_DM. Did you mean [/Test/TimeField] or [/Team/TeamName]?", "[/LinkFields/TimeShare]")));
    ok.accept(has + ", [/Person/FullName] == \"John Doe\")", new String[] {
        "Field with path [/Person/FullName] not found in document model Person_DM. Did you mean [/Person/FirstName] or [/Person/LastName]?", "[/Person/FullName]"});
    ok.accept(has + ", Null, [/LinkFields/TimeShare] == \"20\")", new String[] {"Expected a value of type Number | Null, but got String.", "\"20\""});
    ok.accept(has + ", Null, [/LinkFields/TimeShared] >= 20)", new String[] {
        "Field with path [/LinkFields/TimeShared] not found in document model TeamPerson_LinkFields_DM. Did you mean [/LinkFields/TimeShare] or [/LinkFields/StartDate]?", "[/LinkFields/TimeShared]"});
    ok.accept("Has(\"Team\", \"Person\", [/Person/FirstName] == \"John Doe\")", new String[] {
        "Relationship model with name \"Team\" not found. Did you mean \"TeamCity\" or \"TeamTeam\"?", "\"Team\"",
        "Field with path [/Person/FirstName] not found in document model Team_DM. Did you mean [/Team/TeamName] or [/Test/TimeField]?", "[/Person/FirstName]"});
    ok.accept("[/Team] == \"123\"", new String[] {"Element with path [/Team] is not a field in document model Team_DM.", "[/Team]"});
    ok.accept("[/Test/NonIndexed] == \"123\"", new String[] {"This field is annotated with 'indexed' = false and therefore cannot be used in Filter definitions.", "[/Test/NonIndexed]"});
    ok.accept("[/Team/TeamName] == \"\"", new String[] {"String literal can not be empty. In comparison operators, please compare to Null instead", "\"\""});

    ok.accept("Match(\"pattern\")", NONE);
    ok.accept("Match(\"hello\", \"world\")", NONE);
    ok.accept("Match([/Test/StringField], [/Test/NumberField], \"first\", \"second\")", NONE);
    ok.accept("Match([/Team/InvalidField], \"pattern\")", new String[] {
        "Field with path [/Team/InvalidField] not found in document model Team_DM. Did you mean [/Test/DateField] or [/Test/StringField]?", "[/Team/InvalidField]"});
    ok.accept("Match()", new String[] {"Missing argument(s) with String", "Match()"});
    ok.accept("Match([/Test/StringField], [/Test/NumberField])", new String[] {"Missing argument(s) with String", "Match([/Test/StringField], [/Test/NumberField])"});
    ok.accept("Match(\"pattern\", [/Test/StringField])", new String[] {"Unexpected argument with type FieldReference at this position", "[/Test/StringField]"});
    ok.accept("Match(123)", new String[] {"Expected a value of type Field or String, but got Number.", "123"});
    ok.accept("Match([/Test/StringField], \"pattern\", Null)", new String[] {"Expected a value of type Field or String, but got Null.", "Null"});
    ok.accept("Match(Date(1, 1, 2024))", new String[] {"Expected a value of type Field or String, but got Date.", "Date(1, 1, 2024)"});
    ok.accept("Match([/Test/StringField], 123, \"pattern\", [/Test/NumberField])", new String[] {"Expected a value of type Field or String, but got Number.", "123"});

    ok.accept("[/Test/StringField] == \"a\" or [/Test/StringField] == \"b\" or [/Test/StringField] == \"c\"", NONE);
    ok.accept("[/Test/StringField] != \"a\" and [/Test/StringField] != \"b\"", NONE);
    ok.accept("[/Test/StringField] == \"a\" or Date(1,2,2003)", new String[] {"Expected a value of type BooleanExpression, but got Date.", "Date(1,2,2003)"});
    ok.accept("[/Test/StringField] == \"a\" and Date(1,2,2003)", new String[] {"Expected a value of type BooleanExpression, but got Date.", "Date(1,2,2003)"});

    for (Case testCase : cases) {
      List<String[]> actual = new ArrayList<>();
      QueryFilterTypeChecker checker = world.checker(testCase.keepDocumentModel(), testCase.keepRelationshipModel());
      for (QlDiagnostic diagnostic : checker.check(testCase.input(), team)) {
        actual.add(new String[] {diagnostic.message(english), slice(testCase.input(), diagnostic.start(), diagnostic.stop())});
      }
      if (!same(testCase.expected(), actual)) {
        failures.add(testCase.input() + "\n  expected: " + render(testCase.expected()) + "\n  actual:   " + render(actual));
      }
    }
    assertTrue(failures.isEmpty(), failures.size() + " of " + cases.size() + " differ:\n" + String.join("\n", failures));
  }

  /** The filter a snapshot name stands for: {@code ... > Input = "<filter>" 1} or {@code ... > input = <filter> 1}. */
  private static String inputOf(String name) {
    int upper = name.indexOf("Input = \"");
    if (upper >= 0) {
      String rest = name.substring(upper + "Input = \"".length());
      return rest.endsWith("\" 1") ? rest.substring(0, rest.length() - 3) : null;
    }
    int lower = name.indexOf("input = ");
    if (lower >= 0) {
      String rest = name.substring(lower + "input = ".length());
      return rest.endsWith(" 1") ? rest.substring(0, rest.length() - 2) : null;
    }
    return null;
  }

  /** {message, errorInput} per reported problem; null if the snapshot is a syntax error list. */
  private static List<String[]> expectedOf(String body) {
    List<String[]> result = new ArrayList<>();
    String errorInput = null;
    for (String line : body.split("\\n")) {
      if (line.contains("\"code\":") || line.contains("\"key\":")) {
        return null;
      }
      Matcher input = ERROR_INPUT.matcher(line);
      if (input.matches()) {
        errorInput = input.group(1);
        continue;
      }
      Matcher message = MESSAGE.matcher(line);
      if (message.matches()) {
        result.add(new String[] {message.group(1), errorInput});
      }
    }
    return result;
  }

  private static String slice(String input, int start, int stop) {
    return input.substring(input.offsetByCodePoints(0, start), input.offsetByCodePoints(0, stop + 1));
  }

  private static boolean same(List<String[]> expected, List<String[]> actual) {
    if (expected.size() != actual.size()) {
      return false;
    }
    for (int i = 0; i < expected.size(); i++) {
      if (!expected.get(i)[0].equals(actual.get(i)[0]) || !expected.get(i)[1].equals(actual.get(i)[1])) {
        return false;
      }
    }
    return true;
  }

  private static String render(List<String[]> entries) {
    return entries.stream().map(entry -> entry[1] + " -> " + entry[0]).toList().toString();
  }
}
