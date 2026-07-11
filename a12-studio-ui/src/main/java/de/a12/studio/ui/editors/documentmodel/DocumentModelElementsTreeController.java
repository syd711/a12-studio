package de.a12.studio.ui.editors.documentmodel;

import de.a12.studio.dataservices.models.ModelType;
import de.a12.studio.dataservices.models.documentmodel.DocumentModel;
import de.a12.studio.dataservices.models.documentmodel.Element;
import de.a12.studio.dataservices.models.documentmodel.GroupElement;
import de.a12.studio.dataservices.models.documentmodel.ModelRoot;
import de.a12.studio.commons.util.WidgetFactory;
import de.a12.studio.commons.util.localsettings.BaseTableSettings;
import de.a12.studio.commons.util.localsettings.LocalUISettings;
import de.a12.studio.ui.util.Icons;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import org.jspecify.annotations.NonNull;
import org.kordamp.ikonli.javafx.FontIcon;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class DocumentModelElementsTreeController implements Initializable {

  private static final String TABLE_SETTINGS_ID = ModelType.DOCUMENT.getValue();

  private static final String NAME_COLUMN_ID = "name";

  private static final String TYPE_COLUMN_ID = "type";

  @FXML
  private ToolBar modelTreeToolbarBar;

  @FXML
  private MenuButton modelTreeAddButton;

  @FXML
  private TextField searchField;

  @FXML
  private TreeTableView<ElementViewModel> elementsTreeTable;

  @FXML
  private TreeTableColumn<ElementViewModel, String> nameColumn;

  @FXML
  private TreeTableColumn<ElementViewModel, String> typeColumn;

  private ModelRoot modelRoot;

  public void load(@NonNull DocumentModel model) {
    load(model.getContent().getModelRoot());
  }

  public void load(@NonNull ModelRoot modelRoot) {
    this.modelRoot = modelRoot;
    applyFilter(searchField.getText());
  }

  @FXML
  private void onUndo() {
  }

  @FXML
  private void onRedo() {
  }

  @FXML
  private void onResetSearch() {
    searchField.clear();
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
    searchField.textProperty().addListener((observable, oldValue, newValue) -> applyFilter(newValue));

    elementsTreeTable.setShowRoot(false);
    elementsTreeTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    elementsTreeTable.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<TreeItem<ElementViewModel>>() {
      @Override
      public void changed(ObservableValue<? extends TreeItem<ElementViewModel>> observable, TreeItem<ElementViewModel> oldValue, TreeItem<ElementViewModel> newValue) {
        modelTreeAddButton.setDisable(newValue == null);
      }
    });
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
          return;
        }

        setText(name);
        ElementViewModel viewModel = getTableRow().getItem();
        if (viewModel == null) {
          setGraphic(null);
        }
        else {
          FontIcon icon = WidgetFactory.createIcon(viewModel.getIcon());
          icon.getStyleClass().add("tree-icon");
          setGraphic(icon);
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
    items.add(createMenuItem("_Group", Icons.ELEMENT_GROUP));
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
    items.add(createMenuItem("_Delete", Icons.TRASH));
    return items;
  }

  private List<MenuItem> createElementToolbarMenuItems() {
    List<MenuItem> items = new ArrayList<>();
    items.add(createMenuItem("_Group", Icons.ELEMENT_GROUP));
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
