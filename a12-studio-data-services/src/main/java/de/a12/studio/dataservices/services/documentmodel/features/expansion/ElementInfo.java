package de.a12.studio.dataservices.services.documentmodel.features.expansion;

import lombok.Value;

import java.util.List;

@Value
public class ElementInfo {
  List<String> overwrittenElements;
  List<String> referenceElements;
}
