package de.a12.studio.ui.editors.structuralmappingmodel.dialogs;

import de.a12.studio.models.structuralmappingmodel.StructuralMappingModelContent;
import de.a12.studio.ui.components.DialogController;
import de.a12.studio.ui.util.StudioBundle;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * Modal dialog that moves a field mapping to another mapping block (SME's {@code MoveFieldMappingDialog}). The
 * blocks on offer are the ones the kernel says the mapping can be added to - it would collide in some others, e.g.
 * if they already write the same target field - plus the one it is in now and, last, a new block.
 */
public class MoveFieldMappingDialogController implements DialogController {

  /**
   * One place the mapping can go.
   *
   * @param label    what the combo box shows
   * @param modified the content with the mapping added there, {@code null} for the block the mapping is in now
   */
  public record Choice(String label, @Nullable StructuralMappingModelContent modified) {
  }

  @FXML
  private Label infoLabel;

  @FXML
  private ComboBox<Choice> blockCombo;

  @FXML
  private Button okButton;

  private Stage stage;

  private Choice current;

  private Choice result;

  @FXML
  private void initialize() {
    blockCombo.setConverter(new StringConverter<>() {
      @Override
      public String toString(Choice choice) {
        return choice == null ? "" : choice.label();
      }

      @Override
      public Choice fromString(String string) {
        return null;
      }
    });
    okButton.disableProperty().bind(blockCombo.valueProperty().isNull());
  }

  void initDialog(@NonNull Stage stage, @NonNull String mappingDescription, @NonNull List<Choice> choices, @NonNull Choice current) {
    this.stage = stage;
    this.current = current;
    infoLabel.setText(StudioBundle.get("structural_mapping.move_info", mappingDescription));
    blockCombo.setItems(FXCollections.observableArrayList(choices));
    blockCombo.setValue(current);
  }

  @Override
  public void onDialogCancel() {
    stage.close();
  }

  @FXML
  private void onDialogSubmit() {
    result = blockCombo.getValue();
    stage.close();
  }

  /** The chosen place, or {@code null} if the dialog was cancelled or the current block was kept. */
  @Nullable Choice getResult() {
    return result == current ? null : result;
  }
}
