package de.a12.studio.dataservices.services.documentmodel.features.expansion;

import com.fasterxml.jackson.databind.JsonNode;
import de.a12.studio.dataservices.services.support.TypeDefinitionInfo;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
public class ResolutionResult {
  private final List<JsonNode> typeDefs;
  private final Map<String, TypeDefinitionInfo> typeDefinitionInfo;
  private final String errorMessage;

  public ResolutionResult(List<JsonNode> typeDefs, Map<String, TypeDefinitionInfo> typeDefinitionInfo, String errorMessage) {
    this.typeDefs = typeDefs;
    this.typeDefinitionInfo = typeDefinitionInfo;
    this.errorMessage = errorMessage;
  }

  public ResolutionResult(List<JsonNode> typeDefs, Map<String, TypeDefinitionInfo> typeDefinitionInfo) {
    this(typeDefs, typeDefinitionInfo, null);
  }
}
