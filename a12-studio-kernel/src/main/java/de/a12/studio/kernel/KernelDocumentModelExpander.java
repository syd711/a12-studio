package de.a12.studio.kernel;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.mgmtp.a12.kernel.md.combination.a12internal.CombinationException;
import com.mgmtp.a12.kernel.md.combination.a12internal.ExpandingDmResolver;
import com.mgmtp.a12.kernel.md.combination.a12internal.UnexpandedModelResolverImpl;
import com.mgmtp.a12.kernel.md.facade.DocumentModelServiceFactory;
import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;
import com.mgmtp.a12.model.notification.RankedNotification;

/**
 * Facade over the kernel's Document Model expansion: resolves {@code includeConfig} groups and type definitions
 * from other models and returns the flattened model. This uses the kernel's {@code a12internal} resolver classes
 * (no public equivalent exists), which is why it lives only here.
 */
public final class KernelDocumentModelExpander {

  private final DocumentModelServiceFactory factory = new DocumentModelServiceFactory();

  /**
   * @param modelId id of the model to expand (as {@link KernelModelSource} understands it)
   * @param source  lookup of model JSON by id, used for the model itself and everything it references
   * @throws KernelException if the model or one it references cannot be loaded or is not a Document Model
   */
  public KernelExpansion expand(String modelId, KernelModelSource source) {
    List<RankedNotification> notifications = new ArrayList<>();
    try {
      ExpandingDmResolver resolver = ExpandingDmResolver.of(
          new UnexpandedModelResolverImpl(id -> new StringReader(source.load(id))), Locale.US, notifications::add /* the kernel only supports en_US and de_DE */);
      IDocumentModel expanded = resolver.getDocumentModelById(modelId);

      StringWriter json = new StringWriter();
      factory.createDocumentModelSerializer().serialize(expanded, json, notifications::add);
      return new KernelExpansion(json.toString(), notifications.stream().map(KernelFindings::of).toList());
    } catch (CombinationException | IOException | UncheckedIOException e) {
      throw new KernelException("Cannot expand " + modelId + ": " + e.getMessage(), e);
    }
  }
}
