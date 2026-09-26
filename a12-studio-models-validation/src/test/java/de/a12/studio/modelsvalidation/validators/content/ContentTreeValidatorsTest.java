package de.a12.studio.modelsvalidation.validators.content;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.ModelType;
import de.a12.studio.models.contentmodel.ContentElement;
import de.a12.studio.models.contentmodel.ContentModel;
import de.a12.studio.models.formmodel.FormModel;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.Severity;
import de.a12.studio.modelsvalidation.TestModels;
import de.a12.studio.modelsvalidation.validators.ModelValidator;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static de.a12.studio.modelsvalidation.validators.content.ContentReferenceValidatorsTest.element;
import static de.a12.studio.modelsvalidation.validators.content.ContentReferenceValidatorsTest.form;
import static de.a12.studio.modelsvalidation.validators.content.ContentReferenceValidatorsTest.map;
import static de.a12.studio.modelsvalidation.validators.content.ContentReferenceValidatorsTest.page;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** The Content Model checks that do not need a Document Model: shape, structure rules, settings, warnings and the reference's type. */
class ContentTreeValidatorsTest {

  // ---- shape ----

  @Test
  void everyElementNeedsTypeNamespaceAndProps() {
    ContentElement noProps = element("a", "Paragraph");
    noProps.setProps(null);
    ContentElement noNamespace = element("b", "Paragraph");
    noNamespace.setNamespace(null);
    ContentElement noType = element("c", "Paragraph");
    noType.setType(null);
    ContentModel page = page(null, noProps, noNamespace, noType);

    List<ModelValidationError> errors = run(new ContentNodeShapeValidator(), page);

    assertEquals(List.of("a", "b", "c"), errors.stream().map(ModelValidationError::elementId).toList());
    assertTrue(errors.get(0).message().contains("\"props\""), errors.get(0).message());
    assertTrue(errors.get(1).message().contains("\"namespace\""), errors.get(1).message());
    assertTrue(errors.get(2).message().contains("\"type\""), errors.get(2).message());
    errors.forEach(error -> assertEquals(Severity.ERROR.name(), error.severity()));
  }

  @Test
  void theEngineNamespaceNeedsItsCurrentVersion() {
    ContentModel missing = page(null, element("a", "Paragraph"));
    missing.getContent().getConfiguration().getNamespaceVersions().clear();
    List<ModelValidationError> errors = run(new ContentNodeShapeValidator(), missing);
    assertEquals(1, errors.size(), "once, not for every element: " + errors);
    assertEquals(Severity.WARNING.name(), errors.get(0).severity());
    assertTrue(errors.get(0).message().contains("0.9.0"), errors.get(0).message());

    ContentModel old = page(null, element("a", "Paragraph"));
    old.getContent().getConfiguration().getNamespaceVersions().put("com.mgmtp.a12.contentengine", "0.8.0");
    List<ModelValidationError> other = run(new ContentNodeShapeValidator(), old);
    assertEquals(1, other.size());
    assertTrue(other.get(0).message().contains("0.8.0") && other.get(0).message().contains("0.9.0"), other.get(0).message());

    assertEquals(List.of(), run(new ContentNodeShapeValidator(), page(null, element("a", "Paragraph"))));
  }

  // ---- structure ----

  @Test
  void theStructureRulesAreReportedOnTheElementsThatBreakThem() {
    ContentModel page = page(null,
        element("head", "TableHead"),
        element("table", "Table", new Object[0]));

    List<ModelValidationError> errors = run(new ContentStructureValidator(), page);

    assertEquals(List.of("head", "table"), errors.stream().map(ModelValidationError::elementId).toList(), errors.toString());
    assertTrue(errors.get(0).message().contains("not allowed inside a Box"), errors.get(0).message());
    assertTrue(errors.get(1).message().contains("do not match"), errors.get(1).message());
  }

  @Test
  void aValidTreeAndAnEmptyModelHaveNoStructureFindings() {
    assertEquals(List.of(), run(new ContentStructureValidator(), page(null, element("p", "Paragraph"), element("b", "Box"))));

    ContentModel empty = page(null);
    empty.getContent().setRoot(null);
    assertEquals(List.of(), run(new ContentStructureValidator(), empty));
  }

  // ---- settings ----

  @Test
  void requiredSettingsAreReportedByName() {
    ContentModel page = page(null,
        element("m1", "MessageBox", "label", ""),
        element("m2", "MessageBox", "label", "Hello"),
        element("t1", "Tooltip"),
        element("e1", "Expandable", "icons", map("collapsedIcon", map("name", "x"))));

    List<ModelValidationError> errors = run(new ContentSettingsValidator(), page);

    assertEquals(List.of("m1", "t1", "e1"), errors.stream().map(ModelValidationError::elementId).toList(), errors.toString());
    assertTrue(errors.get(0).message().contains("\"label\"") && errors.get(0).message().contains("Message Box"), errors.get(0).message());
    assertTrue(errors.get(1).message().contains("\"text\""), errors.get(1).message());
    assertTrue(errors.get(2).message().contains("icons.expandedIcon"), errors.get(2).message());
  }

  @Test
  void anImageNeedsASourceAndASafeUrl() {
    ContentModel page = page(null,
        element("none", "Image", "src", map("static", "")),
        element("oldNone", "Image", "src", ""),
        element("static", "Image", "src", map("static", "https://example.com/a.png")),
        element("relative", "Image", "src", map("static", "images/a.png")),
        element("dynamic", "Image", "src", map("static", "", "dynamic", "some_group")),
        element("script", "Image", "src", map("static", "javascript:alert(1)")));

    List<ModelValidationError> errors = run(new ContentSettingsValidator(), page);

    assertEquals(List.of("none", "oldNone", "script"), errors.stream().map(ModelValidationError::elementId).toList(), errors.toString());
    assertTrue(errors.get(0).message().contains("no source"), errors.get(0).message());
    assertTrue(errors.get(2).message().contains("src.static") && errors.get(2).message().contains("not allowed"), errors.get(2).message());
  }

  @Test
  void linkAndVideoUrlsMustUseASafeScheme() {
    ContentModel page = page(null,
        element("l1", "Link", "href", "https://example.com"),
        element("l2", "Link", "href", "mailto:me@example.com"),
        element("l3", "Link", "href", "javascript:alert(1)"),
        element("l4", "Link"),
        element("v1", "Video", "src", ""),
        element("v2", "Video", "src", "data:text/html;base64,AAAA"));

    List<ModelValidationError> errors = run(new ContentSettingsValidator(), page);

    assertEquals(List.of("l3", "v2"), errors.stream().map(ModelValidationError::elementId).toList(), errors.toString());
    assertTrue(errors.get(0).message().contains("\"href\""), errors.get(0).message());
  }

  // ---- warnings ----

  @Test
  void theEditingHintsAreWarningsOnly() {
    ContentElement verticalButton = element("btn", "Button", "vertical", true, "label", "Go");
    ContentElement display = form("mgd", "MessageGroupDisplay", "");
    ContentElement container = form("mgc", "MessageGroupContainer", "");
    container.getChildren().add(form("mgd2", "MessageGroupDisplay", ""));
    ContentElement table = element("table", "Table", "screenReaderColumnRef", "c1",
        "columns", List.of(map("id", "c1", "actionColumn", true)));
    ContentModel page = page(null, element("box", "Box"), verticalButton, display, container, table,
        element("mq", "MediaQuery"));
    page.getContent().getRoot().setType("Grid");

    List<ModelValidationError> errors = run(new ContentWarningsValidator(), page);

    assertEquals(List.of("box", "btn", "mgd", "table", "mq"), errors.stream().map(ModelValidationError::elementId).toList(), errors.toString());
    errors.forEach(error -> assertEquals(Severity.WARNING.name(), error.severity()));
  }

  @Test
  void aBoxWithChildrenAndAMediaQueryUnderABoxRootHaveNoWarnings() {
    ContentElement box = element("box", "Box", new Object[0]);
    box.getChildren().add(element("p", "Paragraph"));
    ContentModel page = page(null, box, element("mq", "MediaQuery"));

    assertEquals(List.of(), run(new ContentWarningsValidator(), page));
  }

  // ---- the document model reference ----

  @Test
  void theDocumentModelMustNotBeAModelOfAnotherType() {
    FormModel form = new FormModel();
    form.setId("Order_FM");
    form.setModelType(ModelType.FORM);
    ContentModel page = page("Order_FM");

    List<ModelValidationError> errors = new ContentDocumentModelTypeValidator()
        .validate(page, TestModels.contextWithOtherModels(page, form));

    assertEquals(1, errors.size());
    assertTrue(errors.get(0).message().contains("Order_FM"), errors.get(0).message());
    assertEquals(List.of(), new ContentDocumentModelTypeValidator().validate(page("Gone_DM"), TestModels.contextWithOtherModels(page)),
        "a missing model is the generic reference validator's finding");
    assertEquals(List.of(), new ContentDocumentModelTypeValidator().validate(page(null), TestModels.contextWithOtherModels(page)));
  }

  // ---- ids ----

  @Test
  void aDuplicateIdIsReportedOnTheElementThatRepeatsIt() {
    ContentModel page = page(null, element("a", "Paragraph"), element("a", "Heading"));

    List<ModelValidationError> errors = run(new ContentElementIdUniqueValidator(), page);

    assertEquals(1, errors.size());
    assertEquals("a", errors.get(0).elementId());
  }

  // ---- the other model types ----

  @Test
  void everyValidatorIgnoresModelsThatAreNotContentModels() {
    FormModel form = new FormModel();
    form.setId("Order_FM");
    List<ModelValidator> validators = List.of(new ContentNodeShapeValidator(), new ContentStructureValidator(), new ContentSettingsValidator(),
        new ContentWarningsValidator(), new ContentBaseGroupValidator(), new ContentGroupReferenceValidator(),
        new ContentFieldReferenceValidator(), new ContentFormElementValidator(), new ContentEventNodeValidator(),
        new ContentDocumentModelTypeValidator());

    for (ModelValidator validator : validators) {
      assertEquals(List.of(), validator.validate(form, TestModels.context(form)), validator.getClass().getSimpleName());
    }
  }

  private static List<ModelValidationError> run(ModelValidator validator, ContentModel page) {
    List<A12Model<?>> none = new ArrayList<>();
    return validator.validate(page, TestModels.contextWithOtherModels(page, none.toArray(new A12Model<?>[0])));
  }
}
