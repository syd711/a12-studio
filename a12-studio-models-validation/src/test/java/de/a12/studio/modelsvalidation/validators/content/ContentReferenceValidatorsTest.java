package de.a12.studio.modelsvalidation.validators.content;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.contentmodel.ContentConfiguration;
import de.a12.studio.models.contentmodel.ContentElement;
import de.a12.studio.models.contentmodel.ContentModel;
import de.a12.studio.models.contentmodel.ContentModelContent;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.Severity;
import de.a12.studio.modelsvalidation.TestModels;
import de.a12.studio.modelsvalidation.validators.ModelValidator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The checks of a Content Model against the Document Model it is bound to, run on small pages against the real
 * {@code Product_DM} of the e-commerce fixture workspace (which includes {@code Product_Common_DM}). Its groups, by the
 * ids the Content Model uses (an element of the included model is prefixed with the Include's id):
 * <pre>
 * Product (1) / Common (1) / Common (1)
 *   NumberOfVariants, IsProductActive (Boolean), HasMatureContent (Confirm), Name (String), Type (Enumeration), ...
 *   General (1)              ERP_ID (String), MainImage (attachment), AdditionalImages (10)
 *   Pricing (1000)           Price, Start, Helper_IsHighestPrice (Confirm), ...
 *   Discounts (100)          Discount (Number), HighlightDiscount, ...
 *   Variants (100)           VariantName (String), VariantDifferentDeliveryTime (Number), Attributes (25) / AttributeName
 * </pre>
 */
class ContentReferenceValidatorsTest {

  private static final String DM = "Product_DM";
  private static final String P = "include_common_";
  private static final String GENERAL = P + "group_1094d";
  private static final String ADDITIONAL_IMAGES = P + "group_57fbb";
  private static final String PRICING = P + "group_7b560";
  private static final String VARIANTS = P + "group_082cd";
  private static final String ATTRIBUTES = P + "group_8767d";
  private static final String MAIN_IMAGE = P + "group_8837c";
  private static final String NAME = P + "field_4217e";
  private static final String NUMBER_OF_VARIANTS = P + "field_12ee1";
  private static final String IS_ACTIVE = P + "field_64375";
  private static final String HAS_MATURE_CONTENT = P + "field_803b8";
  private static final String TYPE = P + "field_0b973";
  private static final String HIGHEST_PRICE = P + "field_d2f50";
  private static final String VARIANT_NAME = P + "field_df210";
  private static final String VARIANT_DELIVERY = P + "field_6fde0";
  private static final String ATTRIBUTE_NAME = P + "field_5bafa";

  private static List<A12Model<?>> workspace;

  @BeforeAll
  static void loadWorkspace() throws IOException {
    Path products = locate().resolve("e-commerce").resolve("models").resolve("01_Products");
    workspace = new ArrayList<>();
    try (Stream<Path> files = Files.list(products)) {
      for (Path file : files.filter(p -> p.getFileName().toString().endsWith(".json")).toList()) {
        A12Model<?> model = new ProjectItem(file.toFile()).getModel();
        if (model != null) {
          workspace.add(model);
        }
      }
    }
  }

  // ---- base group ----

  @Test
  void aBaseGroupWithoutADocumentModelIsReported() {
    ContentModel page = page(null, new ContentElement[0]);
    page.getContent().getConfiguration().setBaseGroupId(VARIANTS);

    List<ModelValidationError> errors = run(new ContentBaseGroupValidator(), page);

    assertEquals(1, errors.size());
    assertTrue(errors.get(0).message().contains(VARIANTS) && errors.get(0).message().contains("not bound"), errors.get(0).message());
    assertEquals(ContentBaseGroupValidator.ELEMENT_ID, errors.get(0).elementId());
  }

  @Test
  void aBaseGroupTheDocumentModelDoesNotHaveOrThatIsAFieldIsReported() {
    ContentModel page = page(DM);
    page.getContent().getConfiguration().setBaseGroupId("nope");
    List<ModelValidationError> missing = run(new ContentBaseGroupValidator(), page);
    assertEquals(1, missing.size());
    assertTrue(missing.get(0).message().contains("nope") && missing.get(0).message().contains(DM), missing.get(0).message());

    page.getContent().getConfiguration().setBaseGroupId(NAME);
    List<ModelValidationError> field = run(new ContentBaseGroupValidator(), page);
    assertEquals(1, field.size());
    assertTrue(field.get(0).message().contains("not a group"), field.get(0).message());
  }

  @Test
  void aBaseGroupOfTheDocumentModelIsFineAndChangesWhatTheElementsMayReference() {
    ContentModel page = page(DM, group("g1", VARIANTS, field("f1", VARIANT_DELIVERY)));
    page.getContent().getConfiguration().setBaseGroupId(P + "group_aef13");
    assertEquals(List.of(), run(new ContentBaseGroupValidator(), page));
    assertEquals(List.of(), run(new ContentGroupReferenceValidator(), page), "Variants is below the base group");
    assertEquals(List.of(), run(new ContentFieldReferenceValidator(), page));

    page.getContent().getConfiguration().setBaseGroupId(VARIANTS);
    List<ModelValidationError> errors = run(new ContentGroupReferenceValidator(), page);
    assertEquals(1, errors.size(), "the group is not below itself: " + errors);
    assertEquals("g1", errors.get(0).elementId());
  }

  @Test
  void nothingElseIsCheckedWhileTheBaseGroupIsInvalid() {
    ContentModel page = page(DM, group("g1", "unknown"), field("f1", "unknown"));
    page.getContent().getConfiguration().setBaseGroupId("nope");

    assertEquals(List.of(), run(new ContentGroupReferenceValidator(), page));
    assertEquals(List.of(), run(new ContentFieldReferenceValidator(), page));
  }

  // ---- group references ----

  @Test
  void repeatableGroupsNeedAnExistingGroupOfTheDocumentModel() {
    ContentModel page = page(DM,
        group("ok", VARIANTS),
        group("empty", ""),
        group("gone", "nope"),
        group("aField", NAME));

    List<ModelValidationError> errors = run(new ContentGroupReferenceValidator(), page);

    assertEquals(List.of("empty", "gone", "aField"), errors.stream().map(ModelValidationError::elementId).toList());
    assertTrue(errors.get(0).message().contains("no group selected"), errors.get(0).message());
    assertTrue(errors.get(1).message().contains("nope") && errors.get(1).message().contains(DM), errors.get(1).message());
    assertTrue(errors.get(2).message().contains("not a group"), errors.get(2).message());
    errors.forEach(error -> assertEquals(Severity.ERROR.name(), error.severity()));
  }

  @Test
  void aGroupInsideARepeatableGroupMustBeBelowIt() {
    ContentModel page = page(DM,
        group("variants", VARIANTS,
            group("attributes", ATTRIBUTES),
            group("pricing", PRICING)));

    List<ModelValidationError> errors = run(new ContentGroupReferenceValidator(), page);

    assertEquals(1, errors.size(), errors.toString());
    assertEquals("pricing", errors.get(0).elementId());
    assertTrue(errors.get(0).message().contains("/Variants") || errors.get(0).message().contains("Variants"), errors.get(0).message());
  }

  @Test
  void anUnboundModelReportsEveryGroup() {
    ContentModel page = page(null, group("g1", VARIANTS, field("f1", "x")));

    List<ModelValidationError> errors = run(new ContentGroupReferenceValidator(), page);

    assertEquals(1, errors.size(), "the elements below a group that cannot be resolved are not visited");
    assertTrue(errors.get(0).message().contains("needs a Document Model"), errors.get(0).message());
  }

  @Test
  void anImagesDynamicSourceIsAGroupReferenceToo() {
    ContentModel page = page(DM,
        element("img1", "Image", "src", map("static", "", "dynamic", MAIN_IMAGE)),
        element("img2", "Image", "src", map("static", "x.png", "dynamic", "nope")),
        element("img3", "Image", "src", map("static", "x.png")));

    List<ModelValidationError> errors = run(new ContentGroupReferenceValidator(), page);

    assertEquals(List.of("img2"), errors.stream().map(ModelValidationError::elementId).toList());
  }

  @Test
  void anAddRowActionMustReferenceAGroupThatCanBeAddedTo() {
    ContentModel page = page(DM,
        eventButton("b1", "AddRowAction", map("groupId", ADDITIONAL_IMAGES)),
        eventButton("b2", "AddRowAction", map("groupId", GENERAL)),
        eventButton("b3", "AddRowAction", map("groupId", "")),
        eventButton("b4", "AddRowAction", map("groupId", MAIN_IMAGE)));

    List<ModelValidationError> errors = run(new ContentGroupReferenceValidator(), page);

    assertEquals(List.of("b2", "b3", "b4"), errors.stream().map(ModelValidationError::elementId).toList(),
        "findings of an event node are reported on the element that holds it");
    assertTrue(errors.get(0).message().contains("repeatable") || errors.get(0).message().contains("added to"), errors.get(0).message());
  }

  // ---- field references ----

  @Test
  void fieldOutputsAndConditionsNeedAFieldThatIsReachableFromWhereTheyStand() {
    ContentModel page = page(DM,
        element("ok1", "FieldOutput", "fieldId", NAME),
        element("ok2", "FieldOutput", "fieldId", NUMBER_OF_VARIANTS),
        element("repeated", "FieldOutput", "fieldId", HIGHEST_PRICE),
        element("gone", "FieldOutput", "fieldId", "nope"),
        element("aGroup", "FieldOutput", "fieldId", PRICING),
        element("blank", "FieldOutput", "fieldId", ""),
        conditional("cond", NAME, HIGHEST_PRICE, "nope"));

    List<ModelValidationError> errors = run(new ContentFieldReferenceValidator(), page);

    assertEquals(List.of("repeated", "gone", "aGroup", "blank", "cond", "cond"), errors.stream().map(ModelValidationError::elementId).toList(),
        errors.toString());
    assertTrue(errors.get(0).message().contains("not available"), errors.get(0).message());
    assertTrue(errors.get(1).message().contains("nope") && errors.get(1).message().contains(DM), errors.get(1).message());
    assertTrue(errors.get(2).message().contains("group, not a field"), errors.get(2).message());
    assertTrue(errors.get(3).message().contains("no field selected"), errors.get(3).message());
  }

  @Test
  void insideARepeatableGroupItsOwnFieldsAndTheOnesAboveAreAvailable() {
    ContentModel page = page(DM,
        group("variants", VARIANTS,
            element("own", "FieldOutput", "fieldId", VARIANT_DELIVERY),
            element("above", "FieldOutput", "fieldId", NUMBER_OF_VARIANTS),
            element("sibling", "FieldOutput", "fieldId", HIGHEST_PRICE),
            element("deeper", "FieldOutput", "fieldId", ATTRIBUTE_NAME)));

    List<ModelValidationError> errors = run(new ContentFieldReferenceValidator(), page);

    assertEquals(List.of("sibling", "deeper"), errors.stream().map(ModelValidationError::elementId).toList());
  }

  @Test
  void fieldReferencesInsideATextAreCheckedToo() {
    ContentElement paragraph = element("p1", "Paragraph");
    paragraph.getProps().put("tree", map("root", map("children", List.of(map("type", "paragraph", "children", List.of(
        map("type", "ce-field-reference", "fieldId", NAME, "fieldPath", "/Product/Name"),
        map("type", "text", "text", " and "),
        map("type", "ce-field-reference", "fieldId", "nope", "fieldPath", "/Product/Nope"),
        map("type", "ce-field-reference", "fieldId", PRICING, "fieldPath", "/Product/Pricing", "isGroup", true),
        map("type", "ce-field-reference", "fieldId", NAME, "fieldPath", "/Product/Name", "isGroup", true)))))));
    ContentModel page = page(DM, paragraph);

    List<ModelValidationError> errors = run(new ContentFieldReferenceValidator(), page);

    assertEquals(2, errors.size(), errors.toString());
    assertTrue(errors.get(0).message().contains("nope"), errors.get(0).message());
    assertTrue(errors.get(1).message().contains("not a group"), errors.get(1).message());
  }

  @Test
  void aTextWithoutReferencesNeedsNoDocumentModel() {
    ContentModel page = page(null, element("p1", "Paragraph"), element("h1", "Heading"));

    assertEquals(List.of(), run(new ContentFieldReferenceValidator(), page));
  }

  // ---- form elements ----

  @Test
  void aFormElementNeedsAnElementOfADataTypeItCanShow() {
    ContentModel page = page(DM,
        form("ok1", "TextLine", NAME),
        form("ok2", "Checkbox", HAS_MATURE_CONTENT),
        form("ok3", "Switch", IS_ACTIVE),
        form("ok4", "Select", TYPE),
        form("bad1", "Checkbox", NAME),
        form("bad2", "TextLine", IS_ACTIVE),
        form("bad3", "Select", NAME));

    List<ModelValidationError> errors = run(new ContentFormElementValidator(), page);

    assertEquals(List.of("bad1", "bad2", "bad3"), errors.stream().map(ModelValidationError::elementId).toList(), errors.toString());
    assertTrue(errors.get(0).message().contains("BooleanType, ConfirmType"), errors.get(0).message());
  }

  @Test
  void aFormElementNeedsAnExistingElementAndTheDocumentModel() {
    ContentModel page = page(DM, form("blank", "TextLine", ""), form("gone", "TextLine", "nope"));
    List<ModelValidationError> errors = run(new ContentFormElementValidator(), page);
    assertEquals(List.of("blank", "gone"), errors.stream().map(ModelValidationError::elementId).toList());
    assertTrue(errors.get(0).message().contains("no Document Model element selected"), errors.get(0).message());
    assertTrue(errors.get(1).message().contains("nope") && errors.get(1).message().contains(DM), errors.get(1).message());

    ContentModel unbound = page(null, form("f", "TextLine", NAME));
    List<ModelValidationError> unboundErrors = run(new ContentFormElementValidator(), unbound);
    assertEquals(1, unboundErrors.size());
    assertTrue(unboundErrors.get(0).message().contains("needs a Document Model"), unboundErrors.get(0).message());
  }

  @Test
  void aFormElementMayNotBeMoreRepeatedThanItsDataContext() {
    ContentModel page = page(DM,
        form("top", "TextLine", VARIANT_NAME),
        group("variants", VARIANTS,
            form("inside", "TextLine", VARIANT_NAME),
            form("outer", "TextLine", NAME),
            form("deeper", "TextLine", ATTRIBUTE_NAME),
            group("attributes", ATTRIBUTES, form("innermost", "TextLine", ATTRIBUTE_NAME))));

    List<ModelValidationError> errors = run(new ContentFormElementValidator(), page);

    assertEquals(List.of("top", "deeper"), errors.stream().map(ModelValidationError::elementId).toList(), errors.toString());
    assertTrue(errors.get(0).message().contains("data context"), errors.get(0).message());
  }

  @Test
  void theMultiSelectElementsTakeMultiSelectGroupsOnly() {
    ContentModel page = page(DM, form("m1", "MultiSelect", GENERAL), form("m2", "CheckboxGroup", NAME));

    List<ModelValidationError> errors = run(new ContentFormElementValidator(), page);

    assertEquals(List.of("m1", "m2"), errors.stream().map(ModelValidationError::elementId).toList());
    assertTrue(errors.get(0).message().contains("multi-select groups"), errors.get(0).message());
  }

  @Test
  void aFormElementWithChildrenIsOnlyWarnedAbout() {
    ContentElement textLine = form("t1", "TextLine", NAME);
    textLine.getChildren().add(element("c1", "Paragraph"));

    List<ModelValidationError> errors = run(new ContentFormElementValidator(), page(DM, textLine));

    assertEquals(1, errors.size());
    assertEquals(Severity.WARNING.name(), errors.get(0).severity());
    assertEquals("t1", errors.get(0).elementId());
  }

  // ---- events ----

  @Test
  void theActionsThatWorkOnTheDocumentNeedOne() {
    ContentModel unbound = page(null,
        eventButton("save", "SaveAction", map()),
        eventButton("commit", "CommitAction", map()),
        eventButton("cancel", "CancelAction", map()),
        eventButton("delete", "DeleteRowAction", map()),
        eventButton("name", null, null));
    assertEquals(List.of("save", "commit", "cancel", "delete"),
        run(new ContentEventNodeValidator(), unbound).stream().map(ModelValidationError::elementId).toList());

    ContentModel bound = page(DM, eventButton("save", "SaveAction", map()), eventButton("delete", "DeleteRowAction", map()));
    assertEquals(List.of(), run(new ContentEventNodeValidator(), bound));
  }

  @Test
  void aTablesRowClickIsAnEventNodeToo() {
    ContentElement table = element("t1", "Table");
    table.getProps().put("onRowClick", eventNode("SaveAction", map()));

    assertEquals(List.of("t1"), run(new ContentEventNodeValidator(), page(null, table)).stream().map(ModelValidationError::elementId).toList());
  }

  // ---- shared context ----

  @Test
  void theValidatorsShareOneResolvedDocumentModelPerCall() {
    ContentModel page = page(DM, element("f", "FieldOutput", "fieldId", NAME));
    var context = TestModels.contextWithOtherModels(page, workspace.toArray(new A12Model<?>[0]));

    Object first = context.cached("k", () -> new Object());
    Object second = context.cached("k", () -> new Object());
    Object nullResult = context.cached("n", () -> null);

    assertEquals(first, second);
    assertEquals(null, nullResult);
    assertEquals(List.of(), new ContentFieldReferenceValidator().validate(page, context));
    assertEquals(List.of(), new ContentGroupReferenceValidator().validate(page, context));
  }

  // ---- helpers ----

  private static List<ModelValidationError> run(ModelValidator validator, ContentModel page) {
    return validator.validate(page, TestModels.contextWithOtherModels(page, workspace.toArray(new A12Model<?>[0])));
  }

  /** A Content Model whose root is a Box with {@code children}, bound to {@code documentModelId} if not null. */
  static ContentModel page(String documentModelId, ContentElement... children) {
    ContentModel model = new ContentModel();
    model.setId("Page_CM");
    ContentModelContent content = new ContentModelContent();
    ContentConfiguration configuration = new ContentConfiguration();
    configuration.getNamespaceVersions().put("com.mgmtp.a12.contentengine", "0.9.0");
    content.setConfiguration(configuration);
    ContentElement root = element("root", "Box");
    root.getChildren().addAll(List.of(children));
    content.setRoot(root);
    model.setContent(content);
    model.setDocumentModelId(documentModelId);
    return model;
  }

  static ContentElement element(String id, String type, Object... propertyPairs) {
    return elementIn("com.mgmtp.a12.contentengine", id, type, propertyPairs);
  }

  static ContentElement form(String id, String type, String elementId) {
    return elementIn("com.mgmtp.a12.formengine", id, type, new Object[] {"elementId", elementId});
  }

  private static ContentElement elementIn(String namespace, String id, String type, Object[] propertyPairs) {
    ContentElement element = new ContentElement();
    element.setId(id);
    element.setNamespace(namespace);
    element.setType(type);
    element.setProps(map(propertyPairs));
    element.setChildren(new ArrayList<>());
    return element;
  }

  static ContentElement group(String id, String groupId, ContentElement... children) {
    ContentElement group = element(id, "Group", "groupId", groupId);
    group.getChildren().addAll(List.of(children));
    return group;
  }

  static ContentElement field(String id, String fieldId) {
    return element(id, "FieldOutput", "fieldId", fieldId);
  }

  static ContentElement conditional(String id, String... fieldIds) {
    List<Object> conditions = new ArrayList<>();
    for (String fieldId : fieldIds) {
      conditions.add(map("operator", "equal", "fieldId", fieldId));
    }
    return element(id, "Conditional", "conditions", conditions);
  }

  /** A Button whose click runs an action of {@code eventType} (a plain event name when it is null). */
  static ContentElement eventButton(String id, String eventType, Map<String, Object> eventProps) {
    ContentElement button = element(id, "Button", "label", "Go");
    button.getProps().put("onClick", eventType == null ? "someEvent" : eventNode(eventType, eventProps));
    return button;
  }

  private static Map<String, Object> eventNode(String type, Map<String, Object> props) {
    return map("id", "event-" + type, "type", type, "namespace", "com.mgmtp.a12.contentengine", "props", props, "children", List.of());
  }

  static Map<String, Object> map(Object... pairs) {
    Map<String, Object> result = new LinkedHashMap<>();
    for (int i = 0; i + 1 < pairs.length; i += 2) {
      result.put((String) pairs[i], pairs[i + 1]);
    }
    return result;
  }

  private static Path locate() {
    for (Path dir = Path.of("").toAbsolutePath(); dir != null; dir = dir.getParent()) {
      Path candidate = dir.resolve("testing").resolve("workspaces");
      if (Files.isDirectory(candidate)) {
        return candidate;
      }
    }
    throw new IllegalStateException("Could not locate testing/workspaces");
  }
}
