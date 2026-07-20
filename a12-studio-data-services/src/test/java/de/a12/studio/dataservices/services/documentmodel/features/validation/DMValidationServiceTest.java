package de.a12.studio.dataservices.services.documentmodel.features.validation;

import de.a12.studio.models.Locale;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.documentmodel.DocumentModelContent;
import de.a12.studio.models.documentmodel.FieldConfig;
import de.a12.studio.models.documentmodel.FieldElement;
import de.a12.studio.models.documentmodel.GroupConfig;
import de.a12.studio.models.documentmodel.GroupElement;
import de.a12.studio.models.documentmodel.ModelConfig;
import de.a12.studio.models.documentmodel.ModelRoot;
import de.a12.studio.models.documentmodel.NumberFieldType;
import de.a12.studio.models.documentmodel.NumberTypeOptions;
import de.a12.studio.models.documentmodel.StringFieldType;
import de.a12.studio.models.documentmodel.TypeDefFieldType;
import de.a12.studio.models.documentmodel.TypeDefTypeOptions;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DMValidationServiceTest {

  private final DMValidationService service = new DMValidationService();

  @Test
  void cleanModelHasNoErrors() {
    DocumentModel model = newModel(newGroup("g1", "root", newField("f1", "name", null)));

    assertEquals(List.of(), service.validateDocument(model, List.of()));
  }

  @Test
  void detectsDuplicateElementIds() {
    FieldElement fieldA = newField("dup", "fieldA", null);
    FieldElement fieldB = newField("dup", "fieldB", null);
    DocumentModel model = newModel(newGroup("g1", "root", fieldA, fieldB));

    List<ElementValidationError> errors = service.validateDocument(model, List.of());

    long duplicateErrors = errors.stream().filter(e -> "dup".equals(e.elementId()) && e.message().contains("not unique")).count();
    assertEquals(2, duplicateErrors);
  }

  @Test
  void detectsNumberFieldValueBeyondLimit() {
    NumberTypeOptions numberType = new NumberTypeOptions();
    numberType.setMaxFractionalDigits(2);
    numberType.setMaxValue(1.0e20);
    NumberFieldType numberFieldType = new NumberFieldType();
    numberFieldType.setNumberType(numberType);

    DocumentModel model = newModel(newGroup("g1", "root", newField("f1", "amount", numberFieldType)));

    List<ElementValidationError> errors = service.validateDocument(model, List.of());

    assertTrue(errors.stream().anyMatch(e -> "f1".equals(e.elementId()) && e.message().contains("maximum value")));
  }

  @Test
  void detectsMissingTypeDefinitionReference() {
    TypeDefFieldType typeDefFieldType = new TypeDefFieldType();
    typeDefFieldType.setTypeDefType(new TypeDefTypeOptions());

    DocumentModel model = newModel(newGroup("g1", "root", newField("f1", "custom", typeDefFieldType)));

    List<ElementValidationError> errors = service.validateDocument(model, List.of());

    assertTrue(errors.stream().anyMatch(e -> "f1".equals(e.elementId()) && e.message().contains("Missing Type Definition")));
  }

  @Test
  void reportsMissingLocale() {
    DocumentModel model = newModel(newGroup("g1", "root", newField("f1", "name", null)));
    model.setLocales(List.of());

    assertEquals("Please add at least one locale.", service.getMissingLocaleError(model).orElseThrow());
  }

  private static DocumentModel newModel(GroupElement rootGroup) {
    DocumentModel model = new DocumentModel();
    model.setId("dm1");
    model.setModelVersion("28.4.0");
    Locale en = new Locale();
    en.setCode("en");
    model.setLocales(List.of(en));

    ModelConfig modelConfig = new ModelConfig();
    modelConfig.setTimeZone("Europe/Berlin");

    ModelRoot modelRoot = new ModelRoot();
    modelRoot.setRootGroups(List.of(rootGroup));

    DocumentModelContent content = new DocumentModelContent();
    content.setModelConfig(modelConfig);
    content.setModelRoot(modelRoot);
    model.setContent(content);
    return model;
  }

  private static GroupElement newGroup(String id, String name, FieldElement... fields) {
    GroupElement group = new GroupElement();
    group.setId(id);
    group.setName(name);
    GroupConfig groupConfig = new GroupConfig();
    groupConfig.setRepeatability(1);
    groupConfig.setElements(List.of(fields));
    group.setGroup(groupConfig);
    return group;
  }

  private static FieldElement newField(String id, String name, de.a12.studio.models.documentmodel.FieldType fieldType) {
    FieldElement field = new FieldElement();
    field.setId(id);
    field.setName(name);
    FieldConfig fieldConfig = new FieldConfig();
    fieldConfig.setFieldType(fieldType == null ? new StringFieldType() : fieldType);
    field.setField(fieldConfig);
    return field;
  }
}
