package de.a12.studio.dataservices.services.support;

import com.mgmtp.a12.kernel.md.model.a12internal.DocumentModel;
import com.mgmtp.a12.kernel.md.model.a12internal.services.DocumentModelReferenceResolver;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class InMemoryDocumentModelReferenceResolver implements DocumentModelReferenceResolver {

  private final Map<String, DocumentModel> map;

  public InMemoryDocumentModelReferenceResolver(List<DocumentModel> documentModels) {
    this.map = documentModels.stream().collect(Collectors.toMap(dm -> dm.getHeader().getId(), Function.identity()));
  }

  @Override
  public DocumentModel getDocumentModel(String reference) {
    if (reference == null) {
      return null;
    }
    String id = reference;
    int lastSlash = id.lastIndexOf('/');
    if (lastSlash >= 0) {
      id = id.substring(lastSlash + 1);
    }
    int jsonSuffix = id.lastIndexOf(".json");
    if (jsonSuffix >= 0) {
      id = id.substring(0, jsonSuffix);
    }
    DocumentModel dm = map.get(id);
    return dm != null ? DocumentModelSupport.deserialize(DocumentModelSupport.serialize(dm)) : null;
  }
}
