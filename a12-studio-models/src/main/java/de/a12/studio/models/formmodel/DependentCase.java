package de.a12.studio.models.formmodel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class DependentCase {

  // The value of the master field this case applies to; null is a meaningful, distinct case (the master
  // field itself being unset/empty), so this must not be omitted from serialization when null.
  private String masterValue;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Boolean notRelevant;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Boolean readonly;
  // LEGACY, a12-studio-only (not an SME property): ids of form nodes hidden when this case applies, as the
  // Confirm control's Dependencies tab used to write them. That tab now writes Control.dependentControls (SME's
  // shape) and moves anything found here over on the first change; kept so old files still load and show.
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private List<String> notRelevantNodes = new ArrayList<>();
  // Field-only (a DependentGroup case never sets these): force the dependent field to this literal value...
  // An empty string is a real value (it clears the field, and SME accepts it as "an action"), so only null is omitted.
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private String value;
  // ...or copy the current value of another field, when the master field changes to masterValue.
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private String fieldRef;

  /**
   * Whether this case does anything when its master value applies (SME's "at least one action per case"):
   * a {@code notRelevant}/{@code readonly} flag (an explicit {@code false} counts, as it does for a filled
   * Boolean field in the kernel), a forced {@code value} (an empty string counts - it clears the field), a
   * {@code fieldRef} to copy from, or - a12-studio's Confirm control Dependencies tab - hidden nodes.
   */
  public boolean hasAction() {
    return notRelevant != null || readonly != null || value != null
        || (fieldRef != null && !fieldRef.isEmpty()) || !notRelevantNodes.isEmpty();
  }
}
