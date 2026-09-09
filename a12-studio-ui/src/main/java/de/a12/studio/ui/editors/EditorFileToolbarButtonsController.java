package de.a12.studio.ui.editors;

import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.ui.bookmarks.BookmarkService;
import de.a12.studio.ui.events.BookmarksChangedEvent;
import de.a12.studio.ui.events.StudioEventListener;
import de.a12.studio.ui.events.StudioEventManager;
import de.a12.studio.ui.events.TabSelectionChangedEvent;
import de.a12.studio.ui.util.SystemUtil;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ToggleButton;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.function.Supplier;

/**
 * Controller for the reusable "Edit File" / "Open Model Folder" / "Bookmark" toolbar buttons,
 * included via {@code fx:include} in every editor toolbar.
 *
 * <p>After loading, call {@link #setFileSupplier(Supplier)} so the component knows
 * which file to act on — typically {@code () -> projectItem.getFile()}.
 */
@Slf4j
public class EditorFileToolbarButtonsController implements Initializable, StudioEventListener {

  @FXML
  private ToggleButton bookmarkButton;

  private Supplier<File> fileSupplier;
  private Supplier<ProjectItem> projectItemSupplier;

  /**
   * Provide the file this component should open/edit.
   * Call this after the owning controller's {@code projectItem} is available.
   */
  public void setFileSupplier(Supplier<File> fileSupplier) {
    this.fileSupplier = fileSupplier;
  }

  /**
   * Provide the current ProjectItem so the bookmark button can reflect and toggle bookmark state.
   */
  public void setProjectItemSupplier(Supplier<ProjectItem> projectItemSupplier) {
    this.projectItemSupplier = projectItemSupplier;
    updateBookmarkButton();
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    StudioEventManager.getInstance().addListener(this);
  }

  @Override
  public void bookmarksChanged(@NonNull BookmarksChangedEvent event) {
    updateBookmarkButton();
  }

  @Override
  public void tabSelectionChanged(@NonNull TabSelectionChangedEvent event) {
    updateBookmarkButton();
  }

  private void updateBookmarkButton() {
    if (bookmarkButton == null) return;
    ProjectItem item = projectItemSupplier != null ? projectItemSupplier.get() : null;
    boolean isBookmarked = item != null && BookmarkService.getInstance().isBookmarked(item);
    bookmarkButton.setSelected(isBookmarked);
    // Swap icon to filled/outline depending on state
    FontIcon icon = (FontIcon) bookmarkButton.getGraphic();
    if (icon != null) {
      icon.setIconLiteral(isBookmarked ? "mdi2b-bookmark" : "mdi2b-bookmark-outline");
    }
  }

  @FXML
  private void onToggleBookmark(ActionEvent e) {
    if (projectItemSupplier == null) return;
    ProjectItem item = projectItemSupplier.get();
    if (item != null) {
      BookmarkService.getInstance().toggle(item);
      // bookmarksChanged event fires via BookmarkService → updateBookmarkButton called
    }
  }

  @FXML
  private void onFileEdit(ActionEvent e) {
    if (fileSupplier != null) {
      SystemUtil.editFile(fileSupplier.get());
    }
  }

  @FXML
  private void onFileOpen(ActionEvent e) {
    if (fileSupplier != null) {
      SystemUtil.openFile(fileSupplier.get());
    }
  }
}
