package de.a12.studio.modelsvalidation.kernel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;

import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.documentmodel.Element;
import de.a12.studio.models.documentmodel.RuleElement;
import de.a12.studio.models.projects.Project;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.ValidationService;

class KernelConditionValidatorTest {

  private static Project workspace() {
    Path dir = Path.of("").toAbsolutePath();
    while (dir != null && !Files.isDirectory(dir.resolve("testing/workspaces/basic"))) {
      dir = dir.getParent();
    }
    Project project = new Project();
    project.load(dir.resolve("testing/workspaces/basic").toFile());
    return project;
  }

  private static List<ModelValidationError> kernelErrors(ValidationService service, DocumentModel model) {
    return service.validate(model).stream().filter(e -> e.message().contains("does not fit the model")).toList();
  }

  @Test
  void aRuleThatReferencesAnUnknownFieldIsReportedAndTheVerdictFollowsTheEdit() {
    Project project = workspace();
    ProjectItem item = project.getRoot().findByModelId("Company_DM");
    DocumentModel model = (DocumentModel) item.getModel();
    ValidationService service = new ValidationService(project);
    assertTrue(kernelErrors(service, model).isEmpty(), "the fixture is consistent");

    RuleElement rule = (RuleElement) new de.a12.studio.modelsvalidation.validators.ElementIndex(model).allElements().stream()
        .filter(e -> e instanceof RuleElement r && r.getRule() != null && r.getRule().getErrorCondition() != null).findFirst().orElseThrow();
    String original = rule.getRule().getErrorCondition();
    rule.getRule().setErrorCondition("FieldNotFilled(DoesNotExist)");

    List<ModelValidationError> errors = kernelErrors(service, model);
    assertEquals(1, errors.size(), errors.toString());
    assertEquals(rule.getId(), errors.get(0).elementId());

    rule.getRule().setErrorCondition(original);
    assertTrue(kernelErrors(service, model).isEmpty(), "the cache is keyed on the content, not the instance");
  }
}
