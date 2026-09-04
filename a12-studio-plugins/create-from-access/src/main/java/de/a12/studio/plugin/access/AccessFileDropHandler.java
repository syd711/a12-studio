package de.a12.studio.plugin.access;

import de.a12.studio.models.NewModelFactory;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.plugin.manager.IFileDropHandler;
import de.a12.studio.ui.editors.propertyeditors.RolesEditorPanelController;
import de.a12.studio.ui.util.DocumentModelBuilder;
import de.a12.studio.ui.util.DocumentModelBuilder.ColumnDescriptor;
import de.a12.studio.ui.util.DocumentModelBuilder.ColumnType;
import de.a12.studio.ui.util.WidgetFactory;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * {@link IFileDropHandler} implementation for Microsoft Access databases.
 *
 * <p>Accepts {@code .accdb} and {@code .mdb} files. When a matching file is dropped onto
 * the studio window, the existing import dialog is opened with the file pre-loaded so the
 * user only needs to choose a table and confirm the model name.
 */
@Slf4j
public class AccessFileDropHandler implements IFileDropHandler {

  private static final List<String> ACCEPTED_TYPES =
      List.of("application/x-msaccess", "*.accdb", "*.mdb");

  @Override
  @NonNull
  public List<String> getAcceptedMimeTypes() {
    return ACCEPTED_TYPES;
  }

  @Override
  public boolean canHandle(@NonNull File file) {
    String name = file.getName().toLowerCase();
    return name.endsWith(".accdb") || name.endsWith(".mdb");
  }

  @Override
  public void handle(@NonNull Stage owner, @NonNull ProjectItem targetFolder, @NonNull File file) {
    Optional<ImportFromAccessDialogController.AccessImportInput> input =
        ImportFromAccessDialogController.showWithFile(owner, targetFolder, file);
    if (input.isEmpty()) {
      return;
    }

    ImportFromAccessDialogController.AccessImportInput data = input.get();
    List<ColumnDescriptor> columns = data.columns().stream()
        .map(c -> new ColumnDescriptor(c.name(), toColumnType(c.fieldType())))
        .toList();

    try {
      DocumentModel model = DocumentModelBuilder.build(data.folder(), data.modelName(), data.tableName(), columns);
      ProjectItem item = NewModelFactory.createModelFromExisting(data.folder(), model, data.modelName());
      boolean needsSave = false;
      if (!data.locales().isEmpty()) {
        item.getModel().setLocales(data.locales());
        needsSave = true;
      }
      if (!data.roles().isEmpty()) {
        RolesEditorPanelController.applyRoles(item.getModel(), data.roles());
        needsSave = true;
      }
      if (needsSave) {
        item.save();
      }
    }
    catch (IOException e) {
      log.error("Failed to create document model from dropped Access file '{}': {}",
          file.getName(), e.getMessage(), e);
      WidgetFactory.showAlert(owner, "Error", e.getMessage());
    }
  }

  private static ColumnType toColumnType(AccessImportService.@NonNull ColumnFieldType type) {
    return switch (type) {
      case BOOLEAN   -> ColumnType.BOOLEAN;
      case NUMBER    -> ColumnType.NUMBER;
      case DATE      -> ColumnType.DATE;
      case DATE_TIME -> ColumnType.DATE_TIME;
      default        -> ColumnType.STRING;
    };
  }
}
