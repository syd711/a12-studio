package de.a12.studio.ui.editors.propertyeditors;

import de.a12.studio.models.overviewmodel.Column;
import de.a12.studio.models.overviewmodel.Icon;
import de.a12.studio.ui.editors.AbstractPropertyEditor;
import de.a12.studio.ui.util.MaterialIcons;
import de.a12.studio.ui.util.SystemUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import org.jspecify.annotations.NonNull;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Edits a single {@link Column}'s {@link Column#getIcon()} name via an editable {@link ComboBox} suggesting
 * every icon name known to the bundled Google "Material Icons" font (see {@link MaterialIcons}, the same
 * names shown at https://fonts.google.com/icons), filtered live as the user types and previewed with the
 * actual glyph rather than a plain name string. A browse button still opens the Google Fonts icon picker
 * directly, for icons outside the bundled classic set. Not bound to a single {@link
 * de.a12.studio.models.documentmodel.Element} (the icon lives on the {@link Column} being edited by {@link
 * de.a12.studio.ui.editors.overviewmodel.dialogs.OverviewColumnDialogController}), so it follows the same
 * per-Column pattern as {@link de.a12.studio.ui.editors.overviewmodel.StylesPanelController#setColumn}.
 */
public class IconPanelController extends AbstractPropertyEditor implements Initializable {

  private static final String GOOGLE_FONTS_ICONS_URL = "https://fonts.google.com/icons";
  private static final double PREVIEW_GLYPH_SIZE = 18;

  @FXML
  private ComboBox<String> iconCombo;

  @FXML
  private Label previewLabel;

  private Column column;

  // Set while setColumn() is repopulating iconCombo from the model, so the listener below doesn't mistake
  // that programmatic change for a user edit.
  private boolean updatingFromModel;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    super.initialize(location, resources);

    previewLabel.setFont(MaterialIcons.font(PREVIEW_GLYPH_SIZE));

    ObservableList<String> iconNames = FXCollections.observableArrayList(MaterialIcons.iconNames());
    FilteredList<String> filteredIconNames = new FilteredList<>(iconNames, name -> true);
    iconCombo.setItems(filteredIconNames);
    iconCombo.setEditable(true);
    iconCombo.setCellFactory(listView -> new IconListCell());

    iconCombo.getEditor().textProperty().addListener((observable, oldValue, newValue) -> {
      updatePreview(newValue);
      if (updatingFromModel) {
        return;
      }
      filterSuggestions(filteredIconNames, newValue);
      setIconName(blankToNull(newValue));
      commitChange();
    });
  }

  public void setColumn(@NonNull Column column) {
    this.column = column;
    updatingFromModel = true;
    try {
      iconCombo.getEditor().setText(column.getIcon() != null ? column.getIcon().getName() : null);
    }
    finally {
      updatingFromModel = false;
    }
  }

  @FXML
  private void onBrowseKioskMode() {
    SystemUtil.openUrlInKioskWindow(GOOGLE_FONTS_ICONS_URL);
  }

  /**
   * Narrows the suggestion popup to icon names containing what's been typed so far, and shows it - unless
   * what's left is a single exact match, meaning the user just picked a suggestion (which itself re-fires
   * this same editor-text listener), in which case the popup is closed instead of instantly flashing back
   * open.
   */
  private void filterSuggestions(FilteredList<String> filteredIconNames, String typed) {
    filteredIconNames.setPredicate(name -> typed == null || typed.isBlank() || name.toLowerCase().contains(typed.toLowerCase()));
    boolean isExactSingleMatch = filteredIconNames.size() == 1 && filteredIconNames.get(0).equalsIgnoreCase(typed);
    if (isExactSingleMatch) {
      iconCombo.hide();
    }
    else if (iconCombo.getEditor().isFocused()) {
      iconCombo.show();
    }
  }

  private void updatePreview(String iconName) {
    Character glyph = MaterialIcons.glyph(iconName);
    previewLabel.setText(glyph != null ? glyph.toString() : "");
  }

  private void setIconName(String name) {
    if (name == null) {
      column.setIcon(null);
      return;
    }
    if (column.getIcon() == null) {
      column.setIcon(new Icon());
    }
    column.getIcon().setName(name);
  }

  private static String blankToNull(String value) {
    return value == null || value.isBlank() ? null : value;
  }

  /**
   * Suggestion list row: the icon's actual glyph (rendered via {@link MaterialIcons#font}) next to its name.
   */
  private static class IconListCell extends ListCell<String> {
    private final Label glyphLabel = new Label();
    private final Label nameLabel = new Label();
    private final HBox graphic = new HBox(8, glyphLabel, nameLabel);

    IconListCell() {
      glyphLabel.setFont(MaterialIcons.font(PREVIEW_GLYPH_SIZE));
      glyphLabel.setMinWidth(PREVIEW_GLYPH_SIZE + 4);
      graphic.setAlignment(Pos.CENTER_LEFT);
    }

    @Override
    protected void updateItem(String name, boolean empty) {
      super.updateItem(name, empty);
      if (empty || name == null) {
        setGraphic(null);
        return;
      }
      Character glyph = MaterialIcons.glyph(name);
      glyphLabel.setText(glyph != null ? glyph.toString() : "");
      nameLabel.setText(name);
      setGraphic(graphic);
    }
  }
}
