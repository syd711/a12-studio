package de.a12.studio.dataservices.services.printmodel;

import com.mgmtp.a12.kernel.md.document.api.IDocument;
import com.mgmtp.a12.kernel.md.document.api.services.DocumentDeserializationConfig;
import com.mgmtp.a12.kernel.md.document.api.services.DocumentSerializationConfig;
import com.mgmtp.a12.kernel.md.document.api.services.IDocumentSerializer;
import com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelResolver;
import com.mgmtp.a12.kernel.md.serializer.document.internal.service.DocumentSerializerImpl;
import com.mgmtp.a12.model.notification.Severity;
import com.mgmtp.a12.print.engine.api.exception.impl.DocumentLoadingException;

import java.io.StringReader;

public class DocumentDeserializer {

  private final DocumentDeserializationConfig deserializationConfig =
      DocumentDeserializationConfig.builder().format(DocumentSerializationConfig.Format.JSON).addTransientFields(true).build();

  public IDocument provide(String documentModelId, String jsonDocument, IDocumentModelResolver documentModelResolver) {
    IDocumentSerializer documentSerializer = new DocumentSerializerImpl(documentModelResolver);
    return convert(documentSerializer, documentModelId, jsonDocument);
  }

  private IDocument convert(IDocumentSerializer documentSerializer, String documentModelId, String jsonDocument) {
    IDocument document;
    try (StringReader reader = new StringReader(jsonDocument)) {
      document =
          documentSerializer.deserialize(reader, documentModelId, deserializationConfig, rankedNotification -> {
            if (rankedNotification.getSeverity() == Severity.ERROR) {
              throw new DocumentLoadingException("Error on parsing document: {}", rankedNotification);
            }
          });
    } catch (Exception exception) {
      throw new DocumentLoadingException("Error on parsing document.", exception);
    }
    return document;
  }
}
