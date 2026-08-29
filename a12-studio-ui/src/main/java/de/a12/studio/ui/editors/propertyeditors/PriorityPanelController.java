package de.a12.studio.ui.editors.propertyeditors;

import de.a12.studio.models.overviewmodel.OverviewButtonLike;
import de.a12.studio.ui.editors.AbstractPropertyEditor;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import org.jspecify.annotations.NonNull;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Edits an {@link OverviewButtonLike}'s {@code priority} ("PRIMARY"/"SECONDARY", defaulting to "SECONDARY")
 * and {@code destructive} - SME's {@code sme-sme-om-ba-docs.md} "Button Styling" section ("Priority drop-down
 * and Destructive toggle switch"). Embedded in {@link
 * de.a12.studio.ui.editors.propertyeditors.dialogs.EventButtonDialogController}; the {@code icon}/{@code
 * labelHidden} part of the same doc section is edited separately by {@link IconPanelController} and a plain
 * checkbox instead, so this panel only covers Priority/Destructive. Not tied to a single {@code
 * de.a12.studio.models.documentmodel.Element}, so it follows the model-header pattern (a plain {@link
 * #setButton} entry point) rather than {@link #setElement}.
 */
public class PriorityPanelController extends AbstractPropertyEditor implements Initializable {

  private static final List<String> PRIORITIES = List.of("PRIMARY", "SECONDARY");
  private static final String DEFAULT_PRIORITY = "SECONDARY";

  @FXML
  private ComboBox<String> priorityField;

  @FXML
  private CheckBox destructiveField;

  private OverviewButtonLike button;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    super.initialize(location, resources);

    priorityField.getItems().setAll(PRIORITIES);
    bindComboBox(priorityField, (el, value) -> button.setPrimary("PRIMARY".equals(value)));
    bindCheckBox(destructiveField, (el, value) -> button.setDestructive(value ? Boolean.TRUE : null));
  }

  public void setButton(@NonNull OverviewButtonLike button) {
    this.button = button;
    setFieldValue(priorityField, Boolean.TRUE.equals(button.getPrimary()) ? "PRIMARY" : DEFAULT_PRIORITY);
    setFieldValue(destructiveField, Boolean.TRUE.equals(button.getDestructive()));
  }
}
