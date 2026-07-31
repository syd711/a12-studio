package de.a12.studio.ui.editors.typedefinitionmodel;

import de.a12.studio.models.ModelReference;
import de.a12.studio.models.ModelType;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.documentmodel.FieldType;
import de.a12.studio.models.documentmodel.StringFieldType;
import de.a12.studio.models.documentmodel.TypeDefFieldType;
import de.a12.studio.models.documentmodel.TypeDefinition;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.models.typedefinitionmodel.TypeDefinitionModel;
import de.a12.studio.modelsvalidation.validators.TransitiveTypeDefinitions;
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
import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Side;
import javafx.scene.control.Button;
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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class TypeDefinitionTableController implements Initializable {

  private static final String TABLE_SETTINGS_ID = ModelType.TYPEDEFINITION.getValue();

  private static final String NAME_COLUMN_ID = "name";
  private static final String BASE_TYPE_COLUMN_ID = "baseType";
  private static final String SOURCE_COLUMN_ID = "source";

  private static final String ID_PREFIX = "typedef_";
  private static final String DEFAULT_NAME = "NewType";

  /** Applied to rows holding a transitively-included (read-only) type definition, styled in stylesheet.css. */
  private static final PseudoClass INCLUDED_ROW = PseudoClass.getPseudoClass("included");

  @FXML
  private SearchFieldController searchController;

  @FXML
  private TableView<TypeDefinitionRow> typeDefinitionsTable;

  @FXML
  private TableColumn<TypeDefinitionRow, String> nameColumn;

  @FXML
  private TableColumn<TypeDefinitionRow, String> baseTypeColumn;

  @FXML
  private TableColumn<TypeDefinitionRow, String> sourceColumn;

  @FXML
  private Button deleteImportButton;

  // The model this table is showing, and every other model in its project - both needed by Import/Delete
  // Import (which mutate model.getModelReferences()) and by TransitiveTypeDefinitions (which needs a
  // candidate pool to resolve Include/Import references against). Set on every load().
  private DocumentModel model;
  private List<DocumentModel> otherModels = List.of();

  // This model's own typeDefinitions: the live, mutable, ordered list backing the JSON file, exactly as
  // before. Add/Delete only ever touch this list - never the transitively-included rows below.
  private List<TypeDefinition> typeDefinitions = List.of();

  // Type definitions inherited through this model's Include/Import graph (see TransitiveTypeDefinitions),
  // recomputed on every load() and after every Import/Delete Import. Shown alongside typeDefinitions but never
  // mutated by this table: they belong to whichever included/imported model actually owns them.
  private List<TypeDefinitionRow> includedTypeDefinitions = List.of();

  private Consumer<TypeDefinitionRow> selectionListener;

  private Runnable onItemAddedListener;

  // Immediate by default: Add/Delete are persisted right away, correct for this table bound to the currently
  // selected project item outside of any dialog. Switched to a shared PropertyEditorSaveMode.Deferred instance
  // via setSaveMode() when this table is embedded in a dialog with its own Save button, so Add/Delete are only
  // persisted once that button is pressed.
  private PropertyEditorSaveMode saveMode = PropertyEditorSaveMode.IMMEDIATE;

  public void setSaveMode(@NonNull PropertyEditorSaveMode saveMode) {
    this.saveMode = saveMode;
  }

  public void setSelectionListener(@NonNull Consumer<TypeDefinitionRow> selectionListener) {
    this.selectionListener = selectionListener;
  }

  public void setOnItemAdded(@NonNull Runnable onItemAddedListener) {
    this.onItemAddedListener = onItemAddedListener;
  }

  public void load(@NonNull DocumentModel model) {
    load(model, List.of());
  }

  /**
   * @param otherModels every other {@link DocumentModel} in the current project, used to resolve {@code model}'s
   *                     Include/Import graph and pull in any type definitions it inherits from it (see
   *                     {@link TransitiveTypeDefinitions}) and to offer candidates for the Import picker. Pass
   *                     {@code List.of()} when {@code model} can't reference other models at all.
   */
  public void load(@NonNull DocumentModel model, @NonNull List<DocumentModel> otherModels) {
    String selectedId = getSelectedId();
    this.model = model;
    this.otherModels = otherModels;
    this.typeDefinitions = model.getContent().getTypeDefinitions();
    refreshIncludedTypeDefinitions();
    if (selectedId != null) {
      selectById(selectedId);
    }
  }

  private void refreshIncludedTypeDefinitions() {
    this.includedTypeDefinitions = TransitiveTypeDefinitions.resolve(model, otherModels).stream()
        .map(TypeDefinitionRow::included)
        .toList();
    applyFilter(searchController.getText());
    deleteImportButton.setDisable(importReferences().isEmpty());
  }

  private String getSelectedId() {
    List<TypeDefinitionRow> selectedItems = typeDefinitionsTable.getSelectionModel().getSelectedItems();
    return selectedItems.size() == 1 ? selectedItems.get(0).typeDefinition().getId() : null;
  }

  private void selectById(@NonNull String id) {
    typeDefinitionsTable.getItems().stream()
        .filter(row -> id.equals(row.typeDefinition().getId()))
        .findFirst()
        .ifPresent(this::selectRow);
  }

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    searchController.setOnSearch(this::applyFilter);
    deleteImportButton.setDisable(true);

    typeDefinitionsTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    typeDefinitionsTable.getSelectionModel().getSelectedItems().addListener((ListChangeListener<TypeDefinitionRow>) change -> {
      if (selectionListener == null) {
        return;
      }
      List<TypeDefinitionRow> selectedItems = typeDefinitionsTable.getSelectionModel().getSelectedItems();
      selectionListener.accept(selectedItems.size() == 1 ? selectedItems.get(0) : null);
    });

    nameColumn.setCellValueFactory(param -> new ReadOnlyStringWrapper(param.getValue().typeDefinition().getName()));
    baseTypeColumn.setCellValueFactory(param -> new ReadOnlyStringWrapper(getBaseTypeName(param.getValue().typeDefinition())));
    sourceColumn.setCellValueFactory(param -> new ReadOnlyStringWrapper(param.getValue().source()));

    BaseTableSettings tableSettings = LocalUISettings.getTablePreference(TABLE_SETTINGS_ID);
    applyColumnWidth(nameColumn, tableSettings, NAME_COLUMN_ID);
    applyColumnWidth(baseTypeColumn, tableSettings, BASE_TYPE_COLUMN_ID);
    applyColumnWidth(sourceColumn, tableSettings, SOURCE_COLUMN_ID);

    nameColumn.widthProperty().addListener((observable, oldValue, newValue) ->
        saveColumnWidth(NAME_COLUMN_ID, newValue.doubleValue()));
    baseTypeColumn.widthProperty().addListener((observable, oldValue, newValue) ->
        saveColumnWidth(BASE_TYPE_COLUMN_ID, newValue.doubleValue()));
    sourceColumn.widthProperty().addListener((observable, oldValue, newValue) ->
        saveColumnWidth(SOURCE_COLUMN_ID, newValue.doubleValue()));

    typeDefinitionsTable.setRowFactory(table -> new TableRow<>() {
      @Override
      protected void updateItem(TypeDefinitionRow item, boolean empty) {
        super.updateItem(item, empty);
        pseudoClassStateChanged(INCLUDED_ROW, !empty && item != null && !item.editable());
        setContextMenu(empty || item == null || !item.editable() ? null : createContextMenu(this));
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
    selectRow(TypeDefinitionRow.own(typeDefinition));
    if (onItemAddedListener != null) {
      onItemAddedListener.run();
    }
    save();
  }

  /**
   * Opens {@link ImportTypeDefDialogController}'s picker (mirrors SME's Import: pick one whole Type Definition
   * Model, not individual type definitions - see {@link TransitiveTypeDefinitions}'s class doc) and, if the
   * user confirms, adds a header {@link ModelReference} of purpose {@link
   * ModelReference#PURPOSE_TYPE_DEFINITIONS} for it. That reference is the only thing persisted; the imported
   * type definitions themselves are never copied into this model, only resolved for display on every load/
   * refresh (see {@link #refreshIncludedTypeDefinitions()}), same as an Include.
   */
  @FXML
  private void onImport() {
    if (model == null) {
      return;
    }
    ImportTypeDefDialogController.show(Studio.stage, importCandidates()).ifPresent(chosenId -> {
      ModelReference reference = new ModelReference();
      reference.setAlias(chosenId);
      reference.setModelType(ModelType.DOCUMENT);
      reference.setPurpose(ModelReference.PURPOSE_TYPE_DEFINITIONS);
      reference.setReference(chosenId);
      model.getModelReferences().add(reference);
      refreshIncludedTypeDefinitions();
      save();
    });
  }

  /**
   * Every {@link TypeDefinitionModel} in the project that could still become a new Import: excludes ones
   * already imported (a second Import of the same model would just be a no-op duplicate reference) and ones
   * that would close an import cycle (a TDM that already imports {@code model}, directly or transitively -
   * see {@link TransitiveTypeDefinitions#importedModelIds}), mirroring SME's own picker filter in
   * {@code importTypeDefsView.tsx} (locale compatibility aside, which a12-studio doesn't enforce here).
   */
  private List<DocumentModel> importCandidates() {
    Set<String> alreadyImported = importReferences().stream().map(ModelReference::getReference).collect(Collectors.toSet());
    return otherModels.stream()
        .filter(TypeDefinitionModel.class::isInstance)
        .filter(candidate -> !alreadyImported.contains(candidate.getId()))
        .filter(candidate -> !TransitiveTypeDefinitions.importedModelIds(candidate, otherModels).contains(model.getId()))
        .sorted(Comparator.comparing(DocumentModel::getId))
        .toList();
  }

  /**
   * Shows every current Import (a header {@link ModelReference} of purpose {@link
   * ModelReference#PURPOSE_TYPE_DEFINITIONS}) in a popup so the user can remove one - mirrors SME's own
   * "Delete Import" popup ({@code removeTypeDefsView.tsx}'s {@code TDPopUpMenu}): deleting an Import always
   * drops the whole reference (and with it every type definition it contributed), there is no way to delete
   * just one imported type definition on its own (see {@link TypeDefinitionRow#editable()}, false for every
   * imported row).
   */
  @FXML
  private void onDeleteImport() {
    List<ModelReference> imports = importReferences();
    if (imports.isEmpty()) {
      return;
    }

    ContextMenu menu = new ContextMenu();
    for (ModelReference reference : imports) {
      FontIcon deleteIcon = WidgetFactory.createIcon(Icons.TRASH);
      deleteIcon.getStyleClass().add("menu-icon");
      MenuItem item = new MenuItem(reference.getReference(), deleteIcon);
      item.setOnAction(event -> removeImport(reference));
      menu.getItems().add(item);
    }
    menu.show(deleteImportButton, Side.BOTTOM, 0, 0);
  }

  private void removeImport(@NonNull ModelReference reference) {
    String message = "Delete the import of \"" + reference.getReference() + "\"? Every type definition it "
        + "contributed to this table will disappear along with it.";
    Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, message, null, null, "Delete");
    if (result.isEmpty() || result.get() != ButtonType.OK) {
      return;
    }

    model.getModelReferences().remove(reference);
    refreshIncludedTypeDefinitions();
    save();
  }

  private List<ModelReference> importReferences() {
    if (model == null || model.getModelReferences() == null) {
      return List.of();
    }
    return model.getModelReferences().stream()
        .filter(reference -> ModelReference.PURPOSE_TYPE_DEFINITIONS.equals(reference.getPurpose()))
        .toList();
  }

  private ContextMenu createContextMenu(@NonNull TableRow<TypeDefinitionRow> row) {
    FontIcon deleteIcon = WidgetFactory.createIcon(Icons.TRASH);
    deleteIcon.getStyleClass().add("menu-icon");

    MenuItem deleteItem = new MenuItem("Delete", deleteIcon);
    deleteItem.setOnAction(event -> onDelete(row));

    ContextMenu contextMenu = new ContextMenu();
    contextMenu.getItems().add(deleteItem);
    return contextMenu;
  }

  private void onDelete(@NonNull TableRow<TypeDefinitionRow> row) {
    List<TypeDefinitionRow> selectedItems = List.copyOf(typeDefinitionsTable.getSelectionModel().getSelectedItems());
    List<TypeDefinitionRow> rowsToDelete = selectedItems.size() > 1 && selectedItems.contains(row.getItem())
        ? selectedItems
        : List.of(row.getItem());
    deleteTypeDefinitions(rowsToDelete);
  }

  @FXML
  private void onDelete() {
    List<TypeDefinitionRow> selectedItems = List.copyOf(typeDefinitionsTable.getSelectionModel().getSelectedItems());
    if (selectedItems.isEmpty()) {
      return;
    }
    deleteTypeDefinitions(selectedItems);
  }

  /** Silently ignores any selected row that isn't {@link TypeDefinitionRow#editable()}: those belong to an
   * included model and can only be deleted by editing that model directly. */
  private void deleteTypeDefinitions(@NonNull List<TypeDefinitionRow> rowsToDelete) {
    List<TypeDefinition> itemsToDelete = rowsToDelete.stream()
        .filter(TypeDefinitionRow::editable)
        .map(TypeDefinitionRow::typeDefinition)
        .toList();
    if (itemsToDelete.isEmpty()) {
      return;
    }

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

  private void selectRow(@NonNull TypeDefinitionRow row) {
    int index = typeDefinitionsTable.getItems().indexOf(row);
    if (index >= 0) {
      typeDefinitionsTable.getSelectionModel().clearAndSelect(index);
      typeDefinitionsTable.scrollTo(index);
    }
  }

  private String uniqueName(@NonNull String baseName) {
    Set<String> usedNames = new HashSet<>();
    for (TypeDefinition typeDefinition : typeDefinitions) {
      usedNames.add(typeDefinition.getName());
    }
    for (TypeDefinitionRow row : includedTypeDefinitions) {
      usedNames.add(row.typeDefinition().getName());
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

    List<TypeDefinitionRow> rows = new ArrayList<>(typeDefinitions.size() + includedTypeDefinitions.size());
    typeDefinitions.stream().map(TypeDefinitionRow::own).forEach(rows::add);
    rows.addAll(includedTypeDefinitions);

    List<TypeDefinitionRow> filtered = term.isEmpty()
        ? rows
        : rows.stream()
            .filter(row -> row.typeDefinition().getName() != null && row.typeDefinition().getName().toLowerCase().contains(term))
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

  private void applyColumnWidth(@NonNull TableColumn<TypeDefinitionRow, String> column, BaseTableSettings tableSettings,
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
