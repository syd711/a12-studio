package de.a12.studio.ui.versioncontrol;

import de.a12.studio.models.auth.AuthFileType;
import de.a12.studio.ui.util.StudioBundle;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.CheckoutCommand;
import org.eclipse.jgit.api.CommitCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.LogCommand;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.BranchConfig;
import org.eclipse.jgit.lib.BranchTrackingStatus;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryState;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Wraps a single JGit {@link Git}/{@link Repository} instance for the currently opened project,
 * scoped to the project's model files (JSON models, excluding {@code settings.json} and auth
 * files - see {@link #isModelFileName}).
 */
@Slf4j
public class GitService implements AutoCloseable {

  private static final long PUSH_TIMEOUT_MINUTES = 5;
  private static final String DEFAULT_REMOTE = "origin";

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
   * Returns every file with outstanding git changes that lies in the repository but <em>outside</em>
   * {@code projectFolder} (any file type - not restricted to model files). Their
   * {@link GitChangedFile#relativePath()} is relative to the project folder (so it starts with
   * {@code ../} and cannot collide with a project file's path); use {@link #repositoryRelativePath}
   * for the path relative to the repository root.
   */
  @NonNull
  public List<GitChangedFile> getChangedFilesOutsideProject(@NonNull File projectFolder) throws GitAPIException {
    Status status = git.status().call();
    Path projectPath = projectFolder.toPath();

    List<GitChangedFile> result = new ArrayList<>();
    for (Map.Entry<String, ChangeStatus> entry : classifyStatus(status).entrySet()) {
      Path absolute = repoRootPath.resolve(entry.getKey());
      if (absolute.startsWith(projectPath)) {
        continue;
      }
      String relativePath = projectPath.relativize(absolute).toString().replace('\\', '/');
      result.add(new GitChangedFile(absolute.toFile(), relativePath, entry.getValue()));
    }
    return result;
  }

  /** {@code file}'s path relative to the repository root, forward-slash normalized. */
  @NonNull
  public String repositoryRelativePath(@NonNull File file) {
    return toRepoRelativePath(file);
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

  /**
   * Returns the checked-out branch and how many commits it is ahead of/behind its upstream (remote
   * tracking branch, as of the last fetch). {@link GitBranchStatus#aheadCount()} is {@code null}
   * when the branch has no upstream configured or HEAD is detached.
   */
  @NonNull
  public GitBranchStatus getBranchStatus() throws IOException {
    Repository repository = git.getRepository();
    String fullBranch = repository.getFullBranch();
    String branch = repository.getBranch();
    boolean hasRemote = !repository.getRemoteNames().isEmpty();
    if (fullBranch == null || branch == null) {
      return new GitBranchStatus("", true, null, null, hasRemote);
    }
    if (!fullBranch.startsWith(Constants.R_HEADS)) {
      return new GitBranchStatus(branch.length() > 7 ? branch.substring(0, 7) : branch, true, null, null, hasRemote);
    }
    BranchTrackingStatus tracking = BranchTrackingStatus.of(repository, branch);
    return tracking == null
        ? new GitBranchStatus(branch, false, null, null, hasRemote)
        : new GitBranchStatus(branch, false, tracking.getAheadCount(), tracking.getBehindCount(), hasRemote);
  }

  /**
   * Pushes the checked-out branch to its upstream, or - if it has none yet - to the only remote (else
   * {@code origin}) under the same name, setting that as its upstream.
   * <p>
   * Runs the installed {@code git} executable rather than JGit's transport, so the user's own SSH keys, credential
   * manager and pre-push hooks apply exactly as on the command line (a12-studio ships only JGit core: no SSH
   * transport, no credential store). {@code GIT_TERMINAL_PROMPT=0} makes git fail instead of waiting for a password
   * on a console nobody sees. {@code force} uses {@code --force-with-lease}: it overwrites the remote branch, but
   * refuses if the remote moved since the last fetch, so nobody else's newer commits are silently dropped.
   */
  public void push(boolean force) throws GitAPIException {
    Repository repository = git.getRepository();
    List<String> command = new ArrayList<>(List.of("git", "push", "--porcelain"));
    try {
      String fullBranch = repository.getFullBranch();
      if (fullBranch == null || !fullBranch.startsWith(Constants.R_HEADS)) {
        throw new GitPushException(StudioBundle.get("versioncontrol_push_detached"));
      }
      String branch = repository.getBranch();
      Set<String> remotes = repository.getRemoteNames();
      if (remotes.isEmpty()) {
        throw new GitPushException(StudioBundle.get("versioncontrol_push_no_remote"));
      }
      BranchConfig branchConfig = new BranchConfig(repository.getConfig(), branch);
      String upstreamRemote = branchConfig.getRemote();
      String upstreamMerge = branchConfig.getMerge();
      if (force) {
        command.add("--force-with-lease");
      }
      if (upstreamRemote != null && upstreamMerge != null && !".".equals(upstreamRemote)) {
        command.add(upstreamRemote);
        command.add(Constants.R_HEADS + branch + ":" + upstreamMerge);
      }
      else {
        command.add("--set-upstream");
        command.add(remotes.size() == 1 ? remotes.iterator().next() : DEFAULT_REMOTE);
        command.add(branch);
      }
    }
    catch (IOException e) {
      throw new GitPushException(StudioBundle.get("versioncontrol_push_failed", e.getMessage()), e);
    }
    GitResult result = execGit(command);
    if (result.exitCode() != 0) {
      throw new GitPushException(describeFailure(result.output(), result.exitCode()));
    }
    log.info("git push: {}", result.output());
  }

  /** Output and exit code of a finished git process (stdout and stderr merged). */
  private record GitResult(int exitCode, String output) {
  }

  /** Runs {@code command} and returns git's output; a non-zero exit becomes an exception carrying that output. */
  private String runGit(List<String> command) throws GitPushException {
    GitResult result = execGit(command);
    if (result.exitCode() != 0) {
      String output = result.output();
      throw new GitPushException(output.isEmpty()
          ? StudioBundle.get("versioncontrol_git_failed", command.get(1), "exit code " + result.exitCode())
          : output);
    }
    log.info("git {}: {}", command.get(1), result.output());
    return result.output();
  }

  /**
   * Runs the installed git executable in the repository root. Only a failure to run git at all (missing executable,
   * timeout, interruption) throws; a non-zero exit is reported in the returned {@link GitResult}.
   * {@code GIT_EDITOR=true} keeps git from opening an editor for merge/rebase messages on a console nobody sees.
   */
  private GitResult execGit(List<String> command) throws GitPushException {
    String operation = command.size() > 1 ? command.get(1) : "git";
    ProcessBuilder builder = new ProcessBuilder(command).directory(repoRootPath.toFile()).redirectErrorStream(true);
    builder.environment().put("GIT_TERMINAL_PROMPT", "0");
    builder.environment().put("GIT_EDITOR", "true");
    Process process;
    try {
      process = builder.start();
    }
    catch (IOException e) {
      throw new GitPushException(StudioBundle.get("versioncontrol_push_git_missing", e.getMessage()), e);
    }
    // Drain git's output on its own thread so the timeout below still applies if git hangs (e.g. an SSH prompt),
    // and a chatty command can't block on a full pipe buffer.
    Process running = process;
    CompletableFuture<String> output = CompletableFuture.supplyAsync(() -> {
      try {
        return new String(running.getInputStream().readAllBytes(), StandardCharsets.UTF_8).trim();
      }
      catch (IOException e) {
        return "";
      }
    });
    try {
      process.getOutputStream().close();
      if (!process.waitFor(PUSH_TIMEOUT_MINUTES, TimeUnit.MINUTES)) {
        process.destroyForcibly();
        throw new GitPushException(StudioBundle.get("versioncontrol_git_timeout", operation, PUSH_TIMEOUT_MINUTES));
      }
      return new GitResult(process.exitValue(), output.get(10, TimeUnit.SECONDS));
    }
    catch (IOException | ExecutionException | TimeoutException e) {
      process.destroyForcibly();
      throw new GitPushException(StudioBundle.get("versioncontrol_git_failed", operation, e.getMessage()), e);
    }
    catch (InterruptedException e) {
      process.destroyForcibly();
      Thread.currentThread().interrupt();
      throw new GitPushException(StudioBundle.get("versioncontrol_git_failed", operation, "interrupted"), e);
    }
  }

  // -------------------------------------------------------------------------
  // Pull
  // -------------------------------------------------------------------------

  /** Whether {@code pull.rebase} is configured to rebase; this is what the Pull button does by default. */
  public boolean prefersRebase() {
    String value = git.getRepository().getConfig().getString("pull", null, "rebase");
    return value != null && !"false".equalsIgnoreCase(value.trim());
  }

  /** Whether git knows who to attribute commits to; merging, rebasing and stashing all create commits. */
  public boolean hasIdentity() {
    var config = git.getRepository().getConfig();
    String name = config.getString("user", null, "name");
    String email = config.getString("user", null, "email");
    return name != null && !name.isBlank() && email != null && !email.isBlank();
  }

  /** Writes {@code user.name}/{@code user.email} to the repository's config, or to the user's global config. */
  public void setIdentity(@NonNull String name, @NonNull String email, boolean global) throws GitAPIException {
    String scope = global ? "--global" : "--local";
    runGit(List.of("git", "config", scope, "user.name", name));
    runGit(List.of("git", "config", scope, "user.email", email));
  }

  /** Whether tracked files have uncommitted changes, which a rebase refuses to start with. */
  public boolean hasUncommittedChanges() throws GitAPIException {
    return git.status().call().hasUncommittedChanges();
  }

  /**
   * Whether {@code git pull} knows where to pull from: the branch has an upstream, or the single/default remote has a
   * branch of the same name. When neither holds there is nothing to pull.
   */
  public boolean canPull() throws IOException {
    return resolvePullTarget() != null;
  }

  /** {@code []} to pull from the configured upstream, {@code [remote, branch]} otherwise, {@code null} for neither. */
  private List<String> resolvePullTarget() throws IOException {
    Repository repository = git.getRepository();
    String fullBranch = repository.getFullBranch();
    if (fullBranch == null || !fullBranch.startsWith(Constants.R_HEADS)) {
      return null;
    }
    String branch = repository.getBranch();
    BranchConfig branchConfig = new BranchConfig(repository.getConfig(), branch);
    if (branchConfig.getRemote() != null && branchConfig.getMerge() != null && !".".equals(branchConfig.getRemote())) {
      return List.of();
    }
    Set<String> remotes = repository.getRemoteNames();
    if (remotes.isEmpty()) {
      return null;
    }
    String remote = remotes.size() == 1 ? remotes.iterator().next() : DEFAULT_REMOTE;
    return repository.exactRef(Constants.R_REMOTES + remote + "/" + branch) == null ? null : List.of(remote, branch);
  }

  /**
   * Fetches and integrates the upstream branch: rebasing local commits onto it, or merging it in. With
   * {@code autostash} uncommitted changes are stashed first and re-applied afterwards, which a rebase needs.
   * Conflicts leave the repository mid-operation (see {@link #isOperationInProgress()}); the exception carries git's
   * output.
   */
  public void pull(boolean rebase, boolean autostash) throws GitAPIException {
    List<String> target;
    try {
      Repository repository = git.getRepository();
      String fullBranch = repository.getFullBranch();
      if (fullBranch == null || !fullBranch.startsWith(Constants.R_HEADS)) {
        throw new GitPushException(StudioBundle.get("versioncontrol_pull_detached"));
      }
      if (repository.getRemoteNames().isEmpty()) {
        throw new GitPushException(StudioBundle.get("versioncontrol_pull_no_remote"));
      }
      target = resolvePullTarget();
      if (target == null) {
        throw new GitPushException(StudioBundle.get("versioncontrol_pull_no_upstream", repository.getBranch()));
      }
    }
    catch (IOException e) {
      throw new GitPushException(StudioBundle.get("versioncontrol_git_failed", "pull", e.getMessage()), e);
    }
    List<String> command = new ArrayList<>(List.of("git", "pull", rebase ? "--rebase" : "--no-rebase"));
    if (!rebase) {
      command.add("--no-edit");
    }
    if (autostash) {
      command.add("--autostash");
    }
    command.addAll(target);
    runGit(command);
  }

  /** Whether a merge or rebase was left unfinished, typically because of conflicts. */
  public boolean isOperationInProgress() {
    return git.getRepository().getRepositoryState() != RepositoryState.SAFE;
  }

  /** Abandons an unfinished merge or rebase, restoring the state from before the pull. */
  public void abortOperation() throws GitAPIException {
    RepositoryState state = git.getRepository().getRepositoryState();
    if (state.isRebasing()) {
      runGit(List.of("git", "rebase", "--abort"));
    }
    else if (state == RepositoryState.MERGING || state == RepositoryState.MERGING_RESOLVED) {
      runGit(List.of("git", "merge", "--abort"));
    }
  }

  // -------------------------------------------------------------------------
  // Stash
  // -------------------------------------------------------------------------

  /** Stash entries, newest first, as git describes them ({@code "On main: message"}). */
  @NonNull
  public List<String> getStashes() throws GitAPIException {
    List<String> result = new ArrayList<>();
    for (RevCommit commit : git.stashList().call()) {
      result.add(commit.getShortMessage());
    }
    return result;
  }

  /** Stashes all uncommitted changes of the repository, including new files, and restores a clean working tree. */
  public void stash(@Nullable String message) throws GitAPIException {
    List<String> command = new ArrayList<>(List.of("git", "stash", "push", "--include-untracked"));
    if (message != null && !message.isBlank()) {
      command.add("--message");
      command.add(message.trim());
    }
    runGit(command);
  }

  /** Applies the newest stash entry and drops it; on conflicts git keeps the entry and says so in its output. */
  public void stashPop() throws GitAPIException {
    runGit(List.of("git", "stash", "pop"));
  }

  // -------------------------------------------------------------------------
  // History
  // -------------------------------------------------------------------------

  /**
   * Newest-first commits reachable from HEAD that touched {@code scope} (a file or folder inside the repository), or
   * all commits when {@code scope} is the repository root. Empty for a repository without commits.
   */
  @NonNull
  public List<GitCommitInfo> getHistory(@NonNull File scope, int maxCount) throws GitAPIException, IOException {
    Repository repository = git.getRepository();
    ObjectId head = repository.resolve(Constants.HEAD);
    if (head == null) {
      return List.of();
    }
    LogCommand log = git.log().add(head).setMaxCount(maxCount);
    String path = toRepoRelativePath(scope);
    if (!path.isEmpty()) {
      log.addPath(path);
    }
    List<GitCommitInfo> result = new ArrayList<>();
    for (RevCommit commit : log.call()) {
      result.add(new GitCommitInfo(commit.name(), commit.abbreviate(7).name(), commit.getShortMessage(),
          commit.getFullMessage().trim(), commit.getAuthorIdent().getName(), commit.getAuthorIdent().getWhenAsInstant()));
    }
    return result;
  }

  /** git's own output, with a hint for the common "remote has commits you don't" rejection. */
  private static String describeFailure(String output, int exitCode) {
    if (output.isEmpty()) {
      return StudioBundle.get("versioncontrol_push_failed", "exit code " + exitCode);
    }
    if (output.contains("stale info")) {
      return StudioBundle.get("versioncontrol_push_lease_rejected") + "\n\n" + output;
    }
    if (output.contains("non-fast-forward") || output.contains("fetch first")) {
      return StudioBundle.get("versioncontrol_push_rejected") + "\n\n" + output;
    }
    return output;
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

  /**
   * Replaces {@code file}'s working-tree (and index) content with its version in commit
   * {@code commitId}, discarding whatever is there now. The result shows up as a regular change
   * against HEAD, to be committed or reverted like any other.
   */
  public void restoreFromCommit(@NonNull File file, @NonNull String commitId) throws GitAPIException {
    git.checkout().setStartPoint(commitId).addPath(toRepoRelativePath(file)).call();
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
