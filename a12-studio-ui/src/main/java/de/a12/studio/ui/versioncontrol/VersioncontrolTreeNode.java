package de.a12.studio.ui.versioncontrol;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * A node in the Versioncontrol panel's changes tree: either a folder used purely for structure,
 * or a leaf wrapping a single {@link GitChangedFile}.
 */
public class VersioncontrolTreeNode {

  private final String relativePath;
  private final String displayName;
  private final boolean folder;
  private final GitChangedFile changedFile;

  private VersioncontrolTreeNode(String relativePath, String displayName, boolean folder, @Nullable GitChangedFile changedFile) {
    this.relativePath = relativePath;
    this.displayName = displayName;
    this.folder = folder;
    this.changedFile = changedFile;
  }

  @NonNull
  public static VersioncontrolTreeNode folder(@NonNull String relativePath, @NonNull String displayName) {
    return new VersioncontrolTreeNode(relativePath, displayName, true, null);
  }

  @NonNull
  public static VersioncontrolTreeNode leaf(@NonNull GitChangedFile changedFile) {
    return new VersioncontrolTreeNode(changedFile.relativePath(), changedFile.file().getName(), false, changedFile);
  }

  /** Key used for {@code checkedByPath}; {@code ""} for the tree root. */
  @NonNull
  public String getRelativePath() {
    return relativePath;
  }

  @NonNull
  public String getDisplayName() {
    return displayName;
  }

  public boolean isFolder() {
    return folder;
  }

  @Nullable
  public GitChangedFile getChangedFile() {
    return changedFile;
  }
}
