package de.a12.studio.models.relationshipuimodel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * {@code componentType: "TableList"}: a single-pane, read-only-by-default list of the currently selected/
 * linked items ({@code selectedItemsOverviewModel} is the only field every real fixture has). Optionally
 * editable via a modal dialog, configured by {@code buttons} (the row of actions above the table, e.g. {@code
 * event_open_edit_modal}) and {@code editConfiguration} (the dialog's own Dual-Pane-shaped picker) - see
 * {@code PersonSkills_Person_Ru.json} for the minimal read-only shape vs. {@code
 * PersonTeamAssignment_TableWithEditModal_Ru.json} for the fully editable one.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class TableListComponent extends RelationshipUiComponent {

  private String selectedItemsOverviewModel;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private String linkFormModel;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private String height;

  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private List<Button> buttons = new ArrayList<>();

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private EditConfiguration editConfiguration;
}
