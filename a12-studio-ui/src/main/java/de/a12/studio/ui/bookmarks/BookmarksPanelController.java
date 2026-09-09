package de.a12.studio.ui.bookmarks;

import de.a12.studio.ui.events.BookmarksChangedEvent;
import de.a12.studio.ui.events.StudioEventListener;
import de.a12.studio.ui.events.StudioEventManager;
import de.a12.studio.ui.util.Icons;
import de.a12.studio.ui.util.StudioBundle;
import de.a12.studio.ui.util.WidgetFactory;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.KeyCode;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import org.jspecify.annotations.NonNull;
import org.kordamp.ikonli.javafx.FontIcon;

import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.function.Consumer;

public class BookmarksPanelController implements Initializable, StudioEventListener {

  @FXML
  private ListView<Bookmark> bookmarkList;

  @FXML
  private Button renameButton;

  @FXML
  private Button deleteButton;

  private Consumer<Bookmark> onOpenBookmark;
  private Runnable collapseProjectViewCallback;

  public void setOnOpenBookmark(Consumer<Bookmark> handler) {
    this.onOpenBookmark = handler;
  }

  public void setCollapseProjectViewCallback(Runnable callback) {
    this.collapseProjectViewCallback = callback;
  }

  @FXML
  private void onCollapseProjectView() {
    if (collapseProjectViewCallback != null) {
      collapseProjectViewCallback.run();
    }
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    bookmarkList.setCellFactory(lv -> new BookmarkCell(this::openBookmark, this::deleteBookmark));
    bookmarkList.setOnMouseClicked(event -> {
      if (event.getClickCount() == 2) {
        Bookmark selected = bookmarkList.getSelectionModel().getSelectedItem();
        if (selected != null && onOpenBookmark != null) {
          onOpenBookmark.accept(selected);
        }
      }
    });
    bookmarkList.setOnKeyPressed(event -> {
      if (event.getCode() == KeyCode.DELETE) {
        onDelete();
      }
    });
    renameButton.disableProperty().bind(bookmarkList.getSelectionModel().selectedItemProperty().isNull());
    deleteButton.disableProperty().bind(bookmarkList.getSelectionModel().selectedItemProperty().isNull());
    refresh();
    StudioEventManager.getInstance().addListener(this);
  }

  @Override
  public void bookmarksChanged(@NonNull BookmarksChangedEvent event) {
    refresh();
  }

  public void refresh() {
    List<Bookmark> bookmarks = BookmarkService.getInstance().getBookmarks();
    bookmarkList.getItems().setAll(bookmarks);
  }

  @FXML
  private void onRename() {
    Bookmark selected = bookmarkList.getSelectionModel().getSelectedItem();
    if (selected == null) return;

    TextInputDialog dialog = new TextInputDialog(selected.getDisplayName());
    dialog.setTitle(StudioBundle.get("bookmark_rename"));
    dialog.setHeaderText(null);
    dialog.setContentText(StudioBundle.get("bookmark_rename_prompt"));
    Optional<String> result = dialog.showAndWait();
    result.filter(name -> !name.isBlank()).ifPresent(name -> {
      BookmarkService.getInstance().rename(selected, name.trim());
    });
  }

  @FXML
  private void onDelete() {
    Bookmark selected = bookmarkList.getSelectionModel().getSelectedItem();
    if (selected == null) return;

    Optional<ButtonType> result = WidgetFactory.showConfirmation(getStage(),
        StudioBundle.get("confirm_delete_bookmark", selected.getDisplayName()), null, null, StudioBundle.get("bookmark_delete"));
    if (result.isPresent() && result.get() == ButtonType.OK) {
      deleteBookmark(selected);
    }
  }

  private void deleteBookmark(@NonNull Bookmark bookmark) {
    BookmarkService.getInstance().delete(bookmark);
  }

  private void openBookmark(@NonNull Bookmark bookmark) {
    if (onOpenBookmark != null) {
      onOpenBookmark.accept(bookmark);
    }
  }

  private Stage getStage() {
    return (Stage) bookmarkList.getScene().getWindow();
  }

  // -----------------------------------------------------------------------
  // Cell
  // -----------------------------------------------------------------------

  private static class BookmarkCell extends ListCell<Bookmark> {

    private final Text nameText = new Text();
    private final Text pathText = new Text();
    private final TextFlow flow;
    private final Consumer<Bookmark> onOpen;
    private final Consumer<Bookmark> onDelete;

    BookmarkCell(Consumer<Bookmark> onOpen, Consumer<Bookmark> onDelete) {
      this.onOpen = onOpen;
      this.onDelete = onDelete;
      nameText.getStyleClass().add("bookmark-cell-name");
      pathText.getStyleClass().add("bookmark-cell-path");
      flow = new TextFlow(nameText, new Text("\n"), pathText);
      flow.setLineSpacing(1);
    }

    @Override
    protected void updateItem(Bookmark item, boolean empty) {
      super.updateItem(item, empty);
      if (empty || item == null) {
        setGraphic(null);
        setText(null);
        setContextMenu(null);
      } else {
        nameText.setText(item.getDisplayName());
        pathText.setText(item.getPath());
        setGraphic(new VBox(flow));
        setText(null);
        setContextMenu(createContextMenu(item));
      }
    }

    private ContextMenu createContextMenu(Bookmark bookmark) {
      FontIcon openIcon = WidgetFactory.createIcon(Icons.OPEN_IN_NEW);
      openIcon.getStyleClass().add("menu-icon");
      MenuItem openItem = new MenuItem(StudioBundle.get("open"), openIcon);
      openItem.setOnAction(event -> onOpen.accept(bookmark));

      FontIcon deleteIcon = WidgetFactory.createIcon(Icons.TRASH);
      deleteIcon.getStyleClass().add("menu-icon");
      MenuItem deleteItem = new MenuItem(StudioBundle.get("delete"), deleteIcon);
      deleteItem.setOnAction(event -> onDelete.accept(bookmark));

      ContextMenu contextMenu = new ContextMenu();
      contextMenu.getItems().addAll(openItem, deleteItem);
      return contextMenu;
    }
  }
}
