package de.a12.studio.ui.versioncontrol;

/**
 * Simplified git change classification for a single project file, collapsing JGit's
 * staged/unstaged distinction (e.g. {@code getAdded()} vs {@code getUntracked()}) since the
 * Versioncontrol panel always commits/reverts the full current working-tree state of a path.
 */
public enum ChangeStatus {
  /** Never committed - covers JGit's staged-new ({@code getAdded()}) and untracked files. */
  NEW,
  /** Tracked file with content changes, staged or unstaged. */
  MODIFIED,
  /** Tracked file removed from the working tree, staged or unstaged. */
  DELETED,
  /** Unresolved merge conflict. */
  CONFLICTING
}
