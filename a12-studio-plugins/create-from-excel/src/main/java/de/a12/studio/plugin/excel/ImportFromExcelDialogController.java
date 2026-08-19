package de.a12.studio.plugin.excel;

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
public class ImportFromExcelDialogController implements DialogController {

  /**
   * Result returned to the caller when the dialog is confirmed.
   *
   * @param excelFile the chosen {@code .xlsx} / {@code .xls} file
   * @param columns   the columns read from the first sheet (header row + inferred types)
   * @param modelName the user-supplied document model name (filename without extension)
   */
  public record ExcelImportInput(
      @NonNull File excelFile,
      @NonNull List<ExcelImportService.ColumnInfo> columns,
      @NonNull String modelName
  ) {
  }

  // -------------------------------------------------------------------------
  // FXML fields
  // -------------------------------------------------------------------------

  @FXML private TextField filePathField;
  @FXML private Button browseButton;
  @FXML private ListView<String> columnListView;
  @FXML private TextField modelNameField;
  @FXML private Label pathLabel;
  @FXML private Button okButton;
  @FXML private Button cancelButton;

  // -------------------------------------------------------------------------
  // State
  // -------------------------------------------------------------------------

  private Stage stage;
  private Optional<ButtonType> result = Optional.of(ButtonType.CANCEL);

  @Nullable
  private File currentExcelFile;

  /** Columns read from the first sheet, populated after a file is chosen. */
  private List<ExcelImportService.ColumnInfo> currentColumns = List.of();

  private final ExcelImportService importService = new ExcelImportService();

  // -------------------------------------------------------------------------
  // Initialisation
  // -------------------------------------------------------------------------

  @FXML
  private void initialize() {
    columnListView.setMouseTransparent(true);
    columnListView.setFocusTraversable(false);

    okButton.disableProperty().bind(Bindings.createBooleanBinding(
        () -> currentExcelFile == null
            || !FileUtils.isValidWindowsFilename(modelNameField.getText()),
        modelNameField.textProperty()
    ));
  }

  // -------------------------------------------------------------------------
  // Event handlers
  // -------------------------------------------------------------------------

  @FXML
  private void onBrowse() {
    StudioFileChooser chooser = new StudioFileChooser();
    chooser.setTitle(StudioBundle.get("import_excel.choose_file_title"));
    chooser.getExtensionFilters().add(
        new javafx.stage.FileChooser.ExtensionFilter(
            StudioBundle.get("import_excel.file_filter_desc"), "*.xlsx", "*.xls"));
    File file = chooser.showOpenDialog(stage);
    if (file == null) {
      return;
    }
    loadExcelFile(file);
  }

  @FXML
  private void onDialogSubmit() {
    if (currentExcelFile == null || modelNameField.getText().isBlank()) {
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

  private void loadExcelFile(@NonNull File file) {
    columnListView.getItems().clear();
    columnListView.setPlaceholder(new Label(StudioBundle.get("import_excel.loading_columns")));
    currentExcelFile = null;
    currentColumns = List.of();

    Thread thread = new Thread(() -> {
      try {
        List<ExcelImportService.ColumnInfo> columns = importService.readFirstSheetColumns(file);
        Platform.runLater(() -> {
          currentExcelFile = file;
          filePathField.setText(file.getAbsolutePath());
          if (modelNameField.getText().isBlank()) {
            String fileName = file.getName();
            int dot = fileName.lastIndexOf('.');
            modelNameField.setText(dot > 0 ? fileName.substring(0, dot) : fileName);
          }
          currentColumns = columns;
          if (columns.isEmpty()) {
            columnListView.setPlaceholder(new Label(StudioBundle.get("import_excel.no_columns")));
          }
          else {
            columnListView.getItems().setAll(
                columns.stream().map(ExcelImportService.ColumnInfo::name).toList());
            columnListView.setPlaceholder(null);
          }
        });
      }
      catch (IOException e) {
        log.warn("Failed to read Excel file '{}': {}", file.getAbsolutePath(), e.getMessage(), e);
        Platform.runLater(() -> {
          filePathField.setText("");
          columnListView.setPlaceholder(
              new Label(StudioBundle.get("import_excel.read_error") + "\n" + e.getMessage()));
          WidgetFactory.showAlert(stage,
              StudioBundle.get("import_excel.read_error"), e.getMessage());
        });
      }
    }, "excel-reader");
    thread.setDaemon(true);
    thread.start();
  }

  // -------------------------------------------------------------------------
  // Static factory
  // -------------------------------------------------------------------------

  /**
   * Opens the dialog modally and blocks until it is closed.
   *
   * @param owner        the owner stage
   * @param targetFolder the project item folder the new model will be placed in
   * @return the user input, or empty if the dialog was cancelled
   */
  public static Optional<ExcelImportInput> show(@NonNull Stage owner, @NonNull ProjectItem targetFolder) {
    FXMLLoader loader = new FXMLLoader(
        ImportFromExcelDialogController.class.getResource("dialog-import-from-excel.fxml"));
    loader.setResources(ResourceBundle.getBundle(
        "de.a12.studio.plugin.excel.messages",
        java.util.Locale.getDefault(),
        ImportFromExcelDialogController.class.getClassLoader()));
    Stage stage = WidgetFactory.createDialogStage(
        "dialog-import-from-excel", loader, owner,
        StudioBundle.get("import_excel.dialog_title"));
    ImportFromExcelDialogController controller =
        (ImportFromExcelDialogController) stage.getUserData();
    controller.stage = stage;
    controller.pathLabel.setText(targetFolder.getPath());
    controller.pathLabel.setTooltip(WidgetFactory.createTooltip(targetFolder.getPath()));
    stage.showAndWait();

    if (controller.result.isPresent() && controller.result.get() == ButtonType.OK) {
      String modelName = controller.modelNameField.getText().trim();
      if (controller.currentExcelFile != null && !modelName.isBlank()) {
        return Optional.of(new ExcelImportInput(
            controller.currentExcelFile,
            controller.currentColumns,
            modelName));
      }
    }
    return Optional.empty();
  }
}
