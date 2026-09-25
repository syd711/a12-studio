package de.a12.studio.ui.components;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Side;
import javafx.scene.control.Skin;
import javafx.scene.control.TabPane;
import javafx.scene.control.skin.TabPaneSkin;

/**
 * A {@link TabPane} whose tab header can be shown in one of two layouts, switchable at runtime:
 * <ul>
 *   <li><b>single row</b> ({@link #multiRowHeaderProperty()} {@code false}): the regular JavaFX
 *   {@link TabPaneSkin} - one row of tab headers, with an overflow menu once they no longer fit;</li>
 *   <li><b>multi row</b> (default, {@code true}): {@link MultiRowTabPaneSkin} - the headers wrap onto as many rows as
 *   the available width needs, so every open tab stays visible and one click away.</li>
 * </ul>
 * Both layouts are styled by the same {@code stylesheet-tab-pane.css} rules ({@code .tab-pane .tab}, ...), so a
 * tab looks the same either way; only the multi-row one carries the extra {@code multi-row-tabs} style class.
 * The multi-row layout only supports {@link Side#TOP} headers - for any other side this falls back to the
 * regular skin, whatever {@link #multiRowHeaderProperty()} says.
 */
public class StudioTabPane extends TabPane {

  private static final String MULTI_ROW_STYLE_CLASS = "multi-row-tabs";

  private final BooleanProperty multiRowHeader = new SimpleBooleanProperty(this, "multiRowHeader", true);

  public StudioTabPane() {
    multiRowHeader.addListener((observable, oldValue, newValue) -> refreshSkin());
    sideProperty().addListener((observable, oldValue, newValue) -> refreshSkin());
  }

  public BooleanProperty multiRowHeaderProperty() {
    return multiRowHeader;
  }

  public boolean isMultiRowHeader() {
    return multiRowHeader.get();
  }

  public void setMultiRowHeader(boolean multiRowHeader) {
    this.multiRowHeader.set(multiRowHeader);
  }

  @Override
  protected Skin<?> createDefaultSkin() {
    if (usesMultiRowSkin()) {
      getStyleClass().add(MULTI_ROW_STYLE_CLASS);
      return new MultiRowTabPaneSkin(this);
    }
    return new TabPaneSkin(this);
  }

  private boolean usesMultiRowSkin() {
    return isMultiRowHeader() && getSide() == Side.TOP;
  }

  /**
   * Swaps the skin in place if the layout that should be used no longer matches the installed one. Nothing to
   * do while no skin exists yet (the pane hasn't been attached to a scene): {@link #createDefaultSkin()} then
   * picks the right one when it is first needed.
   */
  private void refreshSkin() {
    if (getSkin() == null || usesMultiRowSkin() == getSkin() instanceof MultiRowTabPaneSkin) {
      return;
    }
    getStyleClass().remove(MULTI_ROW_STYLE_CLASS);
    setSkin(createDefaultSkin());
  }
}
