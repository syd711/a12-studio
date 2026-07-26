package de.a12.studio.ui.editors.auth;

import de.a12.studio.models.auth.AuthDocument;
import de.a12.studio.models.auth.Role;
import de.a12.studio.models.auth.RolesDocument;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.editors.auth.dialogs.RoleDialogController;
import de.a12.studio.ui.util.Icons;
import de.a12.studio.ui.util.WidgetFactory;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.TextFieldListCell;
import org.jspecify.annotations.NonNull;
import org.kordamp.ikonli.javafx.FontIcon;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class RolesEditorController extends AbstractAuthFileEditorController implements Initializable {

  @FXML
  private TableView<Role> rolesTable;

  @FXML
  private TableColumn<Role, String> nameColumn;

  @FXML
  private TableColumn<Role, String> descriptionColumn;

  @FXML
  private Label accessRightsLabel;

  @FXML
  private ListView<String> accessRightsList;

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    nameColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getName()));
    descriptionColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().getDescription()));

    rolesTable.setRowFactory(table -> {
      TableRow<Role> row = new TableRow<>() {
        @Override
        protected void updateItem(Role item, boolean empty) {
          super.updateItem(item, empty);
          setContextMenu(empty || item == null ? null : createRowContextMenu(this));
        }
      };
      row.setOnMouseClicked(event -> {
        if (event.getClickCount() == 2 && !row.isEmpty()) {
          openEditRoleDialog(row.getItem());
        }
      });
      return row;
    });

    rolesTable.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> showAccessRights(newValue));

    accessRightsList.setCellFactory(TextFieldListCell.forListView());
    accessRightsList.setEditable(true);
    accessRightsList.setOnEditCommit(event -> {
      accessRightsList.getItems().set(event.getIndex(), event.getNewValue());
      save();
    });

    showAccessRights(null);
  }

  @Override
  public void loadDocument(@NonNull AuthDocument document) {
    RolesDocument rolesDocument = (RolesDocument) document;
    rolesTable.setItems(FXCollections.observableList(rolesDocument.getRoles()));
    showAccessRights(null);
  }

  private void showAccessRights(Role role) {
    boolean hasSelection = role != null;
    accessRightsLabel.setVisible(hasSelection);
    accessRightsLabel.setManaged(hasSelection);
    accessRightsList.setVisible(hasSelection);
    accessRightsList.setManaged(hasSelection);
    accessRightsList.setItems(hasSelection ? FXCollections.observableList(role.getAccessRights()) : FXCollections.observableArrayList());
  }

  @FXML
  private void onAddRole() {
    RoleDialogController.showForAdd(Studio.stage).ifPresent(role -> {
      // Mutate through the table's own (list-backed) ObservableList, not document.getRoles()
      // directly, so the TableView actually receives a change notification and redraws.
      rolesTable.getItems().add(role);
      rolesTable.getSelectionModel().select(role);
      rolesTable.scrollTo(role);
      save();
    });
  }

  @FXML
  private void onEditRole() {
    Role selected = rolesTable.getSelectionModel().getSelectedItem();
    if (selected == null) {
      return;
    }
    openEditRoleDialog(selected);
  }

  private void openEditRoleDialog(@NonNull Role role) {
    if (RoleDialogController.showForEdit(Studio.stage, role)) {
      rolesTable.refresh();
      save();
    }
  }

  @FXML
  private void onDeleteRole() {
    Role selected = rolesTable.getSelectionModel().getSelectedItem();
    if (selected != null) {
      onDeleteRole(selected);
    }
  }

  @FXML
  private void onAddAccessRight() {
    Role selected = rolesTable.getSelectionModel().getSelectedItem();
    if (selected == null) {
      return;
    }
    String name = WidgetFactory.showInputDialog(Studio.stage, "New Access Right", "Name", null, null, null);
    if (name == null || name.isBlank()) {
      return;
    }
    accessRightsList.getItems().add(name.trim());
    save();
  }

  @FXML
  private void onRemoveAccessRight() {
    String accessRight = accessRightsList.getSelectionModel().getSelectedItem();
    if (accessRight == null) {
      return;
    }
    accessRightsList.getItems().remove(accessRight);
    save();
  }

  private ContextMenu createRowContextMenu(@NonNull TableRow<Role> row) {
    FontIcon deleteIcon = WidgetFactory.createIcon(Icons.TRASH);
    deleteIcon.getStyleClass().add("menu-icon");

    MenuItem deleteItem = new MenuItem("Delete", deleteIcon);
    deleteItem.setOnAction(event -> onDeleteRole(row.getItem()));

    ContextMenu contextMenu = new ContextMenu();
    contextMenu.getItems().add(deleteItem);
    return contextMenu;
  }

  private void onDeleteRole(@NonNull Role role) {
    Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, "Delete role '" + role.getName() + "'?", null, null, "Delete");
    if (result.isPresent() && result.get() == ButtonType.OK) {
      rolesTable.getItems().remove(role);
      save();
    }
  }
}
