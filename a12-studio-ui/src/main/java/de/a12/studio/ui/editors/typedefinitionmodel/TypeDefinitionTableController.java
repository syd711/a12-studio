package de.a12.studio.ui.editors.typedefinitionmodel;

import de.a12.studio.dataservices.models.ModelType;
import de.a12.studio.dataservices.models.documentmodel.DocumentModel;
import de.a12.studio.dataservices.models.documentmodel.FieldType;
import de.a12.studio.dataservices.models.documentmodel.StringFieldType;
import de.a12.studio.dataservices.models.documentmodel.TypeDefFieldType;
import de.a12.studio.dataservices.models.documentmodel.TypeDefinition;
import de.a12.studio.dataservices.projects.ProjectItem;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.components.SearchFieldController;
import de.a12.studio.ui.util.Icons;
import de.a12.studio.ui.util.WidgetFactory;
import de.a12.studio.ui.util.localsettings.BaseTableSettings;
import de.a12.studio.ui.util.localsettings.LocalUISettings;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import org.jspecify.annotations.NonNull;

import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

public class TypeDefinitionTableController implements Initializable {

  private static final String TABLE_SETTINGS_ID = ModelType.TYPEDEFINITION.getValue();

  private static final String NAME_COLUMN_ID = "name";
  private static final String BASE_TYPE_COLUMN_ID = "baseType";

  private static final String ID_PREFIX = "typedef_";
  private static final String DEFAULT_NAME = "NewType";

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

    typeDefinitionsTable.setRowFactory(table -> new TableRow<>() {
      @Override
      protected void updateItem(TypeDefinition item, boolean empty) {
        super.updateItem(item, empty);
        setContextMenu(empty || item == null ? null : createContextMenu(item));
      }
    });
  }

  @FXML
  private void onAdd() {
    TypeDefinition typeDefinition = new TypeDefinition();
    typeDefinition.setId(generateId());
    typeDefinition.setName(uniqueName(DEFAULT_NAME));
    typeDefinition.setFieldType(new StringFieldType());

    typeDefinitions.add(typeDefinition);
    searchController.clear();
    applyFilter(searchController.getText());
    selectTypeDefinition(typeDefinition);
    save();
  }

  private ContextMenu createContextMenu(@NonNull TypeDefinition typeDefinition) {
    MenuItem deleteItem = new MenuItem("Delete", WidgetFactory.createIcon(Icons.TRASH));
    deleteItem.setOnAction(event -> onDelete(typeDefinition));

    ContextMenu contextMenu = new ContextMenu();
    contextMenu.getItems().add(deleteItem);
    return contextMenu;
  }

  private void onDelete(@NonNull TypeDefinition typeDefinition) {
    Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, "Delete this type definition?", null, null, "Delete");
    if (result.isEmpty() || result.get() != ButtonType.OK) {
      return;
    }

    typeDefinitions.remove(typeDefinition);
    applyFilter(searchController.getText());
    save();
  }

  private void selectTypeDefinition(@NonNull TypeDefinition typeDefinition) {
    typeDefinitionsTable.getSelectionModel().select(typeDefinition);
    int row = typeDefinitionsTable.getSelectionModel().getSelectedIndex();
    if (row >= 0) {
      typeDefinitionsTable.scrollTo(row);
    }
  }

  private String uniqueName(@NonNull String baseName) {
    Set<String> usedNames = new HashSet<>();
    for (TypeDefinition typeDefinition : typeDefinitions) {
      usedNames.add(typeDefinition.getName());
    }
    if (!usedNames.contains(baseName)) {
      return baseName;
    }
    int suffix = 2;
    while (usedNames.contains(baseName + "_" + suffix)) {
      suffix++;
    }
    return baseName + "_" + suffix;
  }

  private String generateId() {
    return ID_PREFIX + UUID.randomUUID();
  }

  private void save() {
    ProjectItem projectItem = Studio.getSelectedProjectItem();
    if (projectItem != null) {
      projectItem.save();
    }
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
