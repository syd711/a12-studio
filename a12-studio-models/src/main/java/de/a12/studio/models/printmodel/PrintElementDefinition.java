package de.a12.studio.models.printmodel;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Getter;
import lombok.Setter;

// Only the element types the studio can render/edit natively are typed; everything else (Table,
// Listing, Image, Headline, Barcode, ...) falls back to GenericPrintElement so it survives untouched.
// EXISTING_PROPERTY keeps the serialized "type" the POJO's own field value, so a GenericPrintElement
// re-emits whatever type string it was loaded with ("Table", "Image", ...) instead of a class-derived id.
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", include = JsonTypeInfo.As.EXISTING_PROPERTY,
    visible = true, defaultImpl = GenericPrintElement.class)
@JsonSubTypes({
    @JsonSubTypes.Type(value = PrintTextElement.class, name = "Text"),
    @JsonSubTypes.Type(value = PrintFieldElement.class, name = "Field"),
    @JsonSubTypes.Type(value = PrintCalculationElement.class, name = "Calculation")
})
@Getter
@Setter
public abstract class PrintElementDefinition extends PrintNode {

  private String type;
}
