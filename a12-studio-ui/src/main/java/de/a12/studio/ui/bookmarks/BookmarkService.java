package de.a12.studio.ui.bookmarks;

import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.ui.events.StudioEventManager;
import de.a12.studio.ui.util.localsettings.LocalUISettings;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Singleton that manages the list of bookmarked project items.
 * Bookmarks are persisted via {@link LocalUISettings} as JSON.
 */
public class BookmarkService {

  private static final String KEY = "bookmarks";

  private static final BookmarkService INSTANCE = new BookmarkService();

  public static BookmarkService getInstance() {
    return INSTANCE;
  }

  private BookmarkService() {}

  @NonNull
  public List<Bookmark> getBookmarks() {
    List<Bookmark> bookmarks = LocalUISettings.getJsonProperty(KEY, List.class, null);
    if (bookmarks == null) {
      return new ArrayList<>();
    }
    // Jackson deserialises generic List<Bookmark> as List<LinkedHashMap> — re-map manually.
    List<Bookmark> result = new ArrayList<>();
    for (Object entry : bookmarks) {
      if (entry instanceof Bookmark b) {
        result.add(b);
      } else if (entry instanceof java.util.Map<?, ?> map) {
        Bookmark b = new Bookmark(
            (String) map.get("path"),
            (String) map.get("displayName")
        );
        if (b.getPath() != null) {
          result.add(b);
        }
      }
    }
    return result;
  }

  public boolean isBookmarked(@NonNull ProjectItem item) {
    String path = item.getPath();
    return getBookmarks().stream().anyMatch(b -> path.equals(b.getPath()));
  }

  public void add(@NonNull ProjectItem item) {
    List<Bookmark> bookmarks = getBookmarks();
    String path = item.getPath();
    if (bookmarks.stream().noneMatch(b -> path.equals(b.getPath()))) {
      bookmarks.add(new Bookmark(path, item.getFile().getName()));
      save(bookmarks);
    }
  }

  public void remove(@NonNull ProjectItem item) {
    List<Bookmark> bookmarks = getBookmarks();
    String path = item.getPath();
    bookmarks.removeIf(b -> path.equals(b.getPath()));
    save(bookmarks);
  }

  public void toggle(@NonNull ProjectItem item) {
    if (isBookmarked(item)) {
      remove(item);
    } else {
      add(item);
    }
  }

  public void rename(@NonNull Bookmark bookmark, @NonNull String newName) {
    List<Bookmark> bookmarks = getBookmarks();
    for (Bookmark b : bookmarks) {
      if (b.getPath().equals(bookmark.getPath())) {
        b.setDisplayName(newName);
        break;
      }
    }
    save(bookmarks);
  }

  public void delete(@NonNull Bookmark bookmark) {
    List<Bookmark> bookmarks = getBookmarks();
    bookmarks.removeIf(b -> b.getPath().equals(bookmark.getPath()));
    save(bookmarks);
  }

  private void save(@NonNull List<Bookmark> bookmarks) {
    LocalUISettings.saveJsonProperty(KEY, bookmarks);
    StudioEventManager.getInstance().fireBookmarksChangedEvent();
  }
}
