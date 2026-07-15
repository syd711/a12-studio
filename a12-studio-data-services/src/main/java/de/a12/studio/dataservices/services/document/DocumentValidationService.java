package de.a12.studio.dataservices.services.document;

import com.fasterxml.jackson.databind.JsonNode;
import com.mgmtp.a12.kernel.md.document.api.services.DocumentDeserializationConfig;
import com.mgmtp.a12.kernel.md.document.apiV2.immutable.DocumentV2;
import com.mgmtp.a12.kernel.md.document.apiV2.services.IDocumentV2Serializer;
import com.mgmtp.a12.kernel.md.facade.DocumentServiceFactory;
import com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelResolver;
import com.mgmtp.a12.kernel.md.rt.api.IDocumentValidationResult;
import com.mgmtp.a12.model.notification.RankedNotification;
import de.a12.studio.dataservices.services.support.DocumentModelSupport;
import de.a12.studio.dataservices.services.support.DocumentRtServices;
import de.a12.studio.dataservices.services.support.InMemoryIDocumentModelResolver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DocumentValidationService {

  public Map<String, Boolean> validateDocuments(List<DocumentAndID> documents, List<JsonNode> documentModelContents) {
    Map<String, Boolean> result = new HashMap<>();
    IDocumentModelResolver dmResolver = getDMResolver(documentModelContents);
    var docRtService = DocumentRtServices.getDocumentRTService(dmResolver);
    for (DocumentAndID document : documents) {
      try {
        List<RankedNotification> problems = new ArrayList<>();
        DocumentV2 deserializedDoc =
            deserializeDocument(dmResolver, document.getDocument(), document.getDocumentModelName(), problems);
        var updatedDocument = docRtService.compute(deserializedDoc, getLocale(document, dmResolver)).applyTo(deserializedDoc);
        IDocumentValidationResult docValResult = docRtService.validateFull(updatedDocument, getLocale(document, dmResolver));
        result.put(document.getId(), docValResult.noErrorOccurred() && problems.isEmpty());
      } catch (Exception e) {
        result.put(document.getId(), false);
      }
    }
    return result;
  }

  public DocumentV2 deserializeDocument(
      IDocumentModelResolver dmResolver, JsonNode document, String documentModelName, List<RankedNotification> problems) {
    IDocumentV2Serializer serializer = new DocumentServiceFactory(dmResolver).createDocumentV2Serializer();

    DocumentDeserializationConfig docDeserializeConfig = DocumentDeserializationConfig.builder().build();

    return serializer.deserializeV2(document, documentModelName, docDeserializeConfig, problems::add);
  }

  public IDocumentModelResolver getDMResolver(List<JsonNode> documentModelContents) {
    var documentModels = documentModelContents.stream().map(DocumentModelSupport::deserialize).toList();
    return new InMemoryIDocumentModelResolver(documentModels);
  }

  private Locale getLocale(IDocument document, IDocumentModelResolver dmResolver) {
    if (document.getLocale() != null) {
      return Locale.of(document.getLocale());
    }
    return dmResolver.getDocumentModelById(document.getDocumentModelName()).getHeader().getLocales().get(0);
  }
}
