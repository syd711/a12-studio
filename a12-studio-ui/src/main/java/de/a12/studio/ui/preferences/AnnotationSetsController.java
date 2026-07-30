package de.a12.studio.ui.preferences;

import de.a12.studio.models.projects.Project;
import de.a12.studio.models.projects.settings.annotations.AnnotationDataSet;
import de.a12.studio.models.projects.settings.annotations.AnnotationSettings;
import de.a12.studio.models.util.JsonSettings;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.components.StudioFileChooser;
import de.a12.studio.ui.preferences.dialogs.Dialogs;
import de.a12.studio.ui.util.WidgetFactory;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.FileChooser;

import java.io.File;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;

public class AnnotationSetsController implements Initializable {

  @FXML
  private TableView<AnnotationDataSet> table;

  @FXML
  private TableColumn<AnnotationDataSet, String> nameColumn;

  @FXML
  private TableColumn<AnnotationDataSet, String> headerEntriesColumn;

  @FXML
  private TableColumn<AnnotationDataSet, String> contentEntriesColumn;

  @FXML
  private Button editButton;

  @FXML
  private Button deleteButton;

  @FXML
  private Button exportButton;

  @FXML
  private Button importButton;

  private Project project;

  // Recomputed on every reload() from the project's annotation registries; identifies the "default" row by
  // reference (not by name) so an imported set that happens to collide with "default" - auto-suffixed to
  // "default_2" on import - is never mistaken for it.
  private AnnotationDataSet defaultDataSet;

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    project = Studio.getCurrentProject();
    if (project == null) {
      return;
    }

    nameColumn.setCellValueFactory(param -> new ReadOnlyStringWrapper(param.getValue().getName()));
    headerEntriesColumn.setCellValueFactory(param ->
        new ReadOnlyStringWrapper(String.valueOf(AnnotationDataSetSupport.countHeaderEntries(param.getValue()))));
    contentEntriesColumn.setCellValueFactory(param ->
        new ReadOnlyStringWrapper(String.valueOf(AnnotationDataSetSupport.countFieldEntries(param.getValue()))));

    table.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> updateButtonStates(newValue));
    updateButtonStates(null);
    table.setOnMouseClicked(event -> {
      if (event.getClickCount() == 2) {
        onEdit();
      }
    });

    reload();
  }

  private void updateButtonStates(AnnotationDataSet selected) {
    boolean isDefault = selected != null && selected == defaultDataSet;
    editButton.setDisable(selected == null || isDefault);
    deleteButton.setDisable(selected == null || isDefault);
    exportButton.setDisable(selected == null);
  }

  private void reload() {
    defaultDataSet = AnnotationDataSetSupport.buildDefault(project);
    List<AnnotationDataSet> items = new ArrayList<>();
    items.add(defaultDataSet);
    items.addAll(project.getSettings().getAnnotationSettings().getDataSets());
    table.setItems(FXCollections.observableArrayList(items));
  }

  @FXML
  private void onEdit() {
    AnnotationDataSet selected = table.getSelectionModel().getSelectedItem();
    if (selected == null || selected == defaultDataSet) {
      return;
    }
    Dialogs.showAnnotationDataSetEditor(Studio.stage, selected, true).ifPresent(edited -> {
      selected.setName(edited.getName());
      selected.setHeaderSet(edited.getHeaderSet());
      selected.setFieldSet(edited.getFieldSet());
      project.getSettings().getAnnotationSettings().save();
      reload();
    });
  }

  @FXML
  private void onDelete() {
    AnnotationDataSet selected = table.getSelectionModel().getSelectedItem();
    if (selected == null || selected == defaultDataSet) {
      return;
    }
    Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage,
        "Delete annotation set \"" + selected.getName() + "\"?", null, null, "Delete");
    if (result.isEmpty() || result.get() != ButtonType.OK) {
      return;
    }
    project.getSettings().getAnnotationSettings().getDataSets().remove(selected);
    project.getSettings().getAnnotationSettings().save();
    reload();
  }

  @FXML
  private void onExport() {
    AnnotationDataSet selected = table.getSelectionModel().getSelectedItem();
    if (selected == null) {
      return;
    }
    Dialogs.showAnnotationDataSetEditor(Studio.stage, selected, false).ifPresent(this::exportToFile);
  }

  private void exportToFile(AnnotationDataSet dataSet) {
    StudioFileChooser chooser = new StudioFileChooser();
    chooser.setTitle("Export Annotation Set");
    chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Annotation Set (*.json)", "*.json"));
    chooser.setInitialFileName(dataSet.getName() + ".json");
    File file = chooser.showSaveDialog(Studio.stage);
    if (file == null) {
      return;
    }
    try {
      Files.writeString(file.toPath(), JsonSettings.objectMapper.writeValueAsString(dataSet), StandardCharsets.UTF_8);
    }
    catch (Exception e) {
      WidgetFactory.showAlert(Studio.stage, "Could not export annotation set: " + e.getMessage());
    }
  }

  @FXML
  private void onImport() {
    StudioFileChooser chooser = new StudioFileChooser();
    chooser.setTitle("Import Annotation Sets");
    chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Annotation Set (*.json)", "*.json"));
    List<File> files = chooser.showOpenMultipleDialog(Studio.stage);
    if (files == null || files.isEmpty()) {
      return;
    }

    AnnotationSettings settings = project.getSettings().getAnnotationSettings();
    Set<String> usedNames = new HashSet<>();
    usedNames.add(defaultDataSet.getName());
    for (AnnotationDataSet dataSet : settings.getDataSets()) {
      usedNames.add(dataSet.getName());
    }

    List<String> failures = new ArrayList<>();
    int importedCount = 0;
    for (File file : files) {
      try {
        String json = Files.readString(file.toPath(), StandardCharsets.UTF_8);
        AnnotationDataSet imported = JsonSettings.objectMapper.readValue(json, AnnotationDataSet.class);
        String baseName = imported.getName() == null || imported.getName().isBlank() ? "imported" : imported.getName();
        String name = uniqueName(baseName, usedNames);
        imported.setName(name);
        usedNames.add(name);
        settings.getDataSets().add(imported);
        importedCount++;
      }
      catch (Exception e) {
        failures.add(file.getName() + ": " + e.getMessage());
      }
    }

    if (importedCount > 0) {
      settings.save();
      reload();
    }
    if (!failures.isEmpty()) {
      WidgetFactory.showAlert(Studio.stage, "Could not import " + failures.size() + " file(s):\n" + String.join("\n", failures));
    }
  }

  private static String uniqueName(String baseName, Set<String> usedNames) {
    String candidate = baseName;
    int suffix = 2;
    while (usedNames.contains(candidate)) {
      candidate = baseName + "_" + suffix;
      suffix++;
    }
    return candidate;
  }
}
