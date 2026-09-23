package de.a12.studio.modelsvalidation.validators.form;

import de.a12.studio.models.ModelReference;
import de.a12.studio.models.ModelType;
import de.a12.studio.models.composeddocumentmodel.ComposedDocumentModel;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.documentmodel.DocumentModelContent;
import de.a12.studio.models.formmodel.BindingContent;
import de.a12.studio.models.formmodel.BindingDetails;
import de.a12.studio.models.formmodel.BindingRepeat;
import de.a12.studio.models.formmodel.FormModel;
import de.a12.studio.models.formmodel.FormModelContent;
import de.a12.studio.models.formmodel.Screen;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.TestModels;
import de.a12.studio.modelsvalidation.ValidationContext;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FormBindingRepeatCdmRequiredValidatorTest {

  private static FormModel formModelBoundTo(String documentModelId) {
    FormModel model = new FormModel();
    model.setId("Order_FM");
    FormModelContent content = new FormModelContent();
    Screen screen = new Screen();
    BindingRepeat bindingRepeat = new BindingRepeat();
    bindingRepeat.setId("bindingrepeat1");
    BindingContent bindingContent = new BindingContent();
    bindingContent.setDetails(new BindingDetails());
    bindingRepeat.setBinding(bindingContent);
    screen.getScreenElements().add(bindingRepeat);
    content.getScreens().add(screen);
    model.setContent(content);

    ModelReference reference = new ModelReference();
    reference.setModelType(ModelType.DOCUMENT);
    reference.setPurpose(ModelReference.PURPOSE_DATA_BINDING);
    reference.setReference(documentModelId);
    model.getModelReferences().add(reference);
    return model;
  }

  @Test
  void reportsABindingRepeatWhenTheBoundModelIsAPlainDocumentModel() {
    FormModel model = formModelBoundTo("Order_DM");
    DocumentModel documentModel = new DocumentModel();
    documentModel.setId("Order_DM");
    documentModel.setContent(new DocumentModelContent());

    ValidationContext context = TestModels.contextWithDocumentModels(model, documentModel);
    List<ModelValidationError> errors = new FormBindingRepeatCdmRequiredValidator().validate(model, context);

    assertEquals(1, errors.size());
    assertEquals("bindingrepeat1", errors.get(0).elementId());
  }

  @Test
  void reportsABindingRepeatWhenTheBoundModelIsUnresolved() {
    FormModel model = formModelBoundTo("Missing_DM");
    List<ModelValidationError> errors = new FormBindingRepeatCdmRequiredValidator().validate(model, TestModels.context(model));

    assertEquals(1, errors.size());
  }

  @Test
  void acceptsABindingRepeatWhenTheBoundModelIsAComposedDocumentModel() {
    FormModel model = formModelBoundTo("Order_CdM");
    ComposedDocumentModel composedDocumentModel = new ComposedDocumentModel();
    composedDocumentModel.setId("Order_CdM");
    composedDocumentModel.setContent(new DocumentModelContent());

    ValidationContext context = TestModels.contextWithDocumentModels(model, composedDocumentModel);
    assertEquals(List.of(), new FormBindingRepeatCdmRequiredValidator().validate(model, context));
  }
}
