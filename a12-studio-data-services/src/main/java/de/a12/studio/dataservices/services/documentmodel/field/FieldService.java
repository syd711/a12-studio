package de.a12.studio.dataservices.services.documentmodel.field;

import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IField;
import com.mgmtp.a12.kernel.md.facade.a12internal.KernelUtils;
import com.mgmtp.a12.kernel.md.model.a12internal.DocumentModel;
import com.mgmtp.a12.kernel.md.model.a12internal.Field;
import com.mgmtp.a12.kernel.md.model.a12internal.services.DocumentModelService;
import de.a12.studio.dataservices.services.support.DocumentModelSupport;
import de.a12.studio.dataservices.services.support.ProblemReporter;

import java.util.List;

public class FieldService {

  public List<String> validateErrorMessage(DocumentModel documentModel, String fieldId, int index) {
    Field field = DocumentModelSupport.getFieldById(documentModel, fieldId);
    var mvkService = new DocumentModelService().getMvkServiceForModel(documentModel, null);
    ProblemReporter pr = new ProblemReporter();
    String fieldPath = new DocumentModelService().getPath(field);
    var iField = mvkService.getIEC().get(fieldPath);
    if (!iField.isField()) {
      throw new IllegalStateException("Not a field: " + fieldPath);
    }
    var locale = documentModel.getHeader().getLocales().get(index);

    mvkService.hasValidErrorText((IField) iField, List.of(locale), pr);
    return pr.getProblems().stream().map(p -> p.getMessage()).toList();
  }

  public List<String> validateCustomRequirednessErrorMessage(DocumentModel documentModel, String fieldId, int index) {
    Field field = DocumentModelSupport.getFieldById(documentModel, fieldId);
    var mvkService = new DocumentModelService().getMvkServiceForModel(documentModel, null);
    ProblemReporter pr = new ProblemReporter();
    String fieldPath = new DocumentModelService().getPath(field);
    var iField = mvkService.getIEC().get(fieldPath);
    if (!iField.isField()) {
      throw new IllegalStateException("Not a field: " + fieldPath);
    }
    var locale = documentModel.getHeader().getLocales().get(index);

    mvkService.hasValidErrorTextInRequirednessConfig((IField) iField, List.of(locale), pr);
    return pr.getProblems().stream().map(p -> p.getMessage()).toList();
  }

  public boolean validatePattern(String pattern) {
    return new DocumentModelService().checkPattern(pattern);
  }

  public List<Boolean> validateEnumValue(List<String> values, List<String> supportedCharacters) {
    return values.stream().map(v -> KernelUtils.isValidValue(v, supportedCharacters)).toList();
  }
}
