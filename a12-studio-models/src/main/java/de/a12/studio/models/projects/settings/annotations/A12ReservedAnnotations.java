package de.a12.studio.models.projects.settings.annotations;

import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Set;

/**
 * Annotation names that carry meaning for the A12 platform itself (kernel, SME, data services, CDM) rather than
 * being free-form project annotations. Used to tag such names with "Source: A12" in annotation suggestions.
 */
public final class A12ReservedAnnotations {

  private static final Set<String> NAMES = Set.of(
      "roles",
      "bindingConfiguration",
      "tdonly",
      "additive-document",
      "superTypes",
      "subTypes",
      "abstract",
      "application",
      "indexed",
      "enable_approximate_match_search",
      "lastMigratedVersion",
      "previousMigratedVersion");

  private static final List<String> PREFIXES = List.of("cdm.");

  private A12ReservedAnnotations() {
  }

  public static boolean isReserved(@Nullable String name) {
    if (name == null || name.isBlank()) {
      return false;
    }
    if (NAMES.contains(name)) {
      return true;
    }
    for (String prefix : PREFIXES) {
      if (name.startsWith(prefix)) {
        return true;
      }
    }
    return false;
  }
}
