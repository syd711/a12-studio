package de.a12.studio.models.formmodel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import de.a12.studio.models.Annotation;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

// One custom row action button in a repeat's RowActionGroup (distinct from DefaultRowAction, which only
// configures the built-in default row interaction).
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class RowAction {

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private ButtonStyling buttonStyling;
  private String event;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private TextContainer confirmation;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private TextContainer confirmationDialogTitle;
  // "ALWAYS"/"DISABLED_IN_EDIT_MODE"/"DISABLED_IN_READONLY_MODE"/"HIDDEN_IN_EDIT_MODE"/"HIDDEN_IN_READONLY_MODE".
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private String scope;
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private List<Style> style = new ArrayList<>();
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private List<Annotation> annotations = new ArrayList<>();
}
