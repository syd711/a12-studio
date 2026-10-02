package de.a12.studio.kernel;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

/** Access to the sample projects under {@code testing/workspaces} for the kernel contract tests. */
final class TestWorkspaces {

  private TestWorkspaces() {
  }

  static Path findRoot() {
    Path dir = Path.of("").toAbsolutePath();
    while (dir != null && !Files.isDirectory(dir.resolve("testing").resolve("workspaces"))) {
      dir = dir.getParent();
    }
    if (dir == null) {
      throw new IllegalStateException("testing/workspaces not found above " + Path.of("").toAbsolutePath());
    }
    return dir;
  }

  static String read(String workspaceRelative) throws IOException {
    return Files.readString(findRoot().resolve("testing/workspaces").resolve(workspaceRelative), StandardCharsets.UTF_8);
  }

  /** Model ids are file names without {@code .json}; indexes every json of the workspace by that. */
  static KernelModelSource source(String workspace) throws IOException {
    Map<String, Path> byId = new HashMap<>();
    try (Stream<Path> files = Files.walk(findRoot().resolve("testing/workspaces").resolve(workspace))) {
      files.filter(f -> f.toString().endsWith(".json"))
          .forEach(f -> byId.putIfAbsent(f.getFileName().toString().replaceFirst("[.]json$", ""), f));
    }
    return id -> {
      Path file = byId.get(id);
      if (file == null) {
        throw new UncheckedIOException(new FileNotFoundException(id));
      }
      try {
        return Files.readString(file, StandardCharsets.UTF_8);
      }
      catch (IOException e) {
        throw new UncheckedIOException(e);
      }
    };
  }
}
