package de.a12.studio.models.transformermodel;

import de.a12.studio.models.ModelType;
import de.a12.studio.models.NewModelFactory;
import de.a12.studio.models.combineddocumentmodel.CombinedDocumentModelElements;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.projects.ProjectItem;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class GeneratedDocumentModelsTest {

  @Test
  void aStoredModelIsFoundAgainAfterTheMemoryIsGone(@TempDir Path folder) throws Exception {
    DocumentModel generated = NewModelFactory.createModel(new ProjectItem(folder.toFile()), ModelType.DOCUMENT, "Scratch_DM").getModel() instanceof DocumentModel dm
        ? dm : null;
    assertNotNull(generated);

    GeneratedDocumentModels.store(folder.toFile(), "Persons_TfM", generated);
    assertEquals("Persons_TfM", GeneratedDocumentModels.find(folder.toFile(), "Persons_TfM").getId());
    assertEquals("Scratch_DM", generated.getId());

    GeneratedDocumentModels.clearMemory();
    DocumentModel reloaded = GeneratedDocumentModels.find(folder.toFile(), "Persons_TfM");
    assertNotNull(reloaded);
    assertEquals("Persons_TfM", reloaded.getId());
  }

  @Test
  void aTransformerModelResolvesToItsGeneratedModelAndOnlyOnceGenerated(@TempDir Path folder) throws Exception {
    GeneratedDocumentModels.clearMemory();
    ProjectItem root = new ProjectItem(folder.toFile());
    ProjectItem transformer = NewModelFactory.createModel(root, ModelType.TRANSFORMER, "Persons_TfM");
    ProjectItem scratch = NewModelFactory.createModel(root, ModelType.DOCUMENT, "Scratch_DM");

    assertNull(CombinedDocumentModelElements.resolveForFieldReferences(transformer, "Persons_TfM"));

    GeneratedDocumentModels.store(folder.toFile(), "Persons_TfM", (DocumentModel) scratch.getModel());
    DocumentModel resolved = CombinedDocumentModelElements.resolveForFieldReferences(transformer, "Persons_TfM");
    assertNotNull(resolved);
    assertEquals("Persons_TfM", resolved.getId());
  }

  @Test
  void aMissAsksTheHandlerOnceItIsSetAndStillAnswersNone(@TempDir Path folder) throws Exception {
    GeneratedDocumentModels.clearMemory();
    ProjectItem root = new ProjectItem(folder.toFile());
    ProjectItem transformer = NewModelFactory.createModel(root, ModelType.TRANSFORMER, "Persons_TfM");
    java.util.List<String> asked = new java.util.ArrayList<>();
    GeneratedDocumentModels.setMissHandler(item -> asked.add(item.getModel().getId()));
    try {
      assertNull(GeneratedDocumentModels.resolve(transformer, "Persons_TfM"));
      assertEquals(java.util.List.of("Persons_TfM"), asked);
      assertNull(GeneratedDocumentModels.resolve(transformer, "Unknown_TfM"));
      assertEquals(1, asked.size());
    }
    finally {
      GeneratedDocumentModels.setMissHandler(null);
    }
  }
}
