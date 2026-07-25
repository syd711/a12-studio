package de.a12.studio.models.applicationmodel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class Module {

  private String name;
  // Leaving the menu name empty means the module has no entry in the navigation, per the app model docs.
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Menu menu;
  private List<Flow> flows = new ArrayList<>();

  // Menu.name has no default and is normally kept in sync with the module's own name (see real app models),
  // so a freshly created menu is seeded with it rather than left null.
  public Menu getOrCreateMenu() {
    if (menu == null) {
      menu = new Menu();
      menu.setName(name);
    }
    return menu;
  }
}
