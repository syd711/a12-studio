package de.a12.studio.models.auth;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class RolesDocument implements AuthDocument {

  private List<Role> roles = new ArrayList<>();
}
