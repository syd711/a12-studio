package de.a12.studio.modelsvalidation.validators;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.additivedocumentmodel.AdditiveDocumentModel;
import de.a12.studio.models.additivedocumentmodel.AdditiveDocumentModelResolver;
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
import java.util.Objects;
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

  // Elements that don't belong to this model's own file at all, but are reachable through it because it's
  // an AdditiveDocumentModel and this element lives in the base Document Model it adds onto, at a position
  // this model itself doesn't (re)define locally - see mergeAdditiveElements(). Kept out of byId/parentOf/all
  // deliberately: those back allElements()/parentOf(), which every structural check (duplicate names, enum
  // counts, type definitions, ...) iterates, and a base model's own fields must not be validated a second
  // time as if they were this model's. Only path resolution (resolveRelativePath, via resolveByNamePath) and
  // getPath() (via parentOfAny) look here - covering the two real use cases: a Computation/Rule's relative
  // path legitimately pointing at an inherited field, and the target-field picker offering such fields too.
  private final Map<Element, GroupElement> additiveParentOf = new HashMap<>();
  private final List<FieldElement> additiveFieldElements = new ArrayList<>();

  // Lazily computed on first effectiveFieldType() lookup that needs it, then reused: TransitiveTypeDefinitions
  // walks the whole Include/Import graph, and effectiveFieldType is called once per TypeDefType field, so this
  // avoids re-walking it from scratch for every such field in the same model.
  private List<TransitiveTypeDefinitions.Entry> transitiveTypeDefinitions;

  public ElementIndex(DocumentModel model) {
    this(model, List.of(), List.of());
  }

  /**
   * @param otherModels every other {@link DocumentModel} in the project, needed by {@link #effectiveFieldType}
   *                     to resolve a {@code TypeDefType} field pointing at a type definition this model
   *                     doesn't own directly, but inherits transitively through an Include or Import (see
   *                     {@link TransitiveTypeDefinitions}). Pass {@code List.of()} if the caller doesn't need
   *                     that (e.g. a check that never touches {@code TypeDefType} fields).
   */
  public ElementIndex(DocumentModel model, List<DocumentModel> otherModels) {
    this(model, otherModels, List.of());
  }

  /**
   * @param otherModelsOfAnyType every other model in the project, of any type - needed, only when {@code
   *                     model} is an {@link AdditiveDocumentModel}, to resolve the base Document Model it
   *                     adds onto (via {@link AdditiveDocumentModelResolver}, which looks for a Combination
   *                     Model among these) so {@link #resolveRelativePath} and the target-field picker (see
   *                     {@link #additiveFieldElements()}) can reach fields that model provides but {@code
   *                     model}'s own file doesn't redefine. Pass {@code List.of()} if the caller doesn't need
   *                     that (e.g. {@code model} is never an Additive Document Model in practice, or the
   *                     check never touches relative paths).
   */
  public ElementIndex(DocumentModel model, List<DocumentModel> otherModels, List<A12Model<?>> otherModelsOfAnyType) {
    this.model = model;
    this.otherModels = otherModels;
    List<GroupElement> rootGroups = model.getContent().getModelRoot().getRootGroups();
    if (rootGroups != null) {
      for (GroupElement rootGroup : rootGroups) {
        index(rootGroup, null);
      }
    }
    if (model instanceof AdditiveDocumentModel additiveModel) {
      AdditiveDocumentModelResolver.findBaseModel(additiveModel, otherModelsOfAnyType, otherModels)
          .ifPresent(baseModel -> mergeAdditiveBaseModel(rootGroups, baseModel));
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

  /**
   * For each of {@code localRootGroups} that shares its name with one of {@code baseModel}'s own root
   * groups (the ordinary case: an Additive Document Model's root group mirrors the base model's), merges
   * that base root group's own descendants in - see {@link #mergeAdditiveElements}.
   */
  private void mergeAdditiveBaseModel(List<GroupElement> localRootGroups, DocumentModel baseModel) {
    if (localRootGroups == null || baseModel.getContent() == null || baseModel.getContent().getModelRoot() == null) {
      return;
    }
    List<GroupElement> baseRootGroups = baseModel.getContent().getModelRoot().getRootGroups();
    if (baseRootGroups == null) {
      return;
    }
    for (GroupElement localRoot : localRootGroups) {
      baseRootGroups.stream()
          .filter(baseRoot -> Objects.equals(baseRoot.getName(), localRoot.getName()))
          .findFirst()
          .ifPresent(baseRoot -> mergeAdditiveElements(localRoot, baseRoot));
    }
  }

  /**
   * Recursively makes {@code baseGroup}'s own children reachable as if they were also children of {@code
   * localGroup} (see the {@link #additiveParentOf}/{@link #additiveFieldElements} field docs for why they're
   * indexed separately from this model's real elements). A child whose name is already used by a real local
   * child shadows the base one - matching the a12 kernel's Addition-step override semantics - but if both
   * are groups, their own children are still merged, so a field the base model nests two levels deep under a
   * group this Additive Document Model also happens to redefine (by name) is still found.
   */
  private void mergeAdditiveElements(GroupElement localGroup, GroupElement baseGroup) {
    if (baseGroup.getGroup() == null || baseGroup.getGroup().getElements() == null) {
      return;
    }
    List<Element> localElements = localGroup.getGroup() == null ? null : localGroup.getGroup().getElements();
    Set<String> localNames = new HashSet<>();
    if (localElements != null) {
      for (Element localElement : localElements) {
        localNames.add(localElement.getName());
      }
    }
    for (Element baseChild : baseGroup.getGroup().getElements()) {
      if (!localNames.contains(baseChild.getName())) {
        indexAdditive(baseChild, localGroup);
        continue;
      }
      if (baseChild instanceof GroupElement baseChildGroup && localElements != null) {
        localElements.stream()
            .filter(GroupElement.class::isInstance).map(GroupElement.class::cast)
            .filter(localChildGroup -> Objects.equals(localChildGroup.getName(), baseChildGroup.getName()))
            .findFirst()
            .ifPresent(localChildGroup -> mergeAdditiveElements(localChildGroup, baseChildGroup));
      }
    }
  }

  private void indexAdditive(Element element, GroupElement parent) {
    additiveParentOf.put(element, parent);
    if (element instanceof FieldElement field) {
      additiveFieldElements.add(field);
    }
    if (element instanceof GroupElement group && group.getGroup() != null && group.getGroup().getElements() != null) {
      for (Element child : group.getGroup().getElements()) {
        indexAdditive(child, group);
      }
    }
  }

  /**
   * Every {@link FieldElement} reachable only through {@link #mergeAdditiveElements} - i.e. fields the base
   * Document Model of an Additive Document Model provides at a position this model's own file doesn't
   * redefine. Empty unless this index's model is an {@link AdditiveDocumentModel} with a resolvable base
   * model (see the 3-arg constructor). Used by the target-field picker to offer these alongside this
   * model's own fields from {@link #allElements()}.
   */
  public List<FieldElement> additiveFieldElements() {
    return additiveFieldElements;
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
      current = parentOfAny(current);
    }
    return new ArrayList<>(names);
  }

  /** {@link #parentOf} for this model's own elements, falling back to {@link #additiveParentOf} for one
   * reached only through {@link #mergeAdditiveElements} - see that field's doc. */
  private GroupElement parentOfAny(Element element) {
    return parentOf.containsKey(element) ? parentOf.get(element) : additiveParentOf.get(element);
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
    return resolve(elementId, new HashSet<>(List.of(model.getId()))).map(Resolution::path).orElse(elementId);
  }

  /** Whether {@code elementId} resolves to an actual element, direct or through an Include (see {@link
   * #resolveDisplayPath}) - i.e. whether the {@code elementId} fallback in that method's result means the
   * reference is dangling rather than a genuinely resolved path. */
  public boolean isResolvable(String elementId) {
    return elementId != null && resolve(elementId, new HashSet<>(List.of(model.getId()))).isPresent();
  }

  /**
   * Resolves {@code elementId} to the actual {@link Element} it refers to, direct or through an Include (see
   * {@link #resolveDisplayPath} for the reference-resolution rules). Unlike {@link #resolveDisplayPath}, this
   * returns the element itself - e.g. so a caller can inspect a {@link FieldElement}'s {@code FieldType} -
   * rather than just its display path. Empty when {@code elementId} is a dangling reference, or {@code null}.
   */
  public Optional<Element> resolveElement(String elementId) {
    if (elementId == null) {
      return Optional.empty();
    }
    return resolve(elementId, new HashSet<>(List.of(model.getId()))).map(Resolution::element);
  }

  /**
   * True if {@code elementId} resolves (directly, or transitively through nested Includes - see {@link
   * #resolveElement}) to an element with a repeatable ancestor. Mirrors SME's {@code
   * getDmReferenceCandidates} {@code isRepeatable} propagation over its merged Include tree: that traversal
   * flattens an included model's elements as ordinary descendants of the Include node, so the Include
   * group's own repeatability - and the repeatability of everything above it back to this model's root - is
   * inherited by every element resolved through it, on top of that element's own ancestors within the
   * included model itself. False when {@code elementId} doesn't resolve at all.
   */
  public boolean isInRepeatableGroup(String elementId) {
    if (elementId == null) {
      return false;
    }
    return resolve(elementId, new HashSet<>(List.of(model.getId()))).map(Resolution::repeatableAncestor).orElse(false);
  }

  private Optional<Resolution> resolve(String elementId, Set<String> visitedModelIds) {
    Optional<Element> direct = findById(elementId);
    if (direct.isPresent()) {
      Element element = direct.get();
      return Optional.of(new Resolution(element, getPath(element), hasRepeatableAncestor(element)));
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
      Optional<Resolution> inner = new ElementIndex(included, otherModels).resolve(elementId.substring(prefix.length()), visitedModelIds);
      if (inner.isPresent()) {
        boolean repeatableThroughInclude = isRepeatable(group) || hasRepeatableAncestor(group);
        // inner.path() starts with the included model's own root-group name, which the include group in
        // this model (getPath(group), above) already represents as its mount point - e.g. an "Addresses"
        // include group referencing a Document Model whose own root group is also named "Addresses" would
        // otherwise duplicate that segment. Strip it so only the elements *below* the included root are
        // appended.
        String innerPathBelowIncludedRoot = inner.get().path().replaceFirst("^/[^/]*", "");
        return Optional.of(new Resolution(inner.get().element(), getPath(group) + innerPathBelowIncludedRoot,
            inner.get().repeatableAncestor() || repeatableThroughInclude));
      }
    }
    return Optional.empty();
  }

  private static boolean isRepeatable(GroupElement group) {
    return group.getGroup() != null && group.getGroup().getRepeatability() != null && group.getGroup().getRepeatability() > 1;
  }

  /** True when any ancestor group of {@code element} (not this index's own root group) has a repeatability
   * above 1. */
  private boolean hasRepeatableAncestor(Element element) {
    GroupElement parent = parentOf(element);
    while (parent != null) {
      if (isRepeatable(parent) && parentOf(parent) != null) {
        return true;
      }
      parent = parentOf(parent);
    }
    return false;
  }

  private record Resolution(Element element, String path, boolean repeatableAncestor) {}

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

  /**
   * The inverse of {@link #resolveRelativePath}: the relative-path string that, passed to that method with
   * {@code from} as the referencing element, resolves back to {@code to} - e.g. {@code "../fieldName"} for a
   * Rule/Computation reaching a sibling field. Used by the UI's target-element picker (Rule's
   * errorEntityRelPath / Computation's computedFieldRelPath) to turn a user's tree selection into the stored
   * relative-path string. Both elements must belong to this index's own model (not resolved through an
   * Include).
   */
  public String relativePathTo(Element from, Element to) {
    List<String> fromPath = ancestorNamesIncludingSelf(from);
    List<String> toPath = ancestorNamesIncludingSelf(to);
    int common = 0;
    while (common < fromPath.size() && common < toPath.size() && fromPath.get(common).equals(toPath.get(common))) {
      common++;
    }
    StringBuilder result = new StringBuilder();
    for (int i = common; i < fromPath.size(); i++) {
      result.append("../");
    }
    result.append(String.join("/", toPath.subList(common, toPath.size())));
    return result.toString();
  }

  /**
   * The direct children of the group reached by descending {@code scopeNames} from the model root - e.g. the
   * enclosing {@code kontext(...)} chain at some point in an Overview/Form "Expression" (see {@code
   * docs/2606-06-doc/expression-expression-docs.md}'s grammar) - or, when {@code scopeNames} is empty, the
   * combined direct children of every root group (normally just one). A {@code kontext(...)} block only
   * exposes its own named group's direct children as bare {@code [FieldName]}/{@code kontext(childName)}
   * targets, not the whole model, mirroring the language's scoping rules. Empty if {@code scopeNames} doesn't
   * resolve to an actual group.
   */
  public List<Element> directChildren(List<String> scopeNames) {
    if (scopeNames.isEmpty()) {
      List<GroupElement> rootGroups = model.getContent().getModelRoot().getRootGroups();
      if (rootGroups == null) {
        return List.of();
      }
      List<Element> children = new ArrayList<>();
      for (GroupElement root : rootGroups) {
        if (root.getGroup() != null && root.getGroup().getElements() != null) {
          children.addAll(root.getGroup().getElements());
        }
      }
      return children;
    }
    return resolveByNamePath(scopeNames)
        .filter(GroupElement.class::isInstance)
        .map(GroupElement.class::cast)
        .map(group -> group.getGroup() != null && group.getGroup().getElements() != null ? group.getGroup().getElements() : List.<Element>of())
        .orElse(List.of());
  }

  private Optional<Element> resolveByNamePath(List<String> names) {
    if (names.isEmpty()) {
      return Optional.empty();
    }
    List<GroupElement> rootGroups = model.getContent().getModelRoot().getRootGroups();
    Element current = rootGroups == null ? null : findByName(rootGroups, names.get(0));
    Optional<Element> local = descendByName(current, names, 1, new HashSet<>(List.of(model.getId())));
    if (local.isPresent() || additiveFieldElements.isEmpty()) {
      return local;
    }
    // Not found among this model's own elements - see if it's one reached only through the base model an
    // Additive Document Model adds onto (mergeAdditiveElements()); getPath() works for those too, via
    // parentOfAny(), so a plain path match is enough without a parallel name-walk.
    String targetPath = "/" + String.join("/", names);
    return additiveFieldElements.stream().filter(field -> getPath(field).equals(targetPath)).findFirst().map(Element.class::cast);
  }

  /**
   * Walks {@code names[i..]} down from {@code current}, mirroring the kernel's flattened Include tree: when a
   * step lands on a Group whose elements aren't stored locally but referenced through an {@link
   * de.a12.studio.models.documentmodel.GroupConfig#getIncludeConfig()} (see {@link #resolve} for the id-based
   * equivalent), the next name segment is looked up among the *included* model's own root-group elements
   * instead - the include group's name already stands in for the included model's own root group, matching how
   * SME requires those two names to match.
   */
  private Optional<Element> descendByName(Element current, List<String> names, int i, Set<String> visitedModelIds) {
    while (current != null && i < names.size()) {
      if (!(current instanceof GroupElement group) || group.getGroup() == null) {
        return Optional.empty();
      }
      if (group.getGroup().getIncludeConfig() != null) {
        DocumentModel included = resolveIncludedModel(group.getGroup().getIncludeConfig().getReference());
        if (included == null || included.getContent() == null || included.getContent().getModelRoot() == null
            || !visitedModelIds.add(included.getId())) {
          return Optional.empty();
        }
        List<GroupElement> includedRootGroups = included.getContent().getModelRoot().getRootGroups();
        current = includedRootGroups == null ? null : findByName(includedRootGroups.stream()
            .filter(rootGroup -> rootGroup.getGroup() != null && rootGroup.getGroup().getElements() != null)
            .flatMap(rootGroup -> rootGroup.getGroup().getElements().stream())
            .toList(), names.get(i));
      } else if (group.getGroup().getElements() != null) {
        current = findByName(group.getGroup().getElements(), names.get(i));
      } else {
        return Optional.empty();
      }
      i++;
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
