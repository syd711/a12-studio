package de.a12.studio.modelsvalidation.documentinsertion;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.Label;
import de.a12.studio.models.Locale;
import de.a12.studio.models.ModelReference;
import de.a12.studio.models.ModelType;
import de.a12.studio.models.additivedocumentmodel.AdditiveDocumentModel;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.documentmodel.DocumentModelContent;
import de.a12.studio.models.documentmodel.Element;
import de.a12.studio.models.documentmodel.FieldConfig;
import de.a12.studio.models.documentmodel.FieldElement;
import de.a12.studio.models.documentmodel.GroupConfig;
import de.a12.studio.models.documentmodel.GroupElement;
import de.a12.studio.models.documentmodel.IncludeConfig;
import de.a12.studio.models.documentmodel.ModelRoot;
import de.a12.studio.models.documentmodel.StringFieldType;
import de.a12.studio.models.documentmodel.TypeDefFieldType;
import de.a12.studio.models.documentmodel.TypeDefinition;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.models.typedefinitionmodel.TypeDefinitionModel;
import de.a12.studio.models.util.JsonSettings;
import de.a12.studio.modelsvalidation.documentinsertion.DocumentModelInsertion.Plan;
import de.a12.studio.modelsvalidation.documentinsertion.DocumentModelInsertion.Problem;
import de.a12.studio.modelsvalidation.documentinsertion.DocumentModelInsertion.Warning;
import de.a12.studio.modelsvalidation.documentinsertion.DocumentModelInsertion.WarningKind;
import de.a12.studio.modelsvalidation.validators.ElementIndex;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DocumentModelInsertionTest {

  // -------------------------------------------------------------------------
  // Includes
  // -------------------------------------------------------------------------

  @Test
  void copiesTheRootGroupsAsDeepCopiesAndLeavesTheSourceUntouched() {
    DocumentModel source = model("Source_DM", group("Root", field("Name")));
    DocumentModel target = model("Target_DM");
    String before = json(source);

    Plan plan = DocumentModelInsertion.plan(target, source, List.of(source));

    assertTrue(plan.isUsable());
    assertEquals(1, plan.groups().size());
    assertEquals("Root", plan.groups().get(0).getName());
    assertNotSame(source.getContent().getModelRoot().getRootGroups().get(0), plan.groups().get(0));
    plan.groups().get(0).getGroup().getElements().get(0).setName("Renamed");
    assertEquals(before, json(source), "editing the copy must not reach the source");
  }

  @Test
  void anIncludeBecomesAPlainGroupHoldingTheIncludedRootGroupsChildren() {
    DocumentModel address = model("Address_DM", group("AddressRoot", field("Street"), field("City")));
    GroupElement include = includeGroup("AddressInclude", "Address_DM");
    include.getGroup().setRepeatability(3);
    DocumentModel source = model("Source_DM", group("Root", field("Name"), include));

    Plan plan = DocumentModelInsertion.plan(model("Target_DM"), source, List.of(source, address));

    GroupElement root = plan.groups().get(0);
    GroupElement flattened = (GroupElement) root.getGroup().getElements().get(1);
    assertNull(flattened.getGroup().getIncludeConfig());
    assertEquals("AddressInclude", flattened.getName());
    assertEquals(3, flattened.getGroup().getRepeatability());
    assertEquals(List.of("Street", "City"), names(flattened.getGroup().getElements()));
    assertTrue(plan.warnings().isEmpty());
  }

  @Test
  void includesInsideIncludedModelsAreFlattenedToo() {
    DocumentModel country = model("Country_DM", group("CountryRoot", field("Code")));
    DocumentModel address = model("Address_DM", group("AddressRoot", field("Street"), includeGroup("Country", "Country_DM")));
    DocumentModel source = model("Source_DM", group("Root", includeGroup("Address", "Address_DM")));

    Plan plan = DocumentModelInsertion.plan(model("Target_DM"), source, List.of(source, address, country));

    assertTrue(allElements(plan.groups()).stream().noneMatch(DocumentModelInsertionTest::isInclude));
    assertEquals(List.of("Root", "Address", "Street", "Country", "Code"), names(allElements(plan.groups())));
  }

  @Test
  void anIncludeOfAMissingModelIsKeptAndReported() {
    DocumentModel source = model("Source_DM", group("Root", includeGroup("Gone", "Gone_DM")));

    Plan plan = DocumentModelInsertion.plan(model("Target_DM"), source, List.of(source));

    assertTrue(plan.isUsable());
    GroupElement kept = (GroupElement) plan.groups().get(0).getGroup().getElements().get(0);
    assertEquals("Gone_DM", kept.getGroup().getIncludeConfig().getReference());
    assertEquals(List.of(new Warning(WarningKind.INCLUDE_MODEL_MISSING, "Gone", "Gone_DM")), plan.warnings());
  }

  @Test
  void anIncludeCycleBelowTheSourceIsKeptAtTheClosingIncludeAndReported() {
    DocumentModel a = model("A_DM", group("ARoot", field("FromA"), includeGroup("ToB", "B_DM")));
    DocumentModel b = model("B_DM", group("BRoot", field("FromB"), includeGroup("BackToB", "B_DM")));
    DocumentModel source = model("Source_DM", group("Root", includeGroup("ToA", "A_DM")));

    Plan plan = DocumentModelInsertion.plan(model("Target_DM"), source, List.of(source, a, b));

    assertTrue(plan.isUsable());
    assertEquals(List.of("Root", "ToA", "FromA", "ToB", "FromB", "BackToB"), names(allElements(plan.groups())));
    assertEquals(1, allElements(plan.groups()).stream().filter(DocumentModelInsertionTest::isInclude).count());
    assertEquals(List.of(new Warning(WarningKind.INCLUDE_CYCLE, "BackToB", "B_DM")), plan.warnings());
  }

  @Test
  void aSourceThatIncludesTheTargetCannotBeInserted() {
    DocumentModel target = model("Target_DM", group("TargetRoot", field("Value")));
    DocumentModel middle = model("Middle_DM", group("MiddleRoot", includeGroup("Target", "Target_DM")));
    DocumentModel source = model("Source_DM", group("Root", includeGroup("Middle", "Middle_DM")));

    Plan plan = DocumentModelInsertion.plan(target, source, List.of(source, middle));

    assertEquals(Problem.SOURCE_INCLUDES_TARGET, plan.problem());
    assertTrue(plan.groups().isEmpty());
  }

  // -------------------------------------------------------------------------
  // Candidates
  // -------------------------------------------------------------------------

  @Test
  void candidatesAreStandaloneDocumentModelsOtherThanTheTarget() {
    DocumentModel target = model("Target_DM");

    assertFalse(DocumentModelInsertion.isCandidate(target, target));
    assertFalse(DocumentModelInsertion.isCandidate(target, model("Target_DM")), "same id, different instance");
    assertFalse(DocumentModelInsertion.isCandidate(target, typeDefinitionModel("Types_TdM")));
    assertFalse(DocumentModelInsertion.isCandidate(target, additiveModel("Add_Ad")));
    assertTrue(DocumentModelInsertion.isCandidate(target, model("Other_DM")));
  }

  @Test
  void candidatesMustHaveACompatibleTypeDefinitionMode() {
    DocumentModel localTarget = model("Local_DM");
    localTarget.getContent().getTypeDefinitions().add(typeDef("typedef_a", "A"));
    DocumentModel importingTarget = model("Importing_DM");
    importingTarget.getModelReferences().add(typeDefinitionImport("Types_TdM"));
    DocumentModel plainCandidate = model("Plain_DM");
    DocumentModel localCandidate = model("LocalCandidate_DM");
    localCandidate.getContent().getTypeDefinitions().add(typeDef("typedef_b", "B"));

    assertTrue(DocumentModelInsertion.isCandidate(localTarget, plainCandidate), "a model without any is always fine");
    assertTrue(DocumentModelInsertion.isCandidate(localTarget, localCandidate));
    assertFalse(DocumentModelInsertion.isCandidate(importingTarget, localCandidate));
    assertTrue(DocumentModelInsertion.isCandidate(importingTarget, plainCandidate));
  }

  // -------------------------------------------------------------------------
  // Type definitions
  // -------------------------------------------------------------------------

  @Test
  void localTypeDefinitionsAreCopiedWithFreshIdsAndTheCopiedFieldsAreRepointed() {
    DocumentModel source = model("Source_DM", group("Root", typeDefField("Kind", "typedef_kind"), field("Other")));
    source.getContent().getTypeDefinitions().add(typeDef("typedef_kind", "Kind"));
    DocumentModel target = model("Target_DM");

    Plan plan = DocumentModelInsertion.plan(target, source, List.of(source));

    assertEquals(1, plan.typeDefinitions().size());
    TypeDefinition copy = plan.typeDefinitions().get(0);
    assertEquals("Kind", copy.getName());
    assertNotEquals("typedef_kind", copy.getId());
    assertTrue(copy.getId().startsWith("typedef_"));
    assertNotSame(source.getContent().getTypeDefinitions().get(0).getFieldType(), copy.getFieldType());
    FieldElement kind = (FieldElement) plan.groups().get(0).getGroup().getElements().get(0);
    assertEquals(copy.getId(), typeDefinitionIdOf(kind));
    FieldElement original = (FieldElement) source.getContent().getModelRoot().getRootGroups().get(0).getGroup().getElements().get(0);
    assertEquals("typedef_kind", typeDefinitionIdOf(original), "the source keeps pointing at its own type definition");
  }

  @Test
  void aCopiedTypeDefinitionGetsAUniqueNameInTheTarget() {
    DocumentModel source = model("Source_DM", group("Root", typeDefField("Kind", "typedef_kind")));
    source.getContent().getTypeDefinitions().add(typeDef("typedef_kind", "Kind"));
    DocumentModel target = model("Target_DM");
    target.getContent().getTypeDefinitions().add(typeDef("typedef_mine", "Kind"));

    Plan plan = DocumentModelInsertion.plan(target, source, List.of(source));

    assertEquals("Kind_2", plan.typeDefinitions().get(0).getName());
  }

  @Test
  void onlyTypeDefinitionsUsedByACopiedFieldAreCarriedOver() {
    DocumentModel source = model("Source_DM", group("Root", typeDefField("Kind", "typedef_used")));
    source.getContent().getTypeDefinitions().add(typeDef("typedef_used", "Used"));
    source.getContent().getTypeDefinitions().add(typeDef("typedef_unused", "Unused"));

    Plan plan = DocumentModelInsertion.plan(model("Target_DM"), source, List.of(source));

    assertEquals(List.of("Used"), plan.typeDefinitions().stream().map(TypeDefinition::getName).toList());
  }

  @Test
  void typeDefinitionsOfIncludedModelsAreCopiedLocallyToo() {
    DocumentModel order = model("Order_DM", group("OrderRoot", typeDefField("Product", "typedef_product")));
    order.getContent().getTypeDefinitions().add(typeDef("typedef_product", "ProductType"));
    DocumentModel source = model("Source_DM", group("Root", includeGroup("Order", "Order_DM")));

    Plan plan = DocumentModelInsertion.plan(model("Target_DM"), source, List.of(source, order));

    assertEquals(1, plan.typeDefinitions().size());
    FieldElement product = (FieldElement) allElements(plan.groups()).stream()
        .filter(element -> element.getName().equals("Product")).findFirst().orElseThrow();
    assertEquals(plan.typeDefinitions().get(0).getId(), typeDefinitionIdOf(product));
    assertTrue(plan.importReferences().isEmpty());
  }

  @Test
  void aTypeDefinitionModelsTypeDefinitionsAreImportedInsteadOfCopied() {
    TypeDefinitionModel types = typeDefinitionModel("Types_TdM");
    types.getContent().getTypeDefinitions().add(typeDef("typedef_kind", "Kind"));
    DocumentModel source = model("Source_DM", group("Root", typeDefField("Kind", "typedef_kind")));
    source.getModelReferences().add(typeDefinitionImport("Types_TdM"));
    DocumentModel target = model("Target_DM");

    Plan plan = DocumentModelInsertion.plan(target, source, List.of(source, types));

    assertTrue(plan.typeDefinitions().isEmpty());
    assertEquals(1, plan.importReferences().size());
    ModelReference reference = plan.importReferences().get(0);
    assertEquals("Types_TdM", reference.getReference());
    assertEquals(ModelReference.PURPOSE_TYPE_DEFINITIONS, reference.getPurpose());
    assertEquals(ModelType.DOCUMENT, reference.getModelType());
    FieldElement kind = (FieldElement) plan.groups().get(0).getGroup().getElements().get(0);
    assertEquals("typedef_kind", typeDefinitionIdOf(kind), "an imported type definition keeps its id");
  }

  @Test
  void aTypeDefinitionModelTheTargetAlreadyImportsIsNotImportedAgain() {
    TypeDefinitionModel types = typeDefinitionModel("Types_TdM");
    types.getContent().getTypeDefinitions().add(typeDef("typedef_kind", "Kind"));
    DocumentModel source = model("Source_DM", group("Root", typeDefField("Kind", "typedef_kind")));
    source.getModelReferences().add(typeDefinitionImport("Types_TdM"));
    DocumentModel target = model("Target_DM");
    target.getModelReferences().add(typeDefinitionImport("Types_TdM"));

    Plan plan = DocumentModelInsertion.plan(target, source, List.of(source, types));

    assertTrue(plan.isUsable());
    assertTrue(plan.importReferences().isEmpty());
  }

  @Test
  void aTypeDefinitionModelImportedByAnotherNeededOneIsNotImportedSeparately() {
    TypeDefinitionModel base = typeDefinitionModel("Base_TdM");
    base.getContent().getTypeDefinitions().add(typeDef("typedef_base", "BaseType"));
    TypeDefinitionModel derived = typeDefinitionModel("Derived_TdM");
    derived.getContent().getTypeDefinitions().add(typeDef("typedef_derived", "DerivedType"));
    derived.getModelReferences().add(typeDefinitionImport("Base_TdM"));
    DocumentModel source = model("Source_DM", group("Root", typeDefField("A", "typedef_base"), typeDefField("B", "typedef_derived")));
    source.getModelReferences().add(typeDefinitionImport("Derived_TdM"));

    Plan plan = DocumentModelInsertion.plan(model("Target_DM"), source, List.of(source, base, derived));

    assertEquals(List.of("Derived_TdM"), plan.importReferences().stream().map(ModelReference::getReference).toList());
  }

  @Test
  void mixingOwnedAndImportedTypeDefinitionsCopiesEverythingLocally() {
    TypeDefinitionModel types = typeDefinitionModel("Types_TdM");
    types.getContent().getTypeDefinitions().add(typeDef("typedef_imported", "Imported"));
    DocumentModel included = model("Included_DM", group("IncludedRoot", typeDefField("Own", "typedef_own")));
    included.getContent().getTypeDefinitions().add(typeDef("typedef_own", "Own"));
    DocumentModel source = model("Source_DM", group("Root", typeDefField("Theirs", "typedef_imported"), includeGroup("Inc", "Included_DM")));
    source.getModelReferences().add(typeDefinitionImport("Types_TdM"));

    Plan plan = DocumentModelInsertion.plan(model("Target_DM"), source, List.of(source, types, included));

    assertEquals(2, plan.typeDefinitions().size());
    assertTrue(plan.importReferences().isEmpty());
  }

  @Test
  void localTypeDefinitionsCannotGoIntoATargetThatImportsAndTheOtherWayAround() {
    TypeDefinitionModel types = typeDefinitionModel("Types_TdM");
    types.getContent().getTypeDefinitions().add(typeDef("typedef_imported", "Imported"));
    DocumentModel localSource = model("Local_DM", group("Root", typeDefField("Kind", "typedef_kind")));
    localSource.getContent().getTypeDefinitions().add(typeDef("typedef_kind", "Kind"));
    DocumentModel importingSource = model("Importing_DM", group("Root", typeDefField("Kind", "typedef_imported")));
    importingSource.getModelReferences().add(typeDefinitionImport("Types_TdM"));

    DocumentModel importingTarget = model("ImportingTarget_DM");
    importingTarget.getModelReferences().add(typeDefinitionImport("Types_TdM"));
    DocumentModel localTarget = model("LocalTarget_DM");
    localTarget.getContent().getTypeDefinitions().add(typeDef("typedef_mine", "Mine"));

    assertEquals(Problem.LOCAL_TYPE_DEFINITIONS_IN_IMPORT_MODE,
        DocumentModelInsertion.plan(importingTarget, localSource, List.of(localSource, types)).problem());
    assertEquals(Problem.IMPORTS_IN_LOCAL_MODE,
        DocumentModelInsertion.plan(localTarget, importingSource, List.of(importingSource, types)).problem());
  }

  @Test
  void aTypeDefinitionThatCannotBeFoundIsReportedAndTheFieldKeepsItsReference() {
    DocumentModel source = model("Source_DM", group("Root", typeDefField("Kind", "typedef_missing")));

    Plan plan = DocumentModelInsertion.plan(model("Target_DM"), source, List.of(source));

    assertTrue(plan.isUsable());
    assertTrue(plan.typeDefinitions().isEmpty());
    assertEquals(List.of(new Warning(WarningKind.TYPE_DEFINITION_MISSING, "typedef_missing", null)), plan.warnings());
    FieldElement kind = (FieldElement) plan.groups().get(0).getGroup().getElements().get(0);
    assertEquals("typedef_missing", typeDefinitionIdOf(kind));
  }

  // -------------------------------------------------------------------------
  // Locales
  // -------------------------------------------------------------------------

  @Test
  void labelsInLocalesTheTargetDoesNotDeclareAreDropped() {
    FieldElement name = field("Name");
    name.getField().getLabel().addAll(List.of(label("en", "Name"), label("de", "Name (de)"), label("fr", "Nom")));
    name.getField().getHelperText().addAll(List.of(label("de", "Hilfe")));
    name.getExternalDescription().addAll(List.of(label("en", "Described"), label("fr", "Decrit")));
    DocumentModel source = model("Source_DM", group("Root", name));
    DocumentModel target = model("Target_DM");
    target.setLocales(List.of(locale("en"), locale("de")));

    Plan plan = DocumentModelInsertion.plan(target, source, List.of(source));

    FieldElement copy = (FieldElement) plan.groups().get(0).getGroup().getElements().get(0);
    assertEquals(List.of("en", "de"), copy.getField().getLabel().stream().map(Label::getLocale).toList());
    assertEquals(1, copy.getField().getHelperText().size());
    assertEquals(List.of("en"), copy.getExternalDescription().stream().map(Label::getLocale).toList());
    assertEquals(3, name.getField().getLabel().size(), "the source keeps every label");
  }

  @Test
  void aTargetWithoutLocalesKeepsEveryLabel() {
    FieldElement name = field("Name");
    name.getField().getLabel().addAll(List.of(label("en", "Name"), label("de", "Name (de)")));
    DocumentModel source = model("Source_DM", group("Root", name));

    Plan plan = DocumentModelInsertion.plan(model("Target_DM"), source, List.of(source));

    assertEquals(2, ((FieldElement) plan.groups().get(0).getGroup().getElements().get(0)).getField().getLabel().size());
  }

  // -------------------------------------------------------------------------
  // Real fixtures
  // -------------------------------------------------------------------------

  @Test
  void invoiceOfTheBasicWorkspaceIsCopiedWithItsIncludedOrderModelAndItsTypeDefinition() throws IOException {
    List<DocumentModel> project = documentModels(loadWorkspace("basic"));
    DocumentModel invoice = byId(project, "Invoice_DM");
    DocumentModel target = model("Target_DM");

    Plan plan = DocumentModelInsertion.plan(target, invoice, project);

    assertTrue(plan.isUsable(), plan.problem() + " " + plan.warnings());
    assertTrue(allElements(plan.groups()).stream().noneMatch(DocumentModelInsertionTest::isInclude), "no Include is left");
    assertFalse(plan.typeDefinitions().isEmpty(), "Order_DM's ProductType has to travel along");
    assertResolvable(plan, target, project);
  }

  @Test
  void everyDocumentModelOfEveryFixtureWorkspaceCanBeInsertedIntoAnEmptyModel() throws IOException {
    int inserted = 0;
    for (Path workspace : workspaces()) {
      List<DocumentModel> project = documentModels(loadWorkspace(workspace));
      for (DocumentModel source : project) {
        if (!DocumentModelInsertion.isCandidate(model("Empty_DM"), source)) {
          continue;
        }
        DocumentModel target = model("Empty_DM");
        Plan plan = DocumentModelInsertion.plan(target, source, project);
        String what = workspace.getFileName() + "/" + source.getId();

        assertTrue(plan.isUsable(), what + ": " + plan.problem());
        assertEquals(source.getContent().getModelRoot().getRootGroups().size(), plan.groups().size(), what);
        assertTrue(plan.warnings().stream().noneMatch(warning -> warning.kind() == WarningKind.INCLUDE_CYCLE),
            what + ": " + plan.warnings());
        assertResolvable(plan, target, project);
        inserted++;
      }
    }
    assertTrue(inserted > 20, "expected to sweep the fixture Document Models, got " + inserted);
  }

  /** After applying the plan to the (empty) target every type definition a copied field names must resolve. */
  private static void assertResolvable(Plan plan, DocumentModel target, List<DocumentModel> project) {
    target.getContent().getModelRoot().getRootGroups().addAll(plan.groups());
    target.getContent().getTypeDefinitions().addAll(plan.typeDefinitions());
    target.getModelReferences().addAll(plan.importReferences());
    ElementIndex index = new ElementIndex(target, project);
    boolean unresolvedWarned = plan.warnings().stream().anyMatch(w -> w.kind() == WarningKind.TYPE_DEFINITION_MISSING);
    for (Element element : allElements(plan.groups())) {
      if (element instanceof FieldElement field && field.getField() != null
          && field.getField().getFieldType() instanceof TypeDefFieldType) {
        boolean resolves = index.effectiveFieldType(field.getField().getFieldType()) != null;
        assertTrue(resolves || unresolvedWarned, "type definition of " + element.getName() + " does not resolve");
      }
    }
  }

  // -------------------------------------------------------------------------
  // Fixture helpers
  // -------------------------------------------------------------------------

  private static List<A12Model<?>> loadWorkspace(String name) throws IOException {
    return loadWorkspace(workspacesRoot().resolve(name));
  }

  private static List<A12Model<?>> loadWorkspace(Path workspace) throws IOException {
    List<A12Model<?>> models = new ArrayList<>();
    try (Stream<Path> walk = Files.walk(workspace)) {
      for (Path file : walk.filter(Files::isRegularFile).filter(path -> path.getFileName().toString().endsWith(".json"))
          .sorted().toList()) {
        try {
          A12Model<?> model = new ProjectItem(file.toFile()).getModel();
          if (model != null) {
            models.add(model);
          }
        }
        catch (RuntimeException e) {
          // not a model file
        }
      }
    }
    return models;
  }

  private static List<DocumentModel> documentModels(List<A12Model<?>> models) {
    return models.stream().filter(DocumentModel.class::isInstance).map(DocumentModel.class::cast).toList();
  }

  private static DocumentModel byId(List<DocumentModel> models, String id) {
    return models.stream().filter(model -> id.equals(model.getId())).findFirst().orElseThrow();
  }

  private static List<Path> workspaces() throws IOException {
    try (Stream<Path> children = Files.list(workspacesRoot())) {
      return children.filter(Files::isDirectory).sorted().toList();
    }
  }

  private static Path workspacesRoot() {
    for (Path dir = Path.of("").toAbsolutePath(); dir != null; dir = dir.getParent()) {
      Path candidate = dir.resolve("testing").resolve("workspaces");
      if (Files.isDirectory(candidate)) {
        return candidate;
      }
    }
    throw new IllegalStateException("testing/workspaces not found");
  }

  // -------------------------------------------------------------------------
  // Model builders
  // -------------------------------------------------------------------------

  private static DocumentModel model(String id, GroupElement... rootGroups) {
    return fill(new DocumentModel(), id, rootGroups);
  }

  private static TypeDefinitionModel typeDefinitionModel(String id) {
    return fill(new TypeDefinitionModel(), id);
  }

  private static AdditiveDocumentModel additiveModel(String id) {
    return fill(new AdditiveDocumentModel(), id);
  }

  private static <T extends DocumentModel> T fill(T model, String id, GroupElement... rootGroups) {
    model.setId(id);
    ModelRoot modelRoot = new ModelRoot();
    modelRoot.setRootGroups(new ArrayList<>(List.of(rootGroups)));
    DocumentModelContent content = new DocumentModelContent();
    content.setModelRoot(modelRoot);
    model.setContent(content);
    return model;
  }

  private static GroupElement group(String name, Element... children) {
    GroupElement group = new GroupElement();
    group.setId("group_" + name);
    group.setName(name);
    GroupConfig config = new GroupConfig();
    config.getElements().addAll(List.of(children));
    group.setGroup(config);
    return group;
  }

  private static GroupElement includeGroup(String name, String reference) {
    GroupElement group = group(name);
    IncludeConfig includeConfig = new IncludeConfig();
    includeConfig.setReference(reference);
    group.getGroup().setIncludeConfig(includeConfig);
    return group;
  }

  private static FieldElement field(String name) {
    FieldElement field = new FieldElement();
    field.setId("field_" + name);
    field.setName(name);
    FieldConfig config = new FieldConfig();
    config.setFieldType(new StringFieldType());
    field.setField(config);
    return field;
  }

  private static FieldElement typeDefField(String name, String typeDefinitionId) {
    FieldElement field = field(name);
    TypeDefFieldType fieldType = new TypeDefFieldType();
    fieldType.getTypeDefType().setTypeDefinitionId(typeDefinitionId);
    field.getField().setFieldType(fieldType);
    return field;
  }

  private static String typeDefinitionIdOf(FieldElement field) {
    return ((TypeDefFieldType) field.getField().getFieldType()).getTypeDefType().getTypeDefinitionId();
  }

  private static TypeDefinition typeDef(String id, String name) {
    TypeDefinition typeDefinition = new TypeDefinition();
    typeDefinition.setId(id);
    typeDefinition.setName(name);
    typeDefinition.setFieldType(new StringFieldType());
    return typeDefinition;
  }

  private static ModelReference typeDefinitionImport(String reference) {
    ModelReference modelReference = new ModelReference();
    modelReference.setAlias(reference);
    modelReference.setModelType(ModelType.DOCUMENT);
    modelReference.setPurpose(ModelReference.PURPOSE_TYPE_DEFINITIONS);
    modelReference.setReference(reference);
    return modelReference;
  }

  private static Label label(String locale, String text) {
    Label label = new Label();
    label.setLocale(locale);
    label.setText(text);
    return label;
  }

  private static Locale locale(String code) {
    Locale locale = new Locale();
    locale.setCode(code);
    return locale;
  }

  private static boolean isInclude(Element element) {
    return element instanceof GroupElement group && group.getGroup() != null && group.getGroup().getIncludeConfig() != null;
  }

  private static List<Element> allElements(List<? extends Element> roots) {
    List<Element> result = new ArrayList<>();
    for (Element root : roots) {
      collect(root, result);
    }
    return result;
  }

  private static void collect(Element element, List<Element> result) {
    result.add(element);
    if (element instanceof GroupElement group && group.getGroup() != null) {
      for (Element child : group.getGroup().getElements()) {
        collect(child, result);
      }
    }
  }

  private static List<String> names(List<? extends Element> elements) {
    return elements.stream().map(Element::getName).toList();
  }

  private static String json(DocumentModel model) {
    return JsonSettings.objectMapper.writeValueAsString(model);
  }
}
