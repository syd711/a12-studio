package de.a12.studio.modelsvalidation.formincludes;

import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.formmodel.FormModel;
import de.a12.studio.models.formmodel.FormModelWalker;
import de.a12.studio.models.formmodel.ScreenElement;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.TestModels;
import de.a12.studio.modelsvalidation.validators.form.FormIncludeProvenanceValidator;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FormIncludeProvenanceValidatorTest {

  private final DocumentModel hostDm = TestModels.load("/formincludes/A-for-host.json", DocumentModel.class);
  private final DocumentModel includedDm = TestModels.load("/formincludes/B-for-include.json", DocumentModel.class);
  private final FormModel includedForm = TestModels.load("/formincludes/IncludedModel.json", FormModel.class);
  private final FormModel host = TestModels.load("/formincludes/HostModel_expanded.json", FormModel.class);

  private List<ModelValidationError> validate(boolean withIncludedForm) {
    return new FormIncludeProvenanceValidator().validate(host, withIncludedForm
        ? TestModels.contextWithOtherModels(host, hostDm, includedDm, includedForm)
        : TestModels.contextWithOtherModels(host, hostDm, includedDm));
  }

  private ScreenElement includedElement() {
    return FormModelWalker.find(host.getContent(), ScreenElement.class).stream()
        .filter(element -> element.getIncludeId() != null).findFirst().orElseThrow();
  }

  @Test
  void anIntactIncludeIsValid() {
    assertEquals(List.of(), validate(true));
  }

  @Test
  void aFormWithoutIncludesIsNotChecked() {
    ScreenElement element = includedElement();
    element.setIncludeId(null);
    element.setFormModelRef(null);
    element.setHostDocumentModelPath(null);

    assertEquals(List.of(), validate(false));
  }

  @Test
  void anIncludedFormThatIsGoneIsReportedOnTheIncludedElementAndNamesIt() {
    List<ModelValidationError> errors = validate(false);

    assertEquals(1, errors.size());
    assertEquals(includedElement().getId(), errors.get(0).elementId());
    assertTrue(errors.get(0).message().contains("IncludedModel"), errors.get(0).message());
  }

  @Test
  void aDocumentModelPathThatNoLongerExistsIsReported() {
    includedElement().setHostDocumentModelPath("/Person/moved");

    List<ModelValidationError> errors = validate(true);

    assertEquals(1, errors.size());
    assertTrue(errors.get(0).message().contains("/Person/moved"), errors.get(0).message());
  }

  @Test
  void anIncompleteIncludeNamesWhatIsMissing() {
    includedElement().setFormModelRef(null);

    List<ModelValidationError> errors = validate(true);

    assertEquals(1, errors.size());
    assertTrue(errors.get(0).message().contains("formModelRef"), errors.get(0).message());
  }
}
