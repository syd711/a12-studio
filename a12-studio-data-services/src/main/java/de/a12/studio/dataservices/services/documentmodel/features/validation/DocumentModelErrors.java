package de.a12.studio.dataservices.services.documentmodel.features.validation;

import com.mgmtp.a12.model.notification.Severity;
import lombok.Value;

@Value
public class DocumentModelErrors {
  String id;
  String message;
  Severity severity;
}
