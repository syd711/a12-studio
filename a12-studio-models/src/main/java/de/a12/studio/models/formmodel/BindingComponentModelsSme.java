package de.a12.studio.models.formmodel;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

/**
 * {@code modelsSME} of a {@link BindingComponent}: the Overview/Form Model references it renders candidates,
 * current links and an additional-fields edit form with - SME's {@code I_BindingComponent.json}
 * {@code modelsSME} group.
 */
@Getter
@Setter
public class BindingComponentModelsSme {

  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private String availableItemsOverview;
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private String selectedItemsOverview;
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private String additionalFieldsForm;
}
