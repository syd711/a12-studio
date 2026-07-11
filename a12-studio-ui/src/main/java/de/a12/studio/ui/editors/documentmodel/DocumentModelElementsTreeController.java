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
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableRow;
import javafx.scene.control.TreeTableView;
import org.jspecify.annotations.NonNull;
import org.kordamp.ikonli.javafx.FontIcon;

import java.net.URL;
import java.util.ResourceBundle;

public class DocumentModelElementsTreeController implements Initializable {

  private static final String TABLE_SETTINGS_ID = ModelType.DOCUMENT.getValue();

  private static final String NAME_COLUMN_ID = "name";

  private static final String TYPE_COLUMN_ID = "type";

  @FXML
  private TreeTableView<ElementViewModel> elementsTreeTable;

  @FXML
  private TreeTableColumn<ElementViewModel, String> nameColumn;

  @FXML
  private TreeTableColumn<ElementViewModel, String> typeColumn;

  public void load(@NonNull DocumentModel model) {
    load(model.getContent().getModelRoot());
  }

  public void load(@NonNull ModelRoot modelRoot) {
    TreeItem<ElementViewModel> root = new TreeItem<>();
    root.setExpanded(true);
    for (GroupElement group : modelRoot.getRootGroups()) {
      root.getChildren().add(toTreeItem(group));
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

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    elementsTreeTable.setShowRoot(false);
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
        } else {
          FontIcon icon = WidgetFactory.createIcon(viewModel.getIcon());
          icon.getStyleClass().add("tree-icon");
          setGraphic(icon);
        }
      }
    });
    typeColumn.setCellValueFactory(param -> new ReadOnlyStringWrapper(param.getValue().getValue().getType()));

    BaseTableSettings tableSettings = LocalUISettings.getTablePreference(TABLE_SETTINGS_ID);
    applyColumnWidth(nameColumn, tableSettings, NAME_COLUMN_ID);
    applyColumnWidth(typeColumn, tableSettings, TYPE_COLUMN_ID);

    nameColumn.widthProperty().addListener((observable, oldValue, newValue) ->
        saveColumnWidth(NAME_COLUMN_ID, newValue.doubleValue()));
    typeColumn.widthProperty().addListener((observable, oldValue, newValue) ->
        saveColumnWidth(TYPE_COLUMN_ID, newValue.doubleValue()));
  }

  private ContextMenu createContextMenu(@NonNull ElementViewModel viewModel) {
    MenuItem cut = new MenuItem("_Cut");
    cut.setGraphic(WidgetFactory.createIcon(Icons.CUT));
    MenuItem copy = new MenuItem("Cop_y");
    copy.setGraphic(WidgetFactory.createIcon(Icons.COPY));
    MenuItem paste = new MenuItem("_Paste");
    MenuItem delete = new MenuItem("_Delete");
    delete.setGraphic(WidgetFactory.createIcon(Icons.TRASH));
    return new ContextMenu(cut, copy, paste, new SeparatorMenuItem(), delete);
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
