package de.a12.studio.ui;

import de.a12.studio.dataservices.projects.Project;
import de.a12.studio.dataservices.projects.ProjectItem;
import de.a12.studio.ui.events.ProjectClosedEvent;
import de.a12.studio.ui.events.ProjectOpenedEvent;
import de.a12.studio.ui.events.StudioEventListener;
import de.a12.studio.ui.events.StudioEventManager;
import de.a12.studio.ui.projecttree.ProjectTreeController;
import de.a12.studio.ui.tabs.TabPaneController;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.StackPane;
import org.jspecify.annotations.NonNull;

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

  private Project project;

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    StudioEventManager.getInstance().addListener(this);

    mainSplitPane.getDividers().get(0).positionProperty().addListener((observable, oldValue, newValue) -> {
      if (project != null) {
        project.getSettings().setDividerPosition(newValue.doubleValue());
        project.getSettings().save();
      }
    });
  }

  @Override
  public void projectOpened(@NonNull ProjectOpenedEvent event) {
    this.mainSplitPane.setVisible(true);
    this.project = event.getProject();
    double dividerPosition = project.getSettings().getDividerPosition();
    Platform.runLater(() -> mainSplitPane.setDividerPositions(dividerPosition));
  }

  @Override
  public void projectClosed(@NonNull ProjectClosedEvent event) {
    this.mainSplitPane.setVisible(false);
  }

  public void setTitle(String s) {
    headerController.setTitle(s);
  }

  public ProjectItem getSelectedProjectItem() {
    return tabPaneController.getSelectedProjectItem();
  }
}
