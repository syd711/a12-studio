package de.a12.studio.kernel;

import java.util.List;

/**
 * @param expandedJson the Document Model as JSON with all includes and type definitions resolved
 * @param findings     what the kernel reported while expanding (unresolvable references, include conflicts, ...)
 */
public record KernelExpansion(String expandedJson, List<KernelFinding> findings) {

  public boolean hasErrors() {
    return findings.stream().anyMatch(KernelFinding::isError);
  }
}
