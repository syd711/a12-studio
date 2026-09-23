package de.a12.studio.models.formmodel;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * {@code binding.details} of a {@link Binding} screen element: the relationship linkage fields ({@code name} -
 * SME's "Binding Name" display label, {@code relationshipName}, {@code targetRole}, {@code metaInformation})
 * plus the UI-component configuration ({@code mainComponent}/{@code editModalComponent}, {@code
 * isFixedRelationship}, {@code cdmChildActivitiesEnabled}, {@code modificationConfiguration}) - SME's
 * {@code I_Binding} shape (see {@code client/src/modules/formModel/fmElements/types/binding.ts}'s
 * {@code Details}). Everything else SME's {@code Details} carries is kept verbatim in {@link #getExtras()}, so
 * it survives a load-then-save.
 */
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
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Boolean isFixedRelationship;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Boolean cdmChildActivitiesEnabled;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private BindingComponent mainComponent;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private BindingComponent editModalComponent;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private BindingModificationConfiguration modificationConfiguration;

  // Everything SME's I_Binding carries that isn't modeled above, kept verbatim so a load-then-save doesn't
  // drop it.
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
