package de.a12.studio.ui.editors.typedefinitionmodel;

import de.a12.studio.dataservices.models.documentmodel.FieldConfig;
import de.a12.studio.dataservices.models.documentmodel.FieldElement;
import de.a12.studio.dataservices.models.documentmodel.FieldType;
import de.a12.studio.dataservices.models.documentmodel.TypeDefinition;
import org.jspecify.annotations.NonNull;

/**
 * Adapts a {@link TypeDefinition} (just id/name/fieldType, not part of the {@code Element} hierarchy) to a
 * {@link FieldElement}, so it can be edited with the same field property editors used for document model
 * fields (see {@code de.a12.studio.ui.editors.propertyeditors} and {@link
 * TypeDefinitionModelFieldEditorController}). Only name and field type are written back to the wrapped
 * TypeDefinition on commit; the other {@link FieldConfig} properties those shared panels expose
 * (requiredness, global, transient, ...) have no equivalent on a type definition and are discarded, same as
 * SME's TypeDefinition.toDocument() conversion.
 */
class TypeDefinitionFieldElement extends FieldElement {

  private final TypeDefinition typeDefinition;

  TypeDefinitionFieldElement(@NonNull TypeDefinition typeDefinition) {
    this.typeDefinition = typeDefinition;
    setId(typeDefinition.getId());
    setName(typeDefinition.getName());

    FieldConfig config = new FieldConfig() {
      @Override
      public void setFieldType(FieldType fieldType) {
        super.setFieldType(fieldType);
        typeDefinition.setFieldType(fieldType);
      }
    };
    config.setFieldType(typeDefinition.getFieldType());
    setField(config);
  }

  @Override
  public void setName(String name) {
    super.setName(name);
    typeDefinition.setName(name);
  }
}
