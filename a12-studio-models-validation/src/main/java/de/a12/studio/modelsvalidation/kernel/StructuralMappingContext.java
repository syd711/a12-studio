package de.a12.studio.modelsvalidation.kernel;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;

import de.a12.studio.kernel.KernelException;
import de.a12.studio.kernel.KernelFinding;
import de.a12.studio.kernel.KernelModelSource;
import de.a12.studio.kernel.KernelStructuralMapping;
import de.a12.studio.models.A12Model;
import de.a12.studio.models.ModelReference;
import de.a12.studio.models.mappingmodel.MappingModel;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.models.structuralmappingmodel.MappingBlock;
import de.a12.studio.models.structuralmappingmodel.ResolutionStrategyType;
import de.a12.studio.models.structuralmappingmodel.SmmElement;
import de.a12.studio.models.structuralmappingmodel.SmmPointers;
import de.a12.studio.models.structuralmappingmodel.StructuralMappingModel;
import de.a12.studio.models.structuralmappingmodel.StructuralMappingModelContent;
import de.a12.studio.models.util.JsonSettings;
import de.a12.studio.modelsvalidation.Severity;

/**
 * What a Structural Mapping Model is edited and checked against, and the kernel services that work on it. SME
 * only ever opens a Structural Mapping Model from its Mapping Model: the groups and fields its mappings point
 * into are not the project Document Models but the <em>source</em> (the Mapping Model's Source models joined,
 * plus its precomputation fragment) and the <em>target</em> the kernel derives from the Mapping Model. This
 * class finds the Mapping Model that uses an SMM, has the kernel compute those two models and then offers the
 * operations of SME's structural mapping backend ({@code StructuralMappingModelController},
 * {@code FieldMappingController}, {@code ResolutionStrategyController}) in a12-studio terms.
 *
 * <p>Kernel access goes through {@link KernelStructuralMapping} only; no kernel type is visible here.
 */
public final class StructuralMappingContext {

  /** Why there is no context for an SMM (no Mapping Model uses it, the kernel cannot combine its models, ...). */
  public static final class Unavailable extends Exception {

    public Unavailable(String message) {
      super(message);
    }

    public Unavailable(String message, Throwable cause) {
      super(message, cause);
    }
  }

  /**
   * A problem the kernel found in a Structural Mapping Model.
   *
   * @param pointer  the element it is about, in {@link SmmPointers} form ({@code /content/MappingBlocks[1]/FieldMappings[2]});
   *                 {@code /content} if it is about the model as a whole
   * @param severity how serious it is
   * @param message  the kernel's message
   */
  public record Finding(String pointer, Severity severity, String message) {
  }

  /**
   * One way to move a field mapping.
   *
   * @param mappingBlock 0-based index of the mapping block it would end up in; one past the last block means a new block
   * @param modified     the resulting content (still containing the original mapping, which the caller removes)
   */
  public record MoveOption(int mappingBlock, StructuralMappingModelContent modified) {
  }

  private static final Logger log = LoggerFactory.getLogger(StructuralMappingContext.class);

  private static final KernelStructuralMapping KERNEL = new KernelStructuralMapping();

  private record Cached(String fingerprint, StructuralMappingContext context) {
  }

  /** Computing the source and target model joins and expands the Document Models, so it is done once per state of them. */
  private static final Map<MappingModel, Cached> cache = Collections.synchronizedMap(new WeakHashMap<>());

  private final MappingModel mappingModel;
  private final KernelStructuralMapping.Context context;
  private final List<SmmElement> sourceRoots;
  private final List<SmmElement> targetRoots;

  private StructuralMappingContext(MappingModel mappingModel, KernelStructuralMapping.Context context, List<SmmElement> sourceRoots,
      List<SmmElement> targetRoots) {
    this.mappingModel = mappingModel;
    this.context = context;
    this.sourceRoots = sourceRoots;
    this.targetRoots = targetRoots;
  }

  /** The Mapping Models of {@code models} that use {@code smm} as their Structural Mapping Model. */
  public static List<MappingModel> findOwners(StructuralMappingModel smm, Collection<? extends A12Model<?>> models) {
    List<MappingModel> owners = new ArrayList<>();
    if (smm.getId() == null) {
      return owners;
    }
    for (A12Model<?> model : models) {
      if (model instanceof MappingModel mapping && mapping.getContent() != null
          && mapping.getContent().getStructuralMappingModel() != null
          && smm.getId().equals(mapping.getContent().getStructuralMappingModel().getId())) {
        owners.add(mapping);
      }
    }
    return owners;
  }

  /**
   * Resolves the context of {@code smm}.
   *
   * @param contextItem any item of the project; the kernel reads the Mapping Model and the Document Models from the
   *                    in-memory project tree, so unsaved edits of open tabs count
   * @param owner       the Mapping Model that uses {@code smm}
   * @throws Unavailable if the Mapping Model is incomplete or the kernel cannot combine its Document Models
   */
  public static StructuralMappingContext resolve(ProjectItem contextItem, MappingModel owner) throws Unavailable {
    String fingerprint = fingerprint(owner, contextItem);
    Cached cached = fingerprint == null ? null : cache.get(owner);
    if (cached != null && cached.fingerprint().equals(fingerprint)) {
      return cached.context();
    }
    StructuralMappingContext context = computeContext(contextItem, owner);
    if (fingerprint != null) {
      cache.put(owner, new Cached(fingerprint, context));
    }
    return context;
  }

  /**
   * Everything the context is computed from: the Mapping Model and the models it references, transitively (its
   * Document Models, what they include). Two equal fingerprints mean the same source and target model.
   *
   * @return {@code null} if the models cannot be serialized
   */
  public static String fingerprint(MappingModel owner, ProjectItem contextItem) {
    try {
      StringBuilder key = new StringBuilder();
      Set<A12Model<?>> visited = new HashSet<>();
      Deque<A12Model<?>> pending = new ArrayDeque<>();
      pending.add(owner);
      while (!pending.isEmpty()) {
        A12Model<?> next = pending.poll();
        if (!visited.add(next)) {
          continue;
        }
        key.append('\n').append(JsonSettings.objectMapper.writeValueAsString(next));
        for (ModelReference reference : next.getModelReferences()) {
          ProjectItem referenced = reference.getReference() == null ? null : contextItem.findByModelId(reference.getReference());
          // The SMM itself is no input: its mappings are what is checked, not what the source and target are.
          if (referenced != null && referenced.getModel() != null && !(referenced.getModel() instanceof StructuralMappingModel)) {
            pending.add(referenced.getModel());
          }
        }
      }
      return key.toString();
    }
    catch (Exception e) {
      return null;
    }
  }

  private static StructuralMappingContext computeContext(ProjectItem contextItem, MappingModel owner) throws Unavailable {
    if (owner.getContent() == null || owner.getContent().getTarget() == null || owner.getContent().getTarget().getDmId() == null) {
      throw new Unavailable("The Mapping Model " + owner.getId() + " has no Target Model.");
    }
    if (owner.getContent().getSource().isEmpty()) {
      throw new Unavailable("The Mapping Model " + owner.getId() + " has no Source Model.");
    }
    try {
      String preComputationId = owner.getContent().getPreComputationFragment() != null
          ? owner.getContent().getPreComputationFragment().getDmId() : null;
      KernelModelSource source = withoutAdditiveAnnotation(ProjectKernelModels.sourceOf(contextItem), preComputationId);
      String mappingJson = JsonSettings.objectMapper.writeValueAsString(owner);
      KernelStructuralMapping.Context kernelContext = KERNEL.computeContext(mappingJson, source);
      return new StructuralMappingContext(owner, kernelContext, SmmElement.parseRoots(kernelContext.sourceDocumentModelJson()),
          SmmElement.parseRoots(kernelContext.targetDocumentModelJson()));
    }
    catch (KernelException e) {
      log.debug("Kernel could not compute the context of {}: {}", owner.getId(), e.getMessage());
      throw new Unavailable("The Mapping Model " + owner.getId() + " contains errors that prevent opening the Structural Mapping Model: "
          + e.getMessage(), e);
    }
    catch (Exception e) {
      throw new Unavailable("The source and target model of " + owner.getId() + " cannot be computed: " + e.getMessage(), e);
    }
  }

  /**
   * The kernel refuses a precomputation fragment that still carries the {@code additive-document} annotation (SME
   * strips it for the same reason), so the fragment is handed over without it.
   */
  private static KernelModelSource withoutAdditiveAnnotation(KernelModelSource source, @Nullable String preComputationId) {
    if (preComputationId == null) {
      return source;
    }
    return id -> {
      String json = source.load(id);
      if (!id.equals(preComputationId)) {
        return json;
      }
      try {
        JsonNode root = JsonSettings.objectMapper.readTree(json);
        if (root.path("header").path("annotations") instanceof ArrayNode annotations) {
          for (int i = annotations.size() - 1; i >= 0; i--) {
            if ("additive-document".equals(annotations.get(i).path("name").asString())) {
              annotations.remove(i);
            }
          }
        }
        return JsonSettings.objectMapper.writeValueAsString(root);
      }
      catch (Exception e) {
        throw new java.io.UncheckedIOException(new java.io.IOException(e));
      }
    };
  }

  /** The Mapping Model this context was computed from. */
  public MappingModel mappingModel() {
    return mappingModel;
  }

  /** The root groups of the joined source model: the Source Models under their names, plus the precomputation fragment. */
  public List<SmmElement> sourceRoots() {
    return sourceRoots;
  }

  /** The root groups of the target model. */
  public List<SmmElement> targetRoots() {
    return targetRoots;
  }

  // ---- checks ---------------------------------------------------------------------------------

  /**
   * The full consistency check of {@code smm} against the source and target model: everything
   * {@link #checkStandalone} finds, plus unknown or mistyped entities, crossing resolution strategies, field
   * mappings without a strategy for a repeating group, ...
   *
   * @throws Unavailable if the kernel cannot read the model
   */
  public List<Finding> check(StructuralMappingModel smm) throws Unavailable {
    return check(snapshot(smm));
  }

  /**
   * The model as text, to be checked later by {@link #check(String)} - on another thread, say, while the model is
   * edited further.
   */
  public static String snapshot(StructuralMappingModel smm) throws Unavailable {
    return json(smm);
  }

  /** {@link #check(StructuralMappingModel)} of a model that was turned into text with {@link #snapshot}. */
  public List<Finding> check(String snapshot) throws Unavailable {
    try {
      return findings(KERNEL.checkFull(snapshot, context));
    }
    catch (KernelException e) {
      throw new Unavailable(e.getMessage(), e);
    }
  }

  /** The structural check that needs no source and target model: required values, name patterns, duplicates. */
  public static List<Finding> checkStandalone(StructuralMappingModel smm) throws Unavailable {
    try {
      return findings(KERNEL.checkStandalone(json(smm)));
    }
    catch (KernelException e) {
      throw new Unavailable(e.getMessage(), e);
    }
  }

  /**
   * Findings about a place in the model: the kernel also reports "the model has no content" without a pointer,
   * which SME ignores (an empty model is what a new one is). Of two duplicates the kernel reports both, SME only
   * the second.
   */
  private static List<Finding> findings(List<KernelFinding> kernelFindings) {
    List<Finding> findings = new ArrayList<>();
    Set<String> reportedDuplicates = new HashSet<>();
    for (KernelFinding finding : kernelFindings) {
      if (finding.elementPath() == null) {
        continue;
      }
      boolean duplicate = finding.message().endsWith("[RESOLUTION_STRATEGY_DUPLICATE]") || finding.message().endsWith("[FIELD_MAPPING_DUPLICATE]");
      if (duplicate && reportedDuplicates.add(finding.message())) {
        continue;
      }
      Severity severity = switch (finding.severity()) {
        case ERROR -> Severity.ERROR;
        case WARNING -> Severity.WARNING;
        case INFO -> Severity.INFO;
      };
      findings.add(new Finding(SmmPointers.normalize(finding.elementPath()), severity, finding.message()));
    }
    return findings;
  }

  // ---- editing --------------------------------------------------------------------------------

  /**
   * Adds a field mapping, with the resolution strategies the kernel finds it needs.
   *
   * @return the content of the model with the field mapping added
   * @throws Unavailable if the kernel cannot add it
   */
  public StructuralMappingModelContent addFieldMapping(StructuralMappingModel smm, String sourceField, String targetField) throws Unavailable {
    try {
      return contentOf(KERNEL.addFieldMapping(json(smm), context, sourceField, targetField));
    }
    catch (KernelException e) {
      throw new Unavailable(e.getMessage(), e);
    }
  }

  /** The mapping blocks a field mapping could be moved to, each with the model that results; the block it is in now is not among them. */
  public List<MoveOption> moveOptions(StructuralMappingModel smm, String sourceField, String targetField) throws Unavailable {
    try {
      List<MoveOption> options = new ArrayList<>();
      for (KernelStructuralMapping.AddOption option : KERNEL.addOptions(json(smm), context, sourceField, targetField)) {
        options.add(new MoveOption(option.mappingBlockIndex() - 1, contentOf(option.modifiedModelJson())));
      }
      return options;
    }
    catch (KernelException e) {
      throw new Unavailable(e.getMessage(), e);
    }
  }

  /** The full names of the groups that can be the source group of the resolution strategy at {@code pointer}. */
  public Set<String> validSourceGroups(StructuralMappingModel smm, String pointer) throws Unavailable {
    try {
      return KERNEL.validSourceGroups(json(smm), context, pointer);
    }
    catch (KernelException e) {
      throw new Unavailable(e.getMessage(), e);
    }
  }

  /** The full names of the fields that can be the slice source field of the resolution strategy at {@code pointer}. */
  public Set<String> validSliceSourceFields(StructuralMappingModel smm, String pointer) throws Unavailable {
    try {
      return KERNEL.validSliceSourceFields(json(smm), context, pointer);
    }
    catch (KernelException e) {
      throw new Unavailable(e.getMessage(), e);
    }
  }

  /** The full names of the fields that can be the slice target field of the resolution strategy at {@code pointer}. */
  public Set<String> validSliceTargetFields(StructuralMappingModel smm, String pointer) throws Unavailable {
    try {
      return KERNEL.validSliceTargetFields(json(smm), context, pointer);
    }
    catch (KernelException e) {
      throw new Unavailable(e.getMessage(), e);
    }
  }

  /** The types the resolution strategy at {@code pointer} can have. */
  public Set<ResolutionStrategyType> validTypes(StructuralMappingModel smm, String pointer) throws Unavailable {
    try {
      Set<ResolutionStrategyType> types = new java.util.LinkedHashSet<>();
      for (String type : KERNEL.validResolutionStrategyTypes(json(smm), context, pointer)) {
        types.add(ResolutionStrategyType.fromValue(type));
      }
      return types;
    }
    catch (KernelException e) {
      throw new Unavailable(e.getMessage(), e);
    }
  }

  private static String json(StructuralMappingModel smm) throws Unavailable {
    try {
      return JsonSettings.objectMapper.writeValueAsString(smm);
    }
    catch (Exception e) {
      throw new Unavailable("The structural mapping model cannot be serialized: " + e.getMessage(), e);
    }
  }

  private static StructuralMappingModelContent contentOf(String smmJson) throws Unavailable {
    try {
      return JsonSettings.objectMapper.readValue(smmJson, StructuralMappingModel.class).getContent();
    }
    catch (Exception e) {
      throw new Unavailable("The kernel returned a structural mapping model that cannot be read: " + e.getMessage(), e);
    }
  }

  /** Copies the mapping blocks of {@code from} into {@code smm} (the kernel returns whole models, but only the blocks change). */
  public static void takeMappingBlocks(StructuralMappingModel smm, StructuralMappingModelContent from) {
    List<MappingBlock> blocks = new ArrayList<>(from.getMappingBlocks());
    smm.getContent().getMappingBlocks().clear();
    smm.getContent().getMappingBlocks().addAll(blocks);
  }
}
