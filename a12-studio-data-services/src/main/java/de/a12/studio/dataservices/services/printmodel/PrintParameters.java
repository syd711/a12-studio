package de.a12.studio.dataservices.services.printmodel;

import lombok.Value;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.TimeZone;

@Value
public class PrintParameters {
  String printModel;
  Map<String, String> printModelMap;
  Map<String, String> documentModelMap;
  Map<String, String> typesettingModelMap;
  Optional<String> document;
  Map<String, String> fonts;
  Locale locale;
  TimeZone timeZone;
  boolean useLegacyRendering;
}
