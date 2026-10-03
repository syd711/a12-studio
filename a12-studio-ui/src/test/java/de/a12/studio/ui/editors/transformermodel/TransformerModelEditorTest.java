package de.a12.studio.ui.editors.transformermodel;

import de.a12.studio.models.ModelType;
import de.a12.studio.models.projects.Project;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.models.transformermodel.A12DataType;
import de.a12.studio.models.transformermodel.TransformerModel;
import de.a12.studio.models.util.JsonSettings;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.editors.AbstractPropertyEditor;
import de.a12.studio.ui.editors.dialogs.ModelSettingsDialog;
import de.a12.studio.ui.editors.formmodel.FxTestSupport;
import de.a12.studio.ui.editors.transformermodel.TransformationOutcome.Discovery;
import de.a12.studio.ui.events.StudioEventManager;
import de.a12.studio.ui.util.Icons;
import javafx.css.PseudoClass;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.TreeView;
import javafx.scene.layout.VBox;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BooleanSupplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * The Transformer Model editor through its FXML: that each of its panels shows its part of the model, what editing does
 * to the model and the file, how the transformation's answer (here the real answer of the installed backend, recorded in
 * {@code /transformer}) reaches the panels, and that an answer to an outdated request is dropped. The transformer itself
 * is replaced by a fake; {@link TransformerRunBackendTest} runs the real one.
 */
class TransformerModelEditorTest {

  private static final String EDITOR_FXML = "/de/a12/studio/ui/editors/transformermodel/transformer-model-editor.fxml";

  private static final PseudoClass ERROR = PseudoClass.getPseudoClass("error");

  private static boolean toolkitAvailable;

  @TempDir
  Path workspace;

  private ProjectItem item;
  private TransformerModel model;
  private final List<TransformerModel> requestedModels = new ArrayList<>();
  private final List<List<File>> requestedXsdFiles = new ArrayList<>();
  private final AtomicInteger runs = new AtomicInteger();

  @BeforeAll
  static void startToolkit() throws Exception {
    toolkitAvailable = FxTestSupport.startToolkit();
  }

  @AfterAll
  static void restoreStudio() throws Exception {
    if (toolkitAvailable) {
      setStatic("currentProject", new Project());
      setStatic("validationService", null);
    }
  }

  @BeforeEach
  void openProject() throws Exception {
    assumeTrue(toolkitAvailable, "No JavaFX toolkit available");
    Files.createDirectories(workspace.resolve("resources"));
    Files.write(workspace.resolve("resources").resolve("EnrollmentCertificate_XSD.xsd"), resource("EnrollmentCertificate_XSD.xsd").getBytes(StandardCharsets.UTF_8));
    Files.write(workspace.resolve("Persons_TfM.json"), resource("Persons_TfM.json").getBytes(StandardCharsets.UTF_8));
    openItem();
  }

  private void openItem() throws Exception {
    Project project = new Project();
    project.load(workspace.toFile());
    setStatic("currentProject", project);
    FxTestSupport.setValidationServiceForProject(project);
    // As the project tree has it: an item below the project's root, so its project folder is the workspace.
    item = project.getRoot().findByModelId("Persons_TfM");
    assertNotNull(item, "the fixture transformer model must be in the project tree");
    model = (TransformerModel) item.getModel();
    assertNotNull(model, "the fixture transformer model must load");
    FxTestSupport.selectProjectItem(item);
  }

  @AfterEach
  void tearDown() throws Exception {
    if (!toolkitAvailable) {
      return;
    }
    if (item != null) {
      StudioEventManager.getInstance().fireModelClosedEvent(item);
    }
    FxTestSupport.onFx(() -> {
    });
    FxTestSupport.clearValidationService();
    FxTestSupport.selectProjectItem(null);
  }

  // ---- helpers --------------------------------------------------------------------------------------------------

  private static String resource(String name) throws Exception {
    try (InputStream in = TransformerModelEditorTest.class.getResourceAsStream("/transformer/" + name)) {
      assertNotNull(in, "Missing test resource " + name);
      return new String(in.readAllBytes(), StandardCharsets.UTF_8);
    }
  }

  private static void setStatic(String name, Object value) throws Exception {
    Field field = Studio.class.getDeclaredField(name);
    field.setAccessible(true);
    field.set(null, value);
  }

  /** The recorded answer of the real transformer to SME's example (see {@link TransformerRunTest}). */
  private static TransformationOutcome realOutcome() throws Exception {
    Discovery discovery = TransformerRun.parseDiscovery(JsonSettings.objectMapper.readTree(resource("discover-response.json")));
    return TransformerRun.parseTransformation(JsonSettings.objectMapper.readTree(resource("transform-response.json")), discovery);
  }

  private TransformerModelEditorController openEditor() throws Exception {
    return openEditor(TransformerModelEditorTest::realOutcomeUnchecked);
  }

  private static TransformationOutcome realOutcomeUnchecked() {
    try {
      return realOutcome();
    }
    catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }

  private TransformerModelEditorController openEditor(java.util.function.Supplier<TransformationOutcome> answer) throws Exception {
    FxTestSupport.Loaded<TransformerModelEditorController> loaded = FxTestSupport.load(EDITOR_FXML);
    TransformerModelEditorController editor = loaded.controller();
    editor.setRunner((snapshot, xsdFiles) -> {
      synchronized (requestedModels) {
        requestedModels.add(snapshot);
        requestedXsdFiles.add(xsdFiles);
      }
      runs.incrementAndGet();
      return answer.get();
    });
    FxTestSupport.onFx(() -> editor.load(item));
    return editor;
  }

  private static void await(BooleanSupplier condition) throws Exception {
    long deadline = System.currentTimeMillis() + 20_000;
    while (System.currentTimeMillis() < deadline) {
      if (FxTestSupport.onFx(() -> condition.getAsBoolean())) {
        return;
      }
      Thread.sleep(50);
    }
    throw new AssertionError("condition not met in time");
  }

  private static void awaitFirstAnswer(TransformerModelEditorController editor) throws Exception {
    await(() -> editor.lastOutcome() != null);
  }

  private static <T> T field(Object target, String name) {
    try {
      return FxTestSupport.field(target, name);
    }
    catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }

  private static <T extends Node> List<T> all(Node root, Class<T> type) {
    List<T> result = new ArrayList<>();
    collect(root, type, result);
    return result;
  }

  private static <T extends Node> void collect(Node node, Class<T> type, List<T> result) {
    if (type.isInstance(node)) {
      result.add(type.cast(node));
    }
    if (node instanceof Parent parent) {
      for (Node child : parent.getChildrenUnmodifiable()) {
        collect(child, type, result);
      }
    }
  }

  private static VBox rows(Object panel) {
    return field(panel, "rows");
  }

  @SuppressWarnings("unchecked")
  private static List<ComboBox<String>> combos(Node root) {
    return all(root, ComboBox.class).stream().map(combo -> (ComboBox<String>) combo).toList();
  }

  private static List<Button> trashButtons(Node root) {
    return all(root, Button.class).stream()
        .filter(button -> button.getGraphic() instanceof FontIcon icon && Icons.TRASH.equals(icon.getIconLiteral())).toList();
  }

  /** The panel's "add" button; a TitledPane without a scene has no skin, so its content is searched, not the pane. */
  private static Button addButton(Object panel) {
    TitledPane root = field(panel, "root");
    Node button = root.getContent().lookup("#addButton");
    assertNotNull(button, "the panel has an add button");
    return (Button) button;
  }

  private static boolean hasError(Node node) {
    return node.getPseudoClassStates().contains(ERROR);
  }

  private TransformerModel fileModel() throws Exception {
    return JsonSettings.objectMapper.readValue(Files.readString(workspace.resolve("Persons_TfM.json"), StandardCharsets.UTF_8), TransformerModel.class);
  }

  /** Waits until the change a panel debounces (150 ms) has been written to the file. */
  private void awaitSaved(BooleanSupplier fileHasChange) throws Exception {
    long deadline = System.currentTimeMillis() + 10_000;
    while (System.currentTimeMillis() < deadline) {
      FxTestSupport.onFx(() -> {
      });
      if (fileHasChange.getAsBoolean()) {
        return;
      }
      Thread.sleep(50);
    }
    throw new AssertionError("the change was not saved in time");
  }

  // ---- the editor and its panels --------------------------------------------------------------------------------

  @Test
  void theEditorShowsEverySectionOfTheModel() throws Exception {
    TransformerModelEditorController editor = openEditor();
    assertEquals(ModelType.TRANSFORMER, editor.getModelType());

    Object source = field(editor, "sourcePanelController");
    VBox sourceFields = field(source, "fields");
    List<ComboBox<String>> sourceCombos = combos(sourceFields);
    assertEquals("EnrollmentCertificate_XSD.xsd", FxTestSupport.onFx(() -> sourceCombos.get(0).getEditor().getText()));
    assertEquals("EnrollmentCertificate", FxTestSupport.onFx(() -> sourceCombos.get(1).getEditor().getText()));

    VBox typeMappings = rows(field(editor, "typeMappingsPanelController"));
    assertEquals(2, typeMappings.getChildren().size(), "one card per type mapping");
    List<ComboBox<String>> firstCard = combos(typeMappings.getChildren().get(0));
    assertEquals("custom_AmountWithTwoDigits", FxTestSupport.onFx(() -> firstCard.get(0).getEditor().getText()));
    assertEquals("NumberType", firstCard.get(1).getValue());
    assertTrue(all(typeMappings.getChildren().get(0), TextField.class).stream().anyMatch(field -> "2".equals(field.getText())),
        "the decimal places of the amount");
    assertTrue(combos(typeMappings.getChildren().get(0)).stream().anyMatch(combo -> "Amount".equals(combo.getValue())), "the unit");

    VBox renames = rows(field(editor, "renamePathsPanelController"));
    assertEquals(1, renames.getChildren().size());
    assertEquals("TaxId", all(renames, TextField.class).stream().map(TextField::getText).filter("TaxId"::equals).findFirst().orElse(null));

    VBox deletes = rows(field(editor, "deletePathsPanelController"));
    assertEquals(1, deletes.getChildren().size());
    assertEquals("/EnrollmentCertificate/CreationDate", FxTestSupport.onFx(() -> combos(deletes).get(0).getEditor().getText()));

    VBox patternErrors = rows(field(editor, "patternErrorsPanelController"));
    assertEquals(1, patternErrors.getChildren().size());
    assertEquals("\\d{11}", FxTestSupport.onFx(() -> combos(patternErrors).get(0).getEditor().getText()));
    assertTrue(all(patternErrors, TextField.class).stream().anyMatch(field -> "[0-9]{11}".equals(field.getText())), "the replacement of REPLACE");
    assertTrue(all(patternErrors, TextField.class).stream().anyMatch(field -> "Eleven digits".equals(field.getText())), "the English message");

    VBox enumLabels = rows(field(editor, "enumLabelsPanelController"));
    assertEquals(1, enumLabels.getChildren().size());
    assertTrue(all(enumLabels, TextField.class).stream().anyMatch(field -> "Baden-Württemberg".equals(field.getText())), "the German text");
  }

  @Test
  void theTextOfEveryLocaleOfTheModelGetsARowAndOnlyThoseOfTheModel() throws Exception {
    TransformerModelEditorController editor = openEditor();
    VBox patternErrors = rows(field(editor, "patternErrorsPanelController"));

    List<javafx.scene.control.Label> locales = all(patternErrors, javafx.scene.control.Label.class).stream()
        .filter(label -> label.getStyleClass().contains("transformer-locale-label")).toList();

    assertEquals(List.of("de", "en"), locales.stream().map(javafx.scene.control.Label::getText).toList(),
        "one text row per locale of the model, in its order");
  }

  @Test
  void theTransformationRunsOnOpeningWithTheModelAndEveryXsdOfTheProject() throws Exception {
    TransformerModelEditorController editor = openEditor();
    awaitFirstAnswer(editor);

    assertEquals(1, runs.get());
    assertEquals("Persons_TfM", requestedModels.get(0).getId());
    assertEquals("EnrollmentCertificate", requestedModels.get(0).getContent().getCmd().getRootElement());
    assertEquals(List.of("EnrollmentCertificate_XSD.xsd"), requestedXsdFiles.get(0).stream().map(File::getName).toList());
  }

  @Test
  void theDiscoveryFeedsTheSuggestionsOfEveryPanel() throws Exception {
    TransformerModelEditorController editor = openEditor();
    awaitFirstAnswer(editor);

    VBox sourceFields = field(field(editor, "sourcePanelController"), "fields");
    List<ComboBox<String>> sourceCombos = combos(sourceFields);
    assertEquals(List.of("EnrollmentCertificate_XSD.xsd"), FxTestSupport.onFx(() -> List.copyOf(sourceCombos.get(0).getItems())));
    assertEquals(List.of("EnrollmentCertificate"), FxTestSupport.onFx(() -> List.copyOf(sourceCombos.get(1).getItems())));
    assertEquals("EnrollmentCertificate", FxTestSupport.onFx(() -> sourceCombos.get(1).getEditor().getText()), "what was typed stays");

    List<String> xsdTypes = FxTestSupport.onFx(() -> List.copyOf(combos(rows(field(editor, "typeMappingsPanelController")).getChildren().get(0)).get(0).getItems()));
    assertTrue(xsdTypes.contains("custom_TaxIDNr") && xsdTypes.contains("custom_AmountWithTwoDigits"), xsdTypes::toString);

    List<String> renamePaths = FxTestSupport.onFx(() -> List.copyOf(combos(rows(field(editor, "renamePathsPanelController"))).get(0).getItems()));
    assertTrue(renamePaths.contains("/EnrollmentCertificate/IdNr"), renamePaths::toString);
    List<String> deletePaths = FxTestSupport.onFx(() -> List.copyOf(combos(rows(field(editor, "deletePathsPanelController"))).get(0).getItems()));
    assertTrue(deletePaths.contains("/EnrollmentCertificate/CreationDate"), deletePaths::toString);

    List<String> patterns = FxTestSupport.onFx(() -> List.copyOf(combos(rows(field(editor, "patternErrorsPanelController"))).get(0).getItems()));
    assertEquals(List.of("\\d{11}"), patterns);

    List<ComboBox<String>> enumCombos = combos(rows(field(editor, "enumLabelsPanelController")));
    assertTrue(FxTestSupport.onFx(() -> enumCombos.get(0).getItems().contains("BW")), "the enumeration values of the XSD");
    assertEquals(List.of("/EnrollmentCertificate/University/State"), FxTestSupport.onFx(() -> List.copyOf(enumCombos.get(1).getItems())),
        "the field paths narrow to the fields the chosen value occurs in");
  }

  @Test
  void theGeneratedDocumentModelIsShownInThePreview() throws Exception {
    TransformerModelEditorController editor = openEditor();
    awaitFirstAnswer(editor);

    Object preview = field(editor, "previewPanelController");
    TreeView<?> tree = field(preview, "tree");
    await(() -> tree.getRoot() != null && !tree.getRoot().getChildren().isEmpty());

    assertEquals(1, FxTestSupport.onFx(() -> tree.getRoot().getChildren().size()), "the one root group");
    assertTrue(FxTestSupport.onFx(() -> tree.getRoot().getChildren().get(0).getChildren().size()) > 3, "with its fields and groups below");
    assertNotNull(editor.lastOutcome().documentModel());
    assertEquals("University_Certificates_TfM", editor.lastOutcome().documentModel().getId());
  }

  @Test
  void aSelectedPreviewElementIsShownReadOnlyWithItsConfiguration() throws Exception {
    TransformerModelEditorController editor = openEditor();
    awaitFirstAnswer(editor);
    Object preview = field(editor, "previewPanelController");
    TreeView<?> tree = field(preview, "tree");
    await(() -> tree.getRoot() != null && !tree.getRoot().getChildren().isEmpty());

    javafx.scene.control.TextArea json = field(preview, "detailsJson");
    FxTestSupport.onFx(() -> {
      tree.getSelectionModel().select(0);
      return null;
    });

    assertFalse(FxTestSupport.onFx(() -> json.isEditable()), "the details can not be edited");
    assertTrue(FxTestSupport.onFx(() -> json.getText()).contains("\"name\""), "the element's configuration is shown");
    assertFalse(FxTestSupport.onFx(() -> json.getText()).contains("\"elements\""), "a group without its children");
  }

  @Test
  void theIssuesOfTheTransformationAreListedWithoutTheInformationMessagesUnlessAskedFor() throws Exception {
    TransformerModelEditorController editor = openEditor();
    awaitFirstAnswer(editor);
    TransformationIssuesPanelController issues = field(editor, "issuesPanelController");

    assertTrue(FxTestSupport.onFx(() -> issues.statusText()).contains("University_Certificates_TfM"), "the status names the generated model");
    assertTrue(FxTestSupport.onFx(() -> issues.shownIssues()).isEmpty(), "the real answer has only information messages");

    CheckBox showInfo = field(issues, "showInfoCheckBox");
    FxTestSupport.onFx(() -> showInfo.setSelected(true));
    assertTrue(FxTestSupport.onFx(() -> issues.shownIssues()).size() > 5, "the 'Fallback to default type mappings' messages");
    FxTestSupport.onFx(() -> showInfo.setSelected(false));
    assertTrue(FxTestSupport.onFx(() -> issues.shownIssues()).isEmpty());
  }

  @Test
  void aTransformationThatCannotRunIsSaidSoAndTheEditorStaysUsable() throws Exception {
    TransformerModelEditorController editor = openEditor(() -> TransformationOutcome.unavailable("No A12 installation folder is configured."));
    awaitFirstAnswer(editor);
    TransformationIssuesPanelController issues = field(editor, "issuesPanelController");

    assertTrue(FxTestSupport.onFx(() -> issues.statusText()).contains("No A12 installation folder is configured."), () -> issues.statusText());
    VBox renames = rows(field(editor, "renamePathsPanelController"));
    FxTestSupport.onFx(() -> combos(renames).get(0).getEditor().setText("/Any/Path"));
    awaitSaved(() -> {
      try {
        return "/Any/Path".equals(fileModel().getContent().getRenamePaths().get(0).getOriginalPath());
      }
      catch (Exception e) {
        return false;
      }
    });
  }

  @Test
  void anAnswerToAnOutdatedRequestIsDropped() throws Exception {
    CountDownLatch release = new CountDownLatch(1);
    AtomicInteger calls = new AtomicInteger();
    TransformerModelEditorController editor = openEditor(() -> {
      if (calls.incrementAndGet() == 1) {
        try {
          release.await(20, TimeUnit.SECONDS);
        }
        catch (InterruptedException e) {
          Thread.currentThread().interrupt();
        }
        return TransformationOutcome.incomplete("answer to the first request");
      }
      return TransformationOutcome.incomplete("answer to the second request");
    });
    FxTestSupport.onFx(editor::runNow);
    release.countDown();

    await(() -> editor.lastOutcome() != null);
    Thread.sleep(300);
    FxTestSupport.onFx(() -> {
    });

    assertEquals(2, calls.get());
    assertEquals("answer to the second request", editor.lastOutcome().message());
  }

  // ---- editing --------------------------------------------------------------------------------------------------

  @Test
  void editingARowWritesTheModelSavesTheFileAndTransformsAgain() throws Exception {
    TransformerModelEditorController editor = openEditor();
    awaitFirstAnswer(editor);
    VBox renames = rows(field(editor, "renamePathsPanelController"));
    TextField newName = all(renames, TextField.class).stream().filter(field -> "TaxId".equals(field.getText())).findFirst().orElseThrow();

    FxTestSupport.onFx(() -> newName.setText("TaxNumber"));

    awaitSaved(() -> {
      try {
        return "TaxNumber".equals(fileModel().getContent().getRenamePaths().get(0).getNewElementName());
      }
      catch (Exception e) {
        return false;
      }
    });
    assertEquals("TaxNumber", model.getContent().getRenamePaths().get(0).getNewElementName());
    await(() -> runs.get() >= 2);
    assertEquals("TaxNumber", requestedModels.get(runs.get() - 1).getContent().getRenamePaths().get(0).getNewElementName(),
        "the next transformation sees the edit");
  }

  @Test
  void clearingATextRemovesItFromTheModel() throws Exception {
    TransformerModelEditorController editor = openEditor();
    awaitFirstAnswer(editor);
    VBox patternErrors = rows(field(editor, "patternErrorsPanelController"));
    TextField english = all(patternErrors, TextField.class).stream().filter(field -> "Eleven digits".equals(field.getText())).findFirst().orElseThrow();

    FxTestSupport.onFx(() -> english.setText(""));

    awaitSaved(() -> {
      try {
        return fileModel().getContent().getPatternErrors().get(0).getErrors() == null;
      }
      catch (Exception e) {
        return false;
      }
    });
    assertNull(model.getContent().getPatternErrors().get(0).getErrors(), "the emptied list is dropped, not written as []");
  }

  @Test
  void theConfigurationFieldsFollowTheDataTypeAndSwitchingClearsWhatNoLongerApplies() throws Exception {
    TransformerModelEditorController editor = openEditor();
    awaitFirstAnswer(editor);
    VBox typeMappings = rows(field(editor, "typeMappingsPanelController"));

    // NumberType: three tri-state options, an amount unit; StringType: two options.
    assertEquals(3, all(typeMappings.getChildren().get(0), CheckBox.class).size());
    assertEquals(2, all(typeMappings.getChildren().get(1), CheckBox.class).size());

    ComboBox<String> a12Type = combos(typeMappings.getChildren().get(0)).get(1);
    FxTestSupport.onFx(() -> a12Type.setValue(A12DataType.DATE.getValue()));

    assertNull(model.getContent().getTypeMapping().get(0).getNumberType(), "the Number configuration is cleared with the type");
    assertEquals("DateType", model.getContent().getTypeMapping().get(0).getA12Type());
    VBox rebuilt = rows(field(editor, "typeMappingsPanelController"));
    assertEquals(0, FxTestSupport.onFx(() -> all(rebuilt.getChildren().get(0), CheckBox.class).size()), "a date has no configuration");
    awaitSaved(() -> {
      try {
        return "DateType".equals(fileModel().getContent().getTypeMapping().get(0).getA12Type());
      }
      catch (Exception e) {
        return false;
      }
    });
    assertFalse(Files.readString(workspace.resolve("Persons_TfM.json")).contains("minFractionalDigits"), "and gone from the file");
  }

  @Test
  void aNumberTypedIntoAConfigurationFieldIsStoredAndOnlyDigitsAreAccepted() throws Exception {
    TransformerModelEditorController editor = openEditor();
    awaitFirstAnswer(editor);
    VBox typeMappings = rows(field(editor, "typeMappingsPanelController"));
    TextField minLength = all(typeMappings.getChildren().get(1), TextField.class).stream().filter(field -> "11".equals(field.getText())).findFirst().orElseThrow();

    FxTestSupport.onFx(() -> minLength.setText("12"));
    FxTestSupport.onFx(() -> minLength.setText("12x"));

    assertEquals("12", FxTestSupport.onFx(() -> minLength.getText()), "a letter is rejected");
    awaitSaved(() -> {
      try {
        return Integer.valueOf(12).equals(fileModel().getContent().getTypeMapping().get(1).getStringType().getMinLength());
      }
      catch (Exception e) {
        return false;
      }
    });
  }

  @Test
  void aRowCanBeAddedToAndRemovedFromEveryList() throws Exception {
    TransformerModelEditorController editor = openEditor();
    awaitFirstAnswer(editor);

    String[] panels = {"typeMappingsPanelController", "renamePathsPanelController", "deletePathsPanelController",
        "patternErrorsPanelController", "enumLabelsPanelController"};
    for (String panel : panels) {
      Object controller = field(editor, panel);
      VBox rows = rows(controller);
      int before = FxTestSupport.onFx(() -> rows.getChildren().size());
      Button add = addButton(controller);

      FxTestSupport.onFx(add::fire);
      assertEquals(before + 1, FxTestSupport.onFx(() -> rows.getChildren().size()), panel + " after adding");

      List<Button> trash = FxTestSupport.onFx(() -> trashButtons(rows));
      FxTestSupport.onFx(() -> trash.get(trash.size() - 1).fire());
      assertEquals(before, FxTestSupport.onFx(() -> rows.getChildren().size()), panel + " after removing the new one again");
    }
  }

  @Test
  void anAddedRowThatIsEmptyIsReportedWhereItIsAndSavedAsAnEmptyEntry() throws Exception {
    TransformerModelEditorController editor = openEditor();
    awaitFirstAnswer(editor);
    Object controller = field(editor, "renamePathsPanelController");

    FxTestSupport.onFx(() -> addButton(controller).fire());

    VBox rows = rows(controller);
    List<ComboBox<String>> originalPaths = FxTestSupport.onFx(() -> combos(rows));
    assertTrue(FxTestSupport.onFx(() -> hasError(originalPaths.get(1))), "the empty original path is marked");
    List<TextField> names = FxTestSupport.onFx(() -> all(rows.getChildren().get(1), TextField.class).stream()
        .filter(field -> field.getPromptText() != null && !field.getPromptText().isEmpty()).toList());
    assertTrue(FxTestSupport.onFx(() -> hasError(names.get(names.size() - 1))), "and the empty new element name");
    assertFalse(FxTestSupport.onFx(() -> hasError(originalPaths.get(0))), "the filled row is fine");
    AbstractPanelAccess.assertPanelShowsError(controller, true);
  }

  @Test
  void twoTypeMappingsOfOneXsdTypeAreReportedOnTheSecondOne() throws Exception {
    TransformerModelEditorController editor = openEditor();
    awaitFirstAnswer(editor);
    VBox typeMappings = rows(field(editor, "typeMappingsPanelController"));
    List<ComboBox<String>> secondXsdType = FxTestSupport.onFx(() -> combos(typeMappings.getChildren().get(1)));

    FxTestSupport.onFx(() -> secondXsdType.get(0).getEditor().setText("custom_AmountWithTwoDigits"));
    awaitSaved(() -> "custom_AmountWithTwoDigits".equals(model.getContent().getTypeMapping().get(1).getXsdType()));
    await(() -> hasError(secondXsdType.get(0)));

    assertFalse(FxTestSupport.onFx(() -> hasError(combos(typeMappings.getChildren().get(0)).get(0))), "only the repeated one");
    AbstractPanelAccess.assertPanelShowsError(field(editor, "typeMappingsPanelController"), true);
  }

  @Test
  void aMainXsdThatIsNotAProjectFileIsReportedOnTheGeneralSection() throws Exception {
    TransformerModelEditorController editor = openEditor();
    awaitFirstAnswer(editor);
    Object source = field(editor, "sourcePanelController");
    VBox fields = field(source, "fields");
    List<ComboBox<String>> sourceCombos = FxTestSupport.onFx(() -> combos(fields));

    FxTestSupport.onFx(() -> sourceCombos.get(0).getEditor().setText("Missing.xsd"));

    await(() -> hasError(sourceCombos.get(0)));
    assertEquals("Missing.xsd", model.getContent().getCmd().getMainXsd());
    AbstractPanelAccess.assertPanelShowsError(source, true);
  }

  // ---- the Settings tab of SME is the generic Model Settings dialog ------------------------------------------------

  @Test
  void theSettingsDialogOffersWhatSmeSettingsTabHasAndNothingForADocumentModel() throws Exception {
    FxTestSupport.Loaded<ModelSettingsDialog> loaded = FxTestSupport.load("/de/a12/studio/ui/editors/dialogs/document-model-settings-dialog.fxml");

    // SME's Settings tab: name, version (not editable), description, locales, labels, roles, annotations.
    for (String name : List.of("modelSettingsNameController", "localesController", "labelsController", "rolesController", "annotationsController")) {
      assertTrue(rootPane(FxTestSupport.field(loaded.controller(), name)).isVisible(), name + " must be offered");
    }
    // Not the Document Model's own supported characters (a Transformer Model's are part of its content), nor free references.
    for (String name : List.of("supportedCharactersController", "modelReferencesController", "timezoneController", "modelConfigController",
        "modelInfoController", "documentUniquenessCriteriaController")) {
      assertFalse(rootPane(FxTestSupport.field(loaded.controller(), name)).isVisible(), name + " must be hidden");
    }
    Button save = FxTestSupport.field(loaded.controller(), "saveBtn");
    assertFalse(FxTestSupport.onFx(() -> save.isDisabled()), "a valid model can be saved");
    for (Field field : ModelSettingsDialog.class.getDeclaredFields()) {
      if (AbstractPropertyEditor.class.isAssignableFrom(field.getType())) {
        field.setAccessible(true);
        ((AbstractPropertyEditor) field.get(loaded.controller())).destroy();
      }
    }
  }

  private static TitledPane rootPane(Object panel) throws Exception {
    return FxTestSupport.field(panel, "root");
  }

  /** Reads a panel's error container state through its public property. */
  private static final class AbstractPanelAccess {

    static void assertPanelShowsError(Object panel, boolean expected) throws Exception {
      TransformerModelPanelController controller = (TransformerModelPanelController) panel;
      long deadline = System.currentTimeMillis() + 5_000;
      while (System.currentTimeMillis() < deadline && FxTestSupport.onFx(() -> controller.errorProperty().get()) != expected) {
        Thread.sleep(50);
      }
      assertEquals(expected, FxTestSupport.onFx(() -> controller.errorProperty().get()), "the panel's own error container");
    }
  }
}
