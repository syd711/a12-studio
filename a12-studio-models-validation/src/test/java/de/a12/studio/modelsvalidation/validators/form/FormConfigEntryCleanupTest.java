package de.a12.studio.modelsvalidation.validators.form;

import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.formmodel.FormModel;
import de.a12.studio.modelsvalidation.TestModels;
import de.a12.studio.modelsvalidation.ValidationContext;
import de.a12.studio.modelsvalidation.validators.ElementIndex;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FormConfigEntryCleanupTest {

  private FormModel load(String name) {
    return TestModels.load("/formmodel/" + name + ".json", FormModel.class);
  }

  private DocumentModel refDm() {
    return TestModels.load("/documentmodel/Ref_DM.json", DocumentModel.class);
  }

  private List<ElementIndex> indexes(FormModel model, ValidationContext context) {
    return FormDocumentModelIndexes.referencedDocumentModelIndexes(model, context);
  }

  @Test
  void removesDanglingFieldEntry() {
    FormModel model = load("FormFieldReferenceValidator_invalid");
    ValidationContext context = TestModels.contextWithDocumentModels(model, refDm());

    int removed = FormConfigEntryCleanup.removeDanglingAndUnusedEntries(model, indexes(model, context));

    assertEquals(1, removed);
    assertTrue(model.getContent().getFieldConfiguration().getField().isEmpty());
  }

  @Test
  void removesUnreferencedButResolvableFieldAndGroupEntries() {
    FormModel model = load("FormUnusedConfigEntryValidator_invalid");
    ValidationContext context = TestModels.contextWithDocumentModels(model, refDm());

    int removed = FormConfigEntryCleanup.removeDanglingAndUnusedEntries(model, indexes(model, context));

    assertEquals(2, removed);
    assertTrue(model.getContent().getFieldConfiguration().getField().isEmpty());
    assertTrue(model.getContent().getGroupConfiguration().getGroup().isEmpty());
  }
}
