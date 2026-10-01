package de.a12.studio.ui.versioncontrol;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * @param branch     short branch name, or the abbreviated commit id when {@code detached}
 * @param detached   whether HEAD is detached (not on a local branch)
 * @param aheadCount number of local commits not on the upstream branch; {@code null} if the
 *                   branch has no upstream or HEAD is detached
 */
public record GitBranchStatus(@NonNull String branch, boolean detached, @Nullable Integer aheadCount) {
}
