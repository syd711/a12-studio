package de.a12.studio.dataservices.services.support;

import com.mgmtp.a12.kernel.md.model.a12internal.DocumentModel;
import com.mgmtp.a12.kernel.md.model.a12internal.services.DocumentModelService;
import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;
import com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelResolver;

import java.util.List;

public class InMemoryIDocumentModelResolver implements IDocumentModelResolver {

  private final List<DocumentModel> documentModels;

  public InMemoryIDocumentModelResolver(List<DocumentModel> documentModels) {
    this.documentModels = documentModels;
  }

  @Override
  public IDocumentModel getDocumentModelById(String name) {
    InMemoryDocumentModelReferenceResolver resolver = new InMemoryDocumentModelReferenceResolver(documentModels);
    DocumentModel dm = resolver.getDocumentModel(name);
    return new DocumentModelService().convertToExternal(dm);
  }
}
