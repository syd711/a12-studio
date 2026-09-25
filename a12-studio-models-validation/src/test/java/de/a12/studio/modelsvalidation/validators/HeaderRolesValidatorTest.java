package de.a12.studio.modelsvalidation.validators;

import de.a12.studio.models.Annotation;
import de.a12.studio.models.projects.Project;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.models.typesettingmodel.TypesettingModel;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.TestModels;
import de.a12.studio.modelsvalidation.ValidationContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** The {@code roles} header annotation rules, with and without a workspace {@code auth/roles.yaml} to check against. */
class HeaderRolesValidatorTest {

  private final HeaderRolesValidator validator = new HeaderRolesValidator();

  @Test
  void aModelWithoutRolesHasNoProblem() {
    TypesettingModel model = model(null);

    assertTrue(validator.validate(model, TestModels.context(model)).isEmpty());
  }

  @Test
  void validRolesHaveNoProblem() {
    TypesettingModel model = model("admin,sys-Admin,_internal,user.1");

    assertTrue(validator.validate(model, TestModels.context(model)).isEmpty());
  }

  @Test
  void spacesAroundARoleAreTolerated() {
    TypesettingModel model = model("admin, guest");

    assertTrue(validator.validate(model, TestModels.context(model)).isEmpty());
  }

  @Test
  void reportsARoleWithInvalidCharactersOrStart() {
    for (String invalid : List.of("1admin", "-admin", "ad min", "adm@in")) {
      TypesettingModel model = model("guest," + invalid);
      List<ModelValidationError> errors = validator.validate(model, TestModels.context(model));

      assertEquals(1, errors.size(), invalid);
      assertEquals("ERROR", errors.get(0).severity());
      assertEquals(HeaderRolesValidator.ELEMENT_ID, errors.get(0).elementId());
      assertTrue(errors.get(0).message().contains(invalid), errors.get(0).message());
    }
  }

  @Test
  void reportsADuplicateRoleOnce() {
    TypesettingModel model = model("admin,guest,admin,admin");
    List<ModelValidationError> errors = validator.validate(model, TestModels.context(model));

    assertEquals(1, errors.size());
    assertTrue(errors.get(0).message().contains("admin"));
  }

  @Test
  void reportsAnEmptyRoleOnce() {
    for (String empty : new String[] {"", " ", "admin,,guest", "admin,", ",guest,,"}) {
      TypesettingModel model = model(empty);

      assertEquals(1, validator.validate(model, TestModels.context(model)).size(), "'" + empty + "'");
    }
  }

  @Test
  void warnsAboutRolesMissingFromTheWorkspaceRolesFile(@TempDir Path tempDir) throws Exception {
    writeRolesFile(tempDir, "admin", "guest");
    TypesettingModel model = model("admin,tester");

    List<ModelValidationError> errors = validator.validate(model, contextIn(tempDir, model));

    assertEquals(1, errors.size());
    assertEquals("WARNING", errors.get(0).severity());
    assertTrue(errors.get(0).message().contains("tester"));
  }

  @Test
  void rolesFromTheWorkspaceRolesFileAreFine(@TempDir Path tempDir) throws Exception {
    writeRolesFile(tempDir, "admin", "guest");
    TypesettingModel model = model("guest,admin");

    assertTrue(validator.validate(model, contextIn(tempDir, model)).isEmpty());
  }

  @Test
  void warnsWhenTheWorkspaceHasRolesButTheModelHasNone(@TempDir Path tempDir) throws Exception {
    writeRolesFile(tempDir, "admin");
    TypesettingModel model = model(null);

    List<ModelValidationError> errors = validator.validate(model, contextIn(tempDir, model));

    assertEquals(1, errors.size());
    assertEquals("WARNING", errors.get(0).severity());
  }

  @Test
  void warnsAboutRolesWithoutAWorkspaceRolesFile(@TempDir Path tempDir) {
    TypesettingModel model = model("admin");

    List<ModelValidationError> errors = validator.validate(model, contextIn(tempDir, model));

    assertEquals(1, errors.size());
    assertEquals("WARNING", errors.get(0).severity());
  }

  @Test
  void noProjectMeansNoWorkspaceChecks() {
    TypesettingModel model = model("anything-goes");

    assertTrue(validator.validate(model, TestModels.context(model)).isEmpty());
  }

  private static TypesettingModel model(String roles) {
    TypesettingModel model = new TypesettingModel();
    model.setId("Test_TSM");
    if (roles != null) {
      Annotation annotation = new Annotation();
      annotation.setName("roles");
      annotation.setValue(roles);
      model.getAnnotations().add(annotation);
    }
    return model;
  }

  private static ValidationContext contextIn(Path projectDir, TypesettingModel model) {
    Project project = new Project();
    project.load(projectDir.toFile());
    return new ValidationContext(project, new ProjectItem(new File("Test_TSM.json")), List.of(), List.of(), model);
  }

  private static void writeRolesFile(Path projectDir, String... roleNames) throws Exception {
    Path authFolder = Files.createDirectories(projectDir.resolve("auth"));
    StringBuilder yaml = new StringBuilder("roles:\n");
    for (String roleName : roleNames) {
      yaml.append("  - name: ").append(roleName).append('\n');
    }
    Files.writeString(authFolder.resolve("roles.yaml"), yaml, StandardCharsets.UTF_8);
  }
}
