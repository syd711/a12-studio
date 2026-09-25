package de.a12.studio.ui.editors.typesettingmodel;

import de.a12.studio.models.ModelType;
import de.a12.studio.models.NewModelFactory;
import de.a12.studio.models.projects.Project;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.models.typesettingmodel.PreventLineBreakRule;
import de.a12.studio.models.typesettingmodel.PreventLineBreakRuleType;
import de.a12.studio.models.typesettingmodel.PreventLineBreakRules;
import de.a12.studio.models.typesettingmodel.SpecialPattern;
import de.a12.studio.models.typesettingmodel.TypesettingModel;
import de.a12.studio.modelsvalidation.ValidationMessages;
import de.a12.studio.modelsvalidation.ValidationService;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.components.ErrorContainerController;
import de.a12.studio.ui.editors.AbstractPropertyEditor;
import de.a12.studio.ui.editors.dialogs.ModelSettingsDialog;
import de.a12.studio.ui.editors.formmodel.FxTestSupport;
import de.a12.studio.ui.events.StudioEventManager;
import javafx.css.PseudoClass;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.GridPane;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * The Typesetting Model editor: its FXML wiring, the three rule panels (one per rule kind, over one shared
 * list), the orphan/widow panel, and the roles-only Model Settings dialog. Panels save through a 150 ms
 * debouncer, so tests that type into a field wait for the file to change ({@link #awaitSaved}).
 */
class TypesettingModelEditorTest {

  private static final String EDITOR_FXML = "/de/a12/studio/ui/editors/typesettingmodel/typesetting-model-editor.fxml";
  private static final String SETTINGS_FXML = "/de/a12/studio/ui/editors/dialogs/document-model-settings-dialog.fxml";
  private static final PseudoClass ERROR = PseudoClass.getPseudoClass("error");

  private static boolean toolkitAvailable;

  @TempDir
  Path workspace;

  private ProjectItem item;
  private TypesettingModel model;
  private final List<AbstractPropertyEditor> panels = new ArrayList<>();

  @BeforeAll
  static void startToolkit() throws Exception {
    toolkitAvailable = FxTestSupport.startToolkit();
  }

  @AfterAll
  static void restoreStudio() throws Exception {
    if (toolkitAvailable) {
      setStatic("currentProject", new Project());
      setStatic("validationService", null);
      FxTestSupport.selectProjectItem(null);
    }
  }

  @BeforeEach
  void openProject() throws Exception {
    assumeTrue(toolkitAvailable, "No JavaFX toolkit available");
    ProjectItem folder = new ProjectItem(workspace.toFile());
    folder.setRoot(true);
    ProjectItem created = NewModelFactory.createModel(folder, ModelType.TYPESETTING, "Team_TSM");
    TypesettingModel fresh = (TypesettingModel) created.getModel();
    fresh.getContent().getPreventLineBreakRules().addAll(List.of(
        rule(PreventLineBreakRuleType.CHARACTER_SEQUENCE, "T-shirt"),
        rule(PreventLineBreakRuleType.CHARACTER_SEQUENCE, "E-mail"),
        rule(PreventLineBreakRuleType.NUMBER_UNIT, "Km"),
        new PreventLineBreakRule(SpecialPattern.PARAGRAPH_SECTION.regex())));
    fresh.getContent().setOrphan(3);
    created.save();

    Project project = new Project();
    project.load(workspace.toFile());
    setStatic("currentProject", project);
    setStatic("validationService", new ValidationService(project));

    item = new ProjectItem(created.getFile());
    model = (TypesettingModel) item.getModel();
    assertNotNull(model, "the fixture typesetting model must load");
    FxTestSupport.selectProjectItem(item);
  }

  @AfterEach
  void tearDown() throws Exception {
    // Unregisters the panels from the application-wide event manager (and cancels pending debounced saves).
    for (AbstractPropertyEditor panel : panels) {
      panel.destroy();
    }
    if (!toolkitAvailable) {
      return;
    }
    if (item != null) {
      StudioEventManager.getInstance().fireModelClosedEvent(item);
    }
    FxTestSupport.onFx(() -> {
    });
    FxTestSupport.selectProjectItem(null);
  }

  // ---- editor ----

  @Test
  void theEditorLoadsItsFourPanelsAndSplitsTheRulesByKind() throws Exception {
    FxTestSupport.Loaded<TypesettingModelEditorController> loaded = FxTestSupport.load(EDITOR_FXML);
    FxTestSupport.onFx(() -> loaded.controller().load(item));

    for (String name : List.of("characterSequenceRulesPanelController", "numberUnitRulesPanelController",
        "specialPatternRulesPanelController", "orphanWidowPanelController")) {
      AbstractPropertyEditor panel = FxTestSupport.field(loaded.controller(), name);
      assertNotNull(panel, name + " must be injected");
      panels.add(panel);
    }
    assertEquals(ModelType.TYPESETTING, loaded.controller().getModelType());
    assertEquals(2, rows(panels.get(0)));
    assertEquals(1, rows(panels.get(1)));
    assertEquals(1, rows(panels.get(2)));
    for (AbstractPropertyEditor panel : panels) {
      assertFalse(errorShown(panel), "a valid model shows no error in " + panel.getClass().getSimpleName());
    }
  }

  // ---- rule panels ----

  @Test
  void aRowShowsThePlainValueNotTheRegex() throws Exception {
    CharacterSequenceRulesPanelController characters = openRules("character-sequence-rules-panel.fxml");
    NumberUnitRulesPanelController units = openRules("number-unit-rules-panel.fxml");
    SpecialPatternRulesPanelController specials = openRules("special-pattern-rules-panel.fxml");

    assertEquals("T-shirt", ((TextField) valueControl(characters, 0)).getText());
    assertEquals("E-mail", ((TextField) valueControl(characters, 1)).getText());
    assertEquals("Km", ((TextField) valueControl(units, 0)).getText());
    ComboBox<SpecialPattern> combo = valueControl(specials, 0);
    assertEquals(SpecialPattern.PARAGRAPH_SECTION, combo.getValue());
    assertEquals(PreventLineBreakRules.SPECIAL_PATTERNS, combo.getItems());
    assertFalse(FxTestSupport.<Label>field(characters, "emptyLabel").isVisible());
  }

  @Test
  void addAppendsAnEmptyRuleOfThePanelsKindReportsItAndSavesIt() throws Exception {
    NumberUnitRulesPanelController units = openRules("number-unit-rules-panel.fxml");

    FxTestSupport.onFx(() -> invokeAdd(units));

    assertEquals(2, rows(units));
    List<PreventLineBreakRule> rules = model.getContent().getPreventLineBreakRules();
    assertEquals(PreventLineBreakRuleType.NUMBER_UNIT.emptyPattern(), rules.get(rules.size() - 1).getPattern());
    assertEquals(PreventLineBreakRuleType.NUMBER_UNIT, PreventLineBreakRules.classify(rules.get(rules.size() - 1).getPattern()));
    assertTrue(errorShown(units));
    assertEquals(ValidationMessages.get("validation.typesetting.unitRequired", 2, 20), errorText(units));
    assertTrue(((Node) valueControl(units, 1)).getPseudoClassStates().contains(ERROR), "the empty row is marked");
    assertFalse(((Node) valueControl(units, 0)).getPseudoClassStates().contains(ERROR));
    awaitSaved(() -> savedRules().size() == 5);
    assertEquals(PreventLineBreakRuleType.NUMBER_UNIT.emptyPattern(), savedRules().get(4).getPattern());
  }

  @Test
  void typingAValueStoresItAsTheEscapedRegexAndClearsTheError() throws Exception {
    NumberUnitRulesPanelController units = openRules("number-unit-rules-panel.fxml");
    FxTestSupport.onFx(() -> invokeAdd(units));
    assertTrue(errorShown(units));

    TextField unitField = valueControl(units, 1);
    FxTestSupport.onFx(() -> unitField.setText("$"));

    assertEquals(PreventLineBreakRules.NUMBER_UNIT_PREFIX + "\\$", model.getContent().getPreventLineBreakRules().get(4).getPattern());
    awaitSaved(() -> savedRules().size() == 5 && savedRules().get(4).getPattern().endsWith("\\$"));
    awaitCondition(() -> !errorShown(units));
    assertFalse(unitField.getPseudoClassStates().contains(ERROR));
    assertEquals(1, savedRules().stream()
        .filter(rule -> PreventLineBreakRules.NUMBER_UNIT_PREFIX.concat("\\$").equals(rule.getPattern())).count());
  }

  @Test
  void anInvalidValueIsMarkedOnItsRowAndNamesTheRow() throws Exception {
    CharacterSequenceRulesPanelController characters = openRules("character-sequence-rules-panel.fxml");
    TextField second = valueControl(characters, 1);

    FxTestSupport.onFx(() -> second.setText("E-mail 2"));

    awaitCondition(() -> errorShown(characters));
    assertEquals(ValidationMessages.get("validation.typesetting.characterInvalid", 2, 20), errorText(characters));
    assertTrue(second.getPseudoClassStates().contains(ERROR));
    assertFalse(((Node) valueControl(characters, 0)).getPseudoClassStates().contains(ERROR));
  }

  @Test
  void aDuplicateMarksBothRowsAndTheOtherPanelsAreUnaffected() throws Exception {
    CharacterSequenceRulesPanelController characters = openRules("character-sequence-rules-panel.fxml");
    NumberUnitRulesPanelController units = openRules("number-unit-rules-panel.fxml");
    TextField second = valueControl(characters, 1);

    FxTestSupport.onFx(() -> second.setText("T-shirt"));

    awaitCondition(() -> errorShown(characters));
    assertEquals(ValidationMessages.get("validation.typesetting.duplicateCharacter", 1), errorText(characters));
    assertTrue(((Node) valueControl(characters, 0)).getPseudoClassStates().contains(ERROR));
    assertTrue(second.getPseudoClassStates().contains(ERROR));
    assertFalse(errorShown(units));
  }

  @Test
  void choosingASpecialPatternStoresItsRegex() throws Exception {
    SpecialPatternRulesPanelController specials = openRules("special-pattern-rules-panel.fxml");
    ComboBox<SpecialPattern> combo = valueControl(specials, 0);

    FxTestSupport.onFx(() -> combo.setValue(SpecialPattern.DOCUMENT_SECTION));

    assertEquals(SpecialPattern.DOCUMENT_SECTION.regex(), model.getContent().getPreventLineBreakRules().get(3).getPattern());
    awaitSaved(() -> savedRules().size() == 4 && SpecialPattern.DOCUMENT_SECTION.regex().equals(savedRules().get(3).getPattern()));
    assertFalse(errorShown(specials));
  }

  @Test
  void addedSpecialRowWaitsForAChoiceAndIsRequired() throws Exception {
    SpecialPatternRulesPanelController specials = openRules("special-pattern-rules-panel.fxml");

    FxTestSupport.onFx(() -> invokeAdd(specials));

    ComboBox<SpecialPattern> combo = valueControl(specials, 1);
    assertNull(combo.getValue());
    assertEquals(ValidationMessages.get("validation.typesetting.specialRequired", 2), errorText(specials));
    assertTrue(combo.getStyleClass().contains("validation-error"), "combo boxes are marked through their style class");
  }

  // ---- orphans and widows ----

  @Test
  void orphanAndWidowShowTheModelsValuesAndAnAbsentOneAsTheDefault() throws Exception {
    model.getContent().setWidow(null);
    OrphanWidowPanelController panel = openOrphanWidow();

    assertEquals(3, FxTestSupport.<Spinner<Integer>>field(panel, "orphanSpinner").getValue());
    assertEquals(2, FxTestSupport.<Spinner<Integer>>field(panel, "widowSpinner").getValue());
    assertNull(model.getContent().getWidow(), "showing the default must not write it");
    assertFalse(errorShown(panel));
  }

  @Test
  void changingALimitSavesIt() throws Exception {
    OrphanWidowPanelController panel = openOrphanWidow();
    Spinner<Integer> orphan = FxTestSupport.field(panel, "orphanSpinner");

    FxTestSupport.onFx(() -> orphan.getValueFactory().setValue(5));

    assertEquals(5, model.getContent().getOrphan());
    awaitSaved(() -> Integer.valueOf(5).equals(saved().getContent().getOrphan()));
    assertFalse(errorShown(panel));
  }

  @Test
  void aLimitOutsideTheRangeInTheFileIsShownAsItIsAndReported() throws Exception {
    model.getContent().setOrphan(11);
    OrphanWidowPanelController panel = openOrphanWidow();
    Spinner<Integer> orphan = FxTestSupport.field(panel, "orphanSpinner");

    assertEquals("11", orphan.getEditor().getText(), "the spinner's own value would be clamped to 10");
    assertEquals(11, model.getContent().getOrphan(), "showing it must not change it");
    assertEquals(ValidationMessages.get("validation.typesetting.orphanOutOfRange", 0, 10), errorText(panel));
    assertTrue(orphan.getEditor().getPseudoClassStates().contains(ERROR));

    // the spinner keeps typed values within the range, which clears the error
    FxTestSupport.onFx(() -> orphan.getValueFactory().setValue(11));
    assertEquals(10, model.getContent().getOrphan());
    assertFalse(errorShown(panel));
    assertFalse(orphan.getEditor().getPseudoClassStates().contains(ERROR));
  }

  // ---- model settings ----

  @Test
  void theModelSettingsDialogOffersNothingButTheRoles() throws Exception {
    FxTestSupport.Loaded<ModelSettingsDialog> loaded = FxTestSupport.load(SETTINGS_FXML);

    for (String name : List.of("modelSettingsNameController", "supportedCharactersController", "localesController",
        "labelsController", "annotationsController", "modelReferencesController")) {
      assertFalse(rootPane(FxTestSupport.field(loaded.controller(), name)).isVisible(), name + " must be hidden");
      assertFalse(rootPane(FxTestSupport.field(loaded.controller(), name)).isManaged(), name + " must not take space");
    }
    assertTrue(rootPane(FxTestSupport.field(loaded.controller(), "rolesController")).isVisible());
    Button save = FxTestSupport.field(loaded.controller(), "saveBtn");
    assertFalse(save.isDisabled(), "no locale is required, so nothing blocks Save");
    // Every panel the dialog embeds registered itself with the application-wide event manager.
    for (Field field : ModelSettingsDialog.class.getDeclaredFields()) {
      if (AbstractPropertyEditor.class.isAssignableFrom(field.getType())) {
        field.setAccessible(true);
        ((AbstractPropertyEditor) field.get(loaded.controller())).destroy();
      }
    }
  }

  // ---- helpers ----

  private <T extends AbstractRulesPanelController> T openRules(String panelFxml) throws Exception {
    FxTestSupport.Loaded<T> loaded = FxTestSupport.load("/de/a12/studio/ui/editors/typesettingmodel/" + panelFxml);
    panels.add(loaded.controller());
    FxTestSupport.onFx(() -> loaded.controller().setModel(model));
    return loaded.controller();
  }

  private OrphanWidowPanelController openOrphanWidow() throws Exception {
    FxTestSupport.Loaded<OrphanWidowPanelController> loaded = FxTestSupport.load(
        "/de/a12/studio/ui/editors/typesettingmodel/orphan-widow-panel.fxml");
    panels.add(loaded.controller());
    FxTestSupport.onFx(() -> loaded.controller().setModel(model));
    return loaded.controller();
  }

  private static int rows(AbstractPropertyEditor panel) throws Exception {
    GridPane grid = FxTestSupport.field(panel, "rulesGrid");
    // every row is the value control plus its actions box
    return grid.getChildren().size() / 2;
  }

  @SuppressWarnings("unchecked")
  private static <T extends Node> T valueControl(AbstractPropertyEditor panel, int row) throws Exception {
    GridPane grid = FxTestSupport.field(panel, "rulesGrid");
    return (T) grid.getChildren().get(row * 2);
  }

  private static void invokeAdd(AbstractRulesPanelController panel) {
    try {
      Method onAdd = AbstractRulesPanelController.class.getDeclaredMethod("onAdd");
      onAdd.setAccessible(true);
      onAdd.invoke(panel);
    }
    catch (ReflectiveOperationException e) {
      throw new AssertionError(e);
    }
  }

  private static TitledPane rootPane(AbstractPropertyEditor panel) throws Exception {
    return FxTestSupport.field(panel, "root");
  }

  private static boolean errorShown(AbstractPropertyEditor panel) {
    try {
      ErrorContainerController container = FxTestSupport.field(panel, "errorContainerController");
      return container.errorProperty().get();
    }
    catch (Exception e) {
      throw new AssertionError(e);
    }
  }

  private static String errorText(AbstractPropertyEditor panel) throws Exception {
    ErrorContainerController container = FxTestSupport.field(panel, "errorContainerController");
    Label label = FxTestSupport.field(container, "errorMessage");
    return label.getText();
  }

  private static PreventLineBreakRule rule(PreventLineBreakRuleType type, String value) {
    return new PreventLineBreakRule(PreventLineBreakRules.toPattern(type, value));
  }

  private TypesettingModel saved() {
    return (TypesettingModel) new ProjectItem(item.getFile()).getModel();
  }

  private List<PreventLineBreakRule> savedRules() {
    return saved().getContent().getPreventLineBreakRules();
  }

  /** Waits (up to 5 s) for the debounced save to have put the expected state on disk. */
  private void awaitSaved(BooleanSupplier onDisk) throws Exception {
    awaitCondition(onDisk);
  }

  private static void awaitCondition(BooleanSupplier condition) throws Exception {
    long deadline = System.currentTimeMillis() + 5000;
    while (System.currentTimeMillis() < deadline) {
      FxTestSupport.onFx(() -> {
      });
      if (condition.getAsBoolean()) {
        return;
      }
      Thread.sleep(50);
    }
    assertTrue(condition.getAsBoolean(), "condition not reached in time");
  }

  private static void setStatic(String name, Object value) throws Exception {
    Field field = Studio.class.getDeclaredField(name);
    field.setAccessible(true);
    field.set(null, value);
  }
}
