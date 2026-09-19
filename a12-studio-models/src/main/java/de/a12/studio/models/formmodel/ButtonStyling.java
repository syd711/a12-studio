package de.a12.studio.models.formmodel;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class ButtonStyling {

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private LocalizedText label;
  // Unlike label, description is a plain TextContainer ({"text": [...]}), not a polymorphic LocalizedText -
  // fixture-evidenced (testing/workspaces/e-commerce/models/01_Products/Product_FM.json).
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private TextContainer description;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Icon icon;
  // "PRIMARY" or "SECONDARY".
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private String priority;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Boolean destructive;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Boolean labelHidden;
  // SME's "stylable_mixin" (see Control#getStyle()) - a plain "style" field on the wire, not nested.
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private List<Style> style = new ArrayList<>();

  @JsonIgnore
  public String getIconName() {
    return icon != null ? icon.getName() : null;
  }

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

  /** Whether none of the styling fields is set, i.e. this object would serialize as an empty {@code {}}. */
  @JsonIgnore
  public boolean isBlank() {
    return label == null && description == null && icon == null && (priority == null || priority.isEmpty())
        && destructive == null && labelHidden == null && style.isEmpty();
  }
}
