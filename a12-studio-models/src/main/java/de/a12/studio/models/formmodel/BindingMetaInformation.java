package de.a12.studio.models.formmodel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

/** {@code binding.details.metaInformation} of a {@link Binding} screen element. */
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class BindingMetaInformation {

  private String version;
}
