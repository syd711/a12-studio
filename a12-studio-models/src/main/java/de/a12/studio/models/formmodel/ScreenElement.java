package de.a12.studio.models.formmodel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import de.a12.studio.models.Annotation;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", visible = true, defaultImpl = GenericScreenElement.class)
@JsonSubTypes({
    @JsonSubTypes.Type(value = Section.class, name = "Section"),
    @JsonSubTypes.Type(value = MultiColumnSection.class, name = "MultiColumnSection"),
    @JsonSubTypes.Type(value = ControlGrid.class, name = "ControlGrid"),
    @JsonSubTypes.Type(value = CustomScreenElement.class, name = "CustomScreenElement"),
    @JsonSubTypes.Type(value = ButtonPanel.class, name = "ButtonPanel"),
    @JsonSubTypes.Type(value = InlineRepeat.class, name = "InlineRepeat"),
    @JsonSubTypes.Type(value = EmbeddedRepeat.class, name = "EmbeddedRepeat"),
    @JsonSubTypes.Type(value = DetachedRepeat.class, name = "DetachedRepeat")
})
@Getter
@Setter
public abstract class ScreenElement {

  // visible = true above also exposes the type id as this plain property; WRITE_ONLY keeps it settable on
  // deserialization without Jackson also emitting it a second time as a regular property on serialization.
  @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
  private ScreenElementType type;
  private String id;
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private String name;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private LocalizedText title;
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private List<Style> style = new ArrayList<>();
  // SME's "annotated_mixin" - a plain "annotations" field on the wire, matching documentmodel.Element.
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private List<Annotation> annotations = new ArrayList<>();

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private HideCondition hideCondition;

  // Transclusion provenance (SME's "Included" mixin): set when this node was copied in from another Form
  // Model's screen tree rather than authored directly here. SME expands an include at author-time (copies
  // the referenced subtree's nodes into this model's own screens with rewritten ids, keeping these three
  // fields as provenance metadata) rather than resolving it live at render time - so modeling these fields
  // is enough for round-trip fidelity even without a12-studio itself performing that expansion yet.
  // includeId: the node's own id within formModelRef's tree (as opposed to this.id, its id in this model).
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private String includeId;
  // The Form Model this subtree was included from.
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private String formModelRef;
  // Where in this model's own Document Model the included subtree's field/group references were remapped to.
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private String hostDocumentModelPath;
}
