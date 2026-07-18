package de.a12.studio.dataservices.services.documentmodel.features.validation;

import com.mgmtp.a12.kernel.md.model.a12internal.Computation;
import com.mgmtp.a12.kernel.md.model.a12internal.DocumentModel;
import com.mgmtp.a12.kernel.md.model.a12internal.Element;
import com.mgmtp.a12.kernel.md.model.a12internal.Field;
import com.mgmtp.a12.kernel.md.model.a12internal.Group;
import com.mgmtp.a12.kernel.md.model.a12internal.fieldtypes.EnumerationType;
import com.mgmtp.a12.kernel.md.model.a12internal.fieldtypes.TypeDefType;
import com.mgmtp.a12.kernel.md.model.a12internal.services.DocumentModelReferenceResolver;
import com.mgmtp.a12.kernel.md.model.a12internal.services.DocumentModelService;
import com.mgmtp.a12.kernel.md.model.a12internal.visitor.DocumentModelVisitor;
import com.mgmtp.a12.kernel.md.model.a12internal.visitor.DocumentModelWalker;
import com.mgmtp.a12.kernel.md.model.api.visitor.DocumentModelWalker.VisitProcess;
import com.mgmtp.a12.model.notification.Severity;
import de.a12.studio.dataservices.util.JsonSettings;
import de.a12.studio.dataservices.services.support.DocumentModelSupport;
import de.a12.studio.dataservices.services.support.InMemoryDocumentModelReferenceResolver;

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

  /**
   * UI-safe entry point: takes/returns only data-services model types and plain strings, so callers that
   * don't have the kernel jars on their classpath (e.g. a12-studio-ui) can still trigger validation.
   */
  public Optional<ElementValidationError> validateElement(
      de.a12.studio.dataservices.models.documentmodel.DocumentModel documentModel,
      String elementId,
      List<de.a12.studio.dataservices.models.documentmodel.DocumentModel> otherDocumentModels) {
    return validateDocument(documentModel, otherDocumentModels).stream()
        .filter(error -> error.elementId().equals(elementId))
        .findFirst();
  }

  /**
   * UI-safe entry point: validates every element of a document model, e.g. for whole-project validation
   * where there's no single element to check (see {@link #validateElement} for that narrower case).
   */
  public List<ElementValidationError> validateDocument(
      de.a12.studio.dataservices.models.documentmodel.DocumentModel documentModel,
      List<de.a12.studio.dataservices.models.documentmodel.DocumentModel> otherDocumentModels) {
    DocumentModel model = DocumentModelSupport.deserialize(JsonSettings.objectMapper.writeValueAsString(documentModel));
    List<DocumentModel> otherModels = otherDocumentModels.stream()
        .map(other -> DocumentModelSupport.deserialize(JsonSettings.objectMapper.writeValueAsString(other)))
        .toList();
    return validate(model, otherModels).stream()
        .map(error -> new ElementValidationError(error.getId(), error.getMessage(), error.getSeverity().name()))
        .toList();
  }

  /**
   * UI-safe entry point: every human-readable settings problem for this model (locales, time zone, etc., as
   * edited via the Model Settings dialog), whether kernel-reported (single model) or cross-model (e.g. a
   * time zone that disagrees with the rest of the project, see {@link #getTimeZoneMismatchError}). Empty if
   * there are none, e.g. for driving both a settings-button badge and its error tooltip. Exceptions from the
   * kernel consistency check (e.g. on a model that's mid-edit and momentarily inconsistent) are treated as
   * "no issues" rather than surfaced, matching {@link #validate} below.
   */
  public List<String> getSettingsIssueMessages(
      de.a12.studio.dataservices.models.documentmodel.DocumentModel documentModel,
      List<de.a12.studio.dataservices.models.documentmodel.DocumentModel> otherDocumentModels) {
    List<String> messages = new ArrayList<>();
    getTimeZoneMismatchError(documentModel, otherDocumentModels).ifPresent(messages::add);
    getMissingLocaleError(documentModel).ifPresent(messages::add);
    return messages;
  }

  /**
   * UI-safe entry point for the Locales settings panel: the kernel's "at least one locale" consistency
   * problem, reworded for end users (the kernel's own message embeds an internal error code).
   */
  public Optional<String> getMissingLocaleError(de.a12.studio.dataservices.models.documentmodel.DocumentModel documentModel) {
    try {
      DocumentModel model = DocumentModelSupport.deserialize(JsonSettings.objectMapper.writeValueAsString(documentModel));
      return DocumentModelSupport.getSettingsProblems(model).stream()
          .filter(p -> p.getMessage().contains("MVK_SUPP_LANGUAGES_MISSING"))
          .findFirst()
          .map(p -> "Please add at least one locale.");
    } catch (Exception e) {
      return Optional.empty();
    }
  }

  /**
   * UI-safe entry point for the Timezone settings panel: every document model in a project must use the
   * same time zone. Mirrors SME's {@code TimeZoneCheck} custom validation rule, which is not part of the
   * kernel's own single-model consistency check (see {@link DocumentModelSupport#getSettingsProblems}) since
   * it depends on the other document models in the project.
   */
  public Optional<String> getTimeZoneMismatchError(
      de.a12.studio.dataservices.models.documentmodel.DocumentModel documentModel,
      List<de.a12.studio.dataservices.models.documentmodel.DocumentModel> otherDocumentModels) {
    String timeZone = getTimeZone(documentModel);
    if (EUROPE_BERLIN.equals(timeZone)) {
      return Optional.empty();
    }
    boolean otherModelUsesEuropeBerlin = otherDocumentModels.stream()
        .anyMatch(other -> EUROPE_BERLIN.equals(getTimeZone(other)));
    return otherModelUsesEuropeBerlin ? Optional.of(TIME_ZONE_MISMATCH_MESSAGE) : Optional.empty();
  }

  private static String getTimeZone(de.a12.studio.dataservices.models.documentmodel.DocumentModel documentModel) {
    de.a12.studio.dataservices.models.documentmodel.DocumentModelContent content = documentModel.getContent();
    de.a12.studio.dataservices.models.documentmodel.ModelConfig modelConfig = content != null ? content.getModelConfig() : null;
    return modelConfig != null ? modelConfig.getTimeZone() : null;
  }

  public List<DocumentModelErrors> validate(DocumentModel model, List<DocumentModel> otherModels) {
    List<DocumentModelErrors> elementErrorsThatKernelDoesNotFind = checkMissingErrors(model, otherModels);
    try {
      List<DocumentModelErrors> kernelElementErrors =
          DocumentModelSupport.getElementProblems(model).stream()
              .map(p -> new DocumentModelErrors(((Element) p.getSource()).getId(), p.getMessage(), p.getSeverity()))
              .toList();
      List<DocumentModelErrors> combined = new ArrayList<>(elementErrorsThatKernelDoesNotFind);
      combined.addAll(kernelElementErrors);
      return combined;
    } catch (Exception e) {
      return elementErrorsThatKernelDoesNotFind;
    }
  }

  /**
   * Checks for Errors that the Kernel validation does not find.
   * We use simple generic Error Messages instead of the Document Model ValidationRule Error Messages so that changes in
   * the Validation Error Messages do not need to be integrated into this code.
   */
  private static List<DocumentModelErrors> checkMissingErrors(DocumentModel model, List<DocumentModel> otherModels) {
    DocumentModelService documentModelService = new DocumentModelService();
    List<DocumentModelErrors> result = new ArrayList<>();
    List<Element> unfixableElements = new ArrayList<>();
    DocumentModelReferenceResolver resolver = new InMemoryDocumentModelReferenceResolver(otherModels);
    DocumentModelVisitor visitor =
        new DocumentModelVisitor() {
          @Override
          public VisitProcess visitGroup(Group group) {
            String groupPath = documentModelService.getPath(group);
            if (hasMissingIncludeReference(group, resolver)) {
              result.add(
                  new DocumentModelErrors(group.getId(), "Include with path '" + groupPath + "': Missing Include Reference", Severity.ERROR));
              unfixableElements.add(group);
            }
            if (hasMissingIndexField(group)) {
              String elementType = DocumentModelSupport.isInclude(group) ? "Include" : "Group";
              result.add(
                  new DocumentModelErrors(group.getId(), elementType + " with path '" + groupPath + "': Missing Index Field", Severity.ERROR));
            }
            Set<Element> duplicatedElements = getElementsWithDuplicatedNames(group);
            for (Element element : duplicatedElements) {
              String elementPath = documentModelService.getPath(group);
              result.add(
                  new DocumentModelErrors(
                      element.getId(), "Element with path '" + elementPath + "': Multiple Elements with same path", Severity.ERROR));
            }
            return VisitProcess.CONTINUE_TRAVERSAL;
          }

          @Override
          public VisitProcess visitComputation(Computation computation) {
            if (hasMissingComputedField(computation)) {
              String path = documentModelService.getPath(computation);
              result.add(
                  new DocumentModelErrors(computation.getId(), "Computation with path '" + path + "': Missing Computed Field", Severity.ERROR));
              unfixableElements.add(computation);
            }
            return VisitProcess.CONTINUE_TRAVERSAL;
          }

          @Override
          public VisitProcess visitField(Field field) {
            String path = documentModelService.getPath(field);
            if (hasTooFewEnumValues(field)) {
              result.add(
                  new DocumentModelErrors(
                      field.getId(), "Field with path '" + path + "': Enumeration must have at least two values", Severity.ERROR));
            }
            if (hasMissingTypeDef(field)) {
              result.add(new DocumentModelErrors(field.getId(), "Field with path '" + path + "': Missing Type Definition", Severity.ERROR));
              unfixableElements.add(field);
            }
            return VisitProcess.CONTINUE_TRAVERSAL;
          }
        };

    new DocumentModelWalker().acceptDocumentModel(model, visitor);
    for (Element element : unfixableElements) {
      element.getParent().removeElement(element);
    }
    return result;
  }

  private static boolean hasMissingIncludeReference(Group group, DocumentModelReferenceResolver resolver) {
    return DocumentModelSupport.isInclude(group)
        && resolver.getDocumentModel(group.getIncludeDetails().get().getModelReference().getReference()) == null;
  }

  private static Set<Element> getElementsWithDuplicatedNames(Group group) {
    Set<Element> result = new LinkedHashSet<>();
    var nameMap = group.getElements().stream().collect(Collectors.groupingBy(Element::getName));
    nameMap.values().stream().filter(v -> v.size() > 1).forEach(result::addAll);
    return result;
  }

  private static boolean hasMissingIndexField(Group group) {
    boolean indexFieldUndefined = group.getIndexField().isPresent() && group.getIndexField().get().getDocumentModelObject().isEmpty();
    if (indexFieldUndefined) {
      group.setIndexField(null);
    }
    return indexFieldUndefined;
  }

  private static boolean hasMissingComputedField(Computation computation) {
    return computation.getComputedField().getDocumentModelObject().isEmpty();
  }

  private static boolean hasMissingTypeDef(Field field) {
    return field.getFieldType() instanceof TypeDefType typeDefType && typeDefType.getTypeDefinition().isEmpty();
  }

  private static boolean hasTooFewEnumValues(Field field) {
    if ("multi-select".equals(field.getParent().getUsageType().orElse(null))) {
      var enumValues = getEnumValues(field);
      return enumValues != null && enumValues.size() < 2;
    }
    return false;
  }

  private static List<EnumerationType.EnumValue> getEnumValues(Field field) {
    if (field.getFieldType() instanceof EnumerationType enumerationType) {
      return enumerationType.getValues();
    } else if (field.getFieldType() instanceof TypeDefType typeDefType
        && typeDefType.getTypeDefinition().isPresent()
        && typeDefType.getTypeDefinition().get().getFieldType() instanceof EnumerationType referencedEnumType) {
      return referencedEnumType.getValues();
    } else {
      return null;
    }
  }
}
