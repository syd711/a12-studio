package de.a12.studio.models.relationshipuimodel;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import de.a12.studio.models.Annotation;
import de.a12.studio.models.Label;
import de.a12.studio.models.overviewmodel.Confirmation;
import de.a12.studio.models.overviewmodel.Icon;
import de.a12.studio.models.overviewmodel.OverviewButtonLike;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * A Relationship UI Model component's action button (e.g. {@code event_open_edit_modal}, {@code
 * event_submit_edit_modal}). Same shape as {@link de.a12.studio.models.overviewmodel.Button} - reusing its
 * {@link Icon}/{@link Confirmation} types and {@link OverviewButtonLike} contract so this class works directly
 * with {@link de.a12.studio.ui.editors.propertyeditors.EventButtonsPanelController}/{@code
 * EventButtonDialogController} without changes - except {@code destructive}/{@code primary} are {@code
 * NON_NULL} here: real fixtures (see e.g. {@code Teammembers_Ru.json}, {@code
 * PersonTeamAssignment_TableWithEditModal_Ru.json}) routinely omit one or the other per button, which
 * {@code overviewmodel.Button}'s always-serialize fields would turn into an explicit {@code null} on save.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class Button implements OverviewButtonLike {

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
