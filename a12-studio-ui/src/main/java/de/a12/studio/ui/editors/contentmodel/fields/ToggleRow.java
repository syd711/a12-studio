package de.a12.studio.ui.editors.contentmodel.fields;

import de.a12.studio.models.contentmodel.ContentProps;
import de.a12.studio.ui.util.WidgetFactory;
import javafx.beans.DefaultProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import org.jspecify.annotations.NonNull;

/**
 * A setting with a small fixed set of keyword values shown as a segmented button group (SME's "toggle" settings:
 * Direction, Wrap, Variant, ...). Exactly one option is always selected; when the key is absent the {@link
 * #getInitial() initial} option is shown, as SME shows its controller's initial value. Options are declared as
 * child {@link ToggleOption}s in FXML.
 */
@DefaultProperty("options")
public class ToggleRow extends SettingRow {

  private final ObservableList<ToggleOption> options = FXCollections.observableArrayList();
  private final ToggleGroup group = new ToggleGroup();
  private final HBox buttons = new HBox();

  private String initial;
  private boolean booleanValued;
  private boolean updatingButtons;

  public ToggleRow() {
    buttons.getStyleClass().add("content-setting-segments");
    options.addListener((ListChangeListener<ToggleOption>) change -> rebuild());
    // A segmented control never has "nothing selected": clicking the selected segment keeps it selected.
    group.selectedToggleProperty().addListener((observable, oldToggle, newToggle) -> {
      if (updatingButtons) {
        return;
      }
      if (newToggle == null) {
        if (oldToggle != null) {
          updatingButtons = true;
          try {
            group.selectToggle(oldToggle);
          }
          finally {
            updatingButtons = false;
          }
        }
        return;
      }
      String value = (String) newToggle.getUserData();
      edited(props -> props.set(getPath(), booleanValued ? Boolean.valueOf(value) : value));
    });
  }

  public ObservableList<ToggleOption> getOptions() {
    return options;
  }

  public String getInitial() {
    return initial;
  }

  public void setInitial(String initial) {
    this.initial = initial;
  }

  public boolean isBooleanValued() {
    return booleanValued;
  }

  /** Options {@code "true"}/{@code "false"} are stored as real booleans (SME's Yes/No and Left/Right toggles). */
  public void setBooleanValued(boolean booleanValued) {
    this.booleanValued = booleanValued;
  }

  private void rebuild() {
    updatingButtons = true;
    try {
      // Four or more segments do not fit beside the label in the narrow column: give them a line of their own.
      boolean below = options.size() >= 4;
      controls().getChildren().remove(buttons);
      getChildren().remove(buttons);
      if (below) {
        addBelow(buttons);
      }
      else {
        controls().getChildren().add(buttons);
      }
      buttons.getChildren().clear();
      group.getToggles().clear();
      for (int i = 0; i < options.size(); i++) {
        ToggleOption option = options.get(i);
        ToggleButton button = new ToggleButton(option.getLabel());
        button.setUserData(option.getValue());
        button.setToggleGroup(group);
        button.getStyleClass().add("content-setting-segment");
        button.setMinWidth(javafx.scene.layout.Region.USE_PREF_SIZE);
        if (i == 0) {
          button.getStyleClass().add("first");
        }
        if (i == options.size() - 1) {
          button.getStyleClass().add("last");
        }
        if (option.getTooltip() != null && !option.getTooltip().isBlank()) {
          button.setTooltip(WidgetFactory.createTooltip(option.getTooltip()));
        }
        buttons.getChildren().add(button);
      }
    }
    finally {
      updatingButtons = false;
    }
  }

  @Override
  protected void load(@NonNull ContentProps props) {
    String value = props.getString(getPath());
    if (value == null) {
      value = initial;
    }
    updatingButtons = true;
    try {
      String shown = value;
      group.selectToggle(group.getToggles().stream()
          .filter(toggle -> toggle.getUserData().equals(shown))
          .findFirst()
          .orElse(null));
    }
    finally {
      updatingButtons = false;
    }
  }
}
