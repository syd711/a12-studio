package de.a12.studio.ui.updater;

import de.a12.studio.ui.util.SystemCommandExecutor;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Applies a downloaded client-update ZIP to the installed application files and relaunches -
 * runs as a separate {@code A12-Studio.exe --apply-update <zip> <pid>} process, started by the
 * process being replaced right before it exits (see {@link Updater#installClientUpdate}),
 * instead of a batch script dropped to disk and executed via {@code cmd.exe}.
 */
@Slf4j
public class UpdateApplier {

  public static final String APPLY_UPDATE_FLAG = "--apply-update";
  private static final String BACKUP_SUFFIX = ".old";

  public static void run(String[] args) {
    if (args.length < 3) {
      log.error("Usage: {} <zipFileName> <parentPid>", APPLY_UPDATE_FLAG);
      return;
    }
    String zipFileName = args[1];
    long parentPid;
    try {
      parentPid = Long.parseLong(args[2]);
    }
    catch (NumberFormatException e) {
      log.error("Invalid parent pid argument: {}", args[2]);
      return;
    }

    waitForProcessToExit(parentPid);

    File baseFolder = Updater.getWriteableBaseFolder();
    File zip = new File(baseFolder, zipFileName);
    try {
      extract(zip, baseFolder);
      Files.deleteIfExists(zip.toPath());
    }
    catch (IOException e) {
      log.error("Failed to apply update from {}: {}", zip.getAbsolutePath(), e.getMessage(), e);
      return;
    }

    relaunch(baseFolder);
  }

  private static void waitForProcessToExit(long pid) {
    ProcessHandle.of(pid).ifPresent(handle -> {
      try {
        handle.onExit().get(30, TimeUnit.SECONDS);
      }
      catch (Exception e) {
        log.warn("Timed out waiting for previous process {} to exit: {}", pid, e.getMessage());
      }
    });
  }

  /**
   * Extracts {@code zip} into {@code targetDir}, replacing each existing target file by first
   * renaming it aside ({@code .old}) rather than truncating it in place. By the time this runs,
   * the previous app process has exited, but this helper process is itself a running instance of
   * the very exe/jar being replaced - Windows only allows a currently-executing file to be
   * renamed/deleted, not overwritten in place. Leftover {@code .old} files are removed on the
   * next ordinary startup, see {@link #cleanupBackupFiles(File)}.
   */
  private static void extract(File zip, File targetDir) throws IOException {
    try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(zip.toPath()))) {
      ZipEntry entry;
      while ((entry = zis.getNextEntry()) != null) {
        File target = new File(targetDir, entry.getName());
        if (entry.isDirectory()) {
          Files.createDirectories(target.toPath());
          continue;
        }
        File parent = target.getParentFile();
        if (parent != null) {
          Files.createDirectories(parent.toPath());
        }
        if (target.exists()) {
          File backup = new File(parent, target.getName() + BACKUP_SUFFIX);
          Files.deleteIfExists(backup.toPath());
          Files.move(target.toPath(), backup.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
        try (OutputStream out = Files.newOutputStream(target.toPath(), StandardOpenOption.CREATE_NEW)) {
          zis.transferTo(out);
        }
      }
    }
  }

  private static void relaunch(File baseFolder) {
    List<String> commands = List.of(new File(baseFolder, "A12-Studio.exe").getAbsolutePath());
    SystemCommandExecutor executor = new SystemCommandExecutor(commands, false);
    executor.setDir(baseFolder);
    executor.executeCommandAsync();
  }

  /**
   * Deletes {@code *.old} backups left behind by {@link #extract}. Called once on ordinary
   * startup rather than from within {@link #run} itself, since the helper process that creates
   * them may still hold them open at the moment it relaunches the app.
   */
  public static void cleanupBackupFiles(File baseFolder) {
    File[] files = baseFolder.listFiles((dir, name) -> name.endsWith(BACKUP_SUFFIX));
    if (files == null) {
      return;
    }
    for (File file : files) {
      if (!file.delete()) {
        log.warn("Failed to delete leftover update backup file {}", file.getAbsolutePath());
      }
    }
  }
}
