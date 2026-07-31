package de.a12.studio.ui.editors.typedefinitionmodel;

import de.a12.studio.models.ModelReference;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.documentmodel.Element;
import de.a12.studio.models.documentmodel.GroupElement;
import de.a12.studio.models.documentmodel.IncludeConfig;
import de.a12.studio.models.documentmodel.TypeDefinition;
import de.a12.studio.models.typedefinitionmodel.TypeDefinitionModel;
import de.a12.studio.modelsvalidation.validators.ElementIndex;
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
 */
public final class TransitiveTypeDefinitions {

  private TransitiveTypeDefinitions() {
  }

  /**
   * One type definition inherited transitively: {@code ownerModelId} is the model whose own {@code
   * typeDefinitions} it's declared in, {@code sourcePath} is the full chain of models it travelled through to
   * get here (ending in {@code ownerModelId}), and {@code imported} says whether it arrived via an Import
   * reference (a {@link TypeDefinitionModel} owns it) rather than an Include (a regular {@link DocumentModel}
   * owns it).
   */
  public record Entry(TypeDefinition typeDefinition, String ownerModelId, String sourcePath, boolean imported) {
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
    collect(model, otherModels, path, found);

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

  private static void collect(DocumentModel model, List<DocumentModel> otherModels, Deque<String> path, List<Entry> result) {
    for (Element element : new ElementIndex(model).allElements()) {
      if (!(element instanceof GroupElement groupElement) || groupElement.getGroup() == null) {
        continue;
      }
      IncludeConfig includeConfig = groupElement.getGroup().getIncludeConfig();
      if (includeConfig == null) {
        continue;
      }
      visit(resolveReference(includeConfig.getReference(), otherModels), otherModels, path, result);
    }

    for (ModelReference reference : importReferences(model)) {
      visit(resolveReference(reference.getReference(), otherModels), otherModels, path, result);
    }
  }

  /**
   * Both an Include and an Import are, from here, just an edge to another model whose own type definitions
   * (and further edges) need to be pulled in - the only difference is what {@link #collect} looked at to find
   * the edge, so both funnel through this one recursion step.
   */
  private static void visit(DocumentModel target, List<DocumentModel> otherModels, Deque<String> path, List<Entry> result) {
    // A missing reference is already surfaced by MissingReferenceValidator; a reference back onto a model
    // already on this branch of the path is a cycle and must not be followed further.
    if (target == null || path.contains(target.getId())) {
      return;
    }

    String sourcePath = sourcePath(path, target.getId());
    List<TypeDefinition> ownTypeDefinitions = target.getContent().getTypeDefinitions();
    if (ownTypeDefinitions != null) {
      boolean imported = target instanceof TypeDefinitionModel;
      for (TypeDefinition typeDefinition : ownTypeDefinitions) {
        result.add(new Entry(typeDefinition, target.getId(), sourcePath, imported));
      }
    }

    path.addLast(target.getId());
    collect(target, otherModels, path, result);
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
