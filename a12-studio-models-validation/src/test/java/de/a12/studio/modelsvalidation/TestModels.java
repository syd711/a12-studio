package de.a12.studio.modelsvalidation;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.models.util.JsonSettings;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/** Fixture loading and {@link ValidationContext} construction for validator tests. */
public final class TestModels {

  private TestModels() {
  }

  public static <T> T load(String resourcePath, Class<T> modelClass) {
    try (InputStream in = TestModels.class.getResourceAsStream(resourcePath)) {
      assertNotNull(in, "Missing test resource: " + resourcePath);
      String json = new String(in.readAllBytes(), StandardCharsets.UTF_8);
      return JsonSettings.objectMapper.readValue(json, modelClass);
    }
    catch (Exception e) {
      throw new RuntimeException("Failed to load fixture " + resourcePath, e);
    }
  }

  /** A context without any sibling models; the item name defaults to "&lt;model id&gt;.json". */
  public static ValidationContext context(A12Model<?> model) {
    return context(model.getId() + ".json", List.of(), List.of());
  }

  /** A context whose project item carries the given file name (for the id/filename rule). */
  public static ValidationContext contextWithFileName(String fileName) {
    return context(fileName, List.of(), List.of());
  }

  /** A context with sibling document models (also listed among the generic other models). */
  public static ValidationContext contextWithDocumentModels(A12Model<?> model, DocumentModel... documentModels) {
    return context(model.getId() + ".json", List.of(documentModels), List.of(documentModels));
  }

  /** A context with arbitrary sibling models (document models filtered out of them automatically). */
  public static ValidationContext contextWithOtherModels(A12Model<?> model, A12Model<?>... otherModels) {
    List<DocumentModel> documentModels = List.of(otherModels).stream()
        .filter(other -> other instanceof DocumentModel)
        .map(other -> (DocumentModel) other)
        .toList();
    return context(model.getId() + ".json", documentModels, List.of(otherModels));
  }

  public static ValidationContext context(String fileName, List<DocumentModel> otherDocumentModels, List<A12Model<?>> otherModels) {
    return new ValidationContext(null, new ProjectItem(new File(fileName)), otherDocumentModels, otherModels);
  }
}
