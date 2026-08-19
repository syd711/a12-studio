package de.a12.studio.ui.components;

import de.a12.studio.ui.util.MaterialIcons;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;

import java.util.function.Consumer;

/**
 * Editable {@link ComboBox} suggesting every icon name known to the bundled Google "Material Icons" font (see
 * {@link MaterialIcons}, the same names shown at https://fonts.google.com/icons), filtered live as the user
 * types and previewed with the actual glyph rather than a plain name string.
 */
public class IconComboController {

  private static final double PREVIEW_GLYPH_SIZE = 18;

  @FXML
  private ComboBox<String> iconCombo;

  @FXML
  private Label previewLabel;

  // Set while setValue() is repopulating iconCombo from the model, so the listener below doesn't mistake that
  // programmatic change for a user edit.
  private boolean updatingFromModel;

  private Consumer<String> onChange = name -> {
  };

  @FXML
  private void initialize() {
    previewLabel.setFont(MaterialIcons.font(PREVIEW_GLYPH_SIZE));

    ObservableList<String> iconNames = FXCollections.observableArrayList(MaterialIcons.iconNames());
    FilteredList<String> filteredIconNames = new FilteredList<>(iconNames, name -> true);
    iconCombo.setItems(filteredIconNames);
    iconCombo.setEditable(true);
    iconCombo.setCellFactory(listView -> new IconListCell());

    iconCombo.getEditor().textProperty().addListener((observable, oldValue, newValue) -> {
      updatePreview(newValue);
      if (StringUtils.isEmpty(oldValue) && StringUtils.isEmpty(newValue)) {
        return;
      }
      if (updatingFromModel) {
        return;
      }
      filterSuggestions(filteredIconNames, newValue);
      onChange.accept(blankToNull(newValue));
    });
  }

  public void setValue(String iconName) {
    updatingFromModel = true;
    try {
      iconCombo.getEditor().setText(iconName);
    }
    finally {
      updatingFromModel = false;
    }
  }

  public String getValue() {
    return blankToNull(iconCombo.getEditor().getText());
  }

  /**
   * Invoked after every user-driven edit (not while {@link #setValue} is repopulating the field), with the
   * normalized icon name (blank input mapped to {@code null}).
   */
  public void setOnChange(@NonNull Consumer<String> onChange) {
    this.onChange = onChange;
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
