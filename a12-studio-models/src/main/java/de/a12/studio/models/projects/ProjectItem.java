package de.a12.studio.models.projects;

import de.a12.studio.models.util.JsonSettings;
import de.a12.studio.models.util.YamlSettings;
import de.a12.studio.models.A12Model;
import de.a12.studio.models.ModelFactory;
import de.a12.studio.models.auth.AuthDocument;
import de.a12.studio.models.auth.AuthFileFactory;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Slf4j
public class ProjectItem {

  @Getter
  private File file;
  @Getter
  private ProjectItem parent;
  private List<ProjectItem> children;
  @Getter
  @Setter
  private boolean root = false;
  @Getter
  @Setter
  private A12Model<?> model;
  @Getter
  @Setter
  private AuthDocument authDocument;
  private boolean loaded = false;

  public ProjectItem(File listFile) {
    this.file = listFile;
    this.load();
  }

  public void save() {
    try {
      if (model != null) {
        JsonSettings.objectMapper.writeValue(new File(getPath()), model);
      }
      else if (authDocument != null) {
        YamlSettings.objectMapper.writeValue(new File(getPath()), authDocument);
      }
      log.info("Saved {}", getPath());
    }
    catch (Exception e) {
      log.error("Failed to save '{}': {}", getPath(), e.getMessage(), e);
    }
  }

  private void load() {
    if (!loaded) {
      this.loaded = true;
      if (!this.isFolder()) {
        this.model = ModelFactory.load(this);
        if (this.model == null) {
          this.authDocument = AuthFileFactory.load(this);
        }
      }
    }
  }

  public void reload() {
    if (!isFolder()) {
      this.model = ModelFactory.load(this);
      this.authDocument = this.model == null ? AuthFileFactory.load(this) : null;
    }
  }

  public boolean isFolder() {
    return file.isDirectory();
  }

  public String getName() {
    return file.getName();
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

    if (!isFolder() && model != null) {
      model.setId(idFromFileName(newFile.getName()));
      save();
    }
  }

  // Convention: a model's header/id always matches its filename without the ".json" suffix.
  public static String idFromFileName(String fileName) {
    return fileName.endsWith(".json") ? fileName.substring(0, fileName.length() - ".json".length()) : fileName;
  }

  /**
   * Searches the whole project tree (from the root, regardless of where {@code this} sits in it) for the
   * model whose id matches {@code modelId}, per the filename/id convention documented on {@link
   * #idFromFileName}. Used by the preview feature to resolve a model reference (e.g. an Overview Model's
   * bound Document Model) into its loaded content.
   */
  public ProjectItem findByModelId(String modelId) {
    ProjectItem root = this;
    while (root.parent != null) {
      root = root.parent;
    }
    return root.findDescendantByModelId(modelId);
  }

  private ProjectItem findDescendantByModelId(String modelId) {
    if (!isFolder()) {
      return model != null && modelId.equals(model.getId()) ? this : null;
    }
    for (ProjectItem child : getChildren()) {
      ProjectItem found = child.findDescendantByModelId(modelId);
      if (found != null) {
        return found;
      }
    }
    return null;
  }

  public boolean isAncestorOf(ProjectItem other) {
    for (ProjectItem current = other.parent; current != null; current = current.parent) {
      if (current.equals(this)) {
        return true;
      }
    }
    return false;
  }

  public void moveTo(ProjectItem newParent) throws IOException {
    File newFile = new File(newParent.file, file.getName());
    if (newFile.exists()) {
      throw new IOException("'" + file.getName() + "' already exists in '" + newParent.getName() + "'");
    }

    Files.move(file.toPath(), newFile.toPath());

    if (parent != null && parent.children != null) {
      parent.children.remove(this);
    }
    this.file = newFile;
    this.parent = newParent;
    if (newParent.children != null) {
      newParent.children.add(this);
    }
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
    if (!copy.isFolder() && copy.model != null) {
      copy.model.setId(idFromFileName(copyFile.getName()));
      copy.save();
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

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) return false;
    ProjectItem that = (ProjectItem) o;
    return Objects.equals(file, that.file);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(file);
  }
}
