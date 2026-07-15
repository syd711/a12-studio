package de.a12.studio.dataservices.services.documentmodel.validationrule;

import lombok.Value;

@Value
public class ValidationResult {
  int line;
  int startCol;
  int endCol;
  String message;
}
