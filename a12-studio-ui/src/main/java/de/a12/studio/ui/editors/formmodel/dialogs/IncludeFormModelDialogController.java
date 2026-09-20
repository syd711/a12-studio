package de.a12.studio.ui.editors.formmodel.dialogs;

import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.formmodel.FormModel;
import de.a12.studio.modelsvalidation.formincludes.FormIncludeException;
import de.a12.studio.modelsvalidation.formincludes.FormIncludeExpander;
import de.a12.studio.ui.components.DialogController;
import de.a12.studio.ui.util.StudioBundle;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Optional;

/**
 * "Include Form Model" dialog: pick the Form Model whose first screen is copied into the host, and the group of the
 * host's Document Model that stands for the source's Document Model root (see {@link FormIncludeExpander}). Every
 * change re-runs the expansion as a dry run, so the summary - or the reason it cannot be done - is always in front of
 * the user, and OK is only enabled for an include that will work. Nothing is changed here: the caller inserts the
 * {@link #getExpansion() expansion} once OK was pressed.
 */
public class IncludeFormModelDialogController implements DialogController {

  @FXML
  private ComboBox<FormModel> sourceComboBox;
  @FXML
  private ComboBox<String> pathComboBox;
  @FXML
  private TextField nameTextField;
  @FXML
  private Label summaryLabel;
  @FXML
  private Label errorLabel;
  @FXML
  private Button okButton;

  private Stage stage;
  private FormModel host;
  private DocumentModel hostModel;
  private List<DocumentModel> documentModels;
  private FormIncludeExpander expander;
  private String includeId;
  private boolean gridSlot;

  private FormIncludeExpander.@Nullable Expansion expansion;
  private boolean confirmed;

  @FXML
  private void initialize() {
    sourceComboBox.setConverter(new StringConverter<>() {
      @Override
      public String toString(FormModel model) {
        return model == null ? "" : model.getId();
      }

      @Override
      public FormModel fromString(String string) {
        return null;
      }
    });
    sourceComboBox.setCellFactory(list -> new ListCell<>() {
      @Override
      protected void updateItem(FormModel item, boolean empty) {
        super.updateItem(item, empty);
        setText(empty || item == null ? null : item.getId());
      }
    });
    sourceComboBox.valueProperty().addListener((observable, oldValue, newValue) -> onSourceChanged());
    pathComboBox.getEditor().textProperty().addListener((observable, oldValue, newValue) -> recompute());
    nameTextField.textProperty().addListener((observable, oldValue, newValue) -> recompute());
    okButton.setDisable(true);
  }

  /**
   * @param gridSlot whether the include goes into the grid slot of an Embedded Repeat, which only takes exactly one
   *                 Control Grid - anything else is reported as the reason the include cannot be done
   */
  void init(@NonNull Stage stage, @NonNull FormModel host, @NonNull DocumentModel hostModel, @NonNull List<FormModel> sources,
      @NonNull List<DocumentModel> documentModels, @NonNull String includeId, boolean gridSlot) {
    this.gridSlot = gridSlot;
    this.stage = stage;
    this.host = host;
    this.hostModel = hostModel;
    this.documentModels = documentModels;
    this.expander = new FormIncludeExpander(documentModels);
    this.includeId = includeId;
    sourceComboBox.getItems().setAll(sources);
    if (sources.isEmpty()) {
      errorLabel.setText(StudioBundle.get("include_form_model.no_source"));
    }
    else {
      sourceComboBox.getSelectionModel().selectFirst();
    }
  }

  /** The path options follow the source: the groups of the host's Document Model that include the source's. */
  private void onSourceChanged() {
    FormModel source = sourceComboBox.getValue();
    List<String> candidates = source == null ? List.of()
        : FormIncludeExpander.documentModelOf(source, documentModels)
            .map(sourceModel -> FormIncludeExpander.candidateHostPaths(hostModel, sourceModel, documentModels)).orElse(List.of());
    pathComboBox.getItems().setAll(candidates);
    pathComboBox.getEditor().setText(candidates.isEmpty() ? "" : candidates.get(0));
    recompute();
  }

  private void recompute() {
    expansion = null;
    summaryLabel.setText("");
    FormModel source = sourceComboBox.getValue();
    String path = pathComboBox.getEditor().getText();
    if (source == null) {
      okButton.setDisable(true);
      return;
    }
    if (path == null || path.isBlank()) {
      errorLabel.setText(StudioBundle.get("include_form_model.path_required"));
      okButton.setDisable(true);
      return;
    }
    try {
      FormIncludeExpander.Expansion result = expander.expand(source, host, path, includeId, nameTextField.getText());
      if (gridSlot && !result.isSingleControlGrid()) {
        errorLabel.setText(StudioBundle.get("include_form_model.grid_required", source.getId()));
        okButton.setDisable(true);
        return;
      }
      expansion = result;
      errorLabel.setText("");
      int configurationEntries = result.fieldEntries().size() + result.groupEntries().size();
      String summary = StudioBundle.get("include_form_model.summary", result.elements().size(), configurationEntries);
      summaryLabel.setText(result.warnings().isEmpty() ? summary : summary + "\n" + String.join("\n", result.warnings()));
      // A name only makes sense for a single element - with several, each is named after the include.
      nameTextField.setDisable(result.elements().size() != 1);
    }
    catch (FormIncludeException e) {
      errorLabel.setText(e.getMessage());
    }
    okButton.setDisable(expansion == null);
  }

  @Override
  public void onDialogCancel() {
    stage.close();
  }

  @FXML
  private void onDialogSubmit() {
    confirmed = true;
    stage.close();
  }

  Optional<FormIncludeExpander.Expansion> getExpansion() {
    return confirmed ? Optional.ofNullable(expansion) : Optional.empty();
  }
}
