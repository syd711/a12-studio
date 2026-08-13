package de.a12.studio.modelsvalidation.validators;

import de.a12.studio.models.documentmodel.ComputationElement;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.documentmodel.Element;
import de.a12.studio.models.documentmodel.FieldElement;
import de.a12.studio.models.documentmodel.FieldType;
import de.a12.studio.models.documentmodel.GroupElement;
import de.a12.studio.models.documentmodel.RuleElement;
import de.a12.studio.models.documentmodel.TypeDefFieldType;
import de.a12.studio.models.documentmodel.TypeDefinition;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Id lookup, parent tracking, and path/relative-path resolution for one {@link DocumentModel}'s element
 * tree, replacing the live object graph the a12 kernel used to build during deserialization. Built once per
 * validation call.
 */
public class ElementIndex {

  private final DocumentModel model;
  private final List<DocumentModel> otherModels;
  private final Map<String, Element> byId = new HashMap<>();
  private final Map<Element, GroupElement> parentOf = new HashMap<>();
  private final List<Element> all = new ArrayList<>();

  // Lazily computed on first effectiveFieldType() lookup that needs it, then reused: TransitiveTypeDefinitions
  // walks the whole Include/Import graph, and effectiveFieldType is called once per TypeDefType field, so this
  // avoids re-walking it from scratch for every such field in the same model.
  private List<TransitiveTypeDefinitions.Entry> transitiveTypeDefinitions;

  public ElementIndex(DocumentModel model) {
    this(model, List.of());
  }

  /**
   * @param otherModels every other {@link DocumentModel} in the project, needed by {@link #effectiveFieldType}
   *                     to resolve a {@code TypeDefType} field pointing at a type definition this model
   *                     doesn't own directly, but inherits transitively through an Include or Import (see
   *                     {@link TransitiveTypeDefinitions}). Pass {@code List.of()} if the caller doesn't need
   *                     that (e.g. a check that never touches {@code TypeDefType} fields).
   */
  public ElementIndex(DocumentModel model, List<DocumentModel> otherModels) {
    this.model = model;
    this.otherModels = otherModels;
    List<GroupElement> rootGroups = model.getContent().getModelRoot().getRootGroups();
    if (rootGroups != null) {
      for (GroupElement rootGroup : rootGroups) {
        index(rootGroup, null);
      }
    }
  }

  private void index(Element element, GroupElement parent) {
    if (element.getId() != null) {
      byId.put(element.getId(), element);
    }
    parentOf.put(element, parent);
    all.add(element);
    if (element instanceof GroupElement group && group.getGroup() != null && group.getGroup().getElements() != null) {
      for (Element child : group.getGroup().getElements()) {
        index(child, group);
      }
    }
  }

  /** Every element in the model, in document order. */
  public List<Element> allElements() {
    return all;
  }

  /** The {@link DocumentModel} this index was built from. */
  public DocumentModel getModel() {
    return model;
  }

  Optional<Element> findById(String id) {
    return Optional.ofNullable(id == null ? null : byId.get(id));
  }

  public GroupElement parentOf(Element element) {
    return parentOf.get(element);
  }

  /** "/"-separated path of element names from the model root, mirroring the kernel's ElementUtils.getPath. */
  public String getPath(Element element) {
    return "/" + String.join("/", ancestorNamesIncludingSelf(element));
  }

  private List<String> ancestorNamesIncludingSelf(Element element) {
    Deque<String> names = new ArrayDeque<>();
    Element current = element;
    while (current != null) {
      names.addFirst(current.getName());
      current = parentOf.get(current);
    }
    return new ArrayList<>(names);
  }

  /**
   * Resolves {@code elementId} to a display path: either a direct element of this model, or - if not found
   * here - a compound {@code "<includeGroupId>_<targetId>"} reference into an included model's own element
   * tree (an Include is a {@link GroupElement} whose {@link
   * de.a12.studio.models.documentmodel.GroupConfig#getIncludeConfig()} is set; its elements live entirely in
   * the referenced model, not locally - see {@link TransitiveTypeDefinitions}), resolved transitively through
   * nested includes the same way SME's {@code dmGetReferenceCandidates.resolveIncludedElementTargets} strips
   * the include's id prefix and looks the remainder up in the included model's own graph. Falls back to
   * {@code elementId} itself if nothing resolves (a dangling reference, or this index wasn't built with the
   * {@code otherModels} needed to follow the include).
   */
  public String resolveDisplayPath(String elementId) {
    if (elementId == null) {
      return null;
    }
    return resolve(elementId, new HashSet<>(List.of(model.getId()))).orElse(elementId);
  }

  /** Whether {@code elementId} resolves to an actual element, direct or through an Include (see {@link
   * #resolveDisplayPath}) - i.e. whether the {@code elementId} fallback in that method's result means the
   * reference is dangling rather than a genuinely resolved path. */
  public boolean isResolvable(String elementId) {
    return elementId != null && resolve(elementId, new HashSet<>(List.of(model.getId()))).isPresent();
  }

  private Optional<String> resolve(String elementId, Set<String> visitedModelIds) {
    Optional<Element> direct = findById(elementId);
    if (direct.isPresent()) {
      return Optional.of(getPath(direct.get()));
    }
    for (Element element : all) {
      if (!(element instanceof GroupElement group) || group.getGroup() == null
          || group.getGroup().getIncludeConfig() == null || element.getId() == null) {
        continue;
      }
      String prefix = element.getId() + "_";
      if (!elementId.startsWith(prefix)) {
        continue;
      }
      DocumentModel included = resolveIncludedModel(group.getGroup().getIncludeConfig().getReference());
      if (included == null || included.getContent() == null || included.getContent().getModelRoot() == null
          || !visitedModelIds.add(included.getId())) {
        continue;
      }
      Optional<String> innerPath = new ElementIndex(included, otherModels).resolve(elementId.substring(prefix.length()), visitedModelIds);
      if (innerPath.isPresent()) {
        return Optional.of(getPath(group) + innerPath.get());
      }
    }
    return Optional.empty();
  }

  private DocumentModel resolveIncludedModel(String reference) {
    if (reference == null) {
      return null;
    }
    return otherModels.stream().filter(candidate -> reference.equals(candidate.getId())).findFirst().orElse(null);
  }

  /**
   * Resolves a relative element-path such as an indexFieldName or a computedFieldRelPath/errorEntityRelPath,
   * treating {@code referencingElement}'s own path as the resolution base: a plain segment descends into a
   * child by name (so a Group's own indexFieldName, a direct child, resolves with no "../"), while "../"
   * pops back up to the parent (so a Computation/Rule, which has no children, reaches sibling fields via
   * "../fieldName"). Mirrors the kernel's ElementPathUtils.absPath(getPath(element), relativePath).
   */
  public Optional<Element> resolveRelativePath(Element referencingElement, String relativePath) {
    if (relativePath == null || relativePath.isBlank()) {
      return Optional.empty();
    }
    Deque<String> path = new ArrayDeque<>(ancestorNamesIncludingSelf(referencingElement));
    for (String segment : relativePath.split("/")) {
      if (segment.isEmpty()) {
        continue;
      } else if (segment.equals("..")) {
        if (!path.isEmpty()) {
          path.removeLast();
        }
      } else {
        path.addLast(segment);
      }
    }
    return resolveByNamePath(new ArrayList<>(path));
  }

  private Optional<Element> resolveByNamePath(List<String> names) {
    if (names.isEmpty()) {
      return Optional.empty();
    }
    List<GroupElement> rootGroups = model.getContent().getModelRoot().getRootGroups();
    Element current = rootGroups == null ? null : findByName(rootGroups, names.get(0));
    for (int i = 1; current != null && i < names.size(); i++) {
      if (!(current instanceof GroupElement group) || group.getGroup() == null || group.getGroup().getElements() == null) {
        return Optional.empty();
      }
      current = findByName(group.getGroup().getElements(), names.get(i));
    }
    return Optional.ofNullable(current);
  }

  private Element findByName(List<? extends Element> elements, String name) {
    for (Element element : elements) {
      if (name.equals(element.getName())) {
        return element;
      }
    }
    return null;
  }

  public static boolean isField(Element element) {
    return element instanceof FieldElement;
  }

  static boolean isGroup(Element element) {
    return element instanceof GroupElement;
  }

  static boolean isRule(Element element) {
    return element instanceof RuleElement;
  }

  static boolean isComputation(Element element) {
    return element instanceof ComputationElement;
  }

  /**
   * Resolves a TypeDefType field to the field type it points to, mirroring the kernel's
   * Field.getEffectiveType(). Looks first at this model's own {@code typeDefinitions}, then - since a
   * {@code TypeDefType} field can just as validly point at a type definition inherited transitively through
   * an Include or Import (see {@link TransitiveTypeDefinitions}) - at every type definition reachable that
   * way, so a field referencing one of those doesn't get wrongly flagged "Missing Type Definition" by {@link
   * MissingReferenceValidator}.
   */
  public FieldType effectiveFieldType(FieldType fieldType) {
    if (fieldType instanceof TypeDefFieldType typeDefFieldType
        && typeDefFieldType.getTypeDefType() != null
        && typeDefFieldType.getTypeDefType().getTypeDefinitionId() != null) {
      String typeDefId = typeDefFieldType.getTypeDefType().getTypeDefinitionId();
      List<TypeDefinition> typeDefinitions = model.getContent().getTypeDefinitions();
      if (typeDefinitions != null) {
        for (TypeDefinition typeDefinition : typeDefinitions) {
          if (typeDefId.equals(typeDefinition.getId())) {
            return typeDefinition.getFieldType();
          }
        }
      }
      for (TransitiveTypeDefinitions.Entry entry : transitiveTypeDefinitions()) {
        if (typeDefId.equals(entry.typeDefinition().getId())) {
          return entry.typeDefinition().getFieldType();
        }
      }
      return null;
    }
    return fieldType;
  }

  private List<TransitiveTypeDefinitions.Entry> transitiveTypeDefinitions() {
    if (transitiveTypeDefinitions == null) {
      transitiveTypeDefinitions = TransitiveTypeDefinitions.resolve(model, otherModels);
    }
    return transitiveTypeDefinitions;
  }
}
