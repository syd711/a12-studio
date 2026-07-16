package de.a12.studio.ui.components;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.jspecify.annotations.NonNull;

public class ErrorContainerController {

  @FXML
  private VBox root;

  @FXML
  private Label errorTitle;

  @FXML
  private Label errorMessage;

  public void show(@NonNull String severity, @NonNull String message) {
    root.setManaged(true);
    root.setVisible(true);
    errorTitle.setText(capitalize(severity));
    errorMessage.setText(message);
  }

  public void hide() {
    root.setManaged(false);
    root.setVisible(false);
  }

  private static String capitalize(@NonNull String value) {
    return value.isEmpty() ? value : Character.toUpperCase(value.charAt(0)) + value.substring(1).toLowerCase();
  }
}
