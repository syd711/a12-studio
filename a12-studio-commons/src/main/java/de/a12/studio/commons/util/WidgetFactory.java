package de.a12.studio.commons.util;

import de.a12.studio.commons.fx.ConfirmationDialogController;
import de.a12.studio.commons.fx.ConfirmationDialogWithCheckboxController;
import de.a12.studio.commons.fx.ConfirmationDialogWithOptionController;
import de.a12.studio.commons.fx.ConfirmationResult;
import de.a12.studio.commons.fx.DialogController;
import de.a12.studio.commons.fx.DialogHeaderController;
import de.a12.studio.commons.fx.InputDialogController;
import de.a12.studio.commons.fx.OutputDialogController;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.apache.commons.io.FilenameUtils;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.kordamp.ikonli.javafx.FontIcon;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Optional;
import java.util.function.UnaryOperator;

/**
 * Icon factory and dialog infrastructure (stage creation, confirmation/alert/input/output
 * dialogs).
 */
@Slf4j
public class WidgetFactory {

  public static final String DISABLED_TEXT_STYLE = "-fx-font-color: #B0ABAB;-fx-text-fill:#B0ABAB;";
  public static final String DEFAULT_TEXT_STYLE = "-fx-font-color: #FFFFFF;-fx-text-fill:#FFFFFF;";
  public static final String DEFAULT_COLOR = "#000000";
  public static final String DISABLED_COLOR = "#767272";
  public static final String ERROR_COLOR = "#FF3333";
  public static final String ERROR_STYLE = "-fx-font-color: " + ERROR_COLOR + ";-fx-text-fill:" + ERROR_COLOR + ";";
  public static final String UPDATE_COLOR = "#CCFF66";
  public static final String OUTDATED_COLOR = "#FFCC66";
  public static final String OK_COLOR = "#66FF66";
  public static final String OK_DARK_COLOR = "#11aa11";
  public static final String OK_STYLE = "-fx-font-color: " + OK_COLOR + ";-fx-text-fill:" + OK_COLOR + ";";
  public static final int DEFAULT_ICON_SIZE = 18;

  public static final int DEFAULT_TOOLTIP_WIDTH = 500;

  public static Tooltip createTooltip(String text) {
    Tooltip tooltip = new Tooltip(text);
    tooltip.setWrapText(true);
    tooltip.setPrefWidth(DEFAULT_TOOLTIP_WIDTH);
    return tooltip;
  }

  public static Label createDefaultLabel(String msg) {
    Label label = new Label(msg);
    label.setStyle("-fx-font-size: 14px;");
    return label;
  }

  public static FontIcon createCheckIcon() {
    return createCheckIcon(null);
  }

  public static FontIcon createCheckIcon(@Nullable String color) {
    FontIcon fontIcon = new FontIcon();
    fontIcon.setIconSize(DEFAULT_ICON_SIZE);
    fontIcon.setIconLiteral("bi-check-circle");
    fontIcon.setIconColor(Paint.valueOf(color != null ? color : "#66FF66"));
    return fontIcon;
  }

  public static FontIcon createEditIcon(@Nullable String color) {
    FontIcon fontIcon = new FontIcon();
    fontIcon.setIconSize(DEFAULT_ICON_SIZE);
    fontIcon.setIconLiteral("mdi2f-file-document-edit-outline");
    fontIcon.setIconColor(Paint.valueOf(color != null ? color : "#FFFFFF"));
    return fontIcon;
  }

  public static FontIcon createAlertIcon(String s) {
    FontIcon fontIcon = new FontIcon();
    fontIcon.setIconSize(DEFAULT_ICON_SIZE);
    fontIcon.setIconColor(Paint.valueOf(ERROR_COLOR));
    fontIcon.setIconLiteral(s);
    return fontIcon;
  }

  public static FontIcon createGreenIcon(String s) {
    FontIcon fontIcon = new FontIcon();
    fontIcon.setIconSize(DEFAULT_ICON_SIZE);
    fontIcon.setIconColor(Paint.valueOf("#66FF66"));
    fontIcon.setIconLiteral(s);
    return fontIcon;
  }

  public static FontIcon createIcon(String s) {
    return createIcon(s, null);
  }

  public static FontIcon createIcon(String s, String color) {
    return createIcon(s, DEFAULT_ICON_SIZE, color);
  }

  public static FontIcon createIcon(String s, int size, String color) {
    FontIcon fontIcon = new FontIcon();
    fontIcon.setIconSize(size);
    fontIcon.setIconColor(Paint.valueOf(color != null ? color : DEFAULT_COLOR));
    fontIcon.setIconLiteral(s);
    return fontIcon;
  }

  public static FontIcon createCheckboxIcon() {
    return createIcon("bi-check-circle", DEFAULT_ICON_SIZE, null);
  }

  public static FontIcon createCheckboxIcon(@Nullable String color) {
    return createIcon("bi-check-circle", DEFAULT_ICON_SIZE, color);
  }

  public static Label createCheckboxIcon(@Nullable String color, @NonNull String tooltip) {
    Label label = new Label();
    label.setTooltip(createTooltip(tooltip));
    label.setGraphic(createCheckboxIcon(color));
    return label;
  }

  public static FontIcon createUnsupportedIcon() {
    FontIcon fontIcon = new FontIcon();
    fontIcon.setIconSize(DEFAULT_ICON_SIZE);
    fontIcon.setIconColor(Paint.valueOf("#FF9933"));
    fontIcon.setIconLiteral("bi-x-circle");
    return fontIcon;
  }

  public static FontIcon createExclamationIcon() {
    return createExclamationIcon(null);
  }

  public static FontIcon createExclamationIcon(@Nullable String color) {
    return createIcon("bi-exclamation-circle-fill", DEFAULT_ICON_SIZE, color != null ? color : ERROR_COLOR);
  }

  public static FontIcon createWarningIcon(@Nullable String color) {
    return createIcon("bi-exclamation-circle", DEFAULT_ICON_SIZE, color);
  }

  public static Label wrapIcon(FontIcon icon, @NonNull String tooltip) {
    Label label = new Label();
    label.setTooltip(createTooltip(tooltip));
    label.setGraphic(icon);
    return label;
  }

  public static String hexColor(Integer color) {
    String hex = "FFFFFF";
    if (color != null) {
      if (color == 0) {
        hex = "000000";
      }
      else {
        hex = "" + Integer.toHexString(color);
      }
    }
    while (hex.length() < 6) {
      hex = "0" + hex;
    }
    return "#" + hex;
  }

  public static void createHelpIcon(Label label, String tooltip) {
    label.setText("");
    FontIcon fontIcon = new FontIcon();
    fontIcon.setIconSize(DEFAULT_ICON_SIZE);
    fontIcon.setIconColor(Paint.valueOf(DEFAULT_COLOR));
    fontIcon.setIconLiteral("mdi2h-help-circle-outline");
    Tooltip tt = createTooltip(tooltip);
    tt.setWrapText(true);
    tt.setMaxWidth(350);
    label.setTooltip(tt);
    label.setGraphic(fontIcon);
  }

  public static void addToTextListener(Label label) {
    label.managedProperty().bindBidirectional(label.visibleProperty());
    label.textProperty().addListener(new ChangeListener<String>() {
      @Override
      public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
        if (!label.isVisible()) {
          TextField textarea = (TextField) label.getUserData();
          if (textarea != null) {
            ((Pane) label.getParent()).getChildren().remove(textarea);
            label.setVisible(true);
          }
        }
      }
    });

    label.setOnMouseClicked(new EventHandler<MouseEvent>() {
      @Override
      public void handle(MouseEvent mouseEvent) {
        if (mouseEvent.getButton().equals(MouseButton.PRIMARY)) {
          if (mouseEvent.getClickCount() == 2) {
            label.setVisible(false);
            TextField textarea = new TextField(label.getText());
            textarea.setEditable(false);
            textarea.setPrefHeight(label.getHeight());
            textarea.setStyle("-fx-font-size: 14px;");
            label.setUserData(textarea);
            int i = ((Pane) label.getParent()).getChildren().indexOf(label);
            ((Pane) label.getParent()).getChildren().add(i, textarea);
            Platform.runLater(() -> {
              textarea.requestFocus();
              textarea.selectAll();
            });

            textarea.setOnKeyPressed(event -> {
              if (event.getCode().toString().equals("ENTER") || event.getCode().toString().equalsIgnoreCase("ESCAPE")) {
                ((Pane) label.getParent()).getChildren().remove(textarea);
                label.setVisible(true);
              }
            });
          }
        }
      }
    });
  }

  /**
   * Restricts a text field to non-negative integer input, rejecting any keystroke that would result in a
   * non-numeric value.
   */
  public static void restrictToNumericInput(TextField textField) {
    UnaryOperator<TextFormatter.Change> filter = change -> {
      String newText = change.getControlNewText();
      return (newText.isEmpty() || newText.matches("\\d*")) ? change : null;
    };
    textField.setTextFormatter(new TextFormatter<>(filter));
  }

  //---------------------------------------------
  // Stage / dialog infrastructure

  public static Stage createStage() {
    Stage stage = new Stage();
    stage.initStyle(StageStyle.TRANSPARENT);
    return stage;
  }

  public static Stage createDialogStage(Class clazz, Stage owner, String title, String fxml) {
    FXMLLoader fxmlLoader = new FXMLLoader(clazz.getResource(fxml));
    String stateId = FilenameUtils.getBaseName(fxml);
    return createDialogStage(stateId, fxmlLoader, owner, title);
  }

  public static Stage createDialogStage(String stateId, Class clazz, Stage owner, String title, String fxml) {
    FXMLLoader fxmlLoader = new FXMLLoader(clazz.getResource(fxml));
    return createDialogStage(stateId, fxmlLoader, owner, title);
  }

  public static Stage createDialogStage(String stateId, FXMLLoader fxmlLoader, Stage owner, String title) {
    Parent root = null;

    try {
      root = fxmlLoader.load();
    }
    catch (IOException e) {
      log.error("Error loading: " + e.getMessage(), e);
    }

    DialogController controller = fxmlLoader.getController();
    final Stage stage = createStage();

    Node header = root.lookup("#header");
    if (header != null && header.getUserData() instanceof DialogHeaderController) {
      DialogHeaderController dialogHeaderController = (DialogHeaderController) header.getUserData();
      dialogHeaderController.setStage(stage);
      dialogHeaderController.setTitle(title);
    }

    stage.setTitle(title);
    stage.setUserData(controller);
    stage.initOwner(owner);
    stage.initModality(Modality.APPLICATION_MODAL);

    Scene scene = new Scene(root, Color.TRANSPARENT);
    stage.setScene(scene);
    scene.addEventHandler(KeyEvent.KEY_PRESSED, t -> {
      if (t.getCode() == KeyCode.ESCAPE) {
        if (controller != null) {
          controller.onDialogCancel();
        }
        t.consume();
        stage.close();
      }
      else if (controller != null) {
        controller.onKeyPressed(t);
      }
    });

    return stage;
  }

  //---------------------------------------------
  // Dialogs

  public static Optional<ButtonType> showConfirmation(Stage owner, String text) {
    return showConfirmation(owner, text, null, null);
  }

  public static Optional<ButtonType> showConfirmation(Stage owner, String text, String help1) {
    return showConfirmation(owner, text, help1, null);
  }

  public static Optional<ButtonType> showConfirmation(Stage owner, String text, String help1, String help2) {
    return showConfirmation(owner, text, help1, help2, null);
  }

  public static Optional<ButtonType> showConfirmationWithOption(Stage owner, String text, String help1, String help2, String btnText, String optionText) {
    Stage stage = createDialogStage("dialog-confirmation-with-option", ConfirmationDialogWithOptionController.class, owner, "Confirmation", "dialog-confirmation-with-option.fxml");
    ConfirmationDialogWithOptionController controller = (ConfirmationDialogWithOptionController) stage.getUserData();
    controller.initDialog(stage, optionText, btnText, text, help1, help2);
    stage.showAndWait();
    return controller.getResult();
  }

  public static Optional<ButtonType> showConfirmation(Stage owner, String text, String help1, String help2, String btnText) {
    Stage stage = createDialogStage("dialog-confirmation", ConfirmationDialogController.class, owner, "Confirmation", "dialog-confirmation.fxml");
    ConfirmationDialogController controller = (ConfirmationDialogController) stage.getUserData();
    controller.initDialog(stage, null, btnText, text, help1, help2);
    stage.showAndWait();
    return controller.getResult();
  }

  public static Optional<ButtonType> showYesNoConfirmation(Stage owner, String text, String help) {
    return showYesNoConfirmation(owner, text, help, null);
  }

  public static Optional<ButtonType> showYesNoConfirmation(Stage owner, String text, String help1, String help2) {
    Optional<ButtonType> result = showConfirmationWithOption(owner, text, help1, help2, "Yes", "No");
    if (result.isPresent()) {
      if (ButtonType.APPLY.equals(result.get())) {
        return Optional.of(ButtonType.NO);
      }
      else if (ButtonType.OK.equals(result.get())) {
        return Optional.of(ButtonType.YES);
      }
    }
    return result;
  }

  public static Optional<ButtonType> showInformation(Stage owner, String text, String help1) {
    return showInformation(owner, text, help1, null);
  }

  public static Optional<ButtonType> showInformation(Stage owner, String text, String help1, String help2) {
    Stage stage = createDialogStage("dialog-confirmation", ConfirmationDialogController.class, owner, "Information", "dialog-confirmation.fxml");
    ConfirmationDialogController controller = (ConfirmationDialogController) stage.getUserData();
    controller.hideCancel();
    controller.initDialog(stage, text, help1, help2);
    stage.showAndWait();
    return controller.getResult();
  }

  public static void showAlert(Stage owner, String msg) {
    showAlert(owner, msg, null, null);
  }

  public static void showAlert(Stage owner, String msg, String help1) {
    showAlert(owner, msg, help1, null);
  }

  public static void showAlert(Stage owner, String msg, String help1, String help2) {
    Stage stage = createDialogStage("dialog-alert", ConfirmationDialogController.class, owner, "Information", "dialog-alert.fxml");
    ConfirmationDialogController controller = (ConfirmationDialogController) stage.getUserData();
    controller.hideCancel();
    controller.initDialog(stage, msg, help1, help2);
    stage.showAndWait();
  }

  public static Optional<ButtonType> showAlertOption(Stage owner, String msg, String altOptionText, String okText, String help1, String help2) {
    Stage stage = createDialogStage("dialog-alert-option", ConfirmationDialogController.class, owner, "Information", "dialog-alert-option.fxml");
    ConfirmationDialogController controller = (ConfirmationDialogController) stage.getUserData();
    controller.hideCancel();
    controller.initDialog(stage, altOptionText, okText, msg, help1, help2);
    stage.showAndWait();
    return controller.getResult();
  }

  public static ConfirmationResult showAlertOptionWithCheckbox(Stage owner, String msg, String altOptionText, String okText, String help1, String help2, String checkBoxText) {
    return showAlertOptionWithCheckbox(owner, msg, altOptionText, okText, help1, help2, checkBoxText, true);
  }

  public static ConfirmationResult showAlertOptionWithCheckbox(Stage owner, String msg, String altOptionText, String okText, String help1, String help2, String checkBoxText, boolean checked) {
    Stage stage = createDialogStage("dialog-alert-option-with-checkbox", ConfirmationDialogWithCheckboxController.class, owner, "Information", "dialog-alert-option-with-checkbox.fxml");
    ConfirmationDialogWithCheckboxController controller = (ConfirmationDialogWithCheckboxController) stage.getUserData();
    controller.hideCancel();
    controller.initDialog(stage, altOptionText, okText, msg, help1, help2, checkBoxText);
    controller.setChecked(checked);
    stage.showAndWait();
    return controller.getResult();
  }

  public static ConfirmationResult showConfirmationWithCheckbox(Stage owner, String msg, String okText, String help1, String help2, String checkBoxText, boolean checked) {
    Stage stage = createDialogStage("dialog-confirmation-with-checkbox", ConfirmationDialogWithCheckboxController.class, owner, "Information", "dialog-confirmation-with-checkbox.fxml");
    ConfirmationDialogWithCheckboxController controller = (ConfirmationDialogWithCheckboxController) stage.getUserData();
    controller.hideCancel();
    controller.initDialog(stage, null, okText, msg, help1, help2, checkBoxText);
    controller.setChecked(checked);
    stage.showAndWait();
    return controller.getResult();
  }

  public static ConfirmationResult showConfirmationWithCheckbox(Stage owner, String msg, String okText, String altText, String help1, String help2, String checkBoxText, boolean checked) {
    Stage stage = createDialogStage("dialog-confirmation-with-checkbox", ConfirmationDialogWithCheckboxController.class, owner, "Information", "dialog-confirmation-with-checkbox.fxml");
    ConfirmationDialogWithCheckboxController controller = (ConfirmationDialogWithCheckboxController) stage.getUserData();
    controller.hideCancel();
    controller.initDialog(stage, altText, okText, msg, help1, help2, checkBoxText);
    controller.setChecked(checked);
    stage.showAndWait();
    return controller.getResult();
  }

  public static ConfirmationResult showAlertOptionWithMandatoryCheckbox(Stage owner, String msg, String altOptionText, String okText, String help1, String help2, String checkBoxText) {
    Stage stage = createDialogStage("dialog-alert-option-with-checkbox", ConfirmationDialogWithCheckboxController.class, owner, "Information", "dialog-alert-option-with-checkbox.fxml");
    ConfirmationDialogWithCheckboxController controller = (ConfirmationDialogWithCheckboxController) stage.getUserData();
    controller.hideCancel();
    controller.initDialog(stage, altOptionText, okText, msg, help1, help2, checkBoxText);
    controller.setCheckboxMandatory();
    stage.showAndWait();
    return controller.getResult();
  }

  public static String showInputDialog(Stage owner, String dialogTitle, String innerTitle, String description, String helpText, String defaultValue) {
    Stage stage = createDialogStage("dialog-input", InputDialogController.class, owner, dialogTitle, "dialog-input.fxml");
    InputDialogController controller = (InputDialogController) stage.getUserData();
    controller.initDialog(stage, innerTitle, description, helpText, defaultValue);
    stage.showAndWait();
    Optional<ButtonType> result = controller.getResult();
    if (result.get().equals(ButtonType.OK)) {
      return controller.getText();
    }

    return null;
  }

  public static void showOutputDialog(Stage owner, String dialogTitle, String innerTitle, String description, String defaultValue) {
    Stage stage = createDialogStage("dialog-output", OutputDialogController.class, owner, dialogTitle, "dialog-output.fxml");
    OutputDialogController controller = (OutputDialogController) stage.getUserData();
    controller.initDialog(stage, innerTitle, description, defaultValue);
    stage.showAndWait();
  }
}
