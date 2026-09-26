package de.a12.studio.ui.editors.contentmodel;

import de.a12.studio.models.ModelType;
import de.a12.studio.models.NewModelFactory;
import de.a12.studio.models.contentmodel.ContentModel;
import de.a12.studio.models.projects.Project;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.modelsvalidation.ValidationService;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.components.ErrorContainerController;
import de.a12.studio.ui.editors.AbstractPropertyEditor;
import de.a12.studio.ui.editors.PropertyEditorSaveMode;
import de.a12.studio.ui.editors.dialogs.ModelSettingsDialog;
import de.a12.studio.ui.editors.formmodel.FxTestSupport;
import de.a12.studio.ui.util.StudioBundle;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TitledPane;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * The Content Model's Model Settings dialog, through the real FXML: the Document Model binding and the Base Group
 * (SME's "Document Model" and "Base Group" settings), written to the header reference and to
 * {@code content.configuration.baseGroupId}, saved with the dialog's Save and undone by Cancel.
 */
class ContentModelSettingsDialogTest {

  private static final String SETTINGS_FXML = "/de/a12/studio/ui/editors/dialogs/document-model-settings-dialog.fxml";

  @TempDir
  Path workspace;

  private ProjectItem item;
  private ContentModel model;
  private FxTestSupport.Loaded<ModelSettingsDialog> dialog;

  @AfterAll
  static void restoreStudio() throws Exception {
    setStatic("currentProject", new Project());
    setStatic("validationService", null);
    FxTestSupport.selectProjectItem(null);
  }

  @BeforeEach
  void openProject() throws Exception {
    assumeTrue(FxTestSupport.startToolkit(), "No JavaFX toolkit available");
    Files.writeString(workspace.resolve("Order_DM.json"), documentModel("Order_DM", "Order", "g_order",
        group("Items", "g_items", 10, group("Details", "g_details", 1))));
    Files.writeString(workspace.resolve("Other_DM.json"), documentModel("Other_DM", "Other", "g_other", ""));
    ProjectItem folder = new ProjectItem(workspace.toFile());
    folder.setRoot(true);
    NewModelFactory.createModel(folder, ModelType.CONTENT, "Page_CM");

    Project project = new Project();
    project.load(workspace.toFile());
    setStatic("currentProject", project);
    setStatic("validationService", new ValidationService(project));
    item = project.getRoot().findByPath(workspace.resolve("Page_CM.json").toString());
    model = (ContentModel) item.getModel();
    FxTestSupport.selectProjectItem(item);
  }

  @AfterEach
  void tearDown() throws Exception {
    if (dialog != null) {
      for (Field field : ModelSettingsDialog.class.getDeclaredFields()) {
        if (AbstractPropertyEditor.class.isAssignableFrom(field.getType())) {
          field.setAccessible(true);
          ((AbstractPropertyEditor) field.get(dialog.controller())).destroy();
        }
      }
    }
    FxTestSupport.selectProjectItem(null);
  }

  @Test
  void anUnboundModelOffersTheDocumentModelButNotTheBaseGroupOrTheGenericReferences() throws Exception {
    open();

    assertTrue(shown("contentDocumentModelController"));
    assertFalse(shown("contentBaseGroupController"), "SME hides the base group while there is no Document Model");
    assertFalse(shown("modelReferencesController"), "the Document Model is the only reference a Content Model has");
    assertEquals(List.of("Order_DM", "Other_DM"), documentModelField().getItems());
    assertNull(documentModelField().getValue());
    assertFalse(errorShown("contentDocumentModelController"));
  }

  @Test
  void otherModelTypesDoNotShowTheContentPanels() throws Exception {
    FxTestSupport.selectProjectItem(project().getRoot().findByPath(workspace.resolve("Order_DM.json").toString()));
    open();

    assertFalse(shown("contentDocumentModelController"));
    assertFalse(shown("contentBaseGroupController"));
    assertTrue(shown("modelReferencesController"));
  }

  @Test
  void bindingADocumentModelWritesSmesReferenceAndOffersItsGroupsAsBaseGroups() throws Exception {
    open();

    FxTestSupport.onFx(() -> documentModelField().setValue("Order_DM"));

    assertEquals("Order_DM", model.getDocumentModelId());
    assertEquals("document-model-for-content-model", model.getModelReferences().get(0).getPurpose());
    assertEquals("DM", model.getModelReferences().get(0).getAlias());
    assertTrue(shown("contentBaseGroupController"));
    assertEquals(List.of("g_order", "g_items", "g_details"), baseGroupField().getItems(), "every group, ordered by path");
    assertEquals(List.of("/Order", "/Order/Items", "/Order/Items/Details"),
        baseGroupField().getItems().stream().map(baseGroupField().getConverter()::toString).toList(), "shown by path");
    assertNull(model.getContent().getConfiguration().getBaseGroupId(), "the base group is optional");
    assertFalse(errorShown("contentDocumentModelController"));
    assertFalse(errorShown("contentBaseGroupController"));
  }

  @Test
  void theChoicesAreSavedWithTheDialogNotBefore() throws Exception {
    open();

    FxTestSupport.onFx(() -> documentModelField().setValue("Order_DM"));
    FxTestSupport.onFx(() -> baseGroupField().setValue("g_items"));

    assertEquals("g_items", model.getContent().getConfiguration().getBaseGroupId());
    assertNull(saved().getDocumentModelId(), "the dialog's Save has not been pressed");
    flush();
    assertEquals("Order_DM", saved().getDocumentModelId());
    assertEquals("g_items", saved().getContent().getConfiguration().getBaseGroupId());
    assertEquals("0.9.0", saved().getContent().getConfiguration().getNamespaceVersions().get("com.mgmtp.a12.contentengine"),
        "the rest of the configuration is untouched");
  }

  @Test
  void anExistingBindingAndBaseGroupAreShown() throws Exception {
    model.setDocumentModelId("Order_DM");
    model.getContent().getConfiguration().setBaseGroupId("g_details");
    open();

    assertEquals("Order_DM", documentModelField().getValue());
    assertTrue(shown("contentBaseGroupController"));
    assertEquals("g_details", baseGroupField().getValue());
    assertEquals("/Order/Items/Details", baseGroupField().getConverter().toString("g_details"));
    assertFalse(errorShown("contentDocumentModelController"));
    assertFalse(errorShown("contentBaseGroupController"));
    assertNull(saved().getDocumentModelId(), "showing the values changes nothing");
  }

  @Test
  void removingTheDocumentModelRemovesTheBaseGroupAndHidesItsPanel() throws Exception {
    model.setDocumentModelId("Order_DM");
    model.getContent().getConfiguration().setBaseGroupId("g_items");
    open();

    FxTestSupport.onFx(() -> clearDocumentModelButton().fire());

    assertNull(model.getDocumentModelId());
    assertTrue(model.getModelReferences().isEmpty());
    assertNull(model.getContent().getConfiguration().getBaseGroupId());
    assertFalse(shown("contentBaseGroupController"));
  }

  @Test
  void removingTheBaseGroupKeepsTheBinding() throws Exception {
    model.setDocumentModelId("Order_DM");
    model.getContent().getConfiguration().setBaseGroupId("g_items");
    open();

    FxTestSupport.onFx(() -> clearBaseGroupButton().fire());

    assertNull(model.getContent().getConfiguration().getBaseGroupId());
    assertEquals("Order_DM", model.getDocumentModelId());
    assertTrue(shown("contentBaseGroupController"));
  }

  @Test
  void aBoundDocumentModelThatIsNotInTheProjectIsReportedAndBlocksSave() throws Exception {
    model.setDocumentModelId("Gone_DM");
    open();

    assertTrue(errorShown("contentDocumentModelController"));
    assertEquals(StudioBundle.get("content_document_model_panel.not_found", "Gone_DM"), errorText("contentDocumentModelController"));
    assertTrue(saveButton().isDisabled());
    assertEquals("Gone_DM", documentModelField().getValue(), "it stays selected, not silently dropped");

    FxTestSupport.onFx(() -> clearDocumentModelButton().fire());

    assertFalse(errorShown("contentDocumentModelController"));
    assertFalse(saveButton().isDisabled());
  }

  @Test
  void aBaseGroupTheNewDocumentModelDoesNotHaveIsKeptAndReportedByName() throws Exception {
    model.setDocumentModelId("Order_DM");
    model.getContent().getConfiguration().setBaseGroupId("g_items");
    open();

    FxTestSupport.onFx(() -> documentModelField().setValue("Other_DM"));

    assertEquals("g_items", model.getContent().getConfiguration().getBaseGroupId(), "like SME, it is not dropped silently");
    assertTrue(shown("contentBaseGroupController"));
    assertEquals(List.of("g_other"), baseGroupField().getItems());
    assertEquals(StudioBundle.get("content_base_group_panel.not_found", "g_items", "Other_DM"), errorText("contentBaseGroupController"));
    assertTrue(saveButton().isDisabled());

    FxTestSupport.onFx(() -> baseGroupField().setValue("g_other"));

    assertFalse(errorShown("contentBaseGroupController"));
    assertFalse(saveButton().isDisabled());
  }

  @Test
  void cancelRestoresTheBindingAndTheBaseGroup() throws Exception {
    model.setDocumentModelId("Order_DM");
    model.getContent().getConfiguration().setBaseGroupId("g_items");
    open();

    FxTestSupport.onFx(() -> documentModelField().setValue("Other_DM"));
    FxTestSupport.onFx(() -> clearDocumentModelButton().fire());
    assertNull(model.getDocumentModelId());

    FxTestSupport.onFx(() -> dialog.controller().onDialogCancel());

    assertEquals("Order_DM", model.getDocumentModelId());
    assertEquals(1, model.getModelReferences().size());
    assertEquals("g_items", model.getContent().getConfiguration().getBaseGroupId());
  }

  // ---- helpers ----

  private void open() throws Exception {
    dialog = FxTestSupport.load(SETTINGS_FXML);
  }

  private Project project() {
    return Studio.getCurrentProject();
  }

  private ComboBox<String> documentModelField() {
    return field(field(dialog.controller(), "contentDocumentModelController"), "documentModelField");
  }

  private Button clearDocumentModelButton() {
    return field(field(dialog.controller(), "contentDocumentModelController"), "clearDocumentModelButton");
  }

  private ComboBox<String> baseGroupField() {
    return field(field(dialog.controller(), "contentBaseGroupController"), "baseGroupField");
  }

  private Button clearBaseGroupButton() {
    return field(field(dialog.controller(), "contentBaseGroupController"), "clearBaseGroupButton");
  }

  private Button saveButton() {
    return field(dialog.controller(), "saveBtn");
  }

  private boolean shown(String panelField) throws Exception {
    TitledPane root = field(field(dialog.controller(), panelField), "root");
    return root.isVisible() && root.isManaged();
  }

  private boolean errorShown(String panelField) throws Exception {
    ErrorContainerController container = field(field(dialog.controller(), panelField), "errorContainerController");
    return container.errorProperty().get();
  }

  private String errorText(String panelField) throws Exception {
    ErrorContainerController container = field(field(dialog.controller(), panelField), "errorContainerController");
    Label label = field(container, "errorMessage");
    return label.getText();
  }

  private void flush() throws Exception {
    PropertyEditorSaveMode.Deferred saveMode = field(dialog.controller(), "saveMode");
    assertTrue(saveMode.flush(), "the panels committed a change");
  }

  private ContentModel saved() {
    return (ContentModel) new ProjectItem(item.getFile()).getModel();
  }

  /** A Document Model with one root group and the given nested groups (each a JSON object, see {@link #group}). */
  private static String documentModel(String id, String rootName, String rootId, String nestedGroups) {
    return "{\"header\": {\"id\": \"" + id + "\", \"modelType\": \"document\", \"modelVersion\": \"29.4.0\","
        + " \"locales\": [{\"code\": \"en\"}], \"annotations\": [], \"modelReferences\": []},"
        + " \"content\": {\"modelInfo\": {\"name\": \"" + id + "\", \"immutable\": false},"
        + " \"modelConfig\": {\"timeZone\": \"UTC\", \"decimalSeparator\": \".\", \"conditionLanguage\": {\"code\": \"en_US\"}},"
        + " \"modelRoot\": {\"rootGroups\": [{\"type\": \"Group\", \"id\": \"" + rootId + "\", \"name\": \"" + rootName + "\","
        + " \"Group\": {\"repeatability\": 1, \"elements\": [" + nestedGroups + "]}}]}}}";
  }

  private static String group(String name, String id, int repeatability, String... nested) {
    return "{\"type\": \"Group\", \"id\": \"" + id + "\", \"name\": \"" + name + "\", \"Group\": {\"repeatability\": "
        + repeatability + ", \"elements\": [" + String.join(",", nested) + "]}}";
  }

  /** Reads a (typically private) field; unchecked, so the accessors above can be used inside FX-thread lambdas. */
  private static <T> T field(Object target, String name) {
    try {
      return FxTestSupport.field(target, name);
    }
    catch (Exception e) {
      throw new AssertionError(e);
    }
  }

  private static void setStatic(String name, Object value) throws Exception {
    Field field = Studio.class.getDeclaredField(name);
    field.setAccessible(true);
    field.set(null, value);
  }
}
