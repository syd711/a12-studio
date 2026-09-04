package de.a12.studio.ui.components;

import de.a12.studio.models.projects.Project;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.ui.events.StudioEventManager;
import de.a12.studio.ui.projecttree.ProjectItemViewModel;
import de.a12.studio.ui.util.Icons;
import de.a12.studio.ui.util.RecentEditsTracker;
import de.a12.studio.ui.util.WidgetFactory;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import org.jspecify.annotations.NonNull;
import org.kordamp.ikonli.javafx.FontIcon;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Headerless "recent files" palette (Ctrl+E): lists the files edited during the current session,
 * most recently edited first, backed by {@link RecentEditsTracker}'s in-memory (never persisted)
 * history. The list is rebuilt fresh every time the dialog opens, so it always reflects the
 * latest edits rather than a stored snapshot.
 */
public class RecentFilesDialogController implements DialogController {

  @FXML
  private TextField searchField;

  @FXML
  private ListView<ProjectItem> resultsList;

  @FXML
  private Label footerPathLabel;

  private Stage stage;
  private Project project;
  private List<ProjectItem> allItems;

  public static void show(@NonNull Stage owner, Project project) {
    if (project == null) {
      return;
    }

    Stage stage = WidgetFactory.createDialogStage(null, RecentFilesDialogController.class, owner, "Recent Files", "dialog-recent-files.fxml");
    RecentFilesDialogController controller = (RecentFilesDialogController) stage.getUserData();
    controller.initDialog(stage, project);

    double width = 640;
    double height = 440;
    stage.setWidth(width);
    stage.setHeight(height);
    stage.setX(owner.getX() + (owner.getWidth() - width) / 2);
    stage.setY(owner.getY() + 90);

    stage.showAndWait();
  }

  private void initDialog(@NonNull Stage stage, @NonNull Project project) {
    this.stage = stage;
    this.project = project;

    allItems = RecentEditsTracker.getInstance().getRecentlyEdited();

    resultsList.setCellFactory(list -> new RecentFileCell());
    resultsList.getSelectionModel().selectedItemProperty().addListener((obs, old, selected) -> updateFooter(selected));
    resultsList.setOnMouseClicked(event -> {
      if (event.getClickCount() == 2) {
        openSelected();
      }
    });
    resultsList.setOnKeyPressed(event -> {
      if (event.getCode() == KeyCode.ENTER) {
        openSelected();
      }
    });

    searchField.textProperty().addListener((obs, old, text) -> refreshResults(text));
    searchField.setOnKeyPressed(event -> {
      switch (event.getCode()) {
        case DOWN -> {
          moveSelection(1);
          event.consume();
        }
        case UP -> {
          moveSelection(-1);
          event.consume();
        }
        case ENTER -> {
          openSelected();
          event.consume();
        }
        default -> {
        }
      }
    });

    searchField.requestFocus();
    refreshResults(null);
  }

  private void refreshResults(String query) {
    String term = query == null ? "" : query.trim().toLowerCase();

    List<ProjectItem> results;
    if (term.isEmpty()) {
      results = allItems;
    }
    else {
      results = new ArrayList<>();
      for (ProjectItem item : allItems) {
        if (item.getName().toLowerCase().contains(term)) {
          results.add(item);
        }
      }
    }

    resultsList.getItems().setAll(results);
    if (!results.isEmpty()) {
      resultsList.getSelectionModel().selectFirst();
    }
    else {
      footerPathLabel.setText("");
    }
  }

  private void moveSelection(int delta) {
    int size = resultsList.getItems().size();
    if (size == 0) {
      return;
    }
    int current = resultsList.getSelectionModel().getSelectedIndex();
    int next = Math.max(0, Math.min(size - 1, current + delta));
    resultsList.getSelectionModel().select(next);
    resultsList.scrollTo(next);
  }

  private void updateFooter(ProjectItem item) {
    footerPathLabel.setText(item == null ? "" : relativePath(item));
  }

  private void openSelected() {
    ProjectItem item = resultsList.getSelectionModel().getSelectedItem();
    if (item == null) {
      return;
    }

    project.getSettings().getUISettings().addOpenedFile(item.getPath());
    project.getSettings().getUISettings().save();
    StudioEventManager.getInstance().fireModelOpenEvent(item);
    stage.close();
  }

  @Override
  public void onDialogCancel() {
    stage.close();
  }

  private String relativePath(@NonNull ProjectItem item) {
    Path relative = project.getFolder().toPath().toAbsolutePath()
        .relativize(item.getFile().toPath().toAbsolutePath());
    return relative.toString();
  }

  private class RecentFileCell extends ListCell<ProjectItem> {
    private final Label nameLabel = new Label();
    private final Label parentPathLabel = new Label();
    private final HBox row;

    RecentFileCell() {
      nameLabel.getStyleClass().add("search-result-name");
      parentPathLabel.getStyleClass().add("search-result-path");
      row = new HBox(8, new Label(), nameLabel, parentPathLabel);
      row.setAlignment(Pos.CENTER_LEFT);
    }

    @Override
    protected void updateItem(ProjectItem item, boolean empty) {
      super.updateItem(item, empty);
      if (empty || item == null) {
        setGraphic(null);
        return;
      }

      nameLabel.setText(item.getName());
      parentPathLabel.setText(relativePath(item));
      row.getChildren().set(0, createResultIcon(item));
      setGraphic(row);
    }
  }

  private Node createResultIcon(ProjectItem item) {
    if (item.getModel() != null) {
      String iconPath = new ProjectItemViewModel(item, Map.of()).getIconPath();
      if (iconPath != null) {
        return WidgetFactory.createModelIcon(iconPath);
      }
    }
    FontIcon icon = new FontIcon();
    icon.setIconSize(18);
    icon.setIconLiteral(Icons.FILE_OUTLINE);
    return icon;
  }
}
