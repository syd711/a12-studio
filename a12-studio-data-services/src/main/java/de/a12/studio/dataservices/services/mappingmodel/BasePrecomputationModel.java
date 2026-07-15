package de.a12.studio.dataservices.services.mappingmodel;

import com.fasterxml.jackson.databind.JsonNode;
import de.a12.studio.dataservices.services.support.TypeDefinitionInfo;
import lombok.Value;

import java.util.Map;

@Value
public class BasePrecomputationModel {
  JsonNode baseModel;
  Map<String, TypeDefinitionInfo> includedTDs;
  Map<String, TypeDefinitionInfo> importedTDs;
  Map<String, TypeDefinitionInfo> includedImportedTDs;
}
