package de.a12.studio.dataservices.services.documentmodel.computationrule;

import lombok.Value;

@Value
public class ComputationValidationResult {
  String conditionType;
  int alternativeIndex;
  int line;
  int startCol;
  int endCol;
  String message;
}
