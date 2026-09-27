package de.a12.studio.modelsvalidation.validators.query;

import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.models.querymodel.QueryModel;
import de.a12.studio.models.querymodel.QueryModelContent;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.ValidationContext;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * A Query Model may target a Combination Model, not just a plain Document Model (the same "reference may name a
 * combination" rule Overview/Content Model already follow) - {@code PersonEmployee_Cm} of the {@code advanced_new}
 * workspace (base {@code Person_Dc} + additive {@code PersonEmployee_Ad}) stands in for the target here.
 * {@link QueryTargetDocumentModelRequiredValidator} already accepted this (it only checks for dangling references);
 * this pins the part that used to be missing: {@link QueryElementResolution#targetDocumentModel} resolving the
 * combination's synthetic merge so field/sort/filter/aggregation validators (and the editor's own Model Tree tab
 * and target picker) actually see its fields.
 */
class QueryModelCombinationTargetTest {

  private static ProjectItem workspace() {
    for (Path dir = Path.of("").toAbsolutePath(); dir != null; dir = dir.getParent()) {
      Path candidate = dir.resolve("testing").resolve("workspaces").resolve("advanced_new");
      if (Files.isDirectory(candidate)) {
        return new ProjectItem(candidate.toFile());
      }
    }
    throw new IllegalStateException("Could not locate 'testing/workspaces/advanced_new'");
  }

  private static QueryModel queryWithTarget(String targetDocumentModel, String... fields) {
    QueryModelContent content = new QueryModelContent();
    content.setTargetDocumentModel(targetDocumentModel);
    content.setFields(List.of(fields));
    QueryModel model = new QueryModel();
    model.setId("Q");
    model.setContent(content);
    return model;
  }

  private static ValidationContext context(QueryModel model) {
    return new ValidationContext(null, workspace(), List.of(), List.of(), model);
  }

  @Test
  void targetDocumentModelResolvesTheCombinationsSyntheticMerge() {
    QueryModel model = queryWithTarget("PersonEmployee_Cm");

    DocumentModel resolved = QueryElementResolution.targetDocumentModel(model, context(model));

    assertNotNull(resolved, "a Combination Model target must resolve to its synthetic merge");
    assertEquals("PersonEmployee_Cm", resolved.getId());
  }

  @Test
  void fieldReferenceValidatorResolvesAFieldOfTheCombinationsBaseModel() {
    QueryModel model = queryWithTarget("PersonEmployee_Cm", "/Person/FirstName");

    List<ModelValidationError> errors = new QueryFieldReferenceValidator().validate(model, context(model));

    assertEquals(List.of(), errors);
  }
}
