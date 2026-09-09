package de.a12.studio.ui.bookmarks;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Bookmark {

  private String path;
  private String displayName;

  /** Required by Jackson. */
  public Bookmark() {}

  public Bookmark(String path, String displayName) {
    this.path = path;
    this.displayName = displayName;
  }

  public String getPath() { return path; }
  public void setPath(String path) { this.path = path; }

  public String getDisplayName() { return displayName; }
  public void setDisplayName(String displayName) { this.displayName = displayName; }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Bookmark b)) return false;
    return path != null && path.equals(b.path);
  }

  @Override
  public int hashCode() { return path == null ? 0 : path.hashCode(); }
}
