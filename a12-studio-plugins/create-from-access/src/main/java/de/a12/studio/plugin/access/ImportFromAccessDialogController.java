package de.a12.studio.plugin.access;

import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.ui.components.DialogController;
import de.a12.studio.ui.components.StudioFileChooser;
import de.a12.studio.ui.util.FileUtils;
import de.a12.studio.ui.util.StudioBundle;
import java.util.ResourceBundle;
import de.a12.studio.ui.util.WidgetFactory;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Slf4j
public class ImportFromAccessDialogController implements DialogController {

  /**
   * The result passed back to the caller after a successful dialog submission.
   *
   * @param accessFile   the Access .accdb/.mdb file chosen by the user
   * @param tableName    the table selected from the list
   * @param columns      all columns of that table (in natural table order)
   * @param modelName    the user-supplied document model name (filename, no extension)
   */
  public record AccessImportInput(
      @NonNull File accessFile,
      @NonNull String tableName,
      @NonNull List<AccessImportService.ColumnInfo> columns,
      @NonNull String modelName
  ) {
  }

  // -------------------------------------------------------------------------
  // FXML fields
  // -------------------------------------------------------------------------

  @FXML private TextField filePathField;
  @FXML private Button browseButton;
  @FXML private ListView<String> tableListView;
  @FXML private Label tableHintLabel;
  @FXML private TextField modelNameField;
  @FXML private Label pathLabel;
  @FXML private Button okButton;
  @FXML private Button cancelButton;

  // -------------------------------------------------------------------------
  // State
  // -------------------------------------------------------------------------

  private Stage stage;
  private Optional<ButtonType> result = Optional.of(ButtonType.CANCEL);

  /** The Access file currently loaded, or {@code null} if none. */
  @Nullable
  private File currentAccessFile;

  /** Columns of the currently selected table, or empty if none selected yet. */
  private List<AccessImportService.ColumnInfo> currentColumns = List.of();

  private final AccessImportService importService = new AccessImportService();

  // -------------------------------------------------------------------------
  // Initialisation
  // -------------------------------------------------------------------------

  @FXML
  private void initialize() {
    tableListView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

    // Populate model name field from the selected table name as a convenience default.
    tableListView.getSelectionModel().selectedItemProperty().addListener((obs, old, selected) -> {
      if (selected != null) {
        loadColumnsForTable(selected);
        if (modelNameField.getText().isBlank()) {
          modelNameField.setText(selected);
        }
      }
    });

    // OK only enabled when: a file is chosen, a table is selected, and a valid name is entered.
    okButton.disableProperty().bind(Bindings.createBooleanBinding(
        () -> currentAccessFile == null
            || tableListView.getSelectionModel().isEmpty()
            || !FileUtils.isValidWindowsFilename(modelNameField.getText()),
        tableListView.getSelectionModel().selectedItemProperty(),
        modelNameField.textProperty()
    ));
  }

  // -------------------------------------------------------------------------
  // Event handlers
  // -------------------------------------------------------------------------

  @FXML
  private void onBrowse() {
    StudioFileChooser chooser = new StudioFileChooser();
    chooser.setTitle(StudioBundle.get("import_access.choose_file_title"));
    chooser.getExtensionFilters().add(
        new javafx.stage.FileChooser.ExtensionFilter(
            StudioBundle.get("import_access.file_filter_desc"), "*.accdb", "*.mdb"));
    File file = chooser.showOpenDialog(stage);
    if (file == null) {
      return;
    }
    loadAccessFile(file);
  }

  @FXML
  private void onDialogSubmit() {
    String selectedTable = tableListView.getSelectionModel().getSelectedItem();
    if (currentAccessFile == null || selectedTable == null || modelNameField.getText().isBlank()) {
      return;
    }
    result = Optional.of(ButtonType.OK);
    stage.close();
  }

  @Override
  public void onDialogCancel() {
    stage.close();
  }

  // -------------------------------------------------------------------------
  // Internal helpers
  // -------------------------------------------------------------------------

  private void loadAccessFile(@NonNull File file) {
    tableListView.getItems().clear();
    tableListView.setPlaceholder(new Label(StudioBundle.get("import_access.loading")));
    currentAccessFile = null;
    currentColumns = List.of();

    // Run the (potentially slow) I/O on a background thread to keep the UI responsive.
    Thread thread = new Thread(() -> {
      try {
        List<String> tableNames = importService.readTableNames(file);
        Platform.runLater(() -> {
          currentAccessFile = file;
          filePathField.setText(file.getAbsolutePath());
          if (tableNames.isEmpty()) {
            tableListView.setPlaceholder(new Label(StudioBundle.get("import_access.no_tables")));
          }
          else {
            tableListView.getItems().setAll(tableNames);
            tableListView.setPlaceholder(null);
          }
        });
      }
      catch (IOException e) {
        log.warn("Failed to read Access database '{}': {}", file.getAbsolutePath(), e.getMessage(), e);
        Platform.runLater(() -> {
          filePathField.setText("");
          tableListView.setPlaceholder(
              new Label(StudioBundle.get("import_access.read_error") + "\n" + e.getMessage()));
          WidgetFactory.showAlert(stage,
              StudioBundle.get("import_access.read_error"), e.getMessage());
        });
      }
    }, "access-db-reader");
    thread.setDaemon(true);
    thread.start();
  }

  private void loadColumnsForTable(@NonNull String tableName) {
    if (currentAccessFile == null) {
      return;
    }
    try {
      currentColumns = importService.readColumns(currentAccessFile, tableName);
    }
    catch (IOException e) {
      log.warn("Failed to read columns for table '{}': {}", tableName, e.getMessage(), e);
      currentColumns = List.of();
      WidgetFactory.showAlert(stage, StudioBundle.get("import_access.read_error"), e.getMessage());
    }
  }

  // -------------------------------------------------------------------------
  // Static factory
  // -------------------------------------------------------------------------

  /**
   * Opens the dialog and blocks until it is closed.
   *
   * @param owner        the owner stage
   * @param targetFolder the project item folder into which the new model will be placed
   * @return the user input, or an empty optional if the dialog was cancelled
   */
  public static Optional<AccessImportInput> show(@NonNull Stage owner, @NonNull ProjectItem targetFolder) {
    FXMLLoader loader = new FXMLLoader(
        ImportFromAccessDialogController.class.getResource("dialog-import-from-access.fxml"));
    loader.setResources(ResourceBundle.getBundle(
        "de.a12.studio.plugin.access.messages",
        java.util.Locale.getDefault(),
        ImportFromAccessDialogController.class.getClassLoader()));
    Stage stage = WidgetFactory.createDialogStage(
        "dialog-import-from-access", loader, owner,
        StudioBundle.get("import_access.dialog_title"));
    ImportFromAccessDialogController controller =
        (ImportFromAccessDialogController) stage.getUserData();
    controller.stage = stage;
    controller.pathLabel.setText(targetFolder.getPath());
    controller.pathLabel.setTooltip(WidgetFactory.createTooltip(targetFolder.getPath()));
    stage.showAndWait();

    if (controller.result.isPresent() && controller.result.get() == ButtonType.OK) {
      String tableName = controller.tableListView.getSelectionModel().getSelectedItem();
      String modelName = controller.modelNameField.getText().trim();
      if (controller.currentAccessFile != null && tableName != null && !modelName.isBlank()) {
        return Optional.of(new AccessImportInput(
            controller.currentAccessFile,
            tableName,
            controller.currentColumns,
            modelName));
      }
    }
    return Optional.empty();
  }
}
