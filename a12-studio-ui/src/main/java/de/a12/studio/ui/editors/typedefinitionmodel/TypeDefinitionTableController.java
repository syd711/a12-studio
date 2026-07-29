package de.a12.studio.ui.editors.typedefinitionmodel;

import de.a12.studio.models.ModelType;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.documentmodel.FieldType;
import de.a12.studio.models.documentmodel.StringFieldType;
import de.a12.studio.models.documentmodel.TypeDefFieldType;
import de.a12.studio.models.documentmodel.TypeDefinition;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.components.SearchFieldController;
import de.a12.studio.ui.editors.PropertyEditorSaveMode;
import de.a12.studio.ui.util.FileUtils;
import de.a12.studio.ui.util.Icons;
import de.a12.studio.ui.util.WidgetFactory;
import de.a12.studio.ui.util.localsettings.BaseTableSettings;
import de.a12.studio.ui.util.localsettings.LocalUISettings;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
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
import org.kordamp.ikonli.javafx.FontIcon;

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

  private Runnable onItemAddedListener;

  // Immediate by default: Add/Delete are persisted right away, correct for this table bound to the currently
  // selected project item outside of any dialog. Switched to a shared PropertyEditorSaveMode.Deferred instance
  // via setSaveMode() when this table is embedded in a dialog with its own Save button, so Add/Delete are only
  // persisted once that button is pressed.
  private PropertyEditorSaveMode saveMode = PropertyEditorSaveMode.IMMEDIATE;

  public void setSaveMode(@NonNull PropertyEditorSaveMode saveMode) {
    this.saveMode = saveMode;
  }

  public void setSelectionListener(@NonNull Consumer<TypeDefinition> selectionListener) {
    this.selectionListener = selectionListener;
  }

  public void setOnItemAdded(@NonNull Runnable onItemAddedListener) {
    this.onItemAddedListener = onItemAddedListener;
  }

  public void load(@NonNull DocumentModel model) {
    String selectedId = getSelectedId();
    this.typeDefinitions = model.getContent().getTypeDefinitions();
    applyFilter(searchController.getText());
    if (selectedId != null) {
      selectById(selectedId);
    }
  }

  private String getSelectedId() {
    List<TypeDefinition> selectedItems = typeDefinitionsTable.getSelectionModel().getSelectedItems();
    return selectedItems.size() == 1 ? selectedItems.get(0).getId() : null;
  }

  private void selectById(@NonNull String id) {
    typeDefinitionsTable.getItems().stream()
        .filter(typeDefinition -> id.equals(typeDefinition.getId()))
        .findFirst()
        .ifPresent(this::selectTypeDefinition);
  }

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    searchController.setOnSearch(this::applyFilter);

    typeDefinitionsTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    typeDefinitionsTable.getSelectionModel().getSelectedItems().addListener((ListChangeListener<TypeDefinition>) change -> {
      if (selectionListener == null) {
        return;
      }
      List<TypeDefinition> selectedItems = typeDefinitionsTable.getSelectionModel().getSelectedItems();
      selectionListener.accept(selectedItems.size() == 1 ? selectedItems.get(0) : null);
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
        setContextMenu(empty || item == null ? null : createContextMenu(this));
      }
    });
  }

  @FXML
  private void onAdd() {
    String name = WidgetFactory.showInputDialog(Studio.stage, "New Type", "Name", null, null, uniqueName(DEFAULT_NAME));
    if (name == null) {
      return;
    }

    name = name.trim();
    if (!FileUtils.isValidWindowsFilename(name)) {
      WidgetFactory.showAlert(Studio.stage, "Invalid name", "The name must be a valid filename and must not contain whitespace.");
      return;
    }

    TypeDefinition typeDefinition = new TypeDefinition();
    typeDefinition.setId(generateId());
    typeDefinition.setName(name);
    typeDefinition.setFieldType(new StringFieldType());

    typeDefinitions.add(typeDefinition);
    searchController.clear();
    applyFilter(searchController.getText());
    selectTypeDefinition(typeDefinition);
    if (onItemAddedListener != null) {
      onItemAddedListener.run();
    }
    save();
  }

  private ContextMenu createContextMenu(@NonNull TableRow<TypeDefinition> row) {
    FontIcon deleteIcon = WidgetFactory.createIcon(Icons.TRASH);
    deleteIcon.getStyleClass().add("menu-icon");

    MenuItem deleteItem = new MenuItem("Delete", deleteIcon);
    deleteItem.setOnAction(event -> onDelete(row));

    ContextMenu contextMenu = new ContextMenu();
    contextMenu.getItems().add(deleteItem);
    return contextMenu;
  }

  private void onDelete(@NonNull TableRow<TypeDefinition> row) {
    List<TypeDefinition> selectedItems = List.copyOf(typeDefinitionsTable.getSelectionModel().getSelectedItems());
    List<TypeDefinition> itemsToDelete = selectedItems.size() > 1 && selectedItems.contains(row.getItem())
        ? selectedItems
        : List.of(row.getItem());
    deleteTypeDefinitions(itemsToDelete);
  }

  @FXML
  private void onDelete() {
    List<TypeDefinition> selectedItems = List.copyOf(typeDefinitionsTable.getSelectionModel().getSelectedItems());
    if (selectedItems.isEmpty()) {
      return;
    }
    deleteTypeDefinitions(selectedItems);
  }

  private void deleteTypeDefinitions(@NonNull List<TypeDefinition> itemsToDelete) {
    String message = itemsToDelete.size() > 1
        ? "Delete " + itemsToDelete.size() + " type definitions?"
        : "Delete this type definition?";
    Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, message, null, null, "Delete");
    if (result.isEmpty() || result.get() != ButtonType.OK) {
      return;
    }

    typeDefinitions.removeAll(itemsToDelete);
    applyFilter(searchController.getText());
    save();
  }

  private void selectTypeDefinition(@NonNull TypeDefinition typeDefinition) {
    int row = typeDefinitionsTable.getItems().indexOf(typeDefinition);
    if (row >= 0) {
      typeDefinitionsTable.getSelectionModel().clearAndSelect(row);
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
      saveMode.commit(projectItem);
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
