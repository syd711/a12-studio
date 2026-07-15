package de.a12.studio.dataservices.services.document;

import com.fasterxml.jackson.databind.JsonNode;

public interface IDocument {
  JsonNode getDocument();

  String getLocale();

  String getDocumentModelName();
}
