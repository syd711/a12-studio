package de.a12.studio.ui.editors.auth;

import de.a12.studio.models.auth.AuthDocument;
import de.a12.studio.models.auth.User;
import de.a12.studio.models.auth.UsersDocument;
import de.a12.studio.ui.Studio;
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
import javafx.scene.control.cell.TextFieldTableCell;
import org.jspecify.annotations.NonNull;
import org.kordamp.ikonli.javafx.FontIcon;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

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
    bindTextColumn(usernameColumn, User::getUsername, User::setUsername);
    bindTextColumn(passwordColumn, User::getPassword, User::setPassword);
    bindTextColumn(emailColumn, User::getEmail, User::setEmail);
    bindTextColumn(firstnameColumn, User::getFirstname, User::setFirstname);
    bindTextColumn(lastnameColumn, User::getLastname, User::setLastname);

    authoritiesColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(joinAuthorities(data.getValue())));
    authoritiesColumn.setCellFactory(TextFieldTableCell.forTableColumn());
    authoritiesColumn.setOnEditCommit(event -> {
      event.getRowValue().setAuthorities(splitAuthorities(event.getNewValue()));
      save();
    });

    usersTable.setRowFactory(table -> new TableRow<>() {
      @Override
      protected void updateItem(User item, boolean empty) {
        super.updateItem(item, empty);
        setContextMenu(empty || item == null ? null : createRowContextMenu(this));
      }
    });
  }

  @Override
  public void loadDocument(@NonNull AuthDocument document) {
    UsersDocument usersDocument = (UsersDocument) document;
    usersTable.setItems(FXCollections.observableList(usersDocument.getUsers()));
  }

  private void bindTextColumn(@NonNull TableColumn<User, String> column, @NonNull Function<User, String> getter,
                               @NonNull BiConsumer<User, String> setter) {
    column.setCellValueFactory(data -> new ReadOnlyStringWrapper(getter.apply(data.getValue())));
    column.setCellFactory(TextFieldTableCell.forTableColumn());
    column.setOnEditCommit(event -> {
      setter.accept(event.getRowValue(), event.getNewValue());
      save();
    });
  }

  private static String joinAuthorities(@NonNull User user) {
    return String.join(", ", user.getAuthorities());
  }

  private static List<String> splitAuthorities(String value) {
    if (value == null || value.isBlank()) {
      return new ArrayList<>();
    }
    return Arrays.stream(value.split(","))
        .map(String::trim)
        .filter(entry -> !entry.isEmpty())
        .collect(Collectors.toCollection(ArrayList::new));
  }

  @FXML
  private void onAddUser() {
    User user = new User();
    user.setUsername("newuser");
    user.setAuthorities(new ArrayList<>());
    usersTable.getItems().add(user);
    usersTable.getSelectionModel().select(user);
    usersTable.scrollTo(user);
    save();
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
