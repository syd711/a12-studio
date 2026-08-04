package de.a12.studio.ui;

import de.a12.studio.ui.util.FXResizeHelper;
import de.a12.studio.ui.util.StudioVersion;
import de.a12.studio.ui.util.WidgetFactory;
import de.a12.studio.ui.util.localsettings.LocalUISettings;
import de.a12.studio.models.A12Model;
import de.a12.studio.models.projects.Project;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.models.projects.settings.A12Settings;
import de.a12.studio.modelsvalidation.ValidationService;
import de.a12.studio.ui.events.PreferencesOpenRequestedEvent;
import de.a12.studio.ui.events.ProjectClosedEvent;
import de.a12.studio.ui.events.ProjectOpenedEvent;
import de.a12.studio.ui.events.StudioEventListener;
import de.a12.studio.ui.events.StudioEventManager;
import de.a12.studio.ui.preview.PreviewServer;
import de.a12.studio.ui.previewapp.PreviewAppProcess;
import de.a12.studio.ui.util.WindowsSnapHook;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * App bootstrap - undecorated window shell (custom draggable/resizable header, empty main
 * region).
 */
@Slf4j
public class Studio extends Application implements StudioEventListener {

  public static Stage stage;
  private static RootController rootController;
  private static Project currentProject;
  private static ValidationService validationService;
  private static WindowsSnapHook windowsSnapHook;

  @Override
  public void start(Stage stage) throws IOException {
    Studio.stage = stage;

    // Apply stored language preference before any FXML is loaded.
    String storedLang = LocalUISettings.getString("language");
    if (storedLang != null && !storedLang.isBlank()) {
      Locale.setDefault(Locale.forLanguageTag(storedLang));
    }

    StudioEventManager.getInstance().addListener(this);

    FXMLLoader loader = new FXMLLoader(Studio.class.getResource("scene-root.fxml"));
    Parent root = loader.load();
    rootController = loader.getController();

    Rectangle2D screenBounds = Screen.getPrimary().getBounds();
    double width = 1480;
    double height = 900;

    Rectangle position = LocalUISettings.getPosition();
    if (position.getWidth() > width && position.getHeight() > height) {
      width = position.getWidth();
      height = position.getHeight();
    }

    Scene scene = new Scene(root, width, height, Color.TRANSPARENT);
    StudioKeyEventHandler keyEventHandler = new StudioKeyEventHandler(stage);
    scene.addEventHandler(KeyEvent.KEY_PRESSED, keyEventHandler);
    // also listen for KEY_RELEASED so StudioKeyEventHandler can track when the Windows key
    // (Win+arrow window snapping) is released, since it isn't reported as a KeyEvent modifier
    scene.addEventHandler(KeyEvent.KEY_RELEASED, keyEventHandler);
    stage.setTitle("A12 Studio");
    stage.getIcons().add(new Image(Studio.class.getResourceAsStream("logo-180.png")));
    stage.setScene(scene);
    stage.setMinWidth(1480);
    stage.setMinHeight(900);
    stage.setResizable(true);
    stage.initStyle(StageStyle.TRANSPARENT);
    if (position.getX() != -1) {
      stage.setX(position.getX());
      stage.setY(position.getY());
    }
    else {
      stage.setX((screenBounds.getWidth() / 2) - (width / 2));
      stage.setY((screenBounds.getHeight() / 2) - (height / 2));
    }

    FXResizeHelper.install(stage, 30, 6);
    stage.show();

    // Windows denies focus/z-order to windows created by a background process (e.g. launched
    // from IDEA), so the stage can open behind the IDE. Toggling always-on-top forces it front.
    stage.setAlwaysOnTop(true);
    stage.toFront();
    stage.requestFocus();
    stage.setAlwaysOnTop(false);

    // Win+Left/Right are reserved by the Windows shell's own Snap Assist ahead of normal window
    // messages, so a plain JavaFX key listener never sees them (unlike Win+Up/Down, which
    // StudioKeyEventHandler already handles as a fallback). This hook intercepts them earlier.
    windowsSnapHook = new WindowsSnapHook(key -> Platform.runLater(() -> handleWindowsSnapKey(key)));
    windowsSnapHook.install();

    Platform.runLater(Studio::checkA12InstallationFolder);
  }

  private static void checkA12InstallationFolder() {
    String installationPath = A12Settings.load().getInstallationPath();
    if (installationPath != null && !installationPath.isEmpty()) {
      return;
    }

    Optional<ButtonType> result = WidgetFactory.showConfirmation(stage,
        "The A12 installation folder is not set.",
        "Some features may not work correctly until it is configured.", null, "Go to Settings");
    if (result.isPresent() && ButtonType.OK.equals(result.get())) {
      StudioEventManager.getInstance().firePreferencesOpenRequestedEvent(PreferencesOpenRequestedEvent.Section.A12_INSTALLATION);
    }
  }

  private static void handleWindowsSnapKey(WindowsSnapHook.SnapKey key) {
    if (!(stage.getUserData() instanceof FXResizeHelper helper)) {
      return;
    }
    switch (key) {
      case LEFT -> helper.snapLeft();
      case RIGHT -> helper.snapRight();
      case UP -> helper.maximize();
      case DOWN -> helper.restoreOrMinimize();
    }
  }

  @Override
  public void stop() {
    if (windowsSnapHook != null) {
      windowsSnapHook.uninstall();
    }
    PreviewServer.stopIfRunning();
    PreviewAppProcess.getInstance().stop();
  }

  public static ProjectItem getSelectedProjectItem() {
    return rootController.getSelectedProjectItem();
  }

  public static void closeSelectedTab() {
    rootController.closeSelectedTab();
  }

  public static void selectNextTab() {
    rootController.selectNextTab();
  }

  public static void selectPreviousTab() {
    rootController.selectPreviousTab();
  }

  public static Project getCurrentProject() {
    return currentProject;
  }

  public static ValidationService getValidationService() {
    return validationService;
  }

  @Override
  public void projectOpened(@NonNull ProjectOpenedEvent event) {
    // Set before anything else: Studio registers as a listener before the FXML (and its nested
    // controllers, e.g. TabPaneController) is loaded, so this runs before TabPaneController restores
    // previously-open tabs - those tabs load their document model editors immediately and need the
    // project to already be available for cross-model settings validation (e.g. the settings button's
    // error badge).
    currentProject = event.getProject();
    validationService = new ValidationService(currentProject);

    String studioVersion = StudioVersion.get();
    stage.setTitle("A12 Studio - " + studioVersion + " - " + currentProject.getName());
    rootController.setTitle("A12 Studio - " + studioVersion + " - " + currentProject.getName() + " (" + currentProject.getRoot().getPath() + ")");

    boolean b = checkModelVersions(currentProject);

//    if (!b) {
//      PreviewAppProcess.getInstance().stop();
//      StudioEventManager.getInstance().fireProjectClosedEvent(currentProject);
//    }
  }

  @Override
  public void projectClosed(@NonNull ProjectClosedEvent event) {
    currentProject = null;
    validationService = null;
    stage.setTitle("A12 Studio");
  }

  private static boolean checkModelVersions(Project project) {
    List<String> incompatibleModels = new ArrayList<>();
    collectIncompatibleModels(project.getRoot(), incompatibleModels);
    if (incompatibleModels.isEmpty()) {
      return true;
    }
    incompatibleModels.forEach(log::warn);

    WidgetFactory.showAlert(stage,
        "Incompatible model versions found" , "Wrong Version: " + incompatibleModels.get(0));
    return false;
  }

  private static void collectIncompatibleModels(ProjectItem item, List<String> incompatibleModels) {
    if (item.isFolder()) {
      for (ProjectItem child : item.getChildren()) {
        collectIncompatibleModels(child, incompatibleModels);
      }
      return;
    }

    A12Model<?> model = item.getModel();
    if (model == null) {
      return;
    }

    String expectedVersion = model.getModelType().getCurrentVersion();
    if (!expectedVersion.equals(model.getModelVersion())) {
      incompatibleModels.add(
          model.getId() + " (" + model.getModelType().getDisplayName() + "): expected " + expectedVersion + ", found "
              + model.getModelVersion());
    }
  }
}
