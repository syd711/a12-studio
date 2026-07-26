package de.a12.studio.models.auth;

import de.a12.studio.models.util.YamlSettings;
import de.a12.studio.models.projects.ProjectItem;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.io.File;

@Slf4j
public class AuthFileFactory {

  @Nullable
  public static AuthDocument load(@NonNull ProjectItem projectItem) {
    if (projectItem.isFolder()) {
      return null;
    }

    AuthFileType type = AuthFileType.fromFileName(projectItem.getName());
    if (type == null) {
      return null;
    }

    try {
      File file = new File(projectItem.getPath());
      return switch (type) {
        case ROLES -> YamlSettings.objectMapper.readValue(file, RolesDocument.class);
        case USERS -> YamlSettings.objectMapper.readValue(file, UsersDocument.class);
      };
    }
    catch (Exception e) {
      log.warn("Failed to load auth file from '{}': {}", projectItem.getPath(), e.getMessage(), e);
      return null;
    }
  }
}
