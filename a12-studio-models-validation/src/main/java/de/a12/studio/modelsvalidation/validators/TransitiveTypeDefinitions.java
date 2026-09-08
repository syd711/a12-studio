package de.a12.studio.modelsvalidation.validators;

import de.a12.studio.models.ModelReference;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.documentmodel.Element;
import de.a12.studio.models.documentmodel.GroupElement;
import de.a12.studio.models.documentmodel.IncludeConfig;
import de.a12.studio.models.documentmodel.TypeDefinition;
import de.a12.studio.models.typedefinitionmodel.TypeDefinitionModel;
import org.jspecify.annotations.NonNull;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Resolves the {@link TypeDefinition}s a {@link DocumentModel} inherits transitively from other models, the
 * same two ways SME's document-model expansion does it server-side (see {@code dmExpansionPostProcessor.ts}'s
 * {@code markTypeDefs}, and {@code dmExpansion.ts}'s {@code getImportedModelsFromImportsAndIncludes}):
 * <ul>
 *   <li><b>Include</b>: a Group ({@link GroupElement}) whose {@link
 *   de.a12.studio.models.documentmodel.GroupConfig#getIncludeConfig()} is set pulls in another (regular)
 *   {@link DocumentModel}'s whole element tree, and any field in there of type {@code TypeDefType} only
 *   resolves if the type definition it points at travels along with it.</li>
 *   <li><b>Import</b>: a header {@link ModelReference} of purpose {@link ModelReference#PURPOSE_TYPE_DEFINITIONS}
 *   pulls in every {@link TypeDefinition} a {@link TypeDefinitionModel} (a "TDM") owns, whole file at a time -
 *   there is no per-type-definition import.</li>
 * </ul>
 * Both travel transitively (an included/imported model can itself include/import further) and are walked
 * together here since they compose: an included model can import a TDM, and a TDM can import another TDM.
 * a12-studio has no server-side expansion step, so this walks both graphs directly against the in-memory
 * sibling models instead.
 * <p>
 * Lives alongside {@link ElementIndex} (rather than in the UI module, where this was first written) since
 * {@link ElementIndex#effectiveFieldType} needs it too: a {@code TypeDefType} field pointing at a type
 * definition that's only reachable transitively (not in the model's own {@code typeDefinitions}) must still
 * resolve, or {@code MissingReferenceValidator} would wrongly flag it as "Missing Type Definition".
 */
public final class TransitiveTypeDefinitions {

  private TransitiveTypeDefinitions() {
  }

  /**
   * One type definition inherited transitively: {@code ownerModelId} is the model whose own {@code
   * typeDefinitions} it's declared in, {@code sourcePath} is the full chain of models it travelled through to
   * get here (ending in {@code ownerModelId}), {@code imported} says whether it arrived via an Import
   * reference (a {@link TypeDefinitionModel} owns it) rather than an Include (a regular {@link DocumentModel}
   * owns it), and {@code includedImported} narrows that further: true when the chain starts with an Include
   * and ends with an Import - a model this model includes that itself imports a Type Definition Model. SME
   * shows these "merely displayed to explain their content" (grey, informational) rather than directly usable
   * in the current model - using one still requires importing that Type Definition Model directly.
   */
  public record Entry(TypeDefinition typeDefinition, String ownerModelId, String sourcePath, boolean imported,
                       boolean includedImported) {
  }

  /**
   * Every type definition reachable from {@code model} through its (possibly nested) Include groups and
   * Import references, transitively, deduplicated by type definition id (the first occurrence found wins).
   * Does not include {@code model}'s own {@code typeDefinitions} - callers already have those directly.
   */
  public static List<Entry> resolve(@NonNull DocumentModel model, @NonNull List<DocumentModel> otherModels) {
    List<Entry> found = new ArrayList<>();
    Deque<String> path = new ArrayDeque<>();
    path.addLast(model.getId());
    collect(model, otherModels, path, new ArrayDeque<>(), found);

    Map<String, Entry> byTypeDefId = new LinkedHashMap<>();
    for (Entry entry : found) {
      byTypeDefId.putIfAbsent(entry.typeDefinition().getId(), entry);
    }
    return List.copyOf(byTypeDefId.values());
  }

  /**
   * Every {@link TypeDefinitionModel} id reachable from {@code model} through Import references alone,
   * transitively - mirrors SME's {@code DocumentModelExpansion.getImportedModelsFromImports}, used to reject a
   * candidate Import in the picker that would close a cycle (importing a TDM that already imports, directly
   * or transitively, the model doing the importing).
   */
  public static Set<String> importedModelIds(@NonNull DocumentModel model, @NonNull List<DocumentModel> otherModels) {
    Set<String> visited = new LinkedHashSet<>();
    collectImportedModelIds(model, otherModels, visited);
    return visited;
  }

  private static void collectImportedModelIds(DocumentModel model, List<DocumentModel> otherModels, Set<String> visited) {
    for (ModelReference reference : importReferences(model)) {
      DocumentModel imported = resolveReference(reference.getReference(), otherModels);
      if (imported == null || !visited.add(imported.getId())) {
        continue;
      }
      collectImportedModelIds(imported, otherModels, visited);
    }
  }

  /**
   * True if {@code model}'s own Import graph - not {@code model}'s direct Import references themselves (those
   * are already checked by {@link de.a12.studio.modelsvalidation.validators.HeaderModelReferenceValidator}),
   * but every Import reference reachable transitively from {@code model} - has a broken edge somewhere.
   * Callers use this on an already-resolved Import target to detect the case SME calls "transitive imports
   * missing": model A imports B, B imports C, C was deleted - opening A alone shows nothing wrong via a
   * direct-reference check on A, since A's own reference to B still resolves fine, but A's effective type
   * definitions are incomplete because of the break further down B's own chain.
   */
  public static boolean hasUnresolvedImportChain(@NonNull DocumentModel model, @NonNull List<DocumentModel> otherModels) {
    Set<String> visited = new LinkedHashSet<>();
    visited.add(model.getId());
    return hasUnresolvedImportChain(model, otherModels, visited);
  }

  private static boolean hasUnresolvedImportChain(DocumentModel model, List<DocumentModel> otherModels, Set<String> visited) {
    for (ModelReference reference : importReferences(model)) {
      DocumentModel imported = resolveReference(reference.getReference(), otherModels);
      if (imported == null) {
        return true;
      }
      if (visited.add(imported.getId()) && hasUnresolvedImportChain(imported, otherModels, visited)) {
        return true;
      }
    }
    return false;
  }

  private static void collect(DocumentModel model, List<DocumentModel> otherModels, Deque<String> path,
      Deque<Boolean> edgeIsImport, List<Entry> result) {
    for (Element element : new ElementIndex(model).allElements()) {
      if (!(element instanceof GroupElement groupElement) || groupElement.getGroup() == null) {
        continue;
      }
      IncludeConfig includeConfig = groupElement.getGroup().getIncludeConfig();
      if (includeConfig == null) {
        continue;
      }
      visit(resolveReference(includeConfig.getReference(), otherModels), otherModels, path, edgeIsImport, result, false);
    }

    for (ModelReference reference : importReferences(model)) {
      visit(resolveReference(reference.getReference(), otherModels), otherModels, path, edgeIsImport, result, true);
    }
  }

  /**
   * Both an Include and an Import are, from here, just an edge to another model whose own type definitions
   * (and further edges) need to be pulled in - the only difference is what {@link #collect} looked at to find
   * the edge ({@code isImportEdge}), so both funnel through this one recursion step.
   */
  private static void visit(DocumentModel target, List<DocumentModel> otherModels, Deque<String> path,
      Deque<Boolean> edgeIsImport, List<Entry> result, boolean isImportEdge) {
    // A missing reference is already surfaced by MissingReferenceValidator; a reference back onto a model
    // already on this branch of the path is a cycle and must not be followed further.
    if (target == null || path.contains(target.getId())) {
      return;
    }

    String sourcePath = sourcePath(path, target.getId());
    // "included-imported" (see Entry's doc): the chain that reached target started with an Include and this,
    // its last edge, is an Import - i.e. an included model that itself imports a Type Definition Model.
    boolean includedImported = target instanceof TypeDefinitionModel && isImportEdge
        && !(edgeIsImport.isEmpty() ? isImportEdge : edgeIsImport.peekFirst());

    List<TypeDefinition> ownTypeDefinitions = target.getContent().getTypeDefinitions();
    if (ownTypeDefinitions != null) {
      boolean imported = target instanceof TypeDefinitionModel;
      for (TypeDefinition typeDefinition : ownTypeDefinitions) {
        result.add(new Entry(typeDefinition, target.getId(), sourcePath, imported, includedImported));
      }
    }

    path.addLast(target.getId());
    edgeIsImport.addLast(isImportEdge);
    collect(target, otherModels, path, edgeIsImport, result);
    edgeIsImport.removeLast();
    path.removeLast();
  }

  /** The model's header references of purpose {@link ModelReference#PURPOSE_TYPE_DEFINITIONS} ("Import"). */
  private static List<ModelReference> importReferences(DocumentModel model) {
    List<ModelReference> references = model.getModelReferences();
    if (references == null) {
      return List.of();
    }
    return references.stream().filter(ref -> ModelReference.PURPOSE_TYPE_DEFINITIONS.equals(ref.getPurpose())).toList();
  }

  /** The chain from directly under the root model down to (and including) {@code ownerModelId}. */
  private static String sourcePath(Deque<String> path, String ownerModelId) {
    List<String> chain = new ArrayList<>(path);
    chain.remove(0); // the root model itself is "this document", not part of its own source description
    chain.add(ownerModelId);
    return String.join(" > ", chain);
  }

  /** Mirrors the strip-path-and-.json-suffix resolution the a12 kernel's reference resolver used. */
  private static DocumentModel resolveReference(String reference, List<DocumentModel> otherModels) {
    if (reference == null) {
      return null;
    }
    String id = reference;
    int lastSlash = id.lastIndexOf('/');
    if (lastSlash >= 0) {
      id = id.substring(lastSlash + 1);
    }
    int jsonSuffix = id.lastIndexOf(".json");
    if (jsonSuffix >= 0) {
      id = id.substring(0, jsonSuffix);
    }
    String finalId = id;
    return otherModels.stream().filter(dm -> finalId.equals(dm.getId())).findFirst().orElse(null);
  }
}
