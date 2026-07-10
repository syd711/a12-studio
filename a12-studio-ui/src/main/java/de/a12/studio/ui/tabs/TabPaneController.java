package de.a12.studio.ui.tabs;

import de.a12.studio.commons.util.WidgetFactory;
import de.a12.studio.ui.events.ModelOpenedEvent;
import de.a12.studio.ui.events.ProjectOpenedEvent;
import de.a12.studio.ui.events.StudioEventListener;
import de.a12.studio.ui.events.StudioEventManager;
import de.a12.studio.ui.modeleditor.ModelEditorController;
import de.a12.studio.ui.util.Icons;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.paint.Color;
import org.jspecify.annotations.NonNull;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.util.ResourceBundle;

public class TabPaneController implements Initializable, StudioEventListener {

  @FXML
  private TabPane tabPane;

  @Override
  public void projectOpened(@NonNull ProjectOpenedEvent event) {
    tabPane.getTabs().clear();
  }

  @Override
  public void modelOpened(@NonNull ModelOpenedEvent event) {
    try {
      FXMLLoader loader = new FXMLLoader(ModelEditorController.class.getResource("scene-model-editor.fxml"));
      Parent content = loader.load();

      Tab tab = new Tab(event.getItem().getName(), content);
      tab.setClosable(true);

      FontIcon icon = WidgetFactory.createIcon(Icons.FILE_OUTLINE);
      icon.setIconColor(Color.valueOf(WidgetFactory.DEFAULT_COLOR));
      tab.setGraphic(icon);
      tabPane.getTabs().add(tab);
      tabPane.getSelectionModel().select(tab);
    }
    catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    StudioEventManager.getInstance().addListener(this);
  }
}
