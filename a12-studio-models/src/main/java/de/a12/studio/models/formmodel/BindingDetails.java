package de.a12.studio.models.formmodel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

/**
 * {@code binding.details} of a {@link Binding} screen element: the relationship linkage fields modeled here
 * ({@code name} - SME's "Binding Name" display label, {@code relationshipName}, {@code targetRole}, {@code
 * metaInformation}). SME's {@code I_Binding} carries several more fields under {@code details} (the
 * dropdown/dual-pane/table-list component configuration, CDM child-activity toggles, edit-modal config) that
 * aren't modeled yet - see {@link BindingContent}'s javadoc.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class BindingDetails {

  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private String name;
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private String relationshipName;
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private String targetRole;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private BindingMetaInformation metaInformation;
}
