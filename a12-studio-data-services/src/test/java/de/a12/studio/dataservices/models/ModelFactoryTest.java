package de.a12.studio.dataservices.models;

import de.a12.studio.dataservices.models.documentmodel.DocumentModel;
import de.a12.studio.dataservices.projects.ProjectItem;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;

class ModelFactoryTest {

  @Test
  void loadsSupportedDocumentModel() {
    File file = resource("/documentmodel/Company_DM.json");
    A12Model model = ModelFactory.load(new ProjectItem(file));

    DocumentModel documentModel = assertInstanceOf(DocumentModel.class, model);
    assertEquals("Company_DM", documentModel.getId());
  }

  @Test
  void returnsNullForFolders() {
    File folder = resource("/documentmodel").getParentFile();
    assertNull(ModelFactory.load(new ProjectItem(folder)));
  }

  @Test
  void returnsNullForNonJsonFiles() throws Exception {
    File file = File.createTempFile("not-a-model", ".txt");
    file.deleteOnExit();
    assertNull(ModelFactory.load(new ProjectItem(file)));
  }

  private File resource(String path) {
    return new File(getClass().getResource(path).getFile());
  }
}
