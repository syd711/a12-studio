package de.a12.studio.models.relationshipuimodel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * {@code componentType: "DualPaneSelection"}: a two-pane picker, the available items on one side and the
 * currently selected/linked items on the other. {@code availableItemsOverviewModel}/{@code
 * selectedItemsOverviewModel} are required by every real fixture; {@code linkFormModel}, {@code height} and
 * {@code buttons} are optional (see e.g. {@code CountryCity_City_Ru.json} for the minimal shape vs. {@code
 * Teammembers_DualPane_Ru.json}/{@code PersonTeamAssignment_OnlyDualPane_Ru.json} for the fuller one).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class DualPaneSelectionComponent extends RelationshipUiComponent {

  private String availableItemsOverviewModel;

  private String selectedItemsOverviewModel;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private String linkFormModel;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private String height;

  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private List<Button> buttons = new ArrayList<>();
}
