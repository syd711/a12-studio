package de.a12.studio.ui.editors.typedefinitionmodel;

import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.documentmodel.Element;
import de.a12.studio.models.documentmodel.GroupElement;
import de.a12.studio.models.documentmodel.IncludeConfig;
import de.a12.studio.models.documentmodel.TypeDefinition;
import de.a12.studio.modelsvalidation.validators.ElementIndex;
import org.jspecify.annotations.NonNull;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Resolves the {@link TypeDefinition}s a {@link DocumentModel} inherits through its Include chain: an Include
 * group (a {@link GroupElement} whose {@link de.a12.studio.models.documentmodel.GroupConfig#getIncludeConfig()}
 * is set) pulls in the referenced model's whole element tree, and any field in there of type {@code TypeDefType}
 * only resolves if the type definition it points at travels along with it. Mirrors what SME's document-model
 * expansion does server-side (see {@code dmExpansionPostProcessor.ts#markTypeDefs} in the SME client, which
 * tags every transitively-included type definition with its owning model's path) - a12-studio has no such
 * expansion step, so this walks the Include graph directly against the in-memory sibling models instead.
 */
public final class TransitiveTypeDefinitions {

  private TransitiveTypeDefinitions() {
  }

  /** One type definition inherited through the include chain, together with the models it travelled through. */
  public record Entry(TypeDefinition typeDefinition, String sourcePath) {
  }

  /**
   * Every type definition reachable from {@code model} through its (possibly nested) Include groups,
   * transitively, deduplicated by type definition id (the first occurrence found wins). Does not include
   * {@code model}'s own {@code typeDefinitions} - callers already have those directly.
   */
  public static List<Entry> resolve(@NonNull DocumentModel model, @NonNull List<DocumentModel> otherModels) {
    List<Entry> found = new ArrayList<>();
    Deque<String> includePath = new ArrayDeque<>();
    includePath.addLast(model.getId());
    collect(model, otherModels, includePath, found);

    Map<String, Entry> byTypeDefId = new LinkedHashMap<>();
    for (Entry entry : found) {
      byTypeDefId.putIfAbsent(entry.typeDefinition().getId(), entry);
    }
    return List.copyOf(byTypeDefId.values());
  }

  private static void collect(DocumentModel model, List<DocumentModel> otherModels, Deque<String> includePath,
                               List<Entry> result) {
    for (Element element : new ElementIndex(model).allElements()) {
      if (!(element instanceof GroupElement groupElement) || groupElement.getGroup() == null) {
        continue;
      }
      IncludeConfig includeConfig = groupElement.getGroup().getIncludeConfig();
      if (includeConfig == null) {
        continue;
      }

      DocumentModel included = resolveReference(includeConfig.getReference(), otherModels);
      // A missing reference is already surfaced by MissingReferenceValidator; a reference back onto a model
      // already on this branch of the include path is a cycle and must not be followed further.
      if (included == null || includePath.contains(included.getId())) {
        continue;
      }

      String sourcePath = sourcePath(includePath, included.getId());
      List<TypeDefinition> ownTypeDefinitions = included.getContent().getTypeDefinitions();
      if (ownTypeDefinitions != null) {
        for (TypeDefinition typeDefinition : ownTypeDefinitions) {
          result.add(new Entry(typeDefinition, sourcePath));
        }
      }

      includePath.addLast(included.getId());
      collect(included, otherModels, includePath, result);
      includePath.removeLast();
    }
  }

  /** The include chain from directly under the root model down to (and including) {@code ownerModelId}. */
  private static String sourcePath(Deque<String> includePath, String ownerModelId) {
    List<String> chain = new ArrayList<>(includePath);
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
