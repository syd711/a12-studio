package de.a12.studio.ui.editors.formmodel.formtree.nodeeditors;

import de.a12.studio.models.formmodel.AbstractRepeat;
import de.a12.studio.models.formmodel.DefaultRowAction;
import de.a12.studio.modelsvalidation.validators.form.DefaultRowActionSupport;
import de.a12.studio.ui.editors.AbstractPropertyEditor;
import de.a12.studio.ui.util.StudioBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.util.StringConverter;
import org.jspecify.annotations.NonNull;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

/**
 * "Default Row Action" property editor for a selected Detached or Embedded {@link AbstractRepeat} (hidden for
 * an Inline Repeat, which has none): which row action runs when a row is clicked, plus whether that action's
 * button is hidden from the action column (SME docs, "Default Row Action"). Offers "Edit/View", "Download" if
 * multi file upload is enabled, and every custom row action without a confirmation; see {@link
 * DefaultRowActionSupport} for those rules and the wire shape.
 * <p>
 * The offered actions depend on the repeat's row actions and multi file upload, which are edited in sibling
 * panels, so {@link #refresh()} re-reads them; {@link FormNodeEditorRepeatPanelController} calls it after those
 * panels changed something. Not tied to a document-model {@code Element}, so it follows the model-header
 * pattern: a plain {@link #setRepeat} entry point and {@link #commitHeaderChange()}.
 */
public class RepeatDefaultRowActionPanelController extends AbstractPropertyEditor implements Initializable {

  @FXML
  private ComboBox<String> eventCombo;
  @FXML
  private CheckBox hideButtonCheckBox;

  private AbstractRepeat repeat;

  // Set while the combo is repopulated from the model, so those changes aren't taken for user edits.
  private boolean populating;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    super.initialize(location, resources);

    eventCombo.setConverter(new StringConverter<>() {
      @Override
      public String toString(String technicalEvent) {
        if (technicalEvent == null) {
          return StudioBundle.get("default_row_action.none");
        }
        return switch (technicalEvent) {
          case DefaultRowActionSupport.EDIT -> StudioBundle.get("default_row_action.edit");
          case DefaultRowActionSupport.DOWNLOAD -> StudioBundle.get("default_row_action.download");
          default -> StudioBundle.get("default_row_action.custom", technicalEvent);
        };
      }

      @Override
      public String fromString(String displayName) {
        return null;
      }
    });

    eventCombo.valueProperty().addListener((observable, oldValue, newValue) -> {
      if (!populating) {
        Boolean hideButton = repeat.getDefaultRowAction() == null ? null : repeat.getDefaultRowAction().getHideButton();
        repeat.setDefaultRowAction(DefaultRowActionSupport.fromTechnicalEvent(newValue, hideButton));
        hideButtonCheckBox.setDisable(newValue == null);
        commitHeaderChange();
      }
    });
    hideButtonCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
      DefaultRowAction action = repeat.getDefaultRowAction();
      if (!populating && action != null) {
        action.setHideButton(newValue ? Boolean.TRUE : null);
        commitHeaderChange();
      }
    });
  }

  public void setRepeat(@NonNull AbstractRepeat repeat) {
    this.repeat = repeat;
    setEditorVisible(DefaultRowActionSupport.isSupported(repeat));
    refresh();
  }

  /** Re-reads the offered actions and the current value from the repeat. */
  public void refresh() {
    if (repeat == null) {
      return;
    }
    List<String> items = new ArrayList<>();
    items.add(null);
    items.addAll(DefaultRowActionSupport.candidates(repeat));
    String current = DefaultRowActionSupport.technicalEvent(repeat.getDefaultRowAction());
    if (current != null && !items.contains(current)) {
      // An invalid default (the validator reports it) still has to be shown rather than silently dropped.
      items.add(current);
    }
    populating = true;
    try {
      eventCombo.getItems().setAll(items);
      eventCombo.setValue(current);
      DefaultRowAction action = repeat.getDefaultRowAction();
      hideButtonCheckBox.setSelected(action != null && Boolean.TRUE.equals(action.getHideButton()));
      hideButtonCheckBox.setDisable(current == null);
    }
    finally {
      populating = false;
    }
  }
}
