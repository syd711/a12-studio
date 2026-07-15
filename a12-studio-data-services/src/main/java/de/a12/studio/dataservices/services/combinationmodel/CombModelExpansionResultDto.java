package de.a12.studio.dataservices.services.combinationmodel;

import com.fasterxml.jackson.databind.JsonNode;
import de.a12.studio.dataservices.services.documentmodel.features.expansion.TypeDefInfo;
import lombok.Value;

import java.util.List;

@Value
public class CombModelExpansionResultDto {
  JsonNode documentModel;
  TypeDefInfo typeDefInfo;
  List<ExpansionError> errors;
}
