package de.a12.studio.ui.editors.overviewmodel;

import de.a12.studio.models.overviewmodel.EnumeratedStringFilter;
import de.a12.studio.models.overviewmodel.OverviewModel;
import de.a12.studio.models.projects.Project;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.ui.editors.formmodel.FxTestSupport;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Spinner;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/** {@link FilterStringFieldsMultiSelectPanelController} - "Enumerated String Filter" gap: the field list editor
 * behind {@code enumeratedStringFilter.fields}, on top of the pre-existing enable/paging-size checkbox+spinner. */
class FilterStringFieldsMultiSelectPanelControllerTest {

  private static final String OVERVIEW = """
      {"header": {"id": "Product_Ov", "modelType": "overview", "modelVersion": "39.0.0", "modelReferences": [
        {"purpose": "document-model-for-overview", "modelType": "document", "alias": "Ref", "reference": "Product_Dc"}
      ]},
       "content": {"configuration": {}, "columns": [{"id": "column_1", "width": 1, "elementRef": "field_1"}]}}
      """;

  private static final String DOCUMENT_MODEL = """
      {"header": {"id": "Product_Dc", "modelType": "document", "modelVersion": "29.4.0"},
       "content": {"modelInfo": {"name": "Product_Dc", "immutable": false},
         "modelConfig": {"timeZone": "UTC", "decimalSeparator": ".", "conditionLanguage": {"code": "en_US"}},
         "modelRoot": {"rootGroups": [{"type": "Group", "id": "group_root", "name": "Root", "Group": {"repeatability": 1,
           "elements": [{"type": "Field", "id": "field_1", "name": "Name", "Field": {"fieldType": {"type": "StringType"}}}]}}]}}}
      """;

  private static boolean toolkitAvailable;

  private FilterStringFieldsMultiSelectPanelController controller;

  @BeforeAll
  static void startToolkit() throws Exception {
    toolkitAvailable = FxTestSupport.startToolkit();
  }

  @AfterEach
  void tearDown() throws Exception {
    if (controller != null) {
      controller.destroy();
    }
    FxTestSupport.onFx(() -> {
    });
    FxTestSupport.selectProjectItem(null);
  }

  @Test
  void enablingShowsFieldsAndAddedFieldPersists(@TempDir Path dir) throws Exception {
    assumeTrue(toolkitAvailable, "No JavaFX toolkit available");
    Files.writeString(dir.resolve("Product_Dc.json"), DOCUMENT_MODEL);
    Path file = dir.resolve("Product_Ov.json");
    Files.writeString(file, OVERVIEW);
    ProjectItem item = new ProjectItem(file.toFile());
    assertNotNull(item.getModel(), "the fixture overview model must load");
    FxTestSupport.selectProjectItem(item);

    FxTestSupport.Loaded<FilterStringFieldsMultiSelectPanelController> loaded =
        FxTestSupport.load("/de/a12/studio/ui/editors/overviewmodel/filter-string-fields-multi-select-panel.fxml");
    controller = loaded.controller();
    OverviewModel model = (OverviewModel) item.getModel();
    Project project = new Project();
    project.load(dir.toFile());
    de.a12.studio.models.documentmodel.DocumentModel documentModel =
        (de.a12.studio.models.documentmodel.DocumentModel) new ProjectItem(dir.resolve("Product_Dc.json").toFile()).getModel();
    FxTestSupport.onFx(() -> controller.setModel(model));
    FxTestSupport.onFx(() -> controller.setDocumentModelIndex(OverviewElementOptions.indexOf(documentModel, java.util.List.of())));

    CheckBox enabled = FxTestSupport.field(controller, "enabledField");
    VBox fieldsBox = FxTestSupport.field(controller, "fieldsBox");
    assertFalse(enabled.isSelected());
    assertFalse(fieldsBox.isVisible());

    FxTestSupport.onFx(() -> enabled.setSelected(true));
    assertEquals(true, fieldsBox.isVisible());

    Button addButton = FxTestSupport.field(controller, "addButton");
    FxTestSupport.onFx(addButton::fire);

    VBox fieldRows = FxTestSupport.field(controller, "fieldRows");
    assertEquals(1, fieldRows.getChildren().size());
    HBox row = (HBox) fieldRows.getChildren().get(0);
    @SuppressWarnings("unchecked")
    ComboBox<String> fieldCombo = (ComboBox<String>) row.getChildren().get(1);
    FxTestSupport.onFx(() -> fieldCombo.setValue("field_1"));

    Spinner<Integer> pagingSizeField = FxTestSupport.field(controller, "pagingSizeField");
    FxTestSupport.onFx(() -> pagingSizeField.getValueFactory().setValue(25));

    OverviewModel reloaded = (OverviewModel) new ProjectItem(item.getFile()).getModel();
    EnumeratedStringFilter savedFilter = reloaded.getContent().getConfiguration().getFilterConfiguration().getEnumeratedStringFilter();
    assertNotNull(savedFilter);
    assertEquals(25, savedFilter.getPagingSize());
    assertEquals(1, savedFilter.getFields().size());
    assertEquals("field_1", savedFilter.getFields().get(0).getFieldId());
  }
}
