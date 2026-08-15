package de.a12.studio.ui.editors.propertyeditors;

import de.a12.studio.models.overviewmodel.Column;
import de.a12.studio.models.overviewmodel.Icon;
import de.a12.studio.ui.components.IconComboController;
import de.a12.studio.ui.editors.AbstractPropertyEditor;
import de.a12.studio.ui.util.SystemUtil;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import org.jspecify.annotations.NonNull;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Edits a single owner's {@link Icon} name via {@link IconComboController}, the shared editable-combobox
 * widget that suggests every icon name known to the bundled Google "Material Icons" font, filtered live as the
 * user types and previewed with the actual glyph. A browse button still opens the Google Fonts icon picker
 * directly, for icons outside the bundled classic set. Not bound to a single {@link
 * de.a12.studio.models.documentmodel.Element}, so it follows the same per-owner pattern as {@link
 * LocalizedTextPanelController}: {@link #setColumn} for a {@link Column} (the icon being edited by {@link
 * de.a12.studio.ui.editors.overviewmodel.dialogs.OverviewColumnDialogController}), or {@link #setCustom} for
 * any other owner (e.g. the Custom Filter Configuration editor's Filter Button/Filter Group/Filter Item icons)
 * via a caller-supplied getter/setter pair rather than a dedicated {@code setXxx}.
 */
public class IconPanelController extends AbstractPropertyEditor implements Initializable {

  private static final String GOOGLE_FONTS_ICONS_URL = "https://fonts.google.com/icons";

  @FXML
  private IconComboController iconComboController;

  private Column column;

  // Set (instead of column) by setCustom(), for owners other than a Column.
  private Supplier<Icon> customIconGetter;
  private Consumer<Icon> customIconSetter;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    super.initialize(location, resources);

    iconComboController.setOnChange(name -> {
      setIconName(name);
      commitChange();
    });
  }

  public void setColumn(@NonNull Column column) {
    this.column = column;
    this.customIconGetter = null;
    this.customIconSetter = null;
    iconComboController.setValue(column.getIcon() != null ? column.getIcon().getName() : null);
  }

  /**
   * Binds this panel to an arbitrary owner's {@link Icon} field via a getter/setter pair, e.g. {@code
   * filterGroup::getIcon}/{@code filterGroup::setIcon}.
   */
  public void setCustom(@NonNull Supplier<Icon> getter, @NonNull Consumer<Icon> setter) {
    this.column = null;
    this.customIconGetter = getter;
    this.customIconSetter = setter;
    Icon icon = getter.get();
    iconComboController.setValue(icon != null ? icon.getName() : null);
  }

  @FXML
  private void onBrowseKioskMode() {
    SystemUtil.openUrlInKioskWindow(GOOGLE_FONTS_ICONS_URL);
  }

  private void setIconName(String name) {
    if (customIconGetter != null) {
      if (name == null) {
        customIconSetter.accept(null);
        return;
      }
      Icon icon = customIconGetter.get();
      if (icon == null) {
        icon = new Icon();
        customIconSetter.accept(icon);
      }
      icon.setName(name);
      return;
    }
    if (name == null) {
      column.setIcon(null);
      return;
    }
    if (column.getIcon() == null) {
      column.setIcon(new Icon());
    }
    column.getIcon().setName(name);
  }
}
