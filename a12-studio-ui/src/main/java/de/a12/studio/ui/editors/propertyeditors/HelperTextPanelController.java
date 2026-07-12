package de.a12.studio.ui.editors.propertyeditors;

import de.a12.studio.ui.editors.AbstractPropertyEditor;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.TextFieldTableCell;

import java.net.URL;
import java.util.ResourceBundle;

public class HelperTextPanelController extends AbstractPropertyEditor implements Initializable {

  @FXML
  private TableView<LocalizedText> helperTextTable;

  @FXML
  private TableColumn<LocalizedText, String> helperTextLocaleColumn;

  @FXML
  private TableColumn<LocalizedText, String> helperTextTextColumn;

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    super.initialize(url, resourceBundle);

    helperTextTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
    helperTextLocaleColumn.setCellValueFactory(param -> param.getValue().localeProperty());
    helperTextTextColumn.setCellValueFactory(param -> param.getValue().textProperty());
    helperTextTextColumn.setCellFactory(TextFieldTableCell.forTableColumn());
    helperTextTextColumn.setOnEditCommit(event -> event.getRowValue().setText(event.getNewValue()));

    helperTextTable.getItems().addAll(
        new LocalizedText("en", ""),
        new LocalizedText("de", ""));
  }
}
