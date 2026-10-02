package de.a12.studio.ui.versioncontrol;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.PersonIdent;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Pull, stash and history in {@link GitService}. Pull and stash run the installed {@code git} executable, so these
 * tests work against local repositories and are skipped where no {@code git} is on the PATH.
 */
class GitServicePullStashHistoryTest {

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
  void pullWithRebaseReplaysLocalCommitsOnTopOfRemote() throws Exception {
    File remote = createBareRemote();
    File local = clone(remote, "local");
    File other = clone(remote, "other");
    commitFile(other, "remote.json", "{}", "remote change");
    pushFrom(other);
    commitFile(local, "local.json", "{}", "local change");

    try (GitService service = open(local)) {
      service.pull(true, false);

      assertEquals(1, service.getBranchStatus().aheadCount());
      assertEquals(0, service.getBranchStatus().behindCount());
      assertEquals(List.of("local change", "remote change", "seed"), messages(service.getHistory(local, 10)));
      assertFalse(service.isOperationInProgress());
    }
  }

  @Test
  void pullWithMergeCreatesMergeCommit() throws Exception {
    File remote = createBareRemote();
    File local = clone(remote, "local");
    File other = clone(remote, "other");
    commitFile(other, "remote.json", "{}", "remote change");
    pushFrom(other);
    commitFile(local, "local.json", "{}", "local change");

    try (GitService service = open(local)) {
      service.pull(false, false);

      assertEquals(0, service.getBranchStatus().behindCount());
      assertEquals(4, service.getHistory(local, 10).size());
    }
  }

  @Test
  void rebasePullNeedsAutostashWithDirtyTree() throws Exception {
    File remote = createBareRemote();
    File local = clone(remote, "local");
    File other = clone(remote, "other");
    commitFile(other, "remote.json", "{}", "remote change");
    pushFrom(other);
    commitFile(local, "local.json", "{}", "local change");
    Files.writeString(local.toPath().resolve("README.md"), "dirty");

    try (GitService service = open(local)) {
      assertTrue(service.hasUncommittedChanges());
      assertThrows(GitPushException.class, () -> service.pull(true, false));

      service.pull(true, true);

      assertEquals("dirty", Files.readString(local.toPath().resolve("README.md")));
      assertEquals(0, service.getBranchStatus().behindCount());
    }
  }

  @Test
  void conflictingPullLeavesOperationThatCanBeAborted() throws Exception {
    File remote = createBareRemote();
    File local = clone(remote, "local");
    File other = clone(remote, "other");
    commitFile(other, "a.json", "{\"side\":\"remote\"}", "remote a");
    pushFrom(other);
    commitFile(local, "a.json", "{\"side\":\"local\"}", "local a");

    try (GitService service = open(local)) {
      assertThrows(GitPushException.class, () -> service.pull(false, false));
      assertTrue(service.isOperationInProgress());

      service.abortOperation();

      assertFalse(service.isOperationInProgress());
      assertEquals("{\"side\":\"local\"}", Files.readString(local.toPath().resolve("a.json")));
    }
  }

  @Test
  void pullWithoutUpstreamOrMatchingRemoteBranchIsRejected() throws Exception {
    File remote = createBareRemote();
    File local = clone(remote, "local");
    try (Git git = Git.open(local)) {
      git.checkout().setCreateBranch(true).setName("feature").call();
    }
    try (GitService service = open(local)) {
      assertFalse(service.canPull());
      assertThrows(GitPushException.class, () -> service.pull(false, false));
    }
  }

  @Test
  void stashRemovesChangesAndPopRestoresThem() throws Exception {
    File remote = createBareRemote();
    File local = clone(remote, "local");
    Files.writeString(local.toPath().resolve("README.md"), "changed");
    Files.writeString(local.toPath().resolve("new.json"), "{}");

    try (GitService service = open(local)) {
      service.stash("my stash");

      assertEquals("seed", Files.readString(local.toPath().resolve("README.md")));
      assertFalse(Files.exists(local.toPath().resolve("new.json")));
      assertEquals(1, service.getStashes().size());
      assertTrue(service.getStashes().get(0).contains("my stash"), service.getStashes().get(0));

      service.stashPop();

      assertEquals("changed", Files.readString(local.toPath().resolve("README.md")));
      assertTrue(Files.exists(local.toPath().resolve("new.json")));
      assertTrue(service.getStashes().isEmpty());
    }
  }

  @Test
  void historyOfFileOnlyListsCommitsTouchingIt() throws Exception {
    File remote = createBareRemote();
    File local = clone(remote, "local");
    commitFile(local, "a.json", "1", "add a");
    commitFile(local, "b.json", "1", "add b");
    commitFile(local, "a.json", "2", "change a");

    try (GitService service = open(local)) {
      assertEquals(List.of("change a", "add b", "add a", "seed"), messages(service.getHistory(local, 10)));
      assertEquals(List.of("change a", "add a"), messages(service.getHistory(new File(local, "a.json"), 10)));
      assertEquals(2, service.getHistory(local, 2).size());
    }
  }

  @Test
  void identityIsStoredInRepositoryConfig() throws Exception {
    File remote = createBareRemote();
    File local = clone(remote, "local");
    try (GitService service = open(local)) {
      service.setIdentity("Jane Doe", "jane@example.com", false);

      assertTrue(service.hasIdentity());
    }
  }

  private static List<String> messages(List<GitCommitInfo> history) {
    return history.stream().map(GitCommitInfo::message).toList();
  }

  /** A bare remote whose {@code main} has one commit ({@code README.md}), so clones get a real upstream. */
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

  /** Clones with a local identity, so git commands run by the service can create commits regardless of the machine. */
  private File clone(File remote, String name) throws Exception {
    File dir = temp.resolve(name).toFile();
    try (Git git = Git.cloneRepository().setURI(remote.toURI().toString()).setDirectory(dir).call()) {
      var config = git.getRepository().getConfig();
      config.setString("user", null, "name", "Test");
      config.setString("user", null, "email", "test@example.com");
      config.setBoolean("commit", null, "gpgsign", false);
      config.save();
    }
    return dir;
  }

  private void commitFile(File repo, String fileName, String content, String message) throws Exception {
    try (Git git = Git.open(repo)) {
      Files.writeString(repo.toPath().resolve(fileName), content);
      git.add().addFilepattern(fileName).call();
      git.commit().setMessage(message).setAuthor(AUTHOR).setCommitter(AUTHOR).call();
    }
  }

  private void pushFrom(File repo) throws Exception {
    try (GitService service = open(repo)) {
      service.push(false);
    }
  }

  private static GitService open(File folder) {
    return GitService.openForProjectFolder(folder).orElseThrow();
  }
}
