package de.a12.studio.modelsvalidation.kernel;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.mappingmodel.MappingModel;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.models.structuralmappingmodel.StructuralMappingModel;
import de.a12.studio.models.util.JsonSettings;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.ValidationContext;
import de.a12.studio.modelsvalidation.validators.ModelValidator;

/**
 * The kernel's verdict on a Structural Mapping Model, the whole of SME's validation of this model type (SME's
 * backend is a thin wrapper around the same kernel check): required values, name patterns and duplicates, plus -
 * as soon as the Mapping Model that uses the SMM is known and the kernel can compute its source and target model -
 * unknown or mistyped entities, crossing resolution strategies, mappings that no resolution strategy covers, and
 * so on. Without that context only the structural half is checked (SME does the same for the model overview).
 *
 * <p>Findings are reported against the element's document pointer ({@code /content/MappingBlocks[1]/FieldMappings[2]},
 * see {@link de.a12.studio.models.structuralmappingmodel.SmmPointers}), which is how the editor finds the tag to
 * mark. The kernel verdict is cached per model: the key is the serialized model plus the serialized Mapping Model
 * and Document Models it is computed from, so it is recomputed only after an edit that can change it.
 */
public final class StructuralMappingKernelValidator implements ModelValidator {

  private static final Logger log = LoggerFactory.getLogger(StructuralMappingKernelValidator.class);

  private record Cached(String fingerprint, List<ModelValidationError> errors) {
  }

  private static final Map<A12Model<?>, Cached> cache = Collections.synchronizedMap(new WeakHashMap<>());

  @Override
  public List<ModelValidationError> validate(A12Model<?> model, ValidationContext context) {
    if (!(model instanceof StructuralMappingModel smm) || smm.getContent() == null) {
      return List.of();
    }
    List<MappingModel> owners = StructuralMappingContext.findOwners(smm, context.otherModels());
    MappingModel owner = owners.isEmpty() ? null : owners.get(0);
    ProjectItem item = context.projectItem();
    String fingerprint = fingerprint(smm, owner, item);
    if (fingerprint == null) {
      return List.of();
    }
    Cached cached = cache.get(model);
    if (cached == null || !cached.fingerprint().equals(fingerprint)) {
      cached = new Cached(fingerprint, compute(smm, owner, item));
      cache.put(model, cached);
    }
    return cached.errors();
  }

  private static List<ModelValidationError> compute(StructuralMappingModel smm, MappingModel owner, ProjectItem item) {
    try {
      List<StructuralMappingContext.Finding> findings = null;
      if (owner != null && item != null) {
        try {
          findings = StructuralMappingContext.resolve(item, owner).check(smm);
        }
        catch (StructuralMappingContext.Unavailable e) {
          log.debug("No full check of {}: {}", smm.getId(), e.getMessage());
        }
      }
      if (findings == null) {
        findings = StructuralMappingContext.checkStandalone(smm);
      }
      return findings.stream()
          .map(finding -> new ModelValidationError(smm, finding.pointer(), finding.message(), finding.severity().name()))
          .toList();
    }
    catch (StructuralMappingContext.Unavailable e) {
      log.debug("Kernel could not check {}: {}", smm.getId(), e.getMessage());
      return List.of();
    }
    catch (RuntimeException e) {
      log.warn("Kernel check of {} failed unexpectedly: {}", smm.getId(), e.getMessage());
      return List.of();
    }
  }

  /** The model and everything the kernel reads for it: its Mapping Model and the models that reference, transitively. */
  private static String fingerprint(StructuralMappingModel smm, MappingModel owner, ProjectItem item) {
    try {
      String context = owner != null && item != null ? StructuralMappingContext.fingerprint(owner, item) : "";
      return context == null ? null : JsonSettings.objectMapper.writeValueAsString(smm) + context;
    }
    catch (Exception e) {
      return null;
    }
  }
}
