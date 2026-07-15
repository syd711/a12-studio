package de.a12.studio.dataservices.services.printmodel;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mgmtp.a12.print.typesetting.internal.model.impl.TypesettingModelDto;
import com.mgmtp.a12.print.typesetting.internal.serialization.ObjectMapperFactory;
import com.mgmtp.a12.print.typesetting.internal.validation.ITypesettingModelValidator;
import com.mgmtp.a12.print.typesetting.internal.validation.TypesettingModelValidator;

import java.util.Locale;

public class TypesettingDeserializer {

  private final ObjectMapper objectMapper = ObjectMapperFactory.createTypesettingModelMapper();
  private final ITypesettingModelValidator typesettingModelValidator = new TypesettingModelValidator();

  public TypesettingModelDto validateAndMarshallTypesettingDto(String typesetting) {
    try {
      var validation = typesettingModelValidator.validate(typesetting, Locale.ENGLISH);
      if (!validation.noErrorOccurred()) {
        throw new RuntimeException("Typesetting is not valid.");
      }
      return objectMapper.readValue(typesetting, TypesettingModelDto.class);
    } catch (JsonProcessingException e) {
      throw new RuntimeException("Unable to load printModel", e);
    }
  }
}
