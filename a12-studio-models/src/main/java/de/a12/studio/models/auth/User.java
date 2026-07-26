package de.a12.studio.models.auth;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class User {

  private String username;
  private String password;
  private List<String> authorities = new ArrayList<>();
  private String email;
  private String firstname;
  private String lastname;
}
