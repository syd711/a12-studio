package de.a12.studio.ui.projecttree.dialogs;

import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.ui.components.DialogController;
import de.a12.studio.ui.components.StudioFileChooser;
import de.a12.studio.ui.projecttree.importdb.AccessImportService.ColumnFieldType;
import de.a12.studio.ui.projecttree.importdb.ExcelImportService;
import de.a12.studio.ui.projecttree.importdb.ExcelImportService.ColumnInfo;
import de.a12.studio.ui.util.FileUtils;
import de.a12.studio.ui.util.StudioBundle;
import de.a12.studio.ui.util.WidgetFactory;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
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
   * @param sheetName the sheet selected in the list
   * @param columns   the columns read from that sheet (header row + inferred types)
   * @param modelName the user-supplied document model name (filename without extension)
   */
  public record ExcelImportInput(
      @NonNull File excelFile,
      @NonNull String sheetName,
      @NonNull List<ColumnInfo> columns,
      @NonNull String modelName
  ) {
  }

  // -------------------------------------------------------------------------
  // FXML fields
  // -------------------------------------------------------------------------

  @FXML private TextField filePathField;
  @FXML private Button browseButton;
  @FXML private ListView<String> sheetListView;
  @FXML private TableView<ColumnInfo> columnPreviewTable;
  @FXML private TableColumn<ColumnInfo, String> colNameColumn;
  @FXML private TableColumn<ColumnInfo, String> colTypeColumn;
  @FXML private Label columnPreviewPlaceholder;
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

  /** Columns of the currently selected sheet; empty when none selected. */
  private List<ColumnInfo> currentColumns = List.of();

  private final ExcelImportService importService = new ExcelImportService();

  // -------------------------------------------------------------------------
  // Initialisation
  // -------------------------------------------------------------------------

  @FXML
  private void initialize() {
    sheetListView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

    // Wire column-preview table columns.
    colNameColumn.setCellValueFactory(
        data -> new SimpleStringProperty(data.getValue().name()));
    colTypeColumn.setCellValueFactory(
        data -> new SimpleStringProperty(localiseFieldType(data.getValue().fieldType())));

    // When a sheet is selected: load columns into the preview table and pre-fill model name.
    sheetListView.getSelectionModel().selectedItemProperty().addListener((obs, old, selected) -> {
      if (selected != null) {
        loadColumnsForSheet(selected);
        if (modelNameField.getText().isBlank()) {
          modelNameField.setText(selected);
        }
      }
      else {
        columnPreviewTable.getItems().clear();
        currentColumns = List.of();
      }
    });

    // OK enabled only when a file is loaded, a sheet is selected, and the name is valid.
    okButton.disableProperty().bind(Bindings.createBooleanBinding(
        () -> currentExcelFile == null
            || sheetListView.getSelectionModel().isEmpty()
            || !FileUtils.isValidWindowsFilename(modelNameField.getText()),
        sheetListView.getSelectionModel().selectedItemProperty(),
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
    String selectedSheet = sheetListView.getSelectionModel().getSelectedItem();
    if (currentExcelFile == null || selectedSheet == null || modelNameField.getText().isBlank()) {
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
    sheetListView.getItems().clear();
    columnPreviewTable.getItems().clear();
    currentExcelFile = null;
    currentColumns = List.of();

    Thread thread = new Thread(() -> {
      try {
        List<String> sheetNames = importService.readSheetNames(file);
        Platform.runLater(() -> {
          currentExcelFile = file;
          filePathField.setText(file.getAbsolutePath());
          if (sheetNames.isEmpty()) {
            sheetListView.setPlaceholder(
                new Label(StudioBundle.get("import_excel.no_sheets")));
          }
          else {
            sheetListView.getItems().setAll(sheetNames);
            sheetListView.setPlaceholder(null);
          }
        });
      }
      catch (IOException e) {
        log.warn("Failed to read Excel file '{}': {}", file.getAbsolutePath(), e.getMessage(), e);
        Platform.runLater(() -> {
          filePathField.setText("");
          sheetListView.setPlaceholder(
              new Label(StudioBundle.get("import_excel.read_error") + "\n" + e.getMessage()));
          WidgetFactory.showAlert(stage,
              StudioBundle.get("import_excel.read_error"), e.getMessage());
        });
      }
    }, "excel-reader");
    thread.setDaemon(true);
    thread.start();
  }

  private void loadColumnsForSheet(@NonNull String sheetName) {
    if (currentExcelFile == null) {
      return;
    }
    columnPreviewTable.getItems().clear();
    columnPreviewTable.setPlaceholder(
        new Label(StudioBundle.get("import_excel.loading_columns")));

    Thread thread = new Thread(() -> {
      try {
        List<ColumnInfo> columns = importService.readColumns(currentExcelFile, sheetName);
        Platform.runLater(() -> {
          currentColumns = columns;
          if (columns.isEmpty()) {
            columnPreviewTable.setPlaceholder(
                new Label(StudioBundle.get("import_excel.no_columns")));
          }
          else {
            columnPreviewTable.getItems().setAll(columns);
            columnPreviewTable.setPlaceholder(null);
          }
        });
      }
      catch (IOException e) {
        log.warn("Failed to read sheet '{}': {}", sheetName, e.getMessage(), e);
        Platform.runLater(() -> {
          currentColumns = List.of();
          columnPreviewTable.setPlaceholder(
              new Label(StudioBundle.get("import_excel.read_error") + "\n" + e.getMessage()));
          WidgetFactory.showAlert(stage,
              StudioBundle.get("import_excel.read_error"), e.getMessage());
        });
      }
    }, "excel-column-reader");
    thread.setDaemon(true);
    thread.start();
  }

  /** Returns a localised display string for a {@link ColumnFieldType}. */
  private String localiseFieldType(@NonNull ColumnFieldType type) {
    return switch (type) {
      case STRING -> StudioBundle.get("import_excel.type_string");
      case NUMBER -> StudioBundle.get("import_excel.type_number");
      case BOOLEAN -> StudioBundle.get("import_excel.type_boolean");
      case DATE -> StudioBundle.get("import_excel.type_date");
      case DATE_TIME -> StudioBundle.get("import_excel.type_datetime");
    };
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
    loader.setResources(StudioBundle.getBundle());
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
      String sheetName = controller.sheetListView.getSelectionModel().getSelectedItem();
      String modelName = controller.modelNameField.getText().trim();
      if (controller.currentExcelFile != null && sheetName != null && !modelName.isBlank()) {
        return Optional.of(new ExcelImportInput(
            controller.currentExcelFile,
            sheetName,
            controller.currentColumns,
            modelName));
      }
    }
    return Optional.empty();
  }
}
