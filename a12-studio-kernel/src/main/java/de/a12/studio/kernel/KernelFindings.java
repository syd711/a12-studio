package de.a12.studio.kernel;

import com.mgmtp.a12.kernel.md.model.api.IElement;
import com.mgmtp.a12.kernel.md.model.api.services.IDocumentModelService;
import com.mgmtp.a12.model.notification.RankedNotification;

/** Translation of kernel notifications to {@link KernelFinding}, shared by the facade classes. */
final class KernelFindings {

  private KernelFindings() {
  }

  static KernelFinding of(RankedNotification notification) {
    return of(null, notification);
  }

  /** @param service used to turn the notification's source element into a path; may be {@code null} */
  static KernelFinding of(IDocumentModelService service, RankedNotification notification) {
    KernelFinding.Severity severity = switch (notification.getSeverity()) {
      case ERROR -> KernelFinding.Severity.ERROR;
      case WARNING -> KernelFinding.Severity.WARNING;
      default -> KernelFinding.Severity.INFO;
    };
    String path = service != null && notification.getSource() instanceof IElement element ? service.getPath(element) : null;
    return new KernelFinding(severity, notification.getMessage(), path);
  }
}
