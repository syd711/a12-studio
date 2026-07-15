package de.a12.studio.dataservices.services.support;

import com.mgmtp.a12.kernel.md.facade.DocumentRtServiceFactory;
import com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelResolver;
import com.mgmtp.a12.kernel.md.rt.api.IDocumentDynamicServiceConfig;
import com.mgmtp.a12.kernel.md.rt.api.IDocumentRtService;
import com.mgmtp.a12.kernel.md.rt.api.ILabelProvider;
import com.mgmtp.a12.kernel.md.rt.api.IModelCode;
import com.mgmtp.a12.kernel.md.rt.api.IModelCodeCache;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class DocumentRtServices {

  private DocumentRtServices() {
  }

  public static IDocumentRtService getDocumentRTService(IDocumentModelResolver dmResolver) {
    return new DocumentRtServiceFactory(dmResolver).createDocumentRtService(new A12DocumentDynamicServiceConfig());
  }

  public static final class A12DocumentDynamicServiceConfig implements IDocumentDynamicServiceConfig {
    @Override
    public Optional<String> getVariant() {
      return Optional.empty(); // not supported in A12
    }

    @Override
    public IModelCodeCache getCache() {
      return new InMemoryModelCodeCache();
    }

    @Override
    public Optional<ILabelProvider> getLabelProvider() {
      return Optional.empty();
    }
  }

  public static final class InMemoryModelCodeCache implements IModelCodeCache {
    private final ConcurrentMap<String, IModelCode> genCodeCache = new ConcurrentHashMap<>();

    @Override
    public IModelCode getModelCode(String modelCodeId) {
      return genCodeCache.get(modelCodeId);
    }

    @Override
    public void addModelCode(String modelCodeId, IModelCode modelCode) {
      genCodeCache.put(modelCodeId, modelCode);
    }
  }
}
