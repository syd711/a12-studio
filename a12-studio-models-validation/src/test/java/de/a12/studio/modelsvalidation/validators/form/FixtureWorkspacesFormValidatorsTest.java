package de.a12.studio.modelsvalidation.validators.form;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.formmodel.FormModel;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.TestModels;
import de.a12.studio.modelsvalidation.validators.ModelValidator;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The style-preset, default-row-action, date-picker-range, column-width, initial-focus, custom-element-height,
 * dependent-control and dependent-field validators are strict checks; the real form models of the
 * sample workspaces (authored in SME, so valid by construction) must not trip them. Guards against the rules
 * being stricter than SME's.
 */
class FixtureWorkspacesFormValidatorsTest {

  @Test
  void realFormModelsHaveNoErrorsFromTheStrictReferenceAndRangeValidators() throws IOException {
    Path workspaces = locateWorkspaces();
    List<ModelValidator> validators = List.of(new FormStyleReferenceValidator(), new FormDefaultRowActionValidator(),
        new FormDatePickerConfigValidator(), new FormColumnWidthValidator(),
        new FormInitiallyFocusedElementValidator(), new FormCustomScreenElementHeightValidator(),
        new DependentControlOptionsMustExistValidator(), new DependentControlsAtLeastOneOptionValidator(),
        new DependentFieldAtLeastOneActionValidator());
    List<String> problems = new ArrayList<>();
    int formModels = 0;
    try (Stream<Path> walk = Files.walk(workspaces)) {
      for (Path file : walk.filter(Files::isRegularFile).filter(p -> p.getFileName().toString().endsWith(".json")).sorted().toList()) {
        if (!(new ProjectItem(file.toFile()).getModel() instanceof FormModel model)) {
          continue;
        }
        formModels++;
        for (ModelValidator validator : validators) {
          for (ModelValidationError error : validator.validate(model, TestModels.context(model))) {
            problems.add(workspaces.relativize(file) + ": " + error.message());
          }
        }
      }
    }
    assertTrue(formModels > 0, "no fixture form models found under " + workspaces);
    assertEquals(List.of(), problems);
  }

  private static final String KNOWN_STALE_CASE =
      "has a case for the value \"false\" of the master field \"field_69593\"";

  /**
   * The drift validators compare a Form Model against its Document Model, so unlike the strict validators above they
   * need the sibling models of the workspace. SME-authored forms and their Document Models are consistent by
   * construction; a report here is either a bug in the validator or a genuinely stale fixture.
   */
  @Test
  void realFormModelsHaveNoDriftAgainstTheirDocumentModels() throws IOException {
    Path workspaces = locateWorkspaces();
    List<ModelValidator> validators = List.of(new FormDependencyDriftValidator(), new FormDependentControlContextValidator(),
        new FormReferenceTypeDriftValidator(), new FormControlIndexRequiredValidator());
    List<String> problems = new ArrayList<>();
    int formModels = 0;
    int withDocumentModel = 0;
    try (Stream<Path> workspaceDirs = Files.list(workspaces)) {
      for (Path workspace : workspaceDirs.filter(Files::isDirectory).sorted().toList()) {
        List<Path> files;
        try (Stream<Path> walk = Files.walk(workspace)) {
          files = walk.filter(Files::isRegularFile).filter(p -> p.getFileName().toString().endsWith(".json")).sorted().toList();
        }
        List<Path> modelFiles = new ArrayList<>();
        List<A12Model<?>> models = new ArrayList<>();
        for (Path file : files) {
          A12Model<?> loaded = new ProjectItem(file.toFile()).getModel();
          if (loaded != null) {
            modelFiles.add(file);
            models.add(loaded);
          }
        }
        for (int i = 0; i < models.size(); i++) {
          if (!(models.get(i) instanceof FormModel model)) {
            continue;
          }
          formModels++;
          if (model.getModelReferences() != null && !model.getModelReferences().isEmpty()) {
            withDocumentModel++;
          }
          for (ModelValidator validator : validators) {
            for (ModelValidationError error : validator.validate(model, TestModels.contextWithOtherModels(model, models.toArray(new A12Model<?>[0])))) {
              problems.add(workspaces.relativize(modelFiles.get(i)) + " [" + validator.getClass().getSimpleName() + "]: " + error.message());
            }
          }
        }
      }
    }
    assertTrue(formModels > 0 && withDocumentModel > 0, "no fixture form models with a document model found under " + workspaces);
    // A genuine finding, not a false positive: advanced_new's City_Fm.json has a dependent group case for the value
    // "false" on HelperDistrict, which is a Confirm field (only "true" or no value) - a leftover of the field having
    // been a Boolean. Pinned so the exemption cannot outlive the fixture being fixed.
    assertTrue(problems.removeIf(problem -> problem.startsWith("advanced_new") && problem.contains("City_Fm.json")
        && problem.contains(KNOWN_STALE_CASE)), "the known stale case in City_Fm.json is gone - drop the exemption");
    assertEquals(List.of(), problems);
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
