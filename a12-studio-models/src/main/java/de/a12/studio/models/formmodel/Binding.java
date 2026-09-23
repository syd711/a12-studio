package de.a12.studio.models.formmodel;

import lombok.Getter;
import lombok.Setter;

/**
 * A Form Model screen element that links a piece of the screen to a {@code RelationshipModel} (SME's {@code
 * Binding} document, wrapping its {@code I_Binding} mixin): {@link BindingContent#getRelationshipName()} names
 * the relationship, {@link BindingContent#getTargetRole()} the role (side) of it this binding shows/edits, and
 * {@link BindingDetails#getMainComponent()}/{@link BindingDetails#getEditModalComponent()} configure which
 * widget (drop-down, dual-pane selection, or an editable table-list) renders it. For a to-many relationship,
 * see {@link BindingRepeat} instead. {@link BindingContent}, {@link BindingDetails}, {@link
 * BindingMetaInformation}, {@link BindingComponent} and {@link BindingModificationConfiguration} keep every key
 * they don't model in an {@code extras} map, so a Binding carrying those fields round-trips unchanged (they
 * just can't be edited). See {@code docs/sme-reference-comparison.md} for the tracked gap.
 */
@Getter
@Setter
public class Binding extends ScreenElement {

  private BindingContent binding = new BindingContent();

  public Binding() {
    setType(ScreenElementType.BINDING);
  }
}
