package de.a12.studio.models.relationshipuimodel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import de.a12.studio.models.Label;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * The "edit in a modal" configuration of a {@link TableListComponent} (a Dual-Pane-shaped picker shown in a
 * dialog opened via a {@code event_open_edit_modal} button). All fields are optional per real fixtures (see
 * e.g. {@code Teammembers_Ru.json} vs. {@code PersonTeamAssignment_TableWithEditModal_Ru.json}, whose
 * populated subsets differ).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class EditConfiguration {

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private String selectedItemsOverviewModel;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private String availableItemsOverviewModel;

  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private List<Label> dialogTitle = new ArrayList<>();

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private String dialogWidth;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private String dialogMaxWidth;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private String dialogMaxHeight;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private String height;
}
