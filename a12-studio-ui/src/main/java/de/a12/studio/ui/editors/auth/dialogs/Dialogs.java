package de.a12.studio.ui.editors.auth.dialogs;

import de.a12.studio.ui.util.StudioBundle;

import de.a12.studio.models.auth.Role;
import de.a12.studio.models.auth.User;
import de.a12.studio.ui.util.WidgetFactory;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;

import java.util.Optional;

public class Dialogs {

  public static Optional<Role> showRoleForAdd(Stage owner) {
    Role role = new Role();
    return showRole(owner, StudioBundle.get("add_role"), role) ? Optional.of(role) : Optional.empty();
  }

  public static boolean showRoleForEdit(Stage owner, Role role) {
    return showRole(owner, StudioBundle.get("edit_role"), role);
  }

  private static boolean showRole(Stage owner, String title, Role role) {
    FXMLLoader fxmlLoader = new FXMLLoader(RoleDialogController.class.getResource("role-dialog.fxml"));
fxmlLoader.setResources(StudioBundle.getBundle());
    Stage stage = WidgetFactory.createDialogStage("dialog-role", fxmlLoader, owner, title);
    RoleDialogController controller = (RoleDialogController) stage.getUserData();
    controller.init(stage, role);
    stage.showAndWait();
    return controller.applyResultTo(role);
  }

  public static Optional<User> showUserForAdd(Stage owner) {
    User user = new User();
    return showUser(owner, StudioBundle.get("add_user"), user) ? Optional.of(user) : Optional.empty();
  }

  public static boolean showUserForEdit(Stage owner, User user) {
    return showUser(owner, StudioBundle.get("edit_user"), user);
  }

  private static boolean showUser(Stage owner, String title, User user) {
    FXMLLoader fxmlLoader = new FXMLLoader(UserDialogController.class.getResource("user-dialog.fxml"));
fxmlLoader.setResources(StudioBundle.getBundle());
    Stage stage = WidgetFactory.createDialogStage("dialog-user", fxmlLoader, owner, title);
    UserDialogController controller = (UserDialogController) stage.getUserData();
    controller.init(stage, user);
    stage.showAndWait();
    return controller.applyResultTo(user);
  }
}
