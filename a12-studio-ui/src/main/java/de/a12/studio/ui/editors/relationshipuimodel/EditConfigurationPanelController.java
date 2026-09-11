package de.a12.studio.ui.editors.relationshipuimodel;

import de.a12.studio.models.relationshipuimodel.EditConfiguration;
import de.a12.studio.models.relationshipuimodel.RelationshipUiModel;
import de.a12.studio.models.relationshipuimodel.TableListComponent;
import de.a12.studio.ui.editors.AbstractPropertyEditor;
import de.a12.studio.ui.editors.propertyeditors.LocalizedTextPanelController;
import de.a12.studio.ui.util.StudioBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import org.jspecify.annotations.NonNull;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Edits a {@link TableListComponent}'s optional {@link EditConfiguration} - the Dual-Pane-shaped picker shown
 * in a modal dialog. Only ever bound (via {@link #setModel}) while {@link TableListComponent#getEditConfiguration()}
 * is already non-null; {@link TableListPanelController} owns creating/clearing it via its "Editable via Modal
 * Dialog" checkbox before showing/hiding this panel.
 */
public class EditConfigurationPanelController extends AbstractPropertyEditor implements Initializable {

  @FXML
  private ComboBox<String> availableItemsField;
  @FXML
  private ComboBox<String> selectedItemsField;
  @FXML
  private LocalizedTextPanelController dialogTitleController;
  @FXML
  private TextField dialogWidthField;
  @FXML
  private TextField dialogMaxWidthField;
  @FXML
  private TextField dialogMaxHeightField;
  @FXML
  private TextField heightField;

  private RelationshipUiModel model;
  private List<String> overviewModelOptions = List.of();

  // Set while fields are being repopulated from the model, so those programmatic updates aren't mistaken for
  // user edits and don't trigger onChange/a save.
  private boolean updatingFromModel;

  private Runnable onChange = () -> {
  };

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    super.initialize(location, resources);

    availableItemsField.valueProperty().addListener((observable, oldValue, newValue) -> {
      if (updatingFromModel || model == null) {
        return;
      }
      editConfiguration().setAvailableItemsOverviewModel(newValue);
      applyAndCommit();
    });
    selectedItemsField.valueProperty().addListener((observable, oldValue, newValue) -> {
      if (updatingFromModel || model == null) {
        return;
      }
      editConfiguration().setSelectedItemsOverviewModel(newValue);
      applyAndCommit();
    });
    dialogWidthField.textProperty().addListener((observable, oldValue, newValue) -> {
      if (updatingFromModel || model == null) {
        return;
      }
      editConfiguration().setDialogWidth(blankToNull(newValue));
      applyAndCommit();
    });
    dialogMaxWidthField.textProperty().addListener((observable, oldValue, newValue) -> {
      if (updatingFromModel || model == null) {
        return;
      }
      editConfiguration().setDialogMaxWidth(blankToNull(newValue));
      applyAndCommit();
    });
    dialogMaxHeightField.textProperty().addListener((observable, oldValue, newValue) -> {
      if (updatingFromModel || model == null) {
        return;
      }
      editConfiguration().setDialogMaxHeight(blankToNull(newValue));
      applyAndCommit();
    });
    heightField.textProperty().addListener((observable, oldValue, newValue) -> {
      if (updatingFromModel || model == null) {
        return;
      }
      editConfiguration().setHeight(blankToNull(newValue));
      applyAndCommit();
    });

    dialogTitleController.configureCustom("dialogTitle", StudioBundle.get("dialog_title"));
  }

  public void setVisible(boolean visible) {
    setEditorVisible(visible);
  }

  public void setOnChange(@NonNull Runnable onChange) {
    this.onChange = onChange;
  }

  public void setOverviewModelOptions(@NonNull List<String> overviewModelOptions) {
    this.overviewModelOptions = overviewModelOptions;
  }

  public void setModel(@NonNull RelationshipUiModel model) {
    this.model = model;
    EditConfiguration editConfiguration = editConfiguration();

    updatingFromModel = true;
    try {
      availableItemsField.getItems().setAll(overviewModelOptions);
      availableItemsField.setValue(editConfiguration.getAvailableItemsOverviewModel());
      selectedItemsField.getItems().setAll(overviewModelOptions);
      selectedItemsField.setValue(editConfiguration.getSelectedItemsOverviewModel());
      setFieldValue(dialogWidthField, editConfiguration.getDialogWidth());
      setFieldValue(dialogMaxWidthField, editConfiguration.getDialogMaxWidth());
      setFieldValue(dialogMaxHeightField, editConfiguration.getDialogMaxHeight());
      setFieldValue(heightField, editConfiguration.getHeight());
    }
    finally {
      updatingFromModel = false;
    }
    dialogTitleController.setCustom(() -> editConfiguration().getDialogTitle());
  }

  private EditConfiguration editConfiguration() {
    return ((TableListComponent) model.getContent().getComponent()).getEditConfiguration();
  }

  private static String blankToNull(String value) {
    return value == null || value.isBlank() ? null : value;
  }

  private void applyAndCommit() {
    onChange.run();
    commitHeaderChange();
  }
}
