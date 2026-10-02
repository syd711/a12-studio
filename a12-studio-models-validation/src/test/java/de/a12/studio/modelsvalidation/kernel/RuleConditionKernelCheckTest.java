package de.a12.studio.modelsvalidation.kernel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.documentmodel.Element;
import de.a12.studio.models.documentmodel.GroupElement;
import de.a12.studio.models.projects.ProjectItem;

class RuleConditionKernelCheckTest {

  private static final String COMPANY_RULE = "rule_b3f12";
  private static final String COMPANY_CONDITION = "GroupFilled(RuleGroup) and FieldNotFilled(internal_filename)";

  private static ProjectItem workspace(String name) {
    Path dir = Path.of("").toAbsolutePath();
    while (dir != null && !Files.isDirectory(dir.resolve("testing/workspaces").resolve(name))) {
      dir = dir.getParent();
    }
    if (dir == null) {
      throw new IllegalStateException("testing/workspaces/" + name + " not found");
    }
    return new ProjectItem(dir.resolve("testing/workspaces").resolve(name).toFile());
  }

  @Test
  void aValidConditionHasNoProblem() {
    RuleConditionKernelCheck check = new RuleConditionKernelCheck(workspace("basic"), "Company_DM", COMPANY_RULE);

    assertNull(check.check(COMPANY_CONDITION));
  }

  @Test
  void anUnknownFieldIsReportedWithItsPositionAndTheKernelErrorCode() {
    RuleConditionKernelCheck check = new RuleConditionKernelCheck(workspace("basic"), "Company_DM", COMPANY_RULE);

    String message = check.check("GroupFilled(RuleGroup) and FieldNotFilled(DoesNotExist)");

    assertNotNull(message);
    assertTrue(message.contains("MVK_INVALID_ENTITY"), message);
    assertTrue(message.matches("(?s).*[(]line 1, column [0-9]+[)]$"), message);
  }

  @Test
  void aSyntaxErrorOnASecondLineIsReportedOnThatLine() {
    RuleConditionKernelCheck check = new RuleConditionKernelCheck(workspace("basic"), "Company_DM", COMPANY_RULE);

    String message = check.check("GroupFilled(RuleGroup)\nand nonsense");

    assertNotNull(message);
    assertTrue(message.contains("MVK_UNEXPECTED_TOKEN"), message);
    assertTrue(message.contains("(line 2, column "), message);
  }

  @Test
  void theCheckIsReusableAcrossKeystrokes() {
    RuleConditionKernelCheck check = new RuleConditionKernelCheck(workspace("basic"), "Company_DM", COMPANY_RULE);

    assertNotNull(check.check("nonsense nonsense"));
    assertNull(check.check(COMPANY_CONDITION));
    assertNotNull(check.check("FieldNotFilled(Nope)"));
  }

  @Test
  void unsavedEditsOfTheModelAreVisibleToTheKernel() {
    ProjectItem project = workspace("basic");
    DocumentModel company = (DocumentModel) project.findByModelId("Company_DM").getModel();
    // rule_79495 (error field "content") refers to attachment_id in its condition; renaming that field in memory
    // only must make the condition's reference unknown. (Renaming a rule's own error field would instead make the
    // whole model inconsistent, and the check then deliberately gives no opinion.)
    Element field = find(company.getContent().getModelRoot().getRootGroups(), "attachment_id", Element.class);
    assertNotNull(field, "fixture changed, adjust the test (field not found)");
    field.setName("renamed_in_memory");

    RuleConditionKernelCheck check = new RuleConditionKernelCheck(project, "Company_DM", "rule_79495");

    String message = check.check("GroupFilled(RuleGroup) and NotExactlyOneFieldFilled(attachment_id, content)");
    assertNotNull(message);
    assertTrue(message.contains("MVK_INVALID_ENTITY"), message);
  }

  @Test
  void aHalfEditedModelGivesNoOpinionInsteadOfAnError() {
    ProjectItem project = workspace("basic");
    DocumentModel company = (DocumentModel) project.findByModelId("Company_DM").getModel();
    // The rule's own error field disappears: the kernel refuses the model, the editor must stay quiet.
    find(company.getContent().getModelRoot().getRootGroups(), "internal_filename", Element.class).setName("renamed");

    assertNull(new RuleConditionKernelCheck(project, "Company_DM", COMPANY_RULE).check("nonsense nonsense"));
  }

  @Test
  void anUnknownRuleOrModelGivesNoOpinionInsteadOfAnError() {
    ProjectItem project = workspace("basic");

    assertNull(new RuleConditionKernelCheck(project, "Company_DM", "no_such_rule").check("nonsense nonsense"));
    assertNull(new RuleConditionKernelCheck(project, "No_Such_DM", COMPANY_RULE).check("nonsense nonsense"));
  }

  @Test
  void theWarmUpModelIsAValidModelTheKernelAccepts() {
    assertTrue(RuleConditionKernelCheck.warmUp());
  }

  @Test
  void describeShowsOneBasedLineAndColumn() {
    String text = RuleConditionKernelCheck.describe(new de.a12.studio.kernel.KernelProblem(2, 16, 24, "boom"));

    assertEquals("boom (line 2, column 17)", text);
  }

  private static Element find(java.util.List<? extends Element> elements, String name, Class<? extends Element> type) {
    for (Element element : elements) {
      if (name.equals(element.getName()) && type.isInstance(element)) {
        return element;
      }
      if (element instanceof GroupElement group && group.getGroup() != null && group.getGroup().getElements() != null) {
        Element found = find(group.getGroup().getElements(), name, type);
        if (found != null) {
          return found;
        }
      }
    }
    return null;
  }
}
