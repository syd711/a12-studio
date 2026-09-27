package de.a12.studio.ui.editors.overviewmodel.dialogs;

import de.a12.studio.models.Locale;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.documentmodel.DocumentModelContent;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.ui.editors.formmodel.FxTestSupport;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/** Gap 14 of the "Overview Model: gap review" - the "Create Query Model" dialog behind {@link
 * de.a12.studio.ui.editors.overviewmodel.OverviewReferencePanelController}'s "Add" button. */
class CreateQueryModelDialogControllerTest {

  private static final String FXML = "/de/a12/studio/ui/editors/overviewmodel/dialogs/create-query-model-dialog.fxml";

  @Test
  void requiresANameAndATargetDocumentModelAndDefaultsGenerateToChecked(@TempDir Path dir) throws Exception {
    assumeTrue(FxTestSupport.startToolkit(), "No JavaFX toolkit available");
    FxTestSupport.Loaded<CreateQueryModelDialogController> loaded = FxTestSupport.load(FXML);
    CreateQueryModelDialogController controller = loaded.controller();
    Stage stage = FxTestSupport.onFx(() -> new Stage());

    ProjectItem targetFolder = new ProjectItem(dir.toFile());
    targetFolder.setRoot(true);
    DocumentModel documentModel = new DocumentModel();
    documentModel.setId("Person_Dc");
    documentModel.setContent(new DocumentModelContent());

    FxTestSupport.onFx(() -> controller.init(stage, targetFolder, List.of(documentModel), "Person_Dc", List.of(), "Person_Qe"));

    TextField nameField = FxTestSupport.field(controller, "nameField");
    ComboBox<String> targetDocumentModelCombo = FxTestSupport.field(controller, "targetDocumentModelCombo");
    CheckBox generateFromOverviewField = FxTestSupport.field(controller, "generateFromOverviewField");
    Button okButton = FxTestSupport.field(controller, "okButton");

    assertEquals("Person_Qe", FxTestSupport.onFx(() -> nameField.getText()));
    assertEquals("Person_Dc", FxTestSupport.onFx(() -> targetDocumentModelCombo.getValue()));
    assertTrue(FxTestSupport.onFx(() -> generateFromOverviewField.isSelected()), "generation defaults to on");
    assertFalse(FxTestSupport.onFx(() -> okButton.isDisabled()));

    FxTestSupport.onFx(() -> targetDocumentModelCombo.setValue(null));
    assertTrue(FxTestSupport.onFx(() -> okButton.isDisabled()), "no target Document Model");

    FxTestSupport.onFx(() -> targetDocumentModelCombo.setValue("Person_Dc"));
    FxTestSupport.onFx(() -> nameField.setText(""));
    assertTrue(FxTestSupport.onFx(() -> okButton.isDisabled()), "no name");

    FxTestSupport.onFx(() -> nameField.setText("Person_Qe"));
    FxTestSupport.onFx(() -> generateFromOverviewField.setSelected(false));
    submit(controller);

    CreateQueryModelDialogController.Result result = FxTestSupport.onFx(() -> controller.getResult()).orElseThrow();
    assertEquals("Person_Qe", result.name());
    assertEquals("Person_Dc", result.targetDocumentModelId());
    assertFalse(result.generateFromOverview());
    assertEquals(targetFolder, result.folder());
  }

  private static void submit(CreateQueryModelDialogController controller) throws Exception {
    Method method = CreateQueryModelDialogController.class.getDeclaredMethod("onDialogSubmit");
    method.setAccessible(true);
    FxTestSupport.onFx(() -> {
      method.invoke(controller);
      return null;
    });
  }
}
