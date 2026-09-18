package de.a12.studio.models.formmodel;

import lombok.Getter;
import lombok.Setter;

/**
 * A Form Model screen element that links a piece of the screen to a {@code RelationshipModel} (SME's {@code
 * Binding} document, wrapping its {@code I_Binding} mixin): {@link BindingContent#getRelationshipName()} names
 * the relationship, {@link BindingContent#getTargetRole()} the role (side) of it this binding shows/edits. Only
 * the relationship-linkage fields are modeled - SME's Binding also carries a UI-component configuration
 * (dropdown/dual-pane/table-list selection widgets, CDM child-activity wiring, edit-modal config) that isn't
 * implemented here yet; {@link BindingContent} is {@code @JsonIgnoreProperties(ignoreUnknown = true)} so a
 * Binding carrying those fields still loads, they just won't round-trip on save. See
 * {@code docs/sme-reference-comparison.md} for the tracked gap.
 */
@Getter
@Setter
public class Binding extends ScreenElement {

  private BindingContent binding = new BindingContent();

  public Binding() {
    setType(ScreenElementType.BINDING);
  }
}
