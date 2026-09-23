package de.a12.studio.models.formmodel;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import de.a12.studio.models.Label;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * {@code binding.details.modificationConfiguration} of a {@link Binding}: how new/edited linked entities are
 * created - either by extending the parent activity descriptor, or through a custom one - SME's
 * {@code ModificationConfiguration} (see {@code client/src/modules/formModel/fmElements/types/binding.ts}).
 * {@code activityDescriptorGroup}/{@code activityDescriptor} (kernel {@code GroupInstance}-typed in SME) aren't
 * modeled natively; they're preserved verbatim in {@link #getExtras()}.
 */
@Getter
@Setter
public class BindingModificationConfiguration {

  // "extendParent" or "custom" - which of the two below applies.
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private String scdmAddEditDescriptor;
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private List<Label> addButtonLabel = new ArrayList<>();
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private List<Label> editButtonLabel = new ArrayList<>();
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Boolean extendParentActivityDescriptor;

  private final Map<String, Object> extras = new LinkedHashMap<>();

  @JsonAnySetter
  public void setExtra(String name, Object value) {
    extras.put(name, value);
  }

  @JsonAnyGetter
  public Map<String, Object> getExtras() {
    return extras;
  }
}
