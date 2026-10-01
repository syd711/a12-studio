package de.a12.studio.ui.versioncontrol;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Repository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * {@link GitService#push(boolean)} runs the installed {@code git} executable, so these tests push to a local bare
 * repository and are skipped where no {@code git} is on the PATH.
 */
class GitServicePushTest {

  private static final PersonIdent AUTHOR = new PersonIdent("Test", "test@example.com");

  @TempDir
  Path temp;

  @BeforeAll
  static void requireGit() {
    boolean available;
    try {
      available = new ProcessBuilder("git", "--version").start().waitFor() == 0;
    }
    catch (Exception e) {
      available = false;
    }
    assumeTrue(available, "git executable not available");
  }

  @Test
  void pushesOutgoingCommits() throws Exception {
    File remote = createBareRemote();
    File local = cloneAndCommit(remote, "a.json", "{}");

    try (GitService service = open(local)) {
      GitBranchStatus before = service.getBranchStatus();
      assertEquals(1, before.aheadCount());
      assertTrue(before.canPush(false));

      service.push(false);

      GitBranchStatus after = service.getBranchStatus();
      assertEquals(0, after.aheadCount());
      assertFalse(after.canPush(false));
      assertEquals(head(local), remoteHead(remote, "main"));
    }
  }

  @Test
  void rewrittenHistoryNeedsForcePush() throws Exception {
    File remote = createBareRemote();
    File local = cloneAndCommit(remote, "a.json", "{}");
    try (GitService service = open(local)) {
      service.push(false);
    }
    try (Git git = Git.open(local)) {
      Files.writeString(local.toPath().resolve("a.json"), "{\"x\":1}");
      git.add().addFilepattern("a.json").call();
      git.commit().setAmend(true).setMessage("amended").setAuthor(AUTHOR).setCommitter(AUTHOR).call();
    }

    try (GitService service = open(local)) {
      GitBranchStatus status = service.getBranchStatus();
      assertEquals(1, status.aheadCount());
      assertEquals(1, status.behindCount());

      GitPushException rejected = assertThrows(GitPushException.class, () -> service.push(false));
      assertTrue(rejected.getMessage().contains("non-fast-forward") || rejected.getMessage().contains("rejected"),
          rejected.getMessage());

      service.push(true);
      assertEquals(head(local), remoteHead(remote, "main"));
      assertEquals(0, service.getBranchStatus().behindCount());
    }
  }

  @Test
  void forcePushCanRewindRemoteThatIsOnlyAhead() {
    GitBranchStatus onlyBehind = new GitBranchStatus("main", false, 0, 2, true);
    assertFalse(onlyBehind.canPush(false));
    assertTrue(onlyBehind.canPush(true));
  }

  @Test
  void firstPushOfNewBranchSetsUpstream() throws Exception {
    File remote = createBareRemote();
    File local = cloneAndCommit(remote, "a.json", "{}");
    try (GitService service = open(local)) {
      service.push(false);
    }
    try (Git git = Git.open(local)) {
      git.checkout().setCreateBranch(true).setName("feature").call();
      Files.writeString(local.toPath().resolve("b.json"), "{}");
      git.add().addFilepattern("b.json").call();
      git.commit().setMessage("feature").setAuthor(AUTHOR).setCommitter(AUTHOR).call();
    }

    try (GitService service = open(local)) {
      GitBranchStatus before = service.getBranchStatus();
      assertNull(before.aheadCount());
      assertTrue(before.canPush(false));

      service.push(false);

      assertEquals(0, service.getBranchStatus().aheadCount());
      assertEquals(head(local), remoteHead(remote, "feature"));
    }
  }

  @Test
  void repositoryWithoutRemoteCannotPush() throws Exception {
    File local = temp.resolve("standalone").toFile();
    try (Git git = Git.init().setDirectory(local).setInitialBranch("main").call()) {
      Files.writeString(local.toPath().resolve("a.json"), "{}");
      git.add().addFilepattern("a.json").call();
      git.commit().setMessage("init").setAuthor(AUTHOR).setCommitter(AUTHOR).call();
    }
    try (GitService service = open(local)) {
      assertFalse(service.getBranchStatus().canPush(false));
      assertFalse(service.getBranchStatus().canPush(true));
      assertThrows(GitPushException.class, () -> service.push(false));
    }
  }

  /** A bare remote whose {@code main} already has one commit, so clones get a real upstream. */
  private File createBareRemote() throws Exception {
    File remote = temp.resolve("remote.git").toFile();
    Git.init().setBare(true).setDirectory(remote).setInitialBranch("main").call().close();
    File seed = temp.resolve("seed").toFile();
    try (Git git = Git.init().setDirectory(seed).setInitialBranch("main").call()) {
      Files.writeString(seed.toPath().resolve("README.md"), "seed");
      git.add().addFilepattern("README.md").call();
      git.commit().setMessage("seed").setAuthor(AUTHOR).setCommitter(AUTHOR).call();
      git.push().setRemote(remote.toURI().toString()).add("main").call();
    }
    return remote;
  }

  /** Clones {@code remote} and adds one unpushed commit on {@code main}. */
  private File cloneAndCommit(File remote, String fileName, String content) throws Exception {
    File local = temp.resolve("local").toFile();
    try (Git git = Git.cloneRepository().setURI(remote.toURI().toString()).setDirectory(local).call()) {
      Files.writeString(local.toPath().resolve(fileName), content);
      git.add().addFilepattern(fileName).call();
      git.commit().setMessage("change").setAuthor(AUTHOR).setCommitter(AUTHOR).call();
    }
    return local;
  }

  private static GitService open(File folder) {
    return GitService.openForProjectFolder(folder).orElseThrow();
  }

  private static ObjectId head(File local) throws Exception {
    try (Git git = Git.open(local)) {
      return git.getRepository().resolve("HEAD");
    }
  }

  private static ObjectId remoteHead(File remote, String branch) throws Exception {
    try (Repository repository = Git.open(remote).getRepository()) {
      return repository.resolve("refs/heads/" + branch);
    }
  }
}
