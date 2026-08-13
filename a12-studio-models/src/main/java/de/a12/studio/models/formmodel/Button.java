package de.a12.studio.models.formmodel;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import de.a12.studio.models.EventButtonLike;
import lombok.Getter;
import lombok.Setter;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", visible = true, defaultImpl = GenericButton.class)
@JsonSubTypes({
    @JsonSubTypes.Type(value = NavigationButton.class, name = "NAVIGATION"),
    @JsonSubTypes.Type(value = EventButton.class, name = "EVENT")
})
@Getter
@Setter
public abstract class Button implements EventButtonLike {

  // visible = true above also exposes the type id as this plain property; WRITE_ONLY keeps it settable on
  // deserialization without Jackson also emitting it a second time as a regular property on serialization.
  @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
  private ButtonType type;
  private String id;
  private String name;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private ButtonStyling buttonStyling;
  // Lives here rather than only on EventButton so any Button subtype (NavigationButton included) satisfies
  // EventButtonLike, letting a majorButtons/minorButtons list - which can mix subtypes, see Company_FM.json's
  // subHeaderBox (NavigationButton) vs footerBox (EventButton) - be edited uniformly by EventButtonsPanelController.
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private String event;
  // Validation mode triggered on a button press: e.g. "partial" (current screen) or "full" (entire document).
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private String validation;
  // Visibility/enablement depending on the form's readonly state, e.g. "HIDDEN_IN_READONLY_MODE".
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private String scope;

  @Override
  @JsonIgnore
  public Boolean getPrimary() {
    return buttonStyling != null && "PRIMARY".equals(buttonStyling.getPriority());
  }

  @Override
  @JsonIgnore
  public void setPrimary(Boolean primary) {
    getOrCreateButtonStyling().setPriority(Boolean.TRUE.equals(primary) ? "PRIMARY" : "SECONDARY");
  }

  @Override
  @JsonIgnore
  public Boolean getDestructive() {
    return buttonStyling != null ? buttonStyling.getDestructive() : null;
  }

  @Override
  @JsonIgnore
  public void setDestructive(Boolean destructive) {
    getOrCreateButtonStyling().setDestructive(destructive);
  }

  @Override
  @JsonIgnore
  public String getIconName() {
    return buttonStyling != null && buttonStyling.getIcon() != null ? buttonStyling.getIcon().getName() : null;
  }

  @Override
  @JsonIgnore
  public void setIconName(String name) {
    if (name == null || name.isEmpty()) {
      if (buttonStyling != null) {
        buttonStyling.setIcon(null);
      }
      return;
    }
    ButtonStyling styling = getOrCreateButtonStyling();
    Icon icon = styling.getIcon();
    if (icon == null) {
      icon = new Icon();
      styling.setIcon(icon);
    }
    icon.setName(name);
  }

  private ButtonStyling getOrCreateButtonStyling() {
    if (buttonStyling == null) {
      buttonStyling = new ButtonStyling();
    }
    return buttonStyling;
  }
}
