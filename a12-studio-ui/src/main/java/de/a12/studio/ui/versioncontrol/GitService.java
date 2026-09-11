package de.a12.studio.ui.versioncontrol;

import de.a12.studio.models.auth.AuthFileType;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.CheckoutCommand;
import org.eclipse.jgit.api.CommitCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.jspecify.annotations.NonNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Wraps a single JGit {@link Git}/{@link Repository} instance for the currently opened project,
 * scoped to the project's model files (JSON models, excluding {@code settings.json} and auth
 * files - see {@link #isModelFileName}).
 */
@Slf4j
public class GitService implements AutoCloseable {

  private final Git git;
  private final Path repoRootPath;

  private GitService(@NonNull Git git) {
    this.git = git;
    this.repoRootPath = git.getRepository().getWorkTree().toPath();
  }

  /**
   * Cheap check for whether {@code folder} sits inside (or at) a git working copy, without
   * opening a {@link Repository}. Used to decide whether the Versioncontrol toggle should be
   * shown at all.
   */
  public static boolean isGitRepository(@NonNull File folder) {
    return new FileRepositoryBuilder().findGitDir(folder).getGitDir() != null;
  }

  /**
   * Opens a {@link GitService} for the git repository containing {@code projectFolder}, walking
   * up from it (JGit's own {@link FileRepositoryBuilder#findGitDir}). Returns empty if
   * {@code projectFolder} is not inside a git working copy - {@code findGitDir} leaves the
   * builder's git-dir unset in that case, and {@link FileRepositoryBuilder#build()} must not be
   * called then, since it silently falls back to a current-working-directory-based guess instead
   * of failing.
   */
  @NonNull
  public static Optional<GitService> openForProjectFolder(@NonNull File projectFolder) {
    FileRepositoryBuilder builder = new FileRepositoryBuilder().findGitDir(projectFolder).readEnvironment();
    if (builder.getGitDir() == null) {
      return Optional.empty();
    }
    try {
      return Optional.of(new GitService(new Git(builder.build())));
    }
    catch (IOException e) {
      log.warn("Failed to open git repository for '{}': {}", projectFolder, e.getMessage(), e);
      return Optional.empty();
    }
  }

  /**
   * Returns the project's model files with outstanding git changes, restricted to files under
   * {@code projectFolder} (files elsewhere in the repository are not part of the opened project).
   */
  @NonNull
  public List<GitChangedFile> getChangedProjectFiles(@NonNull File projectFolder) throws GitAPIException {
    Status status = git.status().call();
    Path projectPath = projectFolder.toPath();

    Map<String, ChangeStatus> statusByRepoPath = classifyStatus(status);

    List<GitChangedFile> result = new ArrayList<>();
    for (Map.Entry<String, ChangeStatus> entry : statusByRepoPath.entrySet()) {
      Path absolute = repoRootPath.resolve(entry.getKey());
      if (!absolute.startsWith(projectPath)) {
        continue;
      }
      File file = absolute.toFile();
      if (!isModelFileName(file.getName())) {
        continue;
      }
      String relativePath = projectPath.relativize(absolute).toString().replace('\\', '/');
      result.add(new GitChangedFile(file, relativePath, entry.getValue()));
    }
    return result;
  }

  /**
   * Returns {@code file}'s outstanding git change, if any - the single-file counterpart to {@link
   * #getChangedProjectFiles}, used by the per-editor toolbar's Commit/Revert buttons to decide
   * whether the currently open file has anything to act on. The status query is scoped to just
   * {@code file}'s repo-relative path, so it stays cheap to call on every tab switch/save, unlike
   * a full {@link #getChangedProjectFiles} scan.
   */
  @NonNull
  public Optional<GitChangedFile> getChangedFile(@NonNull File projectFolder, @NonNull File file) throws GitAPIException {
    if (!isModelFileName(file.getName())) {
      return Optional.empty();
    }
    String repoRelativePath = toRepoRelativePath(file);
    Status status = git.status().addPath(repoRelativePath).call();
    ChangeStatus changeStatus = classifyStatus(status).get(repoRelativePath);
    if (changeStatus == null) {
      return Optional.empty();
    }
    String relativePath = projectFolder.toPath().relativize(file.toPath()).toString().replace('\\', '/');
    return Optional.of(new GitChangedFile(file, relativePath, changeStatus));
  }

  @NonNull
  private Map<String, ChangeStatus> classifyStatus(@NonNull Status status) {
    Map<String, ChangeStatus> statusByRepoPath = new LinkedHashMap<>();
    collect(statusByRepoPath, status.getAdded(), ChangeStatus.NEW);
    collect(statusByRepoPath, status.getUntracked(), ChangeStatus.NEW);
    collect(statusByRepoPath, status.getModified(), ChangeStatus.MODIFIED);
    collect(statusByRepoPath, status.getChanged(), ChangeStatus.MODIFIED);
    collect(statusByRepoPath, status.getRemoved(), ChangeStatus.DELETED);
    collect(statusByRepoPath, status.getMissing(), ChangeStatus.DELETED);
    collect(statusByRepoPath, status.getConflicting(), ChangeStatus.CONFLICTING);
    return statusByRepoPath;
  }

  /** Adds each path not already present, in priority order: CONFLICTING > DELETED > MODIFIED > NEW. */
  private void collect(Map<String, ChangeStatus> target, Set<String> repoRelativePaths, ChangeStatus status) {
    for (String path : repoRelativePaths) {
      ChangeStatus existing = target.get(path);
      if (existing == null || priority(status) > priority(existing)) {
        target.put(path, status);
      }
    }
  }

  private int priority(ChangeStatus status) {
    return switch (status) {
      case CONFLICTING -> 3;
      case DELETED -> 2;
      case MODIFIED -> 1;
      case NEW -> 0;
    };
  }

  private boolean isModelFileName(@NonNull String name) {
    if ("settings.json".equals(name)) {
      return false;
    }
    if (AuthFileType.fromFileName(name) != null) {
      return false;
    }
    return name.endsWith(".json");
  }

  /**
   * Commits the current working-tree state of exactly {@code files} (staged or not, tracked or
   * new, present or deleted), leaving the rest of the index untouched.
   */
  public void stageAndCommit(@NonNull List<GitChangedFile> files, @NonNull String message) throws GitAPIException {
    if (files.isEmpty()) {
      return;
    }
    CommitCommand commit = git.commit().setMessage(message);
    for (GitChangedFile file : files) {
      commit.setOnly(toRepoRelativePath(file.file()));
    }
    commit.call();
  }

  /**
   * Reverts {@code files}: a {@link ChangeStatus#NEW} file is deleted from disk (it was never
   * committed, so there is no HEAD version to restore); every other file is checked out from
   * HEAD, discarding both staged and unstaged changes.
   */
  public void revert(@NonNull List<GitChangedFile> files) throws GitAPIException {
    CheckoutCommand checkout = null;
    for (GitChangedFile file : files) {
      if (file.status() == ChangeStatus.NEW) {
        if (file.file().exists() && !file.file().delete()) {
          log.warn("Failed to delete '{}' while reverting", file.file());
        }
        continue;
      }
      if (checkout == null) {
        checkout = git.checkout().setStartPoint("HEAD");
      }
      checkout.addPath(toRepoRelativePath(file.file()));
    }
    if (checkout != null) {
      checkout.call();
    }
  }

  @NonNull
  private String toRepoRelativePath(@NonNull File file) {
    return repoRootPath.relativize(file.toPath()).toString().replace('\\', '/');
  }

  @Override
  public void close() {
    git.getRepository().close();
  }
}
