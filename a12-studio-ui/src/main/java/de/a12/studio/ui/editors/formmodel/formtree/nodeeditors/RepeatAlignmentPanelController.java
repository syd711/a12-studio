package de.a12.studio.ui.editors.formmodel.formtree.nodeeditors;

import de.a12.studio.models.formmodel.AbstractRepeat;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.events.StudioEventManager;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import org.jspecify.annotations.NonNull;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * "Alignment" property editor for a selected {@link AbstractRepeat} node: edits
 * {@link AbstractRepeat#getDefaultHorizontalAlignment()}, the horizontal alignment applied to all
 * columns of the repeat's overview table (header and body). Possible values: "left", "center", "right".
 */
public class RepeatAlignmentPanelController implements Initializable {

  private static final List<String> ALIGNMENT_VALUES = List.of("left", "center", "right");

  @FXML
  private ComboBox<String> horizontalAlignmentCombo;

  private AbstractRepeat repeat;
  private boolean updatingFromModel;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    horizontalAlignmentCombo.getItems().setAll(ALIGNMENT_VALUES);
    horizontalAlignmentCombo.valueProperty().addListener((obs, old, val) -> {
      if (updatingFromModel || repeat == null) return;
      repeat.setDefaultHorizontalAlignment(val == null || val.isBlank() ? null : val);
      commitChange();
    });
  }

  public void setRepeat(@NonNull AbstractRepeat repeat) {
    this.repeat = repeat;
    updatingFromModel = true;
    try {
      horizontalAlignmentCombo.setValue(repeat.getDefaultHorizontalAlignment());
    } finally {
      updatingFromModel = false;
    }
  }

  private void commitChange() {
    ProjectItem item = Studio.getSelectedProjectItem();
    if (item == null) return;
    item.save();
    StudioEventManager.getInstance().fireModelSavedEvent(item);
  }
}
