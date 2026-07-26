package de.a12.studio.ui.editors.auth;

import de.a12.studio.models.auth.AuthDocument;
import de.a12.studio.models.auth.User;
import de.a12.studio.models.auth.UsersDocument;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.editors.auth.dialogs.Dialogs;
import de.a12.studio.ui.util.Icons;
import de.a12.studio.ui.util.WidgetFactory;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import org.jspecify.annotations.NonNull;
import org.kordamp.ikonli.javafx.FontIcon;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.function.Function;

public class UsersEditorController extends AbstractAuthFileEditorController implements Initializable {

  @FXML
  private TableView<User> usersTable;

  @FXML
  private TableColumn<User, String> usernameColumn;

  @FXML
  private TableColumn<User, String> passwordColumn;

  @FXML
  private TableColumn<User, String> authoritiesColumn;

  @FXML
  private TableColumn<User, String> emailColumn;

  @FXML
  private TableColumn<User, String> firstnameColumn;

  @FXML
  private TableColumn<User, String> lastnameColumn;

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    bindTextColumn(usernameColumn, User::getUsername);
    bindTextColumn(passwordColumn, User::getPassword);
    bindTextColumn(emailColumn, User::getEmail);
    bindTextColumn(firstnameColumn, User::getFirstname);
    bindTextColumn(lastnameColumn, User::getLastname);

    authoritiesColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(joinAuthorities(data.getValue())));

    usersTable.setRowFactory(table -> {
      TableRow<User> row = new TableRow<>() {
        @Override
        protected void updateItem(User item, boolean empty) {
          super.updateItem(item, empty);
          setContextMenu(empty || item == null ? null : createRowContextMenu(this));
        }
      };
      row.setOnMouseClicked(event -> {
        if (event.getClickCount() == 2 && !row.isEmpty()) {
          openEditUserDialog(row.getItem());
        }
      });
      return row;
    });
  }

  @Override
  public void loadDocument(@NonNull AuthDocument document) {
    UsersDocument usersDocument = (UsersDocument) document;
    usersTable.setItems(FXCollections.observableList(usersDocument.getUsers()));
  }

  private void bindTextColumn(@NonNull TableColumn<User, String> column, @NonNull Function<User, String> getter) {
    column.setCellValueFactory(data -> new ReadOnlyStringWrapper(getter.apply(data.getValue())));
  }

  private static String joinAuthorities(@NonNull User user) {
    return String.join(", ", user.getAuthorities());
  }

  @FXML
  private void onAddUser() {
    Dialogs.showUserForAdd(Studio.stage).ifPresent(user -> {
      usersTable.getItems().add(user);
      usersTable.getSelectionModel().select(user);
      usersTable.scrollTo(user);
      save();
    });
  }

  @FXML
  private void onEditUser() {
    User selected = usersTable.getSelectionModel().getSelectedItem();
    if (selected == null) {
      return;
    }
    openEditUserDialog(selected);
  }

  private void openEditUserDialog(@NonNull User user) {
    if (Dialogs.showUserForEdit(Studio.stage, user)) {
      usersTable.refresh();
      save();
    }
  }

  @FXML
  private void onDeleteUser() {
    User selected = usersTable.getSelectionModel().getSelectedItem();
    if (selected != null) {
      onDeleteUser(selected);
    }
  }

  private ContextMenu createRowContextMenu(@NonNull TableRow<User> row) {
    FontIcon deleteIcon = WidgetFactory.createIcon(Icons.TRASH);
    deleteIcon.getStyleClass().add("menu-icon");

    MenuItem deleteItem = new MenuItem("Delete", deleteIcon);
    deleteItem.setOnAction(event -> onDeleteUser(row.getItem()));

    ContextMenu contextMenu = new ContextMenu();
    contextMenu.getItems().add(deleteItem);
    return contextMenu;
  }

  private void onDeleteUser(@NonNull User user) {
    Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, "Delete user '" + user.getUsername() + "'?", null, null, "Delete");
    if (result.isPresent() && result.get() == ButtonType.OK) {
      usersTable.getItems().remove(user);
      save();
    }
  }
}
