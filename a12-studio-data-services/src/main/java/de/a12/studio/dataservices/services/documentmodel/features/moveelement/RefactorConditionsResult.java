package de.a12.studio.dataservices.services.documentmodel.features.moveelement;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Value;

import java.util.List;

@Value
public class RefactorConditionsResult {
  List<JsonNode> rules;
  List<JsonNode> computations;
}
