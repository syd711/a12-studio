package de.a12.studio.dataservices.projects;

import de.a12.studio.dataservices.models.A12Model;
import de.a12.studio.dataservices.models.ModelFactory;
import org.jspecify.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ProjectItem {

  private File file;
  private ProjectItem parent;
  private List<ProjectItem> children;
  private boolean root = false;
  private A12Model model;
  private boolean loaded = false;

  public ProjectItem(File listFile) {
    this.file = listFile;
    this.load();
  }

  private void load() {
    if (!loaded) {
      this.loaded = true;
      if (!this.isFolder()) {
        this.model = ModelFactory.load(this);
      }
    }
  }

  public A12Model getModel() {
    return model;
  }

  public void setRoot(boolean root) {
    this.root = root;
  }

  public boolean isFolder() {
    return file.isDirectory();
  }

  public String getName() {
    return file.getName();
  }

  public ProjectItem getParent() {
    return parent;
  }

  public List<ProjectItem> getChildren() {
    if (!this.isFolder()) {
      return Collections.emptyList();
    }

    if (this.children == null) {
      this.children = new ArrayList<>();

      File[] files = file.listFiles(new ProjectFileFilter());
      for (File listFile : files) {
        ProjectItem item = new ProjectItem(listFile);
        item.parent = this;
        children.add(item);
      }
    }
    return children;
  }

  public String getPath() {
    return this.file.getAbsolutePath();
  }

  public boolean isRoot() {
    return root;
  }

  public ProjectItem createChildFolder(String name) throws IOException {
    return createChild(name, true);
  }

  public ProjectItem createChildModel(String name) throws IOException {
    String fileName = name.endsWith(".json") ? name : name + ".json";
    return createChild(fileName, false);
  }

  private ProjectItem createChild(String name, boolean folder) throws IOException {
    if (!isFolder()) {
      throw new IOException("Cannot create '" + name + "' inside a file");
    }

    File newFile = new File(file, name);
    if (newFile.exists()) {
      throw new IOException("'" + name + "' already exists");
    }

    if (folder) {
      Files.createDirectory(newFile.toPath());
    }
    else {
      Files.createFile(newFile.toPath());
    }

    ProjectItem item = new ProjectItem(newFile);
    item.parent = this;
    if (children != null) {
      children.add(item);
    }
    return item;
  }

  public void renameTo(String newName) throws IOException {
    File newFile = new File(file.getParentFile(), newName);
    if (newFile.exists()) {
      throw new IOException("'" + newName + "' already exists");
    }

    Files.move(file.toPath(), newFile.toPath());
    this.file = newFile;
  }

  public ProjectItem createCopy() throws IOException {
    File copyFile = new File(file.getParentFile(), buildCopyName());
    if (isFolder()) {
      copyDirectory(file.toPath(), copyFile.toPath());
    }
    else {
      Files.copy(file.toPath(), copyFile.toPath(), StandardCopyOption.COPY_ATTRIBUTES);
    }

    ProjectItem copy = new ProjectItem(copyFile);
    copy.parent = parent;
    if (parent != null && parent.children != null) {
      parent.children.add(copy);
    }
    return copy;
  }

  public void delete() throws IOException {
    if (isFolder()) {
      deleteDirectory(file.toPath());
    }
    else {
      Files.delete(file.toPath());
    }

    if (parent != null && parent.children != null) {
      parent.children.remove(this);
    }
  }

  private String buildCopyName() {
    String name = file.getName();
    String base = name;
    String extension = "";
    if (!isFolder()) {
      int dot = name.lastIndexOf('.');
      if (dot > 0) {
        base = name.substring(0, dot);
        extension = name.substring(dot);
      }
    }

    File parentFile = file.getParentFile();
    String candidate = base + " - Copy" + extension;
    int counter = 2;
    while (new File(parentFile, candidate).exists()) {
      candidate = base + " - Copy (" + counter + ")" + extension;
      counter++;
    }
    return candidate;
  }

  private static void copyDirectory(Path source, Path target) throws IOException {
    Files.createDirectory(target);
    File[] files = source.toFile().listFiles(new ProjectFileFilter());
    if (files != null) {
      for (File child : files) {
        Path childTarget = target.resolve(child.getName());
        if (child.isDirectory()) {
          copyDirectory(child.toPath(), childTarget);
        }
        else {
          Files.copy(child.toPath(), childTarget, StandardCopyOption.COPY_ATTRIBUTES);
        }
      }
    }
  }

  private static void deleteDirectory(Path directory) throws IOException {
    File[] files = directory.toFile().listFiles();
    if (files != null) {
      for (File child : files) {
        if (child.isDirectory()) {
          deleteDirectory(child.toPath());
        }
        else {
          Files.delete(child.toPath());
        }
      }
    }
    Files.delete(directory);
  }
}
