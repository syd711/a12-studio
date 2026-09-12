package de.a12.studio.dataservices.preview;

import de.a12.studio.models.ModelReference;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.documentmodel.Element;
import de.a12.studio.models.overviewmodel.OverviewModel;
import de.a12.studio.models.projects.ProjectItem;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class DocumentModelFieldResolverTest {

  /**
   * An Overview Model's {@code document-model-for-overview} reference can point at a Combination Model
   * (see {@code PersonEmployee_Ov.json} / {@code PersonEmployee_Cm.json} in {@code testing/workspaces}) -
   * a {@link de.a12.studio.models.combineddocumentmodel.CombinedDocumentModel}, not a {@link DocumentModel}.
   * {@link DocumentModelFieldResolver#resolveReferencedDocumentModel} must follow that reference through to
   * a merge of the Combination Model's base Document Model and every {@code Addition} step's additive model
   * (see {@code de.a12.studio.models.combineddocumentmodel.CombinedDocumentModelElements}), so both a
   * base-model column and an additive-model-only column resolve. The additive column's {@code elementRef}
   * ({@code md5Hex("CombinationAdditive_DM")_field_additive_extra}) mirrors the real id-rewriting convention
   * confirmed against {@code PersonEmployee_Ov.json}'s last two columns, whose {@code elementRef}s are {@code
   * md5Hex("PersonEmployee_Ad")_F7}/{@code _F9}.
   */
  @Test
  void resolvesThroughCombinationModelToItsBaseAndAdditiveFields(@TempDir Path tempDir) throws IOException {
    copyFixture(tempDir, "CombinationOverview_OM.json");
    copyFixture(tempDir, "CombinationBase_CM.json");
    copyFixture(tempDir, "CombinationBase_DM.json");
    copyFixture(tempDir, "CombinationAdditive_DM.json");

    ProjectItem projectRoot = new ProjectItem(tempDir.toFile());
    ProjectItem overviewItem = projectRoot.getChildren().stream()
        .filter(item -> "CombinationOverview_OM.json".equals(item.getName()))
        .findFirst()
        .orElseThrow();
    OverviewModel overviewModel = (OverviewModel) overviewItem.getModel();

    DocumentModel resolved = DocumentModelFieldResolver.resolveReferencedDocumentModel(
        overviewModel, ModelReference.PURPOSE_DOCUMENT_MODEL_FOR_OVERVIEW, overviewItem);

    assertNotNull(resolved);
    assertEquals("CombinationBase_CM", resolved.getId());

    Map<String, Element> elementsById = DocumentModelFieldResolver.index(resolved);
    Element baseField = elementsById.get("field_base_name");
    assertNotNull(baseField);
    assertEquals("Name", DocumentModelFieldResolver.fieldLabel(baseField));

    Element additiveField = elementsById.get("17935c3abb732de212ef59c69d8fd76f_field_additive_extra");
    assertNotNull(additiveField);
    assertEquals("Extra", DocumentModelFieldResolver.fieldLabel(additiveField));
  }

  private void copyFixture(Path tempDir, String name) throws IOException {
    try (InputStream in = getClass().getResourceAsStream("/preview/" + name)) {
      Files.write(tempDir.resolve(name), in.readAllBytes());
    }
  }
}
