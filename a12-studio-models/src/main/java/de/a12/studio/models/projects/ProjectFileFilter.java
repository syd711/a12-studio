package de.a12.studio.models.projects;

import java.io.File;
import java.io.FileFilter;

public class ProjectFileFilter implements FileFilter {
  @Override
  public boolean accept(File pathname) {
    String name = pathname.getName();
    if(pathname.isDirectory() && name.startsWith(".")) {
      return false;
    }
    return name.equals("settings.json") || name.endsWith(".json") || pathname.isDirectory();
  }
}
