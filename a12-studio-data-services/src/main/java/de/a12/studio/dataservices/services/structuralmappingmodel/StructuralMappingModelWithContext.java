package de.a12.studio.dataservices.services.structuralmappingmodel;

import com.fasterxml.jackson.databind.JsonNode;
import com.mgmtp.a12.kernel.md.model.a12internal.DocumentModel;
import com.mgmtp.a12.kernel.md.structuralmapping.a12internal.services.StructuralMappingMetaModelService;
import com.mgmtp.a12.kernel.mmtypings.mm_structuralmappingmodel_1.views.MM_StructuralMappingModel_1;
import de.a12.studio.dataservices.services.support.DocumentModelSupport;
import lombok.Value;

import java.io.StringReader;
import java.io.StringWriter;

@Value
public class StructuralMappingModelWithContext {
  MM_StructuralMappingModel_1 smm;
  DocumentModel sourceDM;
  DocumentModel targetDM;

  public static StructuralMappingModelWithContext create(JsonNode jsonSMM, JsonNode jsonSourceDM, JsonNode jsonTargetDM) {
    MM_StructuralMappingModel_1 smm = deserializeSMM(jsonSMM.toString());
    DocumentModel sourceDM = DocumentModelSupport.deserialize(jsonSourceDM.toString());
    DocumentModel targetDM = DocumentModelSupport.deserialize(jsonTargetDM.toString());
    return new StructuralMappingModelWithContext(smm, sourceDM, targetDM);
  }

  public static MM_StructuralMappingModel_1 deserializeSMM(String smmContent) {
    return StructuralMappingMetaModelService.deserialize(new StringReader(smmContent), error -> {
      throw new RuntimeException(error.getMessage());
    });
  }

  public static String serializeSMM(MM_StructuralMappingModel_1 smm) {
    StringWriter result = new StringWriter();
    StructuralMappingMetaModelService.serialize(smm, result);
    return result.toString();
  }
}
