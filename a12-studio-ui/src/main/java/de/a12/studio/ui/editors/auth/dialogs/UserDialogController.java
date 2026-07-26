package de.a12.studio.ui.editors.auth.dialogs;

import de.a12.studio.models.auth.User;
import de.a12.studio.ui.components.DialogController;
import javafx.fxml.FXML;
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

  void init(Stage stage, User user) {
    this.stage = stage;
    usernameField.setText(user.getUsername());
    passwordField.setText(user.getPassword());
    authoritiesField.setText(joinAuthorities(user));
    emailField.setText(user.getEmail());
    firstnameField.setText(user.getFirstname());
    lastnameField.setText(user.getLastname());
  }

  boolean applyResultTo(User user) {
    if (result.isPresent() && result.get() == ButtonType.OK) {
      user.setUsername(usernameField.getText().trim());
      user.setPassword(emptyToNull(passwordField.getText()));
      user.setAuthorities(splitAuthorities(authoritiesField.getText()));
      user.setEmail(emptyToNull(emailField.getText()));
      user.setFirstname(emptyToNull(firstnameField.getText()));
      user.setLastname(emptyToNull(lastnameField.getText()));
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
