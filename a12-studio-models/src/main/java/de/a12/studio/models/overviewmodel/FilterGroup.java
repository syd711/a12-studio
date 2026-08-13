package de.a12.studio.models.overviewmodel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import de.a12.studio.models.Label;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * One entry of {@code newFilterConfiguration.filterGroups}: a section users see in the Filter Selector. Per the
 * platform docs ("Filter Groups"): {@link #id} is auto-generated, {@link #name} is a technical/editor-only name,
 * {@link #label} is the multilingual title shown in the application, {@link #icon} decorates the group header,
 * {@link #collapsed} controls its initial state, and {@link #filterItems} holds its {@link FilterItem}s.
 * <p>
 * See {@link FilterItem} for why the field-type-specific option groups and Filter-Definition-based items aren't
 * modeled - {@code testing/basic/models/Company_OM.json}'s {@code filterGroups} is {@code []}, so there is no
 * fixture evidence for a populated group beyond what the docs describe in prose.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class FilterGroup {

  private String id;
  private String name;
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private List<Label> label = new ArrayList<>();
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Icon icon;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Boolean collapsed;
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private List<FilterItem> filterItems = new ArrayList<>();
}
