package de.a12.studio.ui.updater;

import de.a12.studio.ui.util.SystemCommandExecutor;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
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
  private static final String APP_EXE = "A12-Studio.exe";
  private static final String HELPER_EXE = "A12-Studio-Updater.exe";

  /**
   * Copies {@code A12-Studio.exe} next to itself as {@code A12-Studio-Updater.exe} and returns the
   * copy, to be launched with {@link #APPLY_UPDATE_FLAG}. The launch4j exe wraps the application jar
   * ({@code java -jar A12-Studio.exe}), so a JVM started from it keeps the exe open for reading,
   * without delete/rename sharing, for as long as it runs. The helper therefore must not be the exe
   * it is about to replace - Windows rejects renaming {@code A12-Studio.exe} with "used by another
   * process". The copy lives next to the original so launch4j still finds the bundled
   * {@code java-runtime} folder; it is removed again by {@link #cleanupBackupFiles(File)}.
   */
  public static File prepareHelperExe(File baseFolder) throws IOException {
    File helper = new File(baseFolder, HELPER_EXE);
    Files.copy(new File(baseFolder, APP_EXE).toPath(), helper.toPath(), StandardCopyOption.REPLACE_EXISTING);
    return helper;
  }

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
   * renaming it aside ({@code .old}) rather than truncating it in place, so files that are still
   * mapped by a running process can be swapped. This helper runs from a copy of the exe (see
   * {@link #prepareHelperExe}), so neither the exe nor the jar is held open by the helper itself.
   * Leftover {@code .old} files are removed on the next ordinary startup, see
   * {@link #cleanupBackupFiles(File)}.
   * <p>
   * If any file fails, the files already swapped are rolled back so the installation is never left
   * half-updated (e.g. new jar next to the old exe).
   */
  private static void extract(File zip, File targetDir) throws IOException {
    List<File[]> swapped = new ArrayList<>(); // {target, backup or null}
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
        File backup = null;
        if (target.exists()) {
          backup = new File(parent, target.getName() + BACKUP_SUFFIX);
          Files.deleteIfExists(backup.toPath());
          Files.move(target.toPath(), backup.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
        swapped.add(new File[] {target, backup});
        try (OutputStream out = Files.newOutputStream(target.toPath(), StandardOpenOption.CREATE_NEW)) {
          zis.transferTo(out);
        }
      }
    }
    catch (IOException e) {
      rollback(swapped);
      throw e;
    }
  }

  private static void rollback(List<File[]> swapped) {
    for (int i = swapped.size() - 1; i >= 0; i--) {
      File target = swapped.get(i)[0];
      File backup = swapped.get(i)[1];
      try {
        Files.deleteIfExists(target.toPath());
        if (backup != null) {
          Files.move(backup.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
      }
      catch (IOException e) {
        log.error("Failed to roll back {}: {}", target.getAbsolutePath(), e.getMessage());
      }
    }
  }

  private static void relaunch(File baseFolder) {
    List<String> commands = List.of(new File(baseFolder, APP_EXE).getAbsolutePath());
    SystemCommandExecutor executor = new SystemCommandExecutor(commands, false);
    executor.setDir(baseFolder);
    executor.executeCommandAsync();
  }

  /**
   * Deletes {@code *.old} backups left behind by {@link #extract} and the helper exe copy from
   * {@link #prepareHelperExe}. Called once on ordinary
   * startup rather than from within {@link #run} itself, since the helper process that creates
   * them may still hold them open at the moment it relaunches the app.
   */
  public static void cleanupBackupFiles(File baseFolder) {
    File[] files = baseFolder.listFiles((dir, name) -> name.endsWith(BACKUP_SUFFIX) || name.equals(HELPER_EXE));
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
