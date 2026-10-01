package de.a12.studio.modelsvalidation.validators;

import de.a12.studio.models.Annotation;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.documentmodel.DocumentModelContent;
import de.a12.studio.models.documentmodel.FieldElement;
import de.a12.studio.models.documentmodel.GroupElement;
import de.a12.studio.models.documentmodel.ModelRoot;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.TestModels;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ElementAnnotationDuplicateValidatorTest {

  private final ElementAnnotationDuplicateValidator validator = new ElementAnnotationDuplicateValidator();

  @Test
  void distinctNamesOnOneElementAreFine() {
    DocumentModel model = model(annotation("a"), annotation("b"));

    assertTrue(validator.validate(model, TestModels.context(model)).isEmpty());
  }

  @Test
  void sameNameOnDifferentElementsIsFine() {
    FieldElement first = field("first", annotation("a"));
    FieldElement second = field("second", annotation("a"));
    DocumentModel model = model(group("G", first, second));

    assertTrue(validator.validate(model, TestModels.context(model)).isEmpty());
  }

  @Test
  void reportsADuplicateOnceOnTheOwningElement() {
    DocumentModel model = model(annotation("a"), annotation("b"), annotation("a"), annotation("a"));

    List<ModelValidationError> errors = validator.validate(model, TestModels.context(model));

    assertEquals(1, errors.size());
    assertEquals("field_x", errors.get(0).elementId());
    assertTrue(errors.get(0).message().contains("\"a\""), errors.get(0).message());
  }

  @Test
  void blankNamesAreIgnored() {
    DocumentModel model = model(annotation(""), annotation(""));

    assertTrue(validator.validate(model, TestModels.context(model)).isEmpty());
  }

  private static Annotation annotation(String name) {
    Annotation annotation = new Annotation();
    annotation.setName(name);
    return annotation;
  }

  private static FieldElement field(String name, Annotation... annotations) {
    FieldElement field = new FieldElement();
    field.setId("field_" + name);
    field.setName(name);
    field.getAnnotations().addAll(List.of(annotations));
    return field;
  }

  private static GroupElement group(String name, FieldElement... fields) {
    GroupElement group = new GroupElement();
    group.setId("group_" + name);
    group.setName(name);
    de.a12.studio.models.documentmodel.GroupConfig config = new de.a12.studio.models.documentmodel.GroupConfig();
    config.setElements(new ArrayList<>(List.of(fields)));
    group.setGroup(config);
    return group;
  }

  private static DocumentModel model(Annotation... annotations) {
    return model(group("G", field("x", annotations)));
  }

  private static DocumentModel model(GroupElement root) {
    ModelRoot modelRoot = new ModelRoot();
    modelRoot.setRootGroups(new ArrayList<>(List.of(root)));
    DocumentModelContent content = new DocumentModelContent();
    content.setModelRoot(modelRoot);
    DocumentModel model = new DocumentModel();
    model.setId("Test_DM");
    model.setContent(content);
    return model;
  }
}
