package de.a12.studio.kernel;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import com.mgmtp.a12.kernel.md.facade.DocumentModelServiceFactory;
import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;
import com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelService;

/**
 * Facade over the kernel's whole-model consistency check ({@code IDocumentModelService.checkConsistency}, the
 * public kernel API). It reports corrupted rule conditions and computations with line/column, unexpanded includes
 * and invalid entity paths.
 *
 * <p>The kernel refuses to check a model that still has unexpanded includes and reports that as an error, so
 * callers that want semantic validation of a model with includes must expand it first (a later facade slice).
 */
public final class KernelDocumentModelChecker {

  /** The only kernel version a12-studio is built and tested against. */
  public static final String KERNEL_VERSION = "31.1.1";

  private final DocumentModelServiceFactory factory = new DocumentModelServiceFactory();

  /**
   * @param documentModelJson the content of a Document Model {@code .json} file (header + content)
   * @return all findings, errors first in kernel order; empty if the model is consistent
   * @throws IOException if the kernel cannot read the JSON as a Document Model at all
   */
  public List<KernelFinding> check(String documentModelJson) throws IOException {
    IDocumentModel model = factory.createDocumentModelSerializer().deserialize(new StringReader(documentModelJson));
    IDocumentModelService service = factory.createDocumentModelService();
    List<KernelFinding> findings = new ArrayList<>();
    service.checkConsistency(model, notification -> findings.add(KernelFindings.of(service, notification)));
    return findings;
  }
}
