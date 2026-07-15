package de.a12.studio.dataservices.services.combinationmodel;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Value;

import java.util.List;

@Value
public class CombModelRequestDto {
  String modelId;
  List<JsonNode> documentModels;
  List<JsonNode> selectionModels;
  List<JsonNode> combinationModels;
}
