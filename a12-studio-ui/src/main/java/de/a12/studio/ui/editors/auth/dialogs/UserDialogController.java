package de.a12.studio.ui.editors.auth.dialogs;

import de.a12.studio.models.auth.User;
import de.a12.studio.ui.components.DialogController;
import de.a12.studio.ui.util.WidgetFactory;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Add/edit dialog for a single {@link User} entry of {@link de.a12.studio.ui.editors.auth.UsersEditorController}.
 */
public class UserDialogController implements DialogController {

  @FXML
  private TextField usernameField;

  @FXML
  private TextField passwordField;

  @FXML
  private TextField authoritiesField;

  @FXML
  private TextField emailField;

  @FXML
  private TextField firstnameField;

  @FXML
  private TextField lastnameField;

  @FXML
  private Button okButton;

  @FXML
  private Button cancelButton;

  private Stage stage;

  private Optional<ButtonType> result = Optional.of(ButtonType.CANCEL);

  @FXML
  private void initialize() {
    okButton.disableProperty().bind(usernameField.textProperty().map(name -> name == null || name.isBlank()));
    usernameField.requestFocus();
  }

  @Override
  public void onDialogCancel() {
    stage.close();
  }

  @FXML
  private void onDialogSubmit() {
    result = Optional.of(ButtonType.OK);
    stage.close();
  }

  public static Optional<User> showForAdd(Stage owner) {
    User user = new User();
    return show(owner, "Add User", user) ? Optional.of(user) : Optional.empty();
  }

  public static boolean showForEdit(Stage owner, User user) {
    return show(owner, "Edit User", user);
  }

  private static boolean show(Stage owner, String title, User user) {
    FXMLLoader fxmlLoader = new FXMLLoader(UserDialogController.class.getResource("user-dialog.fxml"));
    Stage stage = WidgetFactory.createDialogStage("dialog-user", fxmlLoader, owner, title);
    UserDialogController controller = (UserDialogController) stage.getUserData();
    controller.stage = stage;

    controller.usernameField.setText(user.getUsername());
    controller.passwordField.setText(user.getPassword());
    controller.authoritiesField.setText(joinAuthorities(user));
    controller.emailField.setText(user.getEmail());
    controller.firstnameField.setText(user.getFirstname());
    controller.lastnameField.setText(user.getLastname());

    stage.showAndWait();

    if (controller.result.isPresent() && controller.result.get() == ButtonType.OK) {
      user.setUsername(controller.usernameField.getText().trim());
      user.setPassword(emptyToNull(controller.passwordField.getText()));
      user.setAuthorities(splitAuthorities(controller.authoritiesField.getText()));
      user.setEmail(emptyToNull(controller.emailField.getText()));
      user.setFirstname(emptyToNull(controller.firstnameField.getText()));
      user.setLastname(emptyToNull(controller.lastnameField.getText()));
      return true;
    }
    return false;
  }

  private static String emptyToNull(String value) {
    return value == null || value.isBlank() ? null : value.trim();
  }

  private static String joinAuthorities(User user) {
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
}
