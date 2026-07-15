package de.a12.studio.dataservices.services.documentmodel.features.adhoctest;

import com.mgmtp.a12.kernel.md.model.a12internal.DocumentModel;
import com.mgmtp.a12.kernel.md.model.a12internal.services.DocumentModelService;
import com.mgmtp.a12.model.notification.RankedNotification;
import de.a12.studio.dataservices.services.support.DocumentModelSupport;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class AdHocTestService {

  public AdHocTestResultDto generatePreviewInput(
      DocumentModel documentModel, Set<String> selectedElements, Set<String> partiallySelectedElements) {
    DocumentModel reducedModel = createReducedModel(documentModel, selectedElements, partiallySelectedElements);
    String documentModelString = DocumentModelSupport.serialize(reducedModel);
    String validationCode = DocumentModelSupport.generateValidationCode(reducedModel);

    return new AdHocTestResultDto(documentModelString, validationCode);
  }

  public DocumentModel createReducedModel(
      DocumentModel documentModel, Set<String> selectedElements, Set<String> partiallySelectedElements) {
    List<RankedNotification> notifications = new ArrayList<>();
    var reducedModel =
        new DocumentModelService()
            .createReducedDocumentModel(documentModel, selectedElements, partiallySelectedElements, notifications::add);

    if (reducedModel.isEmpty()) {
      throw new IllegalStateException("Failed to create reduced document model");
    }
    if (!notifications.isEmpty()) {
      throw new IllegalStateException("Unexpected notifications while creating reduced document model: " + notifications);
    }

    return reducedModel.get();
  }
}
