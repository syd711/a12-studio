package de.a12.studio.ui.util;

import de.a12.studio.ui.components.ConfirmationDialogController;
import de.a12.studio.ui.components.ConfirmationDialogWithCheckboxController;
import de.a12.studio.ui.components.ConfirmationDialogWithOptionController;
import de.a12.studio.ui.components.DialogController;
import de.a12.studio.ui.components.DialogHeaderController;
import de.a12.studio.ui.components.InputDialogController;
import de.a12.studio.ui.components.OutputDialogController;
import de.a12.studio.ui.components.TextAreaInputDialogController;
import de.a12.studio.ui.util.localsettings.LocalUISettings;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.PopupControl;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import javafx.stage.WindowEvent;
import javafx.util.Duration;
import org.apache.commons.io.FilenameUtils;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.kordamp.ikonli.javafx.FontIcon;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
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
    tooltip.setMaxWidth(DEFAULT_TOOLTIP_WIDTH);
    tooltip.setShowDuration(Duration.seconds(10));
    // Tooltips inherit -fx-font from their owner node (e.g. a monospace text area or the
    // "Impact"-styled window header title), so pin it back to the platform default explicitly.
    tooltip.setStyle("-fx-font-family: 'System';");
    return tooltip;
  }

  public static Label createDefaultLabel(String msg) {
    Label label = new Label(msg);
    label.setStyle("-fx-font-size: 1em;");
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

  private static final Map<String, Image> MODEL_ICON_CACHE = new HashMap<>();

  public static ImageView createModelIcon(@NonNull String iconPath) {
    ImageView imageView = new ImageView(loadModelIcon(iconPath));
    imageView.getStyleClass().add("tree-icon");
    imageView.setFitWidth(DEFAULT_ICON_SIZE);
    imageView.setFitHeight(DEFAULT_ICON_SIZE);
    imageView.setPreserveRatio(true);
    return imageView;
  }

  private static Image loadModelIcon(@NonNull String iconPath) {
    return MODEL_ICON_CACHE.computeIfAbsent(iconPath,
        path -> new Image(WidgetFactory.class.getResourceAsStream(path), DEFAULT_ICON_SIZE, DEFAULT_ICON_SIZE, true, true));
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
            textarea.setStyle("-fx-font-size: 1em;");
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

  /**
   * Restricts a text field to decimal input, rejecting any keystroke that would result in a value other than
   * an optionally negative, optionally fractional number (e.g. while it is being typed: "-", "-1", "1.").
   */
  public static void restrictToDecimalInput(TextField textField) {
    UnaryOperator<TextFormatter.Change> filter = change -> {
      String newText = change.getControlNewText();
      return (newText.isEmpty() || newText.matches("-?\\d*\\.?\\d*")) ? change : null;
    };
    textField.setTextFormatter(new TextFormatter<>(filter));
  }

  //---------------------------------------------
  // Stage / dialog infrastructure

  private static final int DIALOG_SHADOW_RADIUS = 18;
  private static final int DIALOG_SHADOW_OFFSET_Y = 6;
  // must cover the shadow's max extent (radius + offsetY on the bottom side) with room to spare;
  // public so callers that install FXResizeHelper on a dialog Stage can keep its edge/drag
  // hit-zones aligned with the visible border instead of this padding (see FXResizeHelper.MARGIN)
  public static final int DIALOG_SHADOW_MARGIN = 24;
  // upper bound for a dialog's initial height when no size was restored from the settings
  private static final double DIALOG_MAX_INITIAL_HEIGHT = 800;

  /**
   * Applies the user's configured base UI text size (see {@link LocalUISettings#getFontSize()})
   * to {@code root} as an inline "-fx-font-size" style. Since -fx-font-size is inherited and every
   * other font-size in the theme is expressed relative to it (see stylesheet-typography.css), this
   * single inline override rescales the whole subtree - main window, every dialog (via {@link
   * #createDialogStage}) and the preview app console window all call this on their own root so a
   * Preferences change takes effect immediately, without needing a full app restart.
   */
  public static void applyFontSize(Parent root) {
    root.setStyle("-fx-font-size: " + LocalUISettings.getFontSize() + "px;");
    installPopupFontSizeListener();
  }

  private static final ReadOnlyIntegerWrapper fontSize = new ReadOnlyIntegerWrapper(LocalUISettings.getFontSize());

  /** The configured base UI text size in px; updated by {@link #applyFontSizeToAllOpenWindows()},
   *  i.e. whenever the user changes it, for sizes that can't be expressed in the stylesheet. */
  public static ReadOnlyIntegerProperty fontSizeProperty() {
    return fontSize.getReadOnlyProperty();
  }

  /**
   * Makes {@code icon} scale with the UI font size. The size it has right now is taken as the size
   * authored for {@link LocalUISettings#DEFAULT_FONT_SIZE}. A stylesheet can't do this: an em-based
   * "-fx-icon-size" is resolved against the icon's own font, which itself derives from the icon
   * size, so it compounds.
   */
  public static void bindIconSizeToFontSize(FontIcon icon) {
    int base = icon.getIconSize();
    icon.iconSizeProperty().bind(Bindings.createIntegerBinding(
        () -> (int) Math.round((double) base * fontSize.get() / LocalUISettings.DEFAULT_FONT_SIZE), fontSize));
  }

  /** {@link #bindIconSizeToFontSize(FontIcon)} for bitmap icons: scales the fit size. */
  public static void bindIconSizeToFontSize(ImageView icon) {
    double baseWidth = icon.getFitWidth();
    double baseHeight = icon.getFitHeight();
    icon.fitWidthProperty().bind(Bindings.createDoubleBinding(
        () -> baseWidth * fontSize.get() / LocalUISettings.DEFAULT_FONT_SIZE, fontSize));
    icon.fitHeightProperty().bind(Bindings.createDoubleBinding(
        () -> baseHeight * fontSize.get() / LocalUISettings.DEFAULT_FONT_SIZE, fontSize));
  }

  private static final String FONT_SIZE_STYLE = "-fx-font-size:";
  private static boolean popupFontSizeListenerInstalled;

  /**
   * Menus, context menus, tooltips and combo box dropdowns are {@link PopupControl}s: they live in
   * their own window whose scene is not a child of the window root {@link #applyFontSize} styles,
   * so they would otherwise stay at the JavaFX default size. This applies the configured size to
   * each popup as it is created; already-open ones are covered by
   * {@link #applyFontSizeToAllOpenWindows()}.
   */
  private static void installPopupFontSizeListener() {
    if (popupFontSizeListenerInstalled) {
      return;
    }
    popupFontSizeListenerInstalled = true;
    Window.getWindows().addListener((ListChangeListener<Window>) change -> {
      while (change.next()) {
        for (Window window : change.getAddedSubList()) {
          if (window instanceof PopupControl popup) {
            applyFontSize(popup);
          }
        }
      }
    });
  }

  /** Sets the configured font size on a popup while keeping any other inline style it already has
   *  (e.g. the font family {@link #createTooltip} sets). */
  private static void applyFontSize(PopupControl popup) {
    StringBuilder style = new StringBuilder();
    String existing = popup.getStyle();
    if (existing != null) {
      for (String declaration : existing.split(";")) {
        if (!declaration.isBlank() && !declaration.strip().startsWith(FONT_SIZE_STYLE)) {
          style.append(declaration.strip()).append("; ");
        }
      }
    }
    style.append(FONT_SIZE_STYLE).append(' ').append(LocalUISettings.getFontSize()).append("px;");
    popup.setStyle(style.toString());
  }

  /**
   * Changes the persisted base UI text size by {@code delta} px (clamped to the range the Preferences
   * spinner allows) and applies it to every open window, so the Ctrl+Plus/Ctrl+Minus shortcuts behave
   * exactly like moving the Preferences spinner.
   */
  public static void changeFontSize(int delta) {
    int current = LocalUISettings.getFontSize();
    int size = Math.min(LocalUISettings.MAX_FONT_SIZE, Math.max(LocalUISettings.MIN_FONT_SIZE, current + delta));
    if (size == current) {
      return;
    }
    LocalUISettings.saveProperty(LocalUISettings.FONT_SIZE, String.valueOf(size));
    applyFontSizeToAllOpenWindows();
  }

  /** Re-applies {@link LocalUISettings#getFontSize()} to every currently open window's root (main
   *  window, every open dialog, the preview app console), so moving the Preferences slider takes
   *  effect immediately instead of requiring a restart. */
  public static void applyFontSizeToAllOpenWindows() {
    fontSize.set(LocalUISettings.getFontSize());
    for (Window window : Window.getWindows()) {
      if (window instanceof PopupControl popup) {
        applyFontSize(popup);
        continue;
      }
      Scene scene = window.getScene();
      if (scene == null || scene.getRoot() == null) {
        continue;
      }
      // The dialog/console windows wrap their FXML root in an untargeted shadow-padding StackPane
      // (see #createDialogStage), so the actual ".root"-styled node - the one applyFontSize was
      // originally called on - has to be looked up rather than assumed to be the scene's own root.
      Node styledRoot = scene.getRoot().getStyleClass().contains("root") ? scene.getRoot() : scene.getRoot().lookup(".root");
      if (styledRoot instanceof Parent parent) {
        applyFontSize(parent);
      }
    }
  }

  public static Stage createStage() {
    Stage stage = new Stage();
    stage.initStyle(StageStyle.TRANSPARENT);
    return stage;
  }

  // shared across all dialogs so opening a dialog doesn't spin up a new executor thread each time
  private static final Debouncer dialogPositionDebouncer = new Debouncer();

  public static Stage createDialogStage(Class clazz, Stage owner, String title, String fxml) {
    FXMLLoader fxmlLoader = new FXMLLoader(clazz.getResource(fxml));
fxmlLoader.setClassLoader(clazz.getClassLoader());
fxmlLoader.setResources(StudioBundle.getBundle());
    String stateId = FilenameUtils.getBaseName(fxml);
    return createDialogStage(stateId, fxmlLoader, owner, title);
  }

  public static Stage createDialogStage(String stateId, Class clazz, Stage owner, String title, String fxml) {
    FXMLLoader fxmlLoader = new FXMLLoader(clazz.getResource(fxml));
fxmlLoader.setClassLoader(clazz.getClassLoader());
fxmlLoader.setResources(StudioBundle.getBundle());
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

    if (root != null) {
      // Scene auto-adds the "root" style class (see stylesheet.css's ".root" rule, which supplies
      // -fx-background-color) to whatever node is the actual Scene root. Since shadowWrapper below
      // takes that spot now, root no longer gets it implicitly and must claim it explicitly, or its
      // background/border disappear.
      root.getStyleClass().add("root");
      root.setEffect(new DropShadow(BlurType.GAUSSIAN, Color.rgb(0, 0, 0, 0.35), DIALOG_SHADOW_RADIUS, 0, 0, DIALOG_SHADOW_OFFSET_Y));
      applyFontSize(root);
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

    // The DropShadow effect above is rasterized on top of root's own bounds, so without extra
    // room around it, the Scene (sized to root's layout bounds) clips it away entirely. Wrapping
    // root in a padded, transparent StackPane gives the shadow space to render; the wrapper itself
    // is never targeted by CSS or #header lookups, only root and its subtree are.
    StackPane shadowWrapper = new StackPane(root);
    shadowWrapper.setPadding(new Insets(DIALOG_SHADOW_MARGIN));
    shadowWrapper.setBackground(Background.EMPTY);
    shadowWrapper.setPickOnBounds(false);

    Scene scene = new Scene(shadowWrapper, Color.TRANSPARENT);
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

    boolean sizeRestored = false;
    if(stateId != null) {
      sizeRestored = restoreStagePosition(stateId, stage);
      installStatePersistence(stateId, stage, controller);
    }
    installShowGuards(stage, sizeRestored);

    return stage;
  }

  /**
   * Keeps a freshly shown dialog on screen: unless its size was restored from the local settings, the
   * initial height is capped at {@link #DIALOG_MAX_INITIAL_HEIGHT} (never below the stage's own minimum
   * height), and a negative y coordinate after show (a tall dialog centered on a small screen, or a stale
   * saved position) is reset to 0 so the title bar stays reachable.
   */
  private static void installShowGuards(Stage stage, boolean sizeRestored) {
    if (!sizeRestored) {
      // Runs before the stage is sized to its scene: an explicit height set here wins over the scene's
      // preferred height, and the stage is then centered using the capped height.
      stage.addEventHandler(WindowEvent.WINDOW_SHOWING, e -> {
        Parent sceneRoot = stage.getScene().getRoot();
        sceneRoot.applyCss();
        if (sceneRoot.prefHeight(-1) > DIALOG_MAX_INITIAL_HEIGHT) {
          stage.setHeight(Math.max(DIALOG_MAX_INITIAL_HEIGHT, stage.getMinHeight()));
        }
      });
    }
    stage.addEventHandler(WindowEvent.WINDOW_SHOWN, e -> {
      if (stage.getY() < 0) {
        stage.setY(0);
      }
    });
  }

  /**
   * Enables edge/drag resize (see {@link FXResizeHelper}) on a dialog stage created via {@link
   * #createDialogStage}. The stage's minimum size is derived from the dialog's own FXML root
   * minWidth/minHeight (plus the {@link #DIALOG_SHADOW_MARGIN} padding createDialogStage wraps it
   * in), instead of every call site guessing a number - a mismatched guess smaller than the root's
   * real minimum let the stage shrink past what the content could lay out at, clipping the
   * header/footer. Must be called after {@link #createDialogStage} on the same stage.
   */
  public static void installResizable(Stage stage) {
    FXResizeHelper.install(stage, 30, 6, DIALOG_SHADOW_MARGIN);
    Region content = (Region) ((Pane) stage.getScene().getRoot()).getChildren().get(0);
    stage.setMinWidth(content.minWidth(-1) + 2 * DIALOG_SHADOW_MARGIN);
    stage.setMinHeight(content.minHeight(-1) + 2 * DIALOG_SHADOW_MARGIN);
  }

  /**
   * @return true if a saved height was applied to the stage
   */
  private static boolean restoreStagePosition(String stateId, Stage stage) {
    Rectangle position = LocalUISettings.getPosition(stateId);
    boolean heightRestored = false;
    if (position != null && position.getX() >= 0) {
      stage.setX(position.getX());
      stage.setY(position.getY());
      if (position.getWidth() > 0) {
        stage.setWidth(position.getWidth());
      }
      if (position.getHeight() > 0) {
        stage.setHeight(position.getHeight());
        heightRestored = true;
      }
    }
    return heightRestored;
  }

  private static void installStatePersistence(String stateId, Stage stage, DialogController controller) {
    ChangeListener<Number> listener = (observable, oldValue, newValue) -> {
      int x = (int) stage.getX();
      int y = (int) stage.getY();
      int width = (int) stage.getWidth();
      int height = (int) stage.getHeight();
      dialogPositionDebouncer.debounce(stateId, () -> {
        if (width > 0 && height > 0) {
          LocalUISettings.saveLocation(stateId, x, y, width, height);
        }
        if (controller != null) {
          controller.onResized(x, y, width, height);
        }
      }, 500, true);
    };
    stage.xProperty().addListener(listener);
    stage.yProperty().addListener(listener);
    stage.widthProperty().addListener(listener);
    stage.heightProperty().addListener(listener);
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
    Stage stage = createDialogStage(null, ConfirmationDialogWithOptionController.class, owner, "Confirmation", "dialog-confirmation-with-option.fxml");
    ConfirmationDialogWithOptionController controller = (ConfirmationDialogWithOptionController) stage.getUserData();
    controller.initDialog(stage, optionText, btnText, text, help1, help2);
    stage.showAndWait();
    return controller.getResult();
  }

  public static Optional<ButtonType> showConfirmation(Stage owner, String text, String help1, String help2, String btnText) {
    Stage stage = createDialogStage(null, ConfirmationDialogController.class, owner, "Confirmation", "dialog-confirmation.fxml");
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
    Stage stage = createDialogStage(null, ConfirmationDialogController.class, owner, "Information", "dialog-confirmation.fxml");
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
    Stage stage = createDialogStage(null, ConfirmationDialogController.class, owner, "Information", "dialog-alert.fxml");
    ConfirmationDialogController controller = (ConfirmationDialogController) stage.getUserData();
    controller.hideCancel();
    controller.initDialog(stage, msg, help1, help2);
    stage.showAndWait();
  }

  public static Optional<ButtonType> showAlertOption(Stage owner, String msg, String altOptionText, String okText, String help1, String help2) {
    Stage stage = createDialogStage(null, ConfirmationDialogController.class, owner, "Information", "dialog-alert-option.fxml");
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
    Stage stage = createDialogStage(null, ConfirmationDialogWithCheckboxController.class, owner, "Information", "dialog-alert-option-with-checkbox.fxml");
    ConfirmationDialogWithCheckboxController controller = (ConfirmationDialogWithCheckboxController) stage.getUserData();
    controller.hideCancel();
    controller.initDialog(stage, altOptionText, okText, msg, help1, help2, checkBoxText);
    controller.setChecked(checked);
    stage.showAndWait();
    return controller.getResult();
  }

  public static ConfirmationResult showConfirmationWithCheckbox(Stage owner, String msg, String okText, String help1, String help2, String checkBoxText, boolean checked) {
    Stage stage = createDialogStage(null, ConfirmationDialogWithCheckboxController.class, owner, "Information", "dialog-confirmation-with-checkbox.fxml");
    ConfirmationDialogWithCheckboxController controller = (ConfirmationDialogWithCheckboxController) stage.getUserData();
    controller.hideCancel();
    controller.initDialog(stage, null, okText, msg, help1, help2, checkBoxText);
    controller.setChecked(checked);
    stage.showAndWait();
    return controller.getResult();
  }

  public static ConfirmationResult showConfirmationWithCheckbox(Stage owner, String msg, String okText, String altText, String help1, String help2, String checkBoxText, boolean checked) {
    Stage stage = createDialogStage(null, ConfirmationDialogWithCheckboxController.class, owner, "Information", "dialog-confirmation-with-checkbox.fxml");
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
    return showInputDialog(owner, dialogTitle, innerTitle, description, helpText, defaultValue, null);
  }

  public static String showInputDialog(Stage owner, String dialogTitle, String innerTitle, String description, String helpText, String defaultValue, String fieldTooltip) {
    Stage stage = createDialogStage(null, InputDialogController.class, owner, dialogTitle, "dialog-input.fxml");
    InputDialogController controller = (InputDialogController) stage.getUserData();
    controller.initDialog(stage, innerTitle, description, helpText, defaultValue, fieldTooltip);
    stage.showAndWait();
    Optional<ButtonType> result = controller.getResult();
    if (result.get().equals(ButtonType.OK)) {
      return controller.getText();
    }

    return null;
  }

  public static void showOutputDialog(Stage owner, String dialogTitle, String innerTitle, String description, String defaultValue) {
    Stage stage = createDialogStage(null, OutputDialogController.class, owner, dialogTitle, "dialog-output.fxml");
    OutputDialogController controller = (OutputDialogController) stage.getUserData();
    controller.initDialog(stage, innerTitle, description, defaultValue);
    stage.showAndWait();
  }

  public static String showTextAreaInputDialog(Stage owner, String dialogTitle, String innerTitle, String description, String defaultValue) {
    Stage stage = createDialogStage(null, TextAreaInputDialogController.class, owner, dialogTitle, "dialog-textarea-input.fxml");
    TextAreaInputDialogController controller = (TextAreaInputDialogController) stage.getUserData();
    controller.initDialog(stage, innerTitle, description, defaultValue);
    stage.showAndWait();
    Optional<ButtonType> result = controller.getResult();
    if (result.get().equals(ButtonType.OK)) {
      return controller.getText();
    }

    return null;
  }
}
