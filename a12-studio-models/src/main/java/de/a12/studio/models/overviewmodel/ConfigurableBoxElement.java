package de.a12.studio.models.overviewmodel;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import de.a12.studio.models.Annotation;
import de.a12.studio.models.Label;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * The shape SME's {@code OMTM_Subheader_Element} gives <em>every</em> Subheader/Footer element, whatever its
 * {@code type}: label, description, confirmation, event, priority, destructive, hide-label, icon, styles and
 * annotations. Only a {@link ButtonElement} actually uses event/priority/destructive/icon/confirmation (SME's
 * {@code eventIsRequireForTypeButton}/{@code priorityIsRequireForTypeButton} rules apply to {@code type ==
 * "button"} only); a Search, Filter or Multi-Selection element carries just the presentational fields, so they
 * are editable like a button in SME instead of being bare position markers. Every field is omitted when unset,
 * so a marker that is only {@code {"type": "search"}} on disk stays that way after a load-then-save.
 */
@Getter
@Setter
public abstract class ConfigurableBoxElement extends BoxElement implements OverviewButtonLike {

  // Tree Model buttons carry an id ("button-026dc"); Overview Model elements don't.
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private String id;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private String event;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Confirmation confirmation;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Icon icon;
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private List<Label> label = new ArrayList<>();
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private List<Label> description = new ArrayList<>();
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Boolean destructive;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Boolean primary;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Boolean labelHidden;
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private List<String> styles = new ArrayList<>();
  // SME's "annotated_mixin" - a plain "annotations" field on the wire, matching documentmodel.Element.
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private List<Annotation> annotations = new ArrayList<>();

  @Override
  @JsonIgnore
  public String getIconName() {
    return icon != null ? icon.getName() : null;
  }

  @Override
  @JsonIgnore
  public void setIconName(String name) {
    if (name == null || name.isEmpty()) {
      icon = null;
      return;
    }
    if (icon == null) {
      icon = new Icon();
    }
    icon.setName(name);
  }
}
