package de.a12.studio.models.projects;

import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * The non-model files of a project - SME's "Workspace Resources", e.g. the XSD files a Transformer Model transforms.
 * {@link ProjectItem} only ever lists JSON models and folders (see {@link ProjectFileFilter}), so resources are
 * looked up on the file system instead: every file with the wanted extension below the project folder, skipping
 * hidden folders and build output.
 */
@Slf4j
public final class ProjectResources {

  /** The folder SME's example workspaces and the advanced fixture keep their resources in. */
  public static final String RESOURCES_FOLDER_NAME = "resources";

  private static final Set<String> SKIPPED_FOLDERS = Set.of("build", "node_modules", "target", "out", "bundled", "logs");

  private static final int MAX_DEPTH = 16;

  private ProjectResources() {
  }

  /**
   * Every file below {@code projectFolder} whose name ends with {@code extension} (with or without the dot, any
   * casing), sorted by name then path. Empty when the folder does not exist.
   */
  @NonNull
  public static List<File> findFiles(@Nullable File projectFolder, @NonNull String extension) {
    if (projectFolder == null || !projectFolder.isDirectory()) {
      return List.of();
    }
    String suffix = (extension.startsWith(".") ? extension : "." + extension).toLowerCase(Locale.ROOT);
    List<File> result = new ArrayList<>();
    try {
      Files.walkFileTree(projectFolder.toPath(), Set.of(), MAX_DEPTH, new SimpleFileVisitor<>() {
        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
          Path name = dir.getFileName();
          boolean isRoot = dir.equals(projectFolder.toPath());
          if (!isRoot && name != null && (name.toString().startsWith(".") || SKIPPED_FOLDERS.contains(name.toString()))) {
            return FileVisitResult.SKIP_SUBTREE;
          }
          return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
          if (file.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(suffix)) {
            result.add(file.toFile());
          }
          return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFileFailed(Path file, IOException exc) {
          return FileVisitResult.CONTINUE;
        }
      });
    }
    catch (IOException e) {
      log.warn("Failed to list the '{}' files of '{}': {}", extension, projectFolder, e.getMessage());
    }
    result.sort((a, b) -> {
      int byName = a.getName().compareToIgnoreCase(b.getName());
      return byName != 0 ? byName : a.getPath().compareTo(b.getPath());
    });
    return result;
  }

  /**
   * Makes {@code source} a resource of the project: copies it next to the project's existing files of the same kind
   * (the folder of the first one found, so a project that keeps its XSDs in {@code schemas/} keeps doing so), or into
   * {@value #RESOURCES_FOLDER_NAME} below the project folder when it has none yet. A file of that name that is already
   * a resource is not touched or copied again; either way the resource file is returned.
   */
  @NonNull
  public static File addResource(@NonNull File projectFolder, @NonNull File source, @NonNull String extension) throws IOException {
    List<File> existing = findFiles(projectFolder, extension);
    File present = findByName(existing, source.getName());
    if (present != null) {
      return present;
    }
    File folder = existing.isEmpty() ? new File(projectFolder, RESOURCES_FOLDER_NAME) : existing.get(0).getParentFile();
    Files.createDirectories(folder.toPath());
    Path target = folder.toPath().resolve(source.getName());
    Files.copy(source.toPath(), target);
    return target.toFile();
  }

  /** The first file named {@code fileName} (case-sensitive, extension included) among {@code files}, or null. */
  @Nullable
  public static File findByName(@NonNull List<File> files, @Nullable String fileName) {
    if (fileName == null || fileName.isBlank()) {
      return null;
    }
    return files.stream().filter(file -> file.getName().equals(fileName)).findFirst().orElse(null);
  }
}
