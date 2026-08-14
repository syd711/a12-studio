package de.a12.studio.ui.preferences;

import de.a12.studio.models.ModelType;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.net.URL;
import java.util.Arrays;
import java.util.ResourceBundle;

public class PreferenceModelVersionPanelController implements Initializable {

  @FXML
  private TableView<ModelType> modelVersionTable;

  @FXML
  private TableColumn<ModelType, String> modelNameColumn;

  @FXML
  private TableColumn<ModelType, String> modelVersionColumn;

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    modelNameColumn.setCellValueFactory(cell ->
        new SimpleStringProperty(cell.getValue().getDisplayName()));

    modelVersionColumn.setCellValueFactory(cell ->
        new SimpleStringProperty(cell.getValue().getCurrentVersion()));

    modelVersionTable.setItems(
        FXCollections.observableList(Arrays.asList(ModelType.values())));
  }
}
