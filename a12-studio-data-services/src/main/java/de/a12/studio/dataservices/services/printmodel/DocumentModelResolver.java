package de.a12.studio.dataservices.services.printmodel;

import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;
import com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelResolver;
import com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelSerializer;
import com.mgmtp.a12.kernel.md.serializer.MDSerializerFactory;

import java.io.StringReader;
import java.util.Map;

public class DocumentModelResolver implements IDocumentModelResolver {

  private static final IDocumentModelSerializer documentModelSerializer = new MDSerializerFactory().createDocumentModelSerializer();

  private final Map<String, String> documentModelMap;

  public DocumentModelResolver(Map<String, String> documentModelMap) {
    this.documentModelMap = documentModelMap;
  }

  @Override
  public IDocumentModel getDocumentModelById(String id) {
    if (documentModelMap.containsKey(id)) {
      try {
        return documentModelSerializer.deserialize(new StringReader(documentModelMap.get(id)));
      } catch (java.io.IOException e) {
        throw new RuntimeException(e);
      }
    }
    throw new RuntimeException("No model for " + id);
  }
}
