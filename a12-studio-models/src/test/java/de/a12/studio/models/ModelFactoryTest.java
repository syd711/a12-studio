package de.a12.studio.models;

import de.a12.studio.models.applicationmodel.ApplicationModel;
import de.a12.studio.models.contentmodel.ContentModel;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.formmodel.FormModel;
import de.a12.studio.models.overviewmodel.OverviewModel;
import de.a12.studio.models.printmodel.PrintModel;
import de.a12.studio.models.relationshipmodel.RelationshipModel;
import de.a12.studio.models.treemodel.TreeModel;
import de.a12.studio.models.typedefinitionmodel.TypeDefinitionModel;
import de.a12.studio.models.projects.ProjectItem;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;

class ModelFactoryTest {

  @Test
  void loadsSupportedDocumentModel() {
    File file = resource("/documentmodel/Company_DM.json");
    A12Model<?> model = ModelFactory.load(new ProjectItem(file));

    DocumentModel documentModel = assertInstanceOf(DocumentModel.class, model);
    assertEquals("Company_DM", documentModel.getId());
  }

  @Test
  void loadsSupportedOverviewModel() {
    File file = resource("/overviewmodel/Company_OM.json");
    A12Model<?> model = ModelFactory.load(new ProjectItem(file));

    OverviewModel overviewModel = assertInstanceOf(OverviewModel.class, model);
    assertEquals("Company_OM", overviewModel.getId());
  }

  @Test
  void loadsSupportedApplicationModel() {
    File file = resource("/applicationmodel/PreviewApp_AM.json");
    A12Model<?> model = ModelFactory.load(new ProjectItem(file));

    ApplicationModel applicationModel = assertInstanceOf(ApplicationModel.class, model);
    assertEquals("PreviewApp_AM", applicationModel.getId());
  }

  @Test
  void loadsSupportedFormModel() {
    File file = resource("/formmodel/Company_FM.json");
    A12Model<?> model = ModelFactory.load(new ProjectItem(file));

    FormModel formModel = assertInstanceOf(FormModel.class, model);
    assertEquals("Company_FM", formModel.getId());
  }

  @Test
  void loadsTdOnlyAnnotatedDocumentAsTypeDefinitionModel() {
    File file = resource("/typedefinitionmodel/Basic_TDM.json");
    A12Model<?> model = ModelFactory.load(new ProjectItem(file));

    TypeDefinitionModel typeDefinitionModel = assertInstanceOf(TypeDefinitionModel.class, model);
    assertEquals("Basic_TDM", typeDefinitionModel.getId());
  }

  @Test
  void loadsSupportedRelationshipModel() {
    File file = resource("/relationshipmodel/PersonCompany.json");
    A12Model<?> model = ModelFactory.load(new ProjectItem(file));

    RelationshipModel relationshipModel = assertInstanceOf(RelationshipModel.class, model);
    assertEquals("PersonCompany", relationshipModel.getId());
  }

  @Test
  void loadsSupportedContentModel() {
    File file = resource("/contentmodel/WelcomePage_CM.json");
    A12Model<?> model = ModelFactory.load(new ProjectItem(file));

    ContentModel contentModel = assertInstanceOf(ContentModel.class, model);
    assertEquals("WelcomePage_CM", contentModel.getId());
  }

  @Test
  void loadsSupportedPrintModel() {
    File file = resource("/printmodel/PrintModel.json");
    A12Model<?> model = ModelFactory.load(new ProjectItem(file));

    PrintModel printModel = assertInstanceOf(PrintModel.class, model);
    assertEquals("PrintModel", printModel.getId());
  }

  @Test
  void loadsSupportedTreeModel() {
    File file = resource("/treemodel/TreeModel.json");
    A12Model<?> model = ModelFactory.load(new ProjectItem(file));

    TreeModel treeModel = assertInstanceOf(TreeModel.class, model);
    assertEquals("TreeModel", treeModel.getId());
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
