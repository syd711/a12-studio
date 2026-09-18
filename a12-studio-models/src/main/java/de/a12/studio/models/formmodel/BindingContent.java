package de.a12.studio.models.formmodel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

/**
 * The {@code binding} field of a {@link Binding} screen element, matching SME's {@code I_Binding} mixin's
 * {@code type}/{@code elementId}/{@code details} shape. {@code type} is always {@code "relationship"} today (the
 * only binding kind a12-studio creates), kept as a field rather than hardcoded so a real SME export using some
 * other value still round-trips it losslessly.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class BindingContent {

  private String type;
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private String elementId;
  private BindingDetails details = new BindingDetails();
}
