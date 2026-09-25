package de.a12.studio.models.overviewmodel;

/**
 * A Tree Model Subheader element ({@code type: "expand_all_popup"}) marking where the popup that expands/collapses
 * the whole tree is shown. Like {@link MultiSelectionElement}, it is a position marker that may carry the
 * presentational fields of {@link ConfigurableBoxElement}.
 */
public class ExpandAllPopupElement extends ConfigurableBoxElement {

  public ExpandAllPopupElement() {
    setType(BoxElementType.EXPAND_ALL_POPUP);
  }
}
