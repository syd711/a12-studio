package de.a12.studio.models.overviewmodel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

/**
 * {@code filterGroups[].filterItems[].options}: the field-type-specific configuration of a field-based {@link
 * FilterItem}. {@link #fieldId} is common to every field-based item regardless of type - the field's data type
 * (derived via {@link #fieldId}, see {@link FilterItem#getType()}) then determines which of the further,
 * type-specific option groups the platform docs describe (Boolean/Confirm criteria, String view mode/matching,
 * Enumeration initial criteria/pinned values, Number ranges, Date/Time ranges/periods) apply. Only the String
 * group's matching options ({@link #invert}, {@link #empty}, {@link #caseSensitive}, {@link #exactMatch}) are
 * modeled here, since they're the only ones with fixture evidence (recovered from {@code
 * testing/workspaces/basic/models/Company_OM.json}'s git history, commit {@code 059be31}) - the other groups are
 * silently ignored on load ({@code ignoreUnknown = true}) rather than guessed at.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class FilterItemOptions {

  private String fieldId;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private BooleanUserAccessOption invert;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private BooleanUserAccessOption empty;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private BooleanUserAccessOption caseSensitive;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private BooleanUserAccessOption exactMatch;
}
