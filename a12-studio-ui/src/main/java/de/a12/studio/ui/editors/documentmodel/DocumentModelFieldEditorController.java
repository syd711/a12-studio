package de.a12.studio.ui.editors.documentmodel;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.TextFieldTableCell;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class DocumentModelFieldEditorController implements Initializable {

  private static final List<String> DATA_TYPES = List.of("String", "Number", "Boolean", "Date", "Object");

  @FXML
  private TextField nameField;

  @FXML
  private TextField idField;

  @FXML
  private TextField pathField;

  @FXML
  private CheckBox typeDefinitionCheckBox;

  @FXML
  private ComboBox<String> dataTypeComboBox;

  @FXML
  private CheckBox globalCheckBox;

  @FXML
  private CheckBox transientCheckBox;

  @FXML
  private CheckBox requiredCheckBox;

  @FXML
  private TextField minLengthField;

  @FXML
  private TextField maxLengthField;

  @FXML
  private TextField patternField;

  @FXML
  private CheckBox lineBreaksCheckBox;

  @FXML
  private CheckBox alphabeticalSortingCheckBox;

  @FXML
  private TableView<LocalizedText> labelsTable;

  @FXML
  private TableColumn<LocalizedText, String> labelsLocaleColumn;

  @FXML
  private TableColumn<LocalizedText, String> labelsTextColumn;

  @FXML
  private TextArea internalDescriptionArea;

  @FXML
  private TextArea externalDescriptionArea;

  @FXML
  private TableView<LocalizedText> helperTextTable;

  @FXML
  private TableColumn<LocalizedText, String> helperTextLocaleColumn;

  @FXML
  private TableColumn<LocalizedText, String> helperTextTextColumn;

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    dataTypeComboBox.getItems().addAll(DATA_TYPES);
    dataTypeComboBox.getSelectionModel().selectFirst();

    initLocalizedTextTable(labelsTable, labelsLocaleColumn, labelsTextColumn);
    labelsTable.getItems().addAll(
        new LocalizedText("en", "Country"),
        new LocalizedText("de", "Land"));

    initLocalizedTextTable(helperTextTable, helperTextLocaleColumn, helperTextTextColumn);
    helperTextTable.getItems().addAll(
        new LocalizedText("en", ""),
        new LocalizedText("de", ""));
  }

  private void initLocalizedTextTable(TableView<LocalizedText> table, TableColumn<LocalizedText, String> localeColumn,
                                       TableColumn<LocalizedText, String> textColumn) {
    table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
    localeColumn.setCellValueFactory(param -> param.getValue().localeProperty());
    textColumn.setCellValueFactory(param -> param.getValue().textProperty());
    textColumn.setCellFactory(TextFieldTableCell.forTableColumn());
    textColumn.setOnEditCommit(event -> event.getRowValue().setText(event.getNewValue()));
  }

  public static class LocalizedText {

    private final StringProperty locale;

    private final StringProperty text;

    public LocalizedText(String locale, String text) {
      this.locale = new SimpleStringProperty(locale);
      this.text = new SimpleStringProperty(text);
    }

    public StringProperty localeProperty() {
      return locale;
    }

    public StringProperty textProperty() {
      return text;
    }

    public String getText() {
      return text.get();
    }

    public void setText(String value) {
      text.set(value);
    }
  }
}
