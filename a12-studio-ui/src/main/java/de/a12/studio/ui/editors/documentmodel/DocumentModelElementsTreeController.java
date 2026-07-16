package de.a12.studio.ui.editors.documentmodel;

import de.a12.studio.dataservices.models.ModelType;
import de.a12.studio.dataservices.models.documentmodel.DocumentModel;
import de.a12.studio.dataservices.models.documentmodel.Element;
import de.a12.studio.dataservices.models.documentmodel.GroupElement;
import de.a12.studio.dataservices.models.documentmodel.ModelRoot;
import de.a12.studio.commons.components.SearchFieldController;
import de.a12.studio.commons.util.WidgetFactory;
import de.a12.studio.commons.util.localsettings.BaseTableSettings;
import de.a12.studio.commons.util.localsettings.LocalUISettings;
import de.a12.studio.dataservices.projects.ProjectItem;
import de.a12.studio.ui.editors.documentmodel.commands.DeleteNodeCommand;
import de.a12.studio.ui.editors.util.commandstack.Command;
import de.a12.studio.ui.editors.util.commandstack.CommandStack;
import de.a12.studio.ui.events.ElementValidatedEvent;
import de.a12.studio.ui.events.StudioEventListener;
import de.a12.studio.ui.events.StudioEventManager;
import de.a12.studio.ui.util.Icons;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.jspecify.annotations.NonNull;
import org.kordamp.ikonli.javafx.FontIcon;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Consumer;

public class DocumentModelElementsTreeController implements Initializable, StudioEventListener {

  private static final String TABLE_SETTINGS_ID = ModelType.DOCUMENT.getValue();

  private static final String NAME_COLUMN_ID = "name";

  private static final String TYPE_COLUMN_ID = "type";

  @FXML
  private ToolBar modelTreeToolbarBar;

  @FXML
  private Button undoButton;

  @FXML
  private Button redoButton;

  @FXML
  private MenuButton modelTreeAddButton;

  @FXML
  private SearchFieldController searchController;

  @FXML
  private TreeTableView<ElementViewModel> elementsTreeTable;

  @FXML
  private TreeTableColumn<ElementViewModel, String> nameColumn;

  @FXML
  private TreeTableColumn<ElementViewModel, String> typeColumn;

  private ProjectItem projectItem;
  private ModelRoot modelRoot;

  private final CommandStack commandStack = new CommandStack();

  private Consumer<List<Element>> selectionListener;

  public void load(@NonNull DocumentModel model) {
    load(projectItem, model.getContent().getModelRoot());
  }

  public void setSelectionListener(Consumer<List<Element>> selectionListener) {
    this.selectionListener = selectionListener;
  }

  public void load(ProjectItem projectItem, @NonNull ModelRoot modelRoot) {
    this.projectItem = projectItem;
    this.modelRoot = modelRoot;
    applyFilter(searchController.getText());
    StudioEventManager.getInstance().addListener(this);
  }

  @Override
  public void elementValidated(@NonNull ElementValidatedEvent event) {
    TreeItem<ElementViewModel> treeItem = findTreeItem(elementsTreeTable.getRoot(), event.getElementId());
    if (treeItem == null) {
      return;
    }
    treeItem.getValue().setHasError(event.getError().isPresent());
    elementsTreeTable.refresh();
  }

  public List<Element> getAncestors(@NonNull Element element) {
    List<Element> ancestors = new ArrayList<>();
    TreeItem<ElementViewModel> treeItem = findTreeItem(elementsTreeTable.getRoot(), element.getId());
    if (treeItem == null) {
      return ancestors;
    }

    TreeItem<ElementViewModel> parent = treeItem.getParent();
    while (parent != null && parent.getValue() != null) {
      ancestors.add(0, parent.getValue().getElement());
      parent = parent.getParent();
    }
    return ancestors;
  }

  private TreeItem<ElementViewModel> findTreeItem(TreeItem<ElementViewModel> treeItem, @NonNull String elementId) {
    if (treeItem == null) {
      return null;
    }
    if (treeItem.getValue() != null && elementId.equals(treeItem.getValue().getElement().getId())) {
      return treeItem;
    }
    for (TreeItem<ElementViewModel> child : treeItem.getChildren()) {
      TreeItem<ElementViewModel> found = findTreeItem(child, elementId);
      if (found != null) {
        return found;
      }
    }
    return null;
  }

  @FXML
  private void onUndo() {
    commandStack.undo();
    updateUndoRedoState();
    applyFilter(searchController.getText());
    StudioEventManager.getInstance().fireModelSaveEvent(projectItem);
  }

  @FXML
  private void onRedo() {
    commandStack.redo();
    updateUndoRedoState();
    applyFilter(searchController.getText());
    StudioEventManager.getInstance().fireModelSaveEvent(projectItem);
  }

  private void updateUndoRedoState() {
    undoButton.setDisable(!commandStack.canUndo());
    redoButton.setDisable(!commandStack.canRedo());
  }

  private void notifySelectionChanged() {
    if (selectionListener == null) {
      return;
    }

    List<Element> selectedElements = new ArrayList<>();
    for (TreeItem<ElementViewModel> treeItem : elementsTreeTable.getSelectionModel().getSelectedItems()) {
      if (treeItem != null && treeItem.getValue() != null) {
        selectedElements.add(treeItem.getValue().getElement());
      }
    }
    selectionListener.accept(selectedElements);
  }

  private void applyFilter(String filter) {
    if (modelRoot == null) {
      return;
    }

    String term = filter == null ? "" : filter.trim().toLowerCase();
    TreeItem<ElementViewModel> root = new TreeItem<>();
    for (GroupElement group : modelRoot.getRootGroups()) {
      TreeItem<ElementViewModel> treeItem = term.isEmpty() ? toTreeItem(group) : toFilteredTreeItem(group, term);
      if (treeItem != null) {
        root.getChildren().add(treeItem);
      }
    }
    elementsTreeTable.setRoot(root);
    expandAll(root);
  }

  private void expandAll(@NonNull TreeItem<ElementViewModel> treeItem) {
    treeItem.setExpanded(true);
    for (TreeItem<ElementViewModel> child : treeItem.getChildren()) {
      expandAll(child);
    }
  }

  private TreeItem<ElementViewModel> toTreeItem(@NonNull Element element) {
    ElementViewModel viewModel = new ElementViewModel(element);
    TreeItem<ElementViewModel> treeItem = new TreeItem<>(viewModel);
    for (ElementViewModel child : viewModel.getChildren()) {
      treeItem.getChildren().add(toTreeItem(child.getElement()));
    }
    return treeItem;
  }

  private TreeItem<ElementViewModel> toFilteredTreeItem(@NonNull Element element, @NonNull String term) {
    ElementViewModel viewModel = new ElementViewModel(element);
    List<TreeItem<ElementViewModel>> matchingChildren = new ArrayList<>();
    for (ElementViewModel child : viewModel.getChildren()) {
      TreeItem<ElementViewModel> filteredChild = toFilteredTreeItem(child.getElement(), term);
      if (filteredChild != null) {
        matchingChildren.add(filteredChild);
      }
    }

    boolean selfMatches = viewModel.getName() != null && viewModel.getName().toLowerCase().contains(term);
    if (!selfMatches && matchingChildren.isEmpty()) {
      return null;
    }

    TreeItem<ElementViewModel> treeItem = new TreeItem<>(viewModel);
    treeItem.getChildren().addAll(matchingChildren);
    return treeItem;
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    modelTreeAddButton.setDisable(true);
    updateUndoRedoState();
    searchController.setOnSearch(this::applyFilter);

    elementsTreeTable.setShowRoot(false);
    elementsTreeTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    elementsTreeTable.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<TreeItem<ElementViewModel>>() {
      @Override
      public void changed(ObservableValue<? extends TreeItem<ElementViewModel>> observable, TreeItem<ElementViewModel> oldValue, TreeItem<ElementViewModel> newValue) {
        modelTreeAddButton.setDisable(newValue == null);
      }
    });
    elementsTreeTable.getSelectionModel().getSelectedItems().addListener((ListChangeListener<TreeItem<ElementViewModel>>) change -> notifySelectionChanged());
    elementsTreeTable.setRowFactory(treeTable -> new TreeTableRow<>() {
      @Override
      protected void updateItem(ElementViewModel item, boolean empty) {
        super.updateItem(item, empty);
        setContextMenu(empty || item == null ? null : createContextMenu(item));
      }
    });
    nameColumn.setCellValueFactory(param -> new ReadOnlyStringWrapper(param.getValue().getValue().getName()));
    nameColumn.setCellFactory(column -> new TreeTableCell<>() {
      @Override
      protected void updateItem(String name, boolean empty) {
        super.updateItem(name, empty);
        if (empty || name == null) {
          setText(null);
          setGraphic(null);
          getStyleClass().remove("validation-error");
          return;
        }

        setText(name);
        ElementViewModel viewModel = getTableRow().getItem();
        if (viewModel == null) {
          setGraphic(null);
          getStyleClass().remove("validation-error");
        }
        else {
          Node icon = viewModel.isGroup()
              ? createGroupIcon()
              : WidgetFactory.createIcon(viewModel.getIcon());
          icon.getStyleClass().add("tree-icon");
          setGraphic(icon);

          if (viewModel.hasError()) {
            if (!getStyleClass().contains("validation-error")) {
              getStyleClass().add("validation-error");
            }
          }
          else {
            getStyleClass().remove("validation-error");
          }
        }
      }
    });


    typeColumn.setCellValueFactory(param -> {
      String type = param.getValue().getValue().getType();
      type = type.replaceAll("Type", "");
      if (type.equalsIgnoreCase("Rule")) {
        type = "Validation Rule";
      }

      return new ReadOnlyStringWrapper(type);
    });

    BaseTableSettings tableSettings = LocalUISettings.getTablePreference(TABLE_SETTINGS_ID);
    applyColumnWidth(nameColumn, tableSettings, NAME_COLUMN_ID);
    applyColumnWidth(typeColumn, tableSettings, TYPE_COLUMN_ID);

    nameColumn.widthProperty().addListener((observable, oldValue, newValue) ->
        saveColumnWidth(NAME_COLUMN_ID, newValue.doubleValue()));
    typeColumn.widthProperty().addListener((observable, oldValue, newValue) ->
        saveColumnWidth(TYPE_COLUMN_ID, newValue.doubleValue()));

    modelTreeAddButton.getItems().addAll(createElementToolbarMenuItems());
  }

  private ContextMenu createContextMenu(@NonNull ElementViewModel viewModel) {
    ContextMenu contextMenu = new ContextMenu();
    contextMenu.getItems().addAll(createElementMenuItems());
    return contextMenu;
  }

  private List<MenuItem> createElementMenuItems() {
    List<MenuItem> items = new ArrayList<>();
    items.add(createMenuItem("_Group", createGroupIcon()));
    items.add(createMenuItem("_Field", Icons.ELEMENT_FIELD));
    items.add(createMenuItem("_Validation Rule", Icons.ELEMENT_RULE));
    items.add(createMenuItem("Co_mputation Rule", Icons.ELEMENT_COMPUTATION));
    items.add(createMenuItem("_Attachment", Icons.ELEMENT_ATTACHMENT));
    items.add(createMenuItem("Multi-_Select", Icons.ELEMENT_MULTI_SELECT));
    items.add(createMenuItem("_Include", Icons.ELEMENT_INCLUDE));
    items.add(new SeparatorMenuItem());
    items.add(createMenuItem("_Cut", Icons.CUT));
    items.add(createMenuItem("Cop_y", Icons.COPY));
    items.add(createMenuItem("_Paste", Icons.PASTE));
    items.add(new SeparatorMenuItem());
    MenuItem deleteItem = createMenuItem("_Delete", Icons.TRASH);
    deleteItem.setOnAction(event -> onDeleteModelItem());
    items.add(deleteItem);
    return items;
  }

  private void onDeleteModelItem() {
    List<TreeItem<ElementViewModel>> selection =
        new ArrayList<>(elementsTreeTable.getSelectionModel().getSelectedItems());
    for (TreeItem<ElementViewModel> treeItem : topLevelSelection(selection)) {
      Command command = createDeleteCommand(treeItem);
      if (command != null) {
        commandStack.execute(command);
      }
    }

    updateUndoRedoState();
    applyFilter(searchController.getText());
    StudioEventManager.getInstance().fireModelSaveEvent(projectItem);
  }

  private List<TreeItem<ElementViewModel>> topLevelSelection(@NonNull List<TreeItem<ElementViewModel>> selection) {
    List<TreeItem<ElementViewModel>> result = new ArrayList<>();
    for (TreeItem<ElementViewModel> treeItem : selection) {
      if (treeItem != null && !hasSelectedAncestor(treeItem, selection)) {
        result.add(treeItem);
      }
    }
    return result;
  }

  private boolean hasSelectedAncestor(@NonNull TreeItem<ElementViewModel> treeItem,
                                       @NonNull List<TreeItem<ElementViewModel>> selection) {
    TreeItem<ElementViewModel> ancestor = treeItem.getParent();
    while (ancestor != null) {
      if (selection.contains(ancestor)) {
        return true;
      }
      ancestor = ancestor.getParent();
    }
    return false;
  }

  private Command createDeleteCommand(@NonNull TreeItem<ElementViewModel> treeItem) {
    Element element = treeItem.getValue().getElement();
    TreeItem<ElementViewModel> parentItem = treeItem.getParent();
    if (parentItem == null || parentItem.getValue() == null) {
      return new DeleteNodeCommand<>(modelRoot.getRootGroups(), (GroupElement) element);
    }

    Element parentElement = parentItem.getValue().getElement();
    if (parentElement instanceof GroupElement groupElement && groupElement.getGroup() != null) {
      return new DeleteNodeCommand<>(groupElement.getGroup().getElements(), element);
    }
    return null;
  }

  private List<MenuItem> createElementToolbarMenuItems() {
    List<MenuItem> items = new ArrayList<>();
    items.add(createMenuItem("_Group", createGroupIcon()));
    items.add(createMenuItem("_Field", Icons.ELEMENT_FIELD));
    items.add(createMenuItem("_Validation Rule", Icons.ELEMENT_RULE));
    items.add(createMenuItem("Co_mputation Rule", Icons.ELEMENT_COMPUTATION));
    items.add(createMenuItem("_Attachment", Icons.ELEMENT_ATTACHMENT));
    items.add(createMenuItem("Multi-_Select", Icons.ELEMENT_MULTI_SELECT));
    items.add(createMenuItem("_Include", Icons.ELEMENT_INCLUDE));
    return items;
  }

  private MenuItem createMenuItem(@NonNull String text, @NonNull String icon) {
    MenuItem menuItem = new MenuItem(text);
    FontIcon fontIcon = WidgetFactory.createIcon(icon);
    fontIcon.getStyleClass().add("menu-icon");
    menuItem.setGraphic(fontIcon);
    return menuItem;
  }

  private MenuItem createMenuItem(@NonNull String text, @NonNull Node icon) {
    MenuItem menuItem = new MenuItem(text);
    icon.getStyleClass().add("menu-icon");
    menuItem.setGraphic(icon);
    return menuItem;
  }

  private Node createGroupIcon() {
    Image image = new Image(getClass().getResourceAsStream(Icons.PNG_ELEMENT_GROUP),
        WidgetFactory.DEFAULT_ICON_SIZE, WidgetFactory.DEFAULT_ICON_SIZE, true, true);
    return new ImageView(image);
  }

  private void applyColumnWidth(@NonNull TreeTableColumn<ElementViewModel, String> column,
                                BaseTableSettings tableSettings, @NonNull String columnId) {
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
