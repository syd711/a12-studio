package de.a12.studio.ui.tabs;

import de.a12.studio.commons.util.WidgetFactory;
import de.a12.studio.dataservices.projects.ProjectItem;
import de.a12.studio.ui.events.ModelOpenedEvent;
import de.a12.studio.ui.events.ProjectOpenedEvent;
import de.a12.studio.ui.events.StudioEventListener;
import de.a12.studio.ui.events.StudioEventManager;
import de.a12.studio.ui.editors.documentmodel.DocumentModelEditorController;
import de.a12.studio.ui.util.Icons;
import javafx.application.Platform;
import javafx.collections.ObservableList;
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

  @Override
  public void projectOpened(@NonNull ProjectOpenedEvent event) {
    tabPane.getTabs().clear();

    for (String path : event.getProject().getSettings().getOpenedFiles()) {
      File file = new File(path);
      if (file.exists()) {
        StudioEventManager.getInstance().fireModelOpenEvent(new ProjectItem(file));
      }
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

    try {
      FXMLLoader loader = new FXMLLoader(DocumentModelEditorController.class.getResource("document-model-editor.fxml"));
      Parent content = loader.load();

      Tab tab = new Tab(event.getItem().getName(), content);
      tab.setUserData(event.getItem());
      tab.setClosable(true);

      FontIcon icon = WidgetFactory.createIcon(Icons.FILE_OUTLINE);
      icon.setIconColor(Color.valueOf(WidgetFactory.DEFAULT_COLOR));
      tab.setGraphic(icon);
      tab.setContextMenu(createTabContextMenu(tab));
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

  private ContextMenu createTabContextMenu(@NonNull Tab tab) {
    MenuItem close = new MenuItem("Close");
    close.setOnAction(event -> {
      ProjectItem projectItem = (ProjectItem) tab.getUserData();
      tabPane.getTabs().remove(tab);
      StudioEventManager.getInstance().fireModelClosedEvent(projectItem);
    });

    MenuItem closeAll = new MenuItem("Close All");
    closeAll.setOnAction(event -> {

      ObservableList<Tab> tabs = tabPane.getTabs();
      for (Tab t : new ArrayList<>(tabs)) {
        ProjectItem projectItem = (ProjectItem) t.getUserData();
        tabPane.getTabs().remove(t);
        StudioEventManager.getInstance().fireModelClosedEvent(projectItem);
      }
    });

    MenuItem closeOthers = new MenuItem("Close Others");
    closeOthers.setOnAction(event -> {
      ObservableList<Tab> tabs = tabPane.getTabs();
      for (Tab t : new ArrayList<>(tabs)) {
        if (t != tab) {
          ProjectItem projectItem = (ProjectItem) t.getUserData();
          tabPane.getTabs().remove(t);
          StudioEventManager.getInstance().fireModelClosedEvent(projectItem);
        }
      }
    });

    return new ContextMenu(close, closeAll, closeOthers);
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    StudioEventManager.getInstance().addListener(this);
  }
}
