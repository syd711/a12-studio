package de.a12.studio.models.relationshipuimodel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Getter;
import lombok.Setter;

/**
 * Polymorphic base for a Relationship UI Model's {@code content.component}, discriminated by {@code
 * componentType} (mirrors {@link de.a12.studio.models.documentmodel.FieldType}'s {@code type} discriminator
 * pattern). Real fixtures (see {@code testing/workspaces/advanced_new/models/.../*_Ru.json}) only ever use
 * {@code DualPaneSelection}, {@code TableList} or {@code DropDownSelection}; {@link
 * GenericRelationshipUiComponent} is the fallback for anything else so loading a model with a future/unknown
 * component type degrades to a raw property bag instead of failing to load the whole model.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "componentType", visible = true, defaultImpl = GenericRelationshipUiComponent.class)
@JsonSubTypes({
    @JsonSubTypes.Type(value = DualPaneSelectionComponent.class, name = "DualPaneSelection"),
    @JsonSubTypes.Type(value = TableListComponent.class, name = "TableList"),
    @JsonSubTypes.Type(value = DropDownSelectionComponent.class, name = "DropDownSelection")
})
@Getter
@Setter
public abstract class RelationshipUiComponent {

  public static final String TYPE_DUAL_PANE_SELECTION = "DualPaneSelection";
  public static final String TYPE_TABLE_LIST = "TableList";
  public static final String TYPE_DROP_DOWN_SELECTION = "DropDownSelection";

  // visible = true above also exposes the type id as this plain property; WRITE_ONLY keeps it settable on
  // deserialization without Jackson also emitting it a second time as a regular property on serialization.
  @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
  private String componentType;
}
