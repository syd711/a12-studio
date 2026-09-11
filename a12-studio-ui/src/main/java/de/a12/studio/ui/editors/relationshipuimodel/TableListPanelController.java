package de.a12.studio.ui.editors.relationshipuimodel;

import de.a12.studio.models.relationshipuimodel.Button;
import de.a12.studio.models.relationshipuimodel.EditConfiguration;
import de.a12.studio.models.relationshipuimodel.RelationshipUiModel;
import de.a12.studio.models.relationshipuimodel.TableListComponent;
import de.a12.studio.ui.editors.AbstractPropertyEditor;
import de.a12.studio.ui.editors.propertyeditors.EventButtonsPanelController;
import de.a12.studio.ui.util.StudioBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import org.jspecify.annotations.NonNull;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Edits a {@link TableListComponent}: the required Selected Items Overview Model, the optional Link Form
 * Model/height/buttons, and the optional Edit Dialog (delegated to {@link EditConfigurationPanelController},
 * created/cleared by the "Editable via Modal Dialog" checkbox).
 */
public class TableListPanelController extends AbstractPropertyEditor implements Initializable {

  @FXML
  private ComboBox<String> selectedItemsField;
  @FXML
  private ComboBox<String> linkFormModelField;
  @FXML
  private TextField heightField;
  @FXML
  private EventButtonsPanelController buttonsController;
  @FXML
  private CheckBox editModalEnabledField;
  @FXML
  private EditConfigurationPanelController editConfigurationController;

  private RelationshipUiModel model;
  private List<String> overviewModelOptions = List.of();
  private List<String> formModelOptions = List.of();

  // Set while fields are being repopulated from the model, so those programmatic updates aren't mistaken for
  // user edits and don't trigger onChange/a save.
  private boolean updatingFromModel;

  private Runnable onChange = () -> {
  };

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    super.initialize(location, resources);
    ComboOptions.installNoneConverter(linkFormModelField);

    selectedItemsField.valueProperty().addListener((observable, oldValue, newValue) -> {
      if (updatingFromModel || model == null) {
        return;
      }
      component().setSelectedItemsOverviewModel(newValue);
      applyAndCommit();
    });
    linkFormModelField.valueProperty().addListener((observable, oldValue, newValue) -> {
      if (updatingFromModel || model == null) {
        return;
      }
      component().setLinkFormModel(ComboOptions.valueOrNull(linkFormModelField));
      applyAndCommit();
    });
    heightField.textProperty().addListener((observable, oldValue, newValue) -> {
      if (updatingFromModel || model == null) {
        return;
      }
      component().setHeight(newValue == null || newValue.isBlank() ? null : newValue);
      applyAndCommit();
    });
    editModalEnabledField.selectedProperty().addListener((observable, oldValue, enabled) -> {
      if (updatingFromModel || model == null) {
        return;
      }
      if (enabled) {
        component().setEditConfiguration(new EditConfiguration());
      }
      else {
        component().setEditConfiguration(null);
      }
      updateEditConfigurationVisibility();
      applyAndCommit();
    });

    editConfigurationController.setOnChange(() -> onChange.run());
  }

  public void setVisible(boolean visible) {
    setEditorVisible(visible);
  }

  public void setOnChange(@NonNull Runnable onChange) {
    this.onChange = onChange;
  }

  public void setModelOptions(@NonNull List<String> overviewModelOptions, @NonNull List<String> formModelOptions) {
    this.overviewModelOptions = overviewModelOptions;
    this.formModelOptions = formModelOptions;
    editConfigurationController.setOverviewModelOptions(overviewModelOptions);
  }

  public void setModel(@NonNull RelationshipUiModel model) {
    this.model = model;
    TableListComponent component = component();

    updatingFromModel = true;
    try {
      selectedItemsField.getItems().setAll(overviewModelOptions);
      selectedItemsField.setValue(component.getSelectedItemsOverviewModel());
      linkFormModelField.getItems().setAll(ComboOptions.withNoneOption(formModelOptions));
      linkFormModelField.setValue(component.getLinkFormModel() == null ? "" : component.getLinkFormModel());
      setFieldValue(heightField, component.getHeight());
      editModalEnabledField.setSelected(component.getEditConfiguration() != null);
    }
    finally {
      updatingFromModel = false;
    }
    buttonsController.configure(StudioBundle.get("buttons"), ".tableList", component.getButtons(), Button::new);
    updateEditConfigurationVisibility();
  }

  private void updateEditConfigurationVisibility() {
    boolean enabled = component().getEditConfiguration() != null;
    editConfigurationController.setVisible(enabled);
    if (enabled) {
      editConfigurationController.setModel(model);
    }
  }

  private TableListComponent component() {
    return (TableListComponent) model.getContent().getComponent();
  }

  private void applyAndCommit() {
    onChange.run();
    commitHeaderChange();
  }
}
