package de.a12.studio.ui.editors.formmodel.formtree.nodeeditors;

import de.a12.studio.models.formmodel.AbstractRepeat;
import de.a12.studio.ui.editors.formmodel.StylesPanelController;
import javafx.fxml.FXML;
import org.jspecify.annotations.NonNull;

/**
 * "Header Styles" property editor for a selected {@link AbstractRepeat} node: delegates to the shared
 * {@link StylesPanelController} bound to {@link AbstractRepeat#getHeaderStyle()}, which holds CSS style
 * classes applied specifically to the repeat's column header row (separate from the body styles in
 * {@link de.a12.studio.models.formmodel.ScreenElement#getStyle()}).
 */
public class RepeatHeaderStylesPanelController {

  @FXML
  private StylesPanelController headerStylesListController;

  public void setRepeat(@NonNull AbstractRepeat repeat) {
    headerStylesListController.setCustom(repeat::getHeaderStyle, repeat::getHeaderStyle);
  }
}
