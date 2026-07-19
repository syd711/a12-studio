package de.a12.studio.ui.editors.typedefinitionmodel;

import de.a12.studio.dataservices.models.ModelType;
import de.a12.studio.dataservices.models.documentmodel.DocumentModel;
import de.a12.studio.dataservices.models.documentmodel.FieldType;
import de.a12.studio.dataservices.models.documentmodel.TypeDefFieldType;
import de.a12.studio.dataservices.models.documentmodel.TypeDefinition;
import de.a12.studio.ui.components.SearchFieldController;
import de.a12.studio.ui.util.localsettings.BaseTableSettings;
import de.a12.studio.ui.util.localsettings.LocalUISettings;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import org.jspecify.annotations.NonNull;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Consumer;

public class TypeDefinitionTableController implements Initializable {

  private static final String TABLE_SETTINGS_ID = ModelType.TYPEDEFINITION.getValue();

  private static final String NAME_COLUMN_ID = "name";
  private static final String BASE_TYPE_COLUMN_ID = "baseType";

  @FXML
  private SearchFieldController searchController;

  @FXML
  private TableView<TypeDefinition> typeDefinitionsTable;

  @FXML
  private TableColumn<TypeDefinition, String> nameColumn;

  @FXML
  private TableColumn<TypeDefinition, String> baseTypeColumn;

  private List<TypeDefinition> typeDefinitions = List.of();

  private Consumer<TypeDefinition> selectionListener;

  public void setSelectionListener(@NonNull Consumer<TypeDefinition> selectionListener) {
    this.selectionListener = selectionListener;
  }

  public void load(@NonNull DocumentModel model) {
    this.typeDefinitions = model.getContent().getTypeDefinitions();
    applyFilter(searchController.getText());
  }

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    searchController.setOnSearch(this::applyFilter);

    typeDefinitionsTable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
    typeDefinitionsTable.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
      if (selectionListener != null) {
        selectionListener.accept(newValue);
      }
    });

    nameColumn.setCellValueFactory(param -> new ReadOnlyStringWrapper(param.getValue().getName()));
    baseTypeColumn.setCellValueFactory(param -> new ReadOnlyStringWrapper(getBaseTypeName(param.getValue())));

    BaseTableSettings tableSettings = LocalUISettings.getTablePreference(TABLE_SETTINGS_ID);
    applyColumnWidth(nameColumn, tableSettings, NAME_COLUMN_ID);
    applyColumnWidth(baseTypeColumn, tableSettings, BASE_TYPE_COLUMN_ID);

    nameColumn.widthProperty().addListener((observable, oldValue, newValue) ->
        saveColumnWidth(NAME_COLUMN_ID, newValue.doubleValue()));
    baseTypeColumn.widthProperty().addListener((observable, oldValue, newValue) ->
        saveColumnWidth(BASE_TYPE_COLUMN_ID, newValue.doubleValue()));
  }

  private void applyFilter(String filter) {
    String term = filter == null ? "" : filter.trim().toLowerCase();
    List<TypeDefinition> filtered = term.isEmpty()
        ? typeDefinitions
        : typeDefinitions.stream()
            .filter(typeDefinition -> typeDefinition.getName() != null && typeDefinition.getName().toLowerCase().contains(term))
            .toList();
    typeDefinitionsTable.setItems(FXCollections.observableArrayList(filtered));
  }

  private static String getBaseTypeName(@NonNull TypeDefinition typeDefinition) {
    FieldType fieldType = typeDefinition.getFieldType();
    if (fieldType == null) {
      return "";
    }
    if (fieldType instanceof TypeDefFieldType) {
      return "Type Definition";
    }
    String type = fieldType.getType();
    return type == null ? "" : type.replace("Type", "");
  }

  private void applyColumnWidth(@NonNull TableColumn<TypeDefinition, String> column, BaseTableSettings tableSettings,
                                 @NonNull String columnId) {
    if (tableSettings == null) {
      return;
    }
    double width = tableSettings.getColumnWidth(columnId);
    if (width > 0) {
      column.setPrefWidth(width);
    }
  }

  private void saveColumnWidth(@NonNull String columnId, double width) {
    BaseTableSettings tableSettings = LocalUISettings.getTablePreference(TABLE_SETTINGS_ID);
    if (tableSettings == null) {
      return;
    }
    tableSettings.getColumnWith().put(columnId, width);
    tableSettings.save();
  }
}
