package de.a12.studio.models.relationshipuimodel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class RelationshipUiModelContent {

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private RelationshipUiComponent component;

  private String relationshipName;

  private String targetRole;
}
