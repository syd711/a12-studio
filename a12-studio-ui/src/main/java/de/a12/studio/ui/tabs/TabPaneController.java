package de.a12.studio.ui.tabs;

import de.a12.studio.ui.util.WidgetFactory;
import de.a12.studio.dataservices.models.documentmodel.DocumentModel;
import de.a12.studio.dataservices.projects.Project;
import de.a12.studio.dataservices.projects.ProjectItem;
import de.a12.studio.ui.events.*;
import de.a12.studio.ui.editors.documentmodel.DocumentModelEditorController;
import de.a12.studio.ui.util.Icons;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.paint.Color;
import org.jspecify.annotations.NonNull;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class TabPaneController implements Initializable, StudioEventListener {

  @FXML
  private TabPane tabPane;

  private Project project;

  private boolean restoringSelection;

  @Override
  public void projectOpened(@NonNull ProjectOpenedEvent event) {
    this.project = event.getProject();
    tabPane.getTabs().clear();

    restoringSelection = true;
    try {
      String selectedFile = event.getProject().getSettings().getUISettings().getSelectedFile();
      Tab tabToSelect = null;
      for (String path : event.getProject().getSettings().getUISettings().getOpenedFiles()) {
        File file = new File(path);
        if (file.exists()) {
          open(new ProjectItem(file));
          if (path.equals(selectedFile)) {
            tabToSelect = tabPane.getTabs().get(tabPane.getTabs().size() - 1);
          }
        }
      }
      if (tabToSelect != null) {
        tabPane.getSelectionModel().select(tabToSelect);
      }
    }
    finally {
      restoringSelection = false;
    }
  }

  @Override
  public void modelOpened(@NonNull ModelOpenedEvent event) {
    for (Tab existingTab : tabPane.getTabs()) {
      ProjectItem existingItem = (ProjectItem) existingTab.getUserData();
      if (existingItem != null && existingItem.getPath().equals(event.getItem().getPath())) {
        tabPane.getSelectionModel().select(existingTab);
        return;
      }
    }

    open(event.getItem());
  }

  private void open(@NonNull ProjectItem item) {
    try {
      FXMLLoader loader = new FXMLLoader(DocumentModelEditorController.class.getResource("document-model-editor.fxml"));
      Parent content = loader.load();

      if (item.getModel() instanceof DocumentModel documentModel) {
        DocumentModelEditorController controller = loader.getController();
        controller.load(item);
      }

      Tab tab = new Tab(item.getName(), content);
      tab.setUserData(item);
      tab.setClosable(true);

      FontIcon icon = WidgetFactory.createIcon(Icons.FILE_OUTLINE);
      icon.setIconColor(Color.valueOf(WidgetFactory.DEFAULT_COLOR));
      tab.setGraphic(icon);
      tab.setContextMenu(createTabContextMenu(tab));
      tab.setOnClosed(closeEvent -> onTabClosed(tab));
      tabPane.getTabs().add(tab);
      tabPane.getSelectionModel().select(tab);
      installDoubleClickHandler(tab);
    }
    catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  private void installDoubleClickHandler(@NonNull Tab tab) {
    Platform.runLater(() -> {
      Node tabNode = tab.getTabPane();
      if (tabNode != null) {
        tabNode.setOnMouseClicked(event -> {
          if (event.getClickCount() == 2) {
            ProjectItem projectItem = (ProjectItem) tab.getUserData();
            if (projectItem != null) {
              StudioEventManager.getInstance().fireModelFocusRequestedEvent(projectItem);
            }
          }
        });
      }
    });
  }

  private void onTabClosed(@NonNull Tab tab) {
    ProjectItem projectItem = (ProjectItem) tab.getUserData();
    if (project != null && projectItem != null) {
      project.getSettings().getUISettings().removeOpenedFile(projectItem.getPath());
      project.getSettings().getUISettings().save();
    }
    StudioEventManager.getInstance().fireModelClosedEvent(projectItem);
  }

  private ContextMenu createTabContextMenu(@NonNull Tab tab) {
    MenuItem close = new MenuItem("_Close");
    close.setOnAction(event -> closeTab(tab));

    MenuItem closeAll = new MenuItem("Close _All");
    closeAll.setOnAction(event -> {
      for (Tab t : new ArrayList<>(tabPane.getTabs())) {
        closeTab(t);
      }
    });

    MenuItem closeOthers = new MenuItem("Close _Others");
    closeOthers.setOnAction(event -> {
      for (Tab t : new ArrayList<>(tabPane.getTabs())) {
        if (t != tab) {
          closeTab(t);
        }
      }
    });

    return new ContextMenu(close, closeAll, closeOthers);
  }

  private void closeTab(@NonNull Tab tab) {
    tabPane.getTabs().remove(tab);
    onTabClosed(tab);
  }

  public ProjectItem getSelectedProjectItem() {
    Tab selectedTab = tabPane.getSelectionModel().getSelectedItem();
    return selectedTab == null ? null : (ProjectItem) selectedTab.getUserData();
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    StudioEventManager.getInstance().addListener(this);
    tabPane.getSelectionModel().selectedItemProperty().addListener((observable, oldTab, newTab) -> onSelectionChanged(newTab));
  }

  private void onSelectionChanged(Tab newTab) {
    if (restoringSelection || project == null) {
      return;
    }
    ProjectItem item = newTab == null ? null : (ProjectItem) newTab.getUserData();
    project.getSettings().getUISettings().setSelectedFile(item == null ? null : item.getPath());
    project.getSettings().getUISettings().save();
  }
}
