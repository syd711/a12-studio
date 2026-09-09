package de.a12.studio.ui.bookmarks;

import de.a12.studio.ui.events.BookmarksChangedEvent;
import de.a12.studio.ui.events.StudioEventListener;
import de.a12.studio.ui.events.StudioEventManager;
import de.a12.studio.ui.util.StudioBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import org.jspecify.annotations.NonNull;

import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.function.Consumer;

public class BookmarksPanelController implements Initializable, StudioEventListener {

  @FXML
  private ListView<Bookmark> bookmarkList;

  private Consumer<Bookmark> onOpenBookmark;

  public void setOnOpenBookmark(Consumer<Bookmark> handler) {
    this.onOpenBookmark = handler;
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    bookmarkList.setCellFactory(lv -> new BookmarkCell());
    bookmarkList.setOnMouseClicked(event -> {
      if (event.getClickCount() == 2) {
        Bookmark selected = bookmarkList.getSelectionModel().getSelectedItem();
        if (selected != null && onOpenBookmark != null) {
          onOpenBookmark.accept(selected);
        }
      }
    });
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
    BookmarkService.getInstance().delete(selected);
  }

  // -----------------------------------------------------------------------
  // Cell
  // -----------------------------------------------------------------------

  private static class BookmarkCell extends ListCell<Bookmark> {

    private final Text nameText = new Text();
    private final Text pathText = new Text();
    private final TextFlow flow;

    BookmarkCell() {
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
      } else {
        nameText.setText(item.getDisplayName());
        pathText.setText(item.getPath());
        setGraphic(new VBox(flow));
        setText(null);
      }
    }
  }
}
