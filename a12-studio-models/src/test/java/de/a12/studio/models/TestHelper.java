package de.a12.studio.models;

import java.nio.file.Files;
import java.nio.file.Path;

public class TestHelper {

  public static Path resolveTestingBasicDir() {
    for (Path dir = Path.of("").toAbsolutePath(); dir != null; dir = dir.getParent()) {
      Path candidate = dir.resolve("testing").resolve("workspaces").resolve("basic");
      if (Files.isDirectory(candidate)) {
        return candidate;
      }
    }
    throw new IllegalStateException("Could not locate 'testing/basic' above " + Path.of("").toAbsolutePath());
  }
}

