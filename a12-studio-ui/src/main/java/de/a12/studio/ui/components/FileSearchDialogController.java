package de.a12.studio.ui.components;

import de.a12.studio.models.projects.Project;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.ui.events.StudioEventManager;
import de.a12.studio.ui.projecttree.ProjectItemViewModel;
import de.a12.studio.ui.util.Icons;
import de.a12.studio.ui.util.WidgetFactory;
import javafx.css.PseudoClass;
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

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Headerless "go to file" / "find in files" palette: searches every file under the currently
 * opened project's folder either by name or by content, depending on the selected tab,
 * defaulting to the most recently modified files when the query is empty.
 */
public class FileSearchDialogController implements DialogController {

  /** Which kind of search the dialog performs, selected via the tab headers. */
  public enum SearchMode {
    FIND_FILES,
    FIND_IN_FILES
  }

  private static final PseudoClass SELECTED = PseudoClass.getPseudoClass("selected");

  private static final int MAX_RECENT_RESULTS = 20;
  private static final int MAX_SEARCH_RESULTS = 300;
  private static final int MIN_CONTENT_QUERY_LENGTH = 2;
  private static final long MAX_CONTENT_SEARCH_FILE_SIZE = 2L * 1024 * 1024;
  private static final int MAX_SNIPPET_LENGTH = 100;

  @FXML
  private Label findFilesTab;

  @FXML
  private Label findInFilesTab;

  @FXML
  private TextField searchField;

  @FXML
  private ListView<File> resultsList;

  @FXML
  private Label footerPathLabel;

  private Stage stage;
  private Project project;
  private List<File> allFiles;
  private Map<File, ProjectItem> itemsByFile;
  private Map<File, String> contentMatchSnippets;
  private SearchMode mode = SearchMode.FIND_FILES;

  public static void show(@NonNull Stage owner, Project project) {
    show(owner, project, SearchMode.FIND_FILES);
  }

  public static void show(@NonNull Stage owner, Project project, @NonNull SearchMode initialMode) {
    if (project == null) {
      return;
    }

    Stage stage = WidgetFactory.createDialogStage(null, FileSearchDialogController.class, owner, "Search", "dialog-file-search.fxml");
    FileSearchDialogController controller = (FileSearchDialogController) stage.getUserData();
    controller.initDialog(stage, project, initialMode);

    double width = 640;
    double height = 440;
    stage.setWidth(width);
    stage.setHeight(height);
    stage.setX(owner.getX() + (owner.getWidth() - width) / 2);
    stage.setY(owner.getY() + 90);

    stage.showAndWait();
  }

  private void initDialog(@NonNull Stage stage, @NonNull Project project, @NonNull SearchMode initialMode) {
    this.stage = stage;
    this.project = project;

    itemsByFile = new HashMap<>();
    indexProjectItems(project.getRoot(), itemsByFile);

    allFiles = new ArrayList<>();
    collectFiles(project.getFolder(), allFiles);
    allFiles.sort(Comparator.comparingLong(File::lastModified).reversed());
    contentMatchSnippets = new HashMap<>();

    findFilesTab.setOnMouseClicked(event -> selectMode(SearchMode.FIND_FILES));
    findInFilesTab.setOnMouseClicked(event -> selectMode(SearchMode.FIND_IN_FILES));
    mode = initialMode;
    updateTabStyles();
    updatePromptText();

    resultsList.setCellFactory(list -> new SearchResultCell());
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
        case TAB -> {
          if (event.isControlDown()) {
            selectMode(mode == SearchMode.FIND_FILES ? SearchMode.FIND_IN_FILES : SearchMode.FIND_FILES);
            event.consume();
          }
        }
        default -> {
        }
      }
    });

    searchField.requestFocus();
    refreshResults(null);
  }

  private void indexProjectItems(@NonNull ProjectItem item, @NonNull Map<File, ProjectItem> map) {
    map.put(item.getFile(), item);
    if (item.isFolder()) {
      for (ProjectItem child : item.getChildren()) {
        indexProjectItems(child, map);
      }
    }
  }

  private void collectFiles(@NonNull File dir, @NonNull List<File> out) {
    File[] children = dir.listFiles();
    if (children == null) {
      return;
    }
    for (File child : children) {
      if (child.isDirectory()) {
        if (!child.getName().startsWith(".")) {
          collectFiles(child, out);
        }
      }
      else {
        out.add(child);
      }
    }
  }

  private void selectMode(@NonNull SearchMode newMode) {
    if (mode == newMode) {
      return;
    }
    mode = newMode;
    updateTabStyles();
    updatePromptText();
    refreshResults(searchField.getText());
    searchField.requestFocus();
  }

  private void updateTabStyles() {
    findFilesTab.pseudoClassStateChanged(SELECTED, mode == SearchMode.FIND_FILES);
    findInFilesTab.pseudoClassStateChanged(SELECTED, mode == SearchMode.FIND_IN_FILES);
  }

  private void updatePromptText() {
    searchField.setPromptText(mode == SearchMode.FIND_IN_FILES ? "Search text in files..." : "Search files...");
  }

  private void refreshResults(String query) {
    String term = query == null ? "" : query.trim().toLowerCase();
    contentMatchSnippets.clear();

    List<File> results;
    if (term.isEmpty() || (mode == SearchMode.FIND_IN_FILES && term.length() < MIN_CONTENT_QUERY_LENGTH)) {
      results = allFiles.size() > MAX_RECENT_RESULTS ? allFiles.subList(0, MAX_RECENT_RESULTS) : allFiles;
    }
    else if (mode == SearchMode.FIND_IN_FILES) {
      results = searchFileContents(term);
    }
    else {
      results = new ArrayList<>();
      for (File file : allFiles) {
        if (file.getName().toLowerCase().contains(term)) {
          results.add(file);
          if (results.size() >= MAX_SEARCH_RESULTS) {
            break;
          }
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

  private List<File> searchFileContents(@NonNull String term) {
    List<File> results = new ArrayList<>();
    for (File file : allFiles) {
      if (file.length() == 0 || file.length() > MAX_CONTENT_SEARCH_FILE_SIZE) {
        continue;
      }
      String snippet = findMatchingSnippet(file, term);
      if (snippet != null) {
        results.add(file);
        contentMatchSnippets.put(file, snippet);
        if (results.size() >= MAX_SEARCH_RESULTS) {
          break;
        }
      }
    }
    return results;
  }

  private String findMatchingSnippet(@NonNull File file, @NonNull String term) {
    try {
      for (String line : Files.readAllLines(file.toPath(), StandardCharsets.UTF_8)) {
        if (line.toLowerCase().contains(term)) {
          return truncateSnippet(line.trim());
        }
      }
    }
    catch (IOException | RuntimeException e) {
      // Not a readable text file (binary content, encoding issue, ...) - skip it.
    }
    return null;
  }

  private String truncateSnippet(@NonNull String line) {
    if (line.length() <= MAX_SNIPPET_LENGTH) {
      return line;
    }
    return line.substring(0, MAX_SNIPPET_LENGTH) + "…";
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

  private void updateFooter(File file) {
    footerPathLabel.setText(file == null ? "" : relativePath(file));
  }

  private void openSelected() {
    File file = resultsList.getSelectionModel().getSelectedItem();
    if (file == null) {
      return;
    }

    ProjectItem item = itemsByFile.get(file);
    if (item != null && item.getModel() != null) {
      project.getSettings().getUISettings().addOpenedFile(item.getPath());
      project.getSettings().getUISettings().save();
      StudioEventManager.getInstance().fireModelOpenEvent(item);
    }
    stage.close();
  }

  @Override
  public void onDialogCancel() {
    stage.close();
  }

  private String buildSecondaryText(@NonNull File file) {
    File parent = file.getParentFile();
    String path = parent == null ? "" : relativePath(parent);
    String snippet = contentMatchSnippets.get(file);
    if (snippet == null) {
      return path;
    }
    return path.isEmpty() ? snippet : path + "  —  " + snippet;
  }

  private String relativePath(@NonNull File file) {
    Path relative = project.getFolder().toPath().toAbsolutePath().relativize(file.toPath().toAbsolutePath());
    return relative.toString();
  }

  private class SearchResultCell extends ListCell<File> {
    private final Label nameLabel = new Label();
    private final Label parentPathLabel = new Label();
    private final HBox row;

    SearchResultCell() {
      nameLabel.getStyleClass().add("search-result-name");
      parentPathLabel.getStyleClass().add("search-result-path");
      row = new HBox(8, new Label(), nameLabel, parentPathLabel);
      row.setAlignment(Pos.CENTER_LEFT);
    }

    @Override
    protected void updateItem(File file, boolean empty) {
      super.updateItem(file, empty);
      if (empty || file == null) {
        setGraphic(null);
        return;
      }

      nameLabel.setText(file.getName());
      parentPathLabel.setText(buildSecondaryText(file));
      row.getChildren().set(0, createResultIcon(itemsByFile.get(file)));
      setGraphic(row);
    }
  }

  private Node createResultIcon(ProjectItem item) {
    if (item != null && item.getModel() != null) {
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
