package de.a12.studio.models.relationshipuimodel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

/**
 * {@code componentType: "DropDownSelection"}: an autocomplete-style single-select dropdown, typically bound
 * to a Form Model's screen element ({@code elementRef}). Unlike the pane-based variants, it fetches its
 * candidate/selected item directly via Query Models rather than through Overview Models (see e.g. {@code
 * ParentTeam_Ru.json}).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class DropDownSelectionComponent extends RelationshipUiComponent {

  private String availableItemsQueryModel;

  private String selectedItemQueryModel;

  private String elementRef;
}
