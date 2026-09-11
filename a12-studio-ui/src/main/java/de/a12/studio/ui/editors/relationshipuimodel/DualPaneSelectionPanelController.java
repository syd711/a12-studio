package de.a12.studio.ui.editors.relationshipuimodel;

import de.a12.studio.models.relationshipuimodel.Button;
import de.a12.studio.models.relationshipuimodel.DualPaneSelectionComponent;
import de.a12.studio.models.relationshipuimodel.RelationshipUiModel;
import de.a12.studio.ui.editors.AbstractPropertyEditor;
import de.a12.studio.ui.editors.propertyeditors.EventButtonsPanelController;
import de.a12.studio.ui.util.StudioBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import org.jspecify.annotations.NonNull;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/** Edits a {@link DualPaneSelectionComponent}: both required Overview Model panes, the optional Link Form
 * Model, height and action buttons. */
public class DualPaneSelectionPanelController extends AbstractPropertyEditor implements Initializable {

  @FXML
  private ComboBox<String> availableItemsField;
  @FXML
  private ComboBox<String> selectedItemsField;
  @FXML
  private ComboBox<String> linkFormModelField;
  @FXML
  private TextField heightField;
  @FXML
  private EventButtonsPanelController buttonsController;

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

    availableItemsField.valueProperty().addListener((observable, oldValue, newValue) -> {
      if (updatingFromModel || model == null) {
        return;
      }
      component().setAvailableItemsOverviewModel(newValue);
      applyAndCommit();
    });
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
  }

  public void setModel(@NonNull RelationshipUiModel model) {
    this.model = model;
    DualPaneSelectionComponent component = component();

    updatingFromModel = true;
    try {
      availableItemsField.getItems().setAll(overviewModelOptions);
      availableItemsField.setValue(component.getAvailableItemsOverviewModel());
      selectedItemsField.getItems().setAll(overviewModelOptions);
      selectedItemsField.setValue(component.getSelectedItemsOverviewModel());
      linkFormModelField.getItems().setAll(ComboOptions.withNoneOption(formModelOptions));
      linkFormModelField.setValue(component.getLinkFormModel() == null ? "" : component.getLinkFormModel());
      setFieldValue(heightField, component.getHeight());
    }
    finally {
      updatingFromModel = false;
    }
    buttonsController.configure(StudioBundle.get("buttons"), ".dualPane", component.getButtons(), Button::new);
  }

  private DualPaneSelectionComponent component() {
    return (DualPaneSelectionComponent) model.getContent().getComponent();
  }

  private void applyAndCommit() {
    onChange.run();
    commitHeaderChange();
  }
}
