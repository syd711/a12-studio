package de.a12.studio.ui.editors.typedefinitionmodel;

import de.a12.studio.ui.util.StudioBundle;

import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.ui.components.DialogController;
import de.a12.studio.ui.util.WidgetFactory;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.Optional;

/**
 * Modal picker for the "Import" action in {@link TypeDefinitionTableController}'s toolbar: lets the user pick
 * one Type Definition Model whose whole {@code typeDefinitions} list should become an Import (see {@link
 * de.a12.studio.models.ModelReference#PURPOSE_TYPE_DEFINITIONS}), mirroring SME's own import picker (a single
 * model-name field, not a per-type-definition selection - see {@code importTypeDefsView.tsx}'s
 * {@code DomainImportTypeDefs} form, which has exactly one field, {@code Import.tdModelName}).
 */
public class ImportTypeDefDialogController implements DialogController {

  @FXML
  private ComboBox<DocumentModel> tdModelComboBox;

  @FXML
  private Label emptyLabel;

  @FXML
  private Button okButton;

  private Stage stage;

  private Optional<ButtonType> result = Optional.of(ButtonType.CANCEL);

  @FXML
  private void initialize() {
    tdModelComboBox.setConverter(new StringConverter<>() {
      @Override
      public String toString(DocumentModel model) {
        return model == null ? "" : model.getId();
      }

      @Override
      public DocumentModel fromString(String string) {
        return null;
      }
    });

    okButton.disableProperty().bind(Bindings.createBooleanBinding(
        () -> tdModelComboBox.getValue() == null, tdModelComboBox.valueProperty()));
  }

  @Override
  public void onDialogCancel() {
    stage.close();
  }

  @FXML
  private void onDialogSubmit() {
    result = Optional.of(ButtonType.OK);
    stage.close();
  }

  private void init(Stage stage, @NonNull List<DocumentModel> candidates) {
    this.stage = stage;
    tdModelComboBox.getItems().setAll(candidates);
    boolean empty = candidates.isEmpty();
    emptyLabel.setVisible(empty);
    emptyLabel.setManaged(empty);
    tdModelComboBox.setVisible(!empty);
    tdModelComboBox.setManaged(!empty);
    if (!empty) {
      tdModelComboBox.getSelectionModel().selectFirst();
    }
  }

  private Optional<String> getResult() {
    if (result.isPresent() && result.get() == ButtonType.OK && tdModelComboBox.getValue() != null) {
      return Optional.of(tdModelComboBox.getValue().getId());
    }
    return Optional.empty();
  }

  /**
   * Every {@code candidates} entry must already be filtered to what's actually importable (see {@link
   * TypeDefinitionTableController#importCandidates()}: eligible Type Definition Models, excluding ones
   * already imported or that would close an import cycle) - this dialog itself applies no further filtering.
   */
  public static Optional<String> show(Stage owner, @NonNull List<DocumentModel> candidates) {
    FXMLLoader fxmlLoader = new FXMLLoader(ImportTypeDefDialogController.class.getResource("import-typedef-dialog.fxml"));
    fxmlLoader.setResources(StudioBundle.getBundle());
    Stage stage = WidgetFactory.createDialogStage("import-typedef-dialog", fxmlLoader, owner, "Import Type Definitions");
    ImportTypeDefDialogController controller = (ImportTypeDefDialogController) stage.getUserData();
    controller.init(stage, candidates);
    stage.showAndWait();
    return controller.getResult();
  }
}
