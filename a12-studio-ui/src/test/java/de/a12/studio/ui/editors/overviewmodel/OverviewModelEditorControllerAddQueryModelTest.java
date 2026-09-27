package de.a12.studio.ui.editors.overviewmodel;

import de.a12.studio.models.overviewmodel.OverviewModel;
import de.a12.studio.models.projects.Project;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.models.querymodel.QueryModel;
import de.a12.studio.models.querymodel.QueryModelContent;
import de.a12.studio.modelsvalidation.ValidationService;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.editors.formmodel.FxTestSupport;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/** Gap 14 of the "Overview Model: gap review" - generating a Query Model's Fields/Paging/Sorting from the
 * Overview Model being edited (the part of the "Add" flow that doesn't require driving the blocking Create
 * Query Model dialog - see {@link de.a12.studio.ui.editors.overviewmodel.dialogs.CreateQueryModelDialogControllerTest}
 * for that half). */
class OverviewModelEditorControllerAddQueryModelTest {

  private static final String DOCUMENT_MODEL = """
      {"header": {"id": "Person_Dc", "modelType": "document", "modelVersion": "29.4.0"},
       "content": {"modelInfo": {"name": "Person_Dc", "immutable": false},
         "modelConfig": {"timeZone": "UTC", "decimalSeparator": ".", "conditionLanguage": {"code": "en_US"}},
         "modelRoot": {"rootGroups": [{"type": "Group", "id": "group_root", "name": "Person", "Group": {"repeatability": 1,
           "elements": [
             {"type": "Field", "id": "field_first", "name": "FirstName", "Field": {"fieldType": {"type": "StringType"}}},
             {"type": "Field", "id": "field_last", "name": "LastName", "Field": {"fieldType": {"type": "StringType"}}}
           ]}}]}}}
      """;

  private static final String OVERVIEW = """
      {"header": {"id": "Person_Ov", "modelType": "overview", "modelVersion": "39.0.0", "modelReferences": [
        {"purpose": "document-model-for-overview", "modelType": "document", "alias": "Ref", "reference": "Person_Dc"}
      ]},
       "content": {
         "configuration": {"pagingSize": 25, "initialSorting": [{"idref": "column_last"}]},
         "columns": [
           {"id": "column_first", "width": 1, "elementRef": "field_first"},
           {"id": "column_last", "width": 1, "elementRef": "field_last", "sortable": true, "preferredSorting": "DESC"},
           {"id": "column_expr", "width": 1, "name": "Full Name", "expression": "[FirstName]"}
         ]
       }}
      """;

  private static boolean toolkitAvailable;

  @BeforeAll
  static void startToolkit() throws Exception {
    toolkitAvailable = FxTestSupport.startToolkit();
  }

  @AfterEach
  void tearDown() throws Exception {
    if (toolkitAvailable) {
      setStatic("currentProject", new Project());
      setStatic("validationService", null);
    }
  }

  private static void setStatic(String name, Object value) throws Exception {
    Field field = Studio.class.getDeclaredField(name);
    field.setAccessible(true);
    field.set(null, value);
  }

  @Test
  void generatesFieldsPagingAndSortingFromTheOverview(@TempDir Path dir) throws Exception {
    assumeTrue(toolkitAvailable, "No JavaFX toolkit available");
    Path models = Files.createDirectories(dir.resolve("models"));
    Files.writeString(models.resolve("Person_Dc.json"), DOCUMENT_MODEL);
    Path overviewFile = models.resolve("Person_Ov.json");
    Files.writeString(overviewFile, OVERVIEW);

    Project project = new Project();
    project.load(dir.toFile());
    setStatic("currentProject", project);
    setStatic("validationService", new ValidationService(project));
    ProjectItem item = project.getRoot().findByPath(overviewFile.toString());

    FxTestSupport.Loaded<OverviewModelEditorController> loaded =
        FxTestSupport.load("/de/a12/studio/ui/editors/overviewmodel/overview-model-editor.fxml");
    OverviewModelEditorController controller = loaded.controller();
    FxTestSupport.onFx(() -> controller.load(item));

    QueryModel queryModel = new QueryModel();
    queryModel.setId("Person_Qe");
    queryModel.setContent(new QueryModelContent());
    FxTestSupport.onFx(() -> controller.generateQueryFromOverview(queryModel, "Person_Dc"));

    assertEquals(2, queryModel.getContent().getFields().size(), "the expression column contributes no field");
    assertTrue(queryModel.getContent().getFields().contains("/Person/FirstName"));
    assertTrue(queryModel.getContent().getFields().contains("/Person/LastName"));
    assertEquals(25, queryModel.getContent().getPaging().getPageSize());
    assertEquals(1, queryModel.getContent().getSort().size());
    assertEquals("/Person/LastName", queryModel.getContent().getSort().get(0).getSortBy().getField());
    assertEquals("DESC", queryModel.getContent().getSort().get(0).getSortBy().getDirection());
  }
}
