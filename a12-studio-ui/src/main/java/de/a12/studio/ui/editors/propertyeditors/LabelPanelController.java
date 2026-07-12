package de.a12.studio.ui.editors.propertyeditors;

import de.a12.studio.ui.editors.AbstractPropertyEditor;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.TextFieldTableCell;

import java.net.URL;
import java.util.ResourceBundle;

public class LabelPanelController extends AbstractPropertyEditor implements Initializable {

  @FXML
  private TableView<LocalizedText> labelsTable;

  @FXML
  private TableColumn<LocalizedText, String> labelsLocaleColumn;

  @FXML
  private TableColumn<LocalizedText, String> labelsTextColumn;

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    super.initialize(url, resourceBundle);

    labelsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
    labelsLocaleColumn.setCellValueFactory(param -> param.getValue().localeProperty());
    labelsTextColumn.setCellValueFactory(param -> param.getValue().textProperty());
    labelsTextColumn.setCellFactory(TextFieldTableCell.forTableColumn());
    labelsTextColumn.setOnEditCommit(event -> event.getRowValue().setText(event.getNewValue()));

    labelsTable.getItems().addAll(
        new LocalizedText("en", "Country"),
        new LocalizedText("de", "Land"));
  }
}
