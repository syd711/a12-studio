package de.a12.studio.dataservices.models;

import de.a12.studio.dataservices.models.applicationmodel.ApplicationModel;
import de.a12.studio.dataservices.models.documentmodel.DocumentModel;
import de.a12.studio.dataservices.models.formmodel.FormModel;
import de.a12.studio.dataservices.models.overviewmodel.OverviewModel;
import de.a12.studio.dataservices.models.typedefinitionmodel.TypeDefinitionModel;
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
  void loadsSupportedOverviewModel() {
    File file = resource("/overviewmodel/Company_OM.json");
    A12Model model = ModelFactory.load(new ProjectItem(file));

    OverviewModel overviewModel = assertInstanceOf(OverviewModel.class, model);
    assertEquals("Company_OM", overviewModel.getId());
  }

  @Test
  void loadsSupportedApplicationModel() {
    File file = resource("/applicationmodel/PreviewApp_AM.json");
    A12Model model = ModelFactory.load(new ProjectItem(file));

    ApplicationModel applicationModel = assertInstanceOf(ApplicationModel.class, model);
    assertEquals("PreviewApp_AM", applicationModel.getId());
  }

  @Test
  void loadsSupportedFormModel() {
    File file = resource("/formmodel/Company_FM.json");
    A12Model model = ModelFactory.load(new ProjectItem(file));

    FormModel formModel = assertInstanceOf(FormModel.class, model);
    assertEquals("Company_FM", formModel.getId());
  }

  @Test
  void loadsTdOnlyAnnotatedDocumentAsTypeDefinitionModel() {
    File file = resource("/typedefinitionmodel/Basic_TDM.json");
    A12Model model = ModelFactory.load(new ProjectItem(file));

    TypeDefinitionModel typeDefinitionModel = assertInstanceOf(TypeDefinitionModel.class, model);
    assertEquals("Basic_TDM", typeDefinitionModel.getId());
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
