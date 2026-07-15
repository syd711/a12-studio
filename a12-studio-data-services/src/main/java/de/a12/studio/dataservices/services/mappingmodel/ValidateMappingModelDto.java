package de.a12.studio.dataservices.services.mappingmodel;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Value;

import java.util.List;

@Value
public class ValidateMappingModelDto {
  JsonNode mappingModel;
  JsonNode structuralMappingModel;
  List<JsonNode> documentModels;
}
