package de.a12.studio.ui;

import de.a12.studio.models.projects.Project;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.ui.editors.AnnotationFieldRegistry;
import de.a12.studio.ui.editors.AnnotationHeaderRegistry;
import de.a12.studio.ui.events.PreferencesOpenRequestedEvent;
import de.a12.studio.ui.events.ProjectClosedEvent;
import de.a12.studio.ui.events.ProjectOpenedEvent;
import de.a12.studio.ui.events.StudioEventListener;
import de.a12.studio.ui.events.StudioEventManager;
import de.a12.studio.ui.preferences.PreferencesController;
import de.a12.studio.ui.projecttree.ProjectTreeController;
import de.a12.studio.ui.tabs.TabPaneController;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import org.jspecify.annotations.NonNull;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class RootController implements Initializable, StudioEventListener {

  @FXML
  private StackPane main;

  @FXML
  private SplitPane mainSplitPane;

  @FXML
  private HeaderController headerController;

  @FXML
  private MenuBarController menuBarController;

  @FXML
  private ProjectTreeController projectTreeController;

  @FXML
  private TabPaneController tabPaneController;

  @FXML
  private FooterController footerController;

  @FXML
  private StackPane rootStack;

  private Project project;

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    StudioEventManager.getInstance().addListener(this);
    AnnotationFieldRegistry.getInstance(); // eagerly registers as a listener so it's ready before a project loads
    AnnotationHeaderRegistry.getInstance(); // eagerly registers as a listener so it's ready before a project loads

    mainSplitPane.getDividers().get(0).positionProperty().addListener((observable, oldValue, newValue) -> {
      if (project != null) {
        project.getSettings().getUISettings().setDividerPosition(newValue.doubleValue());
        project.getSettings().getUISettings().save();
      }
    });
  }

  @Override
  public void projectOpened(@NonNull ProjectOpenedEvent event) {
    this.mainSplitPane.setVisible(true);
    this.project = event.getProject();
    double dividerPosition = project.getSettings().getUISettings().getDividerPosition();
    Platform.runLater(() -> mainSplitPane.setDividerPositions(dividerPosition));
  }

  @Override
  public void projectClosed(@NonNull ProjectClosedEvent event) {
    this.mainSplitPane.setVisible(false);
    this.project = null;
  }

  public void setTitle(String s) {
    headerController.setTitle(s);
  }

  public ProjectItem getSelectedProjectItem() {
    return tabPaneController.getSelectedProjectItem();
  }

  public void closeSelectedTab() {
    tabPaneController.closeSelectedTab();
  }

  public void selectNextTab() {
    tabPaneController.selectNextTab();
  }

  public void selectPreviousTab() {
    tabPaneController.selectPreviousTab();
  }

  @Override
  public void preferencesOpenRequested(@NonNull PreferencesOpenRequestedEvent event) {
    try {
      FXMLLoader loader = new FXMLLoader(getClass().getResource("preferences/scene-preferences.fxml"));
      Parent preferencesRoot = loader.load();
      PreferencesController controller = loader.getController();
      controller.setOnCloseRequested(() -> {
        FadeTransition fadeOut = new FadeTransition(Duration.millis(200), preferencesRoot);
        fadeOut.setToValue(0);
        fadeOut.setOnFinished(e -> rootStack.getChildren().remove(preferencesRoot));
        fadeOut.play();
      });
      controller.setProjectOpen(project != null);
      controller.showSection(event.getSection());

      preferencesRoot.setOpacity(0);
      rootStack.getChildren().add(preferencesRoot);
      FadeTransition fadeIn = new FadeTransition(Duration.millis(200), preferencesRoot);
      fadeIn.setToValue(1);
      fadeIn.play();
    }
    catch (IOException e) {
      throw new IllegalStateException("Could not load preferences", e);
    }
  }
}
