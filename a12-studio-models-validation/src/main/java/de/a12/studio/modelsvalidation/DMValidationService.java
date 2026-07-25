package de.a12.studio.modelsvalidation;

import de.a12.studio.models.ModelReference;
import de.a12.studio.models.documentmodel.ComputationElement;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.documentmodel.DocumentModelContent;
import de.a12.studio.models.documentmodel.Element;
import de.a12.studio.models.documentmodel.EnumerationFieldType;
import de.a12.studio.models.documentmodel.EnumerationValue;
import de.a12.studio.models.documentmodel.FieldElement;
import de.a12.studio.models.documentmodel.FieldType;
import de.a12.studio.models.documentmodel.GroupElement;
import de.a12.studio.models.documentmodel.ModelConfig;
import de.a12.studio.models.documentmodel.TypeDefFieldType;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class DMValidationService {

  // The only two time zones a document model's settings can be configured with (see TimezonePanelController);
  // every document model in a project must agree on one of them, mirroring SME's TimeZoneCheck rule.
  private static final String EUROPE_BERLIN = "Europe/Berlin";

  private static final String TIME_ZONE_MISMATCH_MESSAGE =
      "There is a document model with time zone Europe/Berlin in this workspace. This model's time zone needs to be Europe/Berlin as well.";

  private static final String INCLUDE_PURPOSE = "include";

  // Verbatim text of SME's DomainGroup meta-model rule "A12_INDEX_FIELD_INVALID_REFERENCE" (see
  // client/resources/models/documentModel/DomainGroup.json in the SME repo), which fires the moment a group's
  // configured index field no longer resolves to a field in the group (e.g. that field was deleted). Kept
  // verbatim, unlike the other generic messages below, so the Group properties panel can show users the exact
  // wording they already know from SME.
  public static final String INDEX_FIELD_INVALID_MESSAGE = "The index field is not a valid field.";

  /**
   * UI-safe entry point: takes/returns only data-services model types and plain strings.
   */
  public Optional<ElementValidationError> validateElement(
      DocumentModel documentModel, String elementId, List<DocumentModel> otherDocumentModels) {
    return validateDocument(documentModel, otherDocumentModels).stream()
        .filter(error -> error.elementId().equals(elementId))
        .findFirst();
  }

  /**
   * UI-safe entry point: validates every element of a document model, e.g. for whole-project validation
   * where there's no single element to check (see {@link #validateElement} for that narrower case).
   */
  public List<ElementValidationError> validateDocument(DocumentModel documentModel, List<DocumentModel> otherDocumentModels) {
    return validate(documentModel, otherDocumentModels).stream()
        .filter(problem -> problem.elementId() != null)
        .map(problem -> new ElementValidationError(problem.elementId(), problem.message(), problem.severity().name()))
        .toList();
  }

  /**
   * UI-safe entry point: every human-readable settings problem for this model (locales, time zone, etc., as
   * edited via the Model Settings dialog), whether single-model (missing locale) or cross-model (e.g. a time
   * zone that disagrees with the rest of the project, see {@link #getTimeZoneMismatchError}). Empty if there
   * are none, e.g. for driving both a settings-button badge and its error tooltip.
   */
  public List<String> getSettingsIssueMessages(DocumentModel documentModel, List<DocumentModel> otherDocumentModels) {
    List<String> messages = new ArrayList<>();
    getTimeZoneMismatchError(documentModel, otherDocumentModels).ifPresent(messages::add);
    getMissingLocaleError(documentModel).ifPresent(messages::add);
    return messages;
  }

  /**
   * UI-safe entry point for the Locales settings panel: at least one locale is required.
   */
  public Optional<String> getMissingLocaleError(DocumentModel documentModel) {
    boolean missing = documentModel.getLocales() == null || documentModel.getLocales().isEmpty();
    return missing ? Optional.of("Please add at least one locale.") : Optional.empty();
  }

  /**
   * UI-safe entry point for the Timezone settings panel: every document model in a project must use the
   * same time zone. Mirrors SME's {@code TimeZoneCheck} custom validation rule.
   */
  public Optional<String> getTimeZoneMismatchError(DocumentModel documentModel, List<DocumentModel> otherDocumentModels) {
    String timeZone = getTimeZone(documentModel);
    if (EUROPE_BERLIN.equals(timeZone)) {
      return Optional.empty();
    }
    boolean otherModelUsesEuropeBerlin = otherDocumentModels.stream().anyMatch(other -> EUROPE_BERLIN.equals(getTimeZone(other)));
    return otherModelUsesEuropeBerlin ? Optional.of(TIME_ZONE_MISMATCH_MESSAGE) : Optional.empty();
  }

  private static String getTimeZone(DocumentModel documentModel) {
    DocumentModelContent content = documentModel.getContent();
    ModelConfig modelConfig = content != null ? content.getModelConfig() : null;
    return modelConfig != null ? modelConfig.getTimeZone() : null;
  }

  /**
   * Combines this service's own structural checks (below) with a clean-room port of the a12 kernel's
   * consistency rules ({@link DocumentModelConsistencyRules}). A single bad reference elsewhere in the model
   * shouldn't hide every other problem, so a failure in the ported kernel rules falls back to just the
   * structural checks rather than surfacing nothing.
   */
  List<ValidationProblem> validate(DocumentModel model, List<DocumentModel> otherModels) {
    List<ValidationProblem> structuralProblems = checkMissingErrors(model, otherModels);
    try {
      List<ValidationProblem> combined = new ArrayList<>(structuralProblems);
      combined.addAll(DocumentModelConsistencyRules.checkAll(model, new ElementIndex(model)));
      return combined;
    } catch (Exception e) {
      return structuralProblems;
    }
  }

  /**
   * Checks for errors the ported kernel rules don't cover: missing/unresolved references between elements.
   * We use simple generic error messages instead of the kernel's own wording so that changes to that wording
   * don't need to be integrated into this code — except {@link #INDEX_FIELD_INVALID_MESSAGE}, which is shown
   * directly in the Group properties panel and is kept verbatim on purpose.
   */
  private static List<ValidationProblem> checkMissingErrors(DocumentModel model, List<DocumentModel> otherModels) {
    ElementIndex index = new ElementIndex(model);
    List<ValidationProblem> result = new ArrayList<>();
    for (Element element : index.allElements()) {
      if (element instanceof GroupElement groupElement && groupElement.getGroup() != null) {
        String groupPath = index.getPath(groupElement);
        if (hasMissingIncludeReference(groupElement, model, otherModels)) {
          result.add(new ValidationProblem(
              groupElement.getId(), "Include with path '" + groupPath + "': Missing Include Reference", Severity.ERROR));
        }
        if (hasMissingIndexField(groupElement, index)) {
          result.add(new ValidationProblem(groupElement.getId(), INDEX_FIELD_INVALID_MESSAGE, Severity.ERROR));
        }
        for (Element duplicate : getElementsWithDuplicatedNames(groupElement)) {
          result.add(new ValidationProblem(
              duplicate.getId(), "Element with path '" + groupPath + "': Multiple Elements with same path", Severity.ERROR));
        }
      } else if (element instanceof ComputationElement computation) {
        if (hasMissingComputedField(computation, index)) {
          String path = index.getPath(computation);
          result.add(new ValidationProblem(computation.getId(), "Computation with path '" + path + "': Missing Computed Field", Severity.ERROR));
        }
      } else if (element instanceof FieldElement field) {
        String path = index.getPath(field);
        if (hasTooFewEnumValues(field, index)) {
          result.add(new ValidationProblem(
              field.getId(), "Field with path '" + path + "': Enumeration must have at least two values", Severity.ERROR));
        }
        if (hasMissingTypeDef(field, index)) {
          result.add(new ValidationProblem(field.getId(), "Field with path '" + path + "': Missing Type Definition", Severity.ERROR));
        }
      }
    }
    return result;
  }

  private static Optional<ModelReference> findIncludeReference(DocumentModel model, String alias) {
    if (model.getModelReferences() == null) {
      return Optional.empty();
    }
    return model.getModelReferences().stream()
        .filter(r -> INCLUDE_PURPOSE.equals(r.getPurpose()) && alias.equals(r.getAlias()))
        .findFirst();
  }

  private static boolean hasMissingIncludeReference(GroupElement groupElement, DocumentModel model, List<DocumentModel> otherModels) {
    String alias = groupElement.getGroup().getModelAlias();
    if (alias == null || alias.isBlank()) {
      return false;
    }
    Optional<ModelReference> reference = findIncludeReference(model, alias);
    return reference.isEmpty() || resolveOtherModel(reference.get().getReference(), otherModels) == null;
  }

  /** Mirrors the strip-path-and-.json-suffix resolution the a12 kernel's reference resolver used. */
  private static DocumentModel resolveOtherModel(String reference, List<DocumentModel> otherModels) {
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

  private static boolean hasMissingIndexField(GroupElement groupElement, ElementIndex index) {
    String indexFieldName = groupElement.getGroup().getIndexFieldName();
    if (indexFieldName == null || indexFieldName.isBlank()) {
      return false;
    }
    return index.resolveRelativePath(groupElement, indexFieldName).filter(ElementIndex::isField).isEmpty();
  }

  private static Set<Element> getElementsWithDuplicatedNames(GroupElement groupElement) {
    List<Element> elements = groupElement.getGroup().getElements();
    if (elements == null) {
      return Set.of();
    }
    Set<Element> result = new LinkedHashSet<>();
    elements.stream().collect(Collectors.groupingBy(Element::getName)).values().stream()
        .filter(group -> group.size() > 1)
        .forEach(result::addAll);
    return result;
  }

  private static boolean hasMissingComputedField(ComputationElement computation, ElementIndex index) {
    String relPath = computation.getComputation() == null ? null : computation.getComputation().getComputedFieldRelPath();
    if (relPath == null || relPath.isBlank()) {
      return true;
    }
    return index.resolveRelativePath(computation, relPath).filter(ElementIndex::isField).isEmpty();
  }

  private static boolean hasTooFewEnumValues(FieldElement field, ElementIndex index) {
    GroupElement parent = index.parentOf(field);
    if (parent == null || parent.getGroup() == null || !"multi-select".equals(parent.getGroup().getUsageType())) {
      return false;
    }
    List<EnumerationValue> enumValues = getEnumValues(field, index);
    return enumValues != null && enumValues.size() < 2;
  }

  private static List<EnumerationValue> getEnumValues(FieldElement field, ElementIndex index) {
    if (field.getField() == null) {
      return null;
    }
    FieldType effectiveType = index.effectiveFieldType(field.getField().getFieldType());
    if (effectiveType instanceof EnumerationFieldType enumType && enumType.getEnumerationType() != null) {
      return enumType.getEnumerationType().getValues();
    }
    return null;
  }

  private static boolean hasMissingTypeDef(FieldElement field, ElementIndex index) {
    if (field.getField() == null || !(field.getField().getFieldType() instanceof TypeDefFieldType typeDefFieldType)) {
      return false;
    }
    String typeDefId = typeDefFieldType.getTypeDefType() == null ? null : typeDefFieldType.getTypeDefType().getTypeDefinitionId();
    if (typeDefId == null || typeDefId.isBlank()) {
      return true;
    }
    return index.effectiveFieldType(typeDefFieldType) == null;
  }
}
