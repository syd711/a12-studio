package de.a12.studio.ui.editors.documentmodel;

import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.documentmodel.Element;
import de.a12.studio.models.documentmodel.FieldElement;
import de.a12.studio.models.documentmodel.GroupElement;
import de.a12.studio.models.documentmodel.TypeDefFieldType;
import de.a12.studio.models.projects.Project;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.models.util.JsonSettings;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.components.SearchFieldController;
import de.a12.studio.ui.editors.documentmodel.DocumentModelTreeFilter.ElementKind;
import de.a12.studio.ui.editors.documentmodel.DocumentModelTreeFilter.FieldKind;
import de.a12.studio.ui.editors.formmodel.FxTestSupport;
import de.a12.studio.ui.util.Icons;
import de.a12.studio.ui.util.ProjectDocumentModels;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Popup;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * The Document Model tree with a real JavaFX toolkit: the filter popup driving what the tree shows, and the
 * cut/copy/paste shortcuts and bulk actions as single undo steps. Works on copies of the basic workspace's
 * Invoice/Order models (the invoice includes three other models, Order_DM owns a type definition), since every
 * tree action saves the model.
 */
class DocumentModelTreeFxTest {

  private static boolean toolkitAvailable;

  @TempDir
  Path workspace;

  @BeforeAll
  static void startToolkit() throws Exception {
    toolkitAvailable = FxTestSupport.startToolkit();
  }

  @AfterAll
  static void restoreEmptyProject() throws Exception {
    if (toolkitAvailable) {
      setCurrentProject(new Project());
    }
  }

  // ---- filter -----------------------------------------------------------------------------------------------

  @Test
  void aSearchTextKeepsTheMatchesAndTheirAncestorsAndNothingElse() throws Exception {
    Fixture fixture = open("Invoice_DM");

    search(fixture, "billingaddress");

    List<String> leaves = leafNames(fixture);
    assertFalse(leaves.isEmpty());
    assertTrue(leaves.stream().allMatch(name -> name.toLowerCase().contains("billingaddress")), leaves.toString());
    assertTrue(rowNames(fixture).contains("Invoice"), "the ancestors of a match stay");

    search(fixture, "no such element");
    assertTrue(fixture.tree.getRoot().getChildren().isEmpty());
  }

  @Test
  void searchingByIdFindsTheElementWithThatId() throws Exception {
    Fixture fixture = open("Order_DM");
    Element target = element(fixture, "PrefDeliveryTime");
    RadioButton idRadio = FxTestSupport.field(fixture.popup, "searchInIdRadio");
    FxTestSupport.onFx(() -> idRadio.setSelected(true));

    search(fixture, target.getId());

    assertEquals(List.of("PrefDeliveryTime"), leafNames(fixture));
  }

  @Test
  void searchingByNameStillWorksAfterSwitchingBack() throws Exception {
    Fixture fixture = open("Order_DM");
    RadioButton idRadio = FxTestSupport.field(fixture.popup, "searchInIdRadio");
    RadioButton nameRadio = FxTestSupport.field(fixture.popup, "searchInNameRadio");
    FxTestSupport.onFx(() -> idRadio.setSelected(true));
    FxTestSupport.onFx(() -> nameRadio.setSelected(true));

    search(fixture, "TermsAnd");

    assertEquals(List.of("TermsAndConditions"), leafNames(fixture));
  }

  @Test
  void switchingIncludesOffRemovesTheIncludedContent() throws Exception {
    Fixture fixture = open("Invoice_DM");
    Set<String> before = Set.copyOf(rowNames(fixture));
    assertTrue(before.contains("Addresses") && before.contains("Order") && before.contains("PaymentInfo"), before.toString());

    setShown(fixture, ElementKind.INCLUDES, false);

    assertEquals(Set.of("Invoice", "BillingAddress", "BillingAddressComp"), Set.copyOf(rowNames(fixture)));
  }

  @Test
  void switchingRulesAndComputationsOffLeavesFieldsAndGroups() throws Exception {
    Fixture fixture = open("Order_DM");
    setShown(fixture, ElementKind.VALIDATION_RULES, false);
    setShown(fixture, ElementKind.COMPUTATION_RULES, false);

    List<Element> shown = shownElements(fixture);

    assertFalse(shown.isEmpty());
    assertTrue(shown.stream().noneMatch(element -> element.getClass().getSimpleName().matches("RuleElement|ComputationElement")));
    assertTrue(rowNames(fixture).contains("OrderNumber"));
  }

  @Test
  void hidingAFieldTypeAlsoHidesFieldsWhoseTypeDefinitionResolvesToIt() throws Exception {
    Fixture fixture = open("Order_DM");
    List<String> before = rowNames(fixture);
    // ProductType and ProductTypeCustomSelect are TypeDefType fields whose type definition is an enumeration
    assertTrue(before.containsAll(List.of("ProductType", "ProductTypeCustomSelect", "Currency", "OrderNumber")), before.toString());

    setShown(fixture, FieldKind.ENUMERATION, false);

    List<String> after = rowNames(fixture);
    assertTrue(after.contains("OrderNumber"), "numbers are not affected");
    for (String enumeration : List.of("ProductType", "ProductTypeCustomSelect", "Currency", "PrefDeliveryDaysOptions")) {
      assertFalse(after.contains(enumeration), enumeration + " is an enumeration, directly or through its type definition");
    }
  }

  @Test
  void onlyAnnotatedElementsRemainWithTheAnnotatedFilter() throws Exception {
    Fixture fixture = open("Order_DM");
    CheckBox annotated = FxTestSupport.field(fixture.popup, "onlyAnnotatedCheckBox");

    FxTestSupport.onFx(() -> annotated.setSelected(true));

    List<Element> leaves = shownElements(fixture).stream().filter(element -> !(element instanceof GroupElement)).toList();
    assertFalse(leaves.isEmpty());
    assertTrue(leaves.stream().allMatch(element -> !element.getAnnotations().isEmpty()));
    assertTrue(leafNames(fixture).contains("InfosMax"));
  }

  @Test
  void theAlwaysRequiredFilterKeepsTheRequiredFieldsOnly() throws Exception {
    Fixture fixture = open("Order_DM");
    CheckBox alwaysRequired = FxTestSupport.field(fixture.popup, "alwaysRequiredCheckBox");

    FxTestSupport.onFx(() -> alwaysRequired.setSelected(true));

    assertEquals(List.of("TermsAndConditions"), leafNames(fixture));
  }

  @Test
  void theFilterButtonShowsWhetherAFilterIsOnAndResetBringsTheWholeTreeBack() throws Exception {
    Fixture fixture = open("Order_DM");
    FontIcon icon = FxTestSupport.field(fixture.controller, "filterIcon");
    int fullTree = rowNames(fixture).size();
    assertEquals(Icons.FILTER, icon.getIconLiteral());

    setShown(fixture, FieldKind.NUMBER, false);
    assertEquals(Icons.FILTER_ACTIVE, icon.getIconLiteral());
    assertTrue(rowNames(fixture).size() < fullTree);

    Popup popup = FxTestSupport.field(fixture.controller, "filterPopup");
    Button reset = (Button) popup.getContent().get(0).lookup("#resetButton");
    FxTestSupport.onFx(reset::fire);

    assertEquals(Icons.FILTER, icon.getIconLiteral());
    assertEquals(fullTree, rowNames(fixture).size());
    CheckBox number = fieldKindCheckBoxes(fixture).get(FieldKind.NUMBER);
    assertTrue(number.isSelected(), "the popup shows the reset state");
  }

  @Test
  void theFilterSurvivesAnEditOfTheModel() throws Exception {
    Fixture fixture = open("Order_DM");
    setShown(fixture, ElementKind.VALIDATION_RULES, false);
    int filtered = rowNames(fixture).size();

    Element date = element(fixture, "OrderingDate");
    select(fixture, date);
    press(fixture, KeyCode.X);

    assertEquals(filtered - 1, rowNames(fixture).size(), "the tree is rebuilt with the filter still applied");
  }

  // ---- shortcuts and bulk actions ----------------------------------------------------------------------------

  @Test
  void cutOfAMultiSelectionIsOneUndoStep() throws Exception {
    Fixture fixture = open("Order_DM");
    GroupElement order = rootGroup(fixture);
    List<String> before = names(order);
    select(fixture, element(fixture, "OrderingDate"), element(fixture, "DeliveryDate"));

    press(fixture, KeyCode.X);

    assertFalse(names(order).contains("OrderingDate"));
    assertFalse(names(order).contains("DeliveryDate"));
    Button undo = FxTestSupport.field(fixture.controller, "undoButton");
    FxTestSupport.onFx(undo::fire);
    assertEquals(before, names(order), "one Undo brings back both, in their old places");
    assertTrue(undo.isDisable(), "and that was the only step");
  }

  @Test
  void copyThenPasteShortcutsPasteAllCopiedElementsAsOneUndoStep() throws Exception {
    Fixture fixture = open("Order_DM");
    GroupElement information = (GroupElement) element(fixture, "OrderInformation");
    int before = information.getGroup().getElements().size();
    select(fixture, element(fixture, "OrderingDate"), element(fixture, "DeliveryDate"));
    press(fixture, KeyCode.C);
    select(fixture, information);

    press(fixture, KeyCode.V);

    assertEquals(before + 2, information.getGroup().getElements().size());
    List<String> pasted = names(information);
    assertTrue(pasted.contains("OrderingDate") && pasted.contains("DeliveryDate"), pasted.toString());
    Button undo = FxTestSupport.field(fixture.controller, "undoButton");
    FxTestSupport.onFx(undo::fire);
    assertEquals(before, information.getGroup().getElements().size());
    assertTrue(undo.isDisable());
  }

  @Test
  void pastingTwiceGivesTheCopiesDistinctIdsAndNames() throws Exception {
    Fixture fixture = open("Order_DM");
    GroupElement information = (GroupElement) element(fixture, "OrderInformation");
    select(fixture, element(fixture, "OrderingDate"), element(fixture, "DeliveryDate"));
    press(fixture, KeyCode.C);
    select(fixture, information);

    press(fixture, KeyCode.V);
    select(fixture, information);
    press(fixture, KeyCode.V);

    List<Element> children = information.getGroup().getElements();
    assertEquals(children.size(), children.stream().map(Element::getId).distinct().count());
    assertEquals(children.size(), children.stream().map(Element::getName).distinct().count());
  }

  @Test
  void shortcutsDoNothingWhereTheirToolbarButtonIsDisabled() throws Exception {
    Fixture fixture = open("Order_DM");
    FxTestSupport.onFx(() -> fixture.tree.getSelectionModel().clearSelection());
    int before = rowNames(fixture).size();

    press(fixture, KeyCode.X);
    press(fixture, KeyCode.V);

    assertEquals(before, rowNames(fixture).size());
  }

  // ---- insert from another Document Model -------------------------------------------------------------------

  @Test
  void insertingAnotherModelCopiesItsContentAndTypeDefinitionsAsOneUndoStep() throws Exception {
    Fixture fixture = open("Invoice_DM");
    DocumentModel order = otherModel("Order_DM");
    String orderBefore = JsonSettings.objectMapper.writeValueAsString(order);
    List<GroupElement> rootGroups = fixture.model.getContent().getModelRoot().getRootGroups();
    FxTestSupport.onFx(() -> fixture.tree.getSelectionModel().clearSelection());

    insertFrom(fixture, order);

    assertEquals(2, rootGroups.size());
    GroupElement copy = rootGroups.get(1);
    assertEquals("Order", copy.getName());
    assertEquals(1, fixture.model.getContent().getTypeDefinitions().size());
    String newTypeDefinitionId = fixture.model.getContent().getTypeDefinitions().get(0).getId();
    assertEquals("ProductType", fixture.model.getContent().getTypeDefinitions().get(0).getName());
    assertFalse(newTypeDefinitionId.equals(order.getContent().getTypeDefinitions().get(0).getId()), "a fresh id");
    List<TypeDefFieldType> usages = allElements(copy).stream()
        .filter(element -> element instanceof FieldElement field && field.getField().getFieldType() instanceof TypeDefFieldType)
        .map(element -> (TypeDefFieldType) ((FieldElement) element).getField().getFieldType()).toList();
    assertFalse(usages.isEmpty());
    assertTrue(usages.stream().allMatch(usage -> newTypeDefinitionId.equals(usage.getTypeDefType().getTypeDefinitionId())));
    List<String> ids = new ArrayList<>();
    rootGroups.forEach(group -> allElements(group).forEach(element -> ids.add(element.getId())));
    assertEquals(ids.size(), ids.stream().distinct().count(), "every element of the enlarged model has its own id");
    assertEquals(orderBefore, JsonSettings.objectMapper.writeValueAsString(order), "the source is not touched");
    assertTrue(Files.readString(Path.of(fixture.item.getPath())).contains(newTypeDefinitionId), "the model was saved");
    assertTrue(rowNames(fixture).contains("OrderNumber"), "the tree shows the inserted content");

    Button undo = FxTestSupport.field(fixture.controller, "undoButton");
    FxTestSupport.onFx(undo::fire);
    assertEquals(1, rootGroups.size());
    assertTrue(fixture.model.getContent().getTypeDefinitions().isEmpty());
    assertTrue(undo.isDisable(), "the insertion was a single step");
  }

  @Test
  void theInsertedContentLandsBelowTheSelectedGroupWithAUniqueName() throws Exception {
    Fixture fixture = open("Invoice_DM");
    GroupElement invoice = rootGroup(fixture);
    select(fixture, invoice);

    insertFrom(fixture, otherModel("Order_DM"));

    List<Element> children = invoice.getGroup().getElements();
    Element inserted = children.get(children.size() - 1);
    assertEquals("Order_2", inserted.getName(), "the invoice already has a child called Order");
    assertEquals(1, fixture.model.getContent().getModelRoot().getRootGroups().size());
  }

  @Test
  void includesOfTheInsertedModelAreFlattened() throws Exception {
    Fixture fixture = open("Addresses_DM");
    FxTestSupport.onFx(() -> fixture.tree.getSelectionModel().clearSelection());

    insertFrom(fixture, otherModel("PaymentInfo_DM"));

    List<GroupElement> rootGroups = fixture.model.getContent().getModelRoot().getRootGroups();
    assertEquals(2, rootGroups.size());
    assertTrue(allElements(rootGroups.get(1)).stream().noneMatch(element -> element instanceof GroupElement group
        && group.getGroup() != null && group.getGroup().getIncludeConfig() != null));
  }

  // ---- fixture ---------------------------------------------------------------------------------------------

  private record Fixture(DocumentModelElementsTreeController controller, DocumentModelTreeFilterController popup,
                         TreeTableView<ElementViewModel> tree, DocumentModel model, ProjectItem item) {
  }

  private Fixture open(String modelId) throws Exception {
    assumeTrue(toolkitAvailable, "No JavaFX toolkit available");
    Path source = locateBasicModels();
    Path models = Files.createDirectories(workspace.resolve("models"));
    Files.copy(source.resolve("Invoice_DM.json"), models.resolve("Invoice_DM.json"));
    try (var files = Files.list(source.resolve("Invoice-Includes"))) {
      for (Path file : files.toList()) {
        Files.copy(file, models.resolve(file.getFileName().toString()));
      }
    }

    Project project = new Project();
    project.load(workspace.toFile());
    setCurrentProject(project);
    ProjectItem item = project.getRoot().findByPath(models.resolve(modelId + ".json").toString());
    if (item == null) {
      item = new ProjectItem(models.resolve(modelId + ".json").toFile());
    }
    DocumentModel model = (DocumentModel) item.getModel();

    FxTestSupport.Loaded<DocumentModelElementsTreeController> loaded =
        FxTestSupport.load("/de/a12/studio/ui/editors/documentmodel/document-model-elements-tree.fxml");
    ProjectItem openedItem = item;
    FxTestSupport.onFx(() -> loaded.controller().load(openedItem, model.getContent().getModelRoot()));
    TreeTableView<ElementViewModel> tree = FxTestSupport.field(loaded.controller(), "elementsTreeTable");
    DocumentModelTreeFilterController popup = FxTestSupport.field(loaded.controller(), "filterController");
    return new Fixture(loaded.controller(), popup, tree, model, item);
  }

  private static void insertFrom(Fixture fixture, DocumentModel source) throws Exception {
    DocumentModelActions actions = FxTestSupport.field(fixture.controller, "documentModelActions");
    FxTestSupport.onFx(() -> actions.insertFrom(source));
  }

  private static DocumentModel otherModel(String id) {
    return (DocumentModel) ProjectDocumentModels.findProjectItemByModelId(id).orElseThrow().getModel();
  }

  private static List<Element> allElements(Element root) {
    List<Element> result = new ArrayList<>();
    result.add(root);
    if (root instanceof GroupElement group && group.getGroup() != null) {
      group.getGroup().getElements().forEach(child -> result.addAll(allElements(child)));
    }
    return result;
  }

  private static void search(Fixture fixture, String text) throws Exception {
    SearchFieldController search = FxTestSupport.field(fixture.controller, "searchController");
    FxTestSupport.onFx(() -> search.textProperty().set(text));
  }

  private static void setShown(Fixture fixture, ElementKind kind, boolean shown) throws Exception {
    Map<ElementKind, CheckBox> checkBoxes = FxTestSupport.field(fixture.popup, "elementKindCheckBoxes");
    FxTestSupport.onFx(() -> checkBoxes.get(kind).setSelected(shown));
  }

  private static void setShown(Fixture fixture, FieldKind kind, boolean shown) throws Exception {
    FxTestSupport.onFx(() -> fieldKindCheckBoxes(fixture).get(kind).setSelected(shown));
  }

  private static Map<FieldKind, CheckBox> fieldKindCheckBoxes(Fixture fixture) {
    try {
      return FxTestSupport.field(fixture.popup, "fieldKindCheckBoxes");
    }
    catch (Exception e) {
      throw new AssertionError(e);
    }
  }

  /** Every element currently shown as a row, in tree order (the root row has no element). */
  private static List<Element> shownElements(Fixture fixture) throws Exception {
    return FxTestSupport.onFx(() -> {
      List<Element> result = new ArrayList<>();
      collect(fixture.tree.getRoot(), result);
      return result;
    });
  }

  private static void collect(TreeItem<ElementViewModel> item, List<Element> result) {
    if (item.getValue() != null) {
      result.add(item.getValue().getElement());
    }
    item.getChildren().forEach(child -> collect(child, result));
  }

  private static List<String> rowNames(Fixture fixture) throws Exception {
    return shownElements(fixture).stream().map(Element::getName).collect(Collectors.toList());
  }

  private static List<String> leafNames(Fixture fixture) throws Exception {
    return shownElements(fixture).stream().filter(element -> !(element instanceof GroupElement)).map(Element::getName)
        .collect(Collectors.toList());
  }

  /** The first element of the model (its own tree, not an include's) with this name. */
  private static Element element(Fixture fixture, String name) {
    return find(fixture.model.getContent().getModelRoot().getRootGroups(), name);
  }

  private static Element find(List<? extends Element> elements, String name) {
    for (Element element : elements) {
      if (name.equals(element.getName())) {
        return element;
      }
      if (element instanceof GroupElement group && group.getGroup() != null) {
        Element found = find(group.getGroup().getElements(), name);
        if (found != null) {
          return found;
        }
      }
    }
    return null;
  }

  private static GroupElement rootGroup(Fixture fixture) {
    return fixture.model.getContent().getModelRoot().getRootGroups().get(0);
  }

  private static List<String> names(GroupElement group) {
    return group.getGroup().getElements().stream().map(Element::getName).toList();
  }

  private static void select(Fixture fixture, Element... elements) throws Exception {
    FxTestSupport.onFx(() -> {
      fixture.tree.getSelectionModel().clearSelection();
      for (Element element : elements) {
        TreeItem<ElementViewModel> item = findItem(fixture.tree.getRoot(), element);
        fixture.tree.getSelectionModel().select(item);
      }
    });
  }

  private static TreeItem<ElementViewModel> findItem(TreeItem<ElementViewModel> item, Element element) {
    if (item.getValue() != null && item.getValue().getElement() == element) {
      return item;
    }
    for (TreeItem<ElementViewModel> child : item.getChildren()) {
      TreeItem<ElementViewModel> found = findItem(child, element);
      if (found != null) {
        return found;
      }
    }
    return null;
  }

  /** Presses Ctrl (Cmd on a Mac) + {@code code} on the tree. */
  private static void press(Fixture fixture, KeyCode code) throws Exception {
    boolean mac = System.getProperty("os.name", "").toLowerCase().contains("mac");
    FxTestSupport.onFx(() -> fixture.tree.fireEvent(new KeyEvent(KeyEvent.KEY_PRESSED, "", "", code, false, !mac, false, mac)));
  }

  private static void setCurrentProject(Project project) throws Exception {
    Field currentProject = Studio.class.getDeclaredField("currentProject");
    currentProject.setAccessible(true);
    currentProject.set(null, project);
  }

  private static Path locateBasicModels() throws IOException {
    for (Path dir = Path.of("").toAbsolutePath(); dir != null; dir = dir.getParent()) {
      Path candidate = dir.resolve("testing").resolve("workspaces").resolve("basic").resolve("models");
      if (Files.isDirectory(candidate)) {
        return candidate;
      }
    }
    throw new IllegalStateException("Could not locate 'testing/workspaces/basic/models' above " + Path.of("").toAbsolutePath());
  }
}
