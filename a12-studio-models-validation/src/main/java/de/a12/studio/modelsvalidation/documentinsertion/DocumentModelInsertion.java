package de.a12.studio.modelsvalidation.documentinsertion;

import de.a12.studio.models.Label;
import de.a12.studio.models.Locale;
import de.a12.studio.models.ModelReference;
import de.a12.studio.models.ModelType;
import de.a12.studio.models.additivedocumentmodel.AdditiveDocumentModel;
import de.a12.studio.models.documentmodel.ComputationElement;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.documentmodel.Element;
import de.a12.studio.models.documentmodel.EnumerationFieldType;
import de.a12.studio.models.documentmodel.EnumerationValue;
import de.a12.studio.models.documentmodel.FieldConfig;
import de.a12.studio.models.documentmodel.FieldElement;
import de.a12.studio.models.documentmodel.FieldType;
import de.a12.studio.models.documentmodel.GroupConfig;
import de.a12.studio.models.documentmodel.GroupElement;
import de.a12.studio.models.documentmodel.IncludeConfig;
import de.a12.studio.models.documentmodel.RuleElement;
import de.a12.studio.models.documentmodel.StringFieldType;
import de.a12.studio.models.documentmodel.TypeDefFieldType;
import de.a12.studio.models.documentmodel.TypeDefinition;
import de.a12.studio.models.typedefinitionmodel.TypeDefinitionModel;
import de.a12.studio.models.util.JsonSettings;
import de.a12.studio.modelsvalidation.validators.TransitiveTypeDefinitions;
import de.a12.studio.modelsvalidation.validators.TypeDefinitionMode;
import org.jspecify.annotations.NonNull;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Plans "Insert from another Document Model": copies the content of a source Document Model into a target one,
 * the way SME's "Copy Document Model" ({@code event_copy_dm}, {@code copyDocumentModelSagas.ts}) does. The
 * result is a {@link Plan} the caller applies as one undoable step; nothing here touches {@code target} or
 * {@code source}, every element in the plan is a deep copy.
 * <ul>
 *   <li><b>Includes become groups.</b> An Include group of the source (at any depth, and inside the models it
 *   includes) is replaced by a plain group of the same name/repeatability whose children are the included
 *   model's own root-group children - the same "include mounts the root group's children" view the tree shows -
 *   so the copy is self-contained and does not depend on the source's includes. An Include that cannot be
 *   flattened (its model is missing, or it closes a cycle) is kept as an Include and reported as a warning.</li>
 *   <li><b>Type definitions</b> follow SME's all-or-nothing rule that a model either owns its type definitions
 *   or imports a Type Definition Model (see {@link TypeDefinitionMode}): when any type definition the copied
 *   fields use is owned by a regular Document Model (the source or one it includes), all of them are copied as
 *   new local type definitions with fresh ids and the copied fields are re-pointed; when they all come from
 *   Type Definition Models, the target instead imports those models. A target whose mode does not match yields
 *   a {@link Problem} and no plan. Unlike SME, only the type definitions actually used by a copied field are
 *   carried over, not every one the source could see.</li>
 *   <li><b>Locales</b>: labels and texts in a locale the target does not declare are dropped (SME's
 *   {@code syncLocales}). Missing translations for a target locale are not padded with empty texts - the
 *   validators already report them.</li>
 * </ul>
 * Element ids are left as they are in the source; the caller regenerates them for the target, which needs the
 * UI's id convention. Paths in rule/computation conditions are copied verbatim, as in SME: relative paths keep
 * working inside the copied subtree, an absolute path that starts at the source's root group does not.
 */
public final class DocumentModelInsertion {

  /** Why an insertion cannot be planned; {@link #NONE} when it can. */
  public enum Problem {
    NONE,
    /** The source (directly or through its includes) includes the target, so the copy would include itself. */
    SOURCE_INCLUDES_TARGET,
    /** The copied fields need local type definitions, but the target imports a Type Definition Model. */
    LOCAL_TYPE_DEFINITIONS_IN_IMPORT_MODE,
    /** The copied fields come from Type Definition Models to import, but the target owns type definitions. */
    IMPORTS_IN_LOCAL_MODE
  }

  /** What a {@link Warning} is about. */
  public enum WarningKind {
    /** An Include of the copy was kept as an Include: the Document Model it references is not in the project. */
    INCLUDE_MODEL_MISSING,
    /** An Include of the copy was kept as an Include: flattening it would include a model within itself. */
    INCLUDE_CYCLE,
    /** A copied field names a type definition that neither the source nor anything it includes/imports has. */
    TYPE_DEFINITION_MISSING
  }

  /**
   * Something the caller should tell the user about a usable plan. {@code subject} is the name of the Include
   * (or the type definition id) concerned, {@code detail} the id of the Document Model it references, if any.
   */
  public record Warning(WarningKind kind, String subject, String detail) {
  }

  /**
   * What to add to the target. {@code groups} go where the caller wants them (as children of a group or as root
   * groups); {@code typeDefinitions} are appended to the target's type definitions and {@code importReferences}
   * to its header references. {@code warnings} are notes for an information dialog. {@code problem} is {@link
   * Problem#NONE} for a usable plan; otherwise everything else is empty.
   */
  public record Plan(Problem problem, List<GroupElement> groups, List<TypeDefinition> typeDefinitions,
                     List<ModelReference> importReferences, List<Warning> warnings) {

    static Plan failed(Problem problem) {
      return new Plan(problem, List.of(), List.of(), List.of(), List.of());
    }

    public boolean isUsable() {
      return problem == Problem.NONE;
    }
  }

  private static final String TYPE_DEFINITION_ID_PREFIX = "typedef_";

  private final DocumentModel target;
  private final DocumentModel source;
  private final Map<String, DocumentModel> modelsById = new HashMap<>();
  private final List<Warning> warnings = new ArrayList<>();

  private DocumentModelInsertion(DocumentModel target, DocumentModel source, List<DocumentModel> projectModels) {
    this.target = target;
    this.source = source;
    for (DocumentModel model : projectModels) {
      if (model.getId() != null) {
        modelsById.putIfAbsent(model.getId(), model);
      }
    }
    modelsById.put(target.getId(), target);
    modelsById.put(source.getId(), source);
  }

  /**
   * Whether {@code candidate} is worth offering in the picker for {@code target}: a plain Document Model (no
   * Type Definition or Additive model, which SME does not count as standalone either) other than the target
   * itself, whose own Type Definition mode is compatible with the target's (SME's {@code hasSameTDMode}).
   * Deeper conflicts - the target being included by the candidate, a mode conflict only visible through the
   * candidate's includes - are reported by {@link #plan} instead.
   */
  public static boolean isCandidate(@NonNull DocumentModel target, @NonNull DocumentModel candidate) {
    return candidate != target
        && !Objects.equals(candidate.getId(), target.getId())
        && !(candidate instanceof TypeDefinitionModel)
        && !(candidate instanceof AdditiveDocumentModel)
        && candidate.getContent() != null
        && candidate.getContent().getModelRoot() != null
        && TypeDefinitionMode.modeOf(target).isCompatibleWith(TypeDefinitionMode.modeOf(candidate));
  }

  /**
   * @param projectModels every Document Model of the project (the target and source may or may not be among
   *                      them); used to resolve Includes and Type Definition Model imports
   */
  public static Plan plan(@NonNull DocumentModel target, @NonNull DocumentModel source,
                          @NonNull List<DocumentModel> projectModels) {
    return new DocumentModelInsertion(target, source, projectModels).plan();
  }

  private Plan plan() {
    if (includesTransitively(source, target.getId(), new HashSet<>())) {
      return Plan.failed(Problem.SOURCE_INCLUDES_TARGET);
    }

    List<GroupElement> groups = new ArrayList<>();
    for (GroupElement rootGroup : source.getContent().getModelRoot().getRootGroups()) {
      GroupElement copy = (GroupElement) copyOf(rootGroup);
      Deque<String> path = new ArrayDeque<>();
      path.addLast(source.getId());
      flattenIncludes(copy, path);
      groups.add(copy);
    }

    TypeDefinitionPlan typeDefinitions = planTypeDefinitions(groups);
    if (typeDefinitions.problem != Problem.NONE) {
      return Plan.failed(typeDefinitions.problem);
    }

    dropUnsupportedLocales(groups, typeDefinitions.newTypeDefinitions, localeCodes(target));
    return new Plan(Problem.NONE, groups, typeDefinitions.newTypeDefinitions, typeDefinitions.importReferences,
        List.copyOf(warnings));
  }

  // ---------------------------------------------------------------------------
  // Includes
  // ---------------------------------------------------------------------------

  private boolean includesTransitively(DocumentModel model, String modelId, Set<String> visited) {
    if (!visited.add(model.getId())) {
      return false;
    }
    for (Element element : allElements(model.getContent().getModelRoot().getRootGroups())) {
      String reference = includeReference(element);
      if (reference == null) {
        continue;
      }
      if (reference.equals(modelId)) {
        return true;
      }
      DocumentModel included = modelsById.get(reference);
      if (included != null && included.getContent() != null && included.getContent().getModelRoot() != null
          && includesTransitively(included, modelId, visited)) {
        return true;
      }
    }
    return false;
  }

  /** Replaces every flattenable Include below (and including) {@code group} with a group holding its content. */
  private void flattenIncludes(GroupElement group, Deque<String> modelPath) {
    GroupConfig config = group.getGroup();
    if (config == null) {
      return;
    }
    IncludeConfig includeConfig = config.getIncludeConfig();
    if (includeConfig != null) {
      String reference = includeConfig.getReference();
      DocumentModel included = reference == null ? null : modelsById.get(reference);
      if (included == null || included.getContent() == null || included.getContent().getModelRoot() == null) {
        warnings.add(new Warning(WarningKind.INCLUDE_MODEL_MISSING, group.getName(), reference));
        return;
      }
      if (modelPath.contains(included.getId())) {
        warnings.add(new Warning(WarningKind.INCLUDE_CYCLE, group.getName(), reference));
        return;
      }
      config.setIncludeConfig(null);
      config.getElements().clear();
      for (GroupElement rootGroup : included.getContent().getModelRoot().getRootGroups()) {
        if (rootGroup.getGroup() != null) {
          for (Element child : rootGroup.getGroup().getElements()) {
            config.getElements().add(copyOf(child));
          }
        }
      }
      modelPath.addLast(included.getId());
      flattenChildren(config, modelPath);
      modelPath.removeLast();
      return;
    }
    flattenChildren(config, modelPath);
  }

  private void flattenChildren(GroupConfig config, Deque<String> modelPath) {
    for (Element child : config.getElements()) {
      if (child instanceof GroupElement childGroup) {
        flattenIncludes(childGroup, modelPath);
      }
    }
  }

  private static String includeReference(Element element) {
    return element instanceof GroupElement group && group.getGroup() != null && group.getGroup().getIncludeConfig() != null
        ? group.getGroup().getIncludeConfig().getReference()
        : null;
  }

  private static Element copyOf(Element element) {
    return JsonSettings.objectMapper.readValue(JsonSettings.objectMapper.writeValueAsString(element), Element.class);
  }

  private static List<Element> allElements(List<? extends Element> roots) {
    List<Element> result = new ArrayList<>();
    for (Element root : roots) {
      collect(root, result::add);
    }
    return result;
  }

  private static void collect(Element element, Consumer<Element> sink) {
    sink.accept(element);
    if (element instanceof GroupElement group && group.getGroup() != null) {
      for (Element child : group.getGroup().getElements()) {
        collect(child, sink);
      }
    }
  }

  // ---------------------------------------------------------------------------
  // Type definitions
  // ---------------------------------------------------------------------------

  private static final class TypeDefinitionPlan {
    Problem problem = Problem.NONE;
    final List<TypeDefinition> newTypeDefinitions = new ArrayList<>();
    final List<ModelReference> importReferences = new ArrayList<>();
  }

  /** A type definition a copied field uses, and where it comes from. */
  private record UsedTypeDefinition(TypeDefinition typeDefinition, boolean ownedByTypeDefinitionModel,
                                    String ownerModelId) {
  }

  private TypeDefinitionPlan planTypeDefinitions(List<GroupElement> groups) {
    TypeDefinitionPlan result = new TypeDefinitionPlan();

    Set<String> usedIds = new LinkedHashSet<>();
    List<TypeDefFieldType> usages = new ArrayList<>();
    for (Element element : allElements(groups)) {
      if (element instanceof FieldElement fieldElement && fieldElement.getField() != null
          && fieldElement.getField().getFieldType() instanceof TypeDefFieldType typeDefFieldType
          && typeDefFieldType.getTypeDefType() != null && typeDefFieldType.getTypeDefType().getTypeDefinitionId() != null) {
        usedIds.add(typeDefFieldType.getTypeDefType().getTypeDefinitionId());
        usages.add(typeDefFieldType);
      }
    }
    if (usedIds.isEmpty()) {
      return result;
    }

    Map<String, UsedTypeDefinition> available = availableTypeDefinitions();
    List<UsedTypeDefinition> used = new ArrayList<>();
    for (String id : usedIds) {
      UsedTypeDefinition typeDefinition = available.get(id);
      if (typeDefinition == null) {
        warnings.add(new Warning(WarningKind.TYPE_DEFINITION_MISSING, id, null));
      }
      else {
        used.add(typeDefinition);
      }
    }
    if (used.isEmpty()) {
      return result;
    }

    TypeDefinitionMode targetMode = TypeDefinitionMode.modeOf(target);
    boolean copyLocally = used.stream().anyMatch(typeDefinition -> !typeDefinition.ownedByTypeDefinitionModel());
    if (copyLocally) {
      if (targetMode == TypeDefinitionMode.IMPORT) {
        result.problem = Problem.LOCAL_TYPE_DEFINITIONS_IN_IMPORT_MODE;
        return result;
      }
      copyTypeDefinitionsLocally(used, usages, result);
    }
    else {
      if (targetMode == TypeDefinitionMode.LOCAL) {
        result.problem = Problem.IMPORTS_IN_LOCAL_MODE;
        return result;
      }
      importTypeDefinitionModels(used, result);
    }
    return result;
  }

  /**
   * Every type definition the source sees by id: its own ones (owned by a regular Document Model) plus the ones
   * reached through its Includes and Imports, with the owner recorded so the caller can tell a Type Definition
   * Model's (importable) from a Document Model's (must be copied).
   */
  private Map<String, UsedTypeDefinition> availableTypeDefinitions() {
    Map<String, UsedTypeDefinition> available = new LinkedHashMap<>();
    if (source.getContent().getTypeDefinitions() != null) {
      for (TypeDefinition typeDefinition : source.getContent().getTypeDefinitions()) {
        available.putIfAbsent(typeDefinition.getId(), new UsedTypeDefinition(typeDefinition, false, source.getId()));
      }
    }
    for (TransitiveTypeDefinitions.Entry entry : TransitiveTypeDefinitions.resolve(source, new ArrayList<>(modelsById.values()))) {
      available.putIfAbsent(entry.typeDefinition().getId(),
          new UsedTypeDefinition(entry.typeDefinition(), entry.imported(), entry.ownerModelId()));
    }
    return available;
  }

  private void copyTypeDefinitionsLocally(List<UsedTypeDefinition> used, List<TypeDefFieldType> usages,
                                          TypeDefinitionPlan result) {
    Set<String> usedNames = new HashSet<>();
    if (target.getContent().getTypeDefinitions() != null) {
      for (TypeDefinition existing : target.getContent().getTypeDefinitions()) {
        usedNames.add(existing.getName());
      }
    }

    Map<String, String> newIdByOldId = new HashMap<>();
    for (UsedTypeDefinition usedTypeDefinition : used) {
      TypeDefinition original = usedTypeDefinition.typeDefinition();
      TypeDefinition copy = new TypeDefinition();
      copy.setId(TYPE_DEFINITION_ID_PREFIX + UUID.randomUUID());
      copy.setName(uniqueName(original.getName(), usedNames));
      copy.setFieldType(copyOf(original.getFieldType()));
      newIdByOldId.put(original.getId(), copy.getId());
      result.newTypeDefinitions.add(copy);
    }
    for (TypeDefFieldType usage : usages) {
      String newId = newIdByOldId.get(usage.getTypeDefType().getTypeDefinitionId());
      if (newId != null) {
        usage.getTypeDefType().setTypeDefinitionId(newId);
      }
    }
  }

  private static FieldType copyOf(FieldType fieldType) {
    return fieldType == null ? null
        : JsonSettings.objectMapper.readValue(JsonSettings.objectMapper.writeValueAsString(fieldType), FieldType.class);
  }

  private static String uniqueName(String baseName, Set<String> usedNames) {
    String base = baseName == null ? "Type" : baseName;
    String name = base;
    int suffix = 2;
    while (!usedNames.add(name)) {
      name = base + "_" + suffix++;
    }
    return name;
  }

  /**
   * Imports the Type Definition Models that own the used type definitions: the ones the target does not import
   * already (directly or transitively), and - since importing a model brings in what it imports - not one that
   * another needed model imports anyway.
   */
  private void importTypeDefinitionModels(List<UsedTypeDefinition> used, TypeDefinitionPlan result) {
    List<DocumentModel> all = new ArrayList<>(modelsById.values());
    Set<String> alreadyImported = TransitiveTypeDefinitions.importedModelIds(target, all);

    Set<String> needed = new LinkedHashSet<>();
    for (UsedTypeDefinition typeDefinition : used) {
      if (!alreadyImported.contains(typeDefinition.ownerModelId())) {
        needed.add(typeDefinition.ownerModelId());
      }
    }
    for (String modelId : needed) {
      boolean importedByAnotherNeeded = needed.stream().anyMatch(other -> !other.equals(modelId)
          && modelsById.get(other) != null
          && TransitiveTypeDefinitions.importedModelIds(modelsById.get(other), all).contains(modelId));
      if (importedByAnotherNeeded) {
        continue;
      }
      ModelReference reference = new ModelReference();
      reference.setAlias(modelId);
      reference.setModelType(ModelType.DOCUMENT);
      reference.setPurpose(ModelReference.PURPOSE_TYPE_DEFINITIONS);
      reference.setReference(modelId);
      result.importReferences.add(reference);
    }
  }

  // ---------------------------------------------------------------------------
  // Locales
  // ---------------------------------------------------------------------------

  private static Set<String> localeCodes(DocumentModel model) {
    Set<String> codes = new LinkedHashSet<>();
    if (model.getLocales() != null) {
      for (Locale locale : model.getLocales()) {
        if (locale.getCode() != null && !locale.getCode().isBlank()) {
          codes.add(locale.getCode());
        }
      }
    }
    return codes;
  }

  /**
   * Removes every label whose locale is not in {@code targetLocales} from the copied elements and type
   * definitions. A target without any locale keeps everything (nothing to sync against yet).
   */
  private static void dropUnsupportedLocales(List<GroupElement> groups, List<TypeDefinition> typeDefinitions,
                                             Set<String> targetLocales) {
    if (targetLocales.isEmpty()) {
      return;
    }
    Consumer<List<Label>> drop = labels -> {
      if (labels != null) {
        labels.removeIf(label -> label.getLocale() != null && !targetLocales.contains(label.getLocale()));
      }
    };
    for (Element element : allElements(groups)) {
      forEachLabelList(element, drop);
    }
    for (TypeDefinition typeDefinition : typeDefinitions) {
      forEachLabelList(typeDefinition.getFieldType(), drop);
    }
  }

  private static void forEachLabelList(Element element, Consumer<List<Label>> action) {
    action.accept(element.getExternalDescription());
    action.accept(element.getInternalDescription());
    if (element instanceof GroupElement group && group.getGroup() != null) {
      action.accept(group.getGroup().getLabel());
    }
    else if (element instanceof FieldElement field && field.getField() != null) {
      FieldConfig config = field.getField();
      action.accept(config.getLabel());
      action.accept(config.getHelperText());
      if (config.getRequirednessConfig() != null) {
        action.accept(config.getRequirednessConfig().getErrorMessage());
      }
      forEachLabelList(config.getFieldType(), action);
    }
    else if (element instanceof RuleElement rule && rule.getRule() != null) {
      action.accept(rule.getRule().getErrorMessage());
    }
    else if (element instanceof ComputationElement computation && computation.getComputation() != null) {
      action.accept(computation.getComputation().getErrorMessage());
    }
  }

  private static void forEachLabelList(FieldType fieldType, Consumer<List<Label>> action) {
    if (fieldType instanceof StringFieldType string && string.getStringType() != null) {
      action.accept(string.getStringType().getErrorMessage());
    }
    else if (fieldType instanceof EnumerationFieldType enumeration && enumeration.getEnumerationType() != null) {
      action.accept(enumeration.getEnumerationType().getErrorMessage());
      for (EnumerationValue value : enumeration.getEnumerationType().getValues()) {
        action.accept(value.getLabel());
      }
    }
  }
}
