package de.a12.studio.modelsvalidation.kernel;

import de.a12.studio.kernel.KernelDocumentModelChecker;
import de.a12.studio.kernel.KernelFinding;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.util.JsonSettings;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * The kernel's consistency check of the Document Model a Transformer Model generated (SME's {@code isDocumentModelValid}
 * after every transformation). A generated model has no includes, so it can be handed to the kernel as it is.
 */
@Slf4j
public final class GeneratedDocumentModelKernelCheck {

  /** One finding, without any kernel type: {@code error} tells an error from a warning. */
  public record Finding(boolean error, String message, String elementPath) {
  }

  private GeneratedDocumentModelKernelCheck() {
  }

  /**
   * Every finding of the kernel for {@code generated}; empty if it is consistent <em>or</em> the kernel could not be
   * run (that is logged, not reported: the model is not at fault).
   */
  public static List<Finding> check(DocumentModel generated) {
    try {
      return new KernelDocumentModelChecker().check(JsonSettings.objectMapper.writeValueAsString(generated)).stream()
          .filter(finding -> finding.severity() != KernelFinding.Severity.INFO)
          .map(finding -> new Finding(finding.isError(), finding.message(), finding.elementPath()))
          .toList();
    }
    catch (Exception | LinkageError e) {
      log.warn("The kernel check of the generated Document Model '{}' failed: {}", generated.getId(), e.getMessage(), e);
      return List.of();
    }
  }
}
