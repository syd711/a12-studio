package de.a12.studio.dataservices.services.support.additivemodel;

import com.mgmtp.a12.kernel.core.tool.a12internal.api.error.IProblemReporter;
import com.mgmtp.a12.kernel.md.model.a12internal.DocumentModel;
import com.mgmtp.a12.kernel.md.model.a12internal.Element;
import com.mgmtp.a12.kernel.md.model.a12internal.services.DocumentModelSearchService;
import com.mgmtp.a12.kernel.md.model.a12internal.services.join.DocumentModelJoiningService;
import com.mgmtp.a12.kernel.md.model.a12internal.services.join.addition.AdditionOrigins;
import com.mgmtp.a12.kernel.md.model.a12internal.services.join.addition.IAdditionOrigins;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class AdditiveModelSupport {

  private AdditiveModelSupport() {
  }

  public static Optional<DocumentModel> doJoinWithAdm(
      DocumentModel referenceDocumentModel,
      DocumentModel additiveDocumentModel,
      Locale locale,
      IProblemReporter problemReporter,
      IProblemReporter criticalIssuesReporter,
      IAdditionOrigins additionOrigins) {
    DocumentModelJoiningService service = DocumentModelJoiningService.INSTANCE;
    return service.join(referenceDocumentModel, additiveDocumentModel, locale, problemReporter, criticalIssuesReporter, additionOrigins);
  }

  public static Map<String, String> elementOriginsAsMap(AdditionOrigins origins) {
    Map<String, String> elementOrigins = new HashMap<>();
    for (String id : origins.getElementsInRoot()) {
      elementOrigins.put(id, "reference");
    }
    for (String id : origins.getElementsInBoth()) {
      elementOrigins.put(id, "overwritten");
    }
    for (String id : origins.getElementsInAdditive()) {
      elementOrigins.put(id, null);
    }
    return elementOrigins;
  }

  public static void restoreIds(
      Map<String, String> elementOrigins,
      AdditionOrigins additionOrigins,
      AdditionExpansionContextData expansionContext,
      DocumentModel joinedModel) {
    Map<String, String> contextIdsInJoinedByIdInAdditive = new HashMap<>();
    expansionContext.getIdsInAdditiveByIdInJoined().forEach((key, value) -> contextIdsInJoinedByIdInAdditive.put(value, key));
    Map<String, String> contextIdsInJoinedByIdInReference = new HashMap<>();
    expansionContext.getIdsInReferenceByIdInJoined().forEach((key, value) -> contextIdsInJoinedByIdInReference.put(value, key));

    DocumentModelSearchService searchService = new DocumentModelSearchService(joinedModel);
    Set<String> copiedElementsInBoth = new java.util.HashSet<>(additionOrigins.getElementsInBoth());
    Set<String> copiedElementsInRoot = new java.util.HashSet<>(additionOrigins.getElementsInRoot());

    for (Map.Entry<String, String> entry : elementOrigins.entrySet()) {
      String id = entry.getKey();
      String origin = entry.getValue();
      Optional<Element> element = searchService.getById(id);
      if (element.isEmpty()) {
        throw new IllegalStateException("Element not found: " + id);
      }
      String originalId =
          getOriginalId(element.get(), origin, additionOrigins, contextIdsInJoinedByIdInAdditive, contextIdsInJoinedByIdInReference);
      updateElementSets(copiedElementsInBoth, copiedElementsInRoot, origin, element.get().getId(), originalId);
      element.get().setId(originalId);
    }
    updateAdditionOrigins(additionOrigins, copiedElementsInBoth, copiedElementsInRoot);
  }

  public static void updateElementSets(
      Set<String> elementsInBoth, Set<String> elementsInRoot, String origin, String oldId, String newId) {
    if ("overwritten".equals(origin)) {
      elementsInBoth.remove(oldId);
      elementsInBoth.add(newId);
    }
    if ("reference".equals(origin)) {
      elementsInRoot.remove(oldId);
      elementsInRoot.add(newId);
    }
  }

  public static void updateAdditionOrigins(AdditionOrigins additionOrigins, Set<String> newElementsInBoth, Set<String> newElementsInRoot) {
    additionOrigins.getElementsInBoth().clear();
    additionOrigins.getElementsInRoot().clear();
    additionOrigins.getElementsInBoth().addAll(newElementsInBoth);
    additionOrigins.getElementsInRoot().addAll(newElementsInRoot);
  }

  public static String getOriginalId(
      Element element,
      String origin,
      AdditionOrigins additionOrigins,
      Map<String, String> contextIdsInJoinedByIdInAdditive,
      Map<String, String> contextIdsInJoinedByIdInReference) {
    if ("reference".equals(origin)) {
      return contextIdsInJoinedByIdInReference.getOrDefault(element.getId(), element.getId());
    } else {
      // ID from Additive Model
      String additiveId = additionOrigins.getIdsInAdditiveByIdInJoined().get(element.getId());
      if (additiveId != null) {
        // ID from previous joined Model
        return contextIdsInJoinedByIdInAdditive.getOrDefault(additiveId, additiveId);
      }
      return element.getId();
    }
  }

  @Getter
  public static class AdditionExpansionContextData {
    private final Map<String, String> idsInAdditiveByIdInJoined;
    private final Map<String, String> idsInReferenceByIdInJoined;

    @JsonCreator
    public AdditionExpansionContextData(
        @JsonProperty("idsInAdditiveByIdInJoined") Map<String, String> idsInAdditiveByIdInJoined,
        @JsonProperty("idsInReferenceByIdInJoined") Map<String, String> idsInReferenceByIdInJoined) {
      this.idsInAdditiveByIdInJoined = idsInAdditiveByIdInJoined;
      this.idsInReferenceByIdInJoined = idsInReferenceByIdInJoined;
    }
  }
}
