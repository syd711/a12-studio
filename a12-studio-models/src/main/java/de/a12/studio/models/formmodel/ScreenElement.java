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
    @JsonSubTypes.Type(value = Binding.class, name = "Binding"),
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

  // Include provenance (SME's "Included" mixin), set on the top-level elements an include was expanded into. The A12
  // Form Engine's include expansion (FormIncludeExpander is our port) copies the elements of the first screen of the
  // referenced Form Model into this model's own screens with rewritten ids and keeps these three fields on them, so
  // an include stays recognizable and can be expanded again. It is not resolved live at render time.
  // includeId: identifies the include; every copied id is prefixed with "<includeId>_" (so it is not an id of the
  // source Form Model). Neighbors with the same includeId belong to the same include.
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private String includeId;
  // The Form Model this subtree was included from.
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private String formModelRef;
  // The group of this model's own Document Model (e.g. /Person/address) that stands for the root group of the included
  // Form Model's Document Model: the included elements' references were rebound to what is below it.
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private String hostDocumentModelPath;
}
