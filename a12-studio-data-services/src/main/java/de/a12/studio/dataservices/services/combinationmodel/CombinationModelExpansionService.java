package de.a12.studio.dataservices.services.combinationmodel;

import com.fasterxml.jackson.databind.JsonNode;
import com.mgmtp.a12.kernel.md.combination.a12internal.CombinationModelService;
import com.mgmtp.a12.kernel.md.combination.a12internal.UnexpandedModelResolverImpl;
import com.mgmtp.a12.kernel.md.model.a12internal.services.DocumentModelService;
import com.mgmtp.a12.kernel.md.model.api.IDocumentModel;
import com.mgmtp.a12.kernel.mmtypings.mm_combinationmodel_1.views.MM_CombinationModel_1;
import com.mgmtp.a12.model.header.DefaultHeaderParser;
import com.mgmtp.a12.model.notification.RankedNotification;
import de.a12.studio.dataservices.services.combinationmodel.exceptions.CombinationModelNotFoundException;
import de.a12.studio.dataservices.services.combinationmodel.exceptions.ModelNotFoundException;
import de.a12.studio.dataservices.services.documentmodel.features.expansion.TypeDefInfo;
import de.a12.studio.dataservices.services.support.DocumentModelSupport;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public class CombinationModelExpansionService {

  public CombModelExpansionResultDto expand(CombModelRequestDto dto) {
    List<ExpansionError> errors = new ArrayList<>();
    Consumer<RankedNotification> notificationReceiver =
        notification -> errors.add(new ExpansionError(notification.getMessage(), notification.getSeverity().toString()));

    var modelResolver = createUnexpandedModelResolver(dto.getCombinationModels(), dto.getDocumentModels(), dto.getSelectionModels());

    MM_CombinationModel_1 combModel = getCombModel(dto.getModelId(), dto.getCombinationModels(), notificationReceiver);

    Optional<IDocumentModel> expandedCombModelOpt =
        CombinationModelService.expand(combModel, modelResolver, Locale.US, notificationReceiver);
    if (expandedCombModelOpt.isEmpty()) {
      return new CombModelExpansionResultDto(null, null, errors);
    }
    var expandedCombModel = expandedCombModelOpt.get();

    var withCleanedRefs = CombinationModelService.removeAdditiveAndSelectionModelReferences(expandedCombModel);
    var dm = DocumentModelSupport.enrichWithMetaData(new DocumentModelService().convertFromExternal(withCleanedRefs));

    return new CombModelExpansionResultDto(
        DocumentModelSupport.toJson(DocumentModelSupport.serialize(dm)),
        new TypeDefInfo(
            DocumentModelSupport.includedTypeDefinitions(dm.getContent().getTypeDefinitions()),
            DocumentModelSupport.importedTypeDefinitions(dm.getContent().getTypeDefinitions()),
            DocumentModelSupport.includedImportedTypeDefinitions(dm.getContent().getTypeDefinitions())),
        errors);
  }

  private MM_CombinationModel_1 getCombModel(String id, List<JsonNode> combModels, Consumer<RankedNotification> notificationReceiver) {
    JsonNode combModel =
        combModels.stream().filter(m -> parseId(m).equals(id)).findFirst()
            .orElseThrow(() -> new CombinationModelNotFoundException(id));
    return CombinationModelService.deserialize(combModel, notificationReceiver);
  }

  private UnexpandedModelResolverImpl createUnexpandedModelResolver(
      List<JsonNode> combinationModels, List<JsonNode> documentModels, List<JsonNode> selectionModels) {
    Map<String, JsonNode> modelMap = new HashMap<>();
    for (JsonNode model : combinationModels) {
      modelMap.put(parseId(model), model);
    }
    for (JsonNode model : documentModels) {
      modelMap.put(parseId(model), model);
    }
    for (JsonNode model : selectionModels) {
      modelMap.put(parseId(model), model);
    }

    return new UnexpandedModelResolverImpl(modelId -> {
      JsonNode model = modelMap.get(modelId);
      if (model == null) {
        throw new ModelNotFoundException(modelId);
      }
      return new StringReader(model.toString());
    });
  }

  private static String parseId(JsonNode model) {
    try {
      return new DefaultHeaderParser().parseJson(model.toString()).getId();
    } catch (com.mgmtp.a12.model.header.HeaderParseException e) {
      throw new RuntimeException(e);
    }
  }
}
