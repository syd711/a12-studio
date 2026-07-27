package de.a12.studio.modelsvalidation;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.projects.Project;
import de.a12.studio.models.projects.ProjectItem;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Guards the {@link ValidationService} dispatch switch: every model type placed in a real (temp)
 * project must reach its type-specific validation service, visible through a type-specific error.
 */
class ValidationServiceDispatchTest {

  @Test
  void dispatchesEveryModelTypeToItsService(@TempDir Path tempDir) throws Exception {
    copyFixture("/relationshipmodel/RelationshipEntityCountValidator_invalid.json", tempDir);
    copyFixture("/treemodel/TreeNodesNotEmptyValidator_invalid.json", tempDir);
    copyFixture("/printmodel/PrintImageValidator_invalid.json", tempDir);
    copyFixture("/contentmodel/ContentRootElementValidator_invalid.json", tempDir);
    copyFixture("/masterdetailmodel/MasterDetailTypeConsistencyValidator_invalid.json", tempDir);

    Project project = new Project();
    project.load(tempDir.toFile());
    ValidationService validationService = new ValidationService(project);

    assertServiceReached(validationService, project, "RelationshipEntityCountValidator_invalid.json", "content/entityCharacteristics");
    assertServiceReached(validationService, project, "TreeNodesNotEmptyValidator_invalid.json", "content/nodes");
    assertServiceReached(validationService, project, "PrintImageValidator_invalid.json", "content/elementDefinitions/image");
    assertServiceReached(validationService, project, "ContentRootElementValidator_invalid.json", "content/root");
    assertServiceReached(validationService, project, "MasterDetailTypeConsistencyValidator_invalid.json", "content/type");
  }

  private void assertServiceReached(ValidationService validationService, Project project, String fileName, String expectedElementId) {
    ProjectItem item = findItem(project.getRoot(), fileName);
    assertNotNull(item, "Project item not found: " + fileName);
    A12Model<?> model = item.getModel();
    assertNotNull(model, "Model failed to load: " + fileName);

    List<ModelValidationError> errors = validationService.validate(model);
    assertFalse(errors.isEmpty(), "Expected validation errors for " + fileName);
    assertTrue(errors.stream().anyMatch(error -> expectedElementId.equals(error.elementId())),
        "Expected an error with element id " + expectedElementId + " for " + fileName + ", got: "
            + errors.stream().map(ModelValidationError::elementId).toList());
  }

  private ProjectItem findItem(ProjectItem item, String fileName) {
    if (item.isFolder()) {
      for (ProjectItem child : item.getChildren()) {
        ProjectItem found = findItem(child, fileName);
        if (found != null) {
          return found;
        }
      }
      return null;
    }
    return fileName.equals(item.getName()) ? item : null;
  }

  private void copyFixture(String resourcePath, Path targetDir) throws Exception {
    String fileName = resourcePath.substring(resourcePath.lastIndexOf('/') + 1);
    try (InputStream in = getClass().getResourceAsStream(resourcePath)) {
      assertNotNull(in, "Missing test resource: " + resourcePath);
      Files.copy(in, targetDir.resolve(fileName));
    }
  }
}
