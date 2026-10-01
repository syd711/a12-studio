package de.a12.studio.ui.versioncontrol;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * @param branch      short branch name, or the abbreviated commit id when {@code detached}
 * @param detached    whether HEAD is detached (not on a local branch)
 * @param aheadCount  number of local commits not on the upstream branch; {@code null} if the
 *                    branch has no upstream or HEAD is detached
 * @param behindCount number of upstream commits not on the local branch (as of the last fetch);
 *                    {@code null} whenever {@code aheadCount} is
 * @param hasRemote   whether the repository has at least one remote to push to
 */
public record GitBranchStatus(@NonNull String branch, boolean detached, @Nullable Integer aheadCount,
                              @Nullable Integer behindCount, boolean hasRemote) {

  /**
   * Whether the Push button has something to do: commits the upstream lacks, or - for a branch that was never
   * pushed - the branch itself, as long as there is a remote. With {@code force}, an upstream that is only
   * <em>ahead</em> of the local branch (e.g. after a local reset) also counts, since a force push rewinds it.
   */
  public boolean canPush(boolean force) {
    if (detached || !hasRemote) {
      return false;
    }
    if (aheadCount == null) {
      return true;
    }
    return aheadCount > 0 || (force && behindCount != null && behindCount > 0);
  }
}
