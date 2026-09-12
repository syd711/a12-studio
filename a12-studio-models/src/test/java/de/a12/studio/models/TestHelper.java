package de.a12.studio.models;

import java.nio.file.Files;
import java.nio.file.Path;

public class TestHelper {

  public static Path resolveTestingBasicDir() {
    return resolveTestingWorkspaceDir("basic");
  }

  public static Path resolveTestingAdvancedNewDir() {
    return resolveTestingWorkspaceDir("advanced_new");
  }

  private static Path resolveTestingWorkspaceDir(String workspaceName) {
    for (Path dir = Path.of("").toAbsolutePath(); dir != null; dir = dir.getParent()) {
      Path candidate = dir.resolve("testing").resolve("workspaces").resolve(workspaceName);
      if (Files.isDirectory(candidate)) {
        return candidate;
      }
    }
    throw new IllegalStateException("Could not locate 'testing/workspaces/" + workspaceName + "' above " + Path.of("").toAbsolutePath());
  }
}

