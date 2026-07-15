package de.a12.studio.dataservices.services.document;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Value;

@Value
public class DocumentAndID implements IDocument {
  String id;
  JsonNode document;
  String locale;
  String documentModelName;
}
