package de.a12.studio.modelsvalidation.validators;

import de.a12.studio.models.documentmodel.DateRangeFieldType;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.documentmodel.DocumentModelContent;
import de.a12.studio.models.documentmodel.FieldConfig;
import de.a12.studio.models.documentmodel.FieldElement;
import de.a12.studio.models.documentmodel.GroupConfig;
import de.a12.studio.models.documentmodel.GroupElement;
import de.a12.studio.models.documentmodel.ModelInfo;
import de.a12.studio.models.documentmodel.ModelRoot;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.TestModels;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DateInterpretationOfYearValidatorTest {

  private final DateInterpretationOfYearValidator validator = new DateInterpretationOfYearValidator();

  @Test
  void aNonStandardInterpretationNeedsABaseYear() {
    DocumentModel model = model("FROM", null);

    List<ModelValidationError> errors = validator.validate(model, TestModels.context(model));

    assertEquals(1, errors.size());
    assertEquals("field_range", errors.get(0).elementId());
  }

  @Test
  void aBaseYearAllowsAnyInterpretation() {
    DocumentModel model = model("TO", 2020);

    assertTrue(validator.validate(model, TestModels.context(model)).isEmpty());
  }

  @Test
  void theStandardInterpretationIsAlwaysFine() {
    DocumentModel model = model(null, null);

    assertTrue(validator.validate(model, TestModels.context(model)).isEmpty());
  }

  private static DocumentModel model(String interpretation, Integer baseYear) {
    DateRangeFieldType type = new DateRangeFieldType();
    type.getDateRangeType().setInterpretationOfYear(interpretation);
    FieldElement field = new FieldElement();
    field.setId("field_range");
    field.setName("Range");
    FieldConfig fieldConfig = new FieldConfig();
    fieldConfig.setFieldType(type);
    field.setField(fieldConfig);
    GroupElement root = new GroupElement();
    root.setId("group_root");
    root.setName("Root");
    GroupConfig config = new GroupConfig();
    config.setRepeatability(1);
    config.setElements(new ArrayList<>(List.of(field)));
    root.setGroup(config);
    ModelRoot modelRoot = new ModelRoot();
    modelRoot.setRootGroups(new ArrayList<>(List.of(root)));
    DocumentModelContent content = new DocumentModelContent();
    content.setModelRoot(modelRoot);
    ModelInfo info = new ModelInfo();
    info.setBaseYear(baseYear);
    content.setModelInfo(info);
    DocumentModel model = new DocumentModel();
    model.setId("Test_DM");
    model.setContent(content);
    return model;
  }
}
