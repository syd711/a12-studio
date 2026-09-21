package de.a12.studio.modelsvalidation.validators;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.documentmodel.ComputationAlternative;
import de.a12.studio.models.documentmodel.ComputationConfig;
import de.a12.studio.models.documentmodel.ComputationElement;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.documentmodel.DocumentModelContent;
import de.a12.studio.models.documentmodel.GroupConfig;
import de.a12.studio.models.documentmodel.GroupElement;
import de.a12.studio.models.documentmodel.ModelRoot;
import de.a12.studio.models.documentmodel.RuleConfig;
import de.a12.studio.models.documentmodel.RuleElement;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.modelsvalidation.ElementProperty;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.TestModels;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RuleConditionSyntaxValidatorTest {

  private static final String VALID = "GroupFilled(RuleGroup) AND FieldNotFilled(Name)";

  private static final String BROKEN = "GroupFilled(RuleGroup";

  @Test
  void reportsABrokenRuleCondition() {
    RuleElement broken = rule("rule1", BROKEN);
    RuleElement fine = rule("rule2", VALID);

    List<ModelValidationError> errors = validate(broken, fine);

    assertEquals(1, errors.size());
    assertEquals("rule1", errors.get(0).elementId());
    assertEquals(ElementProperty.RULE_PROPERTIES, errors.get(0).property());
    assertTrue(errors.get(0).message().contains("MVK_INCOMPLETE_INPUT"), errors.get(0).message());
  }

  @Test
  void reportsEveryBrokenPartOfAComputationAndNamesIt() {
    ComputationAlternative first = alternative(VALID, VALID);
    ComputationAlternative second = alternative(BROKEN, BROKEN);
    ComputationElement computation = computation("comp1", BROKEN, first, second);

    List<ModelValidationError> errors = validate(computation);

    assertEquals(3, errors.size(), errors.toString());
    assertTrue(errors.stream().allMatch(e -> "comp1".equals(e.elementId())
        && ElementProperty.COMPUTATION_PROPERTIES.equals(e.property())));
    assertTrue(errors.get(0).message().contains("common precondition"), errors.get(0).message());
    assertTrue(errors.get(1).message().contains("precondition of computation alternative 2"), errors.get(1).message());
    assertTrue(errors.get(2).message().contains("operation of computation alternative 2"), errors.get(2).message());
  }

  @Test
  void blankAndAbsentConditionsAreNotSyntaxErrors() {
    ComputationAlternative noPrecondition = alternative(null, VALID);
    ComputationAlternative blank = alternative("  ", "");
    RuleElement blankRule = rule("rule1", "");

    assertEquals(List.of(), validate(computation("comp1", null, noPrecondition, blank), blankRule));
  }

  /** SME-authored models are valid by construction, so the syntax check must not be stricter than the kernel. */
  @Test
  void realDocumentModelsHaveNoSyntaxErrors() throws IOException {
    Path workspaces = locateWorkspaces();
    List<String> problems = new ArrayList<>();
    int documentModels = 0;
    try (Stream<Path> walk = Files.walk(workspaces)) {
      for (Path file : walk.filter(Files::isRegularFile).filter(p -> p.getFileName().toString().endsWith(".json")).sorted().toList()) {
        A12Model<?> loaded = new ProjectItem(file.toFile()).getModel();
        if (!(loaded instanceof DocumentModel model)) {
          continue;
        }
        documentModels++;
        for (ModelValidationError error : new RuleConditionSyntaxValidator().validate(model, TestModels.context(model))) {
          problems.add(workspaces.relativize(file) + " [" + error.elementId() + "]: " + error.message());
        }
      }
    }
    assertTrue(documentModels > 0, "no fixture document models found under " + workspaces);
    assertEquals(List.of(), problems);
  }

  private static RuleElement rule(String id, String errorCondition) {
    RuleConfig config = new RuleConfig();
    config.setErrorCondition(errorCondition);
    RuleElement element = new RuleElement();
    element.setId(id);
    element.setName(id);
    element.setRule(config);
    return element;
  }

  private static ComputationAlternative alternative(String precondition, String operation) {
    ComputationAlternative alternative = new ComputationAlternative();
    alternative.setPrecondition(precondition);
    alternative.setOperation(operation);
    return alternative;
  }

  private static ComputationElement computation(String id, String commonPrecondition, ComputationAlternative... alternatives) {
    ComputationConfig config = new ComputationConfig();
    config.setCommonPrecondition(commonPrecondition);
    config.getComputationAlternatives().addAll(List.of(alternatives));
    ComputationElement element = new ComputationElement();
    element.setId(id);
    element.setName(id);
    element.setComputation(config);
    return element;
  }

  private static List<ModelValidationError> validate(de.a12.studio.models.documentmodel.Element... elements) {
    GroupConfig config = new GroupConfig();
    config.getElements().addAll(List.of(elements));
    GroupElement root = new GroupElement();
    root.setId("root");
    root.setName("Root");
    root.setGroup(config);
    ModelRoot modelRoot = new ModelRoot();
    modelRoot.getRootGroups().add(root);
    DocumentModelContent content = new DocumentModelContent();
    content.setModelRoot(modelRoot);
    DocumentModel model = new DocumentModel();
    model.setId("Rules_DM");
    model.setContent(content);
    return new RuleConditionSyntaxValidator().validate(model, TestModels.context(model));
  }

  private static Path locateWorkspaces() {
    for (Path dir = Path.of("").toAbsolutePath(); dir != null; dir = dir.getParent()) {
      Path candidate = dir.resolve("testing").resolve("workspaces");
      if (Files.isDirectory(candidate)) {
        return candidate;
      }
    }
    throw new IllegalStateException("Could not locate 'testing/workspaces' above " + Path.of("").toAbsolutePath());
  }
}
