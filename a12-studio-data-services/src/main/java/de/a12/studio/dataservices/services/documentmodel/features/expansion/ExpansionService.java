package de.a12.studio.dataservices.services.documentmodel.features.expansion;

import com.fasterxml.jackson.databind.JsonNode;
import com.mgmtp.a12.kernel.md.model.a12internal.DocumentModel;
import com.mgmtp.a12.kernel.md.model.a12internal.services.join.addition.AdditionOrigins;
import com.mgmtp.a12.kernel.md.model.api.services.DocumentModelExpansionException;
import de.a12.studio.dataservices.services.support.DocumentModelSupport;
import de.a12.studio.dataservices.services.support.ProblemReporter;

import java.util.Collections;
import java.util.List;

public class ExpansionService {

  public ExpandedDocumentModelResultDto expand(List<JsonNode> jsonDocumentModels, String documentModelId, JsonNode contextData) {
    List<DocumentModel> documentModels =
        jsonDocumentModels.stream().map(JsonNode::toString).map(DocumentModelSupport::deserialize).toList();
    DocumentModel documentModel =
        documentModels.stream().filter(dm -> dm.getHeader().getId().equals(documentModelId)).findFirst().orElse(null);
    if (documentModel == null) {
      throw new IllegalStateException("Cannot expand Document Model with id '" + documentModelId + "': Model not found");
    }
    AdditionOrigins additionOrigins = new AdditionOrigins();
    DocumentModel expanded =
        DocumentModelSupport.expand(documentModelId, documentModels, new ProblemReporter(), additionOrigins, contextData);
    return new ExpandedDocumentModelResultDto(
        DocumentModelSupport.toJson(DocumentModelSupport.serialize(DocumentModelSupport.enrichWithMetaData(expanded))),
        new TypeDefInfo(
            DocumentModelSupport.includedTypeDefinitions(expanded.getContent().getTypeDefinitions()),
            DocumentModelSupport.importedTypeDefinitions(expanded.getContent().getTypeDefinitions()),
            DocumentModelSupport.includedImportedTypeDefinitions(expanded.getContent().getTypeDefinitions()),
            List.copyOf(additionOrigins.getTypeDefsInBoth()),
            List.copyOf(additionOrigins.getTypeDefsInRoot())),
        new ElementInfo(List.copyOf(additionOrigins.getElementsInBoth()), List.copyOf(additionOrigins.getElementsInRoot())),
        additionOrigins.getCommonElementsWithJoinedAnnotations());
  }

  public ExpandedDocumentModelResultDto expand(List<JsonNode> jsonDocumentModels, String documentModelId) {
    return expand(jsonDocumentModels, documentModelId, null);
  }

  public ResolutionResult resolveImportedTypeDefinitions(String id, String modelReference, List<DocumentModel> documentModels) {
    try {
      List<com.mgmtp.a12.kernel.md.model.a12internal.FieldTypeDefinition> typeDefs =
          DocumentModelSupport.extractImportedTypeDefinitions(id, modelReference, DocumentModelSupport.removeMetaData(documentModels));
      DocumentModel model = documentModels.stream().filter(dm -> dm.getHeader().getId().equals(id)).findFirst().orElse(null);
      if (model == null) {
        throw new IllegalStateException("Document model not found: " + id);
      }
      List<JsonNode> typeDefsJson = DocumentModelSupport.serializeTypeDefs(typeDefs, model);
      return new ResolutionResult(typeDefsJson, DocumentModelSupport.importedTypeDefinitions(typeDefs));
    } catch (DocumentModelExpansionException e) {
      return new ResolutionResult(Collections.emptyList(), Collections.emptyMap(), e.getMessage());
    }
  }

  public EnrichedDocumentModelResultDto removeMetaDataFromModel(JsonNode jsonDocumentModel) {
    DocumentModel documentModel = DocumentModelSupport.deserialize(jsonDocumentModel.toString());
    DocumentModel dmWithoutMetaData = DocumentModelSupport.removeMetaData(List.of(documentModel)).get(0);
    JsonNode serialized = DocumentModelSupport.toJson(DocumentModelSupport.serialize(dmWithoutMetaData));
    return new EnrichedDocumentModelResultDto(serialized);
  }
}
