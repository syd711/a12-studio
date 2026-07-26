package de.a12.studio.models.projects;

import de.a12.studio.models.auth.AuthFileType;

import java.io.File;
import java.io.FileFilter;

public class ProjectFileFilter implements FileFilter {
  @Override
  public boolean accept(File pathname) {
    String name = pathname.getName();
    if(pathname.isDirectory() && name.startsWith(".")) {
      return false;
    }
    return name.equals("settings.json") || AuthFileType.fromFileName(name) != null
        || name.endsWith(".json") || pathname.isDirectory();
  }
}
