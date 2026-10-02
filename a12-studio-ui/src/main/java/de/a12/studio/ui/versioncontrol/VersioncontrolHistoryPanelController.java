package de.a12.studio.ui.versioncontrol;

import de.a12.studio.ui.util.Icons;
import de.a12.studio.ui.util.StudioBundle;
import de.a12.studio.ui.util.WidgetFactory;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;

import java.io.File;
import java.net.URL;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.BiConsumer;

/** Commit history table with a toolbar showing (and allowing to clear) the file the history is scoped to. */
public class VersioncontrolHistoryPanelController implements Initializable {

  private static final DateTimeFormatter HISTORY_DATE_FORMAT = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT);

  @FXML
  private Label historyScopeLabel;

  @FXML
  private Button clearHistoryScopeButton;

  @FXML
  private Button restoreVersionButton;

  @FXML
  private TableView<GitCommitInfo> historyTable;

  @FXML
  private TableColumn<GitCommitInfo, String> historyMessageColumn;

  @FXML
  private TableColumn<GitCommitInfo, String> historyAuthorColumn;

  @FXML
  private TableColumn<GitCommitInfo, String> historyDateColumn;

  private Runnable onClearScope;
  private Runnable onCollapse;
  private BiConsumer<File, GitCommitInfo> onRestore;
  private File scope;

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    historyMessageColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().message()));
    historyAuthorColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(data.getValue().author()));
    historyDateColumn.setCellValueFactory(data -> new ReadOnlyStringWrapper(
        HISTORY_DATE_FORMAT.format(data.getValue().date().atZone(ZoneId.systemDefault()))));
    historyTable.setPlaceholder(new Label(StudioBundle.get("versioncontrol_history_empty")));
    historyTable.setRowFactory(table -> new TableRow<>() {
      private final MenuItem restore = new MenuItem(StudioBundle.get("versioncontrol_history.restore"));
      private final ContextMenu menu = new ContextMenu(restore);

      @Override
      protected void updateItem(GitCommitInfo commit, boolean empty) {
        super.updateItem(commit, empty);
        setTooltip(empty || commit == null ? null
            : WidgetFactory.createTooltip(commit.shortId() + " - " + commit.author() + "\n\n" + commit.fullMessage()));
        // Restoring only makes sense for a single file; the whole-project history has no "one version" to put back.
        // (a scoped file that no longer exists on disk - deleted - can still be brought back)
        boolean restorable = scope != null && !scope.isDirectory();
        setContextMenu(empty || commit == null || !restorable ? null : menu);
      }

      {
        restore.setGraphic(WidgetFactory.createIcon(Icons.UNDO));
        restore.setOnAction(e -> {
          GitCommitInfo commit = getItem();
          if (commit != null && onRestore != null && scope != null) {
            onRestore.accept(scope, commit);
          }
        });
      }
    });
    historyTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> updateRestoreButton());
    setScope(null);
  }

  /** Restoring needs one chosen commit and a single scoped file (see the row context menu). */
  private void updateRestoreButton() {
    restoreVersionButton.setDisable(historyTable.getSelectionModel().getSelectedItem() == null
        || scope == null || scope.isDirectory());
  }

  @FXML
  private void onRestoreVersion() {
    GitCommitInfo commit = historyTable.getSelectionModel().getSelectedItem();
    if (commit != null && onRestore != null && scope != null) {
      onRestore.accept(scope, commit);
    }
  }

  /** Called when the user clears the file scope via the toolbar button. */
  public void setOnClearScope(Runnable onClearScope) {
    this.onClearScope = onClearScope;
  }

  /** Called with the scoped file and the chosen commit when the user picks "restore this version". */
  public void setOnRestore(BiConsumer<File, GitCommitInfo> onRestore) {
    this.onRestore = onRestore;
  }

  public void setItems(List<GitCommitInfo> commits) {
    historyTable.getItems().setAll(commits);
  }

  public void clear() {
    historyTable.getItems().clear();
  }

  /** @param scope file whose history is shown; {@code null} for the whole project. */
  public void setScope(File scope) {
    this.scope = scope;
    boolean scoped = scope != null;
    clearHistoryScopeButton.setVisible(scoped);
    clearHistoryScopeButton.setManaged(scoped);
    historyScopeLabel.setText(scoped ? scope.getName() : "");
    historyScopeLabel.setTooltip(scoped ? WidgetFactory.createTooltip(scope.getAbsolutePath()) : null);
    historyTable.setPlaceholder(new Label(StudioBundle.get(
        scoped ? "versioncontrol_history_empty" : "versioncontrol_history_no_file")));
    updateRestoreButton();
  }

  /** Called when the user hides the history via the toolbar's collapse button. */
  public void setOnCollapse(Runnable onCollapse) {
    this.onCollapse = onCollapse;
  }

  @FXML
  private void onCollapseHistory() {
    if (onCollapse != null) {
      onCollapse.run();
    }
  }

  @FXML
  private void onClearHistoryScope() {
    if (onClearScope != null) {
      onClearScope.run();
    }
  }
}
