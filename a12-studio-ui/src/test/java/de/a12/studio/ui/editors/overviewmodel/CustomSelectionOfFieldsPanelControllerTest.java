package de.a12.studio.ui.editors.overviewmodel;

import de.a12.studio.models.Annotation;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.documentmodel.DocumentModelContent;
import de.a12.studio.models.documentmodel.ModelRoot;
import de.a12.studio.models.overviewmodel.FieldRef;
import de.a12.studio.models.overviewmodel.FilterConfiguration;
import de.a12.studio.models.overviewmodel.OverviewConfiguration;
import de.a12.studio.models.overviewmodel.OverviewModel;
import de.a12.studio.models.overviewmodel.OverviewModelContent;
import de.a12.studio.models.projects.Project;
import de.a12.studio.ui.editors.formmodel.FxTestSupport;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/** Gap 5 of the "Overview Model: gap review" - the Custom Selection Of Fields row's Subtype picker
 * ({@code FieldRef.subModel}). */
class CustomSelectionOfFieldsPanelControllerTest {

  private static final String FXML = "/de/a12/studio/ui/editors/overviewmodel/custom-selection-of-fields-panel.fxml";

  private static boolean toolkitAvailable;

  @BeforeAll
  static void startToolkit() throws Exception {
    toolkitAvailable = FxTestSupport.startToolkit();
    if (toolkitAvailable) {
      // CustomSelectionOfFieldsPanelController.refreshValidationError() calls Studio.getValidationService();
      // its ProjectModels walk needs a real (even empty) folder - an unloaded Project's root has no File and NPEs.
      Project validationProject = new Project();
      validationProject.load(Files.createTempDirectory("custom-selection-of-fields-validation").toFile());
      FxTestSupport.setValidationServiceForProject(validationProject);
    }
  }

  @AfterAll
  static void stopValidationService() throws Exception {
    if (toolkitAvailable) {
      FxTestSupport.clearValidationService();
    }
  }

  @Test
  void pickingASubtypeRepointsTheFieldPickerAndClearsTheStaleFieldId() throws Exception {
    assumeTrue(FxTestSupport.startToolkit(), "No JavaFX toolkit available");

    DocumentModel base = new DocumentModel();
    base.setId("Product_DM");
    base.setContent(new DocumentModelContent());
    base.getContent().setModelRoot(new ModelRoot());

    DocumentModel subType = new DocumentModel();
    subType.setId("ProductBook_DM");
    subType.setContent(new DocumentModelContent());
    subType.getContent().setModelRoot(new ModelRoot());
    Annotation superTypes = new Annotation();
    superTypes.setName("superTypes");
    superTypes.setValue("Product_DM");
    subType.getAnnotations().add(superTypes);

    OverviewModel model = new OverviewModel();
    model.setId("Product_Ov");
    model.setContent(new OverviewModelContent());
    FieldRef fieldRef = new FieldRef();
    fieldRef.setFieldId("some-stale-id");
    OverviewConfiguration configuration = new OverviewConfiguration();
    FilterConfiguration filterConfiguration = new FilterConfiguration();
    filterConfiguration.getFields().add(fieldRef);
    configuration.setFilterConfiguration(filterConfiguration);
    model.getContent().setConfiguration(configuration);

    FxTestSupport.Loaded<CustomSelectionOfFieldsPanelController> loaded = FxTestSupport.load(FXML);
    CustomSelectionOfFieldsPanelController controller = loaded.controller();
    FxTestSupport.onFx(() -> controller.setModel(model));
    FxTestSupport.onFx(() -> controller.setDocumentModelIndex(
        OverviewElementOptions.indexOf(base, List.of(base, subType)), List.of(base, subType)));

    VBox fieldRows = FxTestSupport.field(controller, "fieldRows");
    HBox row = (HBox) fieldRows.getChildren().get(0);
    GridPane contentGrid = (GridPane) row.getChildren().get(1);
    @SuppressWarnings("unchecked")
    ComboBox<String> subtypeField = (ComboBox<String>) contentGrid.getChildren().get(0);
    @SuppressWarnings("unchecked")
    ComboBox<String> fieldField = (ComboBox<String>) contentGrid.getChildren().get(1);

    assertEquals(List.of("ProductBook_DM"), subtypeField.getItems(), "the recursive sub-types of Product_DM");
    assertTrue(fieldField.getValue() == null || "some-stale-id".equals(fieldField.getValue()));

    FxTestSupport.onFx(() -> subtypeField.setValue("ProductBook_DM"));

    assertEquals("ProductBook_DM", fieldRef.getSubModel());
    assertNull(fieldRef.getFieldId(), "the stale field id from the base model no longer applies");
  }

  @Test
  void theSubtypePickerIsDisabledWhenTheDocumentModelHasNoSubTypes() throws Exception {
    assumeTrue(FxTestSupport.startToolkit(), "No JavaFX toolkit available");

    DocumentModel base = new DocumentModel();
    base.setId("Plain_DM");
    base.setContent(new DocumentModelContent());
    base.getContent().setModelRoot(new ModelRoot());

    OverviewModel model = new OverviewModel();
    model.setId("Plain_Ov");
    model.setContent(new OverviewModelContent());
    OverviewConfiguration configuration = new OverviewConfiguration();
    FilterConfiguration filterConfiguration = new FilterConfiguration();
    filterConfiguration.getFields().add(new FieldRef());
    configuration.setFilterConfiguration(filterConfiguration);
    model.getContent().setConfiguration(configuration);

    FxTestSupport.Loaded<CustomSelectionOfFieldsPanelController> loaded = FxTestSupport.load(FXML);
    CustomSelectionOfFieldsPanelController controller = loaded.controller();
    FxTestSupport.onFx(() -> controller.setModel(model));
    FxTestSupport.onFx(() -> controller.setDocumentModelIndex(OverviewElementOptions.indexOf(base, List.of(base)), List.of(base)));

    VBox fieldRows = FxTestSupport.field(controller, "fieldRows");
    HBox row = (HBox) fieldRows.getChildren().get(0);
    GridPane contentGrid = (GridPane) row.getChildren().get(1);
    ComboBox<?> subtypeField = (ComboBox<?>) contentGrid.getChildren().get(0);

    assertTrue(subtypeField.isDisabled());
  }
}
