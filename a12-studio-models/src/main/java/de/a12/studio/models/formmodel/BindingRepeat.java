package de.a12.studio.models.formmodel;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

/**
 * A repeat whose rows come from a CDM relationship traversal ({@link BindingContent}) instead of a plain
 * repeatable Document Model group - SME's {@code BindingRepeat} (see {@code
 * client/src/modules/formModel/fmElements/types/bindingRepeat.ts}'s {@code BindingRepeatProperties}: a {@code
 * repeatBase_mixin} - the same fields {@link AbstractRepeat} already models - plus a {@code binding} field of
 * the same shape as a plain {@link Binding} screen element's own {@link Binding#getBinding()}). Created (instead
 * of a plain {@link Binding}) when the dragged relationship's target role is to-many - see {@code
 * FormModelTreeController#dropRelationshipModel}.
 */
@Getter
@Setter
public class BindingRepeat extends AbstractRepeat {

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private BindingContent binding;

  public BindingRepeat() {
    setType(ScreenElementType.BINDING_REPEAT);
  }
}
