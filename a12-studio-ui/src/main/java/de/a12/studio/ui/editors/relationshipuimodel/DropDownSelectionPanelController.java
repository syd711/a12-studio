package de.a12.studio.ui.editors.relationshipuimodel;

import de.a12.studio.models.relationshipuimodel.DropDownSelectionComponent;
import de.a12.studio.models.relationshipuimodel.RelationshipUiModel;
import de.a12.studio.ui.editors.AbstractPropertyEditor;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import org.jspecify.annotations.NonNull;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/** Edits a {@link DropDownSelectionComponent}: the two required Query Models and the form element it binds to. */
public class DropDownSelectionPanelController extends AbstractPropertyEditor implements Initializable {

  @FXML
  private ComboBox<String> availableItemsQueryField;
  @FXML
  private ComboBox<String> selectedItemQueryField;
  @FXML
  private TextField elementRefField;

  private RelationshipUiModel model;
  private List<String> queryModelOptions = List.of();

  // Set while fields are being repopulated from the model, so those programmatic updates aren't mistaken for
  // user edits and don't trigger onChange/a save.
  private boolean updatingFromModel;

  private Runnable onChange = () -> {
  };

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    super.initialize(location, resources);

    availableItemsQueryField.valueProperty().addListener((observable, oldValue, newValue) -> {
      if (updatingFromModel || model == null) {
        return;
      }
      component().setAvailableItemsQueryModel(newValue);
      applyAndCommit();
    });
    selectedItemQueryField.valueProperty().addListener((observable, oldValue, newValue) -> {
      if (updatingFromModel || model == null) {
        return;
      }
      component().setSelectedItemQueryModel(newValue);
      applyAndCommit();
    });
    elementRefField.textProperty().addListener((observable, oldValue, newValue) -> {
      if (updatingFromModel || model == null) {
        return;
      }
      component().setElementRef(newValue);
      applyAndCommit();
    });
  }

  public void setVisible(boolean visible) {
    setEditorVisible(visible);
  }

  public void setOnChange(@NonNull Runnable onChange) {
    this.onChange = onChange;
  }

  public void setModelOptions(@NonNull List<String> queryModelOptions) {
    this.queryModelOptions = queryModelOptions;
  }

  public void setModel(@NonNull RelationshipUiModel model) {
    this.model = model;
    DropDownSelectionComponent component = component();

    updatingFromModel = true;
    try {
      availableItemsQueryField.getItems().setAll(queryModelOptions);
      availableItemsQueryField.setValue(component.getAvailableItemsQueryModel());
      selectedItemQueryField.getItems().setAll(queryModelOptions);
      selectedItemQueryField.setValue(component.getSelectedItemQueryModel());
      setFieldValue(elementRefField, component.getElementRef());
    }
    finally {
      updatingFromModel = false;
    }
  }

  private DropDownSelectionComponent component() {
    return (DropDownSelectionComponent) model.getContent().getComponent();
  }

  private void applyAndCommit() {
    onChange.run();
    commitHeaderChange();
  }
}
