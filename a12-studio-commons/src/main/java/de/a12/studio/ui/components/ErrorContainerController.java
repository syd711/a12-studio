package de.a12.studio.ui.components;

import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.jspecify.annotations.NonNull;

public class ErrorContainerController {

  private static final PseudoClass WARNING_PSEUDO_CLASS = PseudoClass.getPseudoClass("warning");

  @FXML
  private VBox root;

  @FXML
  private Label errorMessage;

  private final StringProperty severity = new SimpleStringProperty();

  /**
   * Reflects whether this container is currently showing an error, i.e. whether {@link #show} (rather than
   * {@link #hide}) was called last. Lets an outer container (e.g. a dialog whose panels each have their own
   * error container) observe and aggregate the error state of the panels it embeds.
   */
  public ReadOnlyBooleanProperty errorProperty() {
    return root.visibleProperty();
  }

  /**
   * The severity ("ERROR" or "WARNING") passed to the last {@link #show} call. Lets an outer container
   * distinguish warnings from errors when aggregating, rather than treating every visible panel as an error.
   */
  public ReadOnlyStringProperty severityProperty() {
    return severity;
  }

  public void show(@NonNull String severity, @NonNull String message) {
    root.setManaged(true);
    root.setVisible(true);
    root.pseudoClassStateChanged(WARNING_PSEUDO_CLASS, "WARNING".equalsIgnoreCase(severity));
    this.severity.set(severity);
    errorMessage.setText(message);
  }

  public void hide() {
    root.setManaged(false);
    root.setVisible(false);
  }

  /**
   * Adds a style class to the actual error box (the nested {@code fx:id="root"} VBox), not the FXML
   * document root. A {@code styleClass} attribute set on an {@code <fx:include>} of this file is applied to
   * the unnamed wrapper VBox instead, so it can never override rules scoped to the {@code error-container}
   * class (e.g. {@code error-container-no-radius}'s border/background radius reset) - callers needing that
   * must go through this method instead.
   */
  public void addStyleClass(@NonNull String styleClass) {
    root.getStyleClass().add(styleClass);
  }

  private static String capitalize(@NonNull String value) {
    return value.isEmpty() ? value : Character.toUpperCase(value.charAt(0)) + value.substring(1).toLowerCase();
  }
}
